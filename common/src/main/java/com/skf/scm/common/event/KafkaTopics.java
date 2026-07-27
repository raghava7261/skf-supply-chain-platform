package com.skf.scm.common.event;

/**
 * Central registry of Kafka topic names used across the SCM platform.
 * Keeping these as constants in the shared module avoids typo-drift
 * between producer and consumer services.
 */
public final class KafkaTopics {

    private KafkaTopics() {
    }

    /** Published by inventory-service whenever stock quantity changes at any warehouse. */
    public static final String STOCK_UPDATED = "scm.stock.updated";

    /** Published by inventory-service when a SKU's quantity crosses below its reorder threshold. */
    public static final String LOW_STOCK_DETECTED = "scm.stock.low-stock-detected";

    /** Published by purchase-order-service when a new PO is created (manually or auto-triggered). */
    public static final String PO_CREATED = "scm.po.created";

    /** Published by purchase-order-service when a PO is approved and ready to send to supplier. */
    public static final String PO_APPROVED = "scm.po.approved";

    /** Published by purchase-order-service when a PO is fulfilled/received into inventory. */
    public static final String PO_RECEIVED = "scm.po.received";

    /** Published by supplier-service when a new supplier is onboarded. */
    public static final String SUPPLIER_REGISTERED = "scm.supplier.registered";

    /** Published by warehouse-ops-service when a pick/pack/put-away task completes. */
    public static final String WAREHOUSE_TASK_COMPLETED = "scm.warehouse.task-completed";

    /** Published by shipment-service whenever a shipment's status changes. */
    public static final String SHIPMENT_STATUS_UPDATED = "scm.shipment.status-updated";
}
