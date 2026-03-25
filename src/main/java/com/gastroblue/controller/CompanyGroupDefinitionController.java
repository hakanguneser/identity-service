package com.gastroblue.controller;

import com.gastroblue.facade.CompanyGroupDefinitionFacade;
import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.enums.Country;
import com.gastroblue.model.request.CompanyGroupProductSaveRequest;
import com.gastroblue.model.request.CompanyGroupProductUpdateRequest;
import com.gastroblue.model.request.CompanyGroupSaveRequest;
import com.gastroblue.model.request.CompanyGroupUpdateRequest;
import com.gastroblue.model.response.CompanyGroupDefinitionResponse;
import com.gastroblue.model.response.CompanyGroupProductResponse;
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

  private final CompanyGroupDefinitionFacade companyGroupDefinitionFacade;

  @GetMapping
  public ResponseEntity<List<CompanyGroupDefinitionResponse>> findAllCompanyGroups() {
    return ResponseEntity.ok(companyGroupDefinitionFacade.findAllCompanyGroups());
  }

  @PostMapping
  public ResponseEntity<CompanyGroupDefinitionResponse> saveCompanyGroup(
      @Valid @RequestBody final CompanyGroupSaveRequest request) {
    var created = companyGroupDefinitionFacade.saveCompanyGroup(request);
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
    return ResponseEntity.ok(companyGroupDefinitionFacade.findCompanyGroupById(companyGroupId));
  }

  @PutMapping("/{companyGroupId}")
  public ResponseEntity<CompanyGroupDefinitionResponse> updateCompanyGroup(
      @PathVariable(name = "companyGroupId") final String companyGroupId,
      @Valid @RequestBody final CompanyGroupUpdateRequest request) {
    return ResponseEntity.ok(
        companyGroupDefinitionFacade.updateCompanyGroup(companyGroupId, request));
  }

  @GetMapping("/{companyGroupId}/products")
  public ResponseEntity<List<CompanyGroupProductResponse>> findCompanyGroupProducts(
      @PathVariable(name = "companyGroupId") final String companyGroupId) {
    return ResponseEntity.ok(companyGroupDefinitionFacade.findCompanyGroupProducts(companyGroupId));
  }

  @PostMapping("/{companyGroupId}/products")
  public ResponseEntity<CompanyGroupProductResponse> saveCompanyGroupProduct(
      @PathVariable(name = "companyGroupId") final String companyGroupId,
      @Valid @RequestBody final CompanyGroupProductSaveRequest request) {
    var created = companyGroupDefinitionFacade.saveCompanyGroupProduct(companyGroupId, request);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{product}")
            .buildAndExpand(created.getProduct())
            .toUri();
    return ResponseEntity.created(location).body(created);
  }

  @PutMapping("/{companyGroupId}/products/{product}")
  public ResponseEntity<CompanyGroupProductResponse> updateCompanyGroupProduct(
      @PathVariable(name = "companyGroupId") final String companyGroupId,
      @PathVariable(name = "product") final ApplicationProduct product,
      @Valid @RequestBody final CompanyGroupProductUpdateRequest request) {
    return ResponseEntity.ok(
        companyGroupDefinitionFacade.updateCompanyGroupProduct(companyGroupId, product, request));
  }

  @GetMapping("/{companyGroupId}/dropdown/zones")
  public ResponseEntity<List<ResolvedEnum>> findZones(
      @PathVariable(name = "companyGroupId") final String companyGroupId) {
    return ResponseEntity.ok(companyGroupDefinitionFacade.findZones(companyGroupId));
  }

  @GetMapping("/{companyGroupId}/dropdown/countries")
  public ResponseEntity<List<ResolvedEnum>> findCountries(
      @PathVariable(name = "companyGroupId") final String companyGroupId) {
    return ResponseEntity.ok(companyGroupDefinitionFacade.findCountries(companyGroupId));
  }

  @GetMapping("/{companyGroupId}/dropdown/country/{country}/cities")
  public ResponseEntity<List<ResolvedEnum>> findCities(
      @PathVariable(name = "companyGroupId") final String companyGroupId,
      @PathVariable(name = "country") final Country country) {
    return ResponseEntity.ok(companyGroupDefinitionFacade.findCities(companyGroupId, country));
  }

  @GetMapping("/{companyGroupId}/dropdown/segment1")
  public ResponseEntity<List<ResolvedEnum>> findSegment1(
      @PathVariable(name = "companyGroupId") final String companyGroupId) {
    return ResponseEntity.ok(companyGroupDefinitionFacade.findSegment1(companyGroupId));
  }

  @GetMapping("/{companyGroupId}/dropdown/segment2")
  public ResponseEntity<List<ResolvedEnum>> findSegment2(
      @PathVariable(name = "companyGroupId") final String companyGroupId) {
    return ResponseEntity.ok(companyGroupDefinitionFacade.findSegment2(companyGroupId));
  }

  @GetMapping("/{companyGroupId}/dropdown/segment3")
  public ResponseEntity<List<ResolvedEnum>> findSegment3(
      @PathVariable(name = "companyGroupId") final String companyGroupId) {
    return ResponseEntity.ok(companyGroupDefinitionFacade.findSegment3(companyGroupId));
  }

  @GetMapping("/{companyGroupId}/dropdown/segment4")
  public ResponseEntity<List<ResolvedEnum>> findSegment4(
      @PathVariable(name = "companyGroupId") final String companyGroupId) {
    return ResponseEntity.ok(companyGroupDefinitionFacade.findSegment4(companyGroupId));
  }

  @GetMapping("/{companyGroupId}/dropdown/segment5")
  public ResponseEntity<List<ResolvedEnum>> findSegment5(
      @PathVariable(name = "companyGroupId") final String companyGroupId) {
    return ResponseEntity.ok(companyGroupDefinitionFacade.findSegment5(companyGroupId));
  }
}
