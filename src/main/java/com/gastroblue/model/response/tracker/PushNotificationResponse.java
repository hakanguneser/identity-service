package com.gastroblue.model.response.tracker;

import java.util.List;
import lombok.Builder;

/** One result per requested user, in request order. */
@Builder
public record PushNotificationResponse(
    int recipientCount, int successCount, int failureCount, List<PushNotificationResult> results) {}
