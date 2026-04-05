package com.environmental.database;

import com.environmental.models.EnvironmentalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CityRiskHistoryDAO {
    private static final Logger logger = LoggerFactory.getLogger(CityRiskHistoryDAO.class);
    private final JdbcTemplate jdbcTemplate;

    public CityRiskHistoryDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveRiskData(int cityId, double riskScore, EnvironmentalData data) {
        String sql = "INSERT INTO city_risk_history (city_id, risk_score, temperature, aqi, humidity, wind_speed, pressure) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sql, cityId, riskScore,
                    data.getTemperature(), data.getAqi(), data.getHumidity(),
                    data.getWindSpeed(), data.getPressure());
            logger.debug("Saved risk data for city_id={}, risk={}", cityId, riskScore);
        } catch (Exception e) {
            logger.error("Failed to save risk data for city_id={}: {}", cityId, e.getMessage());
        }
    }

    public List<EnvironmentalData> getHistoricalData(int cityId, int days) {
        String sql = "SELECT temperature, aqi, humidity, wind_speed, pressure, recorded_at " +
                "FROM city_risk_history WHERE city_id = ? AND recorded_at >= DATEADD('DAY', -?, CURRENT_TIMESTAMP) " +
                "ORDER BY recorded_at ASC";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                EnvironmentalData data = new EnvironmentalData(
                        rs.getDouble("temperature"),
                        rs.getDouble("aqi"),
                        rs.getDouble("humidity"),
                        rs.getDouble("wind_speed"),
                        rs.getDouble("pressure")
                );
                Timestamp ts = rs.getTimestamp("recorded_at");
                if (ts != null) {
                    data.setTimestamp(ts.toLocalDateTime());
                }
                return data;
            }, cityId, days);
        } catch (Exception e) {
            logger.error("Failed to get historical data for city_id={}: {}", cityId, e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Double> getHistoricalRiskScores(int cityId, int days) {
        String sql = "SELECT risk_score FROM city_risk_history WHERE city_id = ? " +
                "AND recorded_at >= DATEADD('DAY', -?, CURRENT_TIMESTAMP) ORDER BY recorded_at ASC";
        try {
            return jdbcTemplate.queryForList(sql, Double.class, cityId, days);
        } catch (Exception e) {
            logger.error("Failed to get risk scores for city_id={}: {}", cityId, e.getMessage());
            return new ArrayList<>();
        }
    }

    public long getDataPointCount(int cityId) {
        String sql = "SELECT COUNT(*) FROM city_risk_history WHERE city_id = ?";
        try {
            Long count = jdbcTemplate.queryForObject(sql, Long.class, cityId);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}