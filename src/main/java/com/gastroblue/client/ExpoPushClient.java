package com.gastroblue.client;

import com.gastroblue.model.properties.ExpoPushProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/**
 * HTTP client for <a href="https://docs.expo.dev/push-notifications/sending-notifications/">Expo
 * Push API</a> ({@code POST /--/api/v2/push/send}).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExpoPushClient {

  private static final String SEND_PATH = "/--/api/v2/push/send";
  private static final int MAX_MESSAGES_PER_REQUEST = 100;

  private final RestClient expoPushRestClient;
  private final ExpoPushProperties expoPushProperties;

  public void sendNotifications(List<String> expoPushTokens, String title, String body) {
    if (expoPushTokens == null || expoPushTokens.isEmpty()) {
      return;
    }
    List<ExpoPushMessage> batch = new ArrayList<>(MAX_MESSAGES_PER_REQUEST);
    for (String token : expoPushTokens) {
      if (!StringUtils.hasText(token)) {
        continue;
      }
      batch.add(new ExpoPushMessage(token.trim(), title, body));
      if (batch.size() >= MAX_MESSAGES_PER_REQUEST) {
        postBatch(batch);
        batch.clear();
      }
    }
    if (!batch.isEmpty()) {
      postBatch(batch);
    }
  }

  private void postBatch(List<ExpoPushMessage> messages) {
    try {
      ResponseEntity<String> response =
          expoPushRestClient
              .post()
              .uri(SEND_PATH)
              .headers(
                  h -> {
                    if (StringUtils.hasText(expoPushProperties.getAccessToken())) {
                      h.setBearerAuth(expoPushProperties.getAccessToken().trim());
                    }
                  })
              .body(messages)
              .retrieve()
              .toEntity(String.class);

      HttpStatusCode status = response.getStatusCode();
      if (status.is2xxSuccessful()) {
        log.info("expo.push.sent [batchSize={}, httpStatus={}]", messages.size(), status.value());
        log.debug("expo.push.response.body={}", response.getBody());
      } else {
        log.warn(
            "expo.push.unexpected [batchSize={}, httpStatus={}, body={}]",
            messages.size(),
            status.value(),
            response.getBody());
      }
    } catch (Exception ex) {
      log.error(
          "expo.push.request.failed [batchSize={}, error={}]",
          messages.size(),
          ex.getMessage(),
          ex);
    }
  }

  private record ExpoPushMessage(String to, String title, String body) {}
}
