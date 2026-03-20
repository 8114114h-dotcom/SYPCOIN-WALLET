package com.sypcoin.wallet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sypcoin.wallet.ui.theme.*

@Composable
fun RestoreWalletScreen(
    onRestore: (String) -> Boolean,
    onBack:    () -> Unit
) {
    var phrase by remember { mutableStateOf("") }
    var error  by remember { mutableStateOf<String?>(null) }

    val wordCount = phrase.trim().split("\\s+".toRegex()).count { it.isNotEmpty() }

    Surface(modifier = Modifier.fillMaxSize(), color = SypDarkBg) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = SypTextPrimary)
                }
                Spacer(Modifier.width(8.dp))
                Text("استعادة المحفظة", fontSize = 22.sp,
                    fontWeight = FontWeight.Bold, color = SypTextPrimary)
            }

            Spacer(Modifier.height(32.dp))

            Card(modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SypWarning.copy(0.1f)),
                shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("🔐", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("أدخل عبارة الاستعادة المكونة من 12 كلمة بالترتيب الصحيح",
                        fontSize = 13.sp, color = SypWarning)
                }
            }

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value         = phrase,
                onValueChange = { phrase = it; error = null },
                modifier      = Modifier.fillMaxWidth().height(160.dp),
                placeholder   = { Text("word1 word2 word3 ...", color = SypTextSecond) },
                textStyle     = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 15.sp, color = SypTextPrimary),
                shape  = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SypGold,
                    unfocusedBorderColor = SypCard,
                    cursorColor = SypGold),
                maxLines = 5
            )

            Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.End) {
                Text("$wordCount / 12 كلمة",
                    fontSize = 12.sp,
                    color = if (wordCount == 12) SypSuccess else SypTextSecond)
            }

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, fontSize = 14.sp, color = SypError)
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    val ok = onRestore(phrase.trim())
                    if (!ok) error = "عبارة الاستعادة غير صحيحة، تحقق من الكلمات والترتيب"
                },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = SypGold)
            ) {
                Text("استعادة المحفظة", fontSize = 17.sp,
                    fontWeight = FontWeight.Bold, color = SypDarkBg)
            }

            Spacer(Modifier.height(12.dp))
            Text("عبارتك تُخزَّن محلياً مشفرة ولا تُرسَل لأي جهة",
                fontSize = 12.sp, color = SypTextSecond,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}
