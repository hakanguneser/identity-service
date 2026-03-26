package com.gastroblue.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gastroblue.annotations.validation.field.enumkey.ValidEnumKey;
import com.gastroblue.model.enums.EnumTypes;
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
    @ValidEnumKey(enumType = EnumTypes.ZONE) String zone,
    @ValidEnumKey(enumType = EnumTypes.COUNTRY) String country,
    @ValidEnumKey(enumType = EnumTypes.CITY) String city,
    @ValidEnumKey(enumType = EnumTypes.SEGMENT_1) String segment1,
    @ValidEnumKey(enumType = EnumTypes.SEGMENT_2) String segment2,
    @ValidEnumKey(enumType = EnumTypes.SEGMENT_3) String segment3,
    @ValidEnumKey(enumType = EnumTypes.SEGMENT_4) String segment4,
    @ValidEnumKey(enumType = EnumTypes.SEGMENT_5) String segment5,
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
