package com.gastroblue.model.enums;

import com.gastroblue.commons.helper.lookup.model.base.BaseLookup;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Lookups implements BaseLookup {
  ZONE(true, false, false, "ZONE"),
  CITY(true, false, true, "CITY"),
  COUNTRY(true, false, false, "COUNTRY"),
  SEGMENT_1(true, false, false, "SEGMENT_1"),
  SEGMENT_2(true, false, false, "SEGMENT_2"),
  SEGMENT_3(true, false, false, "SEGMENT_3"),
  SEGMENT_4(true, false, false, "SEGMENT_4"),
  SEGMENT_5(true, false, false, "SEGMENT_5"),
  DEPARTMENT(true, true, false, "DEPARTMENT"),
  LANGUAGE(false, true, false, "LANGUAGE"),
  GENDER(false, false, false, "GENDER"),
  APPLICATION_ROLE(false, true, false, "APPLICATION_ROLE");

  private final boolean companyGroupScope;
  private final boolean productScope;
  private final boolean parentNeeded;
  private final String category;
}
