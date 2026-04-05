package com.environmental.api.dto;

import java.time.LocalDateTime;

/**
 * Standardized Alert DTO to ensure consistent JSON property names
 * for the frontend, avoiding case-sensitivity issues with raw DB Maps.
 */
public class AlertDTO {
    private String cityName;
    private String alertType;
    private String severity;
    private double riskScore;
    private String message;
    private String alertTime;

    public AlertDTO() {}

    public AlertDTO(String cityName, String alertType, String severity, double riskScore, String message, String alertTime) {
        this.cityName = cityName;
        this.alertType = alertType;
        this.severity = severity;
        this.riskScore = riskScore;
        this.message = message;
        this.alertTime = alertTime;
    }

    // Getters and Setters
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public double getRiskScore() { return riskScore; }
    public void setRiskScore(double riskScore) { this.riskScore = riskScore; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getAlertTime() { return alertTime; }
    public void setAlertTime(String alertTime) { this.alertTime = alertTime; }
}
