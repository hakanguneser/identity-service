package com.gastroblue.service.impl;

import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.model.entity.CompanyEntity;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.repository.CompanyRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

  private final CompanyRepository companyRepository;

  public List<CompanyEntity> findByBaseId(List<String> idList) {
    return companyRepository.findByIdIn(idList);
  }

  public CompanyEntity findByIdOrThrow(String id) {
    return companyRepository
        .findById(id)
        .orElseThrow(
            () ->
                new IllegalDefinitionException(
                    ErrorCode.COMPANY_NOT_FOUND,
                    String.format("Company not found (companyId=%s)", id)));
  }

  public List<CompanyEntity> findByCompanyGroupId(String companyGroupId) {
    return companyRepository.findByCompanyGroupId(companyGroupId);
  }

  public List<CompanyEntity> findAll() {
    return companyRepository.findAll();
  }

  public CompanyEntity save(CompanyEntity companyEntity) {
    return companyRepository.save(companyEntity);
  }

  public CompanyEntity findByCompanyGroupIdAndId(String companyGroupId, String companyId) {
    return companyRepository
        .findById(companyId)
        .filter(e -> e.getCompanyGroupId().equals(companyGroupId))
        .orElseThrow(
            () ->
                new IllegalDefinitionException(
                    ErrorCode.COMPANY_NOT_FOUND,
                    String.format(
                        "Company not found (companyId=%s, companyGroupId=%s)",
                        companyId, companyGroupId)));
  }

  public CompanyEntity toggleCompanyStatus(String companyGroupId, String companyId) {
    CompanyEntity entity =
        companyRepository
            .findById(companyId)
            .filter(e -> e.getCompanyGroupId().equals(companyGroupId))
            .orElseThrow(
                () ->
                    new IllegalDefinitionException(
                        ErrorCode.COMPANY_NOT_FOUND,
                        String.format(
                            "Company not found (companyId=%s, companyGroupId=%s)",
                            companyId, companyGroupId)));

    entity.setActive(!entity.isActive());
    return companyRepository.save(entity);
  }

  public java.util.Optional<CompanyEntity> findByCompanyCode(String companyCode) {
    return companyRepository.findByCompanyCode(companyCode);
  }
}
