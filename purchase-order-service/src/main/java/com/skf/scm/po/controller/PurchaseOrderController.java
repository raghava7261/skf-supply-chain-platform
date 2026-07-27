package com.skf.scm.po.controller;

import com.skf.scm.po.dto.PurchaseOrderRequest;
import com.skf.scm.po.dto.PurchaseOrderResponse;
import com.skf.scm.po.entity.PurchaseOrderStatus;
import com.skf.scm.po.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> create(@Valid @RequestBody PurchaseOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                purchaseOrderService.createPurchaseOrder(request, false));
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrderResponse>> getAll(
            @RequestParam(required = false) PurchaseOrderStatus status) {
        return ResponseEntity.ok(status != null
                ? purchaseOrderService.getByStatus(status)
                : purchaseOrderService.getAll());
    }

    @GetMapping("/{poNumber}")
    public ResponseEntity<PurchaseOrderResponse> getOne(@PathVariable String poNumber) {
        return ResponseEntity.ok(purchaseOrderService.getByPoNumber(poNumber));
    }

    @PostMapping("/{poNumber}/approve")
    public ResponseEntity<PurchaseOrderResponse> approve(@PathVariable String poNumber) {
        return ResponseEntity.ok(purchaseOrderService.approve(poNumber));
    }

    @PostMapping("/{poNumber}/receive")
    public ResponseEntity<PurchaseOrderResponse> receive(@PathVariable String poNumber) {
        return ResponseEntity.ok(purchaseOrderService.markReceived(poNumber));
    }

    @PostMapping("/{poNumber}/cancel")
    public ResponseEntity<PurchaseOrderResponse> cancel(@PathVariable String poNumber) {
        return ResponseEntity.ok(purchaseOrderService.cancel(poNumber));
    }
}
