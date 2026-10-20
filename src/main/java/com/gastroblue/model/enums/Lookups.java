package com.gastroblue.model.enums;

import com.gastroblue.commons.helper.lookup.model.base.BaseLookup;
import lombok.Getter;

@Getter
public enum Lookups implements BaseLookup {
  ZONE(true, false, false),
  CITY(true, false, true),
  COUNTRY(true, false, false),
  SEGMENT_1(true, false, false),
  SEGMENT_2(true, false, false),
  SEGMENT_3(true, false, false),
  SEGMENT_4(true, false, false),
  SEGMENT_5(true, false, false),
  DEPARTMENT(true, true, false),
  LANGUAGE(false, true, false),
  GENDER(false, false, false),
  APPLICATION_ROLE(false, true, false);

  private final boolean companyGroupScope;
  private final boolean productScope;
  private final boolean parentNeeded;

  Lookups(boolean companyGroupScope, boolean productScope, boolean parentNeeded) {
    this.companyGroupScope = companyGroupScope;
    this.productScope = productScope;
    this.parentNeeded = parentNeeded;
  }

  @Override
  public String getCategory() {
    return name();
  }
}
