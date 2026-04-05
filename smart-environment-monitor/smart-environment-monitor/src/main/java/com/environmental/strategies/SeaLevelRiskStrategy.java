package com.environmental.strategies;

import com.environmental.models.EnvironmentalData;

public class SeaLevelRiskStrategy implements RiskStrategy {
    @Override
    public double calculateRisk(EnvironmentalData data) {
        double pressure = data.getPressure();
        double humidity = data.getHumidity();
        double windSpeed = data.getWindSpeed();
        
        // Low pressure + high humidity + high wind = coastal risk (storm surge)
        double pressureRisk = 0;
        if (pressure < 990) pressureRisk = 80 + (990 - pressure) * 2;
        else if (pressure < 1000) pressureRisk = 50 + (1000 - pressure) * 3;
        else if (pressure < 1010) pressureRisk = 20 + (1010 - pressure) * 3;
        else pressureRisk = Math.max(0, 20 - (pressure - 1010) * 2);
        
        double humidityFactor = humidity > 80 ? 1.3 : (humidity > 70 ? 1.15 : 1.0);
        double windFactor = windSpeed > 20 ? 1.4 : (windSpeed > 15 ? 1.2 : 1.0);
        
        return Math.min(pressureRisk * humidityFactor * windFactor, 100);
    }

    @Override
    public String getRiskFactor() {
        return "Sea Level / Storm Surge Risk";
    }
}
