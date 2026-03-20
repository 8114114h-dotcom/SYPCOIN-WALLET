package com.sypcoin.wallet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sypcoin.wallet.ui.theme.*

@Composable
fun SettingsScreen(
    currentRpcUrl: String,
    onRpcUrlSave:  (String) -> Unit,
    onDelete:      () -> Unit,
    onBack:        () -> Unit
) {
    var rpcUrl     by remember { mutableStateOf(currentRpcUrl) }
    var showDelete by remember { mutableStateOf(false) }
    var saved      by remember { mutableStateOf(false) }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            containerColor   = SypCard,
            icon = { Text("⚠️", fontSize = 28.sp) },
            title = { Text("حذف المحفظة؟", color = SypError, fontWeight = FontWeight.Bold) },
            text  = {
                Text("سيتم حذف محفظتك من هذا الجهاز نهائياً. تأكد من حفظ عبارة الاستعادة.",
                    color = SypTextSecond, fontSize = 14.sp)
            },
            confirmButton = {
                Button(onClick = { showDelete = false; onDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = SypError)) {
                    Text("حذف", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) {
                    Text("إلغاء", color = SypGold)
                }
            }
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = SypDarkBg) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = SypTextPrimary)
                }
                Spacer(Modifier.width(8.dp))
                Text("الإعدادات", fontSize = 22.sp,
                    fontWeight = FontWeight.Bold, color = SypTextPrimary)
            }

            Spacer(Modifier.height(28.dp))

            // RPC Section
            Card(modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SypCard)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("عنوان عقدة الشبكة (RPC)",
                        fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SypTextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text("يتصل التطبيق بهذه العقدة للحصول على بيانات الشبكة",
                        fontSize = 12.sp, color = SypTextSecond)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value         = rpcUrl,
                        onValueChange = { rpcUrl = it; saved = false },
                        modifier      = Modifier.fillMaxWidth(),
                        placeholder   = { Text("http://127.0.0.1:8545", color = SypTextSecond) },
                        textStyle     = LocalTextStyle.current.copy(
                            color = SypTextPrimary, fontSize = 13.sp),
                        shape  = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SypGold, unfocusedBorderColor = SypCardLight,
                            cursorColor = SypGold),
                        singleLine = true
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically) {
                        if (saved) {
                            Text("✓ تم الحفظ", color = SypSuccess, fontSize = 13.sp)
                            Spacer(Modifier.width(12.dp))
                        }
                        Button(
                            onClick = { onRpcUrlSave(rpcUrl.trim()); saved = true },
                            colors  = ButtonDefaults.buttonColors(containerColor = SypGold),
                            shape   = RoundedCornerShape(10.dp)
                        ) {
                            Text("حفظ", color = SypDarkBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Info Section
            Card(modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SypCard)) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoRow("الإصدار", "2.0.0")
                    HorizontalDivider(color = SypCardLight)
                    InfoRow("الشبكة", "Sypcoin Mainnet")
                    HorizontalDivider(color = SypCardLight)
                    InfoRow("العملة", "SYP")
                }
            }

            Spacer(Modifier.weight(1f))

            OutlinedButton(
                onClick  = { showDelete = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = SypError),
                border   = androidx.compose.foundation.BorderStroke(1.dp, SypError)
            ) {
                Text("حذف المحفظة", color = SypError, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = SypTextSecond)
        Text(value, fontSize = 14.sp, color = SypTextPrimary, fontWeight = FontWeight.Medium)
    }
}
