# 🚀 Space Billiard Online - Multiplayer Setup Guide

## 📦 Yapılan Değişiklikler

### ✅ Tamamlanan Adımlar:

1. **Supabase Database Tables** oluşturuldu:
   - `users` - Kullanıcı bilgileri
   - `rooms` - Oyun odaları
   - `room_players` - Odadaki oyuncular
   - `match_history` - Maç geçmişi

2. **Node.js WebSocket Game Server** kuruldu:
   - Lokasyon: `game-server/`
   - Port: `8080`
   - IP: `192.168.1.149`

3. **Android Client Updates**:
   - ✅ `PLAY ONLINE` butonu ana menüye eklendi
   - ✅ `OnlineActivity` - Oda oluşturma/katılma ekranı
   - ✅ `OnlineGameManager` - WebSocket client
   - ✅ Dependencies eklendi (OkHttp, Gson)

## 🎮 Nasıl Kullanılır?

### 1. Game Server'ı Başlat

```bash
cd game-server
npm start
```

Server başladığında göreceksiniz:
```
🚀 Space Billiard Game Server started on ws://192.168.1.149:8080
🎮 Server is ready to accept connections!
```

### 2. Firewall Ayarları

Windows Firewall'da port 8080'i açın:

```powershell
# Yönetici olarak PowerShell'de çalıştırın:
New-NetFirewallRule -DisplayName "Space Billiard Server" -Direction Inbound -LocalPort 8080 -Protocol TCP -Action Allow
```

### 3. Android Uygulamayı Çalıştır

1. Android Studio'da projeyi aç
2. Build > Rebuild Project
3. Uygulamayı çalıştır
4. Ana menüde **PLAY ONLINE** butonuna tıkla
5. Kullanıcı adını gir
6. Oda oluştur veya mevcut bir odaya katıl

## 📱 Online Mode Özellikleri

### Oyun Kuralları:
- **3 Set Maç**: İlk 2 seti kazanan maçı kazanır
- **Skor Sistemi**: Her set sonunda kazanan 1 puan alır (2-0, 2-1, vb.)
- **Top Sayma**: Her setteki patlatılan top sayısına göre kazanan belirlenir
- **Özel Kurallar**: 
  - ❌ Siyah topa çarpma YOK
  - ❌ Yanma (burn) mekanizması YOK

### Server-Side Physics:
- Tüm fizik hesaplamaları server'da yapılır
- Client sadece vuruş açısı ve gücü gönderir
- Server sonuçları tüm oyunculara broadcast eder

## 🔧 Teknoloji Stack

### Backend:
- **Game Server**: Node.js + WebSocket (ws library)
- **Database**: Supabase PostgreSQL
- **Real-time**: WebSocket connections

### Frontend:
- **Android**: Java + OkHttp WebSocket Client
- **Networking**: WebSocket (real-time)
- **Data Format**: JSON (Gson)

## 📊 Server API

### Client → Server Mesajları:

```javascript
// Kullanıcı adı ayarla
{
  "type": "set_username",
  "username": "Player1"
}

// Oda oluştur
{
  "type": "create_room",
  "roomName": "My Room"
}

// Odaya katıl
{
  "type": "join_room",
  "roomId": "uuid-here"
}

// Vuruş at
{
  "type": "shot",
  "angle": 32.5,
  "power": 0.87
}

// Top patladı (bildir)
{
  "type": "ball_destroyed"
}

// Set bitti
{
  "type": "set_ended"
}
```

### Server → Client Mesajları:

```javascript
// Bağlantı kuruldu
{
  "type": "connected",
  "clientId": "uuid",
  "message": "Connected to Space Billiard Server"
}

// Oda listesi
{
  "type": "room_list",
  "rooms": [
    {
      "id": "uuid",
      "name": "My Room",
      "host": "Player1",
      "players": 1,
      "maxPlayers": 2
    }
  ]
}

// Oyuncu katıldı
{
  "type": "player_joined",
  "hostUsername": "Player1",
  "guestUsername": "Player2",
  "status": "playing"
}

// Rakip vuruş yaptı
{
  "type": "opponent_shot",
  "angle": 45.0,
  "power": 0.75,
  "playerRole": "host"
}

// Top sayıları güncellendi
{
  "type": "balls_update",
  "hostBalls": 5,
  "guestBalls": 3
}

// Set bitti
{
  "type": "set_ended",
  "setWinner": "host",
  "currentSet": 2,
  "hostScore": 1,
  "guestScore": 0
}

// Maç bitti
{
  "type": "match_ended",
  "winner": "host",
  "winnerUsername": "Player1",
  "finalScore": "2-1",
  "hostScore": 2,
  "guestScore": 1
}
```

## 🐛 Troubleshooting

### Server bağlanmıyor:
1. Server çalışıyor mu kontrol et (`npm start`)
2. Firewall ayarlarını kontrol et
3. IP adresini doğrula (`ipconfig` - Windows)

### Oda göremiyorum:
1. Username girdiğinden emin ol
2. "Refresh" butonuna tıkla
3. Server loglarını kontrol et

### Build hatası alıyorum:
```bash
./gradlew clean build
```

## 📝 TODO (Gelecek Güncellemeler)

- [ ] Online game ekranı (server-client sync)
- [ ] Skor tablosu UI
- [ ] Maç sonu ekranı
- [ ] Bağlantı kopması handling
- [ ] Reconnect mekanizması
- [ ] Matchmaking sistemi (otomatik eşleşme)

## 🌐 Network Mimarisi

```
┌─────────────────┐
│  Android Client │
│   (Java/Kotlin) │
└────────┬────────┘
         │ WebSocket
         │ (Real-time)
         ▼
┌─────────────────┐
│  Game Server    │
│  (Node.js + WS) │
│  192.168.1.149  │
└────────┬────────┘
         │ HTTP/SQL
         │
         ▼
┌─────────────────┐
│    Supabase     │
│  (PostgreSQL)   │
│ Auth + Lobby +  │
│  Match History  │
└─────────────────┘
```

## 🎉 Önemli Notlar

- Server **geliştirme aşamasında** Windows PC'nde çalışıyor
- Production için cloud hosting (Heroku, Railway, vb.) önerilir
- WebSocket connections sürekli açık kalır (long-lived)
- Room cleanup: 30 dakika inactive odalar otomatik silinir

---

**Developed by**: Antigravity AI
**Project**: Space Billiard Online
**Version**: 1.0.0 (Alpha)
