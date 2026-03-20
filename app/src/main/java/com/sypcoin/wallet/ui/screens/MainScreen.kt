package com.sypcoin.wallet.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sypcoin.wallet.crypto.AddressUtils
import com.sypcoin.wallet.ui.theme.*
import com.sypcoin.wallet.ui.viewmodel.HomeUiState

@Composable
fun MainScreen(
    state:       HomeUiState,
    onRefresh:   () -> Unit,
    onSettings:  () -> Unit,
    onCoinClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = SypDarkBg,
        topBar = {
            MainTopBar(
                isConnected = state.isConnected,
                isLoading   = state.isLoading,
                onSettings  = onSettings,
                onRefresh   = onRefresh
            )
        },
        bottomBar = {
            MainBottomNav(
                selected  = selectedTab,
                onSelect  = { selectedTab = it }
            )
        }
    ) { padding ->
        when (selectedTab) {
            0 -> HomeTab(
                state       = state,
                padding     = padding,
                onCoinClick = onCoinClick
            )
            1 -> HistoryTab(
                state   = state,
                padding = padding
            )
        }
    }
}

// ── Top Bar ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
    isConnected: Boolean,
    isLoading:   Boolean,
    onSettings:  () -> Unit,
    onRefresh:   () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SypDarkBg),
        title  = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.syp_logo),
                    contentDescription = "SYP",
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Sypcoin", fontSize = 18.sp,
                    fontWeight = FontWeight.Bold, color = SypGold)
            }
        },
        actions = {
            // Connection dot
            Surface(
                shape    = CircleShape,
                color    = if (isConnected) SypSuccess else SypError,
                modifier = Modifier.size(8.dp).align(Alignment.CenterVertically)
            ) {}
            Spacer(Modifier.width(8.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(18.dp).align(Alignment.CenterVertically),
                    color       = SypGold,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(4.dp))
            }

            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, null, tint = SypTextSecond)
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Default.Settings, null, tint = SypTextSecond)
            }
        }
    )
}

// ── Bottom Nav ────────────────────────────────────────────────────────────────

@Composable
private fun MainBottomNav(selected: Int, onSelect: (Int) -> Unit) {
    NavigationBar(containerColor = SypSurface) {
        NavigationBarItem(
            selected = selected == 0,
            onClick  = { onSelect(0) },
            icon     = { Icon(Icons.Default.Home, null) },
            label    = { Text("الرئيسية", fontSize = 11.sp) },
            colors   = NavigationBarItemDefaults.colors(
                selectedIconColor   = SypGold,
                selectedTextColor   = SypGold,
                unselectedIconColor = SypTextSecond,
                unselectedTextColor = SypTextSecond,
                indicatorColor      = SypCard
            )
        )
        NavigationBarItem(
            selected = selected == 1,
            onClick  = { onSelect(1) },
            icon     = { Icon(Icons.Default.History, null) },
            label    = { Text("السجل", fontSize = 11.sp) },
            colors   = NavigationBarItemDefaults.colors(
                selectedIconColor   = SypGold,
                selectedTextColor   = SypGold,
                unselectedIconColor = SypTextSecond,
                unselectedTextColor = SypTextSecond,
                indicatorColor      = SypCard
            )
        )
    }
}

// ── Home Tab ──────────────────────────────────────────────────────────────────

@Composable
private fun HomeTab(
    state:       HomeUiState,
    padding:     PaddingValues,
    onCoinClick: () -> Unit
) {
    LazyColumn(
        modifier            = Modifier
            .fillMaxSize()
            .padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Total balance header
        item {
            Spacer(Modifier.height(24.dp))
            Text(
                "الرصيد الكلي",
                fontSize = 13.sp,
                color    = SypTextSecond
            )
            Spacer(Modifier.height(6.dp))
            Text(
                state.balance,
                fontSize   = 32.sp,
                fontWeight = FontWeight.Black,
                color      = SypTextPrimary
            )
            Text(
                "\$0.00",
                fontSize = 14.sp,
                color    = SypTextSecond
            )
            Spacer(Modifier.height(32.dp))
        }

        // Section header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "العملات",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = SypTextPrimary
                )
                Text(
                    "كتلة #${state.chainHeight}",
                    fontSize = 12.sp,
                    color    = SypTextSecond
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        // SYP coin row
        item {
            CoinRow(
                name        = "Sypcoin",
                symbol      = "SYP",
                balance     = state.balance,
                usdValue    = "\$0.00",
                isConnected = state.isConnected,
                onClick     = onCoinClick
            )
        }

        // Error
        if (state.error != null) {
            item {
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SypError.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier          = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null,
                            tint = SypError, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(state.error, fontSize = 12.sp, color = SypError)
                    }
                }
            }
        }
    }
}

@Composable
private fun CoinRow(
    name:        String,
    symbol:      String,
    balance:     String,
    usdValue:    String,
    isConnected: Boolean,
    onClick:     () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SypCard)
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Coin icon
            Image(
                painter = painterResource(R.drawable.syp_logo),
                contentDescription = "SYP",
                modifier = Modifier.size(44.dp)
            )

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold, color = SypTextPrimary)
                Text(symbol, fontSize = 12.sp, color = SypTextSecond)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(balance, fontSize = 14.sp,
                    fontWeight = FontWeight.Bold, color = SypTextPrimary)
                Text(usdValue, fontSize = 12.sp, color = SypTextSecond)
            }

            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, null,
                tint = SypTextSecond, modifier = Modifier.size(18.dp))
        }
    }
}

// ── History Tab ───────────────────────────────────────────────────────────────

@Composable
private fun HistoryTab(
    state:   HomeUiState,
    padding: PaddingValues
) {
    var selectedTx by remember { mutableStateOf<com.sypcoin.wallet.network.TxInfo?>(null) }

    selectedTx?.let { tx ->
        TxDetailDialog(
            tx          = tx,
            chainHeight = state.chainHeight,
            onDismiss   = { selectedTx = null }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        item {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("سجل المعاملات",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SypTextPrimary)
                if (state.transactions.isNotEmpty()) {
                    Text("${state.transactions.size} معاملة",
                        fontSize = 12.sp, color = SypTextSecond)
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        if (state.transactions.isEmpty()) {
            item {
                Box(
                    modifier         = Modifier.fillParentMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("لا توجد معاملات بعد",
                            fontSize = 15.sp, color = SypTextSecond)
                        Spacer(Modifier.height(6.dp))
                        Text("ستظهر معاملاتك هنا",
                            fontSize = 12.sp, color = SypTextSecond)
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

        item { Spacer(Modifier.height(16.dp)) }
    }
}
