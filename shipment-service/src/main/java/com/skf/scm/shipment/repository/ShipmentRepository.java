package com.skf.scm.shipment.repository;

import com.skf.scm.shipment.entity.Shipment;
import com.skf.scm.shipment.entity.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByShipmentNumber(String shipmentNumber);
    List<Shipment> findByStatus(ShipmentStatus status);
    List<Shipment> findByPoNumber(String poNumber);
}
