package com.gastroblue.model.request;

import com.gastroblue.model.enums.Department;
import com.gastroblue.model.enums.Zone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UserUpdateRequest(
    @NotNull(message = "{validation.department.check.null}") List<Department> departments,
    @Email(message = "{validation.email}") String mail,
    Zone zone) {}
