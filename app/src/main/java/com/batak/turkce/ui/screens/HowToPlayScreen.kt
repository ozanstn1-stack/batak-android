package com.batak.turkce.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batak.turkce.game.GameViewModel

@Composable
fun HowToPlayScreen(vm: GameViewModel, onBack: () -> Unit) {
    ScreenShell(title = "NASIL OYNANIR?", onBack = {
        vm.playClick()
        onBack()
    }) {
        HelpSection(
            title = "AMAÇ",
            body = "İhaleli Batak, 4 oyuncunun 52 kartlık desteyle oynadığı bir el oyunudur. " +
                "Her oyuncuya 13 kart dağıtılır ve toplam 13 el oynanır. " +
                "Her oyuncu ihale turunda aldığı el sayısını taahhüt eder; " +
                "taahhüdünü tutan puan kazanır, tutamayan ceza puanı alır."
        )
        Spacer(Modifier.height(12.dp))
        HelpSection(
            title = "KART SIRALAMASI",
            body = "A (As) > K > Q > J > 10 > 9 > 8 > 7 > 6 > 5 > 4 > 3 > 2\n" +
                "Renkler: ♠ Maça, ♥ Kupa, ♦ Karo, ♣ Sinek. Kupa ve Karo kırmızıdır. " +
                "Koz dışında hiçbir renk diğerinden üstün değildir."
        )
        Spacer(Modifier.height(12.dp))
        HelpSection(
            title = "İHALE",
            body = "Sırayla her oyuncu 8 ile 13 arasında bir el sayısı söyler ya da \"Pas\" der. " +
                "Her yeni teklif, o ana kadarki en yüksek tekliften büyük olmalıdır. " +
                "Örneğin biri 9 dediyse, sonraki oyuncular yalnızca 10, 11, 12 veya 13 diyebilir. " +
                "En yüksek ihaleyi veren oyuncu kozu belirler. " +
                "Herkes pas geçerse kartlar yeniden dağıtılır.\n\n" +
                "Not: Bu oyunda her oyuncu kendi verdiği ihaleyle yükümlüdür. " +
                "Pas geçen oyuncu o elden puan alamaz."
        )
        Spacer(Modifier.height(12.dp))
        HelpSection(
            title = "KOZ",
            body = "İhaleyi alan oyuncu dört renkten birini koz seçer. " +
                "Koz rengindeki her kart, diğer renklerdeki tüm kartlardan üstündür. " +
                "Koz seçildikten sonra ilk eli ihaleci başlatır. " +
                "Sonraki ellerde eli kazanan oyuncu başlatır."
        )
        Spacer(Modifier.height(12.dp))
        HelpSection(
            title = "KART OYNAMA KURALLARI",
            body = "• Elin ilk kartını oynayan oyuncu rengi (takımı) belirler.\n" +
                "• Diğer oyuncular ellerinde o renkten kart varsa mutlaka o renkten oynamak zorundadır.\n" +
                "• Elinde o renk yoksa istediği kartı oynayabilir: koz atabilir veya başka renk atabilir.\n" +
                "• Koz atma zorunluluğu yoktur; koz atmadan da kesebilir.\n\n" +
                "Eli kazanan: elde koz varsa en yüksek koz, yoksa başlangıç rengindeki en yüksek karttır."
        )
        Spacer(Modifier.height(12.dp))
        HelpSection(
            title = "PUANLAMA",
            body = "El bittiğinde her oyuncu kendi sonucuna göre puan alır:\n\n" +
                "• İhalesini tutan (aldığı el ≥ ihalesi): aldığı el sayısı kadar artı puan.\n" +
                "   Örnek: İhalesi 9, aldığı el 10 → +10 puan\n" +
                "• İhalesini tutamayan: ihalesi kadar eksi puan.\n" +
                "   Örnek: İhalesi 8, aldığı el 7 → -8 puan\n" +
                "• Pas geçen: 0 puan\n\n" +
                "Oyun sonunda en yüksek toplam puana ulaşan oyuncu kazanır."
        )
        Spacer(Modifier.height(12.dp))
        HelpSection(
            title = "İPUÇLARI",
            body = "• İhale verirken elindeki yüksek kartları ve renk uzunluklarını değerlendir.\n" +
                "• As ve kral içeren uzun renkler el kazandırır.\n" +
                "• Koz sayısı fazla olan renkleri koz seçmek avantaj sağlar.\n" +
                "• Elinde olmayan renklerden sonradan koz atarak el kazanabilirsin.\n" +
                "• Yüksek kartlarını erken kullanma; rakibin elini takip et."
        )
    }
}

@Composable
private fun HelpSection(title: String, body: String) {
    SectionCard {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = body,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
    }
}
