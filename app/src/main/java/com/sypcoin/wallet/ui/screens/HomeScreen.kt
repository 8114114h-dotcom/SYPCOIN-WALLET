package com.sypcoin.wallet.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sypcoin.wallet.crypto.AddressUtils
import com.sypcoin.wallet.network.TxInfo
import com.sypcoin.wallet.ui.theme.*

// ── Shared TxItem ─────────────────────────────────────────────────────────────

@Composable
fun TxItem(
    tx:          TxInfo,
    myAddress:   String,
    chainHeight: Long,
    onClick:     () -> Unit
) {
    val isSent        = tx.from.equals(myAddress, ignoreCase = true)
    val confirmations = tx.blockHeight?.let { (chainHeight - it + 1).coerceAtLeast(0) } ?: 0L
    val isPending     = tx.blockHeight == null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SypCard)
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape    = CircleShape,
                color    = if (isSent) SypError.copy(0.15f) else SypSuccess.copy(0.15f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (isSent) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        null,
                        tint     = if (isSent) SypError else SypSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (isSent) AddressUtils.shorten(tx.to) else AddressUtils.shorten(tx.from),
                    fontSize   = 13.sp,
                    color      = SypTextPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(Modifier.height(2.dp))
                if (isPending) {
                    Surface(shape = RoundedCornerShape(4.dp),
                        color = SypGold.copy(0.15f)) {
                        Text("قيد الانتظار", fontSize = 10.sp, color = SypGold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("كتلة #${tx.blockHeight}",
                            fontSize = 11.sp, color = SypTextSecond)
                        Spacer(Modifier.width(6.dp))
                        Text("· $confirmations تأكيد",
                            fontSize = 11.sp,
                            color    = when {
                                confirmations >= 12 -> SypSuccess
                                confirmations >= 6  -> SypGold
                                else                -> SypTextSecond
                            })
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${if (isSent) "-" else "+"}${tx.amount}",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = if (isSent) SypError else SypSuccess
                )
                Text("SYP", fontSize = 10.sp, color = SypTextSecond)
            }
        }
    }
}

// ── Shared TxDetailDialog ─────────────────────────────────────────────────────

@Composable
fun TxDetailDialog(
    tx:          TxInfo,
    chainHeight: Long,
    onDismiss:   () -> Unit
) {
    val clipboard     = LocalClipboardManager.current
    val confirmations = tx.blockHeight?.let { (chainHeight - it + 1).coerceAtLeast(0) } ?: 0L
    val confirmColor  = when {
        confirmations >= 12 -> SypSuccess
        confirmations >= 6  -> SypGold
        confirmations >= 1  -> SypBlue
        else                -> SypTextSecond
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SypCard)) {
            Column(modifier = Modifier.padding(20.dp)) {

                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("تفاصيل المعاملة", fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, color = SypTextPrimary)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null, tint = SypTextSecond)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Divider(color = SypSurface)
                Spacer(Modifier.height(16.dp))

                TxDetailRow("رقم المعاملة",
                    tx.txId.take(16) + "...", tx.txId, clipboard)
                Spacer(Modifier.height(12.dp))
                TxDetailRow("المبلغ", "${tx.amount} SYP")
                Spacer(Modifier.height(12.dp))
                TxDetailRow("الرسوم", "${tx.fee} SYP")
                Spacer(Modifier.height(12.dp))
                TxDetailRow("المستقبل",
                    AddressUtils.shorten(tx.to), tx.to, clipboard)
                Spacer(Modifier.height(12.dp))
                TxDetailRow("الكتلة",
                    tx.blockHeight?.let { "#$it" } ?: "قيد الانتظار")
                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("التأكيدات", fontSize = 13.sp, color = SypTextSecond)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = confirmColor,
                            modifier = Modifier.size(8.dp)) {}
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (tx.blockHeight == null) "لم تُؤكد بعد"
                            else "$confirmations تأكيد",
                            fontSize = 13.sp, color = confirmColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                Button(onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = SypGold)) {
                    Text("إغلاق", color = SypDarkBg, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TxDetailRow(
    label:     String,
    value:     String,
    copyValue: String? = null,
    clipboard: androidx.compose.ui.platform.ClipboardManager? = null
) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, color = SypTextSecond)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, fontSize = 13.sp, color = SypTextPrimary,
                fontFamily = FontFamily.Monospace)
            if (copyValue != null && clipboard != null) {
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.ContentCopy, null, tint = SypGold,
                    modifier = Modifier.size(14.dp)
                        .clickable { clipboard.setText(AnnotatedString(copyValue)) })
            }
        }
    }
}
