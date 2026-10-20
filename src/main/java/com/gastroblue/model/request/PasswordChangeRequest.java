package com.gastroblue.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
    @NotBlank(message = "{validation.oldPassword.check.null}") String oldPassword,
    @NotBlank(message = "{validation.newPassword.check.null}")
        @Size(min = 5, message = "{validation.newPassword.size.min.5}")
        String newPassword) {}
