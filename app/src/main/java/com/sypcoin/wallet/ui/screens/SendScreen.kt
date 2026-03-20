package com.sypcoin.wallet.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.integration.android.IntentIntegrator
import com.sypcoin.wallet.ui.theme.*
import com.sypcoin.wallet.ui.viewmodel.SendUiState

@Composable
fun SendScreen(
    state:           SendUiState,
    onAddressChange: (String) -> Unit,
    onAmountChange:  (String) -> Unit,
    onSend:          () -> Unit,
    onBack:          () -> Unit,
    onReset:         () -> Unit
) {
    val context = LocalContext.current

    // QR scanner launcher
    val qrLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = IntentIntegrator.parseActivityResult(
                result.resultCode, result.data)
            scanResult?.contents?.let { onAddressChange(it) }
        }
    }

    // Success dialog
    if (state.success && state.txId != null) {
        AlertDialog(
            onDismissRequest = { onReset(); onBack() },
            containerColor   = SypCard,
            icon = { Text("✅", fontSize = 32.sp) },
            title = {
                Text("تم الإرسال بنجاح", color = SypSuccess,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            },
            text = {
                Column {
                    Text("تمت معالجة معاملتك على شبكة Sypcoin",
                        color = SypTextSecond, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("رقم المعاملة:", color = SypTextSecond, fontSize = 12.sp)
                    Text(state.txId, fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp, color = SypTextPrimary)
                }
            },
            confirmButton = {
                Button(onClick = { onReset(); onBack() },
                    colors = ButtonDefaults.buttonColors(containerColor = SypGold)) {
                    Text("تم", color = SypDarkBg, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = SypDarkBg) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {

            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = SypTextPrimary)
                }
                Spacer(Modifier.width(8.dp))
                Text("إرسال SYP", fontSize = 22.sp,
                    fontWeight = FontWeight.Bold, color = SypTextPrimary)
            }

            Spacer(Modifier.height(28.dp))

            // Address field
            Text("عنوان المستلم", fontSize = 13.sp, color = SypTextSecond)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value         = state.toAddress,
                onValueChange = onAddressChange,
                modifier      = Modifier.fillMaxWidth(),
                placeholder   = { Text("0x...", color = SypTextSecond) },
                textStyle     = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 13.sp, color = SypTextPrimary
                ),
                shape  = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = if (state.addressValid) SypSuccess else SypGold,
                    unfocusedBorderColor = SypCard,
                    cursorColor          = SypGold
                ),
                trailingIcon = {
                    Row {
                        // QR scan button
                        IconButton(onClick = {
                            val integrator = IntentIntegrator(context as Activity)
                            integrator.setPrompt("امسح QR Code")
                            integrator.setBeepEnabled(false)
                            qrLauncher.launch(integrator.createScanIntent())
                        }) {
                            Icon(Icons.Default.QrCodeScanner, null,
                                tint = SypGold, modifier = Modifier.size(22.dp))
                        }
                        if (state.toAddress.isNotEmpty()) {
                            Icon(
                                if (state.addressValid) Icons.Default.CheckCircle else Icons.Default.Error,
                                null,
                                tint = if (state.addressValid) SypSuccess else SypError,
                                modifier = Modifier.size(20.dp).padding(end = 8.dp)
                            )
                        }
                    }
                }
            )

            Spacer(Modifier.height(20.dp))

            // Amount field
            Text("المبلغ (SYP)", fontSize = 13.sp, color = SypTextSecond)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value         = state.amount,
                onValueChange = onAmountChange,
                modifier      = Modifier.fillMaxWidth(),
                placeholder   = { Text("0.000000", color = SypTextSecond) },
                textStyle     = LocalTextStyle.current.copy(
                    fontSize = 24.sp, color = SypTextPrimary, fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape  = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = SypGold,
                    unfocusedBorderColor = SypCard,
                    cursorColor          = SypGold
                ),
                suffix = { Text("SYP", color = SypTextSecond) }
            )

            // Fee
            Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.End) {
                Text("رسوم الشبكة: 0.001000 SYP",
                    fontSize = 12.sp, color = SypTextSecond)
            }

            // Error
            if (state.error != null) {
                Spacer(Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = SypError.copy(0.12f)),
                    shape = RoundedCornerShape(10.dp)) {
                    Row(modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, null,
                            tint = SypError, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(state.error, fontSize = 13.sp, color = SypError)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Send button
            Button(
                onClick  = onSend,
                enabled  = !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = SypGold)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp),
                        color = SypDarkBg, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Send, null,
                        tint = SypDarkBg, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("إرسال", fontSize = 17.sp,
                        fontWeight = FontWeight.Bold, color = SypDarkBg)
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("المعاملات على شبكة Sypcoin لا يمكن التراجع عنها",
                fontSize = 12.sp, color = SypTextSecond,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}
