package com.gastroblue.model.request;

import com.gastroblue.model.enums.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record CompanyUpdateRequest(
    @NotNull(message = "{validation.companyName.check.null}") String companyName,
    Zone zone,
    Country country,
    City city,
    CompanySegment1Values segment1,
    CompanySegment2Values segment2,
    CompanySegment3Values segment3,
    CompanySegment4Values segment4,
    CompanySegment5Values segment5,
    @Valid
        List<
                @NotBlank(message = "validation.email.check.blank")
                @Email(message = "{validation.email}")
                @Pattern(regexp = "^[^A-Z\\s]+$", message = "{validation.email.lowercase}") String>
            companyMail) {}
