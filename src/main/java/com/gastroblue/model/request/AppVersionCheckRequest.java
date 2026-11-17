package com.gastroblue.model.request;

import com.gastroblue.commons.shared.enums.ApplicationProduct;
import com.gastroblue.model.enums.DevicePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Sent by the mobile app on start, before login. Version example: 1.4.2 or 1.4.2+15. */
public record AppVersionCheckRequest(
    @NotNull ApplicationProduct product,
    @NotNull DevicePlatform platform,
    @NotBlank @Size(max = 30) @Pattern(regexp = "^\\d{1,9}(\\.\\d{1,9}){0,3}(\\+\\w+)?$")
        String version) {}
