package com.skf.scm.inventory.event;

import com.skf.scm.common.event.KafkaTopics;
import com.skf.scm.common.event.WarehouseTaskCompletedEvent;
import com.skf.scm.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * PICK tasks decrease stock, PUTAWAY tasks increase it. PACK doesn't change
 * quantity (items are already off the shelf once picked; packing just
 * stages them for shipment) so it's a no-op here by design.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WarehouseTaskCompletedListener {

    private final InventoryService inventoryService;

    @KafkaListener(topics = KafkaTopics.WAREHOUSE_TASK_COMPLETED, groupId = "inventory-service")
    public void onWarehouseTaskCompleted(WarehouseTaskCompletedEvent event) {
        log.info("Received WAREHOUSE_TASK_COMPLETED: {} [{}] sku={} qty={}",
                event.taskId(), event.taskType(), event.skuCode(), event.quantity());

        int delta = switch (event.taskType()) {
            case "PICK" -> -event.quantity();
            case "PUTAWAY" -> event.quantity();
            default -> 0; // PACK — no stock quantity impact
        };

        if (delta == 0) {
            return;
        }

        try {
            inventoryService.adjustStock(event.skuCode(), event.warehouseCode(), delta,
                    "WAREHOUSE_TASK:" + event.taskType() + ":" + event.taskId());
        } catch (Exception ex) {
            log.error("Failed to apply WAREHOUSE_TASK_COMPLETED event for task {}: {}",
                    event.taskId(), ex.getMessage(), ex);
        }
    }
}
