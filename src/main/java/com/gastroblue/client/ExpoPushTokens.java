package com.gastroblue.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

/**
 * Push tokens are stored without the Expo wrapper ({@code AMriMGIz_lWhFKXE92QH_1}) and wrapped as
 * {@code ExponentPushToken[AMriMGIz_lWhFKXE92QH_1]} only when sent to Expo. Values still stored in
 * the wrapped form are accepted as well.
 */
@UtilityClass
public class ExpoPushTokens {

  private static final String PREFIX = "ExponentPushToken[";
  private static final String SUFFIX = "]";

  // Expo issues both ExponentPushToken[...] and ExpoPushToken[...]
  private static final Pattern WRAPPED =
      Pattern.compile("^(?:ExponentPushToken|ExpoPushToken)\\[(.+)]$");

  /** Returns the bare token, or null when blank. */
  public static String normalize(String token) {
    if (!StringUtils.hasText(token)) {
      return null;
    }
    String trimmed = token.trim();
    Matcher matcher = WRAPPED.matcher(trimmed);
    return matcher.matches() ? matcher.group(1).trim() : trimmed;
  }

  /** Returns the token in the format Expo expects in the {@code to} field. */
  public static String toExpoFormat(String token) {
    String bare = normalize(token);
    return bare == null ? null : PREFIX + bare + SUFFIX;
  }
}
