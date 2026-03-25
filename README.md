# Identity Service

GastroBlue çatısı altında geliştirilen tüm ürünlerin bağlandığı merkezi kimlik ve erişim yönetimi (IAM) servisidir.

## Genel Bakış

Identity Service; firma tanımları, kullanıcı yönetimi, ürün lisansları ve kimlik doğrulama altyapısını tek çatı altında toplar. Bir firma ile ürün kullanım anlaşması yapıldığında tüm tanımlamalar buradan gerçekleştirilir.

### Desteklenen Ürünler

| Ürün | Açıklama | Durum |
|------|----------|-------|
| **Tracker** | Genel takip uygulaması | Aktif geliştirme |
| **FormFlow** | Form ve iş akışı yönetimi | Planlanan |
| **CheckApp** | Kontrol listesi uygulaması | Planlanan |
| **Admin Panel** | Tüm ürünleri yönetmek için merkezi panel | Planlanan |

---

## Organizasyonel Hiyerarşi

```
CompanyGroup  (örn. Hilton Group)
    └── Company  (örn. Hilton Kemer, Hilton Lara)
            └── User  (Personel, Yöneticiler)
```

### CompanyGroup (Cati Firma)

Bir kuruluşun veya zincirin çatı tanımıdır. Bir firma ile anlaşma yapıldığında önce CompanyGroup oluşturulur.

- EULA içeriği companygroup bazında tanımlanır
- Hangi ürünlerin kullanılabileceği companygroup bazında belirlenir
- Her ürün için API adresi, versiyon, lisans bitiş tarihi ve kullanıcı sayısı bu seviyede tutulur

### Company (Alt Sirket / Üye İşyeri)

Anlaşmanın fiili olarak yapıldığı alt firmadır.

- Bir companygroup altında birden fazla company olabilir
- Enum tanımları (segment, bölge vb.) company bazında aktifleştirilebilir
- Ürün tanımları ve lisanslar companygroup-product üzerinden yönetilir

### Kullanıcı Hiyerarşisi

```
ADMIN
  └── GROUP_MANAGER
        └── ZONE_MANAGER
              └── COMPANY_MANAGER
                    └── SUPERVISOR
                          └── STAFF
```

- Her kullanıcı yalnızca bir alt seviyedeki kullanıcıyı tanımlayabilir
- Ürün ataması yalnızca **ADMIN** tarafından admin-panel aracılığıyla yapılır
- Admin dışındaki kullanıcılar, tanımlandıkları platform için product bilgisiyle ilişkilendirilir

---

## Lisans ve Ürün Tanımı

Bir ürün bir companygroup'a şu bilgilerle tanımlanır:

| Alan | Açıklama |
|------|----------|
| `product` | Ürün adı (TRACKER, FORMFLOW, CHECK, ADMIN_PANEL) |
| `enabled` | Ürün aktif mi? |
| `apiUrl` | Ürünün çalıştığı API adresi |
| `apiVersion` | Ürün versiyonu |
| `agreedUserCount` | Anlaşmadaki kullanıcı sayısı |
| `licenseExpiresAt` | Lisans bitiş tarihi |
| `notes` | Ek notlar |

---

## Login Akışı

```
1. Kullanıcı kimlik bilgilerini gönderir
2. Şifre doğrulanır (BCrypt)
3. Kullanıcının ilgili ürün kaydı (UserProductEntity) kontrol edilir
4. Şifre değiştirilmeli mi? (passwordChangeRequired)
5. EULA imzalanmalı mı? (eulaRequired)
6. JWT ve refresh token üretilir
7. Response: token, refreshToken, apiUrl, apiVersion, passwordChangeRequired, eulaRequired
```

> Login yapan kullanıcının companygroup'una bağlı ürün tanımından `apiUrl` ve `apiVersion` değerleri dönülür.

### JWT Token Yapısı

| Claim | İçerik |
|-------|--------|
| `sub` | Kullanıcı adı |
| `aud` | ApplicationProduct (TRACKER, FORMFLOW vb.) |
| `role` | ApplicationRole |
| `cgId` | CompanyGroup ID |
| `cIds` | Erişilebilir Company ID listesi |
| `lang` | Kullanıcı dili |
| `dpts` | Departmanlar |
| `exp` | Token bitiş zamanı |

