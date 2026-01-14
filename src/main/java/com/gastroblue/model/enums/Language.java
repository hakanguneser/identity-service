package com.gastroblue.model.enums;

import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Language {
  TR;

  public static Language valueOf(Locale language) {
    return Language.valueOf("TR");
  }

  public static Language defaultLang() {
    return TR;
  }
}
