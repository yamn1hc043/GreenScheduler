package com.example.greenscheduler.events;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Cross-cutting event log entity.
 * Records all significant system events for the dashboard timeline.
 */
@Entity
@Table(name = "event_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;

    /** Which service generated this event (CARBON_MONITOR, SCHEDULER, WORKER) */
    private String service;

    /**
     * Event type: GREEN_WINDOW, DIRTY_WINDOW, JOB_TRIGGERED, JOB_COMPLETED,
     * JOB_WAITING
     */
    private String type;

    @Column(length = 500)
    private String message;

    private double intensity;
}
