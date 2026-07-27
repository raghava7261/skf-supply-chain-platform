package com.skf.scm.warehouse.service;

import com.skf.scm.warehouse.dto.TaskCompletionRequest;
import com.skf.scm.warehouse.dto.WarehouseTaskRequest;
import com.skf.scm.warehouse.dto.WarehouseTaskResponse;
import com.skf.scm.warehouse.entity.TaskStatus;
import com.skf.scm.warehouse.entity.WarehouseTask;
import com.skf.scm.warehouse.event.WarehouseTaskEventPublisher;
import com.skf.scm.warehouse.exception.WarehouseTaskExceptions.InvalidTaskStateException;
import com.skf.scm.warehouse.exception.WarehouseTaskExceptions.ScanMismatchException;
import com.skf.scm.warehouse.exception.WarehouseTaskExceptions.TaskNotFoundException;
import com.skf.scm.warehouse.repository.WarehouseTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WarehouseTaskService {

    private final WarehouseTaskRepository repository;
    private final WarehouseTaskEventPublisher eventPublisher;

    public WarehouseTaskResponse createTask(WarehouseTaskRequest request) {
        WarehouseTask task = WarehouseTask.builder()
                .taskId("TASK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .taskType(request.taskType())
                .skuCode(request.skuCode())
                .warehouseCode(request.warehouseCode())
                .binLocation(request.binLocation())
                .quantity(request.quantity())
                .referenceId(request.referenceId())
                .assignedTo(request.assignedTo())
                .status(TaskStatus.PENDING)
                .build();
        return WarehouseTaskResponse.from(repository.save(task));
    }

    @Transactional(readOnly = true)
    public WarehouseTaskResponse getByTaskId(String taskId) {
        return WarehouseTaskResponse.from(findOrThrow(taskId));
    }

    @Transactional(readOnly = true)
    public List<WarehouseTaskResponse> getAll() {
        return repository.findAll().stream().map(WarehouseTaskResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<WarehouseTaskResponse> getByStatus(TaskStatus status) {
        return repository.findByStatus(status).stream().map(WarehouseTaskResponse::from).toList();
    }

    public WarehouseTaskResponse startTask(String taskId) {
        WarehouseTask task = findOrThrow(taskId);
        if (task.getStatus() != TaskStatus.PENDING) {
            throw new InvalidTaskStateException(taskId, task.getStatus().name(), "start");
        }
        task.setStatus(TaskStatus.IN_PROGRESS);
        return WarehouseTaskResponse.from(repository.save(task));
    }

    /**
     * Completing a task requires a barcode/QR scan that matches the task's
     * SKU — this is the system-level guard against picking the wrong
     * near-identical part (the SKU-complexity risk called out in the
     * platform's problem statement). On success, publishes an event that
     * inventory-service consumes to adjust stock automatically.
     */
    public WarehouseTaskResponse completeTask(String taskId, TaskCompletionRequest request) {
        WarehouseTask task = findOrThrow(taskId);
        if (task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new InvalidTaskStateException(taskId, task.getStatus().name(), "complete");
        }
        if (!request.scannedCode().equalsIgnoreCase(task.getSkuCode())) {
            throw new ScanMismatchException(taskId, task.getSkuCode(), request.scannedCode());
        }

        task.setScannedCode(request.scannedCode());
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(Instant.now());
        WarehouseTask saved = repository.save(task);

        eventPublisher.publishTaskCompleted(saved);
        return WarehouseTaskResponse.from(saved);
    }

    public WarehouseTaskResponse cancelTask(String taskId) {
        WarehouseTask task = findOrThrow(taskId);
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new InvalidTaskStateException(taskId, task.getStatus().name(), "cancel");
        }
        task.setStatus(TaskStatus.CANCELLED);
        return WarehouseTaskResponse.from(repository.save(task));
    }

    private WarehouseTask findOrThrow(String taskId) {
        return repository.findByTaskId(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }
}
