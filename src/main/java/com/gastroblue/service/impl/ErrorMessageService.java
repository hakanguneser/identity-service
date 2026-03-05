package com.gastroblue.service.impl;

import com.gastroblue.model.entity.ErrorMessageEntity;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.enums.Language;
import com.gastroblue.repository.ErrorMessageEntityRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ErrorMessageService {

  private final ErrorMessageEntityRepository repository;

  @Cacheable(value = "errorMessages", key = "#errorCode.name() + '_' + #language.name()")
  public ErrorMessageEntity findOrCreatePropertyValue(ErrorCode errorCode, Language language) {
    log.info("Resolving code {} for locale {}", errorCode, language.name());
    return repository
        .findByErrorCodeAndLanguage(errorCode, language)
        .orElseGet(() -> addFirst(errorCode, language));
  }

  private ErrorMessageEntity addFirst(ErrorCode errorCode, Language language) {
    log.info("Creating property for {}-{}", errorCode, language.name());
    ErrorMessageEntity entityToBeSave =
        ErrorMessageEntity.builder()
            .errorCode(errorCode)
            .language(language)
            .message(String.format("%s [%s]", errorCode, language))
            .build();
    return repository.save(entityToBeSave);
  }

  public ErrorMessageEntity save(ErrorMessageEntity entity) {
    return repository.save(entity);
  }

  public ErrorMessageEntity findById(String id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("ErrorMessageEntity not found"));
  }

  public List<ErrorMessageEntity> findAll() {
    return repository.findAll();
  }

  public void delete(String id) {
    repository.deleteById(id);
  }
}
