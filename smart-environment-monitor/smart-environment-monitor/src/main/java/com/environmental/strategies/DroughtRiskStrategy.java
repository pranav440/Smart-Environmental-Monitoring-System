package com.environmental.strategies;

import com.environmental.models.EnvironmentalData;

public class DroughtRiskStrategy implements RiskStrategy {
    @Override
    public double calculateRisk(EnvironmentalData data) {
        double humidity = data.getHumidity();
        double temp = data.getTemperature();
        
        double humidityRisk = 0;
        if (humidity < 15) humidityRisk = 100;
        else if (humidity < 20) humidityRisk = 80 + (20 - humidity) * 4;
        else if (humidity < 30) humidityRisk = 50 + (30 - humidity) * 3;
        else if (humidity < 40) humidityRisk = 20 + (40 - humidity) * 3;
        else humidityRisk = Math.max(0, 20 - (humidity - 40));
        
        double tempFactor = 1.0;
        if (temp > 40) tempFactor = 1.5;
        else if (temp > 35) tempFactor = 1.3;
        else if (temp > 30) tempFactor = 1.1;
        
        return Math.min(humidityRisk * tempFactor, 100);
    }

    @Override
    public String getRiskFactor() {
        return "Drought Risk";
    }
}
