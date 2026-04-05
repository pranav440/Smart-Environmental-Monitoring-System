package com.environmental.service;

import com.environmental.database.CityRiskHistoryDAO;
import com.environmental.models.EnvironmentalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrendAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(TrendAnalysisService.class);
    private final CityRiskHistoryDAO historyDAO;

    public TrendAnalysisService(CityRiskHistoryDAO historyDAO) {
        this.historyDAO = historyDAO;
    }

    public TrendResult analyzeTrend(int cityId, int days) {
        List<Double> riskScores = historyDAO.getHistoricalRiskScores(cityId, days);

        if (riskScores.size() < 3) {
            return new TrendResult("insufficient_data", 0, 0, 0, 0, riskScores.size());
        }

        double mean = riskScores.stream().mapToDouble(d -> d).average().orElse(0);
        double variance = riskScores.stream().mapToDouble(d -> Math.pow(d - mean, 2)).average().orElse(0);
        double stdDev = Math.sqrt(variance);

        // Linear regression for slope
        double slope = calculateSlope(riskScores);
        double latest = riskScores.get(riskScores.size() - 1);
        double earliest = riskScores.get(0);

        String direction;
        if (slope > 1.0) direction = "increasing";
        else if (slope < -1.0) direction = "decreasing";
        else direction = "stable";

        return new TrendResult(direction, slope, mean, variance, stdDev, riskScores.size());
    }

    public TrendResult analyzeEnvironmentalTrend(int cityId, int days) {
        List<EnvironmentalData> history = historyDAO.getHistoricalData(cityId, days);

        if (history.size() < 3) {
            return new TrendResult("insufficient_data", 0, 0, 0, 0, history.size());
        }

        double[] temps = history.stream().mapToDouble(EnvironmentalData::getTemperature).toArray();
        double[] aqis = history.stream().mapToDouble(EnvironmentalData::getAqi).toArray();

        double tempSlope = calculateSlopeFromArray(temps);
        double aqiSlope = calculateSlopeFromArray(aqis);

        String direction;
        double combinedSlope = (tempSlope + aqiSlope) / 2;
        if (combinedSlope > 0.5) direction = "increasing";
        else if (combinedSlope < -0.5) direction = "decreasing";
        else direction = "stable";

        double mean = java.util.Arrays.stream(temps).average().orElse(0);
        double variance = java.util.Arrays.stream(temps).map(t -> Math.pow(t - mean, 2)).average().orElse(0);

        return new TrendResult(direction, combinedSlope, mean, variance, Math.sqrt(variance), history.size());
    }

    private double calculateSlope(List<Double> values) {
        int n = values.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += values.get(i);
            sumXY += i * values.get(i);
            sumX2 += (double) i * i;
        }
        double denominator = n * sumX2 - sumX * sumX;
        if (denominator == 0) return 0;
        return (n * sumXY - sumX * sumY) / denominator;
    }

    private double calculateSlopeFromArray(double[] values) {
        int n = values.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += values[i];
            sumXY += i * values[i];
            sumX2 += (double) i * i;
        }
        double denominator = n * sumX2 - sumX * sumX;
        if (denominator == 0) return 0;
        return (n * sumXY - sumX * sumY) / denominator;
    }

    public static class TrendResult {
        public String direction;
        public double slope;
        public double mean;
        public double variance;
        public double standardDeviation;
        public int dataPoints;

        public TrendResult(String direction, double slope, double mean, double variance, double standardDeviation, int dataPoints) {
            this.direction = direction;
            this.slope = slope;
            this.mean = mean;
            this.variance = variance;
            this.standardDeviation = standardDeviation;
            this.dataPoints = dataPoints;
        }
    }
}
