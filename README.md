# Batak – Türkçe İhaleli Batak Oyunu

Android için Kotlin ve Jetpack Compose ile geliştirilmiş, tamamen Türkçe, çevrimdışı oynanabilen
4 kişilik **İhaleli Batak** kart oyunu. Oyuncuya 3 yapay zekâ rakip eşlik eder.

## Ekran Görüntüleri

| Ana Menü | İhale | Koz Seçimi | Oyun |
|---|---|---|---|
| ![Menü](screenshots/01_menu.png) | ![İhale](screenshots/03_bidding.png) | ![Koz](screenshots/04_trump.png) | ![Oyun](screenshots/06_select.png) |

| El Sonucu | Oyun Sonu | Ayarlar | İstatistikler |
|---|---|---|---|
| ![Sonuç](screenshots/09_round_result.png) | ![Oyun Sonu](screenshots/10_game_over.png) | ![Ayarlar](screenshots/13_settings.png) | ![İstatistik](screenshots/14_stats.png) |

## Uygulama Nedir?

Geleneksel Türk **İhaleli Batak** oyununun modern bir mobil sürümüdür. 52 kartlık deste,
4 oyuncu, 13 el. Oyuncular sırayla ihale verir (8–13 veya Pas), en yüksek ihaleyi alan kozu
belirler ve her oyuncu kendi ihalesini tutmaya çalışır.

- **Tamamen çevrimdışı** çalışır, internet gerektirmez.
- **Tamamen Türkçe** arayüz.
- **3 zorluk seviyesi**: Kolay, Normal, Zor.
- Modern poker masası görünümü, akıcı animasyonlar, ses efektleri ve titreşim.

## Özellikler

- 52 kartlık gerçek deste, doğru kart sıralaması (A > K > Q > J > 10 … > 2)
- Sıralı ihale sistemi (8–13, Pas, geçersiz teklifler engellenir)
- Koz seçimi (♠ Maça, ♥ Kupa, ♦ Karo, ♣ Sinek)
- Takım rengi zorunluluğu ve doğru el kazananı hesaplama
- Kart takibi yapan, ele göre ihale veren üç seviyeli yapay zekâ
- Animasyonlu kart dağıtımı, kart oynama ve el vurgusu
- Ses efektleri (dağıtma, oynama, ihale, el kazanma/kaybetme) ve titreşim
- Kalıcı istatistikler (oynanan/kazanılan oyun, el sayısı, en yüksek skor, seriler)
- Otomatik oyun kaydı ve **Devam Et** desteği
- Koyu / Açık tema, iki kart tasarımı, oyuncu adı, el sayısı ve ses/titreşim ayarları

## Nasıl Çalıştırılır?

### Gereksinimler

- Android Studio (Ladybug veya üzeri) ya da JDK 17+ ve Android SDK
- Android 8.0 (API 26) veya üzeri bir cihaz/emülatör

### Android Studio ile

1. Projeyi Android Studio'da açın.
2. Gradle senkronizasyonunun bitmesini bekleyin.
3. `Run` ile cihazınıza/emülatörünüze yükleyin.

### Komut satırı ile

```bash
./gradlew installDebug        # debug derleyip cihaza kurar
./gradlew :app:assembleRelease # imzalı release APK üretir
```

Windows'ta `gradlew.bat` kullanın.

## Build Komutu ve APK

```bash
./gradlew clean :app:assembleRelease
```

Çıktı: `app/build/outputs/apk/release/app-release.apk`

Hazır APK, depo kökündeki `apk/app-release.apk` dosyasındadır ve
[Releases](https://github.com/ozanstn1-stack/batak-android/releases) sayfasında `v1.0.0`
sürümüne eklenmiştir. APK, hata ayıklama anahtarıyla imzalanmıştır; telefona doğrudan
kurulabilir (bilinmeyen kaynaklardan yükleme izni gerekir).

## Oyun Kuralları

### Amaç

13 el oynanır. Her oyuncu ihale turunda bir söz verir ve o sözü tutmaya çalışır.

### İhale

- Oyuncular sırayla **8, 9, 10, 11, 12, 13** der veya **Pas** geçer.
- Her teklif, o ana kadarki en yüksek tekliften büyük olmalıdır.
- En yüksek ihaleyi veren oyuncu **koz** rengini seçer.
- Herkes pas geçerse kartlar yeniden dağıtılır.

### Kart Oynama

- İlk kartı oynayan oyuncu rengi belirler.
- Elinde o renkten kart olan oyuncular **mutlaka o renkten** oynamak zorundadır.
- Elinde o renk yoksa istediği kartı oynayabilir; koz atma zorunluluğu yoktur.
- Elde koz varsa en yüksek koz, yoksa başlangıç rengindeki en yüksek kart eli kazanır.
- Eli kazanan oyuncu sıradaki eli başlatır.

### Puanlama

| Durum | Puan |
|---|---|
| İhalesini tutan (aldığı el ≥ ihalesi) | **+ aldığı el sayısı** |
| İhalesini tutamayan | **− ihale sayısı** |
| Pas geçen | **0** |

Örnek: İhalesi 9 olan oyuncu 10 el alırsa **+10 puan**; ihalesi 8 olan oyuncu 7 el alırsa
**−8 puan** alır. Oyun sonunda en yüksek toplam puana ulaşan oyuncu kazanır.

## Kullanılan Teknoloji

- **Kotlin 2.2.20**
- **Jetpack Compose** (Compose BOM 2025.09.00, Material 3)
- **Android Gradle Plugin 8.13.0**, Gradle 8.14.3, JDK 17 hedefi
- **Kotlin Coroutines** (oyun döngüsü ve yapay zekâ zamanlaması)
- **DataStore Preferences** (ayarlar, istatistikler ve oyun kaydı)
- **kotlinx.serialization** (oyun durumu kaydı)
- **SoundPool** (kart/buton/sonuç sesleri, Python ile sentezlenmiş WAV dosyaları)
- minSdk 26 · targetSdk 36 · portrait

## Proje Yapısı

```
app/src/main/java/com/batak/turkce/
├── model/        Kart, renk, zorluk gibi temel modeller
├── engine/       Batak kuralları, oyun durumu, puanlama
├── ai/           Üç seviyeli yapay zekâ (ihale, koz, kart oynama)
├── data/         DataStore tabanlı ayarlar/istatistik/kayıt deposu
├── game/         GameViewModel – oyun döngüsü ve durum yönetimi
├── audio/        SoundPool ve titreşim yöneticileri
└── ui/           Compose ekranları, kart bileşenleri ve tema
```

## Testler

```bash
./gradlew :app:testDebugUnitTest
```

- **BatakRulesTest** – deste, dağıtım, takım rengi zorunluluğu, el kazananı, ihale doğrulama, puanlama
- **AiSimulationTest** – 36 tam maç simülasyonu: her hamle kurallara uygun mu, tüm kartlar oynanıyor mu,
  oyun her zaman bitiyor mu, aynı tohum aynı sonucu veriyor mu
- **AiCalibrationTest** – ihale tahmin dağılımı ve yeniden dağıtım oranı ölçümü

## Bilinen Sınırlamalar

- Rakip oyuncular üç zorluk seviyesinde heuristik oynar; kusursuz değildir.
- İhale sırasında yapay zekâ, masayı açmak için iyimser davranabilir (gerçek oyunda olduğu gibi).
- Pas geçmek risk almayı gerektirmez; puanlı oyunda ihale almak ödüllendirilir.
- APK hata ayıklama anahtarıyla imzalanmıştır (mağaza yayını için kendi anahtarınızı kullanın).
