package com.environmental.decorator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class CacheManager {
    private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);
    private static CacheManager instance;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ConcurrentHashMap<String, Object> cacheRegistry = new ConcurrentHashMap<>();

    private CacheManager() {
        scheduler.scheduleAtFixedRate(this::clearAllCaches, 1, 1, TimeUnit.HOURS);
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public static synchronized CacheManager getInstance() {
        if (instance == null) {
            instance = new CacheManager();
        }
        return instance;
    }

    public void registerCache(String name, Object cache) {
        cacheRegistry.put(name, cache);
    }

    public void clearAllCaches() {
        logger.info("Clearing all registered caches ({} entries)", cacheRegistry.size());
        cacheRegistry.clear();
    }

    public void shutdown() {
        logger.info("Shutting down CacheManager scheduler");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}