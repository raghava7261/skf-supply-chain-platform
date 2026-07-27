package com.skf.scm.shipment.dto;

import com.skf.scm.shipment.entity.Shipment;
import com.skf.scm.shipment.entity.ShipmentStatus;

import java.time.Instant;

public record ShipmentResponse(
        Long id,
        String shipmentNumber,
        String poNumber,
        String carrier,
        String trackingNumber,
        String originWarehouse,
        String destination,
        ShipmentStatus status,
        Instant estimatedDelivery,
        Instant actualDelivery,
        boolean overdue,
        Instant createdAt
) {
    public static ShipmentResponse from(Shipment s) {
        return new ShipmentResponse(
                s.getId(), s.getShipmentNumber(), s.getPoNumber(), s.getCarrier(), s.getTrackingNumber(),
                s.getOriginWarehouse(), s.getDestination(), s.getStatus(), s.getEstimatedDelivery(),
                s.getActualDelivery(), s.isOverdue(), s.getCreatedAt()
        );
    }
}
