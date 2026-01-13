package com.gastroblue.model.request;

import com.gastroblue.annotations.validation.field.phone.ValidPhoneNumber;
import com.gastroblue.model.enums.Department;
import com.gastroblue.model.enums.Language;
import com.gastroblue.model.enums.Zone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UserUpdateRequest(
    @NotNull(message = "{validation.department.check.null}") List<Department> departments,
    @NotNull(message = "{validation.username.check.null}") Language language,
    @ValidPhoneNumber String phone,
    @Email(message = "{validation.email}") String mail,
    Zone zone) {}
