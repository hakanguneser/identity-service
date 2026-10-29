package com.gastroblue.client;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gastroblue.model.enums.PushNotificationError;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/**
 * HTTP client for <a href="https://docs.expo.dev/push-notifications/sending-notifications/">Expo
 * Push API</a> ({@code POST /--/api/v2/push/send}).
 *
 * <p>The same notification is sent as a single message whose {@code to} field lists up to {@value
 * #MAX_TOKENS_PER_REQUEST} tokens, instead of one message per token. Expo counts every token as one
 * notification, so larger recipient lists are split into several requests.
 */
@Component
@Slf4j
public class ExpoPushClient {

  private static final String SEND_PATH = "/--/api/v2/push/send";
  private static final int MAX_TOKENS_PER_REQUEST = 100;
  private static final String STATUS_OK = "ok";

  private final RestClient expoPushRestClient;
  private final String accessToken;

  public ExpoPushClient(
      RestClient expoPushRestClient,
      @Value("${app.expo.access-token:#{null}}") String accessToken) {
    this.expoPushRestClient = expoPushRestClient;
    this.accessToken = accessToken;
  }

  /**
   * Sends one notification to the given tokens and returns one result per distinct token. Tokens
   * may be bare or wrapped; results are keyed by the bare token (see {@link ExpoPushTokens}). Never
   * throws: a failed request is reported as {@code ExpoRequestFailed} for every token in it.
   */
  public List<ExpoPushResult> sendNotifications(
      List<String> expoPushTokens, String title, String body) {
    if (expoPushTokens == null || expoPushTokens.isEmpty()) {
      return List.of();
    }
    List<String> tokens =
        new ArrayList<>(
            new LinkedHashSet<>(
                expoPushTokens.stream()
                    .map(ExpoPushTokens::normalize)
                    .filter(StringUtils::hasText)
                    .toList()));
    List<ExpoPushResult> results = new ArrayList<>(tokens.size());
    for (int from = 0; from < tokens.size(); from += MAX_TOKENS_PER_REQUEST) {
      List<String> chunk =
          tokens.subList(from, Math.min(from + MAX_TOKENS_PER_REQUEST, tokens.size())).stream()
              .map(ExpoPushTokens::toExpoFormat)
              .toList();
      results.addAll(post(new ExpoPushMessage(chunk, title, body)));
    }
    return results;
  }

  private List<ExpoPushResult> post(ExpoPushMessage message) {
    try {
      ExpoPushResponse response =
          expoPushRestClient
              .post()
              .uri(SEND_PATH)
              .headers(
                  h -> {
                    if (StringUtils.hasText(accessToken)) {
                      h.setBearerAuth(accessToken.trim());
                    }
                  })
              // Expo expects an array of messages; one message carries all tokens.
              .body(List.of(message))
              .retrieve()
              .body(ExpoPushResponse.class);
      return toResults(message, response);
    } catch (Exception ex) {
      log.error(
          "expo.push.request.failed [tokenCount={}, error={}]",
          message.to().size(),
          ex.getMessage(),
          ex);
      String code = PushNotificationError.EXPO_REQUEST_FAILED.getCode();
      return message.to().stream()
          .map(
              token ->
                  new ExpoPushResult(ExpoPushTokens.normalize(token), false, code, ex.getMessage()))
          .toList();
    }
  }

  /**
   * Expo returns one ticket per token, in the same order as {@code to}. Error tickets also carry
   * the token in {@code details.expoPushToken}, which is preferred over the position.
   */
  private List<ExpoPushResult> toResults(ExpoPushMessage message, ExpoPushResponse response) {
    List<ExpoPushTicket> tickets =
        response != null && response.data() != null ? response.data() : List.of();
    List<ExpoPushResult> results = new ArrayList<>(message.to().size());
    for (int i = 0; i < message.to().size(); i++) {
      String token = ExpoPushTokens.normalize(message.to().get(i));
      ExpoPushTicket ticket = i < tickets.size() ? tickets.get(i) : null;
      if (ticket == null) {
        results.add(
            new ExpoPushResult(
                token,
                false,
                PushNotificationError.UNKNOWN.getCode(),
                "No ticket returned for token"));
      } else if (STATUS_OK.equals(ticket.status())) {
        results.add(new ExpoPushResult(token, true, null, null));
      } else {
        Object detailToken =
            ticket.details() != null ? ticket.details().get("expoPushToken") : null;
        Object errorCode = ticket.details() != null ? ticket.details().get("error") : null;
        results.add(
            new ExpoPushResult(
                detailToken != null ? ExpoPushTokens.normalize(detailToken.toString()) : token,
                false,
                errorCode != null ? errorCode.toString() : PushNotificationError.UNKNOWN.getCode(),
                ticket.message()));
      }
    }
    long errorCount = results.stream().filter(r -> !r.ok()).count();
    log.info(
        "expo.push.sent [tokenCount={}, ticketCount={}, errorCount={}]",
        message.to().size(),
        tickets.size(),
        errorCount);
    results.stream()
        .filter(r -> !r.ok())
        .forEach(
            r ->
                log.warn(
                    "expo.push.ticket.error [token={}, error={}, message={}]",
                    mask(r.token()),
                    r.errorCode(),
                    r.message()));
    return results;
  }

  private static String mask(String token) {
    return token.length() <= 24 ? token : token.substring(0, 24) + "...";
  }

  private record ExpoPushMessage(List<String> to, String title, String body) {}

  /** Expo returns a single ticket object instead of an array for one message to one recipient. */
  @JsonIgnoreProperties(ignoreUnknown = true)
  private record ExpoPushResponse(
      @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
          List<ExpoPushTicket> data) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record ExpoPushTicket(
      String status, String id, String message, Map<String, Object> details) {}
}
