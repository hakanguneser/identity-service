package com.gastroblue.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Set;

public record CompanyGroupSaveRequest(
    @NotBlank(message = "{validation.companyGroupName.check.blank}") String name,
    @NotBlank(message = "{validation.companyGroupCode.check.blank}")
        @Pattern(regexp = "^[A-Z0-9_]+$", message = "{validation.companyGroupCode.pattern}")
        String groupCode,
    @Valid
        Set<
                @NotBlank(message = "{validation.email.check.blank}")
                @Email(message = "{validation.email}")
                @Pattern(
                    regexp = "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$",
                    message = "{validation.email.lowercase}")
                String>
            groupMails,
    String logoUrl,
    @Valid
        Set<
                @NotBlank(message = "{validation.domain.check.blank}")
                @Pattern(
                    regexp = "^[a-z0-9.-]+\\.[a-z]{2,}$",
                    message = "{validation.domain.lowercase}")
                String>
            mailDomains) {}
