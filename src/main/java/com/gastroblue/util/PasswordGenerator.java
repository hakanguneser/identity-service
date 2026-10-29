package com.gastroblue.util;

import java.security.SecureRandom;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Temporary passwords: 8 characters of upper-case letters and digits, in every environment, without
 * look-alike characters.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PasswordGenerator {

  private static final SecureRandom RANDOM = new SecureRandom();
  // Look-alike characters (O/0, I/1) are left out so the password is not misread.
  private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  private static final int LENGTH = 8;

  public static String generate() {
    StringBuilder password = new StringBuilder(LENGTH);
    for (int i = 0; i < LENGTH; i++) {
      password.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
    }
    return password.toString();
  }
}
