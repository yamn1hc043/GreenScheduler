package com.example.greenscheduler.carbonmonitor;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SERVICE A — Carbon Monitor Service
 *
 * Reads the Indian Grid dataset and simulates real-time 15-min interval data
 * by advancing one row every {@code simulation.interval-ms} milliseconds.
 * Calculates grid carbon intensity and flags status as GREEN or DIRTY.
 */
@Service
public class CarbonMonitorService {

    private final DatasetReader datasetReader;

    @Value("${carbon.threshold:600}")
    private double threshold;

    @Value("${data.file.path:dataset.xls}")
    private String dataFilePath;

    @Getter
    private List<GridStatus> dataset = new ArrayList<>();

    private final AtomicInteger currentIndex = new AtomicInteger(0);

    @Getter
    private GridStatus currentStatus;

    /** Last N readings for chart rendering */
    private final List<GridStatus> history = Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_HISTORY = 50;

    public CarbonMonitorService(DatasetReader datasetReader) {
        this.datasetReader = datasetReader;
    }

    @PostConstruct
    public void init() {
        System.out.println("🔄 Loading dataset from: " + dataFilePath);
        this.dataset = datasetReader.readDataset(dataFilePath);

        if (dataset.isEmpty()) {
            System.err.println("⚠️ Dataset is empty! Creating fallback data...");
            createFallbackData();
        }

        // Initialize with first row
        advanceToNextRow();
        System.out.println("🟢 Carbon Monitor Service initialized with " + dataset.size() + " data points.");
        System.out.println("📈 Carbon Intensity Threshold: " + threshold + " gCO2/kWh");
    }

    /**
     * Advances to the next row in the dataset, simulating real-time progression.
     * Called every {@code simulation.interval-ms} milliseconds.
     */
    @Scheduled(fixedDelayString = "${simulation.interval-ms:3000}")
    public void advanceToNextRow() {
        if (dataset.isEmpty())
            return;

        int idx = currentIndex.getAndIncrement();
        if (idx >= dataset.size()) {
            // Loop back to start
            currentIndex.set(1);
            idx = 0;
        }

        GridStatus status = dataset.get(idx);
        // Apply GREEN/DIRTY flag based on threshold
        status.setStatus(status.getIntensity() < threshold ? "GREEN" : "DIRTY");
        this.currentStatus = status;

        // Add to history
        synchronized (history) {
            history.add(status);
            if (history.size() > MAX_HISTORY) {
                history.remove(0);
            }
        }

        String emoji = "GREEN".equals(status.getStatus()) ? "🟢" : "🔴";
        System.out.printf("%s [%s] Intensity: %.2f gCO2/kWh | Status: %s%n",
                emoji, status.getTime(), status.getIntensity(), status.getStatus());
    }

    /**
     * Returns the last N grid status readings for chart rendering.
     */
    public List<GridStatus> getHistory() {
        synchronized (history) {
            return new ArrayList<>(history);
        }
    }

    public double getThreshold() {
        return threshold;
    }

    /**
     * Fallback data in case the dataset file cannot be read.
     */
    private void createFallbackData() {
        String[] times = { "0:00", "0:15", "0:30", "0:45", "1:00", "1:15", "1:30", "1:45",
                "2:00", "2:15", "2:30", "2:45", "3:00", "3:15", "3:30", "3:45",
                "4:00", "4:15", "4:30", "4:45", "5:00", "5:15", "5:30", "5:45",
                "6:00", "6:15", "6:30", "6:45", "7:00", "7:15", "7:30", "7:45",
                "8:00", "8:15", "8:30", "8:45", "9:00", "9:15", "9:30", "9:45",
                "10:00", "10:15", "10:30", "10:45", "11:00", "11:15", "11:30", "11:45",
                "12:00", "12:15", "12:30", "12:45", "13:00", "13:15", "13:30", "13:45" };

        double[][] mix = {
                // thermal, gas, nuclear, hydro, solar, wind, total
                { 120000, 8000, 6800, 12000, 0, 15000, 161800 },
                { 118000, 7800, 6800, 12500, 0, 16000, 161100 },
                { 115000, 7500, 6800, 13000, 0, 17000, 159300 },
                { 112000, 7200, 6800, 14000, 0, 18000, 158000 },
                { 110000, 7000, 6800, 15000, 0, 19000, 157800 },
                { 108000, 6800, 6800, 16000, 0, 20000, 157600 },
                { 105000, 6500, 6800, 18000, 500, 21000, 157800 },
                { 102000, 6200, 6800, 20000, 1500, 22000, 158500 },
                { 98000, 5800, 6800, 22000, 5000, 23000, 160600 },
                { 95000, 5500, 6800, 24000, 10000, 24000, 165300 },
                { 90000, 5000, 6800, 26000, 18000, 25000, 170800 },
                { 85000, 4500, 6800, 28000, 25000, 26000, 175300 },
                { 80000, 4000, 6800, 30000, 32000, 27000, 179800 },
                { 78000, 3800, 6800, 32000, 35000, 28000, 183600 },
                { 82000, 4200, 6800, 30000, 30000, 26000, 179000 },
                { 88000, 4800, 6800, 28000, 22000, 24000, 173600 },
        };

        for (int i = 0; i < Math.min(times.length, mix.length); i++) {
            double intensity = ((mix[i][0] * 820) + (mix[i][1] * 490)) / mix[i][6];
            dataset.add(GridStatus.builder()
                    .time(times[i])
                    .thermalMW(mix[i][0]).gasMW(mix[i][1]).nuclearMW(mix[i][2])
                    .hydroMW(mix[i][3]).solarMW(mix[i][4]).windMW(mix[i][5])
                    .totalGenMW(mix[i][6])
                    .intensity(Math.round(intensity * 100.0) / 100.0)
                    .build());
        }
    }
}
