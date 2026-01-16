package com.gastroblue.model.enums;

import com.gastroblue.model.base.GlobalConfigurableEnum;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Language implements GlobalConfigurableEnum {
  TR;

  public static Language valueOf(Locale language) {
    return Language.valueOf("TR");
  }

  public static Language defaultLang() {
    return TR;
  }
}
