package com.gastroblue.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BatchUserSaveRequest(
    @NotEmpty @Size(max = 2000) @Valid List<UserSaveRequest> items) {}
