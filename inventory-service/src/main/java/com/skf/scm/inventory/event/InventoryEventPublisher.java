package com.skf.scm.inventory.event;

import com.skf.scm.common.event.KafkaTopics;
import com.skf.scm.common.event.LowStockDetectedEvent;
import com.skf.scm.common.event.StockUpdatedEvent;
import com.skf.scm.inventory.entity.InventoryItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishStockUpdated(String skuCode, String warehouseCode,
                                     int previousQuantity, int newQuantity, String reason) {
        StockUpdatedEvent event = new StockUpdatedEvent(
                skuCode, warehouseCode, previousQuantity, newQuantity, reason, Instant.now());
        // Key by sku+warehouse so all updates for the same stock record stay ordered.
        kafkaTemplate.send(KafkaTopics.STOCK_UPDATED, skuCode + ":" + warehouseCode, event);
        log.info("Published STOCK_UPDATED: {} @ {} {} -> {}", skuCode, warehouseCode, previousQuantity, newQuantity);
    }

    public void publishLowStockDetected(InventoryItem item) {
        LowStockDetectedEvent event = new LowStockDetectedEvent(
                item.getSkuCode(),
                item.getWarehouseCode(),
                item.getQuantity(),
                item.getReorderThreshold(),
                item.getReorderQuantity(),
                item.getPreferredSupplierCode(),
                Instant.now()
        );
        kafkaTemplate.send(KafkaTopics.LOW_STOCK_DETECTED, item.getSkuCode() + ":" + item.getWarehouseCode(), event);
        log.warn("Published LOW_STOCK_DETECTED: {} @ {} qty={} threshold={}",
                item.getSkuCode(), item.getWarehouseCode(), item.getQuantity(), item.getReorderThreshold());
    }
}
