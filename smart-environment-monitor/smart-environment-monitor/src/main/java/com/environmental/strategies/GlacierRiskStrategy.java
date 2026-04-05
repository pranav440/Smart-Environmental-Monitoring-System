package com.environmental.strategies;

import com.environmental.models.EnvironmentalData;

public class GlacierRiskStrategy implements RiskStrategy {
    @Override
    public double calculateRisk(EnvironmentalData data) {
        double temp = data.getTemperature();
        
        // Glacier melt risk increases with temperature
        // Relevant primarily for high-altitude cities
        if (temp > 25) return 90 + Math.min((temp - 25) * 1, 10);
        if (temp > 20) return 70 + (temp - 20) * 4;
        if (temp > 15) return 45 + (temp - 15) * 5;
        if (temp > 10) return 20 + (temp - 10) * 5;
        if (temp > 5) return 5 + (temp - 5) * 3;
        if (temp > 0) return temp;
        return 0;
    }

    @Override
    public String getRiskFactor() {
        return "Glacier Melt Risk";
    }
}
