package com.gastroblue.service.impl;

import static com.gastroblue.model.enums.DefinitionType.APPLICATION_PROPERTY;

import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.mapper.ApplicationPropertyMapper;
import com.gastroblue.model.entity.ApplicationPropertyEntity;
import com.gastroblue.model.enums.Language;
import com.gastroblue.model.request.ApplicationPropertySaveRequest;
import com.gastroblue.model.request.ApplicationPropertyUpdateRequest;
import com.gastroblue.model.response.ApplicationPropertyResponse;
import com.gastroblue.model.shared.EnumDisplay;
import com.gastroblue.repository.ApplicationPropertyRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

  @Cacheable(
      value = "dropdownItems",
      key =
          "#enumClass.name + '_' + T(org.springframework.context.i18n.LocaleContextHolder).getLocale().toLanguageTag()")
  public <T extends Enum<T>> List<EnumDisplay> getDropdownItems(Class<T> enumClass) {
    if (enumClass == null || enumClass.getEnumConstants() == null) {
      return Collections.emptyList();
    }
    return Arrays.stream(enumClass.getEnumConstants()).map(EnumDisplay::of).toList();
  }

  public List<ApplicationPropertyResponse> saveBatch(
      List<ApplicationPropertySaveRequest> requests) {
    return requests.stream()
        // Request → Entity
        .map(ApplicationPropertyMapper::toEntity)
        // Save or update
        .map(this::saveOrUpdate)
        // Entity → Response
        .map(ApplicationPropertyMapper::toResponse)
        .toList();
  }

  private ApplicationPropertyEntity saveOrUpdate(ApplicationPropertyEntity entityToBeSave) {
    return applicationPropertyRepository
        .findByPropertyKeyAndLanguage(entityToBeSave.getPropertyKey(), entityToBeSave.getLanguage())
        .map(
            existing -> {
              existing.setPropertyValue(entityToBeSave.getPropertyValue());
              existing.setDescription(entityToBeSave.getDescription());
              return applicationPropertyRepository.save(existing);
            })
        .orElseGet(() -> applicationPropertyRepository.save(entityToBeSave));
  }

  public ApplicationPropertyResponse save(ApplicationPropertySaveRequest request) {
    ApplicationPropertyEntity entityToBeSave = ApplicationPropertyMapper.toEntity(request);
    return ApplicationPropertyMapper.toResponse(applicationPropertyRepository.save(entityToBeSave));
  }

  public List<ApplicationPropertyResponse> findByPropertyKey(String propertyKey) {
    return applicationPropertyRepository.findByPropertyKey(propertyKey).stream()
        .map(ApplicationPropertyMapper::toResponse)
        .toList();
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

  public void delete(String id) {
    ApplicationPropertyEntity entityToBeDelete =
        applicationPropertyRepository
            .findById(id)
            .orElseThrow(() -> new IllegalDefinitionException(APPLICATION_PROPERTY));
    applicationPropertyRepository.delete(entityToBeDelete);
  }

  public List<ApplicationPropertyResponse> findAll() {
    return applicationPropertyRepository.findAll().stream()
        .map(ApplicationPropertyMapper::toResponse)
        .toList();
  }

  public List<ApplicationPropertyResponse> findAllByPropKeyLike(String propertyKey) {
    return applicationPropertyRepository.findByPropertyKeyLike(propertyKey).stream()
        .map(ApplicationPropertyMapper::toResponse)
        .toList();
  }
}
