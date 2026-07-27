package com.skf.scm.common.event;

import java.io.Serializable;
import java.time.Instant;

/**
 * Emitted by inventory-service on the STOCK_UPDATED topic every time a SKU's
 * on-hand quantity changes at a warehouse (receipt, pick, adjustment, transfer).
 * Consumed by notification-service (dashboard feed) and any analytics/AI service.
 */
public record StockUpdatedEvent(
        String skuCode,
        String warehouseCode,
        int previousQuantity,
        int newQuantity,
        String changeReason,   // e.g. "PO_RECEIVED", "ORDER_PICKED", "MANUAL_ADJUSTMENT"
        Instant occurredAt
) implements Serializable {
}
