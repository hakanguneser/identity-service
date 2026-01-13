package com.gastroblue.controller;

import com.gastroblue.facade.CompanyGroupDefinitionFacade;
import com.gastroblue.model.enums.Country;
import com.gastroblue.model.request.*;
import com.gastroblue.model.response.BatchCompanyGroupDefinitionResponse;
import com.gastroblue.model.response.CompanyDefinitionResponse;
import com.gastroblue.model.response.CompanyGroupDefinitionResponse;
import com.gastroblue.model.shared.EnumDisplay;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(value = "v1/definition/company-groups")
@RequiredArgsConstructor
public class CompanyGroupDefinitionController {

  private final CompanyGroupDefinitionFacade companyFacade;

  @GetMapping
  public ResponseEntity<List<CompanyGroupDefinitionResponse>> findAllCompanyGroups() {
    return ResponseEntity.ok(companyFacade.findAllCompanyGroups());
  }

  @PostMapping("/batch")
  public ResponseEntity<BatchCompanyGroupDefinitionResponse> saveCompanyGroupsBatch(
      @Valid @RequestBody final BatchCompanyGroupSaveRequest request) {
    BatchCompanyGroupDefinitionResponse response = companyFacade.saveCompanyGroupsBatch(request);
    return ResponseEntity.ok(response);
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

  @GetMapping("/dropdown/zones")
  public ResponseEntity<List<EnumDisplay>> findZones() {
    return ResponseEntity.ok(companyFacade.findZones());
  }

  @GetMapping("/dropdown/countries")
  public ResponseEntity<List<EnumDisplay>> findCountries() {
    return ResponseEntity.ok(companyFacade.findCountries());
  }

  @GetMapping("/dropdown/country/{country}/cities")
  public ResponseEntity<List<EnumDisplay>> findCities(
      @PathVariable(name = "country") final Country country) {
    return ResponseEntity.ok(companyFacade.findCities(country));
  }

  @GetMapping("/dropdown/segment1")
  public ResponseEntity<List<EnumDisplay>> findSegment1() {
    return ResponseEntity.ok(companyFacade.findSegment1());
  }

  @GetMapping("/dropdown/segment2")
  public ResponseEntity<List<EnumDisplay>> findSegment2() {
    return ResponseEntity.ok(companyFacade.findSegment2());
  }

  @GetMapping("/dropdown/segment3")
  public ResponseEntity<List<EnumDisplay>> findSegment3() {
    return ResponseEntity.ok(companyFacade.findSegment3());
  }

  @GetMapping("/dropdown/segment4")
  public ResponseEntity<List<EnumDisplay>> findSegment4() {
    return ResponseEntity.ok(companyFacade.findSegment4());
  }

  @GetMapping("/dropdown/segment5")
  public ResponseEntity<List<EnumDisplay>> findSegment5() {
    return ResponseEntity.ok(companyFacade.findSegment5());
  }
}
