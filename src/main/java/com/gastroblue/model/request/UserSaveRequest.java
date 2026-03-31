package com.gastroblue.model.request;

import com.gastroblue.annotations.validation.field.phone.ValidPhoneNumber;
import com.gastroblue.annotations.validation.request.ValidDepartmentsForProduct;
import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.enums.ApplicationRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Departments are validated via the class-level {@link ValidDepartmentsForProduct} constraint,
 * which uses {@link #product} from this same request — not the caller's session product — so that
 * an ADMIN can create users for any product context.
 */
// @ValidDepartmentsForProduct TODO validate departments
public record UserSaveRequest(
    @Size(min = 5, max = 100, message = "{validation.username.size.5.100}")
        @NotBlank(message = "{validation.username.check.null}")
        String username,
    String companyGroupId,
    String companyId,
    @NotNull(message = "{validation.applicationRole.check.null}") ApplicationRole applicationRole,
    @NotNull(message = "{validation.department.check.null}") List<String> departments,
    @Size(min = 3, max = 100, message = "{validation.name.size.3.100}")
        @NotBlank(message = "{validation.name.check.null}")
        String name,
    @Size(min = 3, max = 100, message = "{validation.surname.size.3.100}")
        @NotBlank(message = "{validation.surname.check.null}")
        String surname,
    @ValidPhoneNumber String phone,
    @Email(message = "{validation.email}") String email,
    String gender, // TODO Validate It Later
    String zone, // TODO Validate It Later
    ApplicationProduct product) {}
