package com.gastroblue.model.exception;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidationError {
  private String field;
  private Object rejectedValue;
  private String message;
}
