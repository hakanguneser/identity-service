package com.gastroblue.service.impl;

import com.gastroblue.model.entity.ErrorMessageEntity;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.enums.Language;
import com.gastroblue.repository.ErrorMessageEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationPropertyService {

  private final ErrorMessageEntityRepository applicationPropertyRepository;

  @Cacheable(value = "appProperties", key = "#errorCode.name() + '_' + #language.name()")
  public ErrorMessageEntity findOrCreatePropertyValue(ErrorCode errorCode, Language language) {
    log.info("Resolving code {} for locale {}", errorCode, language.name());
    return applicationPropertyRepository
        .findByErrorCodeAfterAndLanguage(errorCode, language)
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
    return applicationPropertyRepository.save(entityToBeSave);
  }
}
