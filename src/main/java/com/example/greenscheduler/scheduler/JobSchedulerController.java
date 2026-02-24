package com.example.greenscheduler.scheduler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Service B — Job Scheduler.
 */
@RestController
@RequestMapping("/api/scheduler")
public class JobSchedulerController {

    private final JobSchedulerService jobSchedulerService;

    public JobSchedulerController(JobSchedulerService jobSchedulerService) {
        this.jobSchedulerService = jobSchedulerService;
    }

    /**
     * GET /api/scheduler/jobs — List all jobs with their status.
     */
    @GetMapping("/jobs")
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobSchedulerService.getAllJobs());
    }

    /**
     * POST /api/scheduler/jobs — Add a new batch job.
     * Body: { "name": "My Job Name" }
     */
    @PostMapping("/jobs")
    public ResponseEntity<Job> addJob(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "Unnamed Batch Job");
        Job job = jobSchedulerService.addJob(name);
        return ResponseEntity.ok(job);
    }

    /**
     * POST /api/scheduler/reset — Reset all jobs to PENDING.
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetJobs() {
        jobSchedulerService.resetAllJobs();
        return ResponseEntity.ok(Map.of("message", "All jobs reset to PENDING"));
    }
}
