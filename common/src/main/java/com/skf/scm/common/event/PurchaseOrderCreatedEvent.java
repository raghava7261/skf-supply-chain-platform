package com.skf.scm.common.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Emitted by purchase-order-service on the PO_CREATED topic, whether the PO
 * was created manually by procurement or auto-triggered by a low-stock event.
 * Consumed by notification-service and supplier-service (for lead-time tracking).
 */
public record PurchaseOrderCreatedEvent(
        String poNumber,
        String supplierCode,
        String skuCode,
        String warehouseCode,
        int quantity,
        BigDecimal unitCost,
        boolean autoTriggered,
        Instant expectedDeliveryDate,
        Instant occurredAt
) implements Serializable {
}
