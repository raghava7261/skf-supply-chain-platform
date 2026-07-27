package com.skf.scm.notification.model;

import java.time.Instant;

/**
 * A single unified alert shape that every inbound Kafka event gets mapped
 * into. This is what the Live Dashboard feature actually consumes — one
 * consistent feed instead of the frontend needing to know about every
 * event type on the bus.
 */
public record NotificationEvent(
        String id,
        NotificationType type,
        Severity severity,
        String message,
        Object payload,
        Instant occurredAt
) {
    public enum NotificationType {
        LOW_STOCK, STOCK_UPDATED, PO_CREATED, SUPPLIER_REGISTERED, WAREHOUSE_TASK, SHIPMENT_STATUS
    }

    public enum Severity {
        INFO, WARNING, CRITICAL
    }
}
