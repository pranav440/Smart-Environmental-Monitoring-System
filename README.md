# 🛡️ Smart Environmental Monitoring System: AI-Driven Risk Intelligence

> **Empowering regional safety with real-time environmental telemetry and predictive machine learning models.**

![Environment Status](https://img.shields.io/badge/Environment-Live_Monitoring-emerald?style=for-the-badge)
![Tech Stack](https://img.shields.io/badge/Technologies-Spring_Boot_%7C_JS_%7C_ML-blue?style=for-the-badge)

## 🌎 Overview
**Smart Environmental Monitoring System** is a comprehensive environmental intelligence platform designed to monitor, analyze, and predict regional hazards. By integrating real-time air quality, thermal data, and meteorological telemetry, the system provides a unified "Risk Score" to help decision-makers identify vulnerable clusters before crises occur.

## 🚀 Key Features

- **Live Intelligence Dashboard**: A bento-style UI providing real-time KPIs for average risk, active anomalies, and station health.
- **Predictive ML Analytics**: Integrated 7-day risk forecasting and seasonal trend analysis using statistical modeling.
- **Interactive High-Resolution Mapping**: Native Leaflet.js integration with Satellite, Street, and Dark-mode layers for spatial awareness.
- **Smart Alerting Engine**: Real-time detection of temperature spikes, hazardous AQI levels, and humidity anomalies with localized advisory messaging.
- **Neural Data Visuals**: Dynamic radar, hourly charts, and yearly trends powered by Chart.js for deep-dive analytics.
- **On-Demand Reporting**: Export intelligence data into professional, high-metadata PDF reports.

## 🛠️ Technical Architecture

### **Backend (The Core)**
- **Framework**: Spring Boot 3.1.5 (Java)
- **Database**: H2 (In-memory development) / MySQL (Production Ready)
- **API Architecture**: RESTful Services with robust DTO transformation and JSON response wrappers.
- **Resilient Logic**: Centralized global exception handling and API health diagnostics.

### **Frontend (The Experience)**
- **Logic**: Vanilla ES6+ JavaScript (zero framework bloat)
- **Styling**: Tailwind CSS (Modern, responsive, glassmorphism)
- **GIS**: Leaflet with high-perf tile rendering.
- **Charts**: Chart.js for data-heavy visualizations.

## 📦 Local Installation

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Setup
1. **Clone the repository**:
   ```bash
   git clone https://github.com/pranav440/Smart-Environmental-Analysis.git
   cd Smart-Environmental-Analysis
   ```
2. **Build and Run**:
   ```bash
   mvn spring-boot:run
   ```
3. **Access**:
   Open `http://localhost:8080` in your browser.

## ⚠️ Configuration
To enable live weather data, set your `WEATHER_API_KEY` (OpenWeatherMap) as an environment variable or update `src/main/resources/application.properties`.

---
*Built with ❤️ for Environmental Resilience.*
