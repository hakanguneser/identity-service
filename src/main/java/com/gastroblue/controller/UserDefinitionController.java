package com.gastroblue.controller;

import com.gastroblue.facade.UserDefinitionFacade;
import com.gastroblue.model.request.LanguageUpdateRequest;
import com.gastroblue.model.request.PasswordChangeRequest;
import com.gastroblue.model.request.UserSaveRequest;
import com.gastroblue.model.request.UserUpdateRequest;
import com.gastroblue.model.response.AccessibleUsersResponse;
import com.gastroblue.model.response.CompanyContextResponse;
import com.gastroblue.model.response.DropdownResponse;
import com.gastroblue.model.response.UserDefinitionResponse;
import jakarta.validation.Valid;
import java.net.URI;
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

  @GetMapping("/{userId}/company-context")
  ResponseEntity<CompanyContextResponse> findUserCompanyContext(
      @PathVariable("userId") final String userId) {
    return ResponseEntity.ok(userFacade.findUserCompanyContext(userId));
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

  @PatchMapping("/{userId}/language")
  ResponseEntity<Void> updateLanguage(
      @PathVariable("userId") final String userId,
      @RequestBody @Valid final LanguageUpdateRequest request) {
    userFacade.updateLanguage(userId, request);
    return ResponseEntity.noContent().build();
  }

  @PutMapping(value = "/password")
  public ResponseEntity<Void> changePassword(
      @Valid @RequestBody final PasswordChangeRequest request) {
    userFacade.changePassword(request);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{userId}/password/otp")
  public ResponseEntity<Void> requestPasswordResetOtp(@PathVariable("userId") final String userId) {
    userFacade.sendOtp(userId);
    return ResponseEntity.accepted().build();
  }

  @GetMapping("/accessible") // pageable ????
  public ResponseEntity<AccessibleUsersResponse> findAccessibleUsers(
      @RequestParam(name = "includeAll", defaultValue = "false", required = false)
          boolean includeAll) {
    return ResponseEntity.ok(userFacade.findAccessibleUsers(includeAll));
  }

  @GetMapping("dropdown/application-roles")
  public ResponseEntity<DropdownResponse> findAllApplicationRoles() {
    return ResponseEntity.ok(userFacade.findAllApplicationRoles());
  }

  @GetMapping("dropdown/departments")
  public ResponseEntity<DropdownResponse> findAllDepartments() {
    return ResponseEntity.ok(userFacade.findAllDepartments());
  }

  @GetMapping("dropdown/genders")
  public ResponseEntity<DropdownResponse> findAllGenders() {
    return ResponseEntity.ok(userFacade.findAllGenders());
  }

  @GetMapping("dropdown/zones")
  public ResponseEntity<DropdownResponse> findAllZones() {
    return ResponseEntity.ok(userFacade.findAllZones());
  }

  @GetMapping("dropdown/company-groups")
  public ResponseEntity<DropdownResponse> findAvailableCompanyGroups() {
    return ResponseEntity.ok(userFacade.findAvailableCompanyGroups());
  }

  @GetMapping("dropdown/companies")
  public ResponseEntity<DropdownResponse> findAvailableCompanies() {
    return ResponseEntity.ok(userFacade.findAvailableCompanies());
  }
}
