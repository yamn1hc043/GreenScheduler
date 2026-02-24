package com.example.greenscheduler.scheduler;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA Entity representing a batch job in the scheduler queue.
 */
@Entity
@Table(name = "batch_job")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    /** PENDING, SCHEDULED, RUNNING, COMPLETED, WAITING */
    private String status;

    private String scheduledTime;

    private String completedTime;

    private Double completedAtIntensity;
}
