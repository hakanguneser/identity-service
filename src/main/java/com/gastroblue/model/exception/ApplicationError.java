package com.gastroblue.model.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gastroblue.model.enums.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationError {
  private String errorMessage;
  private String debugContext;
  private ErrorCode errorCode;
  private String referenceId;
  private HttpStatus httpStatus;
  private LocalDateTime timeStamp;

  /** Correlation handle that maps this error response to the server log entry. */
  private String traceId;

  private List<ValidationError> errorDetails;
}
