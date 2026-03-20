package com.sypcoin.wallet.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.sypcoin.wallet.ui.theme.*

@Composable
fun ReceiveScreen(address: String, onBack: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    var copied    by remember { mutableStateOf(false) }
    val qr        = remember(address) { generateQr(address) }

    Surface(modifier = Modifier.fillMaxSize(), color = SypDarkBg) {
        Column(
            modifier            = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = SypTextPrimary)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("استقبال SYP", fontSize = 24.sp,
                fontWeight = FontWeight.Bold, color = SypTextPrimary)
            Text("شارك عنوانك لاستقبال العملات",
                fontSize = 14.sp, color = SypTextSecond)

            Spacer(Modifier.height(32.dp))

            // QR
            Card(shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)) {
                Box(modifier = Modifier.padding(16.dp).size(220.dp),
                    contentAlignment = Alignment.Center) {
                    if (qr != null)
                        Image(qr.asImageBitmap(), null, modifier = Modifier.fillMaxSize())
                    else
                        CircularProgressIndicator(color = SypGold)
                }
            }

            Spacer(Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SypCard)) {
                Column(modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("عنوان محفظتك", fontSize = 12.sp, color = SypTextSecond)
                    Spacer(Modifier.height(8.dp))
                    Text(address, fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp, color = SypTextPrimary, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { clipboard.setText(AnnotatedString(address)); copied = true },
                            colors  = ButtonDefaults.buttonColors(
                                containerColor = if (copied) SypSuccess else SypGold),
                            shape   = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, null,
                                tint = if (copied) Color.White else SypDarkBg,
                                modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (copied) "تم النسخ!" else "نسخ",
                                color = if (copied) Color.White else SypDarkBg,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("أرسل فقط عملات SYP لهذا العنوان\nإرسال عملات أخرى قد يؤدي لخسارتها نهائياً",
                fontSize = 12.sp, color = SypTextSecond, textAlign = TextAlign.Center)
        }
    }
}

private fun generateQr(content: String, size: Int = 512): Bitmap? = try {
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
    Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also { bmp ->
        for (x in 0 until size)
            for (y in 0 until size)
                bmp.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK
                             else android.graphics.Color.WHITE)
    }
} catch (e: Exception) { null }
