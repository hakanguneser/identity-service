package com.gastroblue.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record TrackerUsersByIdsRequest(
    @NotEmpty(message = "{validation.tracker.userIds.notEmpty}")
        List<@NotBlank String> userIdList) {}
