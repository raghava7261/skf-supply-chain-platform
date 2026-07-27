package com.skf.scm.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * A single unit of floor work: pick a SKU from a bin, pack it, or put away
 * received stock. This is what replaces the paper pick-lists mentioned in
 * the original problem statement — barcode/QR scans (via the scannedCode
 * field) confirm the right SKU was actually touched, not just assumed.
 */
@Entity
@Table(name = "warehouse_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false, unique = true, length = 30)
    private String taskId;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 20)
    private TaskType taskType;

    @Column(name = "sku_code", nullable = false)
    private String skuCode;

    @Column(name = "warehouse_code", nullable = false)
    private String warehouseCode;

    @Column(name = "bin_location", nullable = false)
    private String binLocation;

    @Column(nullable = false)
    private Integer quantity;

    /** Barcode/QR value scanned by the worker to confirm the correct SKU/bin — populated on completion. */
    @Column(name = "scanned_code")
    private String scannedCode;

    /** Free-text reference to the originating order/PO, e.g. "PO-AB12CD34" or "SO-77291". */
    @Column(name = "reference_id")
    private String referenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}