- Access Token: 60 dakika (varsayılan)
- Refresh Token: 7 gün (varsayılan)

---

## Katmanlı Mimari

```
Controller  →  Facade  →  Service  →  Repository
```

| Kural | Açıklama |
|-------|----------|
| Tek yön akış | Aynı katmandaki iki sınıf birbirini çağıramaz |
| Service = 1 Repository | Her service yalnızca tek bir repository'e aittir |
| Controller'da entity yok | Controller yalnızca DTO (request/response) alır/verir |
| Kendi response'u | Her service kendi response modelini kullanır, paylaşım yapılmaz |
| Business logic | İş mantığı Facade katmanındadır, Controller yalnızca HTTP |

---

## Teknoloji Stack'i

| Bileşen | Teknoloji |
|---------|-----------|
| Framework | Spring Boot 4.0.1 |
| Dil | Java 21 |
| Build | Maven 3.9 |
| Veritabanı | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Kimlik Doğrulama | JWT (JJWT 0.11.5, HS256) |
| Önbellek | Caffeine + Spring Cache |
| E-posta | Spring Mail (async) |
| API Dokümantasyonu | SpringDoc OpenAPI 2.8.3 (Swagger) |
| Monitoring | Micrometer + Prometheus |
| Kod Formatı | Spotless (Google Java Format) |
| Container | Docker (multi-stage build) |

---

## API Özeti

### Kimlik Doğrulama — `/api/v1/auth`

| Method | Path | Açıklama |
|--------|------|----------|
| POST | `/login` | Giriş, token üretimi |
| POST | `/refresh` | Access token yenileme |
| GET | `/my/info` | Oturum açık kullanıcı bilgisi |
| GET | `/my/company-groups` | Kullanıcının companygroup'ları |
| GET | `/my/companies` | Kullanıcının company'leri |
| GET | `/eula` | EULA içeriği |
| PATCH | `/eula` | EULA imzalama |

### Kullanıcı Tanımlama — `/api/v1/definition/users`

| Method | Path | Açıklama |
|--------|------|----------|
| POST | `/` | Kullanıcı oluştur |
| GET | `/{userId}` | Kullanıcı getir |
| GET | `/{userId}/company-context` | Kullanıcı company bağlamı |
| PUT | `/{userId}` | Kullanıcı güncelle |
| PATCH | `/{userId}/status` | Aktif/pasif değiştir |
| PATCH | `/{userId}/language` | Dil güncelle |
| PATCH | `/{userId}/password` | Şifre değiştir |
| GET | `/{userId}/accessible-users` | Erişilebilir kullanıcılar |

### CompanyGroup Tanımlama — `/api/v1/definition/company-groups`

| Method | Path | Açıklama |
|--------|------|----------|
| GET | `/` | Tüm companygroup'ları listele |
| POST | `/` | CompanyGroup oluştur |
| GET | `/{id}/products` | Group ürünlerini listele |
| POST | `/{id}/products` | Ürün ekle |
| PUT | `/{id}/products/{product}` | Ürün güncelle |
| DELETE | `/{id}/products/{product}` | Ürün kaldır |
| GET | `/context` | Company bağlamı (APP_CLIENT) |
| GET | `/{id}/companies` | Company'leri listele |
| POST | `/{id}/companies` | Company oluştur |

### EULA — `/api/v1/definition/company-groups/{id}/eula`

| Method | Path | Açıklama |
|--------|------|----------|
| GET | `/` | EULA içeriği |
| POST | `/` | Yeni EULA oluştur |
| PUT | `/{id}` | EULA güncelle |

### Konfigürasyon — `/api/v1/configuration`

| Method | Path | Açıklama |
|--------|------|----------|
| GET/POST/PUT | `/enums` | Dinamik enum yönetimi |
| GET/POST/PUT | `/error-messages` | Hata mesajı yönetimi |

### Diğer

| Method | Path | Açıklama |
|--------|------|----------|
| POST | `/api/v1/eligibility/check` | Ürün erişim kontrolü |
| GET | `/actuator/health` | Sağlık kontrolü |
| GET | `/actuator/prometheus` | Metrik toplayıcı |

---

## Ortam Değişkenleri

| Değişken | Varsayılan | Açıklama |
|----------|-----------|----------|
| `DATABASE_HOST` | `localhost` | PostgreSQL adresi |
| `DATABASE_PORT` | `5432` | PostgreSQL portu |
| `DATABASE_NAME` | `identity` | Veritabanı adı |
| `DATABASE_USERNAME` | `identity_admin` | Veritabanı kullanıcısı |
| `DATABASE_PASSWORD` | `postgres` | Veritabanı şifresi |
| `JWT_SECRET_KEY` | — | JWT imza anahtarı **(zorunlu)** |
| `JWT_TOKEN_VALIDITY_IN_MINUTES` | `60` | Access token süresi |
| `JWT_REFRESH_TOKEN_VALIDITY_IN_DAYS` | `7` | Refresh token süresi |
| `TT_TOKEN` | — | Tracker servis token'ı |
| `FF_TOKEN` | — | FormFlow servis token'ı |
| `ADMIN_TOKEN` | — | Admin servis token'ı |
| `SERVER_PORT` | `7102` | Uygulama portu |
| `MAIL_ENABLED` | `true` | E-posta gönderimi aktif mi? |
| `MAIL_FROM` | — | Gönderici e-posta adresi |
| `MAIL_PASSWORD` | — | SMTP şifresi |
| `MAIL_SMTP_HOST` | `smtp.gmail.com` | SMTP sunucusu |
| `MAIL_SMTP_PORT` | `587` | SMTP portu |
| `MAIL_ADMIN_REDIRECT_ADDRESS` | — | Admin yönlendirme adresi |
| `APP_SWAGGER_ENABLED` | `true` | Swagger UI aktif mi? |
| `APP_ADMIN_REGISTRATION_ENABLED` | `true` | Admin kayıt aktif mi? |
| `SPRING_PROFILES_ACTIVE` | — | Aktif Spring profili |

---

## Kurulum ve Çalıştırma

### Docker Compose ile

```bash
# .env dosyasını oluştur
cp .env.example .env
# Gerekli değerleri doldur (JWT_SECRET_KEY, mail ayarları vb.)

# Servisi başlat
docker compose up -d

# Logları izle
docker compose logs -f identity
```

Uygulama varsayılan olarak `http://localhost:7102` adresinde çalışır.

### Yerel Geliştirme

```bash
# Bağımlılıkları indir
./mvnw dependency:go-offline

# Uygulamayı başlat
./mvnw spring-boot:run

# Testleri çalıştır
./mvnw test

# Paket oluştur (kod formatı da uygulanır)
./mvnw clean package
```

### Swagger UI

`APP_SWAGGER_ENABLED=true` iken: `http://localhost:7102/swagger-ui.html`

---

## Hata Yönetimi

Tüm hatalar `GlobalExceptionHelper` üzerinden yakalanır ve aşağıdaki formatta dönülür:

```json
{
  "errorCode": "USER_NOT_FOUND",
  "message": "Kullanıcı bulunamadı",
  "httpStatus": 404,
  "timeStamp": "2026-01-01T12:00:00",
  "traceId": "550e8400-e29b-41d4-a716-446655440000"
}
```

Hata mesajları veritabanından dil bazında çekilir (`ErrorMessageEntity`).

---

## Proje Yapısı

```
src/main/java/com/gastroblue/
├── annotations/        # Özel validasyon annotation'ları
├── config/             # Spring konfigürasyonları (Security, JWT, Mail, Cache)
│   └── tracing/        # Request izleme ve loglama
├── controller/         # REST endpoint'leri
├── exception/          # Özel exception sınıfları
│   └── helper/         # GlobalExceptionHelper
├── facade/             # İş mantığı koordinasyon katmanı
├── mapper/             # Entity ↔ DTO dönüştürücüler
├── model/
│   ├── base/           # SessionUser (immutable record)
│   ├── entity/         # JPA entity'leri
│   │   └── base/       # Auditable (id, audit alanları)
│   ├── enums/          # Sabit ve dinamik enum'lar
│   ├── request/        # İstek DTO'ları
│   └── response/       # Yanıt DTO'ları
├── repository/         # Spring Data JPA repository'leri
├── service/            # Servis arayüzleri
│   └── impl/           # Servis implementasyonları
└── util/               # Yardımcı sınıflar
```
