package com.gastroblue.model.request;

import com.gastroblue.annotations.validation.field.phone.ValidPhoneNumber;
import com.gastroblue.commons.helper.lookup.validation.ValidLookup;
import com.gastroblue.commons.shared.enums.ApplicationProduct;
import com.gastroblue.commons.shared.enums.ApplicationRole;
import com.gastroblue.model.enums.Lookups;
import jakarta.validation.constraints.*;
import java.util.List;

public record UserSaveRequest(
    @Size(min = 5, max = 100, message = "{validation.username.size.5.100}")
        @NotBlank(message = "{validation.username.check.null}")
        @Pattern(
            regexp = "^[a-zA-Z0-9](?:[a-zA-Z0-9._-]{3,98}[a-zA-Z0-9])?$",
            message = "{validation.username.invalid.format}")
        String username,
    String companyGroupId,
    String companyId,
    @NotNull(message = "{validation.applicationRole.check.null}") ApplicationRole applicationRole,
    @NotNull(message = "{validation.department.check.null}")
        List<@ValidLookup(lookup = Lookups.class, constant = "DEPARTMENT") String> departments,
    @Size(min = 3, max = 100, message = "{validation.name.size.3.100}") String name,
    @Size(min = 3, max = 100, message = "{validation.surname.size.3.100}")
        @NotBlank(message = "{validation.surname.check.null}")
        String surname,
    @ValidPhoneNumber String phone,
    @Email(message = "{validation.email}") String email,
    @ValidLookup(lookup = Lookups.class, constant = "GENDER") String gender,
    @ValidLookup(lookup = Lookups.class, constant = "ZONE") String zone,
    ApplicationProduct product) {}
