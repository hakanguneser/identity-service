package com.gastroblue.controller;

import com.gastroblue.facade.CompanyDefinitionFacade;
import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.request.CompanyProductSaveRequest;
import com.gastroblue.model.request.CompanyProductUpdateRequest;
import com.gastroblue.model.request.CompanySaveRequest;
import com.gastroblue.model.request.CompanyUpdateRequest;
import com.gastroblue.model.response.CompanyContextResponse;
import com.gastroblue.model.response.CompanyDefinitionResponse;
import com.gastroblue.model.response.CompanyProductResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(value = "api/v1/definition/company-groups")
@RequiredArgsConstructor
public class CompanyDefinitionController {

  private final CompanyDefinitionFacade companyDefinitionFacade;

  @GetMapping("/{companyGroupId}/companies")
  public ResponseEntity<List<CompanyDefinitionResponse>> findCompaniesByCompanyGroupId(
      @PathVariable(name = "companyGroupId") final String companyGroupId) {
    return ResponseEntity.ok(companyDefinitionFacade.findCompaniesByCompanyGroupId(companyGroupId));
  }

  @PostMapping("/{companyGroupId}/companies")
  public ResponseEntity<CompanyDefinitionResponse> saveCompany(
      @PathVariable(name = "companyGroupId") final String companyGroupId,
      @Valid @RequestBody final CompanySaveRequest request) {
    var created = companyDefinitionFacade.saveCompany(companyGroupId, request);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{companyId}")
            .buildAndExpand(created.getCompanyId())
            .toUri();
    return ResponseEntity.created(location).body(created);
  }

  @GetMapping("/{companyGroupId}/companies/{companyId}")
  public ResponseEntity<CompanyDefinitionResponse> findCompanyByCompanyIdAndCompanyGroupId(
      @PathVariable(name = "companyGroupId") final String companyGroupId,
      @PathVariable(name = "companyId") final String companyId) {
    return ResponseEntity.ok(
        companyDefinitionFacade.findCompanyByCompanyIdAndCompanyGroupId(companyGroupId, companyId));
  }

  @PutMapping("/{companyGroupId}/companies/{companyId}")
  public ResponseEntity<CompanyDefinitionResponse> updateCompany(
      @PathVariable(name = "companyGroupId") final String companyGroupId,
      @PathVariable(name = "companyId") final String companyId,
      @Valid @RequestBody final CompanyUpdateRequest request) {
    return ResponseEntity.ok(
        companyDefinitionFacade.updateCompany(companyGroupId, companyId, request));
  }

  @PatchMapping("/{companyGroupId}/companies/{companyId}/status")
  public ResponseEntity<CompanyDefinitionResponse> toggleCompanyStatus(
      @PathVariable(name = "companyGroupId") final String companyGroupId,
      @PathVariable(name = "companyId") final String companyId) {
    return ResponseEntity.ok(
        companyDefinitionFacade.toggleCompanyStatus(companyGroupId, companyId));
  }

  @PatchMapping("/{companyGroupId}/companies/{companyId}/products/{product}/toggle")
  public ResponseEntity<CompanyDefinitionResponse> toggleCompanyProduct(
      @PathVariable(name = "companyGroupId") final String companyGroupId,
      @PathVariable(name = "companyId") final String companyId,
      @PathVariable(name = "product") final ApplicationProduct product) {
    // return ResponseEntity.ok(companyDefinitionFacade.toggleCompanyProduct(companyGroupId,
    // companyId, product));
    // TODO: implement
    return ResponseEntity.ok(null);
  }

  @GetMapping("/{companyGroupId}/companies/{companyId}/products")
  public ResponseEntity<List<CompanyProductResponse>> findCompanyProducts(
      @PathVariable final String companyGroupId, @PathVariable final String companyId) {
    return ResponseEntity.ok(
        companyDefinitionFacade.findCompanyProducts(companyGroupId, companyId));
  }

  @PostMapping("/{companyGroupId}/companies/{companyId}/products")
  public ResponseEntity<CompanyProductResponse> saveCompanyProduct(
      @PathVariable final String companyGroupId,
      @PathVariable final String companyId,
      @Valid @RequestBody final CompanyProductSaveRequest request) {
    var created = companyDefinitionFacade.saveCompanyProduct(companyGroupId, companyId, request);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{product}")
            .buildAndExpand(created.getProduct())
            .toUri();
    return ResponseEntity.created(location).body(created);
  }

  @PutMapping("/{companyGroupId}/companies/{companyId}/products/{product}")
  public ResponseEntity<CompanyProductResponse> updateCompanyProduct(
      @PathVariable final String companyGroupId,
      @PathVariable final String companyId,
      @PathVariable final ApplicationProduct product,
      @Valid @RequestBody final CompanyProductUpdateRequest request) {
    return ResponseEntity.ok(
        companyDefinitionFacade.updateCompanyProduct(companyGroupId, companyId, product, request));
  }

  @DeleteMapping("/{companyGroupId}/companies/{companyId}/products/{product}")
  public ResponseEntity<Void> deleteCompanyProduct(
      @PathVariable final String companyGroupId,
      @PathVariable final String companyId,
      @PathVariable final ApplicationProduct product) {
    companyDefinitionFacade.deleteCompanyProduct(companyGroupId, companyId, product);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @GetMapping("/context")
  public ResponseEntity<CompanyContextResponse> findCompanyAndGroupContext(
      @RequestParam(name = "companyGroupCode") final String companyGroupCode,
      @RequestParam(name = "companyCode") final String companyCode) {
    return ResponseEntity.ok(
        companyDefinitionFacade.findCompanyAndGroupContext(companyGroupCode, companyCode));
  }
}
