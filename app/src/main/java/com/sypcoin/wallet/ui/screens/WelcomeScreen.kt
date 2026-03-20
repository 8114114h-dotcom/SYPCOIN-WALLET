package com.sypcoin.wallet.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sypcoin.wallet.ui.theme.*

@Composable
fun WelcomeScreen(
    onCreateWallet:  () -> Unit,
    onRestoreWallet: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue  = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    Surface(modifier = Modifier.fillMaxSize(), color = SypDarkBg) {
        Column(
            modifier            = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            // ── Logo ──────────────────────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⬡", fontSize = 80.sp, color = SypGold,
                    modifier = Modifier.scale(scale))
                Spacer(Modifier.height(20.dp))
                Text("Sypcoin", fontSize = 40.sp,
                    fontWeight = FontWeight.Black, color = SypTextPrimary)
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape  = RoundedCornerShape(20.dp),
                    color  = SypCard
                ) {
                    Text("محفظة SYP",
                        modifier  = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        fontSize  = 15.sp,
                        color     = SypGold
                    )
                }
                Spacer(Modifier.height(32.dp))
                Text(
                    "محفظتك اللامركزية الآمنة\nمفاتيحك، عملاتك، حريتك",
                    fontSize  = 16.sp,
                    color     = SypTextSecond,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )
            }

            // ── Buttons ───────────────────────────────────────────────────────
            Column(
                modifier            = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Button(
                    onClick  = onCreateWallet,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = SypGold)
                ) {
                    Text("إنشاء محفظة جديدة", fontSize = 17.sp,
                        fontWeight = FontWeight.Bold, color = SypDarkBg)
                }

                OutlinedButton(
                    onClick  = onRestoreWallet,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape    = RoundedCornerShape(16.dp),
                    border   = androidx.compose.foundation.BorderStroke(1.5.dp, SypGold),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = SypGold)
                ) {
                    Text("استعادة محفظة موجودة", fontSize = 17.sp, color = SypGold)
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "لا يتم مشاركة بياناتك مع أي جهة",
                    modifier  = Modifier.fillMaxWidth(),
                    fontSize  = 12.sp,
                    color     = SypTextSecond,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
