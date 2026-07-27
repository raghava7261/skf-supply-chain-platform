package com.skf.scm.warehouse.entity;

public enum TaskType {
    PICK,     // remove stock from a bin for an outbound order — decreases inventory
    PACK,     // stage picked items for shipment — no inventory quantity change
    PUTAWAY   // place received stock into a bin — increases inventory
}
