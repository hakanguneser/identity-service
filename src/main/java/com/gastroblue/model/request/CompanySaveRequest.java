package com.gastroblue.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gastroblue.commons.helper.lookup.validation.ValidLookup;
import com.gastroblue.model.enums.Lookups;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CompanySaveRequest(
    @NotBlank(message = "{validation.companyName.check.null}") String companyName,
    @NotBlank(message = "{validation.companyCode.check.blank}")
        @Size(min = 3, max = 20, message = "{validation.companyCode.size}")
        @Pattern(regexp = "^[A-Z0-9_]+$", message = "{validation.companyCode.pattern}")
        String companyCode,
    @ValidLookup(lookup = Lookups.class, constant = "ZONE") String zone,
    @ValidLookup(lookup = Lookups.class, constant = "COUNTRY") String country,
    @ValidLookup(lookup = Lookups.class, constant = "CITY") String city,
    @ValidLookup(lookup = Lookups.class, constant = "SEGMENT_1") String segment1,
    @ValidLookup(lookup = Lookups.class, constant = "SEGMENT_2") String segment2,
    @ValidLookup(lookup = Lookups.class, constant = "SEGMENT_3") String segment3,
    @ValidLookup(lookup = Lookups.class, constant = "SEGMENT_4") String segment4,
    @ValidLookup(lookup = Lookups.class, constant = "SEGMENT_5") String segment5,
    @Valid
        List<
                @NotBlank(message = "{validation.email.check.blank}")
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
