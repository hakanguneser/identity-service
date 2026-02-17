package com.gastroblue.model.enums;

import com.gastroblue.model.base.ConfigurableEnum;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Language implements ConfigurableEnum {
  TR;

  public static Language valueOf(Locale language) {
    return Language.valueOf("TR");
  }

  public static Language defaultLang() {
    return TR;
  }
}
