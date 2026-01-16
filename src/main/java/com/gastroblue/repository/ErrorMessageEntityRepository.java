package com.gastroblue.repository;

import com.gastroblue.model.entity.ErrorMessageEntity;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.enums.Language;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorMessageEntityRepository extends JpaRepository<ErrorMessageEntity, String> {
  Optional<ErrorMessageEntity> findByErrorCodeAndLanguage(ErrorCode errorCode, Language language);
}
