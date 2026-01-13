package com.gastroblue.repository;

import com.gastroblue.model.entity.ApplicationPropertyEntity;
import com.gastroblue.model.enums.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationPropertyRepository
    extends JpaRepository<ApplicationPropertyEntity, String> {
  Optional<ApplicationPropertyEntity> findByPropertyKeyAndLanguage(
      String propertyKey, Language language);

  List<ApplicationPropertyEntity> findByPropertyKey(String propertyKey);

  List<ApplicationPropertyEntity> findByPropertyKeyLike(String propertyKey);
}
