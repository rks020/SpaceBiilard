# 🐛 Online Mode Bug Fixes

## ✅ Düzeltilen Sorunlar

### Problem: Odaya katılamıyorum ve oda bir süre sonra yok oluyor

**Tespit Edilen Hatalar:**

1. **Duplicate Join Kontrolü Yoktu**
   - Client zaten bir odadayken tekrar join yapabiliyordu
   - Bu duplicate room entries oluşturuyordu

2. **Room Cleanup Çok Saldırgan**
   - 30 dakika timeout çok kısaydı
   - Test sırasında odalar hemen siliniyor

3. **Yetersiz Logging**
   - Server'da ne olduğunu görmek zordu
   - Debug yapmak imkansızdı

## 🔧 Yapılan Düzeltmeler

### 1. Server Tarafı (server.js)

#### `joinRoom()` Fonksiyonu
```javascript
// ✅ Eklenen kontroller:
- Client zaten bu odada mı? → Hata ver
- Client başka bir odada mı? → Önce oradan çık
- Oda dolu mu? → Hata ver
- Detaylı logging (join request, success, errors)
```

#### `createRoom()` Fonksiyonu
```javascript
// ✅ Eklenen kontroller:
- Client zaten odada mı? → Önce oradan çık
- Daha iyi logging (room ID kısaltıldı)
- Room count tracking
```

#### Room Cleanup
```javascript
// ❌ Önce: 30 dakika timeout
const timeout = 30 * 60 * 1000;

// ✅ Şimdi: 2 saat timeout
const timeout = 2 * 60 * 60 * 1000;

// + Cleanup sonrası broadcast
// + Detaylı logging (oda yaşı gösteriliyor)
```

### 2. Android Client Tarafı (OnlineActivity.java)

#### Auto-Refresh
```java
// ✅ Oda oluşturulduktan sonra:
- Room name input temizleniyor
- 500ms sonra otomatik room list refresh
- Toast mesajı LONG olarak gösteriliyor

// ✅ Odaya katıldıktan sonra:
- 500ms sonra otomatik room list refresh
- Updated room status gösteriliyor
```

## 📊 Server Logging (Yeni)

### Artık şunları görüyoruz:

```bash
# Client bağlandığında:
✅ New client connected: abc123...

# Oda oluşturulduğunda:
🏠 Room created: "Test Room" (4f3a2b1c...) by Kerem
📊 Total rooms: 1, Status: waiting

# Join isteği geldiğinde:
🔍 Join request: Player2 trying to join room 4f3a2b1c...
✅ Player2 joined room Test Room (Status: playing)
📊 Rooms count: 1, Clients count: 2

# Hatalar:
❌ Room not found: xyz789...
❌ Room is full: Test Room
⚠️  Player already in room Test Room
```

## 🧪 Test Senaryoları

### Senaryo 1: Normal Join
1. Kerem oda oluşturur: "Test Room"
2. Server confirmation: ✅ Room created
3. Rauf odaya katılır
4. Server: ✅ Rauf joined
5. Her iki client'a `player_joined` broadcast edilir

### Senaryo 2: Duplicate Join Prevention
1. Kerem "Test Room" oluşturur
2. Kerem aynı odaya tekrar join dener
3. Server: ⚠️ Already in room
4. Client'a error mesajı gönderilir

### Senaryo 3: Room Full
1. Kerem oda oluşturur
2. Rauf katılır (2/2)
3. 3. oyuncu katılmaya çalışır
4. Server: ❌ Room is full

### Senaryo 4: Switch Rooms
1. Kerem "Room A" oluşturur
2. Kerem "Room B" oluşturmaya çalışır
3. Server otomatik "Room A"dan çıkarır
4. Server "Room B"yi oluşturur
5. Başarılı!

## 📱 Client Davranışı

### CREATE ROOM Butonu
```
Tıkla → Server'a create_room gönder
       → Input temizle
       → 500ms bekle
       → Room list refresh (yeni oda görünür)
       → Toast: "Room created: X"
```

### JOIN Butonu
```
Tıkla → Server'a join_room gönder
       → 500ms bekle
       → Room list refresh (oda "playing" olur, listeden kaybolur)
       → Toast: "Joining room: X"
```

### REFRESH Butonu
```
Tıkla → Server'dan list_rooms iste
       → Room list güncellenir
       → Sadece "waiting" odalar gösterilir
```

## 🔍 Debug İpuçları

### Server loglarını görmek için:
```bash
cd game-server
npm start

# Şunları göreceksin:
🚀 Server started...
✅ New client connected: ...
🏠 Room created: ...
🔍 Join request: ...
```

### Client loglarını görmek için (Android Logcat):
```
Filtre: "OnlineGameManager"

D/OnlineGameManager: WebSocket connected
D/OnlineGameManager: Client ID: abc123...
D/OnlineGameManager: Received message: room_created
D/OnlineGameManager: Room created: 4f3a2b1c...
```

## ✅ Test Checklist

- [ ] Server başlatıldı mı?
- [ ] Client server'a bağlanabiliyor mu?
- [ ] Username set ediliyor mu?
- [ ] Oda oluşturulabiliyor mu?
- [ ] Oda listede görünüyor mu?
- [ ] Odaya katılınabiliyor mu?
- [ ] JOIN sonrası oda listeden kayboluyor mu? (çünkü "playing" oluyor)
- [ ] 2. oyuncu katıldığında her iki client `player_joined` alıyor mu?
- [ ] Duplicate join engelleniyor mu?
- [ ] Room full hatası çalışıyor mu?

## 🎯 Beklenen Davranış

1. **Oda Oluştur** → Oda listede görünür (waiting)
2. **Odaya Katıl** → Oda listeden kaybolur (playing oldu)
3. **Refresh** → Sadece waiting odalar gösterilir
4. **2 Saat** → Timeout, waiting odalar silinir

### Neden "playing" odalar listede yok?
```javascript
// server.js line 294
if (room.status === 'waiting') {
    availableRooms.push(room);
}
```

**Çünkü**: Playing odalar "available" değil, dolu!

## 🚀 Şimdi Dene!

1. Server'ı başlat:
   ```bash
   cd game-server
   npm start
   ```

2. Android Studio'da Build:
   ```bash
   ./gradlew assembleDebug
   ```

3. Uygulamayı çalıştır ve test et!

---

**Notlar:**
- Odalar artık 2 saat boyunca yaşıyor (test için yeterli)
- Production'da daha kısa timeout kullanılabilir
- Server logları çok detaylı, debug kolay!
