package com.gastroblue.model.request;

import com.gastroblue.model.enums.ApplicationProduct;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CompanyProductSaveRequest(
    @NotNull(message = "{validation.product.check.null}") ApplicationProduct product,
    @NotNull(message = "{validation.enabled.check.null}") Boolean enabled,
    LocalDate licenseExpiresAt,
    Integer agreedUserCount) {}
