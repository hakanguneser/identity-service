package com.gastroblue.controller;

import com.gastroblue.facade.UserDefinitionFacade;
import com.gastroblue.model.enums.*;
import com.gastroblue.model.request.PasswordChangeRequest;
import com.gastroblue.model.request.UserSaveRequest;
import com.gastroblue.model.request.UserUpdateRequest;
import com.gastroblue.model.response.UserDefinitionResponse;
import com.gastroblue.model.shared.DropdownModel;
import com.gastroblue.model.shared.ResolvedEnum;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("api/v1/definition/users")
@RequiredArgsConstructor
public class UserDefinitionController {
  private final UserDefinitionFacade userFacade;

  @PostMapping
  public ResponseEntity<UserDefinitionResponse> saveUser(
      @Valid @RequestBody final UserSaveRequest request) {
    UserDefinitionResponse userDefinitionResponse = userFacade.saveUser(request);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(userDefinitionResponse.getUserId())
            .toUri();
    return ResponseEntity.created(location).body(userDefinitionResponse);
  }

  @GetMapping("/{userId}")
  ResponseEntity<UserDefinitionResponse> findById(@PathVariable("userId") final String userId) {
    return ResponseEntity.ok(userFacade.findUserById(userId));
  }

  @PutMapping("/{userId}")
  ResponseEntity<UserDefinitionResponse> updateUser(
      @PathVariable("userId") final String userId,
      @Valid @RequestBody final UserUpdateRequest request) {
    return ResponseEntity.ok(userFacade.updateUser(userId, request));
  }

  @PatchMapping("/{userId}/status")
  ResponseEntity<UserDefinitionResponse> updateStatus(@PathVariable("userId") final String userId) {
    return ResponseEntity.ok(userFacade.toggleUser(userId));
  }

  @PutMapping(value = "/{userId}/password")
  public ResponseEntity<Void> changePassword(
      @PathVariable("userId") final String userId,
      @Valid @RequestBody final PasswordChangeRequest request) {
    userFacade.changePassword(userId, request);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{userId}/password/otp")
  public ResponseEntity<Void> requestPasswordResetOtp(@PathVariable("userId") final String userId) {
    userFacade.sendOtp(userId);
    return ResponseEntity.accepted().build();
  }

  @GetMapping("/accessible") // pageable ????
  public ResponseEntity<List<UserDefinitionResponse>> findAccessibleUsers(
      @RequestParam(name = "includeAll", defaultValue = "false", required = false)
          boolean includeAll) {
    return ResponseEntity.ok(userFacade.findAccessibleUsers(includeAll));
  }

  @GetMapping("dropdown/application-roles")
  public ResponseEntity<List<ResolvedEnum<ApplicationRole>>> findAllApplicationRoles() {
    return ResponseEntity.ok(userFacade.findAllApplicationRoles());
  }

  @GetMapping("dropdown/departments")
  public ResponseEntity<List<ResolvedEnum<Department>>> findAllDepartments() {
    return ResponseEntity.ok(userFacade.findAllDepartments());
  }

  @GetMapping("dropdown/genders")
  public ResponseEntity<List<ResolvedEnum<Gender>>> findAllGenders() {
    return ResponseEntity.ok(userFacade.findAllGenders());
  }

  @GetMapping("dropdown/zones")
  public ResponseEntity<List<ResolvedEnum<Zone>>> findAllZones() {
    return ResponseEntity.ok(userFacade.findAllZones());
  }

  @GetMapping("dropdown/company-groups")
  public ResponseEntity<List<DropdownModel>> findAllCompanyGroups() {
    return ResponseEntity.ok(userFacade.findAllCompanyGroups());
  }

  @GetMapping("dropdown/companies")
  public ResponseEntity<List<DropdownModel>> findAllCompanies() {
    return ResponseEntity.ok(userFacade.findAllCompanies());
  }
}
