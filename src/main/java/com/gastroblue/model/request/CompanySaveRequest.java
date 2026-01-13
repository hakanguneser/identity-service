package com.gastroblue.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gastroblue.model.enums.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CompanySaveRequest(
    @NotNull(message = "{validation.companyName.check.null}") String companyName,
    @NotNull(message = "{validation.companyCode.check.null}") String companyCode,
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
                @Email(message = "{validation.email}") String>
            companyMail,
    @JsonIgnore Boolean isActive) {
  public CompanySaveRequest {
    if (isActive == null) {
      isActive = Boolean.TRUE;
    }
  }
}
