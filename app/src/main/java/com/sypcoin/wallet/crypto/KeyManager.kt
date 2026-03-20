package com.sypcoin.wallet.crypto

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.security.MessageDigest

/**
 * Manages Ed25519 keypair generation and signing for Sypcoin.
 *
 * Key derivation:
 *   child_seed(index) = SHA-256("SYPCOIN_HD_V1" || master_seed || index_le4)
 *   keypair           = Ed25519(child_seed)
 */
object KeyManager {

    /** Derive an Ed25519 keypair at the given HD index from a master seed. */
    fun deriveKeypair(masterSeed: ByteArray, index: Int = 0): SypcoinKeypair {
        val childSeed = deriveChildSeed(masterSeed, index)
        val privateKey = Ed25519PrivateKeyParameters(childSeed, 0)
        val publicKey  = privateKey.generatePublicKey()
        return SypcoinKeypair(
            privateKey  = childSeed,
            publicKey   = publicKey.encoded,
            index       = index
        )
    }

    /** Sign a message (pre-hashed payload) with an Ed25519 private key. */
    fun sign(privateKeyBytes: ByteArray, message: ByteArray): ByteArray {
        val privateKey = Ed25519PrivateKeyParameters(privateKeyBytes, 0)
        val signer     = Ed25519Signer()
        signer.init(true, privateKey)
        signer.update(message, 0, message.size)
        return signer.generateSignature()
    }

    /** Verify an Ed25519 signature. */
    fun verify(publicKeyBytes: ByteArray, message: ByteArray, signature: ByteArray): Boolean {
        return try {
            val publicKey = Ed25519PublicKeyParameters(publicKeyBytes, 0)
            val verifier  = Ed25519Signer()
            verifier.init(false, publicKey)
            verifier.update(message, 0, message.size)
            verifier.verifySignature(signature)
        } catch (e: Exception) {
            false
        }
    }

    /** Derive a 32-byte child seed for HD index. */
    private fun deriveChildSeed(masterSeed: ByteArray, index: Int): ByteArray {
        val pre = "SYPCOIN_HD_V1".toByteArray() +
                  masterSeed +
                  index.toLittleEndianBytes()
        return sha256(pre)
    }

    private fun sha256(input: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(input)

    private fun Int.toLittleEndianBytes(): ByteArray {
        return byteArrayOf(
            (this and 0xFF).toByte(),
            ((this shr 8) and 0xFF).toByte(),
            ((this shr 16) and 0xFF).toByte(),
            ((this shr 24) and 0xFF).toByte()
        )
    }
}

/** An Ed25519 keypair for Sypcoin. */
data class SypcoinKeypair(
    val privateKey: ByteArray,   // 32 bytes — never store unencrypted
    val publicKey:  ByteArray,   // 32 bytes
    val index:      Int
) {
    /** Wipe private key bytes from memory. */
    fun wipe() {
        privateKey.fill(0)
    }
}
