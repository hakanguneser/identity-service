package com.gastroblue.controller;

import com.gastroblue.facade.CompanyGroupDefinitionFacade;
import com.gastroblue.model.enums.*;
import com.gastroblue.model.enums.Country;
import com.gastroblue.model.request.*;
import com.gastroblue.model.response.CompanyContextResponse;
import com.gastroblue.model.response.CompanyDefinitionResponse;
import com.gastroblue.model.response.CompanyGroupDefinitionResponse;
import com.gastroblue.model.shared.ResolvedEnum;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(value = "api/v1/definition/company-groups")
@RequiredArgsConstructor
public class CompanyGroupDefinitionController {

  private final CompanyGroupDefinitionFacade companyFacade;

  @GetMapping
  public ResponseEntity<List<CompanyGroupDefinitionResponse>> findAllCompanyGroups() {
    return ResponseEntity.ok(companyFacade.findAllCompanyGroups());
  }

  @PostMapping
  public ResponseEntity<CompanyGroupDefinitionResponse> saveCompanyGroup(
      @Valid @RequestBody final CompanyGroupSaveRequest request) {
    var created = companyFacade.saveCompanyGroup(request);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.getCompanyGroupId())
            .toUri();
    return ResponseEntity.created(location).body(created);
  }

  @GetMapping("/{companyGroupId}")
  public ResponseEntity<CompanyGroupDefinitionResponse> findCompanyGroupById(
      @PathVariable(name = "companyGroupId") final String companyGroupId) {
    return ResponseEntity.ok(companyFacade.findCompanyGroupById(companyGroupId));
  }

  @PutMapping("/{companyGroupId}")
  public ResponseEntity<CompanyGroupDefinitionResponse> updateCompanyGroup(
      @PathVariable(name = "companyGroupId") final String companyGroupId,
      @Valid @RequestBody final CompanyGroupUpdateRequest request) {
    return ResponseEntity.ok(companyFacade.updateCompanyGroup(companyGroupId, request));
  }

  @GetMapping("/{companyGroupId}/companies")
  public ResponseEntity<List<CompanyDefinitionResponse>> findCompaniesByCompanyGroupId(
      @PathVariable(name = "companyGroupId") final String companyGroupId) {
    return ResponseEntity.ok(companyFacade.findCompaniesByCompanyGroupId(companyGroupId));
  }

  @PostMapping("/{companyGroupId}/companies")
  public ResponseEntity<CompanyDefinitionResponse> saveCompany(
      @PathVariable(name = "companyGroupId") final String companyGroupId,
      @Valid @RequestBody final CompanySaveRequest request) {
    var created = companyFacade.saveCompany(companyGroupId, request);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{companyId}")
            .buildAndExpand(created.getCompanyId())
            .toUri();
    return ResponseEntity.created(location).body(created);
  }

  @PutMapping("/{companyGroupId}/companies/{companyId}")
  public ResponseEntity<CompanyDefinitionResponse> updateCompany(
      @PathVariable(name = "companyGroupId") final String companyGroupId,
      @PathVariable(name = "companyId") final String companyId,
      @Valid @RequestBody final CompanyUpdateRequest request) {
    return ResponseEntity.ok(companyFacade.updateCompany(companyGroupId, companyId, request));
  }

  @GetMapping("/{companyGroupId}/companies/{companyId}")
  public ResponseEntity<CompanyDefinitionResponse> findCompanyByCompanyIdAndCompanyGroupId(
      @PathVariable(name = "companyGroupId") final String companyGroupId,
      @PathVariable(name = "companyId") final String companyId) {
    return ResponseEntity.ok(
        companyFacade.findCompanyByCompanyIdAndCompanyGroupId(companyGroupId, companyId));
  }

  @PatchMapping("/{companyGroupId}/companies/{companyId}/status")
  public ResponseEntity<CompanyDefinitionResponse> toggleCompanyStatus(
      @PathVariable(name = "companyGroupId") final String companyGroupId,
      @PathVariable(name = "companyId") final String companyId) {
    return ResponseEntity.ok(companyFacade.toggleCompanyStatus(companyGroupId, companyId));
  }

  @GetMapping("/{companyGroupId}/dropdown/zones")
  public ResponseEntity<List<ResolvedEnum<Zone>>> findZones(
      @PathVariable(name = "companyGroupId") final String companyGroupId) {
    return ResponseEntity.ok(companyFacade.findZones(companyGroupId));
  }

  @GetMapping("/{companyGroupId}/dropdown/countries")
  public ResponseEntity<List<ResolvedEnum<Country>>> findCountries(
      @PathVariable(name = "companyGroupId") final String companyGroupId) {
    return ResponseEntity.ok(companyFacade.findCountries(companyGroupId));
  }

  @GetMapping("/{companyGroupId}/dropdown/country/{country}/cities")
  public ResponseEntity<List<ResolvedEnum<City>>> findCities(
      @PathVariable(name = "companyGroupId") final String companyGroupId,
      @PathVariable(name = "country") final Country country) {
    return ResponseEntity.ok(companyFacade.findCities(companyGroupId, country));
  }

  @GetMapping("/{companyGroupId}/dropdown/segment1")
  public ResponseEntity<List<ResolvedEnum<CompanySegment1Values>>> findSegment1(
      @PathVariable(name = "companyGroupId") final String companyGroupId) {
    return ResponseEntity.ok(companyFacade.findSegment1(companyGroupId));
  }

  @GetMapping("/{companyGroupId}/dropdown/segment2")
  public ResponseEntity<List<ResolvedEnum<CompanySegment2Values>>> findSegment2(
      @PathVariable(name = "companyGroupId") final String companyGroupId) {
    return ResponseEntity.ok(companyFacade.findSegment2(companyGroupId));
  }

  @GetMapping("/{companyGroupId}/dropdown/segment3")
  public ResponseEntity<List<ResolvedEnum<CompanySegment3Values>>> findSegment3(
      @PathVariable(name = "companyGroupId") final String companyGroupId) {
    return ResponseEntity.ok(companyFacade.findSegment3(companyGroupId));
  }

  @GetMapping("/{companyGroupId}/dropdown/segment4")
  public ResponseEntity<List<ResolvedEnum<CompanySegment4Values>>> findSegment4(
      @PathVariable(name = "companyGroupId") final String companyGroupId) {
    return ResponseEntity.ok(companyFacade.findSegment4(companyGroupId));
  }

  @GetMapping("/{companyGroupId}/dropdown/segment5")
  public ResponseEntity<List<ResolvedEnum<CompanySegment5Values>>> findSegment5(
      @PathVariable(name = "companyGroupId") final String companyGroupId) {
    return ResponseEntity.ok(companyFacade.findSegment5(companyGroupId));
  }
  @GetMapping("/context")
  public ResponseEntity<CompanyContextResponse> findCompanyAndGroupContext(
      @RequestParam(name = "companyGroupCode") final String companyGroupCode,
      @RequestParam(name = "companyCode") final String companyCode) {
    return ResponseEntity.ok(
        companyFacade.findCompanyAndGroupContext(companyGroupCode, companyCode));
  }
}
