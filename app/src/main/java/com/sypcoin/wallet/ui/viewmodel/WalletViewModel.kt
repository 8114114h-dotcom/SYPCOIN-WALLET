package com.sypcoin.wallet.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sypcoin.wallet.network.TxInfo
import com.sypcoin.wallet.wallet.WalletRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val address:      String       = "",
    val balance:      String       = "---",
    val balanceUsd:   String       = "",
    val chainHeight:  Long         = 0L,
    val isLoading:    Boolean      = false,
    val error:        String?      = null,
    val isConnected:  Boolean      = false,
    val transactions: List<TxInfo> = emptyList()
)

data class SendUiState(
    val toAddress:  String  = "",
    val amount:     String  = "",
    val txId:       String? = null,
    val isLoading:  Boolean = false,
    val error:      String? = null,
    val success:    Boolean = false,
    val addressValid: Boolean = false
)

data class PinUiState(
    val pin:         String  = "",
    val confirmed:   String  = "",
    val error:       String? = null,
    val isSetup:     Boolean = false
)

class WalletViewModel(app: Application) : AndroidViewModel(app) {

    val repository = WalletRepository(app)

    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    private val _sendState = MutableStateFlow(SendUiState())
    val sendState: StateFlow<SendUiState> = _sendState.asStateFlow()

    private val _mnemonic = MutableStateFlow<String?>(null)
    val mnemonic: StateFlow<String?> = _mnemonic.asStateFlow()

    private val _pinState = MutableStateFlow(PinUiState(isSetup = repository.storage.hasPin()))
    val pinState: StateFlow<PinUiState> = _pinState.asStateFlow()

    private val _isUnlocked = MutableStateFlow(!repository.storage.hasPin())
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    fun isWalletSetup() = repository.isWalletSetup()

    // ── PIN ───────────────────────────────────────────────────────────────────
    fun verifyPin(input: String): Boolean {
        val ok = repository.storage.verifyPin(input)
        if (ok) _isUnlocked.value = true
        return ok
    }

    fun setupPin(pin: String, confirm: String): Boolean {
        if (pin.length < 4) { _pinState.update { it.copy(error = "الرقم السري 4 أرقام على الأقل") }; return false }
        if (pin != confirm)  { _pinState.update { it.copy(error = "الرقمان لا يتطابقان") }; return false }
        repository.storage.savePin(pin)
        _pinState.value = PinUiState(isSetup = true)
        _isUnlocked.value = true
        return true
    }

    // ── Wallet ────────────────────────────────────────────────────────────────
    fun createWallet() {
        val phrase = repository.createWallet()
        _mnemonic.value = phrase
        loadHomeData()
    }

    fun restoreWallet(phrase: String): Boolean {
        val ok = repository.restoreWallet(phrase)
        if (ok) loadHomeData()
        return ok
    }

    fun clearMnemonicDisplay() { _mnemonic.value = null }

    // ── Home ──────────────────────────────────────────────────────────────────
    fun loadHomeData() {
        val address = repository.getAddress()
        _homeState.update { it.copy(address = address, isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val balance = repository.getBalance()
                val height  = repository.getChainHeight()
                _homeState.update {
                    it.copy(
                        balance     = "$balance SYP",
                        chainHeight = height,
                        isLoading   = false,
                        isConnected = true,
                        error       = null
                    )
                }
            } catch (e: Exception) {
                _homeState.update {
                    it.copy(isLoading = false, isConnected = false,
                        error = "تعذّر الاتصال بالعقدة: ${e.message}")
                }
            }
        }
    }

    // ── Send ──────────────────────────────────────────────────────────────────
    fun updateToAddress(v: String) {
        val valid = v.length == 42 && v.startsWith("0x")
        _sendState.update { it.copy(toAddress = v, error = null, addressValid = valid) }
    }

    fun updateAmount(v: String) { _sendState.update { it.copy(amount = v, error = null) } }

    fun sendTransaction() {
        val state = _sendState.value
        when {
            !state.addressValid   -> { _sendState.update { it.copy(error = "العنوان غير صحيح") }; return }
            state.amount.isBlank()-> { _sendState.update { it.copy(error = "أدخل المبلغ") }; return }
            state.amount.toDoubleOrNull() == null -> { _sendState.update { it.copy(error = "مبلغ غير صالح") }; return }
        }
        _sendState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val txId = repository.sendTransaction(state.toAddress.trim(), state.amount.trim())
                _sendState.update { it.copy(isLoading = false, txId = txId, success = true) }
                loadHomeData()
            } catch (e: Exception) {
                _sendState.update { it.copy(isLoading = false, error = e.message ?: "فشل الإرسال") }
            }
        }
    }

    fun resetSendState() { _sendState.value = SendUiState() }

    // ── Settings ──────────────────────────────────────────────────────────────
    fun setRpcUrl(url: String) { repository.setRpcUrl(url); loadHomeData() }
    fun getRpcUrl() = repository.getRpcUrl()
    fun deleteWallet() { repository.deleteWallet() }
}
