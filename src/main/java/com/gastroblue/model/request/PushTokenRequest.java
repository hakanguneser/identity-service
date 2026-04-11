package com.gastroblue.model.request;

import jakarta.validation.constraints.NotBlank;

public record PushTokenRequest(@NotBlank(message = "{validation.token.check.null}") String token) {}
