# Identity Service — Agent Kuralları

Bu dosya; AI destekli geliştirme araçları (Antigravity, Windsurf, Cursor, Claude vb.) için proje bağlamı ve kısıtlamalarını tanımlar.

---

## Proje Kimliği

| Alan | Değer |
|------|-------|
| Proje | GastroBlue Identity Service |
| Tip | Merkezi IAM (Kimlik ve Erişim Yönetimi) Servisi |
| Framework | Spring Boot 4.0.1 / Java 21 / Maven |
| Ana Paket | `com.gastroblue` |
| Port | 7102 |
| Veritabanı | PostgreSQL (JPA/Hibernate) |
| Kimlik Doğrulama | JWT HS256 |
| Bağlı Ürünler | Tracker, FormFlow, CheckApp, Admin-Panel |

---

## Mimari

### Katman Hiyerarşisi

```
Controller  →  Facade  →  Service  →  Repository
```

Bu hiyerarşi **mutlak ve değiştirilemezdir.**

### Kurallar

| Kural | Açıklama |
|-------|----------|
| Tek yön akış | Aynı katmandaki sınıflar birbirini çağıramaz |
| 1 Service = 1 Repository | Bir service birden fazla repository kullanamaz |
| Controller'da entity yok | Controller sadece DTO (request/response) alır/verir |
| Paylaşımsız response | Her service kendi response modelini kullanır |
| Logic Facade'da | İş mantığı Facade'da toplanır, Controller sadece HTTP |
| @Transactional | Sadece Service metodlarında kullanılır |

---

## Domain Bilgisi

### Organizasyon Hiyerarşisi

```
CompanyGroup (Cati Firma)
    └── Company (Alt Firma / Üye İşyeri)
            └── User (Kullanıcı)
```

- **CompanyGroup:** Zincir/grup firma. EULA ve ürün tanımları bu seviyede.
- **Company:** Anlaşmanın yapıldığı alt işyeri.
- **User:** Sisteme giriş yapan kişi. Bir ürüne (`ApplicationProduct`) ve role (`ApplicationRole`) sahiptir.

### Kullanıcı Rolleri

```
ADMIN > GROUP_MANAGER > ZONE_MANAGER > COMPANY_MANAGER > SUPERVISOR > STAFF
```

- Her rol yalnızca bir alt roldeki kullanıcı oluşturabilir
- Ürün ataması yalnızca ADMIN yapabilir (admin-panel üzerinden)
- Diğer kullanıcılar, oluştukları platformun product bilgisiyle otomatik ilişkilendirilir

### Ürünler (ApplicationProduct)

| Ürün | Açıklama |
|------|----------|
| `TRACKER` | Genel takip uygulaması |
| `FORMFLOW` | Form ve iş akışı yönetimi |
| `CHECK` | Kontrol listesi uygulaması |
| `ADMIN_PANEL` | Yönetim paneli |

---

## Kritik Entity'ler

### CompanyGroupProductEntity

Product bazında tüm lisans ve bağlantı bilgilerini içerir:

```java
String companyGroupId
ApplicationProduct product
Boolean enabled           // Ürün aktif mi?
String apiUrl             // Ürünün API adresi
String apiVersion         // Ürün versiyonu
Integer agreedUserCount   // Lisanslı kullanıcı sayısı
LocalDate licenseExpiresAt // Lisans bitiş tarihi
String notes              // Ek notlar
```

### UserProductEntity

Kullanıcının ürün bazındaki detayları:

```java
String userId
ApplicationProduct product
ApplicationRole applicationRole
String departments        // Delimited string
Boolean isActive
LocalDateTime lastSuccessLogin
LocalDateTime eulaAcceptedAt
```

### Auditable (Tüm Entity'lerin Base Class'ı)

```java
String id                 // UUID (auto-generated)
String createdBy
LocalDateTime createdDate
String lastModifiedBy
LocalDateTime lastModifiedDate
Integer version           // Optimistic locking
```

---

## Kod Üretim Kuralları

### Yeni Özellik Eklerken Sıra

1. Repository (gerekiyorsa yeni query)
2. Service (tek bir repository kullan)
3. Facade (service'leri koordine et)
4. Controller (yalnızca HTTP + DTO)
5. Request/Response DTO'ları
6. Validasyon annotation'ları

### Zorunlu Pratikler

- **Lombok** kullan — `@RequiredArgsConstructor`, `@Slf4j`, `@Builder`, `@Getter/@Setter`
- **Jakarta Validation** — request DTO'larında `@NotBlank`, `@NotNull`, `@Valid`
- **SessionUser** — `@AuthenticationPrincipal SessionUser` ile al, immutable record'dur
- **Şifre** — BCrypt encode et, plain text asla
- **Hata mesajı** — DB'den çek (`ErrorMessageEntity`), hardcode yazma
- **E-posta** — `@Async("mailTaskExecutor")` ile gönder

### Kaçınılması Gerekenler

- Controller'da `@Transactional` kullanma
- Bir service metodunda başka bir service'i inject etme
- Entity'yi controller metodundan geçirme veya döndürme
- `ErrorCode` olmayan bir hata string'i hardcode etme
- Dinamik enum değerlerini (Language, Department, City vb.) kodda sabit string olarak yazma

---

## Güvenlik Mimarisi

```
HTTP Request
    → RequestTracingFilter (Trace ID üret, logla)
    → JwtAuthenticationFilter (JWT doğrula, SessionUser oluştur)
    → SecurityConfig (Endpoint yetki kontrolleri)
    → Controller
```

### Public Endpoint'ler

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `GET /actuator/health/**`
- `GET /swagger-ui/**` (APP_SWAGGER_ENABLED=true ise)

---

## Login Akışı Özeti

1. `username` + `password` → `AuthenticationManager` doğrular
2. `UserProductEntity` bulunur (kullanıcı + ürün)
3. `CompanyGroupProductEntity.enabled` kontrol edilir
4. `apiUrl` ve `apiVersion` alınır
5. `passwordChangeRequired` ve EULA durumu kontrol edilir
6. JWT üretilir (claims: `aud`, `role`, `cgId`, `cIds`, `lang`, `dpts`)
7. Response dönülür: `token`, `refreshToken`, `apiUrl`, `apiVersion`, vb.

---

## Önemli Dosyalar

| Dosya | Konum | Amaç |
|-------|-------|------|
| `AuthenticationFacade` | `facade/` | Login akışı |
| `JwtService` | `service/impl/` | Token üretim/doğrulama |
| `SecurityConfig` | `config/` | HTTP güvenlik kuralları |
| `JwtAuthenticationFilter` | `config/` | JWT filtresi |
| `GlobalExceptionHelper` | `exception/helper/` | Global hata yönetimi |
| `SessionUser` | `model/base/` | JWT payload (record) |
| `Auditable` | `model/entity/base/` | Entity base class |
| `application.yaml` | `src/main/resources/` | Tüm konfigürasyonlar |
