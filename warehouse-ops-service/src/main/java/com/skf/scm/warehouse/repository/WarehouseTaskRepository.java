package com.skf.scm.warehouse.repository;

import com.skf.scm.warehouse.entity.TaskStatus;
import com.skf.scm.warehouse.entity.TaskType;
import com.skf.scm.warehouse.entity.WarehouseTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehouseTaskRepository extends JpaRepository<WarehouseTask, Long> {
    Optional<WarehouseTask> findByTaskId(String taskId);
    List<WarehouseTask> findByStatus(TaskStatus status);
    List<WarehouseTask> findByWarehouseCodeAndStatus(String warehouseCode, TaskStatus status);
    List<WarehouseTask> findByTaskType(TaskType taskType);
    List<WarehouseTask> findByAssignedTo(String assignedTo);
}
