package com.gastroblue.model.request;

import com.gastroblue.commons.shared.enums.ApplicationProduct;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RefreshTokenRequest(
    @NotBlank String refreshToken,
    @NotNull(message = "{validation.channel.check.null}") ApplicationProduct product) {}
