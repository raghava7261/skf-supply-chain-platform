package com.skf.scm.shipment.exception;

public class ShipmentExceptions {

    public static class ShipmentNotFoundException extends RuntimeException {
        public ShipmentNotFoundException(String shipmentNumber) {
            super("Shipment not found: " + shipmentNumber);
        }
    }

    public static class InvalidShipmentStateException extends RuntimeException {
        public InvalidShipmentStateException(String shipmentNumber, String currentStatus, String action) {
            super("Cannot " + action + " shipment " + shipmentNumber + " in status " + currentStatus);
        }
    }
}
