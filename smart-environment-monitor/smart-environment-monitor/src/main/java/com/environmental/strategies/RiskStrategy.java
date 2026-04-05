package com.environmental.strategies;

import com.environmental.models.EnvironmentalData;

public interface RiskStrategy {
    double calculateRisk(EnvironmentalData data);
    String getRiskFactor();
}