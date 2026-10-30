package com.gastroblue.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gastroblue.client.ExpoPushClient;
import com.gastroblue.client.ExpoPushResult;
import com.gastroblue.commons.helper.lookup.service.ILookupService;
import com.gastroblue.commons.shared.enums.ApplicationProduct;
import com.gastroblue.commons.shared.enums.ApplicationRole;
import com.gastroblue.model.entity.UserProductEntity;
import com.gastroblue.model.request.PushNotificationRequest;
import com.gastroblue.model.response.tracker.PushNotificationResponse;
import com.gastroblue.repository.UserProductRepository;
import com.gastroblue.service.CompanyGroupService;
import com.gastroblue.service.CompanyService;
import com.gastroblue.service.UserDefinitionService;
import com.gastroblue.service.UserProductService;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class UnregisteredPushTokenTest {

  private final UserProductService userProductService = mock(UserProductService.class);
  private final ExpoPushClient expoPushClient = mock(ExpoPushClient.class);

  private final TrackerFacade facade =
      new TrackerFacade(
          mock(UserDefinitionService.class),
          userProductService,
          mock(CompanyService.class),
          mock(CompanyGroupService.class),
          mock(ILookupService.class),
          expoPushClient);

  @Test
  void deviceNotRegisteredTokensAreCleared() {
    givenTokens(product("u1", "tok-1"), product("u2", "ExponentPushToken[tok-2]"));
    when(expoPushClient.sendNotifications(anyList(), anyString(), anyString()))
        .thenReturn(
            List.of(
                new ExpoPushResult("tok-1", true, null, null),
                new ExpoPushResult("tok-2", false, "DeviceNotRegistered", "gone")));

    PushNotificationResponse response = send("u1", "u2");

    verify(userProductService)
        .clearPushTokens(eq(ApplicationProduct.TRACKER), eq(List.of("tok-2")));
    assertThat(response.successCount()).isEqualTo(1);
  }

  @Test
  void otherErrorsKeepTheToken() {
    givenTokens(product("u1", "tok-1"));
    when(expoPushClient.sendNotifications(anyList(), anyString(), anyString()))
        .thenReturn(List.of(new ExpoPushResult("tok-1", false, "MessageRateExceeded", "slow")));

    send("u1");

    verify(userProductService, never()).clearPushTokens(any(), anyCollection());
  }

  @Test
  void failedCleanupDoesNotFailThePush() {
    givenTokens(product("u1", "tok-1"));
    when(expoPushClient.sendNotifications(anyList(), anyString(), anyString()))
        .thenReturn(List.of(new ExpoPushResult("tok-1", false, "DeviceNotRegistered", "gone")));
    when(userProductService.clearPushTokens(any(), anyCollection()))
        .thenThrow(new IllegalStateException("db down"));

    assertThat(send("u1").failureCount()).isEqualTo(1);
  }

  @Test
  void everyStoredFormOfATokenIsMatched() {
    UserProductRepository repository = mock(UserProductRepository.class);
    UserProductService service = new UserProductService(repository);

    service.clearPushTokens(ApplicationProduct.TRACKER, List.of("tok-2"));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Collection<String>> tokens = ArgumentCaptor.forClass(Collection.class);
    verify(repository).clearPushTokens(eq(ApplicationProduct.TRACKER), tokens.capture());
    assertThat(tokens.getValue())
        .containsExactlyInAnyOrder("tok-2", "ExponentPushToken[tok-2]", "ExpoPushToken[tok-2]");
  }

  private void givenTokens(UserProductEntity... products) {
    when(userProductService.findByUserIdInAndProduct(anyList(), eq(ApplicationProduct.TRACKER)))
        .thenReturn(List.of(products));
  }

  private PushNotificationResponse send(String... userIds) {
    return facade.sendPushNotifications(
        new PushNotificationRequest(List.of(userIds), "title", "body"));
  }

  private static UserProductEntity product(String userId, String token) {
    return UserProductEntity.builder()
        .userId(userId)
        .product(ApplicationProduct.TRACKER)
        .applicationRole(ApplicationRole.STAFF)
        .pushToken(token)
        .active(true)
        .build();
  }
}
