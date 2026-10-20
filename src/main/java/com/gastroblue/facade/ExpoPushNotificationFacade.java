package com.gastroblue.facade;

import com.gastroblue.client.ExpoPushClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpoPushNotificationFacade {

  private final ExpoPushClient expoPushClient;

  @Async("pushNotificationTaskExecutor")
  public void dispatch(List<String> expoPushTokens, String title, String body) {
    log.debug(
        "expo.push.dispatch [tokenCount={}, title.len={}, body.len={}]",
        expoPushTokens != null ? expoPushTokens.size() : 0,
        title != null ? title.length() : 0,
        body != null ? body.length() : 0);
    try {
      expoPushClient.sendNotifications(expoPushTokens, title, body);
    } catch (Exception ex) {
      log.error("expo.push.dispatch.failed [error={}]", ex.getMessage(), ex);
    }
  }
}
