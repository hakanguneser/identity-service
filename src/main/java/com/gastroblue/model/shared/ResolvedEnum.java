package com.gastroblue.model.shared;

import com.gastroblue.model.base.DefaultConfigurableEnum;
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
public class ResolvedEnum<T extends DefaultConfigurableEnum> {
  private T key;
  private String display;
}
