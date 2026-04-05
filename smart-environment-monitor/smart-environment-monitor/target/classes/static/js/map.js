/**
 * map.js
 * Leaflet.js map integration with multiple view modes:
 *   - Dashboard: standard street map with risk markers
 *   - Interactive Map: satellite + heatmap overlay + pollution rings + wind layer controls
 */

const MapService = {
    instances: {},
    layers: {},
    markerGroups: {},
    heatOverlays: {},
    pollutionRings: {},
    _currentViewMode: 'risk', // 'risk' | 'pollution' | 'temperature'

    legends: {
        risk: [
            { color: '#10b981', label: 'Low / Safe (< 30%)' },
            { color: '#f59e0b', label: 'Moderate / Caution (30-60%)' },
            { color: '#ef4444', label: 'High / Critical (> 60%)' }
        ],
        pollution: [
            { color: '#00e400', label: 'Good (0-50)' },
            { color: '#ffff00', label: 'Moderate (51-100)' },
            { color: '#ff7e00', label: 'Sensitivity Advisory (101-150)' },
            { color: '#ff0000', label: 'Unhealthy (151-200)' },
            { color: '#8f3f97', label: 'Very Unhealthy (201-300)' },
            { color: '#7e0023', label: 'Hazardous (> 300)' }
        ],
        temperature: [
            { color: '#06B6D4', label: 'Cold (< 10°C)' },
            { color: '#10B981', label: 'Mild (10-25°C)' },
            { color: '#F59E0B', label: 'Warm (25-35°C)' },
            { color: '#EF4444', label: 'Hot (35-42°C)' },
            { color: '#7F1D1D', label: 'Extreme (> 42°C)' }
        ]
    },

    // 1. INITIALIZE MAP
    init: (containerId) => {
        if (MapService.instances[containerId]) return MapService.instances[containerId];

        const map = L.map(containerId, {
            zoomControl: false,
            attributionControl: false
        }).setView([20.5937, 78.9629], 5);

        const street = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19
        });

        const satellite = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
            maxZoom: 19,
            attribution: 'Esri World Imagery'
        });

        const dark = L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
            maxZoom: 19
        });

        const topo = L.tileLayer('https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png', {
            maxZoom: 17
        });

        if (containerId === 'full-map') {
            satellite.addTo(map);
        } else {
            street.addTo(map);
        }

        L.control.zoom({ position: 'bottomright' }).addTo(map);

        MapService.instances[containerId] = map;
        MapService.layers[containerId] = { street, satellite, dark, topo };
        MapService.markerGroups[containerId] = L.featureGroup().addTo(map);
        MapService.heatOverlays[containerId] = L.featureGroup().addTo(map);
        MapService.pollutionRings[containerId] = L.featureGroup().addTo(map);

        return map;
    },

    // 2. RENDER CITY MARKERS (standard risk markers for dashboard)
    renderCities: (containerId, cities, onCitySelect) => {
        const map = MapService.instances[containerId];
        if (!map) return;

        const group = MapService.markerGroups[containerId];
        group.clearLayers();

        cities.forEach(city => {
            const color = city.riskScore > 60 ? '#ef4444' : (city.riskScore > 30 ? '#f59e0b' : '#10b981');

            const marker = L.circleMarker([city.latitude, city.longitude], {
                radius: 10,
                fillColor: color,
                color: '#fff',
                weight: 2,
                opacity: 1,
                fillOpacity: 0.8
            });

            marker.bindPopup(`
                <div class="p-2 min-w-[120px]">
                    <p class="text-[10px] font-black uppercase text-slate-400 mb-1">${city.cityType}</p>
                    <h4 class="text-sm font-bold mb-2">${city.name}</h4>
                    <div class="flex flex-col gap-1">
                        <div class="flex justify-between text-xs"><span>Risk:</span> <span class="font-bold">${city.riskScore.toFixed(1)}%</span></div>
                        <div class="flex justify-between text-xs"><span>AQI:</span> <span class="font-bold">${Math.round(city.aqi)}</span></div>
                        <div class="flex justify-between text-xs"><span>Temp:</span> <span class="font-bold">${city.temperature.toFixed(1)}°C</span></div>
                        <div class="flex justify-between text-xs"><span>Humidity:</span> <span class="font-bold">${Math.round(city.humidity)}%</span></div>
                    </div>
                </div>
            `);

            marker.on('click', () => {
                marker.openPopup();
                if (onCitySelect) onCitySelect(city.name);
            });

            group.addLayer(marker);
        });

        if (group.getLayers().length > 0) {
            map.fitBounds(group.getBounds().pad(0.1));
        }
    },

    // 3. RENDER INTERACTIVE MAP (unique features for the Map tab)
    renderInteractiveMap: (containerId, cities, onCitySelect, viewMode) => {
        const map = MapService.instances[containerId];
        if (!map) return;

        const markerGroup = MapService.markerGroups[containerId];
        const heatGroup = MapService.heatOverlays[containerId];
        const pollutionGroup = MapService.pollutionRings[containerId];

        markerGroup.clearLayers();
        heatGroup.clearLayers();
        pollutionGroup.clearLayers();

        MapService._currentViewMode = viewMode || 'risk';
        // Force immediate and delayed sync to ensure DOM readiness
        MapService.updateLegend();
        setTimeout(() => MapService.updateLegend(), 100);

        cities.forEach(city => {
            const riskColor = city.riskScore > 60 ? '#ef4444' : (city.riskScore > 30 ? '#f59e0b' : '#10b981');

            let displayColor, displayRadius, displayMetric, displayValue;

            switch (MapService._currentViewMode) {
                case 'pollution':
                    // AQI-based display: Standard US-EPA AQI Color Scale
                    let pollColor;
                    if (city.aqi <= 50) pollColor = '#00e400';        // Green (Good)
                    else if (city.aqi <= 100) pollColor = '#ffff00';  // Yellow (Moderate)
                    else if (city.aqi <= 150) pollColor = '#ff7e00';  // Orange (Unhealthy for Sensitive)
                    else if (city.aqi <= 200) pollColor = '#ff0000';  // Red (Unhealthy)
                    else if (city.aqi <= 300) pollColor = '#8f3f97';  // Purple (Very Unhealthy)
                    else pollColor = '#7e0023';                      // Maroon (Hazardous)
                    
                    displayColor = pollColor;
                    displayRadius = 12 + Math.min(city.aqi / 300, 1) * 18;
                    displayMetric = 'AQI';
                    displayValue = Math.round(city.aqi);

                    // Pollution radius ring
                    const pollutionRadius = 5000 + city.aqi * 80;
                    const ring = L.circle([city.latitude, city.longitude], {
                        radius: pollutionRadius,
                        fillColor: displayColor,
                        fillOpacity: 0.1,
                        color: displayColor,
                        weight: 1.5,
                        opacity: 0.4,
                        dashArray: '6 4'
                    });
                    pollutionGroup.addLayer(ring);
                    break;

                case 'temperature':
                    // Temperature-based display: Vibrant Thermal Gradient
                    const temp = city.temperature;
                    let thermalColor;
                    if (temp <= 10) thermalColor = '#06B6D4';         // Cyan (Cold)
                    else if (temp <= 25) thermalColor = '#10B981';     // Emerald (Mild)
                    else if (temp <= 35) thermalColor = '#F59E0B';     // Amber (Warm)
                    else if (temp <= 42) thermalColor = '#EF4444';     // Red (Hot)
                    else thermalColor = '#7F1D1D';                    // Deep Red (Extreme)
                    
                    displayColor = thermalColor;
                    const tNorm = Math.min(Math.max((temp - 0) / 50, 0), 1);
                    displayRadius = 10 + tNorm * 12;
                    displayMetric = 'Temp';
                    displayValue = temp.toFixed(1) + '°C';
                    break;

                default: // 'risk'
                    displayColor = riskColor;
                    displayRadius = 10 + (city.riskScore / 100) * 12;
                    displayMetric = 'Risk';
                    displayValue = city.riskScore.toFixed(1) + '%';
            }

            // Main marker
            const marker = L.circleMarker([city.latitude, city.longitude], {
                radius: displayRadius,
                fillColor: displayColor,
                color: '#fff',
                weight: 2.5,
                opacity: 1,
                fillOpacity: 0.85
            });

            // Pulse ring for high-risk cities
            if (city.riskScore > 60 && MapService._currentViewMode === 'risk') {
                const pulseRing = L.circleMarker([city.latitude, city.longitude], {
                    radius: displayRadius + 6,
                    fillColor: 'transparent',
                    color: '#ef4444',
                    weight: 2,
                    opacity: 0.4,
                    fillOpacity: 0,
                    className: 'pulse-ring'
                });
                heatGroup.addLayer(pulseRing);
            }

            // Rich interactive popup
            marker.bindPopup(`
                <div class="p-3 min-w-[200px]">
                    <div class="flex items-center gap-2 mb-2">
                        <div class="w-3 h-3 rounded-full" style="background:${displayColor}"></div>
                        <p class="text-[10px] font-black uppercase text-slate-400">${city.cityType}</p>
                    </div>
                    <h4 class="text-base font-bold mb-3">${city.name}</h4>
                    <div class="grid grid-cols-2 gap-2 text-xs">
                        <div class="bg-slate-50 p-2 rounded-lg text-center">
                            <div class="text-[9px] font-bold text-slate-400 uppercase">Risk</div>
                            <div class="font-black text-sm" style="color:${riskColor}">${city.riskScore.toFixed(1)}%</div>
                        </div>
                        <div class="bg-slate-50 p-2 rounded-lg text-center">
                            <div class="text-[9px] font-bold text-slate-400 uppercase">AQI</div>
                            <div class="font-black text-sm">${Math.round(city.aqi)}</div>
                        </div>
                        <div class="bg-slate-50 p-2 rounded-lg text-center">
                            <div class="text-[9px] font-bold text-slate-400 uppercase">Temp</div>
                            <div class="font-black text-sm">${city.temperature.toFixed(1)}°C</div>
                        </div>
                        <div class="bg-slate-50 p-2 rounded-lg text-center">
                            <div class="text-[9px] font-bold text-slate-400 uppercase">Humidity</div>
                            <div class="font-black text-sm">${Math.round(city.humidity)}%</div>
                        </div>
                    </div>
                    <button onclick="window.onCitySelect('${city.name}')" 
                            class="w-full mt-3 py-2 rounded-lg bg-blue-600 text-white text-xs font-bold hover:bg-blue-700 transition-colors">
                        View Full Analysis →
                    </button>
                </div>
            `);

            marker.on('click', () => {
                marker.openPopup();
                if (onCitySelect) onCitySelect(city.name, false);
            });

            markerGroup.addLayer(marker);

            // Floating label on map
            const label = L.divIcon({
                className: 'city-map-label',
                html: `<div class="map-label-content">${city.name}<br><span style="color:${displayColor};font-weight:900">${displayValue}</span></div>`,
                iconSize: [80, 30],
                iconAnchor: [40, -12]
            });
            const labelMarker = L.marker([city.latitude, city.longitude], { icon: label, interactive: false });
            heatGroup.addLayer(labelMarker);
        });

        if (markerGroup.getLayers().length > 0) {
            map.fitBounds(markerGroup.getBounds().pad(0.1));
        }
    },

    // Switch base layer
    setLayer: (containerId, layerType) => {
        const map = MapService.instances[containerId];
        const layers = MapService.layers[containerId];
        if (!map || !layers) return;

        // Remove all base layers first
        Object.values(layers).forEach(layer => {
            if (map.hasLayer(layer)) map.removeLayer(layer);
        });

        // Add requested layer
        if (layers[layerType]) {
            layers[layerType].addTo(map);
        }
    },

    invalidateSize: (containerId) => {
        if (MapService.instances[containerId]) {
            MapService.instances[containerId].invalidateSize();
        }
    },

    updateLegend: () => {
        const legendContainer = document.getElementById('map-legend');
        if (!legendContainer) return;

        const mode = MapService._currentViewMode;
        const items = MapService.legends[mode] || MapService.legends.risk;

        // Map mode to title for better clarity
        const titles = { risk: 'Risk Index', pollution: 'Pollution (AQI)', temperature: 'Thermal Scale' };
        const title = titles[mode] || 'Color Guide';

        let html = `<p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-3">${title}</p>`;
        
        items.forEach(item => {
            html += `
                <div class="flex items-center gap-3 mb-2">
                    <div class="w-2.5 h-2.5 rounded-full ring-4 shadow-sm" style="background: ${item.color}; border-color: ${item.color}20"></div>
                    <span class="text-[10px] font-bold text-slate-600 tracking-tight">${item.label}</span>
                </div>
            `;
        });

        legendContainer.innerHTML = html;
        console.log(`🗺️ Legend updated for mode: ${mode}`);
    }
};

window.MapService = MapService;
