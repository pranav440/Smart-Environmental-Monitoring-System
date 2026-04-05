/**
 * api.js
 * Service layer for Environmental Monitoring System
 * Handles all backend communication with consistent error handling.
 */

const API_BASE = '/api';

const handleResponse = async (response) => {
    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || `HTTP ${response.status}: ${response.statusText}`);
    }
    const data = await response.json();
    return data;
};

const API = {
    // 1. DASHBOARD DATA
    getCities: async () => {
        const res = await fetch(`${API_BASE}/cities`);
        return handleResponse(res);
    },

    // 2. CITY DETAIL
    getCityDetail: async (cityName) => {
        const res = await fetch(`${API_BASE}/city/${encodeURIComponent(cityName)}`);
        return handleResponse(res);
    },

    // 3. ALERTS
    getAlerts: async (limit = 20) => {
        const res = await fetch(`${API_BASE}/alerts?limit=${limit}`);
        return handleResponse(res);
    },

    // 4. RANKINGS
    getRankings: async (limit = 10) => {
        const res = await fetch(`${API_BASE}/rankings?limit=${limit}`);
        return handleResponse(res);
    },

    // 5. PREDICTIONS (integrated in CityDetail, but exposing for standalone if needed)
    getPredictions: async (cityName) => {
        const res = await fetch(`${API_BASE}/predictions/${encodeURIComponent(cityName)}`);
        return handleResponse(res);
    },

    // 6. ANOMALIES
    getAnomalies: async () => {
        const res = await fetch(`${API_BASE}/anomalies`);
        return handleResponse(res);
    },

    // 7. SYSTEM HEALTH
    getHealth: async () => {
        const res = await fetch(`${API_BASE}/health`);
        return handleResponse(res);
    }
};

window.API = API; // Export to global scope
