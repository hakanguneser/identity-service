package com.gastroblue.model.response;

import com.gastroblue.model.enums.AppUpdateType;
import lombok.Builder;
import lombok.Getter;

/** Version fields and storeUrl are null when updateType is NONE because no version is defined. */
@Getter
@Builder
public class AppVersionCheckResponse {
  private AppUpdateType updateType;
  private String latestVersion;
  private String minSupportedVersion;
  private String storeUrl;
}
