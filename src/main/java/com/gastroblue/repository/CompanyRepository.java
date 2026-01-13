package com.gastroblue.repository;

import com.gastroblue.model.entity.CompanyEntity;
import com.gastroblue.model.enums.Zone;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<CompanyEntity, String> {

  Optional<CompanyEntity> findByCompanyCode(String companyCode);

  List<CompanyEntity> findByCompanyGroupId(String companyGroupId);

  List<CompanyEntity> findByCompanyGroupIdAndZone(String companyGroupId, Zone zone);
}
