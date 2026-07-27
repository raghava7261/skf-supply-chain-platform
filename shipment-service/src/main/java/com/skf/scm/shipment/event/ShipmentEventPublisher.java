package com.skf.scm.shipment.event;

import com.skf.scm.common.event.KafkaTopics;
import com.skf.scm.common.event.ShipmentStatusUpdatedEvent;
import com.skf.scm.shipment.entity.Shipment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishStatusUpdated(Shipment shipment, String previousStatus) {
        ShipmentStatusUpdatedEvent event = new ShipmentStatusUpdatedEvent(
                shipment.getShipmentNumber(), shipment.getPoNumber(), shipment.getCarrier(),
                shipment.getTrackingNumber(), previousStatus, shipment.getStatus().name(),
                shipment.getEstimatedDelivery(), Instant.now()
        );
        kafkaTemplate.send(KafkaTopics.SHIPMENT_STATUS_UPDATED, shipment.getShipmentNumber(), event);
        log.info("Published SHIPMENT_STATUS_UPDATED: {} {} -> {}",
                shipment.getShipmentNumber(), previousStatus, shipment.getStatus());
    }
}
