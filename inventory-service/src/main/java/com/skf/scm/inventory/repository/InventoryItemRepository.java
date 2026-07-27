package com.skf.scm.inventory.repository;

import com.skf.scm.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findBySkuCodeAndWarehouseCode(String skuCode, String warehouseCode);

    List<InventoryItem> findBySkuCode(String skuCode);

    List<InventoryItem> findByWarehouseCode(String warehouseCode);
}
