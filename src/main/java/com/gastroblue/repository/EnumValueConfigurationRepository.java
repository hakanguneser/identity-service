package com.gastroblue.repository;

import com.gastroblue.model.entity.EnumValueConfiguration;
import com.gastroblue.model.enums.Language;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnumValueConfigurationRepository
    extends JpaRepository<EnumValueConfiguration, String> {

  List<EnumValueConfiguration> findByCompanyGroupIdAndEnumTypeAndLanguage(
      String companyGroupId, String enumType, Language language);

  List<EnumValueConfiguration> findByCompanyGroupId(String companyGroupId);
}
