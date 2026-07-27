package com.skf.scm.supplier.exception;

public class SupplierExceptions {

    public static class SupplierNotFoundException extends RuntimeException {
        public SupplierNotFoundException(String supplierCode) {
            super("Supplier not found: " + supplierCode);
        }
    }

    public static class DuplicateSupplierException extends RuntimeException {
        public DuplicateSupplierException(String supplierCode) {
            super("Supplier already exists with code: " + supplierCode);
        }
    }
}
