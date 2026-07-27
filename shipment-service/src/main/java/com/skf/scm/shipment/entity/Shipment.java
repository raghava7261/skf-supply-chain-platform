package com.skf.scm.shipment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "shipments", uniqueConstraints = @UniqueConstraint(columnNames = "shipment_number"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shipment_number", nullable = false, length = 30)
    private String shipmentNumber;

    /** Links back to the purchase order this shipment fulfills, e.g. "PO-AB12CD34". */
    @Column(name = "po_number")
    private String poNumber;

    @Column(nullable = false)
    private String carrier;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "origin_warehouse")
    private String originWarehouse;

    @Column(nullable = false)
    private String destination;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ShipmentStatus status = ShipmentStatus.CREATED;

    @Column(name = "estimated_delivery")
    private Instant estimatedDelivery;

    @Column(name = "actual_delivery")
    private Instant actualDelivery;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /** Used by the scheduled delay-detection sweep — flags shipments running past their ETA. */
    public boolean isOverdue() {
        return status == ShipmentStatus.IN_TRANSIT
                && estimatedDelivery != null
                && Instant.now().isAfter(estimatedDelivery);
    }
}
