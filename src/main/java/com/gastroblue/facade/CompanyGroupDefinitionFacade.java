package com.gastroblue.facade;

import static com.gastroblue.model.enums.ErrorCode.COMPANY_GROUP_BULK_INSERT_EXCEPTION;
import static com.gastroblue.util.DelimitedStringUtil.join;

import com.gastroblue.exception.ValidationException;
import com.gastroblue.mapper.CompanyGroupMapper;
import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.entity.CompanyEntity;
import com.gastroblue.model.enums.*;
import com.gastroblue.model.request.*;
import com.gastroblue.model.response.BatchCompanyGroupDefinitionResponse;
import com.gastroblue.model.response.CompanyDefinitionResponse;
import com.gastroblue.model.response.CompanyGroupDefinitionResponse;
import com.gastroblue.service.IJwtService;
import com.gastroblue.service.impl.ApplicationPropertyService;
import com.gastroblue.service.impl.CompanyGroupService;
import com.gastroblue.service.impl.CompanyService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyGroupDefinitionFacade {

  private final CompanyService companyService;
  private final CompanyGroupService companyGroupService;
  private final ApplicationPropertyService appPropertyService;

  public CompanyGroupDefinitionResponse saveCompanyGroup(CompanyGroupSaveRequest request) {
    return CompanyGroupMapper.toResponse(companyGroupService.save(request));
  }

  public CompanyGroupDefinitionResponse updateCompanyGroup(
      String companyGroupId, CompanyGroupUpdateRequest request) {
    return CompanyGroupMapper.toResponse(companyGroupService.update(companyGroupId, request));
  }

  public List<CompanyGroupDefinitionResponse> findAllCompanyGroups() {
    return companyGroupService.findAll().stream().map(CompanyGroupMapper::toResponse).toList();
  }

  public CompanyGroupDefinitionResponse findCompanyGroupById(String companyGroupId) {
    return CompanyGroupMapper.toResponse(companyGroupService.findById(companyGroupId));
  }

  public List<CompanyDefinitionResponse> findCompaniesByCompanyGroupId(String companyGroupId) {
    return companyService.findByCompanyGroupId(companyGroupId).stream()
        .map(CompanyGroupMapper::toResponse)
        .toList();
  }

  public CompanyDefinitionResponse findCompanyByCompanyIdAndCompanyGroupId(
      String companyGroupId, String companyId) {
    CompanyEntity byId = companyService.findByCompanyGroupIdAndId(companyGroupId, companyId);
    return CompanyGroupMapper.toResponse(byId);
  }

  public CompanyDefinitionResponse saveCompany(
      String companyGroupId, CompanySaveRequest companyRequest) {
    companyGroupService.findById(companyGroupId);
    CompanyEntity entityToBeSave = CompanyGroupMapper.toEntity(companyRequest, companyGroupId);
    CompanyEntity savedCompany = companyService.save(entityToBeSave);
    return CompanyGroupMapper.toResponse(savedCompany);
  }

  public CompanyDefinitionResponse updateCompany(
      String companyGroupId, String companyId, CompanyUpdateRequest companyRequest) {
    CompanyEntity entityToBeUpdated =
        companyService.findByCompanyGroupIdAndId(companyGroupId, companyId);
    entityToBeUpdated.setCompanyName(companyRequest.companyName());
    entityToBeUpdated.setCompanyCode(companyRequest.companyCode());
    entityToBeUpdated.setCompanyGroupId(companyGroupId);
    entityToBeUpdated.setCompanyMail(join(companyRequest.companyMail()));
    entityToBeUpdated.setCountry(companyRequest.country());
    entityToBeUpdated.setCity(companyRequest.city());
    entityToBeUpdated.setZone(companyRequest.zone());
    entityToBeUpdated.setSegment1(companyRequest.segment1());
    entityToBeUpdated.setSegment2(companyRequest.segment2());
    entityToBeUpdated.setSegment3(companyRequest.segment3());
    entityToBeUpdated.setSegment4(companyRequest.segment4());
    entityToBeUpdated.setSegment5(companyRequest.segment5());
    CompanyEntity savedCompany = companyService.save(entityToBeUpdated);
    return CompanyGroupMapper.toResponse(savedCompany);
  }

  public CompanyDefinitionResponse findByCompanyId(String companyId) {
    CompanyEntity company = companyService.findOrThrow(companyId);
    return CompanyGroupMapper.toResponse(company);
  }

  public List<CompanyDefinitionResponse> findByCompanyGroupId(String companyGroupId) {
    return companyService.findByCompanyGroupId(companyGroupId).stream()
        .map(CompanyGroupMapper::toResponse)
        .toList();
  }

  public List<CompanyDefinitionResponse> findByCompanyGroupAndZone(
      String companyGroupId, Zone zone) {
    return companyService.findByCompanyGroupIdAndZone(companyGroupId, zone).stream()
        .map(CompanyGroupMapper::toResponse)
        .toList();
  }

  public List<CompanyDefinitionResponse> findMyCompanies(String companyGroupId) {
    SessionUser user = IJwtService.findSessionUserOrThrow();

    return switch (user.applicationRole()) {
      case ADMIN -> companyGroupId == null ? null : findByCompanyGroupId(companyGroupId);
      case GROUP_MANAGER -> findByCompanyGroupId(user.companyGroupId());
      case ZONE_MANAGER -> findByCompanyGroupAndZone(user.companyGroupId(), user.zone());
      case COMPANY_MANAGER, SUPERVISOR -> List.of(findByCompanyId(user.companyId()));
      default -> null;
    };
  }

  public List<CompanyGroupDefinitionResponse> findMyCompanyGroups() {
    return companyGroupService.findMyCompanyGroups().stream()
        .map(CompanyGroupMapper::toResponse)
        .toList();
  }

  public CompanyDefinitionResponse toggleCompanyStatus(String companyGroupId, String companyId) {
    CompanyEntity companyEntity = companyService.toggleCompanyStatus(companyGroupId, companyId);
    return CompanyGroupMapper.toResponse(companyEntity);
  }

  @Transactional
  public BatchCompanyGroupDefinitionResponse saveCompanyGroupsBatch(
      BatchCompanyGroupSaveRequest request) {

    try {
      CompanyGroupDefinitionResponse createdGroup = saveCompanyGroup(request.companyGroup());
      List<CompanyDefinitionResponse> companies =
          saveCompaniesBatch(createdGroup.getCompanyGroupId(), request.items());
      return new BatchCompanyGroupDefinitionResponse(createdGroup, companies);
    } catch (Exception ex) {
      log.error("Company group bulk save failed: {}", ex.getMessage(), ex);
      throw new ValidationException(COMPANY_GROUP_BULK_INSERT_EXCEPTION);
    }
  }

  private List<CompanyDefinitionResponse> saveCompaniesBatch(
      final String companyGroupId, final List<CompanySaveRequest> items) {

    List<CompanyDefinitionResponse> successes = new ArrayList<>(items.size());
    for (CompanySaveRequest req : items) {
      CompanyDefinitionResponse created = saveCompany(companyGroupId, req);
      successes.add(created);
    }
    return successes;
  }

  public List<Zone> findZones() {
    return Arrays.asList(Zone.values());
  }

  public List<Country> findCountries() {
    return Arrays.asList(Country.values());
  }

  public List<City> findCities(final Country country) {
    // TODO: add country to request
    return Arrays.asList(City.values());
  }

  public List<CompanySegment1Values> findSegment1() {
    return Arrays.asList(CompanySegment1Values.values());
  }

  public List<CompanySegment2Values> findSegment2() {
    return Arrays.asList(CompanySegment2Values.values());
  }

  public List<CompanySegment3Values> findSegment3() {
    return Arrays.asList(CompanySegment3Values.values());
  }

  public List<CompanySegment4Values> findSegment4() {
    return Arrays.asList(CompanySegment4Values.values());
  }

  public List<CompanySegment5Values> findSegment5() {
    return Arrays.asList(CompanySegment5Values.values());
  }
}
