package com.skf.scm.po.repository;

import com.skf.scm.po.entity.PurchaseOrder;
import com.skf.scm.po.entity.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findByPoNumber(String poNumber);
    List<PurchaseOrder> findByStatus(PurchaseOrderStatus status);
    List<PurchaseOrder> findBySupplierCode(String supplierCode);

    /** Used to avoid spamming duplicate auto-POs if a low-stock event fires again before the first PO clears. */
    boolean existsBySkuCodeAndWarehouseCodeAndStatusIn(String skuCode, String warehouseCode, List<PurchaseOrderStatus> statuses);
}
