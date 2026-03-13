package com.gastroblue.repository;

import com.gastroblue.model.entity.EnumValueConfigurationEntity;
import com.gastroblue.model.enums.Language;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EnumValueConfigurationRepository
    extends JpaRepository<EnumValueConfigurationEntity, String> {

  /**
   * Returns default rows (companyGroupId IS NULL) plus company-group-specific overrides in one
   * query. The service merges them so overrides win over defaults.
   */
  @Query(
      "SELECT e FROM EnumValueConfigurationEntity e "
          + "WHERE e.enumType = :enumType AND e.language = :language "
          + "AND (e.companyGroupId IS NULL OR e.companyGroupId = :companyGroupId)")
  List<EnumValueConfigurationEntity> findForGroupWithDefaults(
      @Param("enumType") String enumType,
      @Param("companyGroupId") String companyGroupId,
      @Param("language") Language language);

  /** For admin listing — returns all rows belonging to a company group (or defaults if null). */
  List<EnumValueConfigurationEntity> findByCompanyGroupId(String companyGroupId);

  Optional<EnumValueConfigurationEntity> findByIdAndCompanyGroupId(
      String id, String companyGroupId);
}
