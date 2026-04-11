package com.gastroblue.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PushNotificationRequest(
    @NotEmpty(message = "{validation.push.userIds.notEmpty}") List<@NotBlank String> userIdList,
    @NotBlank(message = "{validation.push.title.notBlank}") String title,
    @NotBlank(message = "{validation.push.body.notBlank}") String body) {}
