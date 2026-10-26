package com.gastroblue.model.response.tracker;

import lombok.Builder;

/**
 * @param expoError Expo error code (e.g. DeviceNotRegistered) or NoTokenFound / ExpoRequestFailed
 * @param errorDescription Turkish description shown to operators
 */
@Builder
public record PushNotificationErrorDetail(String expoError, String errorDescription) {}
