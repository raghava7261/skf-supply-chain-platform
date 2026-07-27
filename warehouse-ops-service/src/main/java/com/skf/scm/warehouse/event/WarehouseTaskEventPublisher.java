package com.skf.scm.warehouse.event;

import com.skf.scm.common.event.KafkaTopics;
import com.skf.scm.common.event.WarehouseTaskCompletedEvent;
import com.skf.scm.warehouse.entity.WarehouseTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class WarehouseTaskEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishTaskCompleted(WarehouseTask task) {
        WarehouseTaskCompletedEvent event = new WarehouseTaskCompletedEvent(
                task.getTaskId(), task.getTaskType().name(), task.getSkuCode(), task.getWarehouseCode(),
                task.getBinLocation(), task.getQuantity(), Instant.now()
        );
        kafkaTemplate.send(KafkaTopics.WAREHOUSE_TASK_COMPLETED, task.getSkuCode() + ":" + task.getWarehouseCode(), event);
        log.info("Published WAREHOUSE_TASK_COMPLETED: {} [{}] sku={} qty={}",
                task.getTaskId(), task.getTaskType(), task.getSkuCode(), task.getQuantity());
    }
}
