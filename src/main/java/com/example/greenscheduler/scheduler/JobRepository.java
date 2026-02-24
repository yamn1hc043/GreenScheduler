package com.example.greenscheduler.scheduler;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findAllByOrderByIdAsc();

    Optional<Job> findFirstByStatusOrderByIdAsc(String status);

    long countByStatus(String status);
}
