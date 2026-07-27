package com.skf.scm.inventory.service;

import com.skf.scm.inventory.dto.InventoryItemRequest;
import com.skf.scm.inventory.dto.InventoryItemResponse;
import com.skf.scm.inventory.entity.InventoryItem;
import com.skf.scm.inventory.event.InventoryEventPublisher;
import com.skf.scm.inventory.exception.InventoryExceptions.DuplicateInventoryItemException;
import com.skf.scm.inventory.exception.InventoryExceptions.InsufficientStockException;
import com.skf.scm.inventory.exception.InventoryExceptions.InventoryItemNotFoundException;
import com.skf.scm.inventory.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final InventoryItemRepository repository;
    private final InventoryEventPublisher eventPublisher;

    public InventoryItemResponse createItem(InventoryItemRequest request) {
        repository.findBySkuCodeAndWarehouseCode(request.skuCode(), request.warehouseCode())
                .ifPresent(i -> { throw new DuplicateInventoryItemException(request.skuCode(), request.warehouseCode()); });

        InventoryItem item = InventoryItem.builder()
                .skuCode(request.skuCode())
                .skuDescription(request.skuDescription())
                .warehouseCode(request.warehouseCode())
                .quantity(request.quantity())
                .reorderThreshold(request.reorderThreshold())
                .reorderQuantity(request.reorderQuantity())
                .preferredSupplierCode(request.preferredSupplierCode())
                .build();

        InventoryItem saved = repository.save(item);
        eventPublisher.publishStockUpdated(saved.getSkuCode(), saved.getWarehouseCode(), 0, saved.getQuantity(), "INITIAL_STOCK");
        if (saved.isBelowThreshold()) {
            eventPublisher.publishLowStockDetected(saved);
        }
        return InventoryItemResponse.from(saved);
    }

    @Cacheable(value = "inventory", key = "#skuCode + ':' + #warehouseCode")
    @Transactional(readOnly = true)
    public InventoryItemResponse getStock(String skuCode, String warehouseCode) {
        return InventoryItemResponse.from(findOrThrow(skuCode, warehouseCode));
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getStockAcrossWarehouses(String skuCode) {
        // Aggregated cross-warehouse view — this is the query that replaces
        // "check four spreadsheets" with a single source of truth.
        return repository.findBySkuCode(skuCode).stream().map(InventoryItemResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getWarehouseInventory(String warehouseCode) {
        return repository.findByWarehouseCode(warehouseCode).stream().map(InventoryItemResponse::from).toList();
    }

    /**
     * Core stock mutation used for receipts (positive delta), picks/shipments
     * (negative delta), and manual corrections. Always publishes StockUpdated;
     * additionally publishes LowStockDetected if the new quantity crosses the
     * reorder threshold on the way down (not re-fired if already below).
     */
    @CacheEvict(value = "inventory", key = "#skuCode + ':' + #warehouseCode")
    public InventoryItemResponse adjustStock(String skuCode, String warehouseCode, int delta, String reason) {
        InventoryItem item = findOrThrow(skuCode, warehouseCode);
        int previousQuantity = item.getQuantity();
        int newQuantity = previousQuantity + delta;

        if (newQuantity < 0) {
            throw new InsufficientStockException(skuCode, warehouseCode, previousQuantity, -delta);
        }

        boolean wasBelowThreshold = item.isBelowThreshold();
        item.setQuantity(newQuantity);
        InventoryItem saved = repository.save(item);

        eventPublisher.publishStockUpdated(skuCode, warehouseCode, previousQuantity, newQuantity, reason);

        if (saved.isBelowThreshold() && !wasBelowThreshold) {
            eventPublisher.publishLowStockDetected(saved);
        }

        return InventoryItemResponse.from(saved);
    }

    @CacheEvict(value = "inventory", key = "#skuCode + ':' + #warehouseCode")
    public InventoryItemResponse updateThresholds(String skuCode, String warehouseCode, int reorderThreshold, int reorderQuantity) {
        InventoryItem item = findOrThrow(skuCode, warehouseCode);
        item.setReorderThreshold(reorderThreshold);
        item.setReorderQuantity(reorderQuantity);
        return InventoryItemResponse.from(repository.save(item));
    }

    private InventoryItem findOrThrow(String skuCode, String warehouseCode) {
        return repository.findBySkuCodeAndWarehouseCode(skuCode, warehouseCode)
                .orElseThrow(() -> new InventoryItemNotFoundException(skuCode, warehouseCode));
    }
}
