package com.gastroblue.repository;

import com.gastroblue.model.entity.CompanyGroupEulaContentEntity;
import com.gastroblue.model.enums.Language;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyGroupEulaContentRepository
    extends JpaRepository<CompanyGroupEulaContentEntity, String> {

  List<CompanyGroupEulaContentEntity> findAllByCompanyGroupId(String companyGroupId);

  @Query(
      "SELECT e FROM CompanyGroupEulaContentEntity e "
          + "WHERE e.companyGroupId = :companyGroupId "
          + "AND e.language = :language "
          + "AND :checkDate >= e.startDate "
          + "AND (e.endDate IS NULL OR :checkDate <= e.endDate)")
  Optional<CompanyGroupEulaContentEntity> findActiveContent(
      String companyGroupId, Language language, LocalDateTime checkDate);
}
