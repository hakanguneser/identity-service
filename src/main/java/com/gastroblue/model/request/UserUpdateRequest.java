package com.gastroblue.model.request;

import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.enums.Department;
import com.gastroblue.model.enums.Zone;
import jakarta.validation.constraints.Email;
import java.util.List;

public record UserUpdateRequest(
    ApplicationProduct product,
    List<Department> departments,
    @Email(message = "{validation.email}") String mail,
    Zone zone) {}
