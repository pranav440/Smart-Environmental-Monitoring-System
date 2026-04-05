package com.environmental.strategies;

import com.environmental.models.EnvironmentalData;

public class HeatwaveRiskStrategy implements RiskStrategy {
    @Override
    public double calculateRisk(EnvironmentalData data) {
        double temp = data.getTemperature();
        if (temp > 45) return 100;
        if (temp > 40) return 80 + (temp - 40) * 4;
        if (temp > 35) return 50 + (temp - 35) * 6;
        if (temp > 30) return 20 + (temp - 30) * 6;
        return Math.max(0, (temp - 20) * 2);
    }
    
    @Override
    public String getRiskFactor() {
        return "Heatwave";
    }
}