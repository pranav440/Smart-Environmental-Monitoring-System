package com.environmental.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DashboardAlertObserver implements AlertObserver {
    private static final Logger logger = LoggerFactory.getLogger(DashboardAlertObserver.class);
    private static ConcurrentLinkedQueue<AlertEvent> alertQueue = new ConcurrentLinkedQueue<>();
    
    @Override
    public void onAlert(AlertEvent event) {
        alertQueue.add(event);
        logger.info("📊 Dashboard alert: {} - {}", event.getCity().getName(), event.getMessage());
        while (alertQueue.size() > 50) {
            alertQueue.poll();
        }
    }
    
    public static ConcurrentLinkedQueue<AlertEvent> getAlertQueue() {
        return alertQueue;
    }
}