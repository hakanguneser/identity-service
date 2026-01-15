package com.gastroblue.exception.helper;

import static com.gastroblue.model.enums.ErrorCode.*;

import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.exception.base.AbstractRuntimeException;
import com.gastroblue.model.entity.ErrorMessageEntity;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.enums.Language;
import com.gastroblue.model.exception.ApplicationError;
import com.gastroblue.model.exception.ValidationError;
import com.gastroblue.service.IJwtService;
import com.gastroblue.service.impl.ErrorMessageService;
import com.gastroblue.util.EnumConfigUtil;
import java.time.LocalDateTime;
import java.util.List;
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

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHelper {
  private final ErrorMessageService errorMessageService;
  private final EnumConfigUtil enumConfigUtil;

  @ExceptionHandler({IllegalDefinitionException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Object> handleGlobalException(final IllegalDefinitionException exception) {
    Language sessionLanguage = sessionLanguage();
    ErrorMessageEntity errorProp =
        errorMessageService.findOrCreatePropertyValue(exception.getErrorCode(), sessionLanguage);
    ApplicationError applicationError =
        ApplicationError.builder()
            .errorMessage(errorProp.getMessage())
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

    ErrorMessageEntity propertyEntity =
        errorMessageService.findOrCreatePropertyValue(exception.getErrorCode(), sessionLanguage());

    ApplicationError applicationError =
        ApplicationError.builder()
            .errorMessage(propertyEntity.getMessage())
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

    ErrorMessageEntity propertyEntity =
        errorMessageService.findOrCreatePropertyValue(UNAUTHORIZED_USER, sessionLanguage());

    ApplicationError applicationError =
        ApplicationError.builder()
            .errorMessage(propertyEntity.getMessage())
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

    ErrorMessageEntity propertyEntity =
        errorMessageService.findOrCreatePropertyValue(INVALID_REQUEST_BODY, sessionLanguage());

    ApplicationError applicationError =
        ApplicationError.builder()
            .errorMessage(propertyEntity.getMessage())
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

    ErrorMessageEntity propertyEntity =
        errorMessageService.findOrCreatePropertyValue(DATA_INTEGRITY_VIOLATION, sessionLanguage());

    log.error("Database exception: ", exception);
    ApplicationError applicationError =
        ApplicationError.builder()
            .errorMessage(propertyEntity.getMessage())
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

    ErrorMessageEntity propertyEntity =
        errorMessageService.findOrCreatePropertyValue(INVALID_ENUM_VALUE, sessionLanguage());

    ApplicationError applicationError =
        ApplicationError.builder()
            .errorMessage(propertyEntity.getMessage())
            .errorCode(INVALID_ENUM_VALUE)
            .referenceId(propertyEntity.getId())
            .httpStatus(HttpStatus.BAD_REQUEST)
            .timeStamp(LocalDateTime.now())
            .build();

    log.warn("Json parse exception: ", exception);
    return new ResponseEntity<>(applicationError, HttpStatus.BAD_REQUEST);
  }

  private Language sessionLanguage() {
    return IJwtService.findSessionUser() == null
        ? Language.defaultLang()
        : IJwtService.findSessionUser().getSessionLanguage();
  }
}
