package com.meli.inventory.query.service.application;

import com.meli.inventory.query.service.domain.model.InventoryProjection;
import com.meli.inventory.query.service.domain.port.in.InventoryQueryService;
import com.meli.inventory.query.service.domain.port.out.InventoryQueryRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación del servicio de consultas
 * Características:
 * - Cache con Caffeine para queries frecuentes
 * - Circuit Breaker para resiliencia
 * - Rate Limiter para protección
 * - Métricas y logging
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryQueryServiceImpl implements InventoryQueryService {

    private final InventoryQueryRepository repository;

    @Override
    @Cacheable(value = "inventoryItems", key = "#id")
    @CircuitBreaker(name = "inventoryQuery")
    @RateLimiter(name = "inventoryQueryApi")
    public InventoryProjection getById(String id) {
        log.debug("Fetching inventory item by ID: {}", id);

        return repository.findById(id)
                .orElseThrow(() -> new InventoryItemNotFoundException(
                        "Inventory item not found: " + id));
    }

    @Override
    @Cacheable(value = "availability", key = "#productId + '_' + #storeId")
    @CircuitBreaker(name = "inventoryQuery")
    @RateLimiter(name = "inventoryQueryApi")
    public AvailabilityInfo checkAvailability(String productId, String storeId) {
        log.debug("Checking availability - Product: {}, Store: {}", productId, storeId);

        InventoryProjection item = repository.findByProductIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new InventoryItemNotFoundException(
                        String.format("Product %s not found in store %s", productId, storeId)));

        return new AvailabilityInfo(
                item.getProductId(),
                item.getStoreId(),
                item.isAvailableForSale(),
                item.getQuantity(),
                item.getReservedQuantity(),
                item.getAvailableStock(),
                item.getProductName()
        );
    }

    @Override
    @Cacheable(value = "storeInventory", key = "#storeId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    @CircuitBreaker(name = "inventoryQuery")
    @RateLimiter(name = "inventoryQueryApi")
    public Page<InventoryProjection> getInventoryByStore(String storeId, Pageable pageable) {
        log.debug("Fetching inventory for store: {} (page: {})", storeId, pageable.getPageNumber());

        return repository.findByStoreId(storeId, pageable);
    }

    @Override
    @Cacheable(value = "lowStockItems", key = "#storeId")
    @CircuitBreaker(name = "inventoryQuery")
    @RateLimiter(name = "inventoryQueryApi")
    public List<InventoryProjection> getLowStockItems(String storeId) {
        log.debug("Fetching low stock items for store: {}", storeId);

        List<InventoryProjection> lowStockItems = repository.findLowStockItems(storeId);

        log.info("Found {} low stock items in store: {}", lowStockItems.size(), storeId);

        return lowStockItems;
    }

    @Override
    @CircuitBreaker(name = "inventoryQuery")
    @RateLimiter(name = "inventoryQueryApi")
    public List<InventoryProjection> getOutOfStockItems(String storeId) {
        log.debug("Fetching out of stock items for store: {}", storeId);

        return repository.findOutOfStockItems(storeId);
    }

    @Override
    @CircuitBreaker(name = "inventoryQuery")
    @RateLimiter(name = "inventoryQueryApi")
    public List<InventoryProjection> searchItems(String storeId, String searchTerm) {
        log.debug("Searching items in store: {} with term: {}", storeId, searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be empty");
        }

        return repository.searchByProductName(storeId, searchTerm.toLowerCase());
    }

    @Override
    @Cacheable(value = "categoryItems", key = "#storeId + '_' + #category")
    @CircuitBreaker(name = "inventoryQuery")
    @RateLimiter(name = "inventoryQueryApi")
    public List<InventoryProjection> getItemsByCategory(String storeId, String category) {
        log.debug("Fetching items by category: {} in store: {}", category, storeId);

        return repository.findByStoreIdAndCategory(storeId, category);
    }

    @Override
    @Cacheable(value = "inventoryStats", key = "#storeId")
    @CircuitBreaker(name = "inventoryQuery")
    @RateLimiter(name = "inventoryQueryApi")
    public InventoryStats getInventoryStats(String storeId) {
        log.debug("Calculating inventory stats for store: {}", storeId);

        List<InventoryProjection> allItems = repository.findByStoreId(storeId);

        long totalItems = allItems.size();
        long lowStockItems = allItems.stream()
                .filter(InventoryProjection::getBelowThreshold)
                .count();
        long outOfStockItems = allItems.stream()
                .filter(item -> item.getAvailableStock() <= 0)
                .count();
        long availableItems = allItems.stream()
                .filter(InventoryProjection::isAvailableForSale)
                .count();

        int totalQuantity = allItems.stream()
                .mapToInt(InventoryProjection::getQuantity)
                .sum();
        int totalReserved = allItems.stream()
                .mapToInt(InventoryProjection::getReservedQuantity)
                .sum();
        int totalAvailable = totalQuantity - totalReserved;

        InventoryStats stats = new InventoryStats(
                storeId,
                totalItems,
                lowStockItems,
                outOfStockItems,
                availableItems,
                totalQuantity,
                totalReserved,
                totalAvailable
        );

        log.info("Inventory stats for store {}: {} total items, {} low stock, {} out of stock",
                storeId, totalItems, lowStockItems, outOfStockItems);

        return stats;
    }

    /**
     * Excepción personalizada para item no encontrado
     */
    public static class InventoryItemNotFoundException extends RuntimeException {
        public InventoryItemNotFoundException(String message) {
            super(message);
        }
    }
}
