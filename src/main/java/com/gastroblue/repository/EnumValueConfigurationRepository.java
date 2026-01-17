package com.gastroblue.repository;

import com.gastroblue.model.entity.EnumValueConfigurationEntity;
import com.gastroblue.model.enums.Language;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnumValueConfigurationRepository
    extends JpaRepository<EnumValueConfigurationEntity, String> {

  List<EnumValueConfigurationEntity> findByCompanyGroupIdAndEnumTypeAndLanguage(
      String companyGroupId, String enumType, Language language);

  List<EnumValueConfigurationEntity> findByCompanyGroupId(String companyGroupId);

  Optional<EnumValueConfigurationEntity> findByIdAndCompanyGroupId(
      String id, String companyGroupId);
}
