# ICard iNFODİF

Personelin QR kod doğrulaması ile güvenli bir şekilde giriş-çıkış yapabildiği, web tabanlı bir personel takip sistemi. InfoDif bünyesinde yaptığım zorunlu staj kapsamında, backend'den frontend'e kadar uçtan uca geliştirdim.

## Özellikler

### Kimlik Doğrulama & Yetkilendirme
- JWT tabanlı kimlik doğrulama
- Rol bazlı yetkilendirme (`EMPLOYEE` / `MANAGER` / `ADMIN`)
- Spring Security ile method-level security (`@PreAuthorize`)

### QR Kod ile Giriş-Çıkış
- UUID tabanlı, 2 dakika geçerlilik süreli QR kod üretimi
- QR kodun PNG olarak mail ile gönderimi (Spring Mail + ZXing)
- Tarayıcı üzerinden webcam ile QR okutma
- Tek kullanımlık QR doğrulama, otomatik Attendance kaydı oluşturma

### Attendance (Giriş-Çıkış) Yönetimi
- Check-in / check-out akışı, aynı anda tek aktif kayıt kuralı
- Çalışılan süre, geç kalma ve fazla mesai hesaplaması
- Departman/şirket bazlı mesai kuralları (`WorkSchedule`)

### Dashboard
- Haftalık/aylık çalışma istatistikleri
- Haftalık çalışma saati grafiği (hafta bazında gezinme)
- Server-side pagination
- Specification API ile dinamik filtreleme (`EQ`, `LIKE`, `IN`, `BETWEEN`, `DATE_BETWEEN`)

### Organizasyon Yönetimi
- Departman oluşturma/düzenleme, departman bazlı istatistikler
- Yönetici-ekip ilişkisi, ekip bazlı Attendance ve izin görünümü
- İzin talebi oluşturma, onay/red akışı (mail bildirimli)
- Admin paneli: kullanıcı, departman ve mesai kuralı yönetimi

## Teknoloji Yığını

**Backend**
- Java, Spring Boot, Spring Security, Spring Data JPA / Hibernate
- PostgreSQL
- JWT (jjwt)
- Spring Mail, ZXing (QR kod üretimi)
- Maven

**Frontend**
- React, TypeScript, Vite
- React Router, Axios
- Ant Design, Ant Design Charts

## Proje Yapısı

```
icard-backend/     Spring Boot REST API
icard-frontend/    React + TypeScript istemci
```

## Kurulum

### Backend

1. PostgreSQL üzerinde bir veritabanı oluşturun.
2. `icard-backend/src/main/resources/application.properties.example` dosyasını `application.properties` olarak kopyalayın ve kendi bilgilerinizle doldurun:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/icard_db
   spring.datasource.username=...
   spring.datasource.password=...

   jwt.secret=...
   jwt.expiration=86400000

   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=...
   spring.mail.password=...
   ```
3. Projeyi çalıştırın:
   ```bash
   cd icard-backend
   mvn spring-boot:run
   ```

### Frontend

1. `icard-frontend` klasöründe bir `.env` dosyası oluşturun:
   ```
   VITE_API_BASE_URL=http://localhost:8080/api
   ```
2. Bağımlılıkları kurup projeyi çalıştırın:
   ```bash
   cd icard-frontend
   npm install
   npm run dev
   ```

## API Genel Bakış

| Alan | Örnek Endpoint |
|---|---|
| Kimlik Doğrulama | `POST /api/auth/register`, `POST /api/auth/login` |
| QR Kod | `POST /api/qr/generate`, `POST /api/qr/verify` |
| Attendance | `POST /api/attendance/checkout`, `POST /api/attendance/filter` |
| Dashboard | `GET /api/dashboard/stats`, `GET /api/dashboard/weekly-chart` |
| Departman | `POST /api/admin/departments`, `GET /api/departments/{id}/stats` |
| İzin Yönetimi | `POST /api/leaves`, `PUT /api/manager/leaves/{id}/approve` |
| Mesai Kuralları | `POST /api/admin/work-schedules` |
| Yönetici Paneli | `GET /api/manager/team`, `GET /api/manager/team/attendance` |

## Notlar

Bu proje, bir staj çalışması olarak geliştirilmiştir. Üretim ortamında kullanılmadan önce ek güvenlik ve performans incelemelerinden geçirilmesi önerilir.

## Lisans

Bu proje eğitim amaçlı geliştirilmiştir.
