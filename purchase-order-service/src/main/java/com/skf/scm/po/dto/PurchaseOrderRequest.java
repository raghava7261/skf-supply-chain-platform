package com.skf.scm.po.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PurchaseOrderRequest(
        @NotBlank String supplierCode,
        @NotBlank String skuCode,
        @NotBlank String warehouseCode,
        @NotNull @Min(1) Integer quantity,
        @NotNull BigDecimal unitCost,
        Integer expectedLeadTimeDays
) {
}
