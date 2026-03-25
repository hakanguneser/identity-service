# Identity Service — Claude Code Kuralları

## Proje Bağlamı

GastroBlue çatısı altında geliştirilen tüm ürünlerin (Tracker, FormFlow, CheckApp, Admin-Panel) bağlandığı merkezi IAM servisidir.

- **Framework:** Spring Boot 4.0.1
- **Dil:** Java 21
- **Paket:** `com.gastroblue`
- **Port:** 7102
- **DB:** PostgreSQL (JPA/Hibernate, `ddl-auto: update`)
- **Auth:** JWT HS256, audience = `ApplicationProduct`

---

## Mimari Kurallar (KESİN — İhlal Edilemez)

### Katman Hiyerarşisi

```
Controller  →  Facade  →  Service  →  Repository
```

1. **Aynı katmandaki sınıflar birbirini çağıramaz.**
   - Bir Controller başka bir Controller'ı çağıramaz.
   - Bir Facade başka bir Facade'ı çağıramaz.
   - Bir Service başka bir Service'i çağıramaz.

2. **Her Service yalnızca tek bir Repository'e aittir.**
   - `UserDefinitionService` sadece `UserRepository` kullanır.
   - Birden fazla repository erişimi gerekiyorsa, bu iş Facade katmanında koordine edilir.

3. **Controller'da entity alınamaz/verilemez.**
   - Controller metodlarının parametreleri ve dönüş tipleri yalnızca DTO (request/response) olmalıdır.
   - Entity'ler hiçbir zaman controller metoduna girmemeli veya controller'dan çıkmamalıdır.

4. **Her Service kendi request/response modellerini kullanır.**
   - Servisler arası response paylaşımı yapılmaz.
   - Ortak kullanım ihtiyacı varsa yeni bir DTO oluşturulur veya Facade'da dönüşüm yapılır.

5. **İş mantığı Facade katmanındadır.**
   - Controller yalnızca HTTP katmanıdır: validasyon, yönlendirme ve response dönme.
   - Facade; service'leri koordine eder, iş akışlarını yönetir.

---

## Domain Modeli

### Organizasyonel Hiyerarşi

```
CompanyGroup  →  Company  →  User
```

- **CompanyGroup:** Cati firma (örn. Hilton Group). EULA ve ürün tanımları bu seviyede.
- **Company:** Alt firma / üye işyeri (örn. Hilton Kemer).
- **User:** Sistemdeki kullanıcı. Her kullanıcı bir ürünle (`ApplicationProduct`) ilişkilidir.

### Kullanıcı Hiyerarşisi (Rol Bazlı)

```
ADMIN > GROUP_MANAGER > ZONE_MANAGER > COMPANY_MANAGER > SUPERVISOR > STAFF
```

- Her rol yalnızca bir alt roldeki kullanıcı tanımlayabilir.
- **Ürün ataması yalnızca ADMIN tarafından yapılır** (admin-panel üzerinden).
- Diğer kullanıcılar oluştukları platform için otomatik product ile ilişkilendirilir.

### Temel Entity'ler

| Entity | Tablo | Açıklama |
|--------|-------|----------|
| `UserEntity` | `USERS` | Kullanıcı kimlik bilgileri |
| `UserProductEntity` | `USER_PRODUCTS` | Kullanıcının ürün bazında rol ve departman bilgisi |
| `CompanyGroupEntity` | `COMPANY_GROUPS` | Cati firma tanımı |
| `CompanyEntity` | `COMPANIES` | Alt firma |
| `CompanyGroupProductEntity` | `COMPANY_GROUP_PRODUCTS` | Ürün lisansı: apiUrl, apiVersion, agreedUserCount, licenseExpiresAt, enabled |
| `CompanyGroupEulaContentEntity` | `COMPANY_GROUP_EULA_CONTENTS` | EULA versiyonları |
| `EnumValueConfigurationEntity` | `ENUM_VALUE_CONFIGURATIONS` | Dinamik enum değerleri |
| `ErrorMessageEntity` | `ERROR_MESSAGES` | Dil bazlı hata mesajları |

---

## Kod Yazım Kuralları

### Genel

- **Lombok** kullan: `@Data`, `@RequiredArgsConstructor`, `@Slf4j`, `@Builder`, `@Getter`, `@Setter`
- **Spotless** (Google Java Format): Kod `mvn package` sırasında otomatik formatlanır. Yeni kod yazarken formata uy.
- **Jakarta Validation** annotation'ları ile request DTO'larını valide et (`@NotBlank`, `@NotNull`, `@Valid` vb.)
- `@Transactional` annotation'ı service metodlarında kullanılır (facade ve controller'da değil)
- E-posta gönderimi `@Async("mailTaskExecutor")` ile asenkron yapılır

### Güvenlik

- Controller metodlarında oturum bilgisi için: `@AuthenticationPrincipal SessionUser sessionUser`
- `SessionUser` immutable bir record'dur — doğrudan kullan, kopyalama
- Şifreleme: `BCryptPasswordEncoder` — plain text şifre asla saklanmaz

### Enum Yönetimi

- **Sabit enum'lar:** `ApplicationRole`, `ApplicationProduct` — kod içinde direkt kullanılır
- **Dinamik enum'lar:** `Language`, `Department`, `Country`, `City`, `Zone`, `CompanySegment1-5` — DB'den gelir (`EnumValueConfigurationEntity`), companygroup bazında aktifleştirilebilir
- Dinamik enum değerlerine doğrudan kod içinde referans verme

### Hata Yönetimi

- Özel exception'lar: `IllegalDefinitionException`, `AccessDeniedException`, `ValidationException`
- Tüm exception'lar `AbstractRuntimeException`'dan türer
- `GlobalExceptionHelper` tüm hataları yakalar — yeni exception türleri eklemeden önce mevcut yapıya bak
- Hata mesajları her zaman DB'den çekilir (`ErrorMessageEntity`) — hardcode mesaj yazma

---

## Login Akışı (Önemli)

1. Kullanıcı adı ve şifre doğrulanır
2. `UserProductEntity` üzerinden ilgili ürün kaydı bulunur
3. `CompanyGroupProductEntity` üzerinden `apiUrl` ve `apiVersion` alınır
4. `passwordChangeRequired` ve EULA durumu kontrol edilir
5. JWT üretilir, response dönülür

Login endpoint'inde `CompanyGroupProductEntity` üzerindeki `enabled` alanı kontrol edilmeli.

---

## Proje Dosya Yapısı

```
src/main/java/com/gastroblue/
├── annotations/validation/     # @ValidPhoneNumber, @UniqueField vb.
├── config/                     # Spring konfigürasyonları
│   ├── AuthenticationConfig    # UserDetailsService, BCrypt, AuthManager
│   ├── SecurityConfig          # HTTP güvenlik kuralları
│   ├── JwtAuthenticationFilter # JWT doğrulama filtresi
│   ├── JwtConfig               # Sistem token'ları → ApplicationProduct
│   ├── MailConfig              # JavaMailSender, async executor
│   ├── CacheConfig             # Caffeine cache
│   ├── OpenApiConfig           # Swagger konfigürasyonu
│   ├── PersistenceConfig       # JPA auditing (AuditorAware)
│   └── tracing/RequestTracingFilter  # Trace ID, istek loglama
├── controller/                 # REST controller'lar (HTTP katmanı)
├── exception/                  # Custom exception'lar
│   └── helper/GlobalExceptionHelper
├── facade/                     # İş mantığı koordinasyonu
├── mapper/                     # Entity ↔ DTO dönüştürücüler
├── model/
│   ├── base/SessionUser        # Immutable JWT payload (record)
│   ├── entity/                 # JPA entity'leri
│   │   └── base/Auditable      # id, audit alanları, version
│   ├── enums/                  # ApplicationRole, ApplicationProduct vb.
│   ├── request/                # İstek DTO'ları
│   └── response/               # Yanıt DTO'ları
├── repository/                 # Spring Data JPA repository'leri
├── service/                    # Servis arayüzleri (IJwtService, IMailService)
│   └── impl/                   # Servis implementasyonları
└── util/                       # DateTimeUtil, PasswordGenerator, MailTemplateRenderer vb.
```

---

## Önemli Notlar

- `HELP.md` Maven/Spring Boot referans dosyasıdır — proje dökümantasyonu `README.md`'dedir
- Veritabanı şeması `ddl-auto: update` ile otomatik güncellenir — migration script'i yoktur
- Request tracing: Her istek için UUID trace ID üretilir, response header'da `X-Trace-Id` olarak dönülür
- Mail loglama: Her gönderim `OutgoingMailLogEntity`'ye kaydedilir
- Sistem token'ları (TT_TOKEN, FF_TOKEN, ADMIN_TOKEN): Servisler arası iletişim için kullanılır, JWT filtresi bunları tanır
