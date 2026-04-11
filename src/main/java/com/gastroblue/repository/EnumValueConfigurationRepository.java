package com.gastroblue.repository;

import com.gastroblue.model.entity.EnumValueConfigurationEntity;
import com.gastroblue.model.enums.ApplicationProduct;
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

  /**
   * Same two-tier lookup as {@link #findForGroupWithDefaults} but strictly scoped to a given {@link
   * ApplicationProduct}. Used for product-specific enums (e.g. Department).
   */
  @Query(
      "SELECT e FROM EnumValueConfigurationEntity e "
          + "WHERE e.enumType = :enumType AND e.language = :language "
          + "AND (e.companyGroupId IS NULL OR e.companyGroupId = :companyGroupId) "
          + "AND e.product = :product")
  List<EnumValueConfigurationEntity> findForGroupWithDefaultsByProduct(
      @Param("enumType") String enumType,
      @Param("companyGroupId") String companyGroupId,
      @Param("language") Language language,
      @Param("product") ApplicationProduct product);

  @Query(
      "SELECT e FROM EnumValueConfigurationEntity e "
          + "WHERE e.enumType = :enumType AND e.language = :language "
          + "AND (e.companyGroupId IS NULL OR e.companyGroupId = :companyGroupId) "
          + "AND e.parentKey = :parentKey AND e.parentEnumType = :parentEnumType")
  List<EnumValueConfigurationEntity> findChildrenWithDefaults(
      @Param("enumType") String enumType,
      @Param("companyGroupId") String companyGroupId,
      @Param("language") Language language,
      @Param("parentKey") String parentKey,
      @Param("parentEnumType") String parentEnumType);
}
