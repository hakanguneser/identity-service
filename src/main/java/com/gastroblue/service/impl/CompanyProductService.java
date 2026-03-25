package com.gastroblue.service.impl;

import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.model.entity.CompanyProductEntity;
import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.repository.CompanyProductRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyProductService {

  private final CompanyProductRepository companyProductRepository;

  public CompanyProductEntity save(CompanyProductEntity entity) {
    return companyProductRepository.save(entity);
  }

  public Optional<CompanyProductEntity> findByCompanyIdAndProduct(
      String companyId, ApplicationProduct product) {
    return companyProductRepository.findByCompanyIdAndProduct(companyId, product);
  }

  public CompanyProductEntity findByCompanyIdAndProductOrThrow(
      String companyId, ApplicationProduct product) {
    return companyProductRepository
        .findByCompanyIdAndProduct(companyId, product)
        .orElseThrow(
            () -> {
              log.debug("CompanyProduct not found for companyId={} product={}", companyId, product);
              return new IllegalDefinitionException(
                  ErrorCode.COMPANY_PRODUCT_NOT_FOUND, "Product not defined for company");
            });
  }

  public List<CompanyProductEntity> findAllByCompanyId(String companyId) {
    return companyProductRepository.findAllByCompanyId(companyId);
  }

  public CompanyProductEntity update(String id, CompanyProductEntity updated) {
    CompanyProductEntity existing =
        companyProductRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new IllegalDefinitionException(
                        ErrorCode.COMPANY_PRODUCT_NOT_FOUND, "CompanyProduct not found"));
    existing.setEnabled(updated.getEnabled());
    existing.setLicenseExpiresAt(updated.getLicenseExpiresAt());
    existing.setAgreedUserCount(updated.getAgreedUserCount());
    return companyProductRepository.save(existing);
  }

  public void delete(String id) {
    companyProductRepository.deleteById(id);
  }
}
