package com.gastroblue.facade;

import static com.gastroblue.util.DelimitedStringUtil.join;
import static com.gastroblue.util.DelimitedStringUtil.split;

import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.mapper.CompanyGroupMapper;
import com.gastroblue.model.entity.CompanyEntity;
import com.gastroblue.model.entity.CompanyGroupEntity;
import com.gastroblue.model.entity.CompanyGroupProductEntity;
import com.gastroblue.model.entity.CompanyProductEntity;
import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.request.CompanyProductSaveRequest;
import com.gastroblue.model.request.CompanyProductUpdateRequest;
import com.gastroblue.model.request.CompanySaveRequest;
import com.gastroblue.model.request.CompanyUpdateRequest;
import com.gastroblue.model.response.CompanyContextResponse;
import com.gastroblue.model.response.CompanyDefinitionResponse;
import com.gastroblue.model.response.CompanyProductResponse;
import com.gastroblue.service.impl.CompanyGroupProductService;
import com.gastroblue.service.impl.CompanyGroupService;
import com.gastroblue.service.impl.CompanyProductService;
import com.gastroblue.service.impl.CompanyService;
import com.gastroblue.util.EmailDomainValidator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyDefinitionFacade {

  private final CompanyService companyService;
  private final CompanyGroupService companyGroupService;
  private final CompanyGroupProductService companyGroupProductService;
  private final CompanyProductService companyProductService;
  private final EnumConfigurationFacade enumConfigurationFacade;

  public List<CompanyDefinitionResponse> findCompaniesByCompanyGroupId(String companyGroupId) {
    return companyService.findByCompanyGroupId(companyGroupId).stream()
        .map(entity -> CompanyGroupMapper.toResponse(entity, enumConfigurationFacade))
        .toList();
  }

  public CompanyDefinitionResponse findCompanyByCompanyIdAndCompanyGroupId(
      String companyGroupId, String companyId) {
    CompanyEntity entity = companyService.findByCompanyGroupIdAndId(companyGroupId, companyId);
    return CompanyGroupMapper.toResponse(entity, enumConfigurationFacade);
  }

  public CompanyDefinitionResponse saveCompany(String companyGroupId, CompanySaveRequest request) {
    CompanyGroupEntity companyGroup = companyGroupService.findByIdOrThrow(companyGroupId);
    EmailDomainValidator.validateAllowedDomains(
        split(companyGroup.getMailDomains()), request.companyMail());

    Map<ApplicationProduct, Boolean> productDefaults =
        companyGroupProductService.findAllByCompanyGroupId(companyGroupId).stream()
            .collect(
                Collectors.toMap(
                    CompanyGroupProductEntity::getProduct, CompanyGroupProductEntity::getEnabled));

    CompanyEntity entityToBeSave = CompanyGroupMapper.toEntity(request, companyGroupId);
    entityToBeSave.setCheckEnabled(productDefaults.getOrDefault(ApplicationProduct.CHECK, false));
    entityToBeSave.setFormflowEnabled(
        productDefaults.getOrDefault(ApplicationProduct.FORMFLOW, false));
    entityToBeSave.setTrackerEnabled(
        productDefaults.getOrDefault(ApplicationProduct.TRACKER, false));

    CompanyEntity savedCompany = companyService.save(entityToBeSave);
    return CompanyGroupMapper.toResponse(savedCompany, enumConfigurationFacade);
  }

  public CompanyDefinitionResponse updateCompany(
      String companyGroupId, String companyId, CompanyUpdateRequest request) {
    CompanyGroupEntity companyGroup = companyGroupService.findByIdOrThrow(companyGroupId);
    CompanyEntity entityToBeUpdated =
        companyService.findByCompanyGroupIdAndId(companyGroupId, companyId);

    EmailDomainValidator.validateAllowedDomains(
        split(companyGroup.getMailDomains()), request.companyMail());
    entityToBeUpdated.setCompanyName(request.companyName());
    entityToBeUpdated.setCompanyGroupId(companyGroupId);
    entityToBeUpdated.setCompanyMail(join(request.companyMail()));
    entityToBeUpdated.setCountry(request.country());
    entityToBeUpdated.setCity(request.city());
    entityToBeUpdated.setZone(request.zone());
    entityToBeUpdated.setSegment1(request.segment1());
    entityToBeUpdated.setSegment2(request.segment2());
    entityToBeUpdated.setSegment3(request.segment3());
    entityToBeUpdated.setSegment4(request.segment4());
    entityToBeUpdated.setSegment5(request.segment5());
    CompanyEntity savedCompany = companyService.save(entityToBeUpdated);
    return CompanyGroupMapper.toResponse(savedCompany, enumConfigurationFacade);
  }

  public CompanyDefinitionResponse toggleCompanyStatus(String companyGroupId, String companyId) {
    CompanyEntity companyEntity = companyService.toggleCompanyStatus(companyGroupId, companyId);
    return CompanyGroupMapper.toResponse(companyEntity, enumConfigurationFacade);
  }

  public CompanyDefinitionResponse toggleCompanyProduct(
      String companyGroupId, String companyId, ApplicationProduct product) {
    companyGroupProductService.findByCompanyGroupIdAndProductOrThrow(companyGroupId, product);
    CompanyEntity updated = companyService.toggleProductEnabled(companyGroupId, companyId, product);
    return CompanyGroupMapper.toResponse(updated, enumConfigurationFacade);
  }

  public List<CompanyProductResponse> findCompanyProducts(String companyGroupId, String companyId) {
    companyService.findByCompanyGroupIdAndId(companyGroupId, companyId);
    return companyProductService.findAllByCompanyId(companyId).stream()
        .map(CompanyGroupMapper::toResponse)
        .toList();
  }

  public CompanyProductResponse saveCompanyProduct(
      String companyGroupId, String companyId, CompanyProductSaveRequest request) {
    companyService.findByCompanyGroupIdAndId(companyGroupId, companyId);
    companyGroupProductService.findByCompanyGroupIdAndProductOrThrow(
        companyGroupId, request.product());
    companyProductService
        .findByCompanyIdAndProduct(companyId, request.product())
        .ifPresent(
            existing -> {
              throw new IllegalDefinitionException(
                  ErrorCode.COMPANY_PRODUCT_ALREADY_EXISTS,
                  "Product already assigned to company: " + request.product());
            });
    CompanyProductEntity entity =
        CompanyProductEntity.builder()
            .companyId(companyId)
            .product(request.product())
            .enabled(request.enabled())
            .licenseExpiresAt(request.licenseExpiresAt())
            .agreedUserCount(request.agreedUserCount())
            .build();
    return CompanyGroupMapper.toResponse(companyProductService.save(entity));
  }

  public CompanyProductResponse updateCompanyProduct(
      String companyGroupId,
      String companyId,
      ApplicationProduct product,
      CompanyProductUpdateRequest request) {
    companyService.findByCompanyGroupIdAndId(companyGroupId, companyId);
    CompanyProductEntity existing =
        companyProductService.findByCompanyIdAndProductOrThrow(companyId, product);
    CompanyProductEntity updated =
        CompanyProductEntity.builder()
            .enabled(request.enabled())
            .licenseExpiresAt(request.licenseExpiresAt())
            .agreedUserCount(request.agreedUserCount())
            .build();
    return CompanyGroupMapper.toResponse(companyProductService.update(existing.getId(), updated));
  }

  public void deleteCompanyProduct(
      String companyGroupId, String companyId, ApplicationProduct product) {
    companyService.findByCompanyGroupIdAndId(companyGroupId, companyId);
    CompanyProductEntity existing =
        companyProductService.findByCompanyIdAndProductOrThrow(companyId, product);
    companyProductService.delete(existing.getId());
  }

  public CompanyContextResponse findCompanyAndGroupContext(String groupCode, String companyCode) {
    CompanyGroupEntity group = companyGroupService.findByGroupCode(groupCode);
    CompanyEntity company =
        companyService
            .findByCompanyCode(companyCode)
            .orElseThrow(
                () -> {
                  log.debug("Company not found with code: {}", companyCode);
                  return new IllegalDefinitionException(
                      ErrorCode.COMPANY_NOT_FOUND, "Company not found: " + companyCode);
                });

    return CompanyContextResponse.builder()
        .companyGroup(CompanyGroupMapper.toResponse(group))
        .company(CompanyGroupMapper.toResponse(company, enumConfigurationFacade))
        .build();
  }
}
