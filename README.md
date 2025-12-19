# Neon Circle Ball - Android Uygulaması

## 📱 Kurulum Adımları

### 1. Android Studio'yu Aç
- Android Studio'yu başlatın
- "Open" seçeneğine tıklayın
- `SpaceBillard` klasörünü seçin

### 2. Gradle Sync
- Android Studio otomatik olarak Gradle sync yapacak
- İlk açılışta biraz zaman alabilir

### 3. game.js Dosyasını Tamamlayın
**ÖNEMLİ:** `app/src/main/assets/game.js` dosyasına HTML'inizdeki `<script>` tagları arasındaki tüm JavaScript kodunu kopyalayın.

### 4. Çalıştırın
- Üst menüden **Run → Run 'app'** (veya Shift+F10)
- Emülatör veya gerçek cihaz seçin

## ✅ Özellikler

- ✅ Tam ekran oyun deneyimi
- ✅ Dokunmatik kontroller çalışır
- ✅ LocalStorage desteklenir (High Score kaydedilir)
- ✅ Ses efektleri çalışır (İnternet gerektirir)
- ✅ Geri tuşu desteği
- ✅ Portrait (dikey) mod kilidi

## 📋 Gereksinimler

- **Minimum SDK:** Android 7.0 (API 24)
- **Target SDK:** Android 14 (API 34)
- **İnternet İzni:** Ses dosyaları için gerekli

## 🎮 Nasıl Oynanır

1. Uygulamayı başlatın
2. "INITIATE" butonuna tıklayın
3. Beyaz topu sürükleyip fırlatın
4. Renkli topları toplayın, siyah toplardan kaçının!

## 🔧 Sorun Giderme

### Gradle Sync Hatası
```bash
File → Invalidate Caches → Invalidate and Restart
```

### Emülatör Bulunamıyor
```
Tools → Device Manager → Create Device
```

### JavaScript Çalışmıyor
`game.js` dosyasına HTML'deki tüm JavaScript kodunu kopyaladığınızdan emin olun.

## 📁 Proje Yapısı

```
SpaceBillard/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── assets/
│   │       │   ├── game.html
│   │       │   └── game.js  ← BURAYA JS KODUNU EKLEYİN
│   │       ├── java/
│   │       │   └── com/example/neoncircleball/
│   │       │       └── MainActivity.kt
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   └── activity_main.xml
│   │       │   └── values/
│   │       │       ├── colors.xml
│   │       │       ├── strings.xml
│   │       │       └── themes.xml
│   │       └── AndroidManifest.xml
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## 🚀 APK Oluşturma

1. **Build → Generate Signed Bundle / APK**
2. **APK** seçin
3. Keystore oluşturun veya mevcut olanı seçin
4. **release** build variant'ı seçin
5. APK `app/release/` klasöründe oluşacak

## 📝 Notlar

- Ses dosyaları CDN'den yüklenir, internet bağlantısı gerektirir
- Oyun verileri (high score) cihazda LocalStorage'da saklanır
- Tam ekran deneyim için ActionBar gizlenmiştir

## 🎨 Özelleştirme

### Uygulama Adını Değiştirme
`app/src/main/res/values/strings.xml` dosyasında:
```xml
<string name="app_name">Yeni İsim</string>
```

### Uygulama İkonunu Değiştirme
`app/src/main/res/mipmap-*/` klasörlerine yeni icon dosyalarını ekleyin.

### Renk Temasını Değiştirme
`app/src/main/res/values/colors.xml` dosyasını düzenleyin.

---

**Geliştirici:** RKS Company  
**Platform:** Android (API 24+)  
**Teknoloji:** Kotlin + WebView + HTML5 Canvas
