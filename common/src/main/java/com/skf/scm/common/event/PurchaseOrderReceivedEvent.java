package com.skf.scm.common.event;

import java.io.Serializable;
import java.time.Instant;

/**
 * Emitted by purchase-order-service on the PO_RECEIVED topic when a shipment
 * against a PO is confirmed received at a warehouse. Consumed by
 * inventory-service to increment on-hand stock for the SKU.
 */
public record PurchaseOrderReceivedEvent(
        String poNumber,
        String skuCode,
        String warehouseCode,
        int quantityReceived,
        Instant occurredAt
) implements Serializable {
}
