package com.sypcoin.wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sypcoin.wallet.ui.screens.*
import com.sypcoin.wallet.ui.theme.SypcoinTheme
import com.sypcoin.wallet.ui.viewmodel.WalletViewModel

object Routes {
    const val WELCOME        = "welcome"
    const val CREATE_WALLET  = "create_wallet"
    const val RESTORE_WALLET = "restore_wallet"
    const val MAIN           = "main"
    const val COIN_DETAIL    = "coin_detail"
    const val SEND           = "send"
    const val RECEIVE        = "receive"
    const val SETTINGS       = "settings"
    const val PIN_SETUP      = "pin_setup"
    const val PIN_LOCK       = "pin_lock"
}

class MainActivity : ComponentActivity() {
    private val viewModel: WalletViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SypcoinTheme { SypcoinApp(viewModel) } }
    }
}

@Composable
fun SypcoinApp(vm: WalletViewModel) {
    val nav        = rememberNavController()
    val homeState  by vm.homeState.collectAsState()
    val sendState  by vm.sendState.collectAsState()
    val mnemonic   by vm.mnemonic.collectAsState()
    val isUnlocked by vm.isUnlocked.collectAsState()
    val pinState   by vm.pinState.collectAsState()

    val startDest = when {
        !vm.isWalletSetup()             -> Routes.WELCOME
        pinState.isSetup && !isUnlocked -> Routes.PIN_LOCK
        else                            -> Routes.MAIN
    }

    LaunchedEffect(Unit) {
        if (vm.isWalletSetup() && isUnlocked) vm.loadHomeData()
    }

    NavHost(navController = nav, startDestination = startDest) {

        composable(Routes.WELCOME) {
            WelcomeScreen(
                onCreateWallet  = { vm.createWallet(); nav.navigate(Routes.CREATE_WALLET) },
                onRestoreWallet = { nav.navigate(Routes.RESTORE_WALLET) }
            )
        }

        composable(Routes.CREATE_WALLET) {
            mnemonic?.let { phrase ->
                CreateWalletScreen(mnemonic = phrase, onContinue = {
                    vm.clearMnemonicDisplay()
                    nav.navigate(Routes.PIN_SETUP) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                })
            }
        }

        composable(Routes.PIN_SETUP) {
            var firstPin by remember { mutableStateOf("") }
            var step     by remember { mutableIntStateOf(1) }
            var error    by remember { mutableStateOf<String?>(null) }

            if (step == 1) {
                PinScreen(
                    title    = "أنشئ رقماً سرياً",
                    subtitle = "6 أرقام لحماية محفظتك",
                    error    = error,
                    onPin    = { firstPin = it; step = 2; error = null }
                )
            } else {
                PinScreen(
                    title    = "تأكيد الرقم السري",
                    subtitle = "أعد إدخال الرقم السري",
                    error    = error,
                    onPin    = { confirm ->
                        if (vm.setupPin(firstPin, confirm)) {
                            nav.navigate(Routes.MAIN) {
                                popUpTo(Routes.PIN_SETUP) { inclusive = true }
                            }
                        } else {
                            error = "الرقمان لا يتطابقان"
                            step = 1
                        }
                    }
                )
            }
        }

        composable(Routes.PIN_LOCK) {
            var attempts by remember { mutableIntStateOf(0) }
            PinScreen(
                title    = "أدخل رقمك السري",
                subtitle = if (attempts > 0) "محاولة ${attempts + 1}" else "",
                error    = if (attempts >= 3) "محاولات خاطئة متعددة" else null,
                onPin    = { pin ->
                    if (vm.verifyPin(pin)) {
                        vm.loadHomeData()
                        nav.navigate(Routes.MAIN) {
                            popUpTo(Routes.PIN_LOCK) { inclusive = true }
                        }
                    } else { attempts++ }
                }
            )
        }

        composable(Routes.RESTORE_WALLET) {
            RestoreWalletScreen(
                onRestore = { phrase ->
                    val ok = vm.restoreWallet(phrase)
                    if (ok) nav.navigate(Routes.PIN_SETUP) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                    ok
                },
                onBack = { nav.popBackStack() }
            )
        }

        // Main screen — Trust Wallet style with bottom nav
        composable(Routes.MAIN) {
            MainScreen(
                state      = homeState,
                onRefresh  = vm::loadHomeData,
                onSettings = { nav.navigate(Routes.SETTINGS) },
                onCoinClick = { nav.navigate(Routes.COIN_DETAIL) }
            )
        }

        // Coin detail screen
        composable(Routes.COIN_DETAIL) {
            CoinDetailScreen(
                state     = homeState,
                onBack    = { nav.popBackStack() },
                onSend    = { nav.navigate(Routes.SEND) },
                onReceive = { nav.navigate(Routes.RECEIVE) }
            )
        }

        composable(Routes.SEND) {
            SendScreen(
                state           = sendState,
                onAddressChange = vm::updateToAddress,
                onAmountChange  = vm::updateAmount,
                onSend          = vm::sendTransaction,
                onBack          = { nav.popBackStack() },
                onReset         = vm::resetSendState
            )
        }

        composable(Routes.RECEIVE) {
            ReceiveScreen(address = homeState.address, onBack = { nav.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                currentRpcUrl = vm.getRpcUrl(),
                onRpcUrlSave  = { url -> vm.setRpcUrl(url); nav.popBackStack() },
                onDelete      = {
                    vm.deleteWallet()
                    nav.navigate(Routes.WELCOME) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                },
                onBack = { nav.popBackStack() }
            )
        }
    }
}
