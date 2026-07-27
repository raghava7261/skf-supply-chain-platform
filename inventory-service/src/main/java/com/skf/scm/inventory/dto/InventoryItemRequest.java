package com.skf.scm.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InventoryItemRequest(
        @NotBlank String skuCode,
        String skuDescription,
        @NotBlank String warehouseCode,
        @NotNull @Min(0) Integer quantity,
        @NotNull @Min(0) Integer reorderThreshold,
        @NotNull @Min(1) Integer reorderQuantity,
        String preferredSupplierCode
) {
}
