package com.sypcoin.wallet.wallet

import com.sypcoin.wallet.crypto.KeyManager
import com.sypcoin.wallet.crypto.SypcoinKeypair
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest

/**
 * Builds and signs Sypcoin transactions compatible with the Rust node (bincode format).
 *
 * The Rust node uses bincode::deserialize<Transaction> which expects:
 *   tx_id        : [u8; 32]   — SHA-256 of the payload
 *   from         : [u8; 20]   — sender address
 *   to           : [u8; 20]   — recipient address
 *   public_key   : [u8; 32]   — sender public key
 *   signature    : [u8; 64]   — Ed25519 signature
 *   amount       : u64 LE
 *   fee          : u64 LE
 *   nonce        : u64 LE
 *   timestamp    : u64 LE
 *   chain_id     : u64 LE
 *   version      : u8
 *   data         : Option<Vec<u8>> — None = [0u8] in bincode
 *
 * Pre-image for signing (matches Rust to_bytes_for_signing):
 *   pubkey(32) | to(20) | amount(8) | fee(8) | nonce(8) | chain_id(8) | timestamp(8)
 *
 * Signature = Ed25519( SHA-256("SYPCOIN_TX_V1" || nonce_le8 || pre_image) )
 */
object TransactionSigner {

    private const val DOMAIN       = "SYPCOIN_TX_V1"
    private const val MIN_FEE_MICRO = 1_000L
    private const val TX_VERSION   = 1.toByte()
    private const val CHAIN_ID     = 1L

    /**
     * Build, sign, and return the transaction as a hex-encoded bincode blob.
     */
    fun buildAndSign(
        keypair:   SypcoinKeypair,
        toAddress: String,
        amountSyp: String,
        feeMicro:  Long = MIN_FEE_MICRO,
        nonce:     Long,
        chainId:   Long = CHAIN_ID
    ): String {
        require(feeMicro >= MIN_FEE_MICRO) { "Fee below minimum: $feeMicro < $MIN_FEE_MICRO" }

        val amountMicro = displayToMicro(amountSyp)
        require(amountMicro > 0) { "Amount must be > 0" }

        val fromBytes   = pubkeyToAddress(keypair.publicKey)
        val toBytes     = addressToBytes(toAddress)
        val timestampMs = System.currentTimeMillis()

        // ── Pre-image (matches Rust to_bytes_for_signing) ─────────────────────
        val preImagePayload = buildPreImage(
            pubkey      = keypair.publicKey,
            toAddr      = toBytes,
            amountMicro = amountMicro,
            feeMicro    = feeMicro,
            nonce       = nonce,
            chainId     = chainId,
            timestampMs = timestampMs
        )

        // ── Sign: Ed25519(SHA-256("SYPCOIN_TX_V1" || nonce_le8 || pre_image)) ─
        val nonceBytes = nonce.toLeBytes()
        val digest     = sha256(DOMAIN.toByteArray() + nonceBytes + preImagePayload)
        val signature  = KeyManager.sign(keypair.privateKey, digest)

        // ── tx_id = SHA-256(pre_image || signature) ───────────────────────────
        val txId = sha256(preImagePayload + signature)

        // ── Encode as bincode-compatible binary ───────────────────────────────
        val wire = encodeBincode(
            txId        = txId,
            from        = fromBytes,
            to          = toBytes,
            publicKey   = keypair.publicKey,
            signature   = signature,
            amountMicro = amountMicro,
            feeMicro    = feeMicro,
            nonce       = nonce,
            timestampMs = timestampMs,
            chainId     = chainId
        )

        return wire.toHex()
    }

    /**
     * Encode to bincode format matching Rust Transaction struct field order.
     *
     * Bincode encodes structs field-by-field in declaration order:
     *   tx_id(32) | from(20) | to(20) | public_key(32) | signature(64) |
     *   amount(8) | fee(8) | nonce(8) | timestamp(8) | chain_id(8) |
     *   version(1) | data(Option::None = 0u8)
     *
     * Fixed arrays [u8; N] are encoded as N raw bytes (no length prefix).
     * u64 is little-endian 8 bytes.
     * Option::None is encoded as 0u8.
     */
    private fun encodeBincode(
        txId:        ByteArray,
        from:        ByteArray,
        to:          ByteArray,
        publicKey:   ByteArray,
        signature:   ByteArray,
        amountMicro: Long,
        feeMicro:    Long,
        nonce:       Long,
        timestampMs: Long,
        chainId:     Long
    ): ByteArray {
        // 32+20+20+32+64+8+8+8+8+8+1+1 = 210 bytes
        val buf = ByteBuffer.allocate(210).order(ByteOrder.LITTLE_ENDIAN)
        buf.put(txId)                    // tx_id:      [u8; 32]
        buf.put(from)                    // from:       [u8; 20]
        buf.put(to)                      // to:         [u8; 20]
        buf.put(publicKey)               // public_key: [u8; 32]
        buf.put(signature)               // signature:  [u8; 64]
        buf.putLong(amountMicro)         // amount:     u64 LE
        buf.putLong(feeMicro)            // fee:        u64 LE
        buf.putLong(nonce)               // nonce:      u64 LE
        buf.putLong(timestampMs)         // timestamp:  u64 LE
        buf.putLong(chainId)             // chain_id:   u64 LE
        buf.put(TX_VERSION)              // version:    u8
        buf.put(0.toByte())              // data:       Option::None
        return buf.array()
    }

    /** Pre-image matches Rust to_bytes_for_signing() exactly. */
    private fun buildPreImage(
        pubkey:      ByteArray,
        toAddr:      ByteArray,
        amountMicro: Long,
        feeMicro:    Long,
        nonce:       Long,
        chainId:     Long,
        timestampMs: Long
    ): ByteArray {
        val buf = ByteBuffer.allocate(92).order(ByteOrder.LITTLE_ENDIAN)
        buf.put(pubkey)              // 32
        buf.put(toAddr)              // 20
        buf.putLong(amountMicro)     // 8
        buf.putLong(feeMicro)        // 8
        buf.putLong(nonce)           // 8
        buf.putLong(chainId)         // 8
        buf.putLong(timestampMs)     // 8
        return buf.array()
    }

    /** Derive 20-byte address from 32-byte Ed25519 public key (last 20 bytes of SHA-256). */
    private fun pubkeyToAddress(pubkey: ByteArray): ByteArray {
        val hash = sha256(pubkey)
        return hash.copyOfRange(hash.size - 20, hash.size)
    }

    /** Decode checksum-hex address ("0x...") to 20 bytes. */
    private fun addressToBytes(address: String): ByteArray {
        val hex = address.removePrefix("0x").lowercase()
        require(hex.length == 40) { "Invalid address: $address" }
        return hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

    /** Convert "10.500000" → 10_500_000L */
    fun displayToMicro(display: String): Long {
        val parts = display.trim().split(".")
        val whole = parts[0].toLongOrNull() ?: return 0L
        val frac  = if (parts.size < 2) 0L else {
            parts[1].padEnd(6, '0').take(6).toLongOrNull() ?: 0L
        }
        return whole * 1_000_000L + frac
    }

    /** Convert 10_500_000L → "10.500000" */
    fun microToDisplay(micro: Long): String {
        val whole = micro / 1_000_000L
        val frac  = micro % 1_000_000L
        return "$whole.${frac.toString().padStart(6, '0')}"
    }

    private fun sha256(input: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(input)

    private fun Long.toLeBytes(): ByteArray =
        ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(this).array()

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
