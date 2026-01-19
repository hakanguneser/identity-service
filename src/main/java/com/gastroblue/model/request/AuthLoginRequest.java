package com.gastroblue.model.request;

import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.enums.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuthLoginRequest(
    @NotBlank(message = "{validation.username.check.null}") String username,
    @NotBlank(message = "{validation.password.check.null}") String password,
    @NotNull(message = "{validation.channel.check.null}") ChannelType channel,
    @NotNull(message = "{validation.channel.check.null}") ApplicationProduct product) {}
