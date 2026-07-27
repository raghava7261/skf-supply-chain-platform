package com.skf.scm.shipment.service;

import com.skf.scm.shipment.dto.ShipmentRequest;
import com.skf.scm.shipment.dto.ShipmentResponse;
import com.skf.scm.shipment.entity.Shipment;
import com.skf.scm.shipment.entity.ShipmentStatus;
import com.skf.scm.shipment.event.ShipmentEventPublisher;
import com.skf.scm.shipment.exception.ShipmentExceptions.InvalidShipmentStateException;
import com.skf.scm.shipment.exception.ShipmentExceptions.ShipmentNotFoundException;
import com.skf.scm.shipment.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ShipmentService {

    private final ShipmentRepository repository;
    private final ShipmentEventPublisher eventPublisher;

    public ShipmentResponse createShipment(ShipmentRequest request) {
        Shipment shipment = Shipment.builder()
                .shipmentNumber("SHP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .poNumber(request.poNumber())
                .carrier(request.carrier())
                .trackingNumber(request.trackingNumber())
                .originWarehouse(request.originWarehouse())
                .destination(request.destination())
                .estimatedDelivery(request.estimatedDelivery())
                .status(ShipmentStatus.CREATED)
                .build();

        Shipment saved = repository.save(shipment);
        eventPublisher.publishStatusUpdated(saved, null);
        return ShipmentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getByShipmentNumber(String shipmentNumber) {
        return ShipmentResponse.from(findOrThrow(shipmentNumber));
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponse> getAll() {
        return repository.findAll().stream().map(ShipmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponse> getByStatus(ShipmentStatus status) {
        return repository.findByStatus(status).stream().map(ShipmentResponse::from).toList();
    }

    public ShipmentResponse markInTransit(String shipmentNumber) {
        return transitionStatus(shipmentNumber, ShipmentStatus.CREATED, ShipmentStatus.IN_TRANSIT);
    }

    public ShipmentResponse markDelivered(String shipmentNumber) {
        Shipment shipment = findOrThrow(shipmentNumber);
        if (shipment.getStatus() != ShipmentStatus.IN_TRANSIT && shipment.getStatus() != ShipmentStatus.DELAYED) {
            throw new InvalidShipmentStateException(shipmentNumber, shipment.getStatus().name(), "deliver");
        }
        String previous = shipment.getStatus().name();
        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setActualDelivery(Instant.now());
        Shipment saved = repository.save(shipment);
        eventPublisher.publishStatusUpdated(saved, previous);
        return ShipmentResponse.from(saved);
    }

    public ShipmentResponse cancelShipment(String shipmentNumber) {
        Shipment shipment = findOrThrow(shipmentNumber);
        if (shipment.getStatus() == ShipmentStatus.DELIVERED) {
            throw new InvalidShipmentStateException(shipmentNumber, shipment.getStatus().name(), "cancel");
        }
        String previous = shipment.getStatus().name();
        shipment.setStatus(ShipmentStatus.CANCELLED);
        Shipment saved = repository.save(shipment);
        eventPublisher.publishStatusUpdated(saved, previous);
        return ShipmentResponse.from(saved);
    }

    /**
     * Runs every 5 minutes and flags any IN_TRANSIT shipment that's past its
     * estimated delivery date as DELAYED, firing an event so the dashboard
     * and procurement see it before the client has to call and ask where
     * their order is — this is the proactive-visibility half of the
     * platform's original "flying blind" problem statement.
     */
    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public void detectDelayedShipments() {
        List<Shipment> inTransit = repository.findByStatus(ShipmentStatus.IN_TRANSIT);
        for (Shipment shipment : inTransit) {
            if (shipment.isOverdue()) {
                String previous = shipment.getStatus().name();
                shipment.setStatus(ShipmentStatus.DELAYED);
                Shipment saved = repository.save(shipment);
                eventPublisher.publishStatusUpdated(saved, previous);
                log.warn("Shipment {} flagged DELAYED (ETA {} has passed)",
                        shipment.getShipmentNumber(), shipment.getEstimatedDelivery());
            }
        }
    }

    private ShipmentResponse transitionStatus(String shipmentNumber, ShipmentStatus expected, ShipmentStatus next) {
        Shipment shipment = findOrThrow(shipmentNumber);
        if (shipment.getStatus() != expected) {
            throw new InvalidShipmentStateException(shipmentNumber, shipment.getStatus().name(), "transition to " + next);
        }
        String previous = shipment.getStatus().name();
        shipment.setStatus(next);
        Shipment saved = repository.save(shipment);
        eventPublisher.publishStatusUpdated(saved, previous);
        return ShipmentResponse.from(saved);
    }

    private Shipment findOrThrow(String shipmentNumber) {
        return repository.findByShipmentNumber(shipmentNumber)
                .orElseThrow(() -> new ShipmentNotFoundException(shipmentNumber));
    }
}
