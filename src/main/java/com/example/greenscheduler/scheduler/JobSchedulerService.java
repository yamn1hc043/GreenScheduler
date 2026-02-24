package com.example.greenscheduler.scheduler;

import com.example.greenscheduler.carbonmonitor.CarbonMonitorService;
import com.example.greenscheduler.carbonmonitor.GridStatus;
import com.example.greenscheduler.events.EventService;
import com.example.greenscheduler.worker.BatchWorkerService;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * SERVICE B — Job Scheduler Service
 *
 * Polls the Carbon Monitor Service at regular intervals.
 * When a GREEN window is detected, it picks the next pending job
 * from the queue and dispatches it to the Batch Worker Service.
 *
 * During DIRTY windows, it logs a waiting message and holds all jobs.
 */
@Service
public class JobSchedulerService {

    private final CarbonMonitorService carbonMonitorService;
    private final BatchWorkerService batchWorkerService;
    private final JobRepository jobRepository;
    private final EventService eventService;

    public JobSchedulerService(CarbonMonitorService carbonMonitorService,
            BatchWorkerService batchWorkerService,
            JobRepository jobRepository,
            EventService eventService) {
        this.carbonMonitorService = carbonMonitorService;
        this.batchWorkerService = batchWorkerService;
        this.jobRepository = jobRepository;
        this.eventService = eventService;
    }

    /**
     * Initialize 5 dummy batch jobs on startup.
     */
    @PostConstruct
    public void initJobs() {
        if (jobRepository.count() == 0) {
            String[] jobNames = {
                    "Data Warehouse ETL Pipeline",
                    "ML Model Training (TensorFlow)",
                    "Database Backup & Compression",
                    "Log Aggregation & Analytics",
                    "Report Generation (PDF Export)"
            };

            for (String name : jobNames) {
                Job job = Job.builder()
                        .name(name)
                        .status("PENDING")
                        .build();
                jobRepository.save(job);
            }

            System.out.println("📋 Initialized " + jobNames.length + " dummy batch jobs.");
        }
    }

    /**
     * Core scheduling loop — polls the Carbon Monitor every interval.
     * Dispatches jobs during GREEN windows, holds during DIRTY windows.
     */
    @Scheduled(fixedDelayString = "${simulation.interval-ms:3000}", initialDelay = 1500)
    public void pollAndSchedule() {
        GridStatus currentStatus = carbonMonitorService.getCurrentStatus();
        if (currentStatus == null)
            return;

        if ("GREEN".equals(currentStatus.getStatus())) {
            // GREEN Window — try to dispatch a pending job
            Optional<Job> pendingJob = jobRepository.findFirstByStatusOrderByIdAsc("PENDING");

            if (pendingJob.isPresent()) {
                Job job = pendingJob.get();
                job.setStatus("RUNNING");
                job.setScheduledTime(currentStatus.getTime());
                jobRepository.save(job);

                String triggerMsg = String.format(
                        "🚀 GREEN WINDOW! Triggering Job [ID: %d] '%s' at Grid Time [%s] | Intensity: %.2f",
                        job.getId(), job.getName(), currentStatus.getTime(), currentStatus.getIntensity());
                System.out.println(triggerMsg);
                eventService.publish("SCHEDULER", "JOB_TRIGGERED", triggerMsg, currentStatus.getIntensity());

                // Dispatch to Batch Worker
                batchWorkerService.execute(job, currentStatus);

                // Mark completed
                job.setStatus("COMPLETED");
                job.setCompletedTime(currentStatus.getTime());
                job.setCompletedAtIntensity(currentStatus.getIntensity());
                jobRepository.save(job);
            } else {
                // All jobs completed
                long completed = jobRepository.countByStatus("COMPLETED");
                System.out.printf("🟢 [%s] GREEN WINDOW | Intensity: %.2f | All %d jobs completed!%n",
                        currentStatus.getTime(), currentStatus.getIntensity(), completed);
            }
        } else {
            // DIRTY Window — hold jobs
            long pending = jobRepository.countByStatus("PENDING");
            if (pending > 0) {
                String waitMsg = String.format(
                        "⏳ Waiting for Green Energy... Current Intensity: %.2f gCO2/kWh at [%s] | %d jobs pending",
                        currentStatus.getIntensity(), currentStatus.getTime(), pending);
                System.out.println(waitMsg);
                eventService.publish("SCHEDULER", "JOB_WAITING", waitMsg, currentStatus.getIntensity());
            }
        }
    }

    /**
     * Get all jobs ordered by ID.
     */
    public List<Job> getAllJobs() {
        return jobRepository.findAllByOrderByIdAsc();
    }

    /**
     * Add a new job to the queue.
     */
    public Job addJob(String name) {
        Job job = Job.builder()
                .name(name)
                .status("PENDING")
                .build();
        return jobRepository.save(job);
    }

    /**
     * Reset all jobs to PENDING status for re-running the simulation.
     */
    public void resetAllJobs() {
        List<Job> jobs = jobRepository.findAll();
        for (Job job : jobs) {
            job.setStatus("PENDING");
            job.setScheduledTime(null);
            job.setCompletedTime(null);
            job.setCompletedAtIntensity(null);
        }
        jobRepository.saveAll(jobs);
    }
}
