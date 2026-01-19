package com.gastroblue.service.impl;

import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.mapper.CompanyGroupMapper;
import com.gastroblue.model.base.Company;
import com.gastroblue.model.entity.CompanyEntity;
import com.gastroblue.model.enums.Zone;
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

  public Company findByBaseId(String id) {
    return companyRepository
        .findById(id)
        .map(CompanyGroupMapper::toBase)
        .orElseThrow(
            () ->
                new IllegalDefinitionException(
                    String.format("Company not found (companyId=%s)", id)));
  }

  public CompanyEntity findOrThrow(String id) {
    return companyRepository
        .findById(id)
        .orElseThrow(
            () ->
                new IllegalDefinitionException(
                    String.format("Company not found (companyId=%s)", id)));
  }

  public List<CompanyEntity> findByCompanyGroupId(String companyGroupId) {
    return companyRepository.findByCompanyGroupId(companyGroupId);
  }

  public List<CompanyEntity> findByCompanyGroupIdAndZone(String companyGroupId, Zone zone) {
    return companyRepository.findByCompanyGroupIdAndZone(companyGroupId, zone);
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
