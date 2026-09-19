package com.batak.turkce.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batak.turkce.game.GameViewModel
import com.batak.turkce.ui.theme.BatakColors
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun StatsScreen(vm: GameViewModel, onBack: () -> Unit) {
    val stats by vm.stats.collectAsStateWithLifecycle()
    var confirmingReset by remember { mutableStateOf(false) }

    LaunchedEffect(confirmingReset) {
        if (confirmingReset) {
            delay(3200)
            confirmingReset = false
        }
    }

    ScreenShell(title = "İSTATİSTİKLER", onBack = {
        vm.playClick()
        onBack()
    }) {
        Row(Modifier.fillMaxWidth()) {
            MiniStatCard("OYNANAN OYUN", "${stats.gamesPlayed}", Modifier.weight(1f))
            Spacer(Modifier.padding(horizontal = 5.dp))
            MiniStatCard("KAZANILAN", "${stats.gamesWon}", Modifier.weight(1f), accent = BatakColors.Success)
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth()) {
            MiniStatCard("KAYBEDİLEN", "${stats.gamesLost}", Modifier.weight(1f), accent = BatakColors.Danger)
            Spacer(Modifier.padding(horizontal = 5.dp))
            MiniStatCard(
                "KAZANMA ORANI",
                "%${(stats.winRate * 100f).roundToInt()}",
                Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth()) {
            MiniStatCard("TOPLAM ALINAN EL", "${stats.totalTricks}", Modifier.weight(1f))
            Spacer(Modifier.padding(horizontal = 5.dp))
            MiniStatCard("EN YÜKSEK SKOR", "${stats.highestScore}", Modifier.weight(1f), accent = BatakColors.Gold)
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth()) {
            MiniStatCard("EN UZUN GALİBİYET SERİSİ", "${stats.longestWinStreak}", Modifier.weight(1f))
            Spacer(Modifier.padding(horizontal = 5.dp))
            MiniStatCard(
                "GÜNCEL SERİ",
                "${stats.currentWinStreak}",
                Modifier.weight(1f),
                accent = if (stats.currentWinStreak > 0) BatakColors.Success else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(18.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    if (confirmingReset) BatakColors.Danger.copy(alpha = 0.85f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(50))
                .clickable {
                    vm.playClick()
                    if (confirmingReset) {
                        vm.resetStats()
                        confirmingReset = false
                    } else {
                        confirmingReset = true
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (confirmingReset) "EMİN MİSİN? TEKRAR DOKUN" else "İSTATİSTİKLERİ SIFIRLA",
                color = if (confirmingReset) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
