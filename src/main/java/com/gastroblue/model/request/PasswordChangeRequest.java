package com.gastroblue.model.request;

import jakarta.validation.constraints.NotBlank;

public record PasswordChangeRequest(
    @NotBlank(message = "{validation.oldPassword.check.null}") String oldPassword,
    @NotBlank(message = "{validation.newPassword.check.null}") String newPassword) {}
