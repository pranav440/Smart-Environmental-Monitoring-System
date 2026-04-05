package com.environmental.strategies;

import com.environmental.models.EnvironmentalData;

public class AQIRiskStrategy implements RiskStrategy {
    @Override
    public double calculateRisk(EnvironmentalData data) {
        double aqi = data.getAqi();
        if (aqi > 300) return 100;
        if (aqi > 200) return 80 + (aqi - 200) * 0.2;
        if (aqi > 150) return 60 + (aqi - 150) * 0.4;
        if (aqi > 100) return 40 + (aqi - 100) * 0.4;
        if (aqi > 50) return 20 + (aqi - 50) * 0.4;
        return aqi * 0.4;
    }
    
    @Override
    public String getRiskFactor() {
        return "Air Quality";
    }
}