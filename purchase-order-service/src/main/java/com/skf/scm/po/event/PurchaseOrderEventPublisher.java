package com.skf.scm.po.event;

import com.skf.scm.common.event.KafkaTopics;
import com.skf.scm.common.event.PurchaseOrderCreatedEvent;
import com.skf.scm.common.event.PurchaseOrderReceivedEvent;
import com.skf.scm.po.entity.PurchaseOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPoCreated(PurchaseOrder po) {
        PurchaseOrderCreatedEvent event = new PurchaseOrderCreatedEvent(
                po.getPoNumber(), po.getSupplierCode(), po.getSkuCode(), po.getWarehouseCode(),
                po.getQuantity(), po.getUnitCost(), po.getAutoTriggered(), po.getExpectedDeliveryDate(), Instant.now()
        );
        kafkaTemplate.send(KafkaTopics.PO_CREATED, po.getPoNumber(), event);
        log.info("Published PO_CREATED: {} (autoTriggered={})", po.getPoNumber(), po.getAutoTriggered());
    }

    public void publishPoApproved(PurchaseOrder po) {
        kafkaTemplate.send(KafkaTopics.PO_APPROVED, po.getPoNumber(), po);
        log.info("Published PO_APPROVED: {}", po.getPoNumber());
    }

    public void publishPoReceived(PurchaseOrder po) {
        PurchaseOrderReceivedEvent event = new PurchaseOrderReceivedEvent(
                po.getPoNumber(), po.getSkuCode(), po.getWarehouseCode(), po.getQuantity(), Instant.now()
        );
        kafkaTemplate.send(KafkaTopics.PO_RECEIVED, po.getPoNumber(), event);
        log.info("Published PO_RECEIVED: {} -> inventory-service will increment stock", po.getPoNumber());
    }
}
