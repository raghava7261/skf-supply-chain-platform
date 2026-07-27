package com.skf.scm.inventory.event;

import com.skf.scm.common.event.KafkaTopics;
import com.skf.scm.common.event.PurchaseOrderReceivedEvent;
import com.skf.scm.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Closes the loop between purchase-order-service and inventory-service:
 * when a PO is marked received, stock is incremented automatically instead
 * of relying on a warehouse clerk to manually update a spreadsheet — this
 * is the exact gap that caused the original overselling incident.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderReceivedListener {

    private final InventoryService inventoryService;

    @KafkaListener(topics = KafkaTopics.PO_RECEIVED, groupId = "inventory-service")
    public void onPurchaseOrderReceived(PurchaseOrderReceivedEvent event) {
        log.info("Received PO_RECEIVED event for PO {}: sku={} qty={} warehouse={}",
                event.poNumber(), event.skuCode(), event.quantityReceived(), event.warehouseCode());
        try {
            inventoryService.adjustStock(
                    event.skuCode(),
                    event.warehouseCode(),
                    event.quantityReceived(),
                    "PO_RECEIVED:" + event.poNumber()
            );
        } catch (Exception ex) {
            // In production this would go to a dead-letter topic for manual reconciliation
            // rather than being swallowed — flagged here as a roadmap item.
            log.error("Failed to apply PO_RECEIVED event for PO {}: {}", event.poNumber(), ex.getMessage(), ex);
        }
    }
}
