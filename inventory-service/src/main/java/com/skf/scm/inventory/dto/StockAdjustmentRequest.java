package com.skf.scm.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * delta can be positive (receipt, correction upward) or negative (pick, damage, correction downward).
 */
public record StockAdjustmentRequest(
        @NotNull Integer delta,
        @NotBlank String reason
) {
}
