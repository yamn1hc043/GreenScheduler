package com.example.greenscheduler.worker;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA Entity tracking executed job results.
 */
@Entity
@Table(name = "execution_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long jobId;

    private String jobName;

    private String executionTime;

    private String gridTime;

    private double intensity;

    @Column(length = 500)
    private String message;
}
