package com.gastroblue.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gastroblue.commons.shared.enums.ApplicationProduct;
import com.gastroblue.model.entity.ApplicationVersionEntity;
import com.gastroblue.model.enums.AppUpdateType;
import com.gastroblue.model.enums.DevicePlatform;
import com.gastroblue.model.request.AppVersionCheckRequest;
import com.gastroblue.model.response.AppVersionCheckResponse;
import com.gastroblue.service.ApplicationVersionService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ApplicationVersionFacadeTest {

  private static final String STORE_URL =
      "https://play.google.com/store/apps/details?id=com.example";

  private final ApplicationVersionService service = mock(ApplicationVersionService.class);
  private final ApplicationVersionFacade facade = new ApplicationVersionFacade(service);

  @ParameterizedTest
  @CsvSource({
    "1.1.9, REQUIRED",
    "1.2.0, OPTIONAL",
    "1.9.9, OPTIONAL",
    "1.10.0, NONE",
    "1.10.0+42, NONE",
    "2.0.0, NONE"
  })
  void theAppVersionIsComparedWithBothThresholds(String appVersion, AppUpdateType expected) {
    when(service.findActive(ApplicationProduct.TRACKER, DevicePlatform.ANDROID))
        .thenReturn(
            Optional.of(
                ApplicationVersionEntity.builder()
                    .product(ApplicationProduct.TRACKER)
                    .platform(DevicePlatform.ANDROID)
                    .minSupportedVersion("1.2.0")
                    .latestVersion("1.10.0")
                    .storeUrl(STORE_URL)
                    .active(true)
                    .build()));

    AppVersionCheckResponse response = facade.check(request(appVersion));

    assertThat(response.getUpdateType()).isEqualTo(expected);
    assertThat(response.getLatestVersion()).isEqualTo("1.10.0");
    assertThat(response.getMinSupportedVersion()).isEqualTo("1.2.0");
    assertThat(response.getStoreUrl()).isEqualTo(STORE_URL);
  }

  @Test
  void withoutADefinitionTheAppIsNotStopped() {
    when(service.findActive(ApplicationProduct.TRACKER, DevicePlatform.ANDROID))
        .thenReturn(Optional.empty());

    AppVersionCheckResponse response = facade.check(request("0.0.1"));

    assertThat(response.getUpdateType()).isEqualTo(AppUpdateType.NONE);
    assertThat(response.getStoreUrl()).isNull();
  }

  private static AppVersionCheckRequest request(String version) {
    return new AppVersionCheckRequest(ApplicationProduct.TRACKER, DevicePlatform.ANDROID, version);
  }
}
