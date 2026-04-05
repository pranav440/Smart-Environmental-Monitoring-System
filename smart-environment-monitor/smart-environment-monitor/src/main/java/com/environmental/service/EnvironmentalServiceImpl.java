package com.environmental.service;

import com.environmental.api.RealTimeWeatherClient;
import com.environmental.database.CityDAO;
import com.environmental.database.CityRiskHistoryDAO;
import com.environmental.ml.LinearRegressionPredictor;

import com.environmental.models.City;
import com.environmental.models.EnvironmentalData;
import com.environmental.strategies.CompositeRiskStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EnvironmentalServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentalServiceImpl.class);

    private final CityDAO cityDAO;
    private final CityRiskHistoryDAO historyDAO;
    private final RealTimeWeatherClient weatherClient;
    private final CompositeRiskStrategy riskStrategy;
    private final AlertService alertService;
    private final LinearRegressionPredictor predictor;


    // In-memory city registry and state cache
    private final ConcurrentHashMap<Integer, City> cityRegistry = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CityState> cityStates = new ConcurrentHashMap<>();

    public EnvironmentalServiceImpl(CityDAO cityDAO, CityRiskHistoryDAO historyDAO,
                                     RealTimeWeatherClient weatherClient,
                                     CompositeRiskStrategy riskStrategy,
                                     AlertService alertService,
                                     LinearRegressionPredictor predictor) {
        this.cityDAO = cityDAO;
        this.historyDAO = historyDAO;
        this.weatherClient = weatherClient;
        this.riskStrategy = riskStrategy;
        this.alertService = alertService;
        this.predictor = predictor;
    }

    @PostConstruct
    public void initialize() {
        loadCitiesFromDB();
        logger.info("Loaded {} cities from database", cityRegistry.size());
        // Trigger initial data refresh asynchronously
        refreshAllCityData();
    }

    private void loadCitiesFromDB() {
        List<City> cities = cityDAO.findAll();
        for (City city : cities) {
            cityRegistry.put(city.getId(), city);
            // Load recent historical data from DB into city object
            List<EnvironmentalData> history = historyDAO.getHistoricalData(city.getId(), 30);
            for (EnvironmentalData data : history) {
                city.updateEnvironmentalData(data);
            }
        }
    }

    @Scheduled(fixedDelayString = "${data.refresh.interval}")
    public void refreshAllCityData() {
        logger.info("Refreshing environmental data for {} cities...", cityRegistry.size());
        int success = 0, failed = 0;

        for (City city : cityRegistry.values()) {
            try {
                RealTimeWeatherClient.WeatherData weather =
                        weatherClient.getWeatherAndAQI(city.getName(), city.getLatitude(), city.getLongitude());

                EnvironmentalData data = new EnvironmentalData(
                        weather.temperature, weather.aqi, weather.humidity,
                        weather.windSpeed, weather.pressure);

                double risk = riskStrategy.calculateRiskWithCityModifier(data, city);
                city.updateEnvironmentalData(data);
                city.setRiskScore(risk);

                // Persist to DB
                historyDAO.saveRiskData(city.getId(), risk, data);

                // Check alerts
                alertService.checkAndTriggerAlerts(city, risk, data);

                // Update state cache
                cityStates.put(city.getId(), new CityState(data, risk, false, LocalDateTime.now()));
                success++;

            } catch (Exception e) {
                logger.warn("Failed to refresh data for {}: {}", city.getName(), e.getMessage());
                CityState existing = cityStates.get(city.getId());
                if (existing != null) {
                    existing.markStale();
                }
                failed++;
            }
        }
        logger.info("Data refresh complete: {} success, {} failed", success, failed);
    }

    public List<CitySnapshot> getAllCitySnapshots() {
        List<CitySnapshot> snapshots = new ArrayList<>();
        for (City city : cityRegistry.values()) {
            CityState state = cityStates.get(city.getId());
            if (state != null) {
                snapshots.add(new CitySnapshot(city, state));
            }
        }
        snapshots.sort(Comparator.comparing(s -> s.name));
        return snapshots;
    }

    public Optional<CityDetail> getCityDetail(String name) {
        for (City city : cityRegistry.values()) {
            if (city.getName().equalsIgnoreCase(name)) {
                CityState state = cityStates.get(city.getId());
                if (state == null) return Optional.empty();

                List<Double> riskHistory = historyDAO.getHistoricalRiskScores(city.getId(), 30);
                List<Double> forecast = predictor.predictNext7Days(riskHistory);
                boolean isAnomaly = predictor.isAnomaly(state.riskScore, riskHistory);
                
                return Optional.of(new CityDetail(city, state, forecast, isAnomaly));
            }
        }
        return Optional.empty();
    }
    public List<Double> getPredictions(String cityName) {
        Optional<City> cityOpt = getCityByName(cityName);
        if (cityOpt.isEmpty()) return Collections.emptyList();
        List<Double> history = historyDAO.getHistoricalRiskScores(cityOpt.get().getId(), 30);
        return predictor.predictNext7Days(history);
    }

    public List<CitySnapshot> getAnomalies() {
        List<CitySnapshot> anomalies = new ArrayList<>();
        for (City city : cityRegistry.values()) {
            CityState state = cityStates.get(city.getId());
            if (state == null) continue;
            List<Double> history = historyDAO.getHistoricalRiskScores(city.getId(), 30);
            if (predictor.isAnomaly(state.riskScore, history)) {
                anomalies.add(new CitySnapshot(city, state, true));
            }
        }
        return anomalies;
    }



    public List<CitySnapshot> getCityRankings(int limit) {
        List<CitySnapshot> all = getAllCitySnapshots();
        all.sort((a, b) -> Double.compare(b.riskScore, a.riskScore));
        return all.subList(0, Math.min(limit, all.size()));
    }

    public Collection<City> getAllCities() {
        return cityRegistry.values();
    }

    public Optional<City> getCityByName(String name) {
        return cityRegistry.values().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    // --- Inner classes for data transfer ---

    public static class CityState {
        public EnvironmentalData data;
        public double riskScore;
        public boolean isStale;
        public LocalDateTime lastUpdated;

        public CityState(EnvironmentalData data, double riskScore, boolean isStale, LocalDateTime lastUpdated) {
            this.data = data;
            this.riskScore = riskScore;
            this.isStale = isStale;
            this.lastUpdated = lastUpdated;
        }

        public void markStale() { this.isStale = true; }
    }

    public static class CitySnapshot {
        public String name;
        public double latitude;
        public double longitude;
        public double temperature;
        public double aqi;
        public double humidity;
        public double windSpeed;
        public double riskScore;
        public String riskLevel;
        public String cityType;
        public String riskIcon;
        public boolean isStale;
        public boolean isAnomaly;
        public String lastUpdated;

        public CitySnapshot(City city, CityState state) {
            this(city, state, false);
        }

        public CitySnapshot(City city, CityState state, boolean isAnomaly) {
            this.name = city.getName();
            this.latitude = city.getLatitude();
            this.longitude = city.getLongitude();
            this.temperature = state.data.getTemperature();
            this.aqi = state.data.getAqi();
            this.humidity = state.data.getHumidity();
            this.windSpeed = state.data.getWindSpeed();
            this.riskScore = state.riskScore;
            this.riskLevel = city.getRiskLevel() != null ? city.getRiskLevel().text : "Unknown";
            this.cityType = city.getCityType();
            this.riskIcon = city.getRiskLevel() != null ? city.getRiskLevel().icon : "";
            this.isStale = state.isStale;
            this.isAnomaly = isAnomaly;
            this.lastUpdated = state.lastUpdated.toString();
        }
    }

    public static class CityDetail extends CitySnapshot {
        public List<Double> forecast;
        public CityDetail(City city, CityState state, List<Double> forecast, boolean isAnomaly) {
            super(city, state, isAnomaly);
            this.forecast = forecast;
        }
    }
}
