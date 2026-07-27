package com.skf.scm.shipment.controller;

import com.skf.scm.shipment.dto.ShipmentRequest;
import com.skf.scm.shipment.dto.ShipmentResponse;
import com.skf.scm.shipment.entity.ShipmentStatus;
import com.skf.scm.shipment.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<ShipmentResponse> create(@Valid @RequestBody ShipmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shipmentService.createShipment(request));
    }

    @GetMapping
    public ResponseEntity<List<ShipmentResponse>> getAll(@RequestParam(required = false) ShipmentStatus status) {
        return ResponseEntity.ok(status != null ? shipmentService.getByStatus(status) : shipmentService.getAll());
    }

    @GetMapping("/{shipmentNumber}")
    public ResponseEntity<ShipmentResponse> getOne(@PathVariable String shipmentNumber) {
        return ResponseEntity.ok(shipmentService.getByShipmentNumber(shipmentNumber));
    }

    @PostMapping("/{shipmentNumber}/in-transit")
    public ResponseEntity<ShipmentResponse> markInTransit(@PathVariable String shipmentNumber) {
        return ResponseEntity.ok(shipmentService.markInTransit(shipmentNumber));
    }

    @PostMapping("/{shipmentNumber}/delivered")
    public ResponseEntity<ShipmentResponse> markDelivered(@PathVariable String shipmentNumber) {
        return ResponseEntity.ok(shipmentService.markDelivered(shipmentNumber));
    }

    @PostMapping("/{shipmentNumber}/cancel")
    public ResponseEntity<ShipmentResponse> cancel(@PathVariable String shipmentNumber) {
        return ResponseEntity.ok(shipmentService.cancelShipment(shipmentNumber));
    }
}
