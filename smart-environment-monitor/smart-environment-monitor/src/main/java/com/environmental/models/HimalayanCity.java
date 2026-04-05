package com.environmental.models;

public class HimalayanCity extends City {
    private double altitude;
    private boolean avalancheRisk;
    
    public HimalayanCity(String name, double latitude, double longitude, double altitude, boolean avalancheRisk) {
        super(name, latitude, longitude);
        this.altitude = altitude;
        this.avalancheRisk = avalancheRisk;
    }
    
    @Override
    public String getCityType() {
        return "Himalayan";
    }
    
    @Override
    public double calculateRiskModifier() {
        double modifier = 1.0;
        if (altitude > 3000) modifier += 0.15;
        if (avalancheRisk) modifier += 0.25;
        return Math.min(modifier, 1.3);
    }
}