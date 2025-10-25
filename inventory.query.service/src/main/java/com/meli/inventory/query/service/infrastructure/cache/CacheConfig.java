package com.meli.inventory.query.service.infrastructure.cache;


import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuración de Cache con Caffeine
 * Optimiza queries frecuentes reduciendo acceso a CSV.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "inventoryItems",
                "availability",
                "storeInventory",
                "lowStockItems",
                "categoryItems",
                "inventoryStats"
        );

        cacheManager.setCaffeine(caffeineCacheBuilder());

        return cacheManager;
    }

    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats();
    }
}