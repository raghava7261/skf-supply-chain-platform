package com.skf.scm.supplier.event;

import com.skf.scm.common.event.KafkaTopics;
import com.skf.scm.supplier.entity.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SupplierEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishSupplierRegistered(Supplier supplier) {
        // Keyed by supplierCode so all events for the same supplier land on the same partition,
        // preserving per-supplier ordering for downstream consumers.
        kafkaTemplate.send(KafkaTopics.SUPPLIER_REGISTERED, supplier.getSupplierCode(), supplier);
        log.info("Published SUPPLIER_REGISTERED event for supplier {}", supplier.getSupplierCode());
    }
}
