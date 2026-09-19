package com.batak.turkce.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batak.turkce.game.GameViewModel
import com.batak.turkce.model.AppTheme
import com.batak.turkce.model.CardDesign
import com.batak.turkce.model.Difficulty
import com.batak.turkce.ui.theme.BatakColors

@Composable
fun SettingsScreen(vm: GameViewModel, onBack: () -> Unit) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    var nameInput by remember(settings.playerName) { mutableStateOf(settings.playerName) }

    ScreenShell(title = "AYARLAR", onBack = {
        vm.playClick()
        onBack()
    }) {
        SectionCard {
            Text("GENEL", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))
            SwitchRow(
                label = "Ses Efektleri",
                description = "Kart, buton ve sonuç sesleri",
                checked = settings.soundEnabled,
                onChange = { vm.setSoundEnabled(it) }
            )
            Spacer(Modifier.height(6.dp))
            SwitchRow(
                label = "Titreşim",
                description = "Kart oynama ve el kazanma geri bildirimi",
                checked = settings.vibrationEnabled,
                onChange = { vm.setVibrationEnabled(it) }
            )
            Spacer(Modifier.height(6.dp))
            SwitchRow(
                label = "Animasyonlar",
                description = "Kart dağıtımı ve geçiş animasyonları",
                checked = settings.animationsEnabled,
                onChange = { vm.setAnimationsEnabled(it) }
            )
        }

        Spacer(Modifier.height(14.dp))

        SectionCard {
            Text("YAPAY ZEKA ZORLUĞU", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))
            SettingLabel("Rakip seviyesi")
            Spacer(Modifier.height(8.dp))
            OptionPills(
                options = Difficulty.entries.map { it.labelTr to (it == settings.difficulty) },
                onSelect = { index ->
                    vm.playClick()
                    vm.setDifficulty(Difficulty.entries[index])
                }
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Kolay: basit kararlar  ·  Normal: temel strateji  ·  Zor: kart sayar ve olasılık hesaplar",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
        }

        Spacer(Modifier.height(14.dp))

        SectionCard {
            Text("GÖRÜNÜM", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))
            SettingLabel("Kart tasarımı")
            Spacer(Modifier.height(8.dp))
            OptionPills(
                options = CardDesign.entries.map { it.labelTr to (it == settings.cardDesign) },
                onSelect = { index ->
                    vm.playClick()
                    vm.setCardDesign(CardDesign.entries[index])
                }
            )
            Spacer(Modifier.height(14.dp))
            SettingLabel("Tema")
            Spacer(Modifier.height(8.dp))
            OptionPills(
                options = AppTheme.entries.map { it.labelTr to (it == settings.theme) },
                onSelect = { index ->
                    vm.playClick()
                    vm.setTheme(AppTheme.entries[index])
                }
            )
        }

        Spacer(Modifier.height(14.dp))

        SectionCard {
            Text("OYUN", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))
            SettingLabel("Bir oyundaki el sayısı")
            Spacer(Modifier.height(8.dp))
            OptionPills(
                options = listOf(3, 5, 7, 10).map { "$it el" to (it == settings.roundsPerGame) },
                onSelect = { index ->
                    vm.playClick()
                    vm.setRoundsPerGame(listOf(3, 5, 7, 10)[index])
                }
            )
            Spacer(Modifier.height(14.dp))
            SettingLabel("Oyuncu adı")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = nameInput,
                onValueChange = {
                    if (it.length <= 16) {
                        nameInput = it
                        vm.setPlayerName(it)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Oyuncu", fontSize = 14.sp) }
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Yeni oyun başlattığında bu ad kullanılır.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    description: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                fontSize = 11.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF2B1F05),
                checkedTrackColor = BatakColors.Gold,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}
