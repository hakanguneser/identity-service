package com.gastroblue.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PasswordGenerator {

  private static final java.security.SecureRandom RANDOM = new java.security.SecureRandom();

  public static String generate() {
    String profile = System.getProperty("spring.profiles.active", "");
    if (!profile.contains("prod")) {
      return "12345";
    }
    return generate5DigitCode();
  }

  public static String generate5DigitCode() {
    return String.format("%05d", RANDOM.nextInt(100000));
  }
}
