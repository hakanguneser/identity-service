package com.gastroblue.util;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DelimitedStringUtil {

  public static final char DEFAULT = ';';

  private static final Pattern DEFAULT_SPLIT = Pattern.compile("\\s*;\\s*");

  public static List<String> split(String raw) {
    if (raw == null || raw.isBlank()) return List.of();
    return Arrays.asList(DEFAULT_SPLIT.split(raw));
  }

  public static List<String> splitClean(String raw) {
    if (raw == null || raw.isBlank()) return List.of();
    return Arrays.stream(DEFAULT_SPLIT.split(raw))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(toUnmodifiableList());
  }

  public static String join(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return "";
    }

    return params.entrySet().stream()
        .map(e -> e.getKey() + ":" + e.getValue())
        .collect(Collectors.joining(System.lineSeparator()));
  }

  public static String join(Collection<?> items) {
    return join(items, DEFAULT);
  }

  public static String join(Collection<?> items, char delimiter) {
    if (items == null || items.isEmpty()) return "";
    return items.stream()
        .filter(Objects::nonNull)
        .map(Object::toString)
        .sorted(String::compareToIgnoreCase)
        .collect(Collectors.joining(String.valueOf(delimiter)));
  }

  private static <T> Collector<T, ?, List<T>> toUnmodifiableList() {
    return Collectors.collectingAndThen(Collectors.toList(), List::copyOf);
  }

  public static <E extends Enum<E>> List<E> splitToEnumList(String raw, Class<E> enumType) {
    if (raw == null || raw.isBlank()) return List.of();
    return splitClean(raw).stream()
        .map(s -> Enum.valueOf(enumType, s))
        .collect(toUnmodifiableList());
  }
}
