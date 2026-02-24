package com.example.greenscheduler.events;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cross-cutting event service used by all microservices
 * to publish events for the dashboard timeline.
 */
@Service
public class EventService {

    private final EventLogRepository eventLogRepository;

    public EventService(EventLogRepository eventLogRepository) {
        this.eventLogRepository = eventLogRepository;
    }

    public void publish(String service, String type, String message, double intensity) {
        EventLog event = EventLog.builder()
                .timestamp(LocalDateTime.now())
                .service(service)
                .type(type)
                .message(message)
                .intensity(intensity)
                .build();
        eventLogRepository.save(event);
    }

    public List<EventLog> getRecentEvents() {
        return eventLogRepository.findTop50ByOrderByTimestampDesc();
    }
}
