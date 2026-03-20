package com.sypcoin.wallet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sypcoin.wallet.ui.theme.*

@Composable
fun PinScreen(
    title:    String = "أدخل رقم السري",
    subtitle: String = "",
    onPin:    (String) -> Unit,
    error:    String? = null
) {
    var pin by remember { mutableStateOf("") }

    LaunchedEffect(pin) {
        if (pin.length == 6) { onPin(pin); pin = "" }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = SypDarkBg) {
        Column(
            modifier            = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("⬡", fontSize = 48.sp, color = SypGold)
            Spacer(Modifier.height(24.dp))
            Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SypTextPrimary)
            if (subtitle.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(subtitle, fontSize = 14.sp, color = SypTextSecond, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(40.dp))

            // ── PIN dots ──────────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(6) { i ->
                    Surface(
                        shape  = CircleShape,
                        color  = if (i < pin.length) SypGold else SypCard,
                        modifier = Modifier.size(18.dp)
                    ) {}
                }
            }

            if (error != null) {
                Spacer(Modifier.height(12.dp))
                Text(error, fontSize = 13.sp, color = SypError, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(48.dp))

            // ── Keypad ────────────────────────────────────────────────────────
            val keys = listOf("1","2","3","4","5","6","7","8","9","","0","⌫")
            keys.chunked(3).forEach { row ->
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { key ->
                        Box(modifier = Modifier.weight(1f)) {
                            when {
                                key.isEmpty() -> {}
                                key == "⌫" -> OutlinedButton(
                                    onClick  = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
                                    modifier = Modifier.fillMaxWidth().height(64.dp),
                                    shape    = RoundedCornerShape(16.dp),
                                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = SypTextSecond),
                                    border   = androidx.compose.foundation.BorderStroke(1.dp, SypCard)
                                ) {
                                    Icon(Icons.Default.Backspace, contentDescription = null,
                                        tint = SypTextSecond, modifier = Modifier.size(20.dp))
                                }
                                else -> Button(
                                    onClick  = { if (pin.length < 6) pin += key },
                                    modifier = Modifier.fillMaxWidth().height(64.dp),
                                    shape    = RoundedCornerShape(16.dp),
                                    colors   = ButtonDefaults.buttonColors(containerColor = SypCard)
                                ) {
                                    Text(key, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                                        color = SypTextPrimary)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
