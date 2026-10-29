package com.gastroblue.model.enums;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Push delivery errors returned per user. {@code code} values of Expo errors match the Expo Push
 * API ({@code details.error}); the others are produced by identity-service.
 */
@Getter
@RequiredArgsConstructor
public enum PushNotificationError {
  NO_TOKEN_FOUND(
      "NoTokenFound", "Kullanıcı henüz uygulamaya giriş yapmadı; cihaz token'ı bulunamadı."),
  DEVICE_NOT_REGISTERED(
      "DeviceNotRegistered",
      "Cihaz sistemde bulunamadı; kullanıcı uygulamadan çıkış yapmış ya da uygulamayı kaldırmış"
          + " olabilir."),
  MESSAGE_TOO_BIG("MessageTooBig", "Bildirim içeriği izin verilen boyutu aşıyor."),
  MESSAGE_RATE_EXCEEDED(
      "MessageRateExceeded", "Bu cihaza çok sık bildirim gönderildi; daha sonra tekrar deneyin."),
  MISMATCH_SENDER_ID(
      "MismatchSenderId", "Cihaz token'ı bu uygulamanın bildirim ayarlarıyla eşleşmiyor."),
  INVALID_CREDENTIALS("InvalidCredentials", "Expo bildirim kimlik bilgileri geçersiz ya da eksik."),
  EXPO_REQUEST_FAILED("ExpoRequestFailed", "Expo bildirim servisine ulaşılamadı."),
  UNKNOWN("Unknown", "Bildirim gönderilemedi; Expo bilinmeyen bir hata döndürdü.");

  private final String code;
  private final String description;

  public static PushNotificationError fromExpoCode(String expoCode) {
    return Arrays.stream(values()).filter(e -> e.code.equals(expoCode)).findFirst().orElse(UNKNOWN);
  }
}
