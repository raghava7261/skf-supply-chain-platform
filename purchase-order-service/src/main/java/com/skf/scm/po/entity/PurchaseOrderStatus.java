package com.skf.scm.po.entity;

public enum PurchaseOrderStatus {
    DRAFT,      // auto-created from low-stock event, or manually started, awaiting approval
    APPROVED,   // approved by procurement manager, sent to supplier
    RECEIVED,   // shipment confirmed received at warehouse
    CANCELLED
}
