package com.gastroblue.model.shared;

import lombok.*;

import java.time.LocalDate;

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
