package com.gastroblue.model.response.tracker;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gastroblue.model.enums.PushNotificationResultType;
import lombok.Builder;

/** Push delivery result of one requested user; {@code error} is present only when NOK. */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PushNotificationResult(
    String userId, PushNotificationResultType result, PushNotificationErrorDetail error) {}
