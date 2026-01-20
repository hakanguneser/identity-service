package com.gastroblue.controller;

import com.gastroblue.facade.CompanyGroupEulaContentFacade;
import com.gastroblue.model.request.CompanyGroupEulaContentSaveRequest;
import com.gastroblue.model.request.CompanyGroupEulaContentUpdateRequest;
import com.gastroblue.model.response.CompanyGroupEulaContentResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(value = "api/v1/definition/company-groups/{companyGroupId}/eula-contents")
@RequiredArgsConstructor
public class CompanyGroupEulaContentController {

  // TODO : burada once summary verip detayinda companyGroupId ile contente gidilmeli

  private final CompanyGroupEulaContentFacade eulaContentFacade;

  @GetMapping
  public ResponseEntity<List<CompanyGroupEulaContentResponse>> findAll(
      @PathVariable(name = "companyGroupId") final String companyGroupId) {
    return ResponseEntity.ok(eulaContentFacade.findAllByCompanyGroupId(companyGroupId));
  }

  @PostMapping
  public ResponseEntity<CompanyGroupEulaContentResponse> create(
      @PathVariable(name = "companyGroupId") final String companyGroupId,
      @Valid @RequestBody final CompanyGroupEulaContentSaveRequest request) {
    var created = eulaContentFacade.create(companyGroupId, request);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.getId())
            .toUri();
    return ResponseEntity.created(location).body(created);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CompanyGroupEulaContentResponse> findById(
      @PathVariable(name = "companyGroupId") final String companyGroupId,
      @PathVariable(name = "id") final String id) {
    return ResponseEntity.ok(eulaContentFacade.findById(companyGroupId, id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<CompanyGroupEulaContentResponse> update(
      @PathVariable(name = "companyGroupId") final String companyGroupId,
      @PathVariable(name = "id") final String id,
      @Valid @RequestBody final CompanyGroupEulaContentUpdateRequest request) {
    return ResponseEntity.ok(eulaContentFacade.update(companyGroupId, id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @PathVariable(name = "companyGroupId") final String companyGroupId,
      @PathVariable(name = "id") final String id) {
    eulaContentFacade.delete(companyGroupId, id);
    return ResponseEntity.noContent().build();
  }
}
