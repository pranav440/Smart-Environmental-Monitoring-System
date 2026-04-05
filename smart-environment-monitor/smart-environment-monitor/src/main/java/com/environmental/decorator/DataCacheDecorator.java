package com.environmental.decorator;

import com.environmental.models.City;
import com.environmental.models.EnvironmentalData;
import com.environmental.service.EnvironmentalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Decorator Pattern: Wraps an EnvironmentalService with a time-based cache layer.
 * Cache entries expire after CACHE_DURATION milliseconds.
 */
public class DataCacheDecorator implements EnvironmentalService {
    private static final Logger logger = LoggerFactory.getLogger(DataCacheDecorator.class);
    private final EnvironmentalService wrappedService;
    private final ConcurrentHashMap<String, EnvironmentalData> cache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> cacheTime = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 60000; // 1 minute

    public DataCacheDecorator(EnvironmentalService wrappedService) {
        this.wrappedService = wrappedService;
    }

    @Override
    public EnvironmentalData getCurrentData(City city) {
        String cacheKey = city.getName();

        if (cache.containsKey(cacheKey)) {
            Long lastUpdate = cacheTime.get(cacheKey);
            if (lastUpdate != null && System.currentTimeMillis() - lastUpdate < CACHE_DURATION) {
                logger.debug("Cache hit for: {}", city.getName());
                return cache.get(cacheKey);
            }
        }

        logger.debug("Cache miss for: {}", city.getName());
        EnvironmentalData data = wrappedService.getCurrentData(city);
        if (data != null) {
            cache.put(cacheKey, data);
            cacheTime.put(cacheKey, System.currentTimeMillis());
        }
        return data;
    }

    @Override
    public double calculateRisk(City city) {
        return wrappedService.calculateRisk(city);
    }
}