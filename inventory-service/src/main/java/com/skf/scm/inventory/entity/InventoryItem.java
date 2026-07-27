package com.skf.scm.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * A single SKU's stock record at a single warehouse. This is the row that
 * would previously have lived in a warehouse-local spreadsheet — moving it
 * here with a unique (skuCode, warehouseCode) constraint is what eliminates
 * the "42 in one system, 4 in reality" discrepancy across locations.
 */
@Entity
@Table(name = "inventory_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"sku_code", "warehouse_code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku_code", nullable = false, length = 40)
    private String skuCode;

    @Column(name = "sku_description")
    private String skuDescription;

    @Column(name = "warehouse_code", nullable = false, length = 20)
    private String warehouseCode;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    /** Quantity at/below which a LowStockDetectedEvent is fired. */
    @Column(name = "reorder_threshold", nullable = false)
    @Builder.Default
    private Integer reorderThreshold = 10;

    /** Suggested quantity to reorder once the threshold is crossed. */
    @Column(name = "reorder_quantity", nullable = false)
    @Builder.Default
    private Integer reorderQuantity = 50;

    @Column(name = "preferred_supplier_code")
    private String preferredSupplierCode;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        this.updatedAt = Instant.now();
    }

    public boolean isBelowThreshold() {
        return this.quantity <= this.reorderThreshold;
    }
}
