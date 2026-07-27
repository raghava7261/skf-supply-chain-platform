package com.skf.scm.supplier.controller;

import com.skf.scm.supplier.dto.SupplierRequest;
import com.skf.scm.supplier.dto.SupplierResponse;
import com.skf.scm.supplier.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.createSupplier(request));
    }

    @GetMapping
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @GetMapping("/{supplierCode}")
    public ResponseEntity<SupplierResponse> getSupplier(@PathVariable String supplierCode) {
        return ResponseEntity.ok(supplierService.getBySupplierCode(supplierCode));
    }

    @PutMapping("/{supplierCode}")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable String supplierCode, @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(supplierService.updateSupplier(supplierCode, request));
    }

    @PatchMapping("/{supplierCode}/reliability")
    public ResponseEntity<SupplierResponse> adjustReliability(
            @PathVariable String supplierCode, @RequestParam boolean onTime) {
        return ResponseEntity.ok(supplierService.adjustReliabilityScore(supplierCode, onTime));
    }

    @DeleteMapping("/{supplierCode}")
    public ResponseEntity<Void> deactivateSupplier(@PathVariable String supplierCode) {
        supplierService.deactivateSupplier(supplierCode);
        return ResponseEntity.noContent().build();
    }
}
