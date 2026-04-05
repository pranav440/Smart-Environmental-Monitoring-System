package com.environmental.models;

import java.time.LocalDateTime;

public class EnvironmentalData {
    private double temperature;
    private double aqi;
    private double humidity;
    private double windSpeed;
    private double pressure;
    private LocalDateTime timestamp;
    
    public EnvironmentalData(double temperature, double aqi, double humidity, double windSpeed) {
        this(temperature, aqi, humidity, windSpeed, 1013.0);
    }
    
    public EnvironmentalData(double temperature, double aqi, double humidity, double windSpeed, double pressure) {
        this.temperature = temperature;
        this.aqi = aqi;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.pressure = pressure;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
    public double getTemperature() { return temperature; }
    public double getAqi() { return aqi; }
    public double getHumidity() { return humidity; }
    public double getWindSpeed() { return windSpeed; }
    public double getPressure() { return pressure; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    // Setters
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public void setAqi(double aqi) { this.aqi = aqi; }
    public void setHumidity(double humidity) { this.humidity = humidity; }
    public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }
    public void setPressure(double pressure) { this.pressure = pressure; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}