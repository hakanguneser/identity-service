package com.gastroblue.facade;

import com.gastroblue.commons.helper.exception.type.NotFoundException;
import com.gastroblue.commons.helper.lookup.model.dto.BaseLookupModel;
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
import com.gastroblue.model.response.CompanyGroupDefinitionResponse;
import com.gastroblue.model.response.CompanyGroupProductResponse;
import com.gastroblue.service.impl.CompanyGroupProductService;
import com.gastroblue.service.impl.CompanyGroupService;
import com.gastroblue.util.EmailDomainValidator;
import java.util.List;
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

  public List<BaseLookupModel> findZones(final String companyGroupId) {
    return lookupService.findAll(
        LookupQuery.of().lookup(Lookups.ZONE).companyGroupId(companyGroupId));
  }

  public List<BaseLookupModel> findCountries(final String companyGroupId) {
    return lookupService.findAll(
        LookupQuery.of().lookup(Lookups.COUNTRY).companyGroupId(companyGroupId));
  }

  public List<BaseLookupModel> findCities(final String companyGroupId, final String country) {
    return lookupService.findAll(
        LookupQuery.of().lookup(Lookups.CITY).companyGroupId(companyGroupId));
  }

  public List<BaseLookupModel> findSegment1(final String companyGroupId) {
    return lookupService.findAll(
        LookupQuery.of().lookup(Lookups.SEGMENT_1).companyGroupId(companyGroupId));
  }

  public List<BaseLookupModel> findSegment2(final String companyGroupId) {
    return lookupService.findAll(
        LookupQuery.of().lookup(Lookups.SEGMENT_2).companyGroupId(companyGroupId));
  }

  public List<BaseLookupModel> findSegment3(final String companyGroupId) {
    return lookupService.findAll(
        LookupQuery.of().lookup(Lookups.SEGMENT_3).companyGroupId(companyGroupId));
  }

  public List<BaseLookupModel> findSegment4(final String companyGroupId) {
    return lookupService.findAll(
        LookupQuery.of().lookup(Lookups.SEGMENT_4).companyGroupId(companyGroupId));
  }

  public List<BaseLookupModel> findSegment5(final String companyGroupId) {
    return lookupService.findAll(
        LookupQuery.of().lookup(Lookups.SEGMENT_5).companyGroupId(companyGroupId));
  }
}
