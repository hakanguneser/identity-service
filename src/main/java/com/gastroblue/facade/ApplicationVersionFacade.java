package com.gastroblue.facade;

import com.gastroblue.model.entity.ApplicationVersionEntity;
import com.gastroblue.model.enums.AppUpdateType;
import com.gastroblue.model.request.AppVersionCheckRequest;
import com.gastroblue.model.response.AppVersionCheckResponse;
import com.gastroblue.service.ApplicationVersionService;
import com.gastroblue.util.AppVersion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationVersionFacade {

  private final ApplicationVersionService applicationVersionService;

  /**
   * Compares the app's version with the defined one. Without a definition the app is not stopped
   * (NONE): the check must never be the reason an app cannot open.
   */
  public AppVersionCheckResponse check(AppVersionCheckRequest request) {
    AppVersion current = AppVersion.parse(request.version());
    return applicationVersionService
        .findActive(request.product(), request.platform())
        .map(definition -> toResponse(updateType(current, definition), definition))
        .orElseGet(() -> AppVersionCheckResponse.builder().updateType(AppUpdateType.NONE).build());
  }

  private static AppUpdateType updateType(AppVersion current, ApplicationVersionEntity definition) {
    if (current.isOlderThan(AppVersion.parse(definition.getMinSupportedVersion()))) {
      return AppUpdateType.REQUIRED;
    }
    if (current.isOlderThan(AppVersion.parse(definition.getLatestVersion()))) {
      return AppUpdateType.OPTIONAL;
    }
    return AppUpdateType.NONE;
  }

  private static AppVersionCheckResponse toResponse(
      AppUpdateType updateType, ApplicationVersionEntity definition) {
    return AppVersionCheckResponse.builder()
        .updateType(updateType)
        .latestVersion(definition.getLatestVersion())
        .minSupportedVersion(definition.getMinSupportedVersion())
        .storeUrl(definition.getStoreUrl())
        .build();
  }
}
