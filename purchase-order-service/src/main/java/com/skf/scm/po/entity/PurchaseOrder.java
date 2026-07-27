package com.skf.scm.po.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "purchase_orders", uniqueConstraints = @UniqueConstraint(columnNames = "po_number"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "po_number", nullable = false, length = 30)
    private String poNumber;

    @Column(name = "supplier_code", nullable = false)
    private String supplierCode;

    @Column(name = "sku_code", nullable = false)
    private String skuCode;

    @Column(name = "warehouse_code", nullable = false)
    private String warehouseCode;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_cost", precision = 12, scale = 2)
    private BigDecimal unitCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PurchaseOrderStatus status = PurchaseOrderStatus.DRAFT;

    /** True when this PO was created automatically from a LowStockDetectedEvent rather than by a human. */
    @Column(name = "auto_triggered", nullable = false)
    @Builder.Default
    private Boolean autoTriggered = false;

    @Column(name = "expected_delivery_date")
    private Instant expectedDeliveryDate;

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
}
