package com.gastroblue.model.enums;

import com.gastroblue.model.base.DefaultConfigurableEnum;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Language implements DefaultConfigurableEnum {
  TR;

  public static Language valueOf(Locale language) {
    return Language.valueOf("TR");
  }

  public static Language defaultLang() {
    return TR;
  }
}
