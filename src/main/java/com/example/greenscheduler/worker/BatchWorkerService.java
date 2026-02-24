package com.example.greenscheduler.worker;

import com.example.greenscheduler.carbonmonitor.GridStatus;
import com.example.greenscheduler.events.EventService;
import com.example.greenscheduler.scheduler.Job;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * SERVICE C — Batch Worker Service
 *
 * Executes batch jobs that have been triggered by the Job Scheduler.
 * Simulates processing with a brief delay and logs execution details.
 */
@Service
public class BatchWorkerService {

    private final ExecutionLogRepository executionLogRepository;
    private final EventService eventService;

    public BatchWorkerService(ExecutionLogRepository executionLogRepository, EventService eventService) {
        this.executionLogRepository = executionLogRepository;
        this.eventService = eventService;
    }

    /**
     * Execute a batch job using clean energy.
     *
     * @param job        the job to execute
     * @param gridStatus current grid status at time of execution
     * @return the execution log entry
     */
    public ExecutionLog execute(Job job, GridStatus gridStatus) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Simulate processing delay (500ms)
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String message = String.format(
                "✅ Executing Heavy Batch Job [ID: %d, Name: %s] at Grid Time [%s] using Clean Energy! Intensity: %.2f gCO2/kWh",
                job.getId(), job.getName(), gridStatus.getTime(), gridStatus.getIntensity());

        System.out.println(message);

        // Save execution log
        ExecutionLog log = ExecutionLog.builder()
                .jobId(job.getId())
                .jobName(job.getName())
                .executionTime(now)
                .gridTime(gridStatus.getTime())
                .intensity(gridStatus.getIntensity())
                .message(message)
                .build();
        executionLogRepository.save(log);

        // Publish event
        eventService.publish("WORKER", "JOB_COMPLETED", message, gridStatus.getIntensity());

        return log;
    }

    public java.util.List<ExecutionLog> getAllLogs() {
        return executionLogRepository.findAllByOrderByIdDesc();
    }
}
