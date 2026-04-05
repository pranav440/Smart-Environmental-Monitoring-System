package com.environmental.models;

public class MetroCity extends City {
    private int populationDensity;
    private int vehicleCount;
    
    public MetroCity(String name, double latitude, double longitude, int populationDensity, int vehicleCount) {
        super(name, latitude, longitude);
        this.populationDensity = populationDensity;
        this.vehicleCount = vehicleCount;
    }
    
    @Override
    public String getCityType() {
        return "Metro";
    }
    
    @Override
    public double calculateRiskModifier() {
        double modifier = 1.0;
        modifier += (populationDensity / 10000.0) * 0.1;
        modifier += (vehicleCount / 100000.0) * 0.05;
        return Math.min(modifier, 1.5);
    }
}