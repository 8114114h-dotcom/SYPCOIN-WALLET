package com.sypcoin.wallet.crypto

import java.security.MessageDigest

/**
 * Sypcoin address derivation and checksum validation.
 *
 * address = SHA-256("SYPCOIN_ADDR_V1" || pubkey_bytes)[0..20]
 * displayed as EIP-55-style mixed-case checksum hex with "0x" prefix.
 */
object AddressUtils {

    /** Derive a Sypcoin address from a 32-byte Ed25519 public key. */
    fun fromPublicKey(publicKey: ByteArray): String {
        val pre = "SYPCOIN_ADDR_V1".toByteArray() + publicKey
        val digest = sha256(pre)
        val addrBytes = digest.copyOf(20)
        return toChecksumHex(addrBytes)
    }

    /** Encode 20 bytes as EIP-55-style checksum hex. */
    fun toChecksumHex(bytes: ByteArray): String {
        require(bytes.size == 20) { "Address must be 20 bytes" }
        val lowerHex  = bytes.joinToString("") { "%02x".format(it) }
        val checksum  = sha256(lowerHex.toByteArray())

        val result = StringBuilder("0x")
        lowerHex.forEachIndexed { i, ch ->
            val byteIdx = i / 2
            val bitSet  = if (i % 2 == 0) {
                checksum[byteIdx].toInt() and 0x80 != 0
            } else {
                checksum[byteIdx].toInt() and 0x08 != 0
            }
            result.append(if (bitSet && ch.isLetter()) ch.uppercaseChar() else ch)
        }
        return result.toString()
    }

    /** Validate a checksum hex address. */
    fun isValid(address: String): Boolean {
        if (!address.startsWith("0x") || address.length != 42) return false
        val hexPart = address.removePrefix("0x")
        if (!hexPart.all { it.isLetterOrDigit() }) return false
        return try {
            val bytes = hexPart.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            toChecksumHex(bytes) == address
        } catch (e: Exception) {
            false
        }
    }

    /** Shorten address for display: "0x1234...abcd". */
    fun shorten(address: String): String {
        if (address.length < 12) return address
        return "${address.take(8)}...${address.takeLast(6)}"
    }

    private fun sha256(input: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(input)
}
