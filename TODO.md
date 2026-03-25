# Identity Service — TODO

Bu dosya; aktif geliştirme, eksik bırakılan parçalar ve planlanan özellikleri takip eder.

---

## Kritik / Acil

- [ ] **`CompanyGroupProductEntity` alanlarının login akışına entegrasyonu**
  - Entity'deki `apiUrl` ve `apiVersion` login response'una eklenmeli
  - `enabled = false` olan product için login reddedilmeli 

- [ ] **`CompanyProductEntity` alanlarının login akışına entegrasyonu**
  - `enabled = false` olan product için login reddedilmeli
  - `licenseExpiresAt` ve `agreedUserCount` CompanyGroupProductEntity'dan cikarilip buraya tasinmali
  - `licenseExpiresAt` ve `agreedUserCount` login sırasında kontrol edilmeli 

- [ ] **Lisans limit kontrolü (gerçek zamanlı)**
  - Login sırasında `agreedUserCount` ve aktif user sayısı karşılaştırılmalı
  - Limit aşıldığında hata dönülmeli (`ErrorCode` eklenmeli)
  - `licenseExpiresAt` süresi geçmişse login engellenmeli


---

## Aktif Geliştirme

- [ ] **Tracker ürünü login entegrasyonu**
  - Tracker kullanıcısı login olduğunda `CompanyGroupProductEntity.apiUrl` ve `apiVersion` dönülmeli
  - `UserProductEntity.product = TRACKER` olan kullanıcılar için akış test edilmeli

- [ ] **`PermissionsController` implementasyonu** (`/api/v1/permissions`)
  - Şu an sınıf boş
  - İzin/yetki yönetimi endpoint'leri tanımlanacak
  - Hangi endpoint'lerin ekleneceği netleştirilmeli

---

## İyileştirme

- [ ] **`EnumValueConfigurationEntity` — Segment label ve hiyerarşik enum desteği**

  **1. Segment label'larının companygroup bazında konfigüre edilebilmesi (`SEGMENT_META` convention):**
  - `CompanyEntity.segment1-5` alanları Java enum'dan (`CompanySegment1Values` vb.) `String`'e dönüştürülmeli
  - `CompanySegment1Values`, `CompanySegment2Values`, ..., `CompanySegment5Values` Java enum'ları silinmeli
  - Segment *etiketleri* `EnumValueConfigurationEntity`'de `enumType = "SEGMENT_META"` convention'ı ile saklanacak:
    - `enumType = "SEGMENT_META"`, `enumKey = "SEGMENT_1"`, `label = "Otel Tipi"` → Hilton için
    - `enumType = "SEGMENT_META"`, `enumKey = "SEGMENT_1"`, `label = "Şube Kodu"` → başka bir müşteri için
  - Segment *değerleri* aynı tabloda `enumType = "SEGMENT_1"`, `enumKey = "DELUXE"` vb. ile saklanacak
  - Company kaydedilirken seçilen segment değerinin, o companygroup'un `EnumValueConfigurationEntity`'sinde var olduğu doğrulanmalı
  - `enumKey` format validasyonu: `^[A-Z][A-Z0-9]*(_[A-Z][A-Z0-9]*)*$` (mevcut `@ValidItemCode` kullanılabilir)

  **2. `EnumValueConfigurationEntity`'e `parentKey` alanı eklenmesi (N-seviye hiyerarşik cascade desteği):**
  - `parentKey` nullable `String` (50 karakter) alanı eklenmeli
  - Root seviye kayıtlar (örn. ülkeler) `parentKey = null`
  - Alt seviye kayıtlar, üst seviyenin `enumKey`'ini referans alır:
    - `enumType = "CITY"`, `enumKey = "ISTANBUL"`, `parentKey = "TURKEY"`
    - `enumType = "DISTRICT"`, `enumKey = "KADIKOY"`, `parentKey = "ISTANBUL"`
    - `enumType = "NEIGHBOURHOOD"`, `enumKey = "MODA"`, `parentKey = "KADIKOY"`
  - Cascade dropdown sorgusu: `WHERE enumType = ? AND parentKey = ? AND companyGroupId = ? AND language = ? AND isActive = true`
  - `EnumConfigurationService`'e `parentKey` bazlı sorgular eklenmeli
  - `EnumValueConfigurationEntity` unique constraint'i `parentKey` dahil edilecek şekilde güncellenmeli
  - `parentKey` de `enumKey` ile aynı format kuralına tabi: `^[A-Z][A-Z0-9]*(_[A-Z][A-Z0-9]*)*$`

