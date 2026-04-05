-- Environmental Monitoring System - Production Schema

CREATE TABLE IF NOT EXISTS cities (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    city_type VARCHAR(20) NOT NULL,
    population_density INT DEFAULT 0,
    vehicle_count INT DEFAULT 0,
    sea_level DOUBLE DEFAULT 0.0,
    cyclone_prone BOOLEAN DEFAULT FALSE,
    altitude DOUBLE DEFAULT 0.0,
    avalanche_risk BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS city_risk_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    city_id INT NOT NULL,
    risk_score DOUBLE NOT NULL,
    temperature DOUBLE,
    aqi DOUBLE,
    humidity DOUBLE,
    wind_speed DOUBLE,
    pressure DOUBLE,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (city_id) REFERENCES cities(id) ON DELETE CASCADE,
    INDEX idx_city_date (city_id, recorded_at)
);

CREATE TABLE IF NOT EXISTS alert_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    city_id INT NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    risk_score DOUBLE,
    message TEXT,
    alert_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (city_id) REFERENCES cities(id) ON DELETE CASCADE,
    INDEX idx_city_alert_time (city_id, alert_time),
    INDEX idx_alert_time (alert_time)
);

CREATE TABLE IF NOT EXISTS alert_thresholds (
    id INT AUTO_INCREMENT PRIMARY KEY,
    alert_type VARCHAR(50) UNIQUE NOT NULL,
    threshold_value DOUBLE NOT NULL,
    description VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
