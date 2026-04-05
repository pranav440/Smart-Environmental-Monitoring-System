package com.environmental.models;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class City {
    protected int id;
    protected String name;
    protected double latitude;
    protected double longitude;
    protected EnvironmentalData currentData;
    protected List<EnvironmentalData> historicalData;
    protected double currentRiskScore;
    protected RiskLevel riskLevel;
    
    public City(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.historicalData = new CopyOnWriteArrayList<>();
    }
    
    public abstract String getCityType();
    public abstract double calculateRiskModifier();
    
    public void updateEnvironmentalData(EnvironmentalData data) {
        this.historicalData.add(data);
        this.currentData = data;
        if (historicalData.size() > 365) {
            historicalData.remove(0);
        }
    }
    
    public void setRiskScore(double score) {
        this.currentRiskScore = score;
        this.riskLevel = RiskLevel.fromScore(score);
    }
    
    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public EnvironmentalData getCurrentData() { return currentData; }
    public List<EnvironmentalData> getHistoricalData() { return historicalData; }
    public double getCurrentRiskScore() { return currentRiskScore; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    
    // Setters
    public void setId(int id) { this.id = id; }
}