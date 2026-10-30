-- =====================================================================
-- identity-service :: error_messages (TR)
-- Run after the existing rows are restored. Safe to run more than once:
-- placeholders are updated, missing codes are added, existing texts are kept.
-- =====================================================================
BEGIN;

-- 1. Placeholders ("KOD [TR]") and texts that no longer match the behaviour
UPDATE error_messages SET message = 'Oturum bilgisi bulunamadı. Lütfen tekrar giriş yapın.',
       last_modified_by = 'SYSTEM', last_modified_date = now()
 WHERE error_code = 'USER_NOT_INITIALIZED' AND "language" = 'TR';

UPDATE error_messages SET message = 'E-posta adresi şirket grubu için tanımlı alan adlarından biriyle bitmelidir.',
       last_modified_by = 'SYSTEM', last_modified_date = now()
 WHERE error_code = 'INVALID_MAIL_DOMAINS' AND "language" = 'TR';

-- The lock is temporary (15 minutes); the old text sent the user to an administrator.
UPDATE error_messages SET message = 'Hesabınız geçici olarak kilitlendi. Lütfen 15 dakika sonra tekrar deneyin.',
       last_modified_by = 'SYSTEM', last_modified_date = now()
 WHERE error_code = 'ACCOUNT_LOCKED' AND "language" = 'TR';

