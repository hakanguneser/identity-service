package com.gastroblue.model.request;

import com.gastroblue.model.enums.ApplicationProduct;
import jakarta.validation.constraints.NotNull;

public record CompanyGroupProductSaveRequest(
    @NotNull(message = "{validation.product.check.null}") ApplicationProduct product,
    String apiUrl,
    String apiVersion,
    String notes) {}
