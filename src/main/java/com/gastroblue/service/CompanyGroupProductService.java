package com.gastroblue.service;

import com.gastroblue.commons.helper.exception.type.NotFoundException;
import com.gastroblue.commons.shared.enums.ApplicationProduct;
import com.gastroblue.model.entity.CompanyGroupProductEntity;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.repository.CompanyGroupProductRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyGroupProductService {

  private final CompanyGroupProductRepository companyGroupProductRepository;

  public CompanyGroupProductEntity save(CompanyGroupProductEntity entity) {
    return companyGroupProductRepository.save(entity);
  }

  public List<CompanyGroupProductEntity> findAllByCompanyGroupId(String companyGroupId) {
    return companyGroupProductRepository.findAllByCompanyGroupId(companyGroupId);
  }

  public Optional<CompanyGroupProductEntity> findByCompanyGroupIdAndProduct(
      String companyGroupId, ApplicationProduct product) {
    return companyGroupProductRepository.findByCompanyGroupIdAndProduct(companyGroupId, product);
  }

  public CompanyGroupProductEntity findByCompanyGroupIdAndProductOrThrow(
      String companyGroupId, ApplicationProduct product) {
    return companyGroupProductRepository
        .findByCompanyGroupIdAndProduct(companyGroupId, product)
        .orElseThrow(
            () -> {
              log.debug(
                  "CompanyGroupProduct not found for groupId={} product={}",
                  companyGroupId,
                  product);
              return new NotFoundException(
                  ErrorCode.COMPANY_GROUP_PRODUCT_NOT_FOUND,
                  "Product not defined for company group");
            });
  }
}
