package com.gastroblue.model.shared;

import java.time.LocalDate;
import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryDate {
  private LocalDate startDate;
  private LocalDate endDate;
}
