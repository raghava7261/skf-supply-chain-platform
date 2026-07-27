package com.skf.scm.shipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ShipmentRequest(
        String poNumber,
        @NotBlank String carrier,
        String trackingNumber,
        @NotBlank String originWarehouse,
        @NotBlank String destination,
        @NotNull Instant estimatedDelivery
) {
}
