package com.gastroblue.repository;

import com.gastroblue.model.entity.ErrorMessageEntity;
import io.gastroblue.commons.shared.enums.Language;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorMessageEntityRepository extends JpaRepository<ErrorMessageEntity, String> {
  Optional<ErrorMessageEntity> findByErrorCodeAndLanguage(String errorCode, Language language);
}
