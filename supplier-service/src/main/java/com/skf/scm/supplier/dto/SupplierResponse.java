package com.skf.scm.supplier.dto;

import com.skf.scm.supplier.entity.Supplier;

import java.time.Instant;

public record SupplierResponse(
        Long id,
        String supplierCode,
        String name,
        String contactEmail,
        String country,
        Integer leadTimeDays,
        Double reliabilityScore,
        Boolean active,
        Instant createdAt
) {
    public static SupplierResponse from(Supplier s) {
        return new SupplierResponse(
                s.getId(), s.getSupplierCode(), s.getName(), s.getContactEmail(),
                s.getCountry(), s.getLeadTimeDays(), s.getReliabilityScore(),
                s.getActive(), s.getCreatedAt()
        );
    }
}
