package com.gastroblue.model.request;

import jakarta.validation.constraints.Email;
import java.util.List;

public record UserUpdateRequest(
    // Optional, plain strings for now: null keeps the user's current departments.
    List<String> departments,
    @Email(message = "{validation.email}") String mail,
    String zone) {} // @ValidEnumKey(enumType = EnumTypes.ZONE)
