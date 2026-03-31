package com.gastroblue.controller;

import com.gastroblue.facade.EligibilityFacade;
import com.gastroblue.model.response.UserEligibilityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/eligibility")
@RequiredArgsConstructor
public class EligibilityController {

  private final EligibilityFacade eligibilityFacade;

  @GetMapping("/tracker/add-user")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<UserEligibilityResponse> addUser() {
    UserEligibilityResponse response = eligibilityFacade.trackerAddUser();
    return ResponseEntity.ok(response);
  }
}
