package com.gastroblue.model.request;

import lombok.Builder;

@Builder
public record EnumConfigurationUpdateRequest(String label, Boolean active) {}
