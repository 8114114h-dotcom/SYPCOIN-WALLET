package com.sypcoin.wallet.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

class SecureStorage(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context, "sypcoin_secure_prefs", masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        const val KEY_MNEMONIC    = "mnemonic"
        const val KEY_ADDRESS     = "address"
        const val KEY_RPC_URL     = "rpc_url"
        const val KEY_PIN_HASH    = "pin_hash"
        const val DEFAULT_RPC_URL = "http://127.0.0.1:8545"
    }

    fun saveMnemonic(m: String)  = prefs.edit().putString(KEY_MNEMONIC, m).apply()
    fun loadMnemonic(): String?  = prefs.getString(KEY_MNEMONIC, null)
    fun hasMnemonic(): Boolean   = prefs.contains(KEY_MNEMONIC)
    fun saveAddress(a: String)   = prefs.edit().putString(KEY_ADDRESS, a).apply()
    fun loadAddress(): String?   = prefs.getString(KEY_ADDRESS, null)
    fun saveRpcUrl(u: String)    = prefs.edit().putString(KEY_RPC_URL, u).apply()
    fun loadRpcUrl(): String     = prefs.getString(KEY_RPC_URL, DEFAULT_RPC_URL) ?: DEFAULT_RPC_URL

    // ── PIN ───────────────────────────────────────────────────────────────────
    fun savePin(pin: String) {
        val hash = sha256(pin)
        prefs.edit().putString(KEY_PIN_HASH, hash).apply()
    }

    fun verifyPin(pin: String): Boolean {
        val stored = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return sha256(pin) == stored
    }

    fun hasPin(): Boolean = prefs.contains(KEY_PIN_HASH)

    fun clear() = prefs.edit().clear().apply()

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
