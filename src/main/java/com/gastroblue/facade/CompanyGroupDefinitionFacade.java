package com.gastroblue.facade;

import static com.gastroblue.util.DelimitedStringUtil.join;
import static com.gastroblue.util.DelimitedStringUtil.split;

import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.mapper.CompanyGroupMapper;
import com.gastroblue.model.entity.CompanyEntity;
import com.gastroblue.model.entity.CompanyGroupEntity;
import com.gastroblue.model.entity.CompanyGroupProductEntity;
import com.gastroblue.model.entity.CompanyProductEntity;
import com.gastroblue.model.enums.*;
import com.gastroblue.model.request.CompanyGroupProductSaveRequest;
import com.gastroblue.model.request.CompanyGroupProductUpdateRequest;
import com.gastroblue.model.request.CompanyGroupSaveRequest;
import com.gastroblue.model.request.CompanyGroupUpdateRequest;
import com.gastroblue.model.request.CompanyProductSaveRequest;
import com.gastroblue.model.request.CompanyProductUpdateRequest;
import com.gastroblue.model.request.CompanySaveRequest;
import com.gastroblue.model.request.CompanyUpdateRequest;
import com.gastroblue.model.response.CompanyContextResponse;
import com.gastroblue.model.response.CompanyDefinitionResponse;
import com.gastroblue.model.response.CompanyGroupDefinitionResponse;
import com.gastroblue.model.response.CompanyGroupProductResponse;
import com.gastroblue.model.response.CompanyProductResponse;
import com.gastroblue.model.shared.ResolvedEnum;
import com.gastroblue.service.impl.CompanyGroupProductService;
import com.gastroblue.service.impl.CompanyGroupService;
import com.gastroblue.service.impl.CompanyProductService;
import com.gastroblue.service.impl.CompanyService;
import com.gastroblue.util.EmailDomainValidator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyGroupDefinitionFacade {

  private final CompanyService companyService;
  private final CompanyGroupService companyGroupService;
  private final CompanyGroupProductService companyGroupProductService;
  private final CompanyProductService companyProductService;
  private final EnumConfigurationFacade enumConfigurationFacade;

  public CompanyGroupDefinitionResponse saveCompanyGroup(CompanyGroupSaveRequest request) {
    EmailDomainValidator.validateAllowedDomains(
        request.mailDomains().stream().toList(), request.groupMails().stream().toList());
    CompanyGroupEntity savedEntity = companyGroupService.save(request);
    return CompanyGroupMapper.toResponse(savedEntity);
  }

  public CompanyGroupDefinitionResponse updateCompanyGroup(
      String companyGroupId, CompanyGroupUpdateRequest request) {

    EmailDomainValidator.validateAllowedDomains(request.mailDomains(), request.groupMails());
    return CompanyGroupMapper.toResponse(companyGroupService.update(companyGroupId, request));
  }

  public List<CompanyGroupDefinitionResponse> findAllCompanyGroups() {
    return companyGroupService.findAll().stream().map(CompanyGroupMapper::toResponse).toList();
  }

  public CompanyGroupDefinitionResponse findCompanyGroupById(String companyGroupId) {
    return CompanyGroupMapper.toResponse(companyGroupService.findByIdOrThrow(companyGroupId));
  }

  public List<CompanyDefinitionResponse> findCompaniesByCompanyGroupId(String companyGroupId) {
    return companyService.findByCompanyGroupId(companyGroupId).stream()
        .map(entity -> CompanyGroupMapper.toResponse(entity, enumConfigurationFacade))
        .toList();
  }

  public CompanyDefinitionResponse findCompanyByCompanyIdAndCompanyGroupId(
      String companyGroupId, String companyId) {
    CompanyEntity byId = companyService.findByCompanyGroupIdAndId(companyGroupId, companyId);
    return CompanyGroupMapper.toResponse(byId, enumConfigurationFacade);
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

  public List<ResolvedEnum> findZones(final String companyGroupId) {
    return enumConfigurationFacade.getDropdownValues(Zone.class, companyGroupId);
  }

  public List<ResolvedEnum> findCountries(final String companyGroupId) {
    return enumConfigurationFacade.getDropdownValues(Country.class, companyGroupId);
  }

  public List<ResolvedEnum> findCities(final String companyGroupId, final Country country) {
    List<ResolvedEnum> allCities =
        enumConfigurationFacade.getDropdownValues(City.class, companyGroupId);
    return allCities.stream()
        .filter(resolved -> Objects.equals(resolved.getKey(), country.name()))
        .toList();
  }

  public List<ResolvedEnum> findSegment1(final String companyGroupId) {
    return enumConfigurationFacade.getDropdownValues(CompanySegment1Values.class, companyGroupId);
  }

  public List<ResolvedEnum> findSegment2(final String companyGroupId) {
    return enumConfigurationFacade.getDropdownValues(CompanySegment2Values.class, companyGroupId);
  }

  public List<ResolvedEnum> findSegment3(final String companyGroupId) {
    return enumConfigurationFacade.getDropdownValues(CompanySegment3Values.class, companyGroupId);
  }

  public List<ResolvedEnum> findSegment4(final String companyGroupId) {
    return enumConfigurationFacade.getDropdownValues(CompanySegment4Values.class, companyGroupId);
  }

  public List<ResolvedEnum> findSegment5(final String companyGroupId) {
    return enumConfigurationFacade.getDropdownValues(CompanySegment5Values.class, companyGroupId);
  }

  public CompanyGroupProductResponse saveCompanyGroupProduct(
      String companyGroupId, CompanyGroupProductSaveRequest request) {

    companyGroupService.findByIdOrThrow(companyGroupId);

    companyGroupProductService
        .findByCompanyGroupIdAndProduct(companyGroupId, request.product())
        .ifPresent(
            existing -> {
              throw new IllegalDefinitionException(
                  ErrorCode.COMPANY_GROUP_PRODUCT_ALREADY_EXISTS,
                  "Product already assigned to company group: " + request.product());
            });

    CompanyGroupProductEntity entity =
        CompanyGroupProductEntity.builder()
            .companyGroupId(companyGroupId)
            .product(request.product())
            .enabled(request.enabled())
            .apiUrl(request.apiUrl())
            .apiVersion(request.apiVersion())
            .notes(request.notes())
            .build();

    return CompanyGroupMapper.toResponse(companyGroupProductService.save(entity));
  }

  public CompanyGroupProductResponse updateCompanyGroupProduct(
      String companyGroupId, ApplicationProduct product, CompanyGroupProductUpdateRequest request) {

    companyGroupService.findByIdOrThrow(companyGroupId);

    CompanyGroupProductEntity existing =
        companyGroupProductService.findByCompanyGroupIdAndProductOrThrow(companyGroupId, product);

    CompanyGroupProductEntity updated =
        CompanyGroupProductEntity.builder()
            .enabled(request.enabled())
            .apiUrl(request.apiUrl())
            .apiVersion(request.apiVersion())
            .notes(request.notes())
            .build();

    return CompanyGroupMapper.toResponse(
        companyGroupProductService.update(existing.getId(), updated));
  }

  public List<CompanyGroupProductResponse> findCompanyGroupProducts(String companyGroupId) {
    companyGroupService.findByIdOrThrow(companyGroupId);
    return companyGroupProductService.findAllByCompanyGroupId(companyGroupId).stream()
        .map(CompanyGroupMapper::toResponse)
        .toList();
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
