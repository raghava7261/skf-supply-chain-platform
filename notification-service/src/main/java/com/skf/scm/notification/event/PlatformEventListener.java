package com.skf.scm.notification.event;

import com.skf.scm.common.event.*;
import com.skf.scm.notification.model.NotificationEvent;
import com.skf.scm.notification.model.NotificationEvent.NotificationType;
import com.skf.scm.notification.model.NotificationEvent.Severity;
import com.skf.scm.notification.store.NotificationStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlatformEventListener {

    private final NotificationStore store;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = KafkaTopics.LOW_STOCK_DETECTED, groupId = "notification-service")
    public void onLowStock(LowStockDetectedEvent event) {
        publish(new NotificationEvent(
                UUID.randomUUID().toString(),
                NotificationType.LOW_STOCK,
                Severity.CRITICAL,
                "Low stock: %s at %s (qty %d <= threshold %d) — auto-reorder in progress".formatted(
                        event.skuCode(), event.warehouseCode(), event.currentQuantity(), event.reorderThreshold()),
                event,
                Instant.now()
        ));
    }

    @KafkaListener(topics = KafkaTopics.STOCK_UPDATED, groupId = "notification-service")
    public void onStockUpdated(StockUpdatedEvent event) {
        publish(new NotificationEvent(
                UUID.randomUUID().toString(),
                NotificationType.STOCK_UPDATED,
                Severity.INFO,
                "Stock updated: %s at %s (%d -> %d) [%s]".formatted(
                        event.skuCode(), event.warehouseCode(), event.previousQuantity(), event.newQuantity(), event.changeReason()),
                event,
                Instant.now()
        ));
    }

    @KafkaListener(topics = KafkaTopics.PO_CREATED, groupId = "notification-service")
    public void onPoCreated(PurchaseOrderCreatedEvent event) {
        publish(new NotificationEvent(
                UUID.randomUUID().toString(),
                NotificationType.PO_CREATED,
                event.autoTriggered() ? Severity.WARNING : Severity.INFO,
                "%sPO %s created: %d x %s from supplier %s".formatted(
                        event.autoTriggered() ? "[AUTO] " : "", event.poNumber(), event.quantity(), event.skuCode(), event.supplierCode()),
                event,
                Instant.now()
        ));
    }

    @KafkaListener(topics = KafkaTopics.SUPPLIER_REGISTERED, groupId = "notification-service")
    public void onSupplierRegistered(Object event) {
        publish(new NotificationEvent(
                UUID.randomUUID().toString(),
                NotificationType.SUPPLIER_REGISTERED,
                Severity.INFO,
                "New supplier registered",
                event,
                Instant.now()
        ));
    }

    @KafkaListener(topics = KafkaTopics.WAREHOUSE_TASK_COMPLETED, groupId = "notification-service")
    public void onWarehouseTaskCompleted(WarehouseTaskCompletedEvent event) {
        publish(new NotificationEvent(
                UUID.randomUUID().toString(),
                NotificationType.WAREHOUSE_TASK,
                Severity.INFO,
                "%s completed: %s at %s (qty %d, bin %s)".formatted(
                        event.taskType(), event.skuCode(), event.warehouseCode(), event.quantity(), event.binLocation()),
                event,
                Instant.now()
        ));
    }

    @KafkaListener(topics = KafkaTopics.SHIPMENT_STATUS_UPDATED, groupId = "notification-service")
    public void onShipmentStatusUpdated(ShipmentStatusUpdatedEvent event) {
        Severity severity = "DELAYED".equals(event.newStatus()) ? Severity.CRITICAL : Severity.INFO;
        publish(new NotificationEvent(
                UUID.randomUUID().toString(),
                NotificationType.SHIPMENT_STATUS,
                severity,
                "Shipment %s: %s -> %s (carrier %s)".formatted(
                        event.shipmentNumber(), event.previousStatus(), event.newStatus(), event.carrier()),
                event,
                Instant.now()
        ));
    }

    private void publish(NotificationEvent event) {
        store.add(event);
        messagingTemplate.convertAndSend("/topic/alerts", event);
        log.info("[{}] {}", event.severity(), event.message());
    }
}
