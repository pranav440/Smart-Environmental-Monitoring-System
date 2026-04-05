package com.environmental.strategies;

import com.environmental.models.City;
import com.environmental.models.EnvironmentalData;
import java.util.*;

public class CompositeRiskStrategy {
    private final Map<RiskStrategy, Double> strategies = new LinkedHashMap<>();

    public CompositeRiskStrategy() {
        // Default empty — configure via addStrategy()
    }

    public void addStrategy(RiskStrategy strategy, double weight) {
        strategies.put(strategy, weight);
    }

    public double calculateRisk(EnvironmentalData data) {
        if (strategies.isEmpty()) return 0;
        double totalRisk = 0.0;
        double totalWeight = 0.0;
        for (Map.Entry<RiskStrategy, Double> entry : strategies.entrySet()) {
            totalRisk += entry.getKey().calculateRisk(data) * entry.getValue();
            totalWeight += entry.getValue();
        }
        // Normalize if weights don't sum to 1.0
        if (totalWeight > 0 && Math.abs(totalWeight - 1.0) > 0.01) {
            totalRisk = totalRisk / totalWeight;
        }
        return Math.min(totalRisk, 100);
    }

    public double calculateRiskWithCityModifier(EnvironmentalData data, City city) {
        double baseRisk = calculateRisk(data);
        double modifier = city.calculateRiskModifier();
        return Math.min(baseRisk * modifier, 100);
    }

    public Map<String, Double> getStrategyBreakdown(EnvironmentalData data) {
        Map<String, Double> breakdown = new LinkedHashMap<>();
        for (Map.Entry<RiskStrategy, Double> entry : strategies.entrySet()) {
            double contribution = entry.getKey().calculateRisk(data) * entry.getValue();
            breakdown.put(entry.getKey().getRiskFactor(), contribution);
        }
        return breakdown;
    }

    public String getRiskFactor() {
        return "Composite Environmental Risk";
    }
}