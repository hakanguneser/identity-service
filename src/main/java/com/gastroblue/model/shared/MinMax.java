package com.gastroblue.model.shared;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MinMax {
  private Double min;
  private Double max;

  public Double getMin() {
    return min == null ? Double.MIN_VALUE : min;
  }

  public Double getMax() {
    return max == null ? Double.MAX_VALUE : max;
  }
}
