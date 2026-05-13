# identity-service — Claude Context

## Project Overview

GastroBlue kimlik doğrulama ve yetkilendirme servisi. Spring Boot 4.0.6 / Java 21 / PostgreSQL.
Diğer servisler (tracker, formflow) bu servise JWT doğrulama için bağımlıdır.

**Koordinatlar**
- Port: `7102` (varsayılan)
- Base package: `com.gastroblue`
- Maven group: `com.gastroblue`, artifactId: `identity-service`, version: `1.0.0`

---

## Bağımlılık Yapısı

```
gastroblue-parent (1.0.4)
  └─ extends spring-boot-starter-parent:4.0.6
       └─ identity-service
            ├─ gastroblue-commons:1.7.0   (ortak kütüphane — GitHub Packages)
            └─ postgresql (runtime)
```

**`gastroblue-commons`** auto-configuration ile çalışır (`AutoConfiguration.imports`). İçerdiği
modüller:
- `JwtService` / `IJwtService` — EC P-256 JWT imzalama ve doğrulama
- `SecurityAutoConfig` — Spring Security filter zinciri, `JwtAuthenticationFilter`
- `MailService` / `IMailService` — SMTP mail gönderimi
- `LookupService` / `ILookupService` — kod→label çevirisi (cache destekli)
- `ExceptionAutoConfig` — `@ControllerAdvice` hata yanıtları
- `SwaggerAutoConfig` — OpenAPI / Swagger UI
- `Auditable` — tüm entity'lerin kalıtım aldığı taban sınıf
- `TracingAutoConfig` — istek izleme

Commons kaynak kodu: `D:\git-clones\gastroblue-commons`

---

## Mimari — Katman Kuralları

```
Controller  →  Facade  →  Service  →  Repository
```

| Katman | Sorumluluk | Kural |
|--------|-----------|-------|
| **Controller** | HTTP mapping, request/response | İş mantığı YOK; sadece facade çağrısı |
| **Facade** | İş orchestration, yetki kontrolü | `@Transactional` burada; birden fazla service çağrısı burada birleşir |
| **Service** | Tek bir aggregate üzerinde CRUD | Başka service'e bağımlılık yok; sadece kendi repository'si |
| **Repository** | JPA/JPQL sorguları | Sadece veri erişimi |

- Facade → Service arası bağımlılık OK.
- Service → Service bağımlılığı **yasak** (circular risk + test zorluğu).
- Controller asla `@Transactional` almaz.

---

## Paket Yapısı

```
com.gastroblue
├── annotations/validation/   Özel Bean Validation anotasyonları (@ValidPhoneNumber vb.)
├── client/                   Dış servis HTTP istemcileri (Expo push)
├── config/                   Spring @Configuration sınıfları
├── controller/               @RestController'lar
├── facade/                   İş orchestration (@Service)
├── mapper/                   Entity↔DTO dönüşümleri (static metotlar)
├── model/
│   ├── base/                 Paylaşılan hafif DTO'lar (Company, CompanyGroup vb.)
│   ├── entity/               JPA entity'leri
│   ├── enums/                ErrorCode, MailTemplate, MailParameters, Lookups
│   ├── request/              Gelen DTO'lar (Java records, Bean Validation)
│   └── response/             Giden DTO'lar (Lombok veya record)
├── repository/               Spring Data JPA repository'leri
├── service/                  Tek-aggregate servisler
└── util/                     Yardımcı sınıflar (PasswordGenerator, EmailDomainValidator)
```

---

## Entity Kuralları

### Taban Sınıf — `Auditable`

Tüm entity'ler `Auditable`'dan kalıtım alır. Auditable alanları:

| Alan | DB Kolonu | Tip |
|------|-----------|-----|
| `id` | `ID` VARCHAR(36) PK | `@UuidGenerator` — UUID, otomatik atanır |
| `createdBy` | `CREATED_BY` | `@CreatedBy` |
| `createdDate` | `CREATED_DATE` | `@CreatedDate` |
| `lastModifiedBy` | `LAST_MODIFIED_BY` | `@LastModifiedBy` |
| `lastModifiedDate` | `LAST_MODIFIED_DATE` | `@LastModifiedDate` |
| `version` | `VERSION` BIGINT | `@Version` — optimistic locking |

