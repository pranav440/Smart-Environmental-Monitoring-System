package com.environmental.strategies;

import com.environmental.models.EnvironmentalData;

public class HumidityRiskStrategy implements RiskStrategy {
    @Override
    public double calculateRisk(EnvironmentalData data) {
        double humidity = data.getHumidity();
        if (humidity > 80) return 80 + (humidity - 80);
        if (humidity > 60) return 40 + (humidity - 60) * 2;
        if (humidity < 20) return 60 + (20 - humidity) * 2;
        return 20;
    }
    
    @Override
    public String getRiskFactor() {
        return "Humidity";
    }
}