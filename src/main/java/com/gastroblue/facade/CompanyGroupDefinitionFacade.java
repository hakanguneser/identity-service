package com.gastroblue.facade;

import static com.gastroblue.util.DelimitedStringUtil.join;

import com.gastroblue.mapper.CompanyGroupMapper;
import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.entity.CompanyEntity;
import com.gastroblue.model.enums.*;
import com.gastroblue.model.request.CompanyGroupSaveRequest;
import com.gastroblue.model.request.CompanyGroupUpdateRequest;
import com.gastroblue.model.request.CompanySaveRequest;
import com.gastroblue.model.request.CompanyUpdateRequest;
import com.gastroblue.model.response.CompanyDefinitionResponse;
import com.gastroblue.model.response.CompanyGroupDefinitionResponse;
import com.gastroblue.model.shared.ResolvedEnum;
import com.gastroblue.service.IJwtService;
import com.gastroblue.service.impl.CompanyGroupService;
import com.gastroblue.service.impl.CompanyService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyGroupDefinitionFacade {

  private final CompanyService companyService;
  private final CompanyGroupService companyGroupService;
  private final EnumConfigurationFacade enunConfigurationFacade;

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
    return CompanyGroupMapper.toResponse(companyGroupService.findByIdOrThrow(companyGroupId));
  }

  public List<CompanyDefinitionResponse> findCompaniesByCompanyGroupId(String companyGroupId) {
    return companyService.findByCompanyGroupId(companyGroupId).stream()
        .map(entity -> CompanyGroupMapper.toResponse(entity, enunConfigurationFacade))
        .toList();
  }

  public CompanyDefinitionResponse findCompanyByCompanyIdAndCompanyGroupId(
      String companyGroupId, String companyId) {
    CompanyEntity byId = companyService.findByCompanyGroupIdAndId(companyGroupId, companyId);
    return CompanyGroupMapper.toResponse(byId, enunConfigurationFacade);
  }

  public CompanyDefinitionResponse saveCompany(
      String companyGroupId, CompanySaveRequest companyRequest) {
    companyGroupService.findByIdOrThrow(companyGroupId);
    CompanyEntity entityToBeSave = CompanyGroupMapper.toEntity(companyRequest, companyGroupId);
    CompanyEntity savedCompany = companyService.save(entityToBeSave);
    return CompanyGroupMapper.toResponse(savedCompany, enunConfigurationFacade);
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
    return CompanyGroupMapper.toResponse(savedCompany, enunConfigurationFacade);
  }

  public CompanyDefinitionResponse findByCompanyId(String companyId) {
    CompanyEntity company = companyService.findOrThrow(companyId);
    return CompanyGroupMapper.toResponse(company, enunConfigurationFacade);
  }

  public List<CompanyDefinitionResponse> findByCompanyGroupId(String companyGroupId) {
    return companyService.findByCompanyGroupId(companyGroupId).stream()
        .map(entity -> CompanyGroupMapper.toResponse(entity, enunConfigurationFacade))
        .toList();
  }

  public List<CompanyDefinitionResponse> findByCompanyGroupAndZone(
      String companyGroupId, Zone zone) {
    return companyService.findByCompanyGroupIdAndZone(companyGroupId, zone).stream()
        .map(entity -> CompanyGroupMapper.toResponse(entity, enunConfigurationFacade))
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
    return CompanyGroupMapper.toResponse(companyEntity, enunConfigurationFacade);
  }

  public List<ResolvedEnum<Zone>> findZones(String companyGroupId) {
    return enunConfigurationFacade.getDropdownValues(Zone.class, companyGroupId);
  }

  public List<ResolvedEnum<Country>> findCountries(String companyGroupId) {
    return enunConfigurationFacade.getDropdownValues(Country.class, companyGroupId);
  }

  public List<ResolvedEnum<City>> findCities(String companyGroupId, final Country country) {
    List<ResolvedEnum<City>> allCities =
        enunConfigurationFacade.getDropdownValues(City.class, companyGroupId);
    return allCities.stream().filter(resolved -> resolved.getKey().country() == country).toList();
  }

  public List<ResolvedEnum<CompanySegment1Values>> findSegment1(String companyGroupId) {
    return enunConfigurationFacade.getDropdownValues(CompanySegment1Values.class, companyGroupId);
  }

  public List<ResolvedEnum<CompanySegment2Values>> findSegment2(String companyGroupId) {
    return enunConfigurationFacade.getDropdownValues(CompanySegment2Values.class, companyGroupId);
  }

  public List<ResolvedEnum<CompanySegment3Values>> findSegment3(String companyGroupId) {
    return enunConfigurationFacade.getDropdownValues(CompanySegment3Values.class, companyGroupId);
  }

  public List<ResolvedEnum<CompanySegment4Values>> findSegment4(String companyGroupId) {
    return enunConfigurationFacade.getDropdownValues(CompanySegment4Values.class, companyGroupId);
  }

  public List<ResolvedEnum<CompanySegment5Values>> findSegment5(String companyGroupId) {
    return enunConfigurationFacade.getDropdownValues(CompanySegment5Values.class, companyGroupId);
  }
}