### Tablo / Kolon İsimlendirmesi

- Tablo isimleri: `BÜYÜK_HARF_SNAKE_CASE` (örn. `COMPANY_GROUPS`, `USERS`)
- Kolon isimleri: `BÜYÜK_HARF_SNAKE_CASE` (örn. `COMPANY_GROUP_ID`, `IS_ACTIVE`)
- Boolean kolonlar: `IS_` ön eki (örn. `IS_ACTIVE`, `IS_ENABLED`)
- Timestamp kolonlar: `_AT` son eki (örn. `LOCKED_UNTIL`, `PASSWORD_EXPIRES_AT`)

### Kolon Sıralaması (entity field tanım sırası)

1. Audit alanları (`Auditable`'dan gelir — kodda tekrar yazılmaz)
2. Foreign key'ler (`COMPANY_GROUP_ID`, `COMPANY_ID`)
3. Kimlik / anahtar alanlar (`USERNAME`, `EMAIL`)
4. Detay alanlar (`NAME`, `SURNAME`, `PHONE`, `GENDER`, `ZONE`)
5. Durum alanları (`PASSWORD_CHANGE_REQUIRED`, `PASSWORD_EXPIRES_AT`, `PASSWORD_VERSION`)
6. Aktiflik / pasiflik alanı en sona (`IS_ACTIVE`, `IS_ENABLED`)

### Yeni Kolon Eklerken

```java
@Column(name = "MY_COLUMN", nullable = false)
@Builder.Default
private int myField = 0;   // mevcut kayıtlar için güvenli default
```

`ddl-auto: update` → kolon otomatik eklenir ama `NOT NULL` + default için var olan satırları
güncellemek gerekebilir:
```sql
UPDATE USERS SET MY_COLUMN = 0 WHERE MY_COLUMN IS NULL;
```

---

## Güvenlik Modeli

### JWT — EC P-256 Asimetrik Anahtar

- **Private key** (`EC_PRIVATE_KEY`): imzalama — sadece identity-service'te
- **Public key** (`EC_PUBLIC_KEY`): doğrulama — tüm servislerde commons üzerinden

Token tipler: access token (60 dk) + refresh token (7 gün). Her ikisi de aynı `extraClaims`
yapısını taşır.

### JWT Claim'leri (`IJwtService` sabitleri)

| Claim Adı | Sabit | Açıklama |
|-----------|-------|---------|
| `uid` | `JWT_USER_ID` | UUID |
| `sub` | `SUBJECT` | username |
| `prd` | `JWT_APPLICATION_PRODUCT` | `ApplicationProduct` enum adı |
| `rol` | `JWT_ROLE` | `ApplicationRole` enum adı |
| `cgId` | `JWT_COMPANY_GROUP_ID` | Şirket grubu UUID |
| `cIds` | `JWT_COMPANY_IDS` | Sorumlu şirket UUID listesi |
| `lang` | `JWT_LANGUAGE` | `Language` enum adı |
| `depts` | `JWT_DEPARTMENTS` | Departman listesi |
| `pwdVer` | `JWT_PASSWORD_VERSION` | Şifre versiyonu (revocation) |

### Password Version (Stateless Token Revocation)

`UserEntity.passwordVersion` (integer, default 1) JWT'ye `pwdVer` claim olarak yazılır.

- **Şifre değişince** `passwordVersion++` yapılır → eski refresh token'lar geçersiz olur.
- **Logout'ta** `passwordVersion++` yapılır → o kullanıcının tüm tokenları geçersiz olur.
- `refreshToken()` içinde: `sessionUser.passwordVersion() != userEntity.getPasswordVersion()` →
  `INVALID_JWT_TOKEN` hatası.
- Null check zorunlu (eski tokenlar `pwdVer` taşımayabilir).

### Rate Limiting / Account Locking

`UserEntity` üzerinde:
- `loginAttemptCount` — başarısız giriş sayacı (5 denemede hesap kilitlenir)
- `lockedUntil` — kilit bitiş zamanı (15 dakika)
- `isAccountNonLocked()` → `lockedUntil == null || lockedUntil.isBefore(now())`

`UserDefinitionService.incrementLoginAttempts()`:
- `loginAttemptCount >= 5` → `lockedUntil = now() + 15 min`, counter sıfırlanır
- Aksi → counter artırılır

### Sistem Token'ları (Servisler Arası)

`application.yaml` → `gastroblue.commons.security.sys-tokens` altında statik token'lar:
- `TRACKER_TOKEN` — Tracker servisi için
- `FORMFLOW_TOKEN` — Formflow servisi için
- `ADMIN_TOKEN` — Admin panel için (product: CHECK)

---

## Rol Hiyerarşisi

```
ADMIN > GROUP_MANAGER > ZONE_MANAGER > COMPANY_MANAGER > SUPERVISOR > STAFF
```

- `ADMIN`: Tüm şirket gruplarına erişir.
- `GROUP_MANAGER`: Kendi şirket grubuna (companyGroupId) erişir; tüm şirketler görünür.
- `ZONE_MANAGER`: Kendi zone'undaki şirketler (`UserEntity.zone == CompanyEntity.zone`).
- `COMPANY_MANAGER / SUPERVISOR / STAFF`: Tek şirket (companyId).

`isAdministrator()` → `ADMIN` veya `GROUP_MANAGER` için `true` döner.

### Yönetilebilir Roller

Bir rol yalnızca kendisinden **düşük** roldeki kullanıcıları yönetebilir:
- `ADMIN` → GROUP_MANAGER yönetir
- `GROUP_MANAGER` → ZONE_MANAGER, COMPANY_MANAGER
- `ZONE_MANAGER` → COMPANY_MANAGER
- `COMPANY_MANAGER` → SUPERVISOR
- `SUPERVISOR` → STAFF

---

## Multi-Tenancy

- **Yatay izolasyon**: `companyGroupId` bazlı. Her şirket grubu bağımsız bir kiracıdır.
- ADMIN hariç tüm sorgular `companyGroupId` filtresi içerir.
- `SessionUser.companyGroupId()` — token'dan gelir; DB doğrulaması yapılmaz (performans).
- ADMIN tokenları boş `companyIds` listesi taşır → tüm gruplara erişim anlamına gelir.

---

## Kodlama Kuralları

### Format

- **Google Java Format** — Spotless Maven plugin ile zorlanır (`mvn spotless:apply`).
- Build'den önce `spotless:check` çalışır; format hatası varsa build patlar.
- Yeni bir şey yazınca mutlaka `./mvnw spotless:apply` çalıştır.

### DTO Kuralları

- **Request DTO'ları**: Java `record` + Bean Validation.
  ```java
  public record UserSaveRequest(
      @NotBlank String username,
      @Email String email) {}
  ```
- **Response DTO'ları**: Lombok `@Data` / `@Builder` veya record. Builder tercih edilir.
- İstek/cevap DTO'larında asla JPA entity'si kullanılmaz.

### Mapper Kuralları

- `mapper/` paketindeki sınıflarda **static metotlar** (inject edilmez, yeni instance yaratılmaz).
- `UserMapper.toResponse(entity, userProduct, lookupService)` gibi parametre ile.
- MapStruct **kullanılmıyor** — manuel dönüşüm.

### Transactional Kullanımı

- Sadece yazma işlemleri: `@Transactional` (Facade veya Service'te).
- Okuma işlemleri: `@Transactional(readOnly = true)` — gereksiz write-lock'u önler.
- Controller'da `@Transactional` **yok**.

### Hata Yönetimi

Commons'tan gelen exception tipleri:
- `NotFoundException` → 404
- `AccessDeniedException` → 403
- `BusinessException` → 400

Her exception bir `ErrorCode` (enum, `ErrorCodeBase` implement eder) alır.
Yeni hata eklerken `ErrorCode.java`'ya enum sabiti eklenir, başka bir şey değişmez.

### Loglama

- Facade seviyesinde `@Slf4j` ile `log.info(...)` / `log.error(...)`.
- Log mesajlarında **PII yazılmaz** (şifre, tam isim, e-posta içeriği gibi şeyler).
- `UserEntity` → `@ToString` ile `password` field'ı `@ToString.Exclude`.

---

## Önemli Servisler

### `AuthenticationFacade`

- `login()` — kimlik doğrulama, token üretimi, lisans kontrolü
- `refreshToken()` — token yenileme + `passwordVersion` kontrolü
- `logout()` — `passwordVersion++` ile token invalidation
- `buildExtraClaims()` — JWT claim map'ini oluşturur (tüm claim'ler burada bir arada)

### `UserDefinitionFacade`

- `saveUser()` — kullanıcı kaydı (rol hiyerarşisi + şirket grubu domain doğrulaması)
- `updateUser()` — güncelleme (rol hiyerarşisi kontrolü)
- `changePassword()` — şifre değiştirme; `passwordVersion` artırılır
- `sendOtp()` — OTP ile şifre sıfırlama; `passwordVersion` artırılır
- `findAccessibleUsers()` — role göre erişilebilir kullanıcı listesi

### `TokenGenerationService`

`AuthenticationFacade` ve `UserDefinitionFacade` tarafından kullanılır.
`IJwtService.generateToken(subject, extraClaims, expiryMs)` wrapper'ı.

---

## Çevre Değişkenleri

Tüm değişkenler `application.yaml`'da `${ENV_VAR:default}` formatında tanımlıdır.
Tam liste ve test değerleri `test.env` dosyasında mevcuttur.

Kritik değişkenler:

| Değişken | Açıklama |
|----------|---------|
| `EC_PRIVATE_KEY` | Base64 PKCS8 EC private key (JWT imzalama) |
| `EC_PUBLIC_KEY` | Base64 X.509 EC public key (JWT doğrulama) |
| `DATABASE_*` | PostgreSQL bağlantısı |
| `TRACKER_TOKEN` | Tracker servisinin kimlik doğrulama token'ı |
| `FORMFLOW_TOKEN` | Formflow servisinin kimlik doğrulama token'ı |
| `ADMIN_TOKEN` | Admin panel token'ı |
| `MAIL_ENABLED` | `false` → mail gönderilmez (test için) |
| `APP_ADMIN_REGISTRATION_ENABLED` | `true` → ADMIN rolünde kullanıcı kaydı açık |

---

## Deployment

```bash
# Test ortamında çalıştırma (env file ile)
./mvnw spring-boot:run -Dspring-boot.run.envFile=test.env

# Docker
docker compose --env-file test.env up --build
```

Docker Compose `docker-compose.yml` → `network_infra` external network'e bağlanır.
Remote debug portu: `17102`.

---

## Veritabanı

- `ddl-auto: update` — entity değişiklikleri otomatik uygulanır.
- Timezone: `Europe/Istanbul` (Hikari `connection-init-sql` ile set edilir).
- Connection pool: min 5, max 20 (HikariCP).
- **Flyway** şu an entegre değil (`pom.xml`'de bağımlılık yok).
  Migration SQL dosyaları `src/main/resources/db/migration/` altında hazır bekliyor:
  - `V1__init.sql` — tam şema (`IF NOT EXISTS`)
  - `V2__add_rate_limiting_and_password_version.sql` — yeni kolonlar
  Flyway eklendiğinde `pom.xml`'e `flyway-core` + `application.yaml`'a config eklenmeli.

---

## Bekleyen / Dikkat Gerektiren Konular

1. **`ErrorCode.PASSWORD_SAME_AS_OLD` eksik** — `UserDefinitionFacade.changePassword()` içinde
   bu constant'a referans var ama `ErrorCode.java`'da tanımlı değil. Derleme hatası verebilir;
   `ErrorCode`'a `PASSWORD_SAME_AS_OLD` eklenmeli.

2. **`CompanyGroupService.findMyCompanyGroups()` — ZONE_MANAGER eksik** — switch case'de
   `ZONE_MANAGER` branch'i yok. Bu rol için boş liste dönüyor. Düzeltmek için
   `GROUP_MANAGER, COMPANY_MANAGER, SUPERVISOR` ile aynı branch'e eklenmeli.

3. **Flyway entegrasyonu** — `test.env` + `CLAUDE.md` → Flyway yapılandırması için hazır.
   `IF NOT EXISTS` kullanıldığından mevcut DB üzerinde de güvenle çalışır.
