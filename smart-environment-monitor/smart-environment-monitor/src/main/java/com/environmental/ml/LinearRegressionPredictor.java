package com.environmental.ml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Modernized Linear Regression Predictor for Environmental Risk.
 * Provides 7-day forecasting and anomaly detection using historical trends.
 */
@Component
public class LinearRegressionPredictor {
    private static final Logger logger = LoggerFactory.getLogger(LinearRegressionPredictor.class);

    /**
     * Generates a 7-day risk forecast based on historical data.
     */
    public List<Double> predictNext7Days(List<Double> history) {
        if (history == null || history.size() < 3) {
            // Fallback: Generate a slightly oscillating trend based on the latest value
            double lastVal = (history != null && !history.isEmpty()) ? history.get(history.size() - 1) : 50.0;
            return generateFallbackForecast(lastVal);
        }

        int n = history.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += history.get(i);
            sumXY += (double) i * history.get(i);
            sumX2 += (double) i * i;
        }

        double denominator = n * sumX2 - sumX * sumX;
        if (denominator == 0) return generateFallbackForecast(history.get(n - 1));

        double slope = (n * sumXY - sumX * sumY) / denominator;
        double intercept = (sumY - slope * sumX) / n;

        List<Double> forecast = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            double pred = slope * (n + i - 1) + intercept;
            // Cap risk between 0 and 100
            forecast.add(Math.max(0.0, Math.min(100.0, pred)));
        }
        return forecast;
    }

    /**
     * Detects if the current value is an anomaly based on historical standard deviation.
     */
    public boolean isAnomaly(double current, List<Double> history) {
        if (history == null || history.size() < 5) return false;

        double mean = history.stream().mapToDouble(d -> d).average().orElse(0.0);
        double variance = history.stream().mapToDouble(d -> Math.pow(d - mean, 2)).average().orElse(0.0);
        double stdDev = Math.sqrt(variance);

        // Threshold: 2.5 standard deviations from mean
        return Math.abs(current - mean) > (2.5 * stdDev);
    }

    private List<Double> generateFallbackForecast(double lastVal) {
        List<Double> forecast = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            // Subtle random variation around the last value
            double variation = (Math.random() - 0.5) * 5.0;
            forecast.add(Math.max(0.0, Math.min(100.0, lastVal + variation)));
        }
        return forecast;
    }
}
