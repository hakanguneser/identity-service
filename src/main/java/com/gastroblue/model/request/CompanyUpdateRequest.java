package com.gastroblue.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record CompanyUpdateRequest(
    @NotNull(message = "{validation.companyName.check.null}") String companyName,
    String zone,
    String country,
    String city,
    String segment1,
    String segment2,
    String segment3,
    String segment4,
    String segment5,
    @Valid
        List<
                @NotBlank(message = "validation.email.check.blank")
                @Email(message = "{validation.email}")
                @Pattern(regexp = "^[^A-Z\\s]+$", message = "{validation.email.lowercase}") String>
            companyMail) {}
