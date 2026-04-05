package com.environmental.utils;

import com.environmental.models.City;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFGenerator {
    
    public static void generateReport(List<City> cities, String filePath) throws Exception {
        // Generate CSV report instead of PDF (no external dependency)
        String csvPath = filePath.replace(".pdf", ".csv");
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(csvPath))) {
            // Write header
            writer.println("City,Type,Risk Level,Risk Score,Temperature (°C),AQI,Humidity (%),Timestamp");
            
            // Write data
            for (City city : cities) {
                writer.printf("%s,%s,%s,%.1f,%.1f,%.0f,%.0f,%s%n",
                    city.getName(),
                    city.getCityType(),
                    city.getRiskLevel().text,
                    city.getCurrentRiskScore(),
                    city.getCurrentData().getTemperature(),
                    city.getCurrentData().getAqi(),
                    city.getCurrentData().getHumidity(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                );
            }
        }
        
        System.out.println("✅ Report generated: " + csvPath);
    }
}