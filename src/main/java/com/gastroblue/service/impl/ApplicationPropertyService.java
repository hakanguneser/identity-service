package com.gastroblue.service.impl;

import static com.gastroblue.model.enums.DefinitionType.APPLICATION_PROPERTY;

import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.mapper.ApplicationPropertyMapper;
import com.gastroblue.model.entity.ApplicationPropertyEntity;
import com.gastroblue.model.enums.Language;
import com.gastroblue.model.request.ApplicationPropertyUpdateRequest;
import com.gastroblue.model.response.ApplicationPropertyResponse;
import com.gastroblue.repository.ApplicationPropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationPropertyService {

  private final ApplicationPropertyRepository applicationPropertyRepository;

  @Cacheable(value = "appProperties", key = "#propertyKey + '_' + #language.name()")
  public ApplicationPropertyEntity findOrCreatePropertyValue(
      String propertyKey, Language language) {
    log.info("Resolving code {} for locale {}", propertyKey, language.name());
    return applicationPropertyRepository
        .findByPropertyKeyAndLanguage(propertyKey, language)
        .orElseGet(() -> addFirst(propertyKey, language));
  }

  private ApplicationPropertyEntity addFirst(String propertyKey, Language language) {
    log.info("Creating property for {}-{}", propertyKey, language.name());
    ApplicationPropertyEntity entityToBeSave =
        ApplicationPropertyEntity.builder()
            .propertyKey(propertyKey)
            .language(language)
            .propertyValue(String.format("%s [%s]", propertyKey, language))
            .build();
    return applicationPropertyRepository.save(entityToBeSave);
  }

  public ApplicationPropertyResponse update(String id, ApplicationPropertyUpdateRequest request) {
    ApplicationPropertyEntity entityToBeUpdate =
        applicationPropertyRepository
            .findById(id)
            .orElseThrow(() -> new IllegalDefinitionException(APPLICATION_PROPERTY));
    entityToBeUpdate.setPropertyValue(request.propertyValue());
    return ApplicationPropertyMapper.toResponse(
        applicationPropertyRepository.save(entityToBeUpdate));
  }

  public ApplicationPropertyResponse findById(String id) {
    return ApplicationPropertyMapper.toResponse(
        applicationPropertyRepository
            .findById(id)
            .orElseThrow(() -> new IllegalDefinitionException(APPLICATION_PROPERTY)));
  }
}
