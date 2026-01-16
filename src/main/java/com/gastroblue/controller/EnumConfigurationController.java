package com.gastroblue.controller;

import com.gastroblue.facade.EnumConfigurationFacade;
import com.gastroblue.model.request.EnumConfigurationSaveRequest;
import com.gastroblue.model.request.EnumConfigurationUpdateRequest;
import com.gastroblue.model.response.EnumConfigurationResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/configuration/enums")
@RequiredArgsConstructor
public class EnumConfigurationController {

  private final EnumConfigurationFacade facade;

  @PostMapping
  public ResponseEntity<EnumConfigurationResponse> save(
      @RequestBody @Valid EnumConfigurationSaveRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(facade.save(request));
  }

  @PutMapping("/{id}")
  public ResponseEntity<EnumConfigurationResponse> update(
      @PathVariable String id,
      @RequestBody @Valid EnumConfigurationUpdateRequest request,
      @RequestParam String companyGroupId) {
    return ResponseEntity.ok(facade.update(id, request, companyGroupId));
  }

  @GetMapping("/{id}")
  public ResponseEntity<EnumConfigurationResponse> findById(
      @PathVariable String id, @RequestParam String companyGroupId) {
    return ResponseEntity.ok(facade.findById(id, companyGroupId));
  }

  @GetMapping
  public ResponseEntity<List<EnumConfigurationResponse>> findAll(
      @RequestParam String companyGroupId) {
    return ResponseEntity.ok(facade.findAll(companyGroupId));
  }
}
