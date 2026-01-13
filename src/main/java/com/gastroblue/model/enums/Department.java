package com.gastroblue.model.enums;

import com.gastroblue.util.enums.IConfigurableEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Department implements IConfigurableEnum {
  ALL,
  KITCHEN,
  F_B,
  SALOON_RESTAURANT,
  BAR,
  STEWARD,
  HOUSEKEEPING,
  HUMAN_RESOURCES,
  WAREHOUSE,
  TECHNICAL,
  ANIMATION,
  SECURITY,
  LIFEGUARD,
  GUEST_RELATIONS,
  MINI_CLUB,
  ACCOUNTING,
  QUALITY_CONTROL,
  RECEPTION,
  GARDEN_LANDSCAPE
}
