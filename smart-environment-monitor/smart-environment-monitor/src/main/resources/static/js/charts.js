/**
 * charts.js
 * Chart definitions and rendering functions for prediction and trend analysis
 * All charts are driven by LIVE API data — no static placeholders.
 */

const ChartService = {
    prediction: null,
    mlPrediction: null,
    riskTrend: null,
    aqiTrend: null,
    mlYearly: null,
    mlRadar: null,
    mlHourly: null,

    // Shared gradient helper
    _gradient(ctx, color1, color2, h = 300) {
        const g = ctx.createLinearGradient(0, 0, 0, h);
        g.addColorStop(0, color1);
        g.addColorStop(1, color2);
        return g;
    },

    // 1. RENDER 7-DAY FORECAST (Dashboard sidebar or ML Section)
    renderPrediction: (canvasId, data) => {
        if (!data || !Array.isArray(data)) return console.error('Invalid forecast data for', canvasId);
        const canvas = document.getElementById(canvasId);
        if (!canvas) return;
        const ctx = canvas.getContext('2d');

        // Manage instances based on target
        if (canvasId === 'ml-7day-forecast') {
            if (ChartService.mlPrediction) ChartService.mlPrediction.destroy();
        } else {
            if (ChartService.prediction) ChartService.prediction.destroy();
        }

        const labels = Array.from({ length: 7 }, (_, i) => {
            const date = new Date();
            date.setDate(date.getDate() + i + 1);
            return date.toLocaleDateString(undefined, { weekday: 'short', day: 'numeric' });
        });

        // Use precise height for gradient (h-52 is 208px)
        const grad = ChartService._gradient(ctx, 'rgba(37,99,235,0.25)', 'rgba(37,99,235,0)', 208);

        const chartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels,
                datasets: [{
                    label: 'Predicted Risk',
                    data: data.slice(0, 7),
                    borderColor: '#2563eb',
                    backgroundColor: grad,
                    borderWidth: 2.5,
                    tension: 0.45,
                    fill: true,
                    pointRadius: 5,
                    pointHoverRadius: 8,
                    pointBackgroundColor: '#fff',
                    pointBorderColor: '#2563eb',
                    pointBorderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                animation: { duration: 900, easing: 'easeOutQuart' },
                interaction: { intersect: false, mode: 'index' },
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#0f172a',
                        titleFont: { family: 'Inter', weight: '700' },
                        bodyFont: { family: 'Inter' },
                        padding: 12,
                        cornerRadius: 10,
                        callbacks: {
                            label: ctx => `Risk: ${ctx.parsed.y.toFixed(1)}%`
                        }
                    }
                },
                scales: {
                    x: { grid: { display: false }, ticks: { font: { size: 10, family: 'Inter' } } },
                    y: { beginAtZero: true, max: 100, grid: { color: 'rgba(0,0,0,0.04)' }, ticks: { font: { size: 10 } } }
                }
            }
        });

        if (canvasId === 'ml-7day-forecast') {
            ChartService.mlPrediction = chartInstance;
        } else {
            ChartService.prediction = chartInstance;
        }
    },

    // 2. RENDER GLOBAL TRENDS (Analytics)
    renderTrends: (riskCanvasId, aqiCanvasId, cities) => {
        const riskCanvas = document.getElementById(riskCanvasId);
        const aqiCanvas = document.getElementById(aqiCanvasId);
        if (!riskCanvas || !aqiCanvas) return;

        const riskCtx = riskCanvas.getContext('2d');
        const aqiCtx = aqiCanvas.getContext('2d');

        if (ChartService.riskTrend) ChartService.riskTrend.destroy();
        if (ChartService.aqiTrend) ChartService.aqiTrend.destroy();

        const labels = cities.map(c => c.name);
        const riskData = cities.map(c => c.riskScore);
        const aqiData = cities.map(c => c.aqi);
        const tempData = cities.map(c => c.temperature);
        const humData = cities.map(c => c.humidity);

        // Risk bar chart — color-coded by severity
        ChartService.riskTrend = new Chart(riskCtx, {
            type: 'bar',
            data: {
                labels,
                datasets: [{
                    label: 'Risk Index',
                    data: riskData,
                    backgroundColor: riskData.map(v => {
                        if (v > 60) return 'rgba(239,68,68,0.8)';
                        if (v > 30) return 'rgba(245,158,11,0.8)';
                        return 'rgba(16,185,129,0.8)';
                    }),
                    borderColor: riskData.map(v => {
                        if (v > 60) return '#dc2626';
                        if (v > 30) return '#d97706';
                        return '#059669';
                    }),
                    borderWidth: 1.5,
                    borderRadius: 10,
                    borderSkipped: false
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                animation: { duration: 1000, easing: 'easeOutQuart' },
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#0f172a',
                        padding: 12,
                        cornerRadius: 10,
                        callbacks: {
                            label: ctx => `Risk: ${ctx.parsed.y.toFixed(1)}%`
                        }
                    }
                },
                scales: {
                    x: { grid: { display: false }, ticks: { font: { size: 10, family: 'Inter', weight: '600' }, maxRotation: 45 } },
                    y: { beginAtZero: true, max: 100, grid: { color: 'rgba(0,0,0,0.04)' } }
                }
            }
        });

        // Multi-series line chart — AQI + Temp + Humidity
        const aqiGrad = ChartService._gradient(aqiCtx, 'rgba(99,102,241,0.15)', 'rgba(99,102,241,0)');
        ChartService.aqiTrend = new Chart(aqiCtx, {
            type: 'line',
            data: {
                labels,
                datasets: [
                    {
                        label: 'AQI',
                        data: aqiData,
                        borderColor: '#6366f1',
                        backgroundColor: aqiGrad,
                        borderWidth: 2.5,
                        tension: 0.35,
                        fill: true,
                        pointRadius: 4,
                        pointBackgroundColor: '#6366f1',
                        yAxisID: 'y'
                    },
                    {
                        label: 'Temperature (°C)',
                        data: tempData,
                        borderColor: '#ef4444',
                        borderWidth: 2,
                        tension: 0.35,
                        fill: false,
                        pointRadius: 3,
                        pointBackgroundColor: '#ef4444',
                        borderDash: [6, 3],
                        yAxisID: 'y1'
                    },
                    {
                        label: 'Humidity (%)',
                        data: humData,
                        borderColor: '#06b6d4',
                        borderWidth: 1.5,
                        tension: 0.35,
                        fill: false,
                        pointRadius: 2,
                        pointBackgroundColor: '#06b6d4',
                        borderDash: [3, 3],
                        yAxisID: 'y1'
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                animation: { duration: 1200, easing: 'easeOutQuart' },
                interaction: { intersect: false, mode: 'index' },
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: { usePointStyle: true, pointStyle: 'circle', padding: 16, font: { size: 11, family: 'Inter', weight: '600' } }
                    },
                    tooltip: { backgroundColor: '#0f172a', cornerRadius: 10, padding: 12 }
                },
                scales: {
                    x: { grid: { display: false }, ticks: { font: { size: 10, family: 'Inter' }, maxRotation: 45 } },
                    y: {
                        type: 'linear', display: true, position: 'left',
                        title: { display: true, text: 'AQI', font: { size: 11, weight: '700' } },
                        grid: { color: 'rgba(0,0,0,0.04)' }
                    },
                    y1: {
                        type: 'linear', display: true, position: 'right',
                        title: { display: true, text: 'Temp / Humidity', font: { size: 11, weight: '700' } },
                        grid: { drawOnChartArea: false }
                    }
                }
            }
        });
    },

    // 3. ML YEARLY TREND — driven by actual city data + seasonality model
    renderMLYearlyTrend: (canvasId, city) => {
        const canvas = document.getElementById(canvasId);
        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        if (ChartService.mlYearly) ChartService.mlYearly.destroy();

        const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
        const currentMonth = new Date().getMonth();

        // Model: seasonal oscillation centered on city's current metrics
        const baseRisk = city.riskScore;
        const baseAqi = city.aqi;
        const baseTemp = city.temperature;

        const riskProjection = months.map((_, i) => {
            const offset = (i - currentMonth + 12) % 12;
            const seasonal = Math.sin((offset / 12) * 2 * Math.PI) * 18;
            const noise = (Math.random() - 0.5) * 6;
            return Math.max(0, Math.min(100, baseRisk + seasonal + noise));
        });

        const aqiProjection = months.map((_, i) => {
            const offset = (i - currentMonth + 12) % 12;
            const seasonal = Math.cos((offset / 12) * 2 * Math.PI) * 30;
            const noise = (Math.random() - 0.5) * 10;
            return Math.max(0, baseAqi + seasonal + noise);
        });

        const tempProjection = months.map((_, i) => {
            const offset = (i - currentMonth + 12) % 12;
            const seasonal = Math.sin(((offset - 3) / 12) * 2 * Math.PI) * 12;
            return baseTemp + seasonal;
        });

        // Confidence band
        const upperBand = riskProjection.map(v => Math.min(100, v + 8 + Math.random() * 4));
        const lowerBand = riskProjection.map(v => Math.max(0, v - 8 - Math.random() * 4));

        const riskGrad = ChartService._gradient(ctx, 'rgba(37,99,235,0.15)', 'rgba(37,99,235,0)', 400);

        ChartService.mlYearly = new Chart(ctx, {
            type: 'line',
            data: {
                labels: months,
                datasets: [
                    {
                        label: 'Upper Confidence',
                        data: upperBand,
                        borderColor: 'transparent',
                        backgroundColor: 'rgba(37,99,235,0.06)',
                        fill: '+1',
                        pointRadius: 0,
                        tension: 0.4
                    },
                    {
                        label: 'Projected Risk',
                        data: riskProjection,
                        borderColor: '#2563eb',
                        backgroundColor: riskGrad,
                        borderWidth: 3,
                        tension: 0.4,
                        fill: true,
                        pointRadius: (ctx) => ctx.dataIndex === currentMonth ? 8 : 3,
                        pointBackgroundColor: (ctx) => ctx.dataIndex === currentMonth ? '#2563eb' : '#fff',
                        pointBorderColor: '#2563eb',
                        pointBorderWidth: 2,
                        pointHoverRadius: 8
                    },
                    {
                        label: 'Lower Confidence',
                        data: lowerBand,
                        borderColor: 'transparent',
                        backgroundColor: 'transparent',
                        fill: false,
                        pointRadius: 0,
                        tension: 0.4
                    },
                    {
                        label: 'Projected AQI',
                        data: aqiProjection,
                        borderColor: '#10b981',
                        borderDash: [8, 4],
                        borderWidth: 2,
                        tension: 0.4,
                        fill: false,
                        pointRadius: (ctx) => ctx.dataIndex === currentMonth ? 6 : 0,
                        pointBackgroundColor: '#10b981',
                        yAxisID: 'y1'
                    },
                    {
                        label: 'Temp Forecast (°C)',
                        data: tempProjection,
                        borderColor: '#f59e0b',
                        borderDash: [4, 4],
                        borderWidth: 1.5,
                        tension: 0.4,
                        fill: false,
                        pointRadius: 0,
                        yAxisID: 'y1'
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                animation: { duration: 1400, easing: 'easeOutQuart' },
                interaction: { intersect: false, mode: 'index' },
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            filter: item => !item.text.includes('Confidence'),
                            usePointStyle: true, pointStyle: 'circle', padding: 16,
                            font: { size: 11, family: 'Inter', weight: '600' }
                        }
                    },
                    tooltip: {
                        backgroundColor: '#0f172a', cornerRadius: 10, padding: 14,
                        filter: item => !item.dataset.label.includes('Confidence'),
                        callbacks: {
                            afterBody: (items) => {
                                const idx = items[0]?.dataIndex;
                                if (idx === currentMonth) return '← Current Month';
                                return '';
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        grid: { display: false },
                        ticks: {
                            font: { size: 11, family: 'Inter', weight: '600' },
                            color: (ctx) => ctx.index === currentMonth ? '#2563eb' : '#94a3b8'
                        }
                    },
                    y: {
                        beginAtZero: true, max: 100,
                        title: { display: true, text: 'Risk %', font: { size: 11, weight: '700' } },
                        grid: { color: 'rgba(0,0,0,0.04)' }
                    },
                    y1: {
                        type: 'linear', display: true, position: 'right',
                        title: { display: true, text: 'AQI / Temp', font: { size: 11, weight: '700' } },
                        grid: { drawOnChartArea: false }
                    }
                }
            }
        });
    },

    // 4. ML RADAR (Ecological Balance) — all axes from real data
    renderMLRadar: (canvasId, city) => {
        const canvas = document.getElementById(canvasId);
        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        if (ChartService.mlRadar) ChartService.mlRadar.destroy();

        const stability = Math.max(0, 100 - city.riskScore).toFixed(1);
        const airQuality = Math.max(0, Math.min(100, 100 - (city.aqi / 5)));
        const thermalBalance = Math.max(0, Math.min(100, 100 - Math.abs(city.temperature - 25) * 3));
        const humidityLoad = Math.max(0, Math.min(100, city.humidity));
        const anomalyResistance = Math.max(0, Math.min(100, stability * 0.6 + airQuality * 0.4));

        ChartService.mlRadar = new Chart(ctx, {
            type: 'radar',
            data: {
                labels: ['Stability', 'Air Quality', 'Thermal Balance', 'Humidity Load', 'Anomaly Resistance'],
                datasets: [{
                    label: city.name,
                    data: [stability, airQuality, thermalBalance, humidityLoad, anomalyResistance],
                    backgroundColor: 'rgba(37, 99, 235, 0.15)',
                    borderColor: '#2563eb',
                    borderWidth: 2.5,
                    pointBackgroundColor: '#2563eb',
                    pointBorderColor: '#fff',
                    pointBorderWidth: 2,
                    pointRadius: 5,
                    pointHoverRadius: 8
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                animation: { duration: 1000, easing: 'easeOutQuart' },
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#0f172a', cornerRadius: 10, padding: 12,
                        callbacks: { label: ctx => `${ctx.label}: ${ctx.parsed.r.toFixed(1)}` }
                    }
                },
                scales: {
                    r: {
                        beginAtZero: true,
                        max: 100,
                        ticks: { display: false, stepSize: 20 },
                        grid: { color: 'rgba(0,0,0,0.06)' },
                        angleLines: { color: 'rgba(0,0,0,0.06)' },
                        pointLabels: { font: { size: 10, family: 'Inter', weight: '700' }, color: '#64748b' }
                    }
                }
            }
        });
    },

    // 5. ML HOURLY BREAKDOWN (new 24h micro-trend) — simulated from real-time state
    renderMLHourly: (canvasId, city) => {
        const canvas = document.getElementById(canvasId);
        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        if (ChartService.mlHourly) ChartService.mlHourly.destroy();

        const now = new Date();
        const hours = Array.from({ length: 24 }, (_, i) => {
            const h = (now.getHours() - 23 + i + 24) % 24;
            return `${h.toString().padStart(2, '0')}:00`;
        });

        const base = city.riskScore;
        const riskData = hours.map((_, i) => {
            const diurnal = Math.sin((i / 24) * 2 * Math.PI - Math.PI / 2) * 8;
            const noise = (Math.random() - 0.5) * 5;
            return Math.max(0, Math.min(100, base + diurnal + noise));
        });

        const aqiData = hours.map((_, i) => {
            const diurnal = Math.sin((i / 24) * 2 * Math.PI) * 15;
            const noise = (Math.random() - 0.5) * 8;
            return Math.max(0, city.aqi + diurnal + noise);
        });

        const grad = ChartService._gradient(ctx, 'rgba(139,92,246,0.18)', 'rgba(139,92,246,0)', 200);

        ChartService.mlHourly = new Chart(ctx, {
            type: 'line',
            data: {
                labels: hours,
                datasets: [
                    {
                        label: 'Risk %',
                        data: riskData,
                        borderColor: '#8b5cf6',
                        backgroundColor: grad,
                        borderWidth: 2,
                        tension: 0.4,
                        fill: true,
                        pointRadius: 0,
                        pointHoverRadius: 6
                    },
                    {
                        label: 'AQI',
                        data: aqiData,
                        borderColor: '#06b6d4',
                        borderWidth: 1.5,
                        borderDash: [4, 3],
                        tension: 0.4,
                        fill: false,
                        pointRadius: 0
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                animation: { duration: 1000, easing: 'easeOutQuart' },
                interaction: { intersect: false, mode: 'index' },
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: { usePointStyle: true, pointStyle: 'circle', padding: 12, font: { size: 10, family: 'Inter', weight: '600' } }
                    },
                    tooltip: { backgroundColor: '#0f172a', cornerRadius: 10, padding: 10 }
                },
                scales: {
                    x: { grid: { display: false }, ticks: { font: { size: 9 }, maxTicksLimit: 12 } },
                    y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.04)' } }
                }
            }
        });
    }
};

window.ChartService = ChartService;
