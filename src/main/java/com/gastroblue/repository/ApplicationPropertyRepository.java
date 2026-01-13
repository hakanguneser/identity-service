package com.gastroblue.repository;

import com.gastroblue.model.entity.ApplicationPropertyEntity;
import com.gastroblue.model.enums.Language;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationPropertyRepository
    extends JpaRepository<ApplicationPropertyEntity, String> {
  Optional<ApplicationPropertyEntity> findByPropertyKeyAndLanguage(
      String propertyKey, Language language);

  List<ApplicationPropertyEntity> findByPropertyKey(String propertyKey);

  List<ApplicationPropertyEntity> findByPropertyKeyLike(String propertyKey);
}
