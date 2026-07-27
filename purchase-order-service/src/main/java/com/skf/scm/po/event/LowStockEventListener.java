package com.skf.scm.po.event;

import com.skf.scm.common.event.KafkaTopics;
import com.skf.scm.common.event.LowStockDetectedEvent;
import com.skf.scm.po.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * This is the automation that directly prevents the "missed reorder window"
 * failure mode from the original problem statement: instead of relying on a
 * human to notice a threshold breach, inventory-service's LOW_STOCK_DETECTED
 * event is consumed here and a DRAFT purchase order is created automatically,
 * ready for a procurement manager to approve.
 *
 * A placeholder unit cost is used here because pricing isn't owned by this
 * event — in a fuller build this would call supplier-service's pricing
 * endpoint (or a dedicated pricing/catalog service) before drafting the PO.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LowStockEventListener {

    private final PurchaseOrderService purchaseOrderService;

    private static final BigDecimal PLACEHOLDER_UNIT_COST = BigDecimal.valueOf(25.00);
    private static final int DEFAULT_LEAD_TIME_DAYS = 10;

    @KafkaListener(topics = KafkaTopics.LOW_STOCK_DETECTED, groupId = "purchase-order-service")
    public void onLowStockDetected(LowStockDetectedEvent event) {
        log.warn("LOW_STOCK_DETECTED received: sku={} warehouse={} qty={} threshold={}",
                event.skuCode(), event.warehouseCode(), event.currentQuantity(), event.reorderThreshold());

        if (event.preferredSupplierCode() == null || event.preferredSupplierCode().isBlank()) {
            log.error("Cannot auto-create PO for sku={} at warehouse={}: no preferred supplier configured",
                    event.skuCode(), event.warehouseCode());
            return;
        }

        try {
            purchaseOrderService.autoCreateFromLowStock(
                    event.preferredSupplierCode(),
                    event.skuCode(),
                    event.warehouseCode(),
                    event.suggestedReorderQuantity(),
                    PLACEHOLDER_UNIT_COST,
                    DEFAULT_LEAD_TIME_DAYS
            );
        } catch (Exception ex) {
            // Roadmap: route to a dead-letter topic for manual procurement follow-up
            // instead of silently dropping a failed auto-reorder attempt.
            log.error("Failed to auto-create PO for sku={} at warehouse={}: {}",
                    event.skuCode(), event.warehouseCode(), ex.getMessage(), ex);
        }
    }
}
