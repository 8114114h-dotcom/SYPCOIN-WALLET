package com.sypcoin.wallet.wallet

import android.content.Context
import com.sypcoin.wallet.crypto.AddressUtils
import com.sypcoin.wallet.crypto.KeyManager
import com.sypcoin.wallet.crypto.Mnemonic
import com.sypcoin.wallet.network.RpcClient
import com.sypcoin.wallet.network.TxInfo
import com.sypcoin.wallet.storage.SecureStorage

/**
 * Central wallet repository — orchestrates crypto, storage, and network.
 */
class WalletRepository(context: Context) {

    val storage   = SecureStorage(context)
    val rpcClient = RpcClient(storage.loadRpcUrl())

    // ── Wallet setup ──────────────────────────────────────────────────────────

    /** Create a new wallet. Returns the mnemonic phrase for backup. */
    fun createWallet(): String {
        val mnemonic = Mnemonic.generate()
        val seed     = Mnemonic.toSeed(mnemonic)
        val keypair  = KeyManager.deriveKeypair(seed, index = 0)
        val address  = AddressUtils.fromPublicKey(keypair.publicKey)

        storage.saveMnemonic(mnemonic)
        storage.saveAddress(address)

        // Wipe private key from memory after deriving address.
        keypair.wipe()

        return mnemonic
    }

    /** Restore wallet from a mnemonic phrase. Returns true on success. */
    fun restoreWallet(phrase: String): Boolean {
        if (!Mnemonic.validate(phrase)) return false

        val seed    = Mnemonic.toSeed(phrase)
        val keypair = KeyManager.deriveKeypair(seed, index = 0)
        val address = AddressUtils.fromPublicKey(keypair.publicKey)

        storage.saveMnemonic(phrase)
        storage.saveAddress(address)
        keypair.wipe()

        return true
    }

    /** Returns true if a wallet is already set up on this device. */
    fun isWalletSetup(): Boolean = storage.hasMnemonic()

    /** The wallet's primary address. */
    fun getAddress(): String = storage.loadAddress() ?: ""

    // ── Network operations ────────────────────────────────────────────────────

    /** Fetch current balance from the node. */
    suspend fun getBalance(): String = rpcClient.getBalance(getAddress())

    /** Fetch current nonce from the node. */
    suspend fun getNonce(): Long = rpcClient.getNonce(getAddress())

    /** Fetch chain height. */
    suspend fun getChainHeight(): Long = rpcClient.getBlockHeight()

    /** Fetch a transaction by ID. */
    suspend fun getTransaction(txId: String): TxInfo = rpcClient.getTransaction(txId)

    // ── Send transaction ──────────────────────────────────────────────────────

    /**
     * Build, sign, and broadcast a transaction.
     *
     * Returns the tx_id on success.
     */
    suspend fun sendTransaction(
        toAddress: String,
        amountSyp: String,
        feeMicro:  Long = 1_000L
    ): String {
        val mnemonic = storage.loadMnemonic()
            ?: error("No wallet found")

        val seed    = Mnemonic.toSeed(mnemonic)
        val keypair = KeyManager.deriveKeypair(seed, index = 0)
        val nonce   = rpcClient.getNonce(getAddress())

        val txHex = TransactionSigner.buildAndSign(
            keypair    = keypair,
            toAddress  = toAddress,
            amountSyp  = amountSyp,
            feeMicro   = feeMicro,
            nonce      = nonce + 1,
            chainId    = 1L
        )

        keypair.wipe()

        return rpcClient.sendTransaction(txHex)
    }

    // ── Settings ──────────────────────────────────────────────────────────────

    fun setRpcUrl(url: String) {
        storage.saveRpcUrl(url)
        rpcClient.setUrl(url)
    }

    fun getRpcUrl(): String = storage.loadRpcUrl()

    /** Delete the wallet from this device. */
    fun deleteWallet() = storage.clear()
}
