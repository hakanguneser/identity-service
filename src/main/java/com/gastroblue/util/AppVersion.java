package com.gastroblue.util;

import com.gastroblue.commons.helper.exception.type.BusinessException;
import com.gastroblue.model.enums.ErrorCode;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * A dotted app version such as {@code 1.4.2}, compared number by number: {@code 1.10.0} is newer
 * than {@code 1.9.0}, which a string comparison gets wrong. Missing parts count as zero ({@code
 * 1.4} equals {@code 1.4.0}); a build suffix ({@code 1.4.2+15}) is ignored.
 */
public record AppVersion(int[] parts) implements Comparable<AppVersion> {

  private static final int PART_COUNT = 4;
  private static final Pattern FORMAT = Pattern.compile("^\\d{1,9}(\\.\\d{1,9}){0,3}$");

  public static AppVersion parse(String value) {
    String version = value == null ? "" : value.strip();
    int buildSuffix = version.indexOf('+');
    if (buildSuffix >= 0) {
      version = version.substring(0, buildSuffix);
    }
    if (!FORMAT.matcher(version).matches()) {
      throw new BusinessException(ErrorCode.INVALID_APP_VERSION, "Invalid app version: " + value);
    }
    int[] parts = new int[PART_COUNT];
    String[] tokens = version.split("\\.");
    for (int i = 0; i < tokens.length; i++) {
      parts[i] = Integer.parseInt(tokens[i]);
    }
    return new AppVersion(parts);
  }

  public boolean isOlderThan(AppVersion other) {
    return compareTo(other) < 0;
  }

  @Override
  public int compareTo(AppVersion other) {
    return Arrays.compare(parts, other.parts);
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof AppVersion other && Arrays.equals(parts, other.parts);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(parts);
  }

  @Override
  public String toString() {
    return String.join(".", Arrays.stream(parts).mapToObj(String::valueOf).toList());
  }
}
