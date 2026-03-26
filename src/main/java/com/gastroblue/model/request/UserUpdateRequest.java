package com.gastroblue.model.request;

import com.gastroblue.annotations.validation.field.enumkey.ValidEnumKey;
import com.gastroblue.model.enums.EnumTypes;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UserUpdateRequest(
    @NotNull(message = "{validation.department.check.null}")
        List<@ValidEnumKey(enumType = EnumTypes.DEPARTMENT, productScoped = true) String>
            departments,
    @Email(message = "{validation.email}") String mail,
    @ValidEnumKey(enumType = EnumTypes.ZONE) String zone) {}
