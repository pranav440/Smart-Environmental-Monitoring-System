package com.environmental.observer;

import com.environmental.database.AlertLogDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingAlertObserver implements AlertObserver {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAlertObserver.class);
    private final AlertLogDAO alertLogDAO;

    public LoggingAlertObserver(AlertLogDAO alertLogDAO) {
        this.alertLogDAO = alertLogDAO;
    }

    @Override
    public void onAlert(AlertEvent event) {
        logger.warn("ALERT [{}] {} - City: {} - Risk: {} - {}",
                event.getSeverity(), event.getType(),
                event.getCity().getName(),
                String.format("%.1f", event.getRiskScore()),
                event.getMessage());

        // Persist to database
        alertLogDAO.saveAlert(
                event.getCity().getId(),
                event.getType().name(),
                event.getSeverity().name(),
                event.getRiskScore(),
                event.getMessage()
        );
    }
}
