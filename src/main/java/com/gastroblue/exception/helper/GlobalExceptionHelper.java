package com.gastroblue.exception.helper;

import static com.gastroblue.model.enums.ErrorCode.*;

import com.gastroblue.config.tracing.TraceIdConstants;
import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.exception.base.AbstractRuntimeException;
import com.gastroblue.model.entity.ErrorMessageEntity;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.exception.ApplicationError;
import com.gastroblue.model.exception.ValidationError;
import com.gastroblue.service.IJwtService;
import com.gastroblue.service.impl.ErrorMessageService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralised exception handler.
 *
 * <p>Every error response includes a {@code traceId} field read from MDC. This allows API consumers
 * to paste the trace ID into logs / ELK and immediately find the full request context — without any
 * stack-trace leak to the client.
 */
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHelper {

  private final ErrorMessageService errorMessageService;

  // ---------------------------------------------------------------------------
  // Application-defined exceptions
  // ---------------------------------------------------------------------------

  @ExceptionHandler(IllegalDefinitionException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Object> handleIllegalDefinition(final IllegalDefinitionException ex) {
    log.warn(
        "IllegalDefinitionException | errorCode={} | message={}",
        ex.getErrorCode(),
        ex.getMessage());
    ErrorMessageEntity errorProp =
        errorMessageService.findOrCreatePropertyValue(
            ex.getErrorCode(), IJwtService.getSessionLanguage());

    return badRequest(
        ApplicationError.builder()
            .errorMessage(errorProp.getMessage())
            .errorCode(ex.getErrorCode())
            .referenceId(errorProp.getId())
            .httpStatus(HttpStatus.BAD_REQUEST)
            .timeStamp(LocalDateTime.now())
            .debugContext(ex.getMessage())
            .traceId(currentTraceId())
            .build());
  }

  @ExceptionHandler(AbstractRuntimeException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Object> handleAbstractRuntime(final AbstractRuntimeException ex) {
    log.warn(
        "ApplicationException | errorCode={} | message={}", ex.getErrorCode(), ex.getMessage());
    ErrorMessageEntity propertyEntity =
        errorMessageService.findOrCreatePropertyValue(
            ex.getErrorCode(), IJwtService.getSessionLanguage());

    return badRequest(
        ApplicationError.builder()
            .errorMessage(propertyEntity.getMessage())
            .errorCode(ex.getErrorCode())
            .referenceId(propertyEntity.getId())
            .httpStatus(HttpStatus.BAD_REQUEST)
            .timeStamp(LocalDateTime.now())
            .debugContext(ex.getMessage())
            .traceId(currentTraceId())
            .build());
  }

  // ---------------------------------------------------------------------------
  // Spring Security exceptions
  // ---------------------------------------------------------------------------

  @ExceptionHandler(BadCredentialsException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<Object> handleBadCredentials(final BadCredentialsException ex) {
    log.warn("BadCredentialsException — authentication failed");
    ErrorMessageEntity propertyEntity =
        errorMessageService.findOrCreatePropertyValue(
            UNAUTHORIZED_USER, IJwtService.getSessionLanguage());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(
            ApplicationError.builder()
                .errorMessage(propertyEntity.getMessage())
                .errorCode(ErrorCode.UNAUTHORIZED_USER)
                .referenceId(propertyEntity.getId())
                .httpStatus(HttpStatus.UNAUTHORIZED)
                .timeStamp(LocalDateTime.now())
                .traceId(currentTraceId())
                .build());
  }

  // ---------------------------------------------------------------------------
  // Validation exceptions
  // ---------------------------------------------------------------------------

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Object> handleValidation(final MethodArgumentNotValidException ex) {
    List<ValidationError> validationErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                error ->
                    ValidationError.builder()
                        .field(error.getField())
                        .rejectedValue(error.getRejectedValue())
                        .message(error.getDefaultMessage())
                        .build())
            .toList();

    log.warn(
        "Validation failed | fields={}",
        validationErrors.stream().map(ValidationError::getField).toList());

    ErrorMessageEntity propertyEntity =
        errorMessageService.findOrCreatePropertyValue(
            INVALID_REQUEST_BODY, IJwtService.getSessionLanguage());

    return badRequest(
        ApplicationError.builder()
            .errorMessage(propertyEntity.getMessage())
            .errorCode(INVALID_REQUEST_BODY)
            .referenceId(propertyEntity.getId())
            .httpStatus(HttpStatus.BAD_REQUEST)
            .timeStamp(LocalDateTime.now())
            .errorDetails(validationErrors)
            .traceId(currentTraceId())
            .build());
  }

  // ---------------------------------------------------------------------------
  // Database exceptions
  // ---------------------------------------------------------------------------

  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Object> handleDataIntegrityViolation(
      final DataIntegrityViolationException ex) {
    log.error("DataIntegrityViolationException", ex);
    ErrorMessageEntity propertyEntity =
        errorMessageService.findOrCreatePropertyValue(
            DATA_INTEGRITY_VIOLATION, IJwtService.getSessionLanguage());

    return badRequest(
        ApplicationError.builder()
            .errorMessage(propertyEntity.getMessage())
            .errorCode(DATA_INTEGRITY_VIOLATION)
            .referenceId(propertyEntity.getId())
            .httpStatus(HttpStatus.BAD_REQUEST)
            .timeStamp(LocalDateTime.now())
            .debugContext(ex.getMessage())
            .traceId(currentTraceId())
            .build());
  }

  // ---------------------------------------------------------------------------
  // JSON / parsing exceptions
  // ---------------------------------------------------------------------------

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Object> handleNotReadable(final HttpMessageNotReadableException ex) {
    log.warn(
        "HttpMessageNotReadableException (invalid enum or unreadable JSON): {}", ex.getMessage());
    ErrorMessageEntity propertyEntity =
        errorMessageService.findOrCreatePropertyValue(
            INVALID_ENUM_VALUE, IJwtService.getSessionLanguage());

    return badRequest(
        ApplicationError.builder()
            .errorMessage(propertyEntity.getMessage())
            .errorCode(INVALID_ENUM_VALUE)
            .referenceId(propertyEntity.getId())
            .httpStatus(HttpStatus.BAD_REQUEST)
            .timeStamp(LocalDateTime.now())
            .debugContext(ex.getMessage())
            .traceId(currentTraceId())
            .build());
  }

  // ---------------------------------------------------------------------------
  // Catch-all — prevents stack trace leakage to clients
  // ---------------------------------------------------------------------------

  /**
   * Safety net for any exception not handled above.
   *
   * <p>Logs at ERROR level with the full stack trace (server-side only), but returns a sanitised
   * response to the client. The {@code traceId} in the response lets the client report the exact
   * request to support without exposing internal details.
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<Object> handleUnexpected(final Exception ex) {
    log.error(
        "Unexpected error | type={} | message={}",
        ex.getClass().getSimpleName(),
        ex.getMessage(),
        ex);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            ApplicationError.builder()
                .errorMessage("An unexpected error occurred. Please contact support.")
                .errorCode(ErrorCode.UNEXPECTED_ERROR)
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .timeStamp(LocalDateTime.now())
                .traceId(currentTraceId())
                .build());
  }

  // ---------------------------------------------------------------------------
  // Private helpers
  // ---------------------------------------------------------------------------

  /**
   * Reads the trace ID from MDC set by {@link com.gastroblue.config.tracing.RequestTracingFilter}.
   * Returns {@code null} if the filter did not run (e.g. unit tests without filter context).
   */
  private String currentTraceId() {
    return MDC.get(TraceIdConstants.MDC_TRACE_ID_KEY);
  }

  private ResponseEntity<Object> badRequest(ApplicationError error) {
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }
}
