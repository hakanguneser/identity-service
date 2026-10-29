package com.gastroblue.util;

import java.security.SecureRandom;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Temporary passwords: 8 characters of upper-case letters and digits, in every environment. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PasswordGenerator {

  private static final SecureRandom RANDOM = new SecureRandom();
  private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final int LENGTH = 8;

  public static String generate() {
    StringBuilder password = new StringBuilder(LENGTH);
    for (int i = 0; i < LENGTH; i++) {
      password.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
    }
    return password.toString();
  }
}
