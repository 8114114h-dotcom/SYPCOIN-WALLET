package com.sypcoin.wallet.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.sypcoin.wallet.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sypcoin.wallet.crypto.AddressUtils
import com.sypcoin.wallet.ui.theme.*
import com.sypcoin.wallet.ui.viewmodel.HomeUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinDetailScreen(
    state:     HomeUiState,
    onBack:    () -> Unit,
    onSend:    () -> Unit,
    onReceive: () -> Unit
) {
    val clipboard  = LocalClipboardManager.current
    var selectedTx by remember { mutableStateOf<com.sypcoin.wallet.network.TxInfo?>(null) }
    var showSwapMsg by remember { mutableStateOf(false) }

    selectedTx?.let { tx ->
        TxDetailDialog(
            tx          = tx,
            chainHeight = state.chainHeight,
            onDismiss   = { selectedTx = null }
        )
    }

    if (showSwapMsg) {
        AlertDialog(
            onDismissRequest = { showSwapMsg = false },
            containerColor   = SypCard,
            title = { Text("مبادلة", color = SypTextPrimary, fontWeight = FontWeight.Bold) },
            text  = { Text("قريباً — ميزة المبادلة قيد التطوير", color = SypTextSecond) },
            confirmButton = {
                TextButton(onClick = { showSwapMsg = false }) {
                    Text("حسناً", color = SypGold)
                }
            }
        )
    }

    Scaffold(
        containerColor = SypDarkBg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SypDarkBg),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = SypTextPrimary)
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.syp_logo),
                            contentDescription = "SYP",
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Sypcoin", fontSize = 16.sp,
                                fontWeight = FontWeight.Bold, color = SypTextPrimary)
                            Text("SYP", fontSize = 11.sp, color = SypTextSecond)
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier            = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Balance
            item {
                Spacer(Modifier.height(24.dp))
                Text(state.balance,
                    fontSize = 34.sp, fontWeight = FontWeight.Black, color = SypTextPrimary)
                Spacer(Modifier.height(4.dp))
                Text("\$0.00", fontSize = 16.sp, color = SypTextSecond)
                Spacer(Modifier.height(4.dp))

                // Address chip
                Surface(
                    shape    = RoundedCornerShape(8.dp),
                    color    = SypSurface,
                    modifier = Modifier.clickable {
                        clipboard.setText(AnnotatedString(state.address))
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ContentCopy, null,
                            tint = SypGold, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(AddressUtils.shorten(state.address),
                            fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                            color = SypTextSecond)
                    }
                }
                Spacer(Modifier.height(28.dp))
            }

            // Action buttons
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CoinActionBtn(
                        icon     = Icons.Default.ArrowUpward,
                        label    = "إرسال",
                        color    = SypGold,
                        modifier = Modifier.weight(1f),
                        onClick  = onSend
                    )
                    CoinActionBtn(
                        icon     = Icons.Default.ArrowDownward,
                        label    = "استقبال",
                        color    = SypBlue,
                        modifier = Modifier.weight(1f),
                        onClick  = onReceive
                    )
                    CoinActionBtn(
                        icon     = Icons.Default.SwapHoriz,
                        label    = "مبادلة",
                        color    = SypTextSecond,
                        modifier = Modifier.weight(1f),
                        onClick  = { showSwapMsg = true }
                    )
                }
                Spacer(Modifier.height(28.dp))
            }

            // Divider + transactions header
            item {
                Divider(
                    modifier  = Modifier.padding(horizontal = 20.dp),
                    color     = SypSurface,
                    thickness = 1.dp
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("المعاملات",
                        fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SypTextPrimary)
                    Text("كتلة #${state.chainHeight}",
                        fontSize = 12.sp, color = SypTextSecond)
                }
                Spacer(Modifier.height(8.dp))
            }

            // Transactions
            if (state.transactions.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📭", fontSize = 40.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("لا توجد معاملات بعد",
                                fontSize = 14.sp, color = SypTextSecond)
                        }
                    }
                }
            } else {
                items(state.transactions.size) { i ->
                    val tx = state.transactions[i]
                    TxItem(
                        tx          = tx,
                        myAddress   = state.address,
                        chainHeight = state.chainHeight,
                        onClick     = { selectedTx = tx }
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun CoinActionBtn(
    icon:     ImageVector,
    label:    String,
    color:    androidx.compose.ui.graphics.Color,
    modifier: Modifier,
    onClick:  () -> Unit
) {
    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilledIconButton(
            onClick  = onClick,
            modifier = Modifier.size(52.dp),
            colors   = IconButtonDefaults.filledIconButtonColors(
                containerColor = SypCard
            )
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 12.sp, color = SypTextSecond)
    }
}
