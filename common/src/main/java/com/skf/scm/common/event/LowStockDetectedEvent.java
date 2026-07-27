package com.skf.scm.common.event;

import java.io.Serializable;
import java.time.Instant;

/**
 * Emitted by inventory-service on the LOW_STOCK_DETECTED topic when a SKU's
 * quantity at a warehouse drops at or below its configured reorder threshold.
 * Consumed by purchase-order-service to auto-draft a PO, and by
 * notification-service to alert procurement.
 */
public record LowStockDetectedEvent(
        String skuCode,
        String warehouseCode,
        int currentQuantity,
        int reorderThreshold,
        int suggestedReorderQuantity,
        String preferredSupplierCode,
        Instant occurredAt
) implements Serializable {
}
