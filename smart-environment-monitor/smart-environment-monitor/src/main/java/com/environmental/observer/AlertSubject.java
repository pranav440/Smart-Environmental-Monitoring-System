package com.environmental.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AlertSubject {
    private final List<AlertObserver> observers = new CopyOnWriteArrayList<>();

    public void attach(AlertObserver observer) {
        observers.add(observer);
    }

    public void detach(AlertObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(AlertEvent event) {
        for (AlertObserver observer : observers) {
            try {
                observer.onAlert(event);
            } catch (Exception e) {
                // Prevent one observer failure from blocking others
                System.err.println("Observer notification failed: " + e.getMessage());
            }
        }
    }
}