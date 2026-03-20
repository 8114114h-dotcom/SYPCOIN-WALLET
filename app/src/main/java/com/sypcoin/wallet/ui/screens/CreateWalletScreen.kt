import androidx.compose.ui.graphics.Color
package com.sypcoin.wallet.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sypcoin.wallet.ui.theme.*

@Composable
fun CreateWalletScreen(
    mnemonic:   String,
    onContinue: () -> Unit
) {
    val clipboard   = LocalClipboardManager.current
    var confirmed   by remember { mutableStateOf(false) }
    val words       = mnemonic.split(" ")

    Surface(modifier = Modifier.fillMaxSize(), color = SypDarkBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            Text("Your Recovery Phrase",
                fontSize   = 24.sp,
                fontWeight = FontWeight.Bold,
                color      = SypTextPrimary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Write down these 12 words in order and keep them safe.\nAnyone with this phrase can access your wallet.",
                fontSize  = 14.sp,
                color     = SypTextSecond,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // ── Warning card ──────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(containerColor = Color(0xFF3D2400)),
                shape    = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "⚠  Never share your recovery phrase. Sypcoin cannot recover it for you.",
                    modifier  = Modifier.padding(16.dp),
                    fontSize  = 13.sp,
                    color     = Color(0xFFFFCC02)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Word grid ─────────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(containerColor = SypCard),
                shape    = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    words.chunked(3).forEachIndexed { rowIdx, rowWords ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowWords.forEachIndexed { colIdx, word ->
                                val idx = rowIdx * 3 + colIdx + 1
                                WordChip(index = idx, word = word, modifier = Modifier.weight(1f))
                            }
                        }
                        if (rowIdx < words.chunked(3).size - 1) Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Copy button ───────────────────────────────────────────────────
            TextButton(
                onClick = { clipboard.setText(AnnotatedString(mnemonic)) }
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null,
                    tint = SypGold, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Copy to clipboard", color = SypGold, fontSize = 13.sp)
            }

            Spacer(Modifier.height(16.dp))

            // ── Confirmation checkbox ─────────────────────────────────────────
            Row(
                modifier     = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked         = confirmed,
                    onCheckedChange = { confirmed = it },
                    colors          = CheckboxDefaults.colors(checkedColor = SypGold)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "I have written down my recovery phrase",
                    fontSize = 14.sp,
                    color    = SypTextPrimary
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick  = onContinue,
                enabled  = confirmed,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = SypGold,
                    disabledContainerColor = SypCard
                )
            ) {
                Text(
                    "Continue to Wallet",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (confirmed) SypDarkBg else SypTextSecond
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun WordChip(index: Int, word: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(8.dp),
        color    = SypSurface
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$index.",
                fontSize = 11.sp,
                color    = SypTextSecond,
                modifier = Modifier.width(20.dp)
            )
            Text(
                word,
                fontSize    = 13.sp,
                fontFamily  = FontFamily.Monospace,
                fontWeight  = FontWeight.Medium,
                color       = SypTextPrimary
            )
        }
    }
}