-- 2. Missing codes (existing ones are left as they are)
INSERT INTO error_messages (id, created_by, created_date, "version", error_code, "language", message)
SELECT gen_random_uuid()::text, 'SYSTEM', now(), 0, v.code, 'TR', v.message
FROM (VALUES
    -- identity ErrorCode
    ('USER_NOT_FOUND', 'Kullanıcı bulunamadı.'),
    ('INVALID_JWT_TOKEN', 'Oturumunuz geçersiz. Lütfen tekrar giriş yapın.'),
    ('INACTIVE_USER', 'Kullanıcı hesabı aktif değil.'),
    ('COMPANY_GROUP_PRODUCT_NOT_FOUND', 'Şirket grubuna tanımlı ürün bulunamadı.'),
    ('COMPANY_GROUP_PRODUCT_ALREADY_EXISTS', 'Bu ürün şirket grubuna zaten tanımlı.'),
    ('COMPANY_PRODUCT_ALREADY_EXISTS', 'Bu ürün şirkete zaten tanımlı.'),
    ('USER_NOT_ALLOWED_FOR_REGISTRATION', 'Kullanıcı oluşturma yetkiniz bulunmuyor.'),
    ('ACCESS_DENIED', 'Bu işlem için yetkiniz bulunmuyor.'),
    ('PRODUCT_NOT_SELECTED', 'Ürün seçilmedi ya da ürün kullanıma açık değil.'),
    ('THERMOMETER_TRACKER_APP_NOT_ENABLED_FOR_COMPANY_GROUP', 'Tracker uygulaması şirket grubunuz için aktif değil.'),
    ('FORMFLOW_APP_NOT_ENABLED_FOR_COMPANY_GROUP', 'Form Flow uygulaması şirket grubunuz için aktif değil.'),
    ('COMPANY_PRODUCT_LICENSE_EXPIRED', 'Şirketinizin ürün lisansının süresi dolmuştur.'),
    ('LICENSE_USER_LIMIT_EXCEEDED', 'Lisanstaki kullanıcı sayısı sınırı aşıldı.'),
    ('COMPANY_PRODUCT_NOT_ACTIVE', 'Ürün şirketiniz için aktif değil.'),
    ('PRODUCT_NOT_ALLOWED_FOR_REGISTRATION', 'Kullanıcı kaydı için ürün seçilmelidir.'),
    ('USER_NOT_ELIGIBLE_EMAIL', 'Kullanıcı ekleyebilmek için hesabınızda e-posta adresi tanımlı olmalıdır.'),
    ('USER_NOT_ELIGIBLE_SUPERVISOR_OR_ABOVE', 'Kullanıcı eklemek için en az Supervisor yetkisi gereklidir.'),
    ('USER_PRODUCT_NOT_FOUND', 'Kullanıcının bu ürün için kaydı bulunamadı.'),
    -- gastroblue-commons CommonErrorCode
    ('BAD_CREDENTIALS', 'Kimlik bilgileri doğrulanamadı.'),
    ('FORBIDDEN', 'Bu işlem için yetkiniz bulunmuyor.'),
    ('EXPIRED_JWT_TOKEN', 'Oturumunuzun süresi doldu. Lütfen tekrar giriş yapın.'),
    ('PARENT_KEY_NOT_FOUND', 'Üst tanım bilgisi eksik.'),
    -- request validation
    ('{validation.applicationRole.check.null}', 'Rol seçilmelidir.'),
    ('{validation.channel.check.null}', 'Kanal bilgisi boş olamaz.'),
    ('{validation.companyCode.check.blank}', 'Şirket kodu boş olamaz.'),
    ('{validation.companyCode.pattern}', 'Şirket kodu yalnızca büyük harf, rakam ve alt çizgi içerebilir.'),
    ('{validation.companyCode.size}', 'Şirket kodu 3 ile 20 karakter arasında olmalıdır.'),
    ('{validation.companyGroupCode.check.blank}', 'Şirket grubu kodu boş olamaz.'),
    ('{validation.companyGroupCode.pattern}', 'Şirket grubu kodu yalnızca büyük harf, rakam ve alt çizgi içerebilir.'),
    ('{validation.companyGroupName.check.blank}', 'Şirket grubu adı boş olamaz.'),
    ('{validation.companyName.check.null}', 'Şirket adı boş olamaz.'),
    ('{validation.content.check.blank}', 'İçerik boş olamaz.'),
    ('{validation.department.check.null}', 'Departman seçilmelidir.'),
    ('{validation.domain.check.blank}', 'Alan adı boş olamaz.'),
    ('{validation.domain.lowercase}', 'Alan adı küçük harflerle ve boşluksuz yazılmalıdır.'),
    ('{validation.email.check.blank}', 'E-posta adresi boş olamaz.'),
    ('{validation.email.lowercase}', 'E-posta adresi küçük harflerle ve boşluksuz yazılmalıdır.'),
    ('{validation.email}', 'Geçerli bir e-posta adresi giriniz.'),
    ('{validation.enabled.check.null}', 'Aktiflik bilgisi boş olamaz.'),
    ('{validation.eulaVersion.check.blank}', 'Sözleşme sürümü boş olamaz.'),
    ('{validation.language.check.null}', 'Dil seçilmelidir.'),
    ('{validation.name.check.null}', 'Ad boş olamaz.'),
    ('{validation.newPassword.check.null}', 'Yeni şifre boş olamaz.'),
    ('{validation.oldPassword.check.null}', 'Mevcut şifre boş olamaz.'),
    ('{validation.password.check.null}', 'Şifre boş olamaz.'),
    ('{validation.phone.number}', 'Telefon numarası 5 ile başlayan 10 haneli olmalıdır.'),
    ('{validation.product.check.null}', 'Ürün seçilmelidir.'),
    ('{validation.push.body.notBlank}', 'Bildirim metni boş olamaz.'),
    ('{validation.push.title.notBlank}', 'Bildirim başlığı boş olamaz.'),
    ('{validation.push.userIds.notEmpty}', 'En az bir kullanıcı seçilmelidir.'),
    ('{validation.startDate.check.null}', 'Başlangıç tarihi boş olamaz.'),
    ('{validation.surname.check.null}', 'Soyad boş olamaz.'),
    ('{validation.tracker.userIds.notEmpty}', 'En az bir kullanıcı seçilmelidir.'),
    ('{validation.username.check.null}', 'Kullanıcı adı boş olamaz.'),
    ('{validation.username.invalid.format}', 'Kullanıcı adı 5 ile 100 karakter arasında olmalı; harf, rakam, nokta, alt çizgi ve tire içerebilir, harf veya rakamla başlayıp bitmelidir.')
) AS v(code, message)
ON CONFLICT (error_code, "language") DO NOTHING;

COMMIT;
