package com.gastroblue.controller;

import com.gastroblue.facade.TrackerFacade;
import com.gastroblue.model.request.PushNotificationRequest;
import com.gastroblue.model.request.TrackerUsersByIdsRequest;
import com.gastroblue.model.response.tracker.PushNotificationAcceptedResponse;
import com.gastroblue.model.response.tracker.TrackerCompanyContextResponse;
import com.gastroblue.model.response.tracker.TrackerCompanyUsersResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tracker")
@RequiredArgsConstructor
public class TrackerController {

  private final TrackerFacade trackerFacade;

  @GetMapping("/company-groups/{companyGroupId}/companies/{companyId}/users")
  public ResponseEntity<TrackerCompanyUsersResponse> findCompanyUsers(
      @PathVariable("companyGroupId") String companyGroupId,
      @PathVariable("companyId") String companyId) {
    return ResponseEntity.ok(trackerFacade.findCompanyUsers(companyGroupId, companyId));
  }

  @GetMapping("/context/by-codes")
  public ResponseEntity<TrackerCompanyContextResponse> findCompanyContext(
      @RequestParam("companyGroupCode") String companyGroupCode,
      @RequestParam("companyCode") String companyCode) {
    return ResponseEntity.ok(
        trackerFacade.findCompanyContextByCodes(companyGroupCode, companyCode));
  }

  @PostMapping("/users/push-notifications")
  public ResponseEntity<PushNotificationAcceptedResponse> pushNotification(
      @Valid @RequestBody PushNotificationRequest request) {
    return ResponseEntity.accepted().body(trackerFacade.enqueuePushNotifications(request));
  }

  @PostMapping("/users/by-ids")
  public ResponseEntity<TrackerCompanyUsersResponse> findUsersByIds(
      @Valid @RequestBody TrackerUsersByIdsRequest request) {
    return ResponseEntity.ok(trackerFacade.findUsersByIds(request));
  }
}
