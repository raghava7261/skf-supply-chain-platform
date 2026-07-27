package com.skf.scm.po.dto;

import com.skf.scm.po.entity.PurchaseOrder;
import com.skf.scm.po.entity.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PurchaseOrderResponse(
        Long id,
        String poNumber,
        String supplierCode,
        String skuCode,
        String warehouseCode,
        Integer quantity,
        BigDecimal unitCost,
        PurchaseOrderStatus status,
        Boolean autoTriggered,
        Instant expectedDeliveryDate,
        Instant createdAt
) {
    public static PurchaseOrderResponse from(PurchaseOrder po) {
        return new PurchaseOrderResponse(
                po.getId(), po.getPoNumber(), po.getSupplierCode(), po.getSkuCode(), po.getWarehouseCode(),
                po.getQuantity(), po.getUnitCost(), po.getStatus(), po.getAutoTriggered(),
                po.getExpectedDeliveryDate(), po.getCreatedAt()
        );
    }
}
