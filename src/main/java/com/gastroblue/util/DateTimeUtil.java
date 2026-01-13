package com.gastroblue.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeUtil {

  public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
  public static final DateTimeFormatter TIME_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");

  /**
   * Belirtilen formatter ile tarih nesnesini formatlayarak stringe çevirir.
   *
   * @param dateTime formatlanacak tarih/zaman
   * @param formatter kullanılacak biçimlendirici
   * @return formatlanmış string ya da hata durumunda null
   */
  public static String formatDateTime(LocalDateTime dateTime, DateTimeFormatter formatter) {
    try {
      return dateTime.format(formatter);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Eğer verilen LocalTime null ise defaultValue döndürür.
   *
   * @param time kontrol edilecek saat
   * @param defaultValue eğer null ise kullanılacak varsayılan saat
   * @return time ya da defaultValue
   */
  public static LocalTime getOrDefault(LocalTime time, LocalTime defaultValue) {
    return time != null ? time : defaultValue;
  }
}
