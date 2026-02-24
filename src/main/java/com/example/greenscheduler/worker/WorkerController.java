package com.example.greenscheduler.worker;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for Service C — Batch Worker.
 */
@RestController
@RequestMapping("/api/worker")
public class WorkerController {

    private final BatchWorkerService batchWorkerService;

    public WorkerController(BatchWorkerService batchWorkerService) {
        this.batchWorkerService = batchWorkerService;
    }

    @GetMapping("/logs")
    public ResponseEntity<List<ExecutionLog>> getAllLogs() {
        return ResponseEntity.ok(batchWorkerService.getAllLogs());
    }
}
