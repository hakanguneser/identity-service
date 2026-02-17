package com.gastroblue.model.shared;

import com.gastroblue.model.base.ConfigurableEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolvedEnum<T extends ConfigurableEnum> {
  private T key;
  private String display;
  private Integer displayOrder;
}
