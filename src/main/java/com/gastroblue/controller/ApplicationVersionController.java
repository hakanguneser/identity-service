package com.gastroblue.controller;

import com.gastroblue.facade.ApplicationVersionFacade;
import com.gastroblue.model.request.AppVersionCheckRequest;
import com.gastroblue.model.response.AppVersionCheckResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public: the mobile app calls it on start, before login. */
@RestController
@RequestMapping(value = "api/v1/app-versions")
@RequiredArgsConstructor
public class ApplicationVersionController {

  private final ApplicationVersionFacade applicationVersionFacade;

  // GET /api/v1/app-versions/check?product=TRACKER&platform=ANDROID&version=1.4.2
  @GetMapping("/check")
  public ResponseEntity<AppVersionCheckResponse> check(
      @Valid @ModelAttribute final AppVersionCheckRequest request) {
    return ResponseEntity.ok(applicationVersionFacade.check(request));
  }
}
