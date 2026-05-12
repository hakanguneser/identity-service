package com.gastroblue.facade;

import com.gastroblue.commons.helper.exception.type.NotFoundException;
import com.gastroblue.commons.helper.lookup.model.dto.LookupQuery;
import com.gastroblue.commons.helper.lookup.service.ILookupService;
import com.gastroblue.commons.shared.enums.ApplicationProduct;
import com.gastroblue.mapper.CompanyGroupMapper;
import com.gastroblue.model.entity.CompanyGroupEntity;
import com.gastroblue.model.entity.CompanyGroupProductEntity;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.enums.Lookups;
import com.gastroblue.model.request.CompanyGroupProductSaveRequest;
import com.gastroblue.model.request.CompanyGroupProductUpdateRequest;
import com.gastroblue.model.request.CompanyGroupSaveRequest;
import com.gastroblue.model.request.CompanyGroupUpdateRequest;
import com.gastroblue.model.response.CompanyGroupDefinitionListResponse;
import com.gastroblue.model.response.CompanyGroupDefinitionResponse;
import com.gastroblue.model.response.CompanyGroupProductListResponse;
import com.gastroblue.model.response.CompanyGroupProductResponse;
import com.gastroblue.model.response.DropdownResponse;
import com.gastroblue.service.CompanyGroupProductService;
import com.gastroblue.service.CompanyGroupService;
import com.gastroblue.util.EmailDomainValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyGroupDefinitionFacade {

  private final CompanyGroupService companyGroupService;
  private final CompanyGroupProductService companyGroupProductService;
  private final ILookupService lookupService;

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

  public CompanyGroupDefinitionListResponse findAllCompanyGroups() {
    return CompanyGroupDefinitionListResponse.builder()
        .companyGroups(
            companyGroupService.findAll().stream().map(CompanyGroupMapper::toResponse).toList())
        .build();
  }

  public CompanyGroupDefinitionResponse findCompanyGroupById(String companyGroupId) {
    return CompanyGroupMapper.toResponse(companyGroupService.findByIdOrThrow(companyGroupId));
  }

  public CompanyGroupProductListResponse findCompanyGroupProducts(String companyGroupId) {
    companyGroupService.findByIdOrThrow(companyGroupId);
    return CompanyGroupProductListResponse.builder()
        .products(
            companyGroupProductService.findAllByCompanyGroupId(companyGroupId).stream()
                .map(CompanyGroupMapper::toResponse)
                .toList())
        .build();
  }

  public CompanyGroupProductResponse saveCompanyGroupProduct(
      String companyGroupId, CompanyGroupProductSaveRequest request) {
    companyGroupService.findByIdOrThrow(companyGroupId);
    companyGroupProductService
        .findByCompanyGroupIdAndProduct(companyGroupId, request.product())
        .ifPresent(
            existing -> {
              throw new NotFoundException(
                  ErrorCode.COMPANY_GROUP_PRODUCT_ALREADY_EXISTS,
                  "Product already assigned to company group: " + request.product());
            });
    CompanyGroupProductEntity entity =
        CompanyGroupProductEntity.builder()
            .companyGroupId(companyGroupId)
            .product(request.product())
            .enabled(true)
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

  public DropdownResponse findZones(final String companyGroupId) {
    return DropdownResponse.builder()
        .items(
            lookupService.findAll(
                LookupQuery.of().lookup(Lookups.ZONE).companyGroupId(companyGroupId)))
        .build();
  }

  public DropdownResponse findCountries(final String companyGroupId) {
    return DropdownResponse.builder()
        .items(
            lookupService.findAll(
                LookupQuery.of().lookup(Lookups.COUNTRY).companyGroupId(companyGroupId)))
        .build();
  }

  public DropdownResponse findCities(final String companyGroupId, final String country) {
    return DropdownResponse.builder()
        .items(
            lookupService.findAll(
                LookupQuery.of().lookup(Lookups.CITY).companyGroupId(companyGroupId)))
        .build();
  }

  public DropdownResponse findSegment1(final String companyGroupId) {
    return DropdownResponse.builder()
        .items(
            lookupService.findAll(
                LookupQuery.of().lookup(Lookups.SEGMENT_1).companyGroupId(companyGroupId)))
        .build();
  }

  public DropdownResponse findSegment2(final String companyGroupId) {
    return DropdownResponse.builder()
        .items(
            lookupService.findAll(
                LookupQuery.of().lookup(Lookups.SEGMENT_2).companyGroupId(companyGroupId)))
        .build();
  }

  public DropdownResponse findSegment3(final String companyGroupId) {
    return DropdownResponse.builder()
        .items(
            lookupService.findAll(
                LookupQuery.of().lookup(Lookups.SEGMENT_3).companyGroupId(companyGroupId)))
        .build();
  }

  public DropdownResponse findSegment4(final String companyGroupId) {
    return DropdownResponse.builder()
        .items(
            lookupService.findAll(
                LookupQuery.of().lookup(Lookups.SEGMENT_4).companyGroupId(companyGroupId)))
        .build();
  }

  public DropdownResponse findSegment5(final String companyGroupId) {
    return DropdownResponse.builder()
        .items(
            lookupService.findAll(
                LookupQuery.of().lookup(Lookups.SEGMENT_5).companyGroupId(companyGroupId)))
        .build();
  }
}
