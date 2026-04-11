package com.gastroblue.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record CompanySaveRequest(
    @NotBlank(message = "{validation.companyName.check.null}") String companyName,
    @NotBlank(message = "{validation.companyCode.check.blank}")
        @Pattern(regexp = "^[A-Z0-9_]+$", message = "{validation.companyCode.pattern}")
        String companyCode,
    String zone, // TODO Validate it later
    String country, // TODO Validate it later
    String city, // TODO Validate it later
    String segment1, // TODO Validate it later
    String segment2, // TODO Validate it later
    String segment3, // TODO Validate it later
    String segment4, // TODO Validate it later
    String segment5, // TODO Validate it later
    @Valid
        List<
                @NotBlank(message = "validation.email.check.blank")
                @Email(message = "{validation.email}")
                @Pattern(regexp = "^[^A-Z\\s]+$", message = "{validation.email.lowercase}") String>
            companyMail,
    @JsonIgnore Boolean isActive) {
  public CompanySaveRequest {
    if (isActive == null) {
      isActive = Boolean.TRUE;
    }
  }
}
