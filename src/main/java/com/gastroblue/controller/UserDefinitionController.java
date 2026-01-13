package com.gastroblue.controller;

import com.gastroblue.facade.UserDefinitionFacade;
import com.gastroblue.model.request.BatchUserSaveRequest;
import com.gastroblue.model.request.PasswordChangeRequest;
import com.gastroblue.model.request.UserSaveRequest;
import com.gastroblue.model.request.UserUpdateRequest;
import com.gastroblue.model.response.BatchUserDefinitionResponse;
import com.gastroblue.model.response.UserDefinitionResponse;
import com.gastroblue.model.shared.EnumDisplay;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("v1/definition/users")
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

  @PostMapping("/batch")
  public ResponseEntity<BatchUserDefinitionResponse> saveUsersBatch(
      @Valid @RequestBody final BatchUserSaveRequest request) {
    BatchUserDefinitionResponse response = userFacade.saveUsersBatch(request.items());
    return ResponseEntity.ok(response);
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
  public ResponseEntity<List<EnumDisplay>> findAllApplicationRoles() {
    return ResponseEntity.ok(userFacade.findAllApplicationRoles());
  }

  @GetMapping("dropdown/departments")
  public ResponseEntity<List<EnumDisplay>> findAllDepartments() {
    return ResponseEntity.ok(userFacade.findAllDepartments());
  }

  @GetMapping("dropdown/genders")
  public ResponseEntity<List<EnumDisplay>> findAllGenders() {
    return ResponseEntity.ok(userFacade.findAllGenders());
  }

  @GetMapping("dropdown/zones")
  public ResponseEntity<List<EnumDisplay>> findAllZones() {
    return ResponseEntity.ok(userFacade.findAllZones());
  }
}
