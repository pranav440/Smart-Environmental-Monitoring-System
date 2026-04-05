package com.environmental.service;

import com.environmental.database.AlertLogDAO;
import com.environmental.models.City;
import com.environmental.models.EnvironmentalData;
import com.environmental.observer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class AlertService {
    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);
    private final AlertSubject alertSubject = new AlertSubject();
    private final AlertLogDAO alertLogDAO;

    private double extremeRiskThreshold;
    private double heatwaveThreshold;
    private double highAqiThreshold;

    public AlertService(AlertLogDAO alertLogDAO) {
        this.alertLogDAO = alertLogDAO;
    }

    @PostConstruct
    public void init() {
        // Load configurable thresholds from DB
        extremeRiskThreshold = alertLogDAO.getThreshold("EXTREME_RISK");
        heatwaveThreshold = alertLogDAO.getThreshold("HEATWAVE");
        highAqiThreshold = alertLogDAO.getThreshold("HIGH_AQI");

        // Register observers
        alertSubject.attach(new EmailAlertObserver());
        alertSubject.attach(new DashboardAlertObserver());
        alertSubject.attach(new LoggingAlertObserver(alertLogDAO));

        logger.info("AlertService initialized with thresholds: risk={}, temp={}, aqi={}",
                extremeRiskThreshold, heatwaveThreshold, highAqiThreshold);
    }

    public void checkAndTriggerAlerts(City city, double riskScore, EnvironmentalData data) {
        if (riskScore > extremeRiskThreshold) {
            AlertEvent event = new AlertEvent(
                    AlertEvent.AlertType.EXTREME_RISK, city, riskScore,
                    "EXTREME RISK! " + city.getName() + " - Score: " + String.format("%.1f", riskScore),
                    AlertEvent.Severity.CRITICAL
            );
            alertSubject.notifyObservers(event);
        }

        if (data.getTemperature() > heatwaveThreshold) {
            AlertEvent event = new AlertEvent(
                    AlertEvent.AlertType.HEATWAVE, city, riskScore,
                    "HEATWAVE! " + city.getName() + ": " + String.format("%.1f", data.getTemperature()) + "°C",
                    data.getTemperature() > 45 ? AlertEvent.Severity.CRITICAL : AlertEvent.Severity.HIGH
            );
            alertSubject.notifyObservers(event);
        }

        if (data.getAqi() > highAqiThreshold) {
            AlertEvent event = new AlertEvent(
                    AlertEvent.AlertType.HIGH_AQI, city, riskScore,
                    "POOR AIR QUALITY! " + city.getName() + ": AQI " + String.format("%.0f", data.getAqi()),
                    data.getAqi() > 300 ? AlertEvent.Severity.CRITICAL : AlertEvent.Severity.HIGH
            );
            alertSubject.notifyObservers(event);
        }

        if (data.getHumidity() < 20) {
            AlertEvent event = new AlertEvent(
                    AlertEvent.AlertType.DROUGHT_RISK, city, riskScore,
                    "DROUGHT RISK! " + city.getName() + ": Humidity " + String.format("%.0f", data.getHumidity()) + "%",
                    AlertEvent.Severity.MEDIUM
            );
            alertSubject.notifyObservers(event);
        }
    }

    public void triggerAnomalyAlert(City city, double riskScore, String detail) {
        AlertEvent event = new AlertEvent(
                AlertEvent.AlertType.ANOMALY_DETECTED, city, riskScore,
                "ANOMALY DETECTED! " + city.getName() + ": " + detail,
                AlertEvent.Severity.HIGH
        );
        alertSubject.notifyObservers(event);
    }

    public AlertSubject getAlertSubject() {
        return alertSubject;
    }
}