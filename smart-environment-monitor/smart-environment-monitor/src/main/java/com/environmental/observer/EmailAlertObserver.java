package com.environmental.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailAlertObserver implements AlertObserver {
    private static final Logger logger = LoggerFactory.getLogger(EmailAlertObserver.class);
    
    @Override
    public void onAlert(AlertEvent event) {
        logger.info("📧 EMAIL ALERT: {} - {}", event.getCity().getName(), event.getMessage());
    }
}