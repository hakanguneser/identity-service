package com.gastroblue.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CompanyGroupUpdateRequest(
    @NotBlank(message = "{validation.companyGroupName.check.blank}") String name,
    @NotBlank(message = "{validation.companyGroupCode.check.blank}") String groupCode,
    @Valid
        List<
                @NotBlank(message = "validation.email.check.blank")
                @Email(message = "{validation.email}") String>
            groupMails,
    String logoUrl,
    String thermometerTrackerApiUrl,
    String thermometerTrackerApiVersion,
    Boolean thermometerTrackerEnabled,
    String formflowApiUrl,
    String formflowApiVersion,
    Boolean formflowEnabled) {}
