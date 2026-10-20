package com.gastroblue.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record CompanyUpdateRequest(
    @NotNull(message = "{validation.companyName.check.null}") String companyName,
    String zone, // @ValidEnumKey(enumType = EnumTypes.ZONE)
    String country, // @ValidEnumKey(enumType = EnumTypes.COUNTRY)
    String city, // @ValidEnumKey(enumType = EnumTypes.CITY)
    String segment1, // @ValidEnumKey(enumType = EnumTypes.SEGMENT_1)
    String segment2, // @ValidEnumKey(enumType = EnumTypes.SEGMENT_2)
    String segment3, // @ValidEnumKey(enumType = EnumTypes.SEGMENT_3)
    String segment4, // @ValidEnumKey(enumType = EnumTypes.SEGMENT_4)
    String segment5, // @ValidEnumKey(enumType = EnumTypes.SEGMENT_5)
    @Valid
        List<
                @NotBlank(message = "validation.email.check.blank")
                @Email(message = "{validation.email}")
                @Pattern(regexp = "^[^A-Z\\s]+$", message = "{validation.email.lowercase}") String>
            companyMail) {}
