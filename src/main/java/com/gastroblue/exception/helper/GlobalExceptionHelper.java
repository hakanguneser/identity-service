package com.gastroblue.exception.helper;

import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.exception.base.AbstractRuntimeException;
import com.gastroblue.model.entity.ApplicationPropertyEntity;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.enums.Language;
import com.gastroblue.model.exception.ApplicationError;
import com.gastroblue.model.exception.ValidationError;
import com.gastroblue.service.IJwtService;
import com.gastroblue.service.impl.ApplicationPropertyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

import static com.gastroblue.model.enums.ErrorCode.*;


@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHelper {
  private final ApplicationPropertyService errorMessageService;

  @ExceptionHandler({IllegalDefinitionException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Object> handleGlobalException(final IllegalDefinitionException exception) {
    Language sessionLanguage = sessionLanguage();
    ApplicationPropertyEntity definitionType =
        errorMessageService.findOrCreatePropertyValue(
            exception.getDefinitionType().getMessageKey(), sessionLanguage);
    ApplicationPropertyEntity errorProp =
        errorMessageService.findOrCreatePropertyValue(
            exception.getErrorCode().getMessageKey(), sessionLanguage);

    ApplicationError applicationError =
        ApplicationError.builder()
            .errorMessage(
                String.format(
                    "%s : %s", errorProp.getPropertyValue(), definitionType.getPropertyValue()))
            .errorCode(exception.getErrorCode())
            .referenceId(errorProp.getId())
            .httpStatus(HttpStatus.BAD_REQUEST)
            .timeStamp(LocalDateTime.now())
            .build();

    return new ResponseEntity<>(applicationError, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({AbstractRuntimeException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Object> handleGlobalException(final AbstractRuntimeException exception) {

    ApplicationPropertyEntity propertyEntity =
        errorMessageService.findOrCreatePropertyValue(
            exception.getErrorCode().getMessageKey(), sessionLanguage());

    ApplicationError applicationError =
        ApplicationError.builder()
            .errorMessage(propertyEntity.getPropertyValue())
            .errorCode(exception.getErrorCode())
            .referenceId(propertyEntity.getId())
            .httpStatus(HttpStatus.BAD_REQUEST)
            .timeStamp(LocalDateTime.now())
            .build();

    return new ResponseEntity<>(applicationError, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({BadCredentialsException.class})
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<Object> handleBadCredentialsException(
      final BadCredentialsException exception) {

    ApplicationPropertyEntity propertyEntity =
        errorMessageService.findOrCreatePropertyValue(
            UNAUTHORIZED_USER.getMessageKey(), sessionLanguage());

    ApplicationError applicationError =
        ApplicationError.builder()
            .errorMessage(propertyEntity.getPropertyValue())
            .errorCode(ErrorCode.UNAUTHORIZED_USER)
            .referenceId(propertyEntity.getId())
            .httpStatus(HttpStatus.UNAUTHORIZED)
            .timeStamp(LocalDateTime.now())
            .build();

    return new ResponseEntity<>(applicationError, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({MethodArgumentNotValidException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Object> handleGlobalException(
      final MethodArgumentNotValidException exception) {

    List<ValidationError> validationErrors =
        exception.getBindingResult().getFieldErrors().stream()
            .map(
                error ->
                    ValidationError.builder()
                        .field(error.getField())
                        .rejectedValue(error.getRejectedValue())
                        .message(error.getDefaultMessage())
                        .build())
            .toList();

    ApplicationPropertyEntity propertyEntity =
        errorMessageService.findOrCreatePropertyValue(
            INVALID_REQUEST_BODY.getMessageKey(), sessionLanguage());

    ApplicationError applicationError =
        ApplicationError.builder()
            .errorMessage(propertyEntity.getPropertyValue())
            .errorCode(INVALID_REQUEST_BODY)
            .referenceId(propertyEntity.getId())
            .httpStatus(HttpStatus.BAD_REQUEST)
            .timeStamp(LocalDateTime.now())
            .errorDetails(validationErrors)
            .build();

    return new ResponseEntity<>(applicationError, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({DataIntegrityViolationException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Object> handleDatabaseException(
      final DataIntegrityViolationException exception) {

    ApplicationPropertyEntity propertyEntity =
        errorMessageService.findOrCreatePropertyValue(
            DATA_INTEGRITY_VIOLATION.getMessageKey(), sessionLanguage());

    log.error("Database exception: ", exception);
    ApplicationError applicationError =
        ApplicationError.builder()
            .errorMessage(propertyEntity.getPropertyValue())
            .errorCode(DATA_INTEGRITY_VIOLATION)
            .referenceId(propertyEntity.getId())
            .httpStatus(HttpStatus.BAD_REQUEST)
            .timeStamp(LocalDateTime.now())
            .build();

    return new ResponseEntity<>(applicationError, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Object> handleInvalidEnumValue(
      final HttpMessageNotReadableException exception) {

    ApplicationPropertyEntity propertyEntity =
        errorMessageService.findOrCreatePropertyValue(
            INVALID_ENUM_VALUE.getMessageKey(), sessionLanguage());

    ApplicationError applicationError =
        ApplicationError.builder()
            .errorMessage(propertyEntity.getPropertyValue())
            .errorCode(INVALID_ENUM_VALUE)
            .referenceId(propertyEntity.getId())
            .httpStatus(HttpStatus.BAD_REQUEST)
            .timeStamp(LocalDateTime.now())
            .build();

    log.warn("Json parse exception: ", exception);
    return new ResponseEntity<>(applicationError, HttpStatus.BAD_REQUEST);
  }

  private Language sessionLanguage() {
    return IJwtService.findSessionUser().getSessionLanguage();
  }
}
