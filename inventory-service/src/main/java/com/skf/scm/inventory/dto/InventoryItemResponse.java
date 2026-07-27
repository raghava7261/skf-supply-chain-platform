package com.skf.scm.inventory.dto;

import com.skf.scm.inventory.entity.InventoryItem;

import java.io.Serializable;
import java.time.Instant;

public record InventoryItemResponse(
        Long id,
        String skuCode,
        String skuDescription,
        String warehouseCode,
        Integer quantity,
        Integer reorderThreshold,
        Integer reorderQuantity,
        String preferredSupplierCode,
        boolean belowThreshold,
        Instant updatedAt
) implements Serializable {
    public static InventoryItemResponse from(InventoryItem i) {
        return new InventoryItemResponse(
                i.getId(), i.getSkuCode(), i.getSkuDescription(), i.getWarehouseCode(),
                i.getQuantity(), i.getReorderThreshold(), i.getReorderQuantity(),
                i.getPreferredSupplierCode(), i.isBelowThreshold(), i.getUpdatedAt()
        );
    }
}
