package com.example.greenscheduler.carbonmonitor;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Reads the Indian Grid SCADA dataset from an Excel (.xls) file.
 * Dynamically locates headers and parses each row into a GridStatus object
 * with pre-calculated carbon intensity.
 *
 * Formula: Intensity = ((Thermal_MW * 820) + (Gas_MW * 490)) / Total_Gen_MW
 */
@Component
public class DatasetReader {

    /**
     * Parse the Excel file and return all valid grid data rows.
     *
     * @param filePath path to the .xls file
     * @return list of GridStatus objects with computed intensity
     */
    public List<GridStatus> readDataset(String filePath) {
        List<GridStatus> dataList = new ArrayList<>();

        try (InputStream is = new FileInputStream(filePath);
                Workbook workbook = new HSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getPhysicalNumberOfRows();

            // --- Step 1: Find the header row dynamically ---
            int headerRowIdx = -1;
            Map<String, Integer> columnMap = new HashMap<>();

            for (int i = 0; i < Math.min(totalRows, 10); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null && cell.getCellType() == CellType.STRING) {
                        String val = cell.getStringCellValue().trim().toUpperCase();
                        if (val.contains("TIME") || val.contains("THERMAL") || val.contains("TOTAL")) {
                            columnMap.put(val, j);
                        }
                    }
                }

                if (columnMap.containsKey("TIME") || columnMap.size() >= 3) {
                    headerRowIdx = i;
                    // Re-scan this row fully to capture all headers
                    columnMap.clear();
                    for (int j = 0; j < row.getLastCellNum(); j++) {
                        Cell cell = row.getCell(j);
                        if (cell != null && cell.getCellType() == CellType.STRING) {
                            String val = cell.getStringCellValue().trim().toUpperCase();
                            columnMap.put(val, j);
                        }
                    }
                    break;
                }
                columnMap.clear();
            }

            if (headerRowIdx == -1) {
                System.err.println("⚠️ Could not find header row in dataset!");
                return dataList;
            }

            System.out.println("📊 Dataset headers found at row " + (headerRowIdx + 1) + ": " + columnMap.keySet());

            // --- Step 2: Resolve column indices ---
            int timeCol = findColumn(columnMap, "TIME");
            int thermalCol = findColumn(columnMap, "THERMAL");
            int gasCol = findColumn(columnMap, "GAS");
            int nuclearCol = findColumn(columnMap, "NUCLEAR");
            int hydroCol = findColumn(columnMap, "HYDRO");
            int solarCol = findColumn(columnMap, "SOLAR");
            int windCol = findColumn(columnMap, "WIND");
            int totalCol = findColumn(columnMap, "TOTAL");

            // --- Step 3: Parse data rows ---
            for (int i = headerRowIdx + 1; i < totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                try {
                    String time = getCellAsString(row.getCell(timeCol));
                    if (time == null || time.isBlank())
                        continue;

                    double thermal = getCellAsDouble(row.getCell(thermalCol));
                    double gas = getCellAsDouble(row.getCell(gasCol));
                    double nuclear = getCellAsDouble(row.getCell(nuclearCol));
                    double hydro = getCellAsDouble(row.getCell(hydroCol));
                    double solar = getCellAsDouble(row.getCell(solarCol));
                    double wind = getCellAsDouble(row.getCell(windCol));
                    double totalGen = getCellAsDouble(row.getCell(totalCol));

                    // Avoid division by zero
                    if (totalGen <= 0)
                        continue;

                    // Carbon Intensity Formula: gCO2/kWh
                    double intensity = ((thermal * 820) + (gas * 490)) / totalGen;
                    intensity = Math.round(intensity * 100.0) / 100.0;

                    GridStatus status = GridStatus.builder()
                            .time(time)
                            .thermalMW(thermal)
                            .gasMW(gas)
                            .nuclearMW(nuclear)
                            .hydroMW(hydro)
                            .solarMW(solar)
                            .windMW(wind)
                            .totalGenMW(totalGen)
                            .intensity(intensity)
                            .build();

                    dataList.add(status);
                } catch (Exception e) {
                    // Skip malformed rows silently
                }
            }

            System.out.println("✅ Loaded " + dataList.size() + " data points from dataset.");

        } catch (IOException e) {
            System.err.println("❌ Error reading dataset: " + e.getMessage());
            e.printStackTrace();
        }

        return dataList;
    }

    /**
     * Find a column index by searching for a key that contains the given keyword.
     */
    private int findColumn(Map<String, Integer> map, String keyword) {
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getKey().contains(keyword)) {
                return entry.getValue();
            }
        }
        return -1; // not found
    }

    private String getCellAsString(Cell cell) {
        if (cell == null)
            return null;
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            // Time might be stored as a number (e.g., 0.0, 0.0104...)
            if (DateUtil.isCellDateFormatted(cell)) {
                java.util.Date date = cell.getDateCellValue();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("H:mm");
                return sdf.format(date);
            }
            return String.valueOf(cell.getNumericCellValue());
        }
        return null;
    }

    private double getCellAsDouble(Cell cell) {
        if (cell == null)
            return 0.0;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue().trim().replace(",", ""));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
}
