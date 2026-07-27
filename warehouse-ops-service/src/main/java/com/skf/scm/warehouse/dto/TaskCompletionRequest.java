package com.skf.scm.warehouse.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * scannedCode is what a barcode/QR scanner would populate at the point of
 * completion — the system-level check that the worker touched the SKU the
 * task actually asked for, not a different but similar-looking part.
 */
public record TaskCompletionRequest(
        @NotBlank String scannedCode
) {
}
