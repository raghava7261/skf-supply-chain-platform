package com.skf.scm.po.exception;

public class PurchaseOrderExceptions {

    public static class PurchaseOrderNotFoundException extends RuntimeException {
        public PurchaseOrderNotFoundException(String poNumber) {
            super("Purchase order not found: " + poNumber);
        }
    }

    public static class InvalidPurchaseOrderStateException extends RuntimeException {
        public InvalidPurchaseOrderStateException(String poNumber, String currentStatus, String action) {
            super("Cannot " + action + " PO " + poNumber + " in status " + currentStatus);
        }
    }
}
