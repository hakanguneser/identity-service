package com.gastroblue.repository;

import com.gastroblue.model.entity.CompanyProductEntity;
import com.gastroblue.model.enums.ApplicationProduct;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyProductRepository extends JpaRepository<CompanyProductEntity, String> {

  Optional<CompanyProductEntity> findByCompanyIdAndProduct(
      String companyId, ApplicationProduct product);

  List<CompanyProductEntity> findAllByCompanyId(String companyId);
}
