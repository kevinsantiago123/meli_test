package com.meli.inventory.query.service.infrastructure.rest;

import com.meli.inventory.query.service.domain.model.InventoryProjection;
import com.meli.inventory.query.service.domain.port.in.InventoryQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para operaciones de consulta (Query)
 * Endpoints de solo lectura optimizados para consultas frecuentes.
 */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory Query", description = "Read operations for inventory查询")
public class InventoryQueryController {

    private final InventoryQueryService queryService;

    @GetMapping("/{id}")
    @Operation(summary = "Get inventory item by ID",
            description = "Retrieves a single inventory item by its unique identifier")
    @ApiResponse(responseCode = "200", description = "Item found")
    @ApiResponse(responseCode = "404", description = "Item not found")
    public ResponseEntity<InventoryProjection> getById(
            @PathVariable @Parameter(description = "Item ID") String id) {

        log.info("REST: Getting inventory item by ID: {}", id);

        InventoryProjection item = queryService.getById(id);
        return ResponseEntity.ok(item);
    }

    @GetMapping("/availability")
    @Operation(summary = "Check product availability",
            description = "Checks if a product is available in a specific store")
    @ApiResponse(responseCode = "200", description = "Availability checked")
    @ApiResponse(responseCode = "404", description = "Product not found in store")
    public ResponseEntity<InventoryQueryService.AvailabilityInfo> checkAvailability(
            @RequestParam @Parameter(description = "Product ID") String productId,
            @RequestParam @Parameter(description = "Store ID") String storeId) {

        log.info("REST: Checking availability - Product: {}, Store: {}", productId, storeId);

        InventoryQueryService.AvailabilityInfo availability =
                queryService.checkAvailability(productId, storeId);

        return ResponseEntity.ok(availability);
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Get inventory by store",
            description = "Retrieves all inventory items for a specific store with pagination")
    @ApiResponse(responseCode = "200", description = "Inventory retrieved")
    public ResponseEntity<Page<InventoryProjection>> getInventoryByStore(
            @PathVariable @Parameter(description = "Store ID") String storeId,
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "Page size") int size) {

        log.info("REST: Getting inventory for store: {} (page: {}, size: {})", storeId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<InventoryProjection> items = queryService.getInventoryByStore(storeId, pageable);

        return ResponseEntity.ok(items);
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock items",
            description = "Retrieves items that are below their minimum threshold")
    @ApiResponse(responseCode = "200", description = "Low stock items retrieved")
    public ResponseEntity<List<InventoryProjection>> getLowStockItems(
            @RequestParam @Parameter(description = "Store ID") String storeId) {

        log.info("REST: Getting low stock items for store: {}", storeId);

        List<InventoryProjection> items = queryService.getLowStockItems(storeId);

        return ResponseEntity.ok(items);
    }

    @GetMapping("/out-of-stock")
    @Operation(summary = "Get out of stock items",
            description = "Retrieves items with zero available stock")
    @ApiResponse(responseCode = "200", description = "Out of stock items retrieved")
    public ResponseEntity<List<InventoryProjection>> getOutOfStockItems(
            @RequestParam @Parameter(description = "Store ID") String storeId) {

        log.info("REST: Getting out of stock items for store: {}", storeId);

        List<InventoryProjection> items = queryService.getOutOfStockItems(storeId);

        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    @Operation(summary = "Search inventory items",
            description = "Searches items by product name")
    @ApiResponse(responseCode = "200", description = "Search results")
    @ApiResponse(responseCode = "400", description = "Invalid search term")
    public ResponseEntity<List<InventoryProjection>> searchItems(
            @RequestParam @Parameter(description = "Store ID") String storeId,
            @RequestParam @Parameter(description = "Search term") String q) {

        log.info("REST: Searching items in store: {} with term: {}", storeId, q);

        List<InventoryProjection> items = queryService.searchItems(storeId, q);

        return ResponseEntity.ok(items);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get items by category",
            description = "Retrieves items filtered by category")
    @ApiResponse(responseCode = "200", description = "Category items retrieved")
    public ResponseEntity<List<InventoryProjection>> getItemsByCategory(
            @PathVariable @Parameter(description = "Category") String category,
            @RequestParam @Parameter(description = "Store ID") String storeId) {

        log.info("REST: Getting items by category: {} in store: {}", category, storeId);

        List<InventoryProjection> items = queryService.getItemsByCategory(storeId, category);

        return ResponseEntity.ok(items);
    }

    @GetMapping("/stats/{storeId}")
    @Operation(summary = "Get inventory statistics",
            description = "Retrieves aggregated statistics for store inventory")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved")
    public ResponseEntity<InventoryQueryService.InventoryStats> getInventoryStats(
            @PathVariable @Parameter(description = "Store ID") String storeId) {

        log.info("REST: Getting inventory stats for store: {}", storeId);

        InventoryQueryService.InventoryStats stats = queryService.getInventoryStats(storeId);

        return ResponseEntity.ok(stats);
    }
}
