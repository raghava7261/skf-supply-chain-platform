package com.skf.scm.inventory.controller;

import com.skf.scm.inventory.dto.InventoryItemRequest;
import com.skf.scm.inventory.dto.InventoryItemResponse;
import com.skf.scm.inventory.dto.StockAdjustmentRequest;
import com.skf.scm.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryItemResponse> createItem(@Valid @RequestBody InventoryItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createItem(request));
    }

    @GetMapping("/{skuCode}/{warehouseCode}")
    public ResponseEntity<InventoryItemResponse> getStock(
            @PathVariable String skuCode, @PathVariable String warehouseCode) {
        return ResponseEntity.ok(inventoryService.getStock(skuCode, warehouseCode));
    }

    @GetMapping("/sku/{skuCode}")
    public ResponseEntity<List<InventoryItemResponse>> getStockAcrossWarehouses(@PathVariable String skuCode) {
        return ResponseEntity.ok(inventoryService.getStockAcrossWarehouses(skuCode));
    }

    @GetMapping("/warehouse/{warehouseCode}")
    public ResponseEntity<List<InventoryItemResponse>> getWarehouseInventory(@PathVariable String warehouseCode) {
        return ResponseEntity.ok(inventoryService.getWarehouseInventory(warehouseCode));
    }

    @PostMapping("/{skuCode}/{warehouseCode}/adjust")
    public ResponseEntity<InventoryItemResponse> adjustStock(
            @PathVariable String skuCode, @PathVariable String warehouseCode,
            @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(inventoryService.adjustStock(
                skuCode, warehouseCode, request.delta(), request.reason()));
    }

    @PatchMapping("/{skuCode}/{warehouseCode}/thresholds")
    public ResponseEntity<InventoryItemResponse> updateThresholds(
            @PathVariable String skuCode, @PathVariable String warehouseCode,
            @RequestParam int reorderThreshold, @RequestParam int reorderQuantity) {
        return ResponseEntity.ok(inventoryService.updateThresholds(
                skuCode, warehouseCode, reorderThreshold, reorderQuantity));
    }
}