- [ ] **Kullanıcı hiyerarşisi yetki kontrollerinin sıkılaştırılması**
  - Her kullanıcı yalnızca kendi altındaki role user tanımlayabilmeli (şu anki kontrol yeterli mi?)
  - Zone Manager akışı tam test edilmeli

- [ ] **User product ataması akışının doğrulanması**
  - Yalnızca ADMIN, admin-panel üzerinden herhangi bir platforma user tanımlayabilmeli
  - Diğer kullanıcılar oluştukları platform için otomatik product ile ilişkilendirilmeli
  - Bu kuralın `UserDefinitionFacade`'da doğru uygulandığını doğrula

- [ ] **Company bazında department/role görünümü**
  - Dropdown listelerinde her company kendi product bazındaki departmanlarını görmeli
  - `EnumValueConfigurationEntity` companygroup bazında çalışıyor — yeterli mi değerlendirilmeli

- [ ] **Sistem token'larına (TT_TOKEN, FF_TOKEN, ADMIN_TOKEN) kullanım dokümantasyonu**
  - Bu token'ların hangi endpoint'leri çağırabileceği netleştirilmeli
  - `SecurityConfig`'deki kural setleri gözden geçirilmeli

- [ ] **`OutgoingMailLogEntity` temizlik stratejisi**
  - Mail logları büyüyebilir — eski kayıtlar için temizlik/arşivleme politikası belirlenmeli

---

## Test ve Kalite

- [ ] **Unit test coverage artırılması**
  - Facade katmanı için unit testler yazılmalı
  - `JwtService` token üretim ve doğrulama testleri
  - `GlobalExceptionHelper` hata senaryoları testleri

- [ ] **Integration test**
  - Login akışı end-to-end testi (şifre kontrolü, EULA, token üretimi)
  - Company group → company → user oluşturma akışı

---

## Planlanan Yeni Özellikler

- [ ] **FormFlow ürün entegrasyonu**
  - Login akışında FormFlow `apiUrl`/`apiVersion` desteği
  - `FF_TOKEN` sistem token'ı ile servis arası iletişim

- [ ] **CheckApp ürün entegrasyonu**
  - Login akışında CheckApp desteği
  - `ADMIN_TOKEN` ile servis arası iletişim

- [ ] **Admin-Panel geliştirmesi**
  - Tüm platformlara user tanımlayabilme (`ADMIN_PANEL` ürünü)
  - `UserDefinitionController` üzerinden admin özel endpoint'ler
  - Admin-panel'e özel UI gereksinimlerinin belirlenmesi

- [ ] **Password expiry notification**
  - `passwordExpiresAt` yaklaşan kullanıcılara uyarı maili
  - Login response'unda `passwordExpiresAt` bilgisi dönülmesi değerlendirilebilir

---

## Teknik Borç

- [ ] **`show-sql: true`** production'da kapatılmalı (şu an application.yaml'da açık)
- [ ] **Veritabanı migration aracı**
  - `ddl-auto: update` production için risklidir
  - Flyway veya Liquibase entegrasyonu değerlendirilebilir
- [ ] **Cache invalidation stratejisi**
  - Caffeine cache TTL ve eviction policy gözden geçirilmeli
  - Hangi verilerin önbelleklendiği dokümante edilmeli
