package com.skf.scm.warehouse.dto;

import com.skf.scm.warehouse.entity.TaskStatus;
import com.skf.scm.warehouse.entity.TaskType;
import com.skf.scm.warehouse.entity.WarehouseTask;

import java.time.Instant;

public record WarehouseTaskResponse(
        Long id,
        String taskId,
        TaskType taskType,
        String skuCode,
        String warehouseCode,
        String binLocation,
        Integer quantity,
        String scannedCode,
        String referenceId,
        TaskStatus status,
        String assignedTo,
        Instant createdAt,
        Instant completedAt
) {
    public static WarehouseTaskResponse from(WarehouseTask t) {
        return new WarehouseTaskResponse(
                t.getId(), t.getTaskId(), t.getTaskType(), t.getSkuCode(), t.getWarehouseCode(),
                t.getBinLocation(), t.getQuantity(), t.getScannedCode(), t.getReferenceId(),
                t.getStatus(), t.getAssignedTo(), t.getCreatedAt(), t.getCompletedAt()
        );
    }
}
