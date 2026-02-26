package com.gastroblue.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record CompanyGroupUpdateRequest(
    @NotBlank(message = "{validation.companyGroupName.check.blank}") String name,
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
