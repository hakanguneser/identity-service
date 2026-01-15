package com.gastroblue.repository;

import com.gastroblue.model.entity.EnumValueConfiguration;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnumValueConfigurationRepository
    extends JpaRepository<EnumValueConfiguration, String> {
  List<EnumValueConfiguration> findByCompanyIdAndEnumTypeAndLanguage(
      String companyId, String enumType, String language);
}
