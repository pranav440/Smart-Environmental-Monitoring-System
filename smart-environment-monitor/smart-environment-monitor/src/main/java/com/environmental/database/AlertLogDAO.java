package com.environmental.database;

import com.environmental.api.dto.AlertDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class AlertLogDAO {
    private static final Logger logger = LoggerFactory.getLogger(AlertLogDAO.class);
    private final JdbcTemplate jdbcTemplate;

    public AlertLogDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveAlert(int cityId, String alertType, String severity, double riskScore, String message) {
        String sql = "INSERT INTO alert_logs (city_id, alert_type, severity, risk_score, message) VALUES (?, ?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sql, cityId, alertType, severity, riskScore, message);
            logger.info("Alert saved: city_id={}, type={}, severity={}", cityId, alertType, severity);
        } catch (Exception e) {
            logger.error("Failed to save alert for city_id={}: {}", cityId, e.getMessage());
        }
    }

    public List<AlertDTO> getRecentAlerts(int limit) {
        String sql = "SELECT al.*, c.name as city_name FROM alert_logs al " +
                "JOIN cities c ON al.city_id = c.id " +
                "ORDER BY al.alert_time DESC LIMIT ?";
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, limit);
            List<AlertDTO> alerts = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                alerts.add(mapToDto(row));
            }
            return alerts;
        } catch (Exception e) {
            logger.error("Failed to get recent alerts: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<AlertDTO> getAlertsByCityId(int cityId, int limit) {
        String sql = "SELECT al.*, c.name as city_name FROM alert_logs al " +
                "JOIN cities c ON al.city_id = c.id " +
                "WHERE al.city_id = ? ORDER BY al.alert_time DESC LIMIT ?";
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, cityId, limit);
            List<AlertDTO> alerts = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                alerts.add(mapToDto(row));
            }
            return alerts;
        } catch (Exception e) {
            logger.error("Failed to get alerts for city_id={}: {}", cityId, e.getMessage());
            return new ArrayList<>();
        }
    }

    private AlertDTO mapToDto(Map<String, Object> row) {
        // Handle case-insensitive keys (some drivers return UPPERCASE)
        String cityName = getString(row, "city_name");
        String alertType = getString(row, "alert_type");
        String severity = getString(row, "severity");
        double riskScore = getDouble(row, "risk_score");
        String message = getString(row, "message");
        String alertTime = row.get("ALERT_TIME") != null ? row.get("ALERT_TIME").toString() : 
                          (row.get("alert_time") != null ? row.get("alert_time").toString() : "");

        return new AlertDTO(cityName, alertType, severity, riskScore, message, alertTime);
    }

    private String getString(Map<String, Object> row, String key) {
        Object val = row.get(key.toUpperCase());
        if (val == null) val = row.get(key.toLowerCase());
        return val != null ? val.toString() : "";
    }

    private double getDouble(Map<String, Object> row, String key) {
        Object val = row.get(key.toUpperCase());
        if (val == null) val = row.get(key.toLowerCase());
        if (val instanceof Number) return ((Number) val).doubleValue();
        return 0.0;
    }

    public double getThreshold(String alertType) {
        String sql = "SELECT threshold_value FROM alert_thresholds WHERE alert_type = ?";
        try {
            Double val = jdbcTemplate.queryForObject(sql, Double.class, alertType);
            return val != null ? val : getDefaultThreshold(alertType);
        } catch (Exception e) {
            return getDefaultThreshold(alertType);
        }
    }

    private double getDefaultThreshold(String alertType) {
        switch (alertType) {
            case "EXTREME_RISK": return 75.0;
            case "HEATWAVE": return 40.0;
            case "HIGH_AQI": return 200.0;
            case "ANOMALY_DETECTED": return 3.5;
            case "DROUGHT_RISK": return 20.0;
            default: return 50.0;
        }
    }
}
