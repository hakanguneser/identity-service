package com.gastroblue.facade;

import static com.gastroblue.commons.shared.util.DelimitedStringUtil.join;
import static com.gastroblue.commons.shared.util.DelimitedStringUtil.split;

import com.gastroblue.commons.helper.exception.type.NotFoundException;
import com.gastroblue.commons.helper.lookup.service.ILookupService;
import com.gastroblue.commons.shared.enums.ApplicationProduct;
import com.gastroblue.mapper.CompanyGroupMapper;
import com.gastroblue.model.entity.CompanyEntity;
import com.gastroblue.model.entity.CompanyGroupEntity;
import com.gastroblue.model.entity.CompanyProductEntity;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.request.CompanyProductSaveRequest;
import com.gastroblue.model.request.CompanyProductUpdateRequest;
import com.gastroblue.model.request.CompanySaveRequest;
import com.gastroblue.model.request.CompanyUpdateRequest;
import com.gastroblue.model.response.CompanyContextResponse;
import com.gastroblue.model.response.CompanyDefinitionListResponse;
import com.gastroblue.model.response.CompanyDefinitionResponse;
import com.gastroblue.model.response.CompanyProductListResponse;
import com.gastroblue.model.response.CompanyProductResponse;
import com.gastroblue.service.CompanyGroupProductService;
import com.gastroblue.service.CompanyGroupService;
import com.gastroblue.service.CompanyProductService;
import com.gastroblue.service.CompanyService;
import com.gastroblue.util.EmailDomainValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyDefinitionFacade {

  private final CompanyService companyService;
  private final CompanyGroupService companyGroupService;
  private final CompanyGroupProductService companyGroupProductService;
  private final CompanyProductService companyProductService;
  private final ILookupService lookupService;

  public CompanyDefinitionListResponse findCompaniesByCompanyGroupId(String companyGroupId) {
    return CompanyDefinitionListResponse.builder()
        .companies(
            companyService.findByCompanyGroupId(companyGroupId).stream()
                .map(entity -> CompanyGroupMapper.toResponse(entity, lookupService))
                .toList())
        .build();
  }

  public CompanyDefinitionResponse findCompanyByCompanyIdAndCompanyGroupId(
      String companyGroupId, String companyId) {
    CompanyEntity entity = companyService.findByCompanyGroupIdAndId(companyGroupId, companyId);
    return CompanyGroupMapper.toResponse(entity, lookupService);
  }

  public CompanyDefinitionResponse saveCompany(String companyGroupId, CompanySaveRequest request) {
    CompanyGroupEntity companyGroup = companyGroupService.findByIdOrThrow(companyGroupId);
    EmailDomainValidator.validateAllowedDomains(
        split(companyGroup.getMailDomains()), request.companyMail());

    CompanyEntity entityToBeSave = CompanyGroupMapper.toEntity(request, companyGroupId);

    CompanyEntity savedCompany = companyService.save(entityToBeSave);
    return CompanyGroupMapper.toResponse(savedCompany, lookupService);
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
    return CompanyGroupMapper.toResponse(savedCompany, lookupService);
  }

  public CompanyDefinitionResponse toggleCompanyStatus(String companyGroupId, String companyId) {
    CompanyEntity companyEntity = companyService.toggleCompanyStatus(companyGroupId, companyId);
    return CompanyGroupMapper.toResponse(companyEntity, lookupService);
  }

  public CompanyProductListResponse findCompanyProducts(String companyGroupId, String companyId) {
    companyService.findByCompanyGroupIdAndId(companyGroupId, companyId);
    return CompanyProductListResponse.builder()
        .products(
            companyProductService.findAllByCompanyId(companyId).stream()
                .map(CompanyGroupMapper::toResponse)
                .toList())
        .build();
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
              throw new NotFoundException(
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
                  return new NotFoundException(
                      ErrorCode.COMPANY_NOT_FOUND, "Company not found: " + companyCode);
                });

    return CompanyContextResponse.builder()
        .companyGroup(CompanyGroupMapper.toResponse(group))
        .company(CompanyGroupMapper.toResponse(company, lookupService))
        .build();
  }

  public @Nullable CompanyProductResponse toggleCompanyProduct(
      String companyGroupId, String companyId, ApplicationProduct product) {
    CompanyProductEntity companyProductEntity =
        companyProductService.findByCompanyIdAndProductOrThrow(companyId, product);
    companyProductEntity.setEnabled(!companyProductEntity.isEnabled());
    return CompanyGroupMapper.toResponse(companyProductService.save(companyProductEntity));
  }
}
