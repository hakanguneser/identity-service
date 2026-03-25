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
