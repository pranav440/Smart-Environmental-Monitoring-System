package com.environmental.database;

import com.environmental.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class CityDAO {
    private static final Logger logger = LoggerFactory.getLogger(CityDAO.class);
    private final JdbcTemplate jdbcTemplate;

    public CityDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<City> findAll() {
        String sql = "SELECT * FROM cities ORDER BY name";
        return jdbcTemplate.query(sql, new CityRowMapper());
    }

    public Optional<City> findByName(String name) {
        String sql = "SELECT * FROM cities WHERE name = ?";
        List<City> results = jdbcTemplate.query(sql, new CityRowMapper(), name);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<City> findById(int id) {
        String sql = "SELECT * FROM cities WHERE id = ?";
        List<City> results = jdbcTemplate.query(sql, new CityRowMapper(), id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    private static class CityRowMapper implements RowMapper<City> {
        @Override
        public City mapRow(ResultSet rs, int rowNum) throws SQLException {
            String type = rs.getString("city_type");
            String name = rs.getString("name");
            double lat = rs.getDouble("latitude");
            double lon = rs.getDouble("longitude");
            int id = rs.getInt("id");

            City city;
            switch (type) {
                case "Coastal":
                    city = new CoastalCity(name, lat, lon,
                            rs.getDouble("sea_level"), rs.getBoolean("cyclone_prone"));
                    break;
                case "Himalayan":
                    city = new HimalayanCity(name, lat, lon,
                            rs.getDouble("altitude"), rs.getBoolean("avalanche_risk"));
                    break;
                default:
                    city = new MetroCity(name, lat, lon,
                            rs.getInt("population_density"), rs.getInt("vehicle_count"));
                    break;
            }
            city.setId(id);
            return city;
        }
    }
}
