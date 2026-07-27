package com.skf.scm.warehouse.dto;

import com.skf.scm.warehouse.entity.TaskType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WarehouseTaskRequest(
        @NotNull TaskType taskType,
        @NotBlank String skuCode,
        @NotBlank String warehouseCode,
        @NotBlank String binLocation,
        @NotNull @Min(1) Integer quantity,
        String referenceId,
        String assignedTo
) {
}
