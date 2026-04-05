package com.environmental.api;

import com.environmental.api.dto.AlertDTO;
import com.environmental.api.dto.ApiResponse;
import com.environmental.database.AlertLogDAO;

import com.environmental.models.*;
import com.environmental.service.EnvironmentalServiceImpl;
import com.environmental.service.EnvironmentalServiceImpl.CityDetail;
import com.environmental.service.EnvironmentalServiceImpl.CitySnapshot;
import com.environmental.service.TrendAnalysisService;
import com.environmental.observer.DashboardAlertObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class EnvironmentalAPI {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentalAPI.class);

    private final EnvironmentalServiceImpl environmentalService;
    private final TrendAnalysisService trendService;
    private final AlertLogDAO alertLogDAO;

    public EnvironmentalAPI(EnvironmentalServiceImpl environmentalService,
                            TrendAnalysisService trendService,
                            AlertLogDAO alertLogDAO) {
        this.environmentalService = environmentalService;
        this.trendService = trendService;
        this.alertLogDAO = alertLogDAO;
    }

    @GetMapping("/cities")
    public ResponseEntity<ApiResponse<List<CitySnapshot>>> getAllCities() {
        List<CitySnapshot> snapshots = environmentalService.getAllCitySnapshots();
        if (snapshots.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error("No city data available yet. System is initializing."));
        }
        boolean anyStale = snapshots.stream().anyMatch(s -> s.isStale);
        ApiResponse<List<CitySnapshot>> response = anyStale
                ? ApiResponse.stale(snapshots)
                : ApiResponse.success(snapshots);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/city/{name}")
    public ResponseEntity<ApiResponse<CityDetail>> getCityDetails(@PathVariable String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("City name is required"));
        }
        Optional<CityDetail> detail = environmentalService.getCityDetail(name.trim());
        if (detail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("City not found: " + name));
        }
        CityDetail d = detail.get();
        ApiResponse<CityDetail> response = d.isStale
                ? ApiResponse.stale(d)
                : ApiResponse.success(d);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/predictions/{cityName}")
    public ResponseEntity<ApiResponse<List<Double>>> getPredictions(@PathVariable String cityName) {
        List<Double> predictions = environmentalService.getPredictions(cityName);
        if (predictions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("City not found: " + cityName));
        }
        return ResponseEntity.ok(ApiResponse.success(predictions));
    }

    @GetMapping("/anomalies")
    public ResponseEntity<ApiResponse<List<CitySnapshot>>> getAnomalies() {
        List<CitySnapshot> anomalies = environmentalService.getAnomalies();
        return ResponseEntity.ok(ApiResponse.success(anomalies));
    }

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<AlertDTO>>> getRecentAlerts(
            @RequestParam(defaultValue = "50") int limit) {
        List<AlertDTO> alerts = alertLogDAO.getRecentAlerts(Math.min(limit, 200));
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @GetMapping("/rankings")
    public ResponseEntity<ApiResponse<List<CitySnapshot>>> getCityRankings(
            @RequestParam(defaultValue = "20") int limit) {
        List<CitySnapshot> rankings = environmentalService.getCityRankings(Math.min(limit, 50));
        return ResponseEntity.ok(ApiResponse.success(rankings));
    }

    @GetMapping("/trends/{cityName}")
    public ResponseEntity<ApiResponse<TrendAnalysisService.TrendResult>> getTrends(
            @PathVariable String cityName,
            @RequestParam(defaultValue = "30") int days) {
        Optional<City> cityOpt = environmentalService.getCityByName(cityName);
        if (cityOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("City not found: " + cityName));
        }
        TrendAnalysisService.TrendResult trend = trendService.analyzeTrend(cityOpt.get().getId(), days);
        return ResponseEntity.ok(ApiResponse.success(trend));
    }

    @GetMapping("/download/csv")
    public ResponseEntity<String> downloadCSV() {
        StringBuilder csv = new StringBuilder();
        csv.append("City,Type,Temperature (C),AQI,Humidity (%),Wind Speed (km/h),Risk Score,Risk Level,Stale,Timestamp\n");

        List<CitySnapshot> snapshots = environmentalService.getAllCitySnapshots();
        for (CitySnapshot s : snapshots) {
            csv.append(s.name).append(",");
            csv.append(s.cityType).append(",");
            csv.append(String.format("%.1f", s.temperature)).append(",");
            csv.append(String.format("%.0f", s.aqi)).append(",");
            csv.append(String.format("%.0f", s.humidity)).append(",");
            csv.append(String.format("%.1f", s.windSpeed)).append(",");
            csv.append(String.format("%.1f", s.riskScore)).append(",");
            csv.append(s.riskLevel).append(",");
            csv.append(s.isStale).append(",");
            csv.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            csv.append("\n");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "environmental_data_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
        return new ResponseEntity<>(csv.toString(), headers, HttpStatus.OK);
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("totalCities", environmentalService.getAllCities().size());
        health.put("citiesWithData", environmentalService.getAllCitySnapshots().size());
        health.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(ApiResponse.success(health));
    }
}