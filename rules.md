# Development Rules

Bu dosya proje bağımsız geliştirme kurallarını içerir.
Her projede geçerlidir; "bu kuralları uygula" diyerek kullanılır.

---

## 1. Katman Mimarisi

### Akış

```
Controller  →  Facade  →  Service  →  Repository
```

### Aynı katman birbirini çağıramaz

- ❌ Controller → Controller
- ❌ Facade → Facade
- ❌ Service → Service
- ✅ Facade → birden fazla Service (bu Facade'ın tek amacıdır)
- ✅ Service → kendi Repository'si (sadece kendi aggregate'i)

### Her katmanın net sorumluluğu var

| Katman | Sorumluluk | Yasak |
|--------|-----------|-------|
| **Controller** | HTTP mapping, istek al / yanıt dön | İş mantığı, doğrudan Service çağrısı |
| **Facade** | Orchestration, yetki kontrolü, transaction yönetimi | Başka Facade'ı inject etmek |
| **Service** | Tek aggregate üzerinde CRUD operasyonları | Başka Service'i inject etmek |
| **Repository** | Veri erişimi, sorgular | İş mantığı |

---

## 2. Request / Response Model Kuralları

### Controller ve Facade asla ham `List<T>` dönmez

Her liste yanıtı bir wrapper sınıfa sarılır:

```java
// ✅ DOĞRU — wrapper sınıf
public class AccessibleUsersResponse {
    private List<UserDefinitionResponse> users;
}

public class DropdownResponse {
    private List<BaseLookupModel> items;
}

// ❌ YANLIŞ — ham liste
public List<UserDefinitionResponse> findAccessibleUsers() { ... }
```

**Neden:** İleride listeye metadata (pagination, total count, filtre bilgisi vb.) eklemek için
geriye dönük uyumlu değişiklik yapılabilir.

### Request ve Response ayrı sınıflardır

- Aynı sınıf hem request hem response olarak kullanılmaz.
- Request: `model/request/` → Java `record` + Bean Validation anotasyonları.
- Response: `model/response/` → Lombok `@Builder` veya `record`.
- Entity asla doğrudan controller'dan / facade'dan dışarı verilmez.

### Request DTO örneği

```java
public record UserSaveRequest(
    @NotBlank String username,
    @Email    String email,
    @Size(min = 10, max = 10) String phone) {}
```

### Response DTO örneği

```java
@Getter
@Builder
public class UserDefinitionResponse {
    private String userId;
    private String username;
    private String email;
}
```

---

## 3. Mapper Kuralları

### Mapper her zaman ayrı bir sınıftır

- `mapper/` paketi altında, her aggregate için kendi mapper'ı (`UserMapper`, `CompanyMapper` vb.).
- `@UtilityClass` — instance yaratılmaz, inject edilmez, her şey `static`.
- MapStruct **kullanılmaz** — manuel, explicit dönüşüm.

```java
@UtilityClass
public class UserMapper {

    public static UserDefinitionResponse toResponse(UserEntity entity, ...) { ... }

    public static UserEntity toEntity(UserSaveRequest request, ...) { ... }

    public static UserEntity updateEntity(UserEntity existing, UserUpdateRequest request) { ... }
}
```

### Mapper'a ne girer?

- Entity + ihtiyaç duyulan servisler (LookupService vb.) parametre olarak geçilir.
- Mapper içinde `@Autowired` / Spring bean erişimi yoktur.
- Dönüşüm sırasında iş mantığı yazılmaz — sadece alan eşlemesi.

---

## 4. Transaction Kuralları

- `@Transactional` → sadece **yazma** işlemleri (Facade veya Service seviyesinde).
- `@Transactional(readOnly = true)` → sadece **okuma** işlemleri.
- Controller'da `@Transactional` **kesinlikle yok**.
- Gereksiz yere tüm sınıfa `@Transactional` eklenmez; metot bazında uygulanır.

