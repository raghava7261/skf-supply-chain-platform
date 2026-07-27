package com.skf.scm.inventory.exception;

public class InventoryExceptions {

    public static class InventoryItemNotFoundException extends RuntimeException {
        public InventoryItemNotFoundException(String skuCode, String warehouseCode) {
            super("Inventory item not found: sku=" + skuCode + ", warehouse=" + warehouseCode);
        }
    }

    public static class InsufficientStockException extends RuntimeException {
        public InsufficientStockException(String skuCode, String warehouseCode, int available, int requested) {
            super("Insufficient stock for sku=" + skuCode + " at warehouse=" + warehouseCode
                    + ": available=" + available + ", requested=" + requested);
        }
    }

    public static class DuplicateInventoryItemException extends RuntimeException {
        public DuplicateInventoryItemException(String skuCode, String warehouseCode) {
            super("Inventory item already exists for sku=" + skuCode + " at warehouse=" + warehouseCode);
        }
    }
}
