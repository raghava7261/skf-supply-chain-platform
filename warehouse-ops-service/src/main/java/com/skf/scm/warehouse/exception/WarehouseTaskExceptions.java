package com.skf.scm.warehouse.exception;

public class WarehouseTaskExceptions {

    public static class TaskNotFoundException extends RuntimeException {
        public TaskNotFoundException(String taskId) {
            super("Warehouse task not found: " + taskId);
        }
    }

    public static class InvalidTaskStateException extends RuntimeException {
        public InvalidTaskStateException(String taskId, String currentStatus, String action) {
            super("Cannot " + action + " task " + taskId + " in status " + currentStatus);
        }
    }

    public static class ScanMismatchException extends RuntimeException {
        public ScanMismatchException(String taskId, String expectedSku, String scannedCode) {
            super("Scan mismatch on task " + taskId + ": expected SKU " + expectedSku + " but scanned " + scannedCode);
        }
    }
}
