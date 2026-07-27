package com.skf.scm.common.event;

import java.io.Serializable;
import java.time.Instant;

/**
 * Emitted by shipment-service on the SHIPMENT_STATUS_UPDATED topic any time
 * a shipment's status changes. Consumed by notification-service to power the
 * live dashboard's delivery/delay visibility.
 */
public record ShipmentStatusUpdatedEvent(
        String shipmentNumber,
        String poNumber,
        String carrier,
        String trackingNumber,
        String previousStatus,
        String newStatus,
        Instant estimatedDelivery,
        Instant occurredAt
) implements Serializable {
}
