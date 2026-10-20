package com.gastroblue.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UserUpdateRequest(
    @NotNull(message = "{validation.department.check.null}")
        List<String>
            departments, // @ValidEnumKey(enumType = EnumTypes.DEPARTMENT, productScoped = true)
    @Email(message = "{validation.email}") String mail,
    String zone) {} // @ValidEnumKey(enumType = EnumTypes.ZONE)
