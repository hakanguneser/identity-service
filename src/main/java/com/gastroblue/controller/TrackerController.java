package com.gastroblue.controller;

import com.gastroblue.facade.TrackerFacade;
import com.gastroblue.model.response.tracker.TrackerCompanyContextResponse;
import com.gastroblue.model.response.tracker.TrackerCompanyUsersResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/tracker")
@RequiredArgsConstructor
public class TrackerController {

  private final TrackerFacade trackerFacade;

  @GetMapping("/company-groups/{companyGroupId}/companies/{companyId}/users")
  public ResponseEntity<TrackerCompanyUsersResponse> findCompanyUsers(
      @PathVariable("companyGroupId") String companyGroupId,
      @PathVariable("companyId") String companyId) {
    return ResponseEntity.ok(trackerFacade.findCompanyUsers(companyGroupId, companyId));
  }

  @GetMapping("/context")
  public ResponseEntity<TrackerCompanyContextResponse> findCompanyContext(
      @RequestParam("companyGroupCode") String companyGroupCode,
      @RequestParam("companyCode") String companyCode) {
    return ResponseEntity.ok(
        trackerFacade.findCompanyContextByCodes(companyGroupCode, companyCode));
  }
}
