package com.gastroblue.repository;

import com.gastroblue.model.entity.CompanyGroupProductEntity;
import com.gastroblue.model.enums.ApplicationProduct;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyGroupProductRepository
    extends JpaRepository<CompanyGroupProductEntity, String> {

  List<CompanyGroupProductEntity> findAllByCompanyGroupId(String companyGroupId);

  Optional<CompanyGroupProductEntity> findByCompanyGroupIdAndProduct(
      String companyGroupId, ApplicationProduct product);
}
