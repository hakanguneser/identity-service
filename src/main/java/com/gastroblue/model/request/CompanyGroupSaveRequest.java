package com.gastroblue.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record CompanyGroupSaveRequest(
    @NotBlank(message = "{validation.companyGroupName.check.blank}") String name,
    @NotBlank(message = "{validation.companyGroupCode.check.blank}")
        @Pattern(regexp = "^[A-Z0-9_]+$", message = "{validation.companyGroupCode.pattern}")
        String groupCode,
    @Valid
        List<
                @NotBlank(message = "validation.email.check.blank")
                @Email(message = "{validation.email}")
                @Pattern(regexp = "^[^A-Z\\s]+$", message = "{validation.email.lowercase}") String>
            groupMails,
    String logoUrl,
    String thermometerTrackerApiUrl,
    String thermometerTrackerApiVersion,
    Boolean thermometerTrackerEnabled,
    String formflowApiUrl,
    String formflowApiVersion,
    Boolean formflowEnabled,
    @Valid
        List<
                @Pattern(regexp = "^[^A-Z\\s]+$", message = "{validation.domain.lowercase}")
                @NotBlank(message = "{validation.domain.check.blank}") String>
            mailDomains) {}
