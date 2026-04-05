package com.environmental.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RealTimeWeatherClient {
    private static final Logger logger = LoggerFactory.getLogger(RealTimeWeatherClient.class);

    @Value("${weather.api.key}")
    private String apiKey;

    private static final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String AIR_POLLUTION_URL = "http://api.openweathermap.org/data/2.5/air_pollution";
    private final OkHttpClient client;

    public RealTimeWeatherClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Fetches real weather data AND real AQI from OpenWeatherMap APIs.
     * Throws RuntimeException on failure — caller decides recovery strategy.
     */
    public WeatherData getWeatherAndAQI(String cityName, double lat, double lon) {
        // Fetch weather data
        double temperature, humidity, pressure, windSpeed;
        try {
            String url = String.format("%s?q=%s,IN&units=metric&appid=%s", WEATHER_URL, cityName, apiKey);
            Request request = new Request.Builder().url(url).build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    throw new RuntimeException("Weather API returned " + response.code() + " for " + cityName);
                }
                String jsonData = response.body().string();
                JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
                JsonObject main = json.getAsJsonObject("main");
                JsonObject wind = json.getAsJsonObject("wind");

                temperature = main.get("temp").getAsDouble();
                humidity = main.get("humidity").getAsDouble();
                pressure = main.get("pressure").getAsDouble();
                windSpeed = wind.get("speed").getAsDouble();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch weather for " + cityName + ": " + e.getMessage(), e);
        }

        // Fetch real AQI from Air Pollution API
        double aqi = fetchRealAQI(lat, lon);

        logger.info("REAL DATA: {} - {:.1f}°C, AQI {:.0f}, Humidity {:.0f}%",
                cityName, temperature, aqi, humidity);
        return new WeatherData(temperature, aqi, humidity, windSpeed, pressure);
    }

    /**
     * Calls OpenWeatherMap Air Pollution API and converts PM2.5 to standard AQI (0-500).
     */
    private double fetchRealAQI(double lat, double lon) {
        try {
            String url = String.format("%s?lat=%.4f&lon=%.4f&appid=%s",
                    AIR_POLLUTION_URL, lat, lon, apiKey);
            Request request = new Request.Builder().url(url).build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    logger.warn("Air Pollution API returned {}", response.code());
                    return -1; // Unknown AQI
                }
                String jsonData = response.body().string();
                JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
                JsonArray list = json.getAsJsonArray("list");
                if (list != null && list.size() > 0) {
                    JsonObject components = list.get(0).getAsJsonObject()
                            .getAsJsonObject("components");
                    double pm25 = components.get("pm2_5").getAsDouble();
                    double pm10 = components.get("pm10").getAsDouble();
                    return convertPM25toAQI(pm25, pm10);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch AQI for ({}, {}): {}", lat, lon, e.getMessage());
        }
        return -1;
    }

    /**
     * Converts PM2.5 concentration (μg/m³) to Indian AQI (0-500 scale).
     * Uses the EPA breakpoint table for PM2.5.
     */
    private double convertPM25toAQI(double pm25, double pm10) {
        double pm25Aqi = calculatePM25AQI(pm25);
        double pm10Aqi = calculatePM10AQI(pm10);
        // Return the higher of the two (dominant pollutant)
        return Math.max(pm25Aqi, pm10Aqi);
    }

    private double calculatePM25AQI(double pm25) {
        if (pm25 <= 12.0) return linearScale(pm25, 0, 12.0, 0, 50);
        if (pm25 <= 35.4) return linearScale(pm25, 12.1, 35.4, 51, 100);
        if (pm25 <= 55.4) return linearScale(pm25, 35.5, 55.4, 101, 150);
        if (pm25 <= 150.4) return linearScale(pm25, 55.5, 150.4, 151, 200);
        if (pm25 <= 250.4) return linearScale(pm25, 150.5, 250.4, 201, 300);
        if (pm25 <= 500.4) return linearScale(pm25, 250.5, 500.4, 301, 500);
        return 500;
    }

    private double calculatePM10AQI(double pm10) {
        if (pm10 <= 54) return linearScale(pm10, 0, 54, 0, 50);
        if (pm10 <= 154) return linearScale(pm10, 55, 154, 51, 100);
        if (pm10 <= 254) return linearScale(pm10, 155, 254, 101, 150);
        if (pm10 <= 354) return linearScale(pm10, 255, 354, 151, 200);
        if (pm10 <= 424) return linearScale(pm10, 355, 424, 201, 300);
        if (pm10 <= 604) return linearScale(pm10, 425, 604, 301, 500);
        return 500;
    }

    private double linearScale(double value, double cLow, double cHigh, double iLow, double iHigh) {
        return ((iHigh - iLow) / (cHigh - cLow)) * (value - cLow) + iLow;
    }

    public static class WeatherData {
        public final double temperature;
        public final double aqi;
        public final double humidity;
        public final double windSpeed;
        public final double pressure;

        public WeatherData(double temperature, double aqi, double humidity, double windSpeed, double pressure) {
            this.temperature = temperature;
            this.aqi = aqi;
            this.humidity = humidity;
            this.windSpeed = windSpeed;
            this.pressure = pressure;
        }
    }
}