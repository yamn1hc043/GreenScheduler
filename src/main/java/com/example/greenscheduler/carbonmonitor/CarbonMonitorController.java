package com.example.greenscheduler.carbonmonitor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Service A — Carbon Monitor.
 * Exposes endpoints for current grid status, history, and configuration.
 */
@RestController
@RequestMapping("/api/carbon")
public class CarbonMonitorController {

    private final CarbonMonitorService carbonMonitorService;

    public CarbonMonitorController(CarbonMonitorService carbonMonitorService) {
        this.carbonMonitorService = carbonMonitorService;
    }

    /**
     * GET /api/carbon/current-status
     * Returns the current grid status with carbon intensity.
     */
    @GetMapping("/current-status")
    public ResponseEntity<GridStatus> getCurrentStatus() {
        GridStatus status = carbonMonitorService.getCurrentStatus();
        if (status == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(status);
    }

    /**
     * GET /api/carbon/history
     * Returns last N readings for chart rendering.
     */
    @GetMapping("/history")
    public ResponseEntity<List<GridStatus>> getHistory() {
        return ResponseEntity.ok(carbonMonitorService.getHistory());
    }

    /**
     * GET /api/carbon/config
     * Returns current configuration (threshold, dataset size).
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        return ResponseEntity.ok(Map.of(
                "threshold", carbonMonitorService.getThreshold(),
                "datasetSize", carbonMonitorService.getDataset().size(),
                "intervalMs", 3000));
    }
}
