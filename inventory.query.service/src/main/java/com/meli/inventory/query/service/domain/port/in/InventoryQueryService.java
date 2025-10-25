package com.meli.inventory.query.service.domain.port.in;

import com.meli.inventory.query.service.domain.model.InventoryProjection;
import com.meli.inventory.query.service.domain.exception.InventoryItemNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Puerto de entrada - Servicio de Consultas (Input Port)
 * Define todas las operaciones de lectura disponibles.
 * Implementado por InventoryQueryServiceImpl en la capa de aplicación.
 */
public interface InventoryQueryService {

    /**
     * Obtiene un item por ID
     *
     * @param id Identificador del item
     * @return Proyección del item de inventario
     * @throws InventoryItemNotFoundException si no existe
     */
    InventoryProjection getById(String id);

    /**
     * Verifica disponibilidad de un producto en una tienda
     *
     * @param productId ID del producto
     * @param storeId ID de la tienda
     * @return Información de disponibilidad
     * @throws InventoryItemNotFoundException si no existe
     */
    AvailabilityInfo checkAvailability(String productId, String storeId);

    /**
     * Obtiene todos los items de una tienda con paginación
     *
     * @param storeId ID de la tienda
     * @param pageable Configuración de paginación
     * @return Página de items de inventario
     */
    Page<InventoryProjection> getInventoryByStore(String storeId, Pageable pageable);

    /**
     * Obtiene items con stock bajo (below threshold)
     *
     * @param storeId ID de la tienda
     * @return Lista de items con stock bajo
     */
    List<InventoryProjection> getLowStockItems(String storeId);

    /**
     * Obtiene items sin stock (quantity = 0)
     *
     * @param storeId ID de la tienda
     * @return Lista de items sin stock
     */
    List<InventoryProjection> getOutOfStockItems(String storeId);

    /**
     * Busca items por nombre de producto
     *
     * @param storeId ID de la tienda
     * @param searchTerm Término de búsqueda
     * @return Lista de items que coinciden con la búsqueda
     */
    List<InventoryProjection> searchItems(String storeId, String searchTerm);

    /**
     * Obtiene items por categoría
     *
     * @param storeId ID de la tienda
     * @param category Categoría del producto
     * @return Lista de items de la categoría
     */
    List<InventoryProjection> getItemsByCategory(String storeId, String category);

    /**
     * Obtiene estadísticas de inventario por tienda
     *
     * @param storeId ID de la tienda
     * @return Estadísticas agregadas del inventario
     */
    InventoryStats getInventoryStats(String storeId);

    /**
     * DTO para información de disponibilidad
     *
     * Record inline - no necesita archivo separado
     */
    record AvailabilityInfo(
            String productId,
            String storeId,
            boolean available,
            Integer quantity,
            Integer reservedQuantity,
            Integer availableStock,
            String productName
    ) {}

    /**
     * DTO para estadísticas de inventario
     *
     * Record inline - no necesita archivo separado
     */
    record InventoryStats(
            String storeId,
            long totalItems,
            long lowStockItems,
            long outOfStockItems,
            long availableItems,
            Integer totalQuantity,
            Integer totalReserved,
            Integer totalAvailable
    ) {}
}
