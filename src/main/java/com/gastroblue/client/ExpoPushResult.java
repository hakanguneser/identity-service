package com.gastroblue.client;

/**
 * Delivery result of one Expo push token.
 *
 * @param token the Expo push token
 * @param ok true when Expo accepted the notification for this token
 * @param errorCode Expo error code ({@code details.error}) or {@code ExpoRequestFailed}; null when
 *     ok
 * @param message Expo's error message; null when ok
 */
public record ExpoPushResult(String token, boolean ok, String errorCode, String message) {}
