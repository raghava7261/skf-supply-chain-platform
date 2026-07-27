package com.skf.scm.common.event;

import java.io.Serializable;
import java.time.Instant;

/**
 * Emitted by warehouse-ops-service on the WAREHOUSE_TASK_COMPLETED topic when
 * a pick, pack, or put-away task finishes. Consumed by inventory-service to
 * adjust stock: PICK decreases quantity, PUTAWAY increases it. PACK does not
 * change quantity (it's a staging step between pick and shipment).
 */
public record WarehouseTaskCompletedEvent(
        String taskId,
        String taskType,      // PICK, PACK, PUTAWAY
        String skuCode,
        String warehouseCode,
        String binLocation,
        int quantity,
        Instant occurredAt
) implements Serializable {
}
