package com.skf.scm.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SupplierRequest(
        @NotBlank(message = "supplierCode is required") String supplierCode,
        @NotBlank(message = "name is required") String name,
        @Email(message = "contactEmail must be valid") String contactEmail,
        String country,
        @NotNull @Min(value = 1, message = "leadTimeDays must be at least 1") Integer leadTimeDays
) {
}
