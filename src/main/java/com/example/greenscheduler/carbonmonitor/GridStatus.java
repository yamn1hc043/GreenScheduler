package com.example.greenscheduler.carbonmonitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single time-slice snapshot of the Indian Power Grid.
 * Contains generation mix data and computed carbon intensity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GridStatus {

    private String time;
    private double thermalMW;
    private double gasMW;
    private double nuclearMW;
    private double hydroMW;
    private double solarMW;
    private double windMW;
    private double totalGenMW;

    /** Carbon intensity in gCO2/kWh */
    private double intensity;

    /** GREEN if intensity < threshold, else DIRTY */
    private String status;
}