```java
// ✅ Facade'da
@Transactional
public UserDefinitionResponse saveUser(UserSaveRequest request) { ... }

@Transactional(readOnly = true)
public UserDefinitionResponse findUserById(String userId) { ... }
```

---

## 5. Hata Yönetimi

### Exception tipleri

| Exception | HTTP | Ne zaman |
|-----------|------|----------|
| `NotFoundException` | 404 | Kayıt bulunamadı |
| `AccessDeniedException` | 403 | Yetki / erişim hatası |
| `BusinessException` | 400 | İş kuralı ihlali |

### ErrorCode enum

- Her hata bir `ErrorCode` enum sabiti alır.
- Yeni hata eklemek = `ErrorCode.java`'ya yeni sabit eklemek. Başka bir şey değişmez.
- `ErrorCode` implements `ErrorCodeBase` (commons'tan gelir).

```java
throw new AccessDeniedException(ErrorCode.ACCESS_DENIED, "Açıklama");
throw new NotFoundException(ErrorCode.USER_NOT_FOUND, "userId=" + userId);
```

---

## 6. Loglama

- `@Slf4j` → **Facade** seviyesinde.
- Service ve Repository'de genellikle log gerekmez; exception mesajları yeterli.
- Log mesajlarında **PII yazılmaz**: şifre, e-posta içeriği, tam ad, TC kimlik no vb.
- Başarılı iş akışları için `log.info(...)`, beklenmedik durumlar için `log.error(...)`.

```java
// ✅
log.info("Login request: username={}, product={}", request.username(), request.product());

// ❌ PII
log.info("Login: password={}", request.password());
```

---

## 7. Entity Kuralları

### Taban sınıf `Auditable`

Tüm entity'ler projedeki `Auditable` taban sınıfından türer. Auditable şu alanları sağlar:
`id` (UUID, otomatik), `createdBy`, `createdDate`, `lastModifiedBy`, `lastModifiedDate`,
`version` (optimistic locking). Bu alanlar entity'de tekrar tanımlanmaz.

### Tablo / Kolon isimlendirmesi

- Tablo: `BÜYÜK_HARF_SNAKE_CASE` → `COMPANY_GROUPS`, `USERS`
- Kolon: `BÜYÜK_HARF_SNAKE_CASE` → `COMPANY_GROUP_ID`, `IS_ACTIVE`
- Boolean: `IS_` ön eki → `IS_ACTIVE`, `IS_ENABLED`
- Timestamp: `_AT` son eki → `PASSWORD_EXPIRES_AT`, `LOCKED_UNTIL`

### Entity field sıralama (yukarıdan aşağıya)

1. FK alanlar (`COMPANY_GROUP_ID`, `COMPANY_ID`)
2. Kimlik / anahtar alanlar (`USERNAME`, `EMAIL`)
3. Detay alanlar (`NAME`, `SURNAME`, `PHONE`, `GENDER`)
4. Durum / davranış alanları (`PASSWORD_VERSION`, `LOGIN_ATTEMPT_COUNT`)
5. Aktiflik en sona (`IS_ACTIVE`, `IS_ENABLED`)

### Yeni kolon eklerken `@Builder.Default` kullan

```java
@Column(name = "MY_COLUMN", nullable = false)
@Builder.Default
private int myColumn = 0;
```

Mevcut kayıtlar için migration gerekebilir:
```sql
UPDATE MY_TABLE SET MY_COLUMN = 0 WHERE MY_COLUMN IS NULL;
```

### `@ToString` ve PII koruması

Hassas alanları (`password`, token vb.) `@ToString.Exclude` ile işaretle.

---

## 8. Konfigürasyon ve Çevre Değişkenleri

### `application.yaml` — `${ENV_VAR:default}` formatı

Tüm dış bağımlılıklar (DB, mail, JWT, API anahtarları) environment variable üzerinden gelir.
Kod içine hardcode değer yazılmaz.

```yaml
datasource:
  url: jdbc:postgresql://${DATABASE_HOST:localhost}:${DATABASE_PORT:5432}/${DATABASE_NAME:mydb}
  username: ${DATABASE_USERNAME:app_user}
  password: ${DATABASE_PASSWORD:}
```

### `test.env` dosyası

- Test ortamı için tüm env var'ları içerir, git'e commit edilir (üretim secret'ı içermez).
- `docker-compose.yml` → `${VAR}` ile okur; `.env` veya `--env-file test.env` ile sağlanır.
- Production secret'ları bu dosyaya yazılmaz; CI/CD ortamından inject edilir.

### `docker-compose.yml`

- `environment:` bloğunda her değişken açıkça listelenir, `env_file:` kullanılmaz.
- `application.yaml`'daki her `${VAR}` için `docker-compose.yml`'de karşılığı olmalı.

---

## 9. Library Yapısı

### Parent POM

- Tüm servisler ortak bir `parent` POM'dan extend eder.
- Parent: Spring Boot BOM, Spotless, ortak plugin versiyonları buradadır.
- Servis `pom.xml`'inde plugin / bağımlılık versiyonu tekrar yazılmaz; parent'tan miras alınır.

### Commons Library

- Tekrar kullanılabilir cross-cutting concern'ler (JWT, mail, exception, swagger, tracing,
  lookup, persistence base) commons kütüphanesinde yaşar.
- Commons `AutoConfiguration.imports` ile Spring Boot auto-config olarak çalışır;
  servis `@Import` ya da manuel bean tanımı yazmaz.
- Commons'ta olmayan, tek servise özel bir şeyi commons'a taşıma — orada kirlilik yaratır.

### Yeni bağımlılık eklerken

- Önce parent POM'da versiyon tanımlanır (`<dependencyManagement>`).
- Servis `pom.xml`'ine sadece `groupId` + `artifactId` yazılır, versiyon yazılmaz.

---

## 10. Kod Formatı

- **Google Java Format** — Spotless Maven plugin ile zorlanır.
- Her değişiklikten sonra: `./mvnw spotless:apply`
- Build pipeline'da `spotless:check` çalışır; format hatalı kod build'i patlatır.
- IDE formatter'ı Google Java Format olarak ayarla (IntelliJ plugin veya `.editorconfig`).

---

## 11. Güvenlik — JWT Kuralları

- **Private key** sadece token üreten serviste (identity-service) bulunur.
- **Public key** doğrulama yapan tüm servislerde bulunur; commons üzerinden inject edilir.
- JWT claim'leri `IJwtService` sabitlerinden okunur/yazılır — magic string kullanılmaz.
- Token claim'lerine hassas veri (şifre hash, kredi kartı vb.) yazılmaz.
- Token revocation için stateless pattern: `passwordVersion` claim'i kullanılır
  (şifre değişince / logout'ta `passwordVersion++` → eski token geçersiz).

---

## 12. Genel Yasaklar (Neyi Asla Yapma)

| ❌ Yasak | ✅ Doğru |
|---------|---------|
| Controller'dan doğrudan Service çağırmak | Controller → Facade → Service |
| Facade → Facade çağrısı | Ortak kodu Service'e veya util'e taşı |
| Service → Service çağrısı | Orchestration'ı Facade'a taşı |
| Controller'da `@Transactional` | Facade veya Service'te kullan |
| Endpoint'ten ham `List<T>` dönmek | Wrapper response sınıfı yaz |
| Entity'yi response olarak doğrudan döndürmek | Mapper ile DTO'ya çevir |
| Hardcode connection string / secret | `${ENV_VAR:default}` kullan |
| Log'a şifre / PII yazmak | Sadece id / username gibi teknik tanımlayıcılar |
| Mapper'ı `@Service` ile inject etmek | `@UtilityClass` + static metotlar |
| MapStruct kullanmak | Manuel, explicit dönüşüm |
| Versiyonsuz bağımlılık eklemek | Parent `dependencyManagement`'tan miras al |
