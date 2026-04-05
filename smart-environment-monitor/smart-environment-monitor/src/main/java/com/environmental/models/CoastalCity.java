package com.environmental.models;

public class CoastalCity extends City {
    private double seaLevel;
    private boolean cycloneProne;
    
    public CoastalCity(String name, double latitude, double longitude, double seaLevel, boolean cycloneProne) {
        super(name, latitude, longitude);
        this.seaLevel = seaLevel;
        this.cycloneProne = cycloneProne;
    }
    
    @Override
    public String getCityType() {
        return "Coastal";
    }
    
    @Override
    public double calculateRiskModifier() {
        double modifier = 1.0;
        if (seaLevel > 5.0) modifier += 0.2;
        if (cycloneProne) modifier += 0.3;
        return Math.min(modifier, 1.4);
    }
}