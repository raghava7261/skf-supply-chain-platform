package com.skf.scm.warehouse.controller;

import com.skf.scm.warehouse.dto.TaskCompletionRequest;
import com.skf.scm.warehouse.dto.WarehouseTaskRequest;
import com.skf.scm.warehouse.dto.WarehouseTaskResponse;
import com.skf.scm.warehouse.entity.TaskStatus;
import com.skf.scm.warehouse.service.WarehouseTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/warehouse-tasks")
@RequiredArgsConstructor
public class WarehouseTaskController {

    private final WarehouseTaskService taskService;

    @PostMapping
    public ResponseEntity<WarehouseTaskResponse> create(@Valid @RequestBody WarehouseTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request));
    }

    @GetMapping
    public ResponseEntity<List<WarehouseTaskResponse>> getAll(
            @RequestParam(required = false) TaskStatus status) {
        return ResponseEntity.ok(status != null ? taskService.getByStatus(status) : taskService.getAll());
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<WarehouseTaskResponse> getOne(@PathVariable String taskId) {
        return ResponseEntity.ok(taskService.getByTaskId(taskId));
    }

    @PostMapping("/{taskId}/start")
    public ResponseEntity<WarehouseTaskResponse> start(@PathVariable String taskId) {
        return ResponseEntity.ok(taskService.startTask(taskId));
    }

    @PostMapping("/{taskId}/complete")
    public ResponseEntity<WarehouseTaskResponse> complete(
            @PathVariable String taskId, @Valid @RequestBody TaskCompletionRequest request) {
        return ResponseEntity.ok(taskService.completeTask(taskId, request));
    }

    @PostMapping("/{taskId}/cancel")
    public ResponseEntity<WarehouseTaskResponse> cancel(@PathVariable String taskId) {
        return ResponseEntity.ok(taskService.cancelTask(taskId));
    }
}
