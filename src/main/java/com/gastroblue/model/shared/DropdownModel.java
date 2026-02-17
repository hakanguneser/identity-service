package com.gastroblue.model.shared;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DropdownModel {
  private String key;
  private String display;
  private Integer displayOrder;
}
