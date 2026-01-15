package com.gastroblue.controller;

import com.gastroblue.facade.AuthenticationFacade;
import com.gastroblue.model.request.AuthLoginRequest;
import com.gastroblue.model.response.*;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final AuthenticationFacade authenticationFacade;

  @PostMapping("/login")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<AuthLoginResponse> login(
      @Valid @RequestBody AuthLoginRequest loginRequest) {
    return ResponseEntity.ok(authenticationFacade.login(loginRequest));
  }

  @GetMapping("/my/info")
  public ResponseEntity<AuthUserInfoResponse> findAuthenticatedUserInfo() {
    return ResponseEntity.ok(authenticationFacade.findAuthenticatedUserInfo());
  }

  @GetMapping("/my/company-groups")
  public ResponseEntity<List<AuthUserCompanyGroupResponse>> findMyCompanyGroups() {
    return ResponseEntity.ok(authenticationFacade.findMyCompanyGroups());
  }

  @GetMapping("/my/companies")
  public ResponseEntity<List<AuthUserCompanyResponse>> findMyCompanies() {
    return ResponseEntity.ok(authenticationFacade.findMyCompanies());
  }

  @GetMapping("/agreement")
  public ResponseEntity<AgreementResponse> getAgreement() {
    return ResponseEntity.ok(authenticationFacade.getAgreement());
  }

  @PatchMapping("/agreement")
  public ResponseEntity<Void> signAgreement() {
    authenticationFacade.signAgreement();
    return ResponseEntity.noContent().build();
  }
}
