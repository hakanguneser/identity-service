package com.gastroblue.model.enums;

import com.gastroblue.util.enums.IConfigurableEnum;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Language implements IConfigurableEnum {
  TR;

  public static Language valueOf(Locale language) {
    return Language.valueOf("TR");
  }
}
