package com.environmental.observer;

import com.environmental.models.City;
import java.time.LocalDateTime;

public class AlertEvent {
    public enum AlertType {
        HEATWAVE, HIGH_AQI, EXTREME_RISK, ANOMALY_DETECTED, DROUGHT_RISK, GLACIER_MELT
    }

    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    private AlertType type;
    private City city;
    private double riskScore;
    private String message;
    private Severity severity;
    private LocalDateTime timestamp;

    public AlertEvent(AlertType type, City city, double riskScore, String message, Severity severity) {
        this.type = type;
        this.city = city;
        this.riskScore = riskScore;
        this.message = message;
        this.severity = severity;
        this.timestamp = LocalDateTime.now();
    }

    // Backward-compatible constructor
    public AlertEvent(AlertType type, City city, double riskScore, String message) {
        this(type, city, riskScore, message, deriveSeverity(riskScore));
    }

    private static Severity deriveSeverity(double riskScore) {
        if (riskScore >= 90) return Severity.CRITICAL;
        if (riskScore >= 75) return Severity.HIGH;
        if (riskScore >= 50) return Severity.MEDIUM;
        return Severity.LOW;
    }

    public AlertType getType() { return type; }
    public City getCity() { return city; }
    public double getRiskScore() { return riskScore; }
    public String getMessage() { return message; }
    public Severity getSeverity() { return severity; }
    public LocalDateTime getTimestamp() { return timestamp; }
}