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
                "Oyuncular ihale turunda alacakları el sayısını söyler; " +
                "sözünü tutan puan kazanır, tutamayan ceza puanı alır."
        )
        Spacer(Modifier.height(12.dp))
        HelpSection(
            title = "KART SIRALAMASI",
            body = "A (As) > K (Papaz) > Q (Kız) > J (Vale) > 10 > 9 > 8 > 7 > 6 > 5 > 4 > 3 > 2\n" +
                "Renkler: Maça, Kupa, Karo, Sinek. Kupa ve Karo kırmızı, Maça ve Sinek siyahtır. " +
                "Koz dışında hiçbir renk diğerinden üstün değildir.\n\n" +
                "Eldeki kartlar otomatik olarak renklerine göre gruplanır ve her renk kendi içinde " +
                "büyükten küçüğe sıralanır (Maça, Kupa, Karo, Sinek)."
        )
        Spacer(Modifier.height(12.dp))
        HelpSection(
            title = "İHALE",
            body = "Sırayla her oyuncu bir el sayısı söyler ya da \"Pas\" der.\n\n" +
                "• Eşsiz (tek) modda ihale 5'ten başlar: 5, 6, 7, 8, 9, 10, 11, 12, 13\n" +
                "• Eşli (2v2) modda ihale 7'den başlar: 7, 8, 9, 10, 11, 12, 13\n\n" +
                "Her yeni teklif, o ana kadarki en yüksek tekliften büyük olmalıdır. " +
                "Örneğin biri 7 dediyse, sonraki oyuncular yalnızca 8 ve üzerini söyleyebilir. " +
                "En yüksek ihaleyi veren oyuncu kozu belirler. " +
                "Herkes pas geçerse kartlar yeniden dağıtılır."
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
                "• Elinde o renkten kart varsa mutlaka o renkten oynamak zorundadır.\n" +
                "• Kart yükseltme zorunluluğu: Oynanan en yüksek takım rengi kartından " +
                "daha büyük bir kartın varsa, onu oynamak zorundasın. " +
                "Örneğin masaya 9 kupa atıldıysa ve elinde K kupa varsa K kupa oynamalısın.\n" +
                "• Elinde o renk hiç yoksa istediği kartı oynayabilir: koz atabilir veya " +
                "başka renk atabilir. Koz atma zorunluluğu yoktur.\n\n" +
                "Eli kazanan: elde koz varsa en yüksek koz, yoksa başlangıç rengindeki en yüksek karttır."
        )
        Spacer(Modifier.height(12.dp))
        HelpSection(
            title = "EŞSİZ (TEK) MOD PUANLAMA",
            body = "Her oyuncu kendi verdiği ihaleyle yükümlüdür:\n\n" +
                "• İhalesini tutan (aldığı el ≥ ihalesi): aldığı el sayısı kadar artı puan.\n" +
                "   Örnek: İhalesi 9, aldığı el 10 → +10 puan\n" +
                "• İhalesini tutamayan: ihalesi kadar eksi puan.\n" +
                "   Örnek: İhalesi 8, aldığı el 7 → −8 puan\n" +
                "• Pas geçen: 0 puan\n\n" +
                "Oyun sonunda en yüksek toplam puana ulaşan oyuncu kazanır."
        )
        Spacer(Modifier.height(12.dp))
        HelpSection(
            title = "EŞLİ (2V2) MOD",
            body = "Karşılıklı oturan iki oyuncu eş olur:\n" +
                "Sen + Rakip 2  ·  Rakip 1 + Rakip 3\n\n" +
                "Yalnızca en yüksek ihaleyi veren taraf sözleşmelidir. " +
                "Takımın toplam el sayısı ihaleyi tutarsa iki eş de + ihale puanı alır; " +
                "tutamazsa iki eş de − ihale puanı alır ve rakip takımın üyeleri + ihale puanı kazanır.\n\n" +
                "Örnek: İhale 8, senin takımın toplam 9 el aldı → iki eş de +8 puan."
        )
        Spacer(Modifier.height(12.dp))
        HelpSection(
            title = "İPUÇLARI",
            body = "• İhale verirken elindeki yüksek kartları ve renk uzunluklarını değerlendir.\n" +
                "• As ve papaz içeren uzun renkler el kazandırır.\n" +
                "• Koz sayısı fazla olan renkleri koz seçmek avantaj sağlar.\n" +
                "• Elinde olmayan renklerden sonradan koz atarak el kazanabilirsin.\n" +
                "• Yüksek kartlarını erken kullanma; rakiplerin oynadığı kartları takip et.\n" +
                "• Eşli modda eşinin elini kazanmasına engel olma, gereksiz yere üstüne kart atma."
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
