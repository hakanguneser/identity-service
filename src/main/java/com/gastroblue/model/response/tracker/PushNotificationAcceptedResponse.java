package com.gastroblue.model.response.tracker;

import lombok.Builder;

@Builder
public record PushNotificationAcceptedResponse(int recipientCount) {}
