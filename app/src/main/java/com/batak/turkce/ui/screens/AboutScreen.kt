package com.batak.turkce.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batak.turkce.game.GameViewModel

@Composable
fun AboutScreen(vm: GameViewModel, onBack: () -> Unit) {
    ScreenShell(title = "HAKKINDA", onBack = {
        vm.playClick()
        onBack()
    }) {
        SectionCard {
            Text(
                text = "BATAK – Türkçe Batak Oyunu",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Sürüm 1.1.0",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Geleneksel Türk İhaleli Batak oyununun modern, çevrimdışı çalışan " +
                    "bir Android uygulamasıdır. 3 yapay zekâ rakibe karşı, 4 kişilik masada oynanır.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
        Spacer(Modifier.height(12.dp))
        SectionCard {
            Text(
                text = "ÖZELLİKLER",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "• Eşsiz (tek) ve eşli (2v2) oyun modları\n" +
                    "• 3 zorluk seviyesinde yapay zekâ rakipler\n" +
                    "• Tam ihale sistemi (5–13), koz seçimi ve kart yükseltme zorunluluğu\n" +
                    "• Animasyonlu kart dağıtımı ve kart oyunu\n" +
                    "• Ses efektleri ve titreşim\n" +
                    "• Kalıcı istatistikler ve oyun kaydı\n" +
                    "• Koyu / açık tema ve kart tasarımları\n" +
                    "• Tamamen Türkçe arayüz, internet gerektirmez",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                fontSize = 13.sp,
                lineHeight = 21.sp
            )
        }
        Spacer(Modifier.height(12.dp))
        SectionCard {
            Text(
                text = "TEKNOLOJİ",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Kotlin  ·  Jetpack Compose  ·  Material 3\n" +
                    "Kotlin Coroutines  ·  DataStore  ·  Kotlinx Serialization\n" +
                    "Min SDK 26 (Android 8.0)  ·  Hedef SDK 36",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                fontSize = 13.sp,
                lineHeight = 21.sp
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = "İyi oyunlar!",
            modifier = Modifier,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}
