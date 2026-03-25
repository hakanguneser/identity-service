package com.gastroblue.facade;

import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.mapper.CompanyGroupMapper;
import com.gastroblue.model.entity.CompanyGroupEntity;
import com.gastroblue.model.entity.CompanyGroupProductEntity;
import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.enums.City;
import com.gastroblue.model.enums.CompanySegment1Values;
import com.gastroblue.model.enums.CompanySegment2Values;
import com.gastroblue.model.enums.CompanySegment3Values;
import com.gastroblue.model.enums.CompanySegment4Values;
import com.gastroblue.model.enums.CompanySegment5Values;
import com.gastroblue.model.enums.Country;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.enums.Zone;
import com.gastroblue.model.request.CompanyGroupProductSaveRequest;
import com.gastroblue.model.request.CompanyGroupProductUpdateRequest;
import com.gastroblue.model.request.CompanyGroupSaveRequest;
import com.gastroblue.model.request.CompanyGroupUpdateRequest;
import com.gastroblue.model.response.CompanyGroupDefinitionResponse;
import com.gastroblue.model.response.CompanyGroupProductResponse;
import com.gastroblue.model.shared.ResolvedEnum;
import com.gastroblue.service.impl.CompanyGroupProductService;
import com.gastroblue.service.impl.CompanyGroupService;
import com.gastroblue.util.EmailDomainValidator;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyGroupDefinitionFacade {

  private final CompanyGroupService companyGroupService;
  private final CompanyGroupProductService companyGroupProductService;
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

  public List<CompanyGroupProductResponse> findCompanyGroupProducts(String companyGroupId) {
    companyGroupService.findByIdOrThrow(companyGroupId);
    return companyGroupProductService.findAllByCompanyGroupId(companyGroupId).stream()
        .map(CompanyGroupMapper::toResponse)
        .toList();
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

  public List<ResolvedEnum> findZones(final String companyGroupId) {
    return enumConfigurationFacade.getDropdownValues(Zone.class, companyGroupId);
  }

  public List<ResolvedEnum> findCountries(final String companyGroupId) {
    return enumConfigurationFacade.getDropdownValues(Country.class, companyGroupId);
  }

  public List<ResolvedEnum> findCities(final String companyGroupId, final Country country) {
    return enumConfigurationFacade.getDropdownValues(City.class, companyGroupId).stream()
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
}
