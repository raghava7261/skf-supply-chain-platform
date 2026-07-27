package com.skf.scm.supplier.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "suppliers", uniqueConstraints = @UniqueConstraint(columnNames = "supplier_code"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supplier_code", nullable = false, length = 20)
    private String supplierCode;

    @Column(nullable = false)
    private String name;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "country")
    private String country;

    /** Average number of days between PO placement and delivery, used by AI forecasting. */
    @Column(name = "lead_time_days", nullable = false)
    private Integer leadTimeDays;

    /** 0-100 reliability score based on historical on-time delivery %. */
    @Column(name = "reliability_score")
    @Builder.Default
    private Double reliabilityScore = 100.0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

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
