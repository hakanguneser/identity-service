package com.gastroblue.repository;

import com.gastroblue.model.entity.CompanyGroupEulaContentEntity;
import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.enums.Language;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyGroupEulaContentRepository
    extends JpaRepository<CompanyGroupEulaContentEntity, String> {

  List<CompanyGroupEulaContentEntity> findAllByCompanyGroupId(String companyGroupId);

  @Query(
      """
      select
          e
      from
          CompanyGroupEulaContentEntity e
      where
          e.language = :language
          and e.product = :product
          and e.startDate <= :checkDate
          and (e.endDate is null or e.endDate >= :checkDate)
          and (e.companyGroupId = :companyGroupId or e.companyGroupId is null)
      order by case when e.companyGroupId = :companyGroupId then 0 else 1 end fetch first 1 row only
 """)
  Optional<CompanyGroupEulaContentEntity> findActiveContent(
      @Param("companyGroupId") String companyGroupId,
      @Param("product") ApplicationProduct product,
      @Param("language") Language language,
      @Param("checkDate") LocalDate checkDate);
}
