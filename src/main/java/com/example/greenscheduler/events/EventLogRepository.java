package com.example.greenscheduler.events;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventLogRepository extends JpaRepository<EventLog, Long> {

    List<EventLog> findTop50ByOrderByTimestampDesc();
}
