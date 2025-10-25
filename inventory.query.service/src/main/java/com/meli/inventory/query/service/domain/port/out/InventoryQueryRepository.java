package com.meli.inventory.query.service.domain.port.out;

import com.meli.inventory.query.service.domain.model.InventoryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio optimizado para operaciones de lectura.
 * Solo operaciones GET, sin modificaciones de estado.
 */
public interface InventoryQueryRepository {

    /**
     * Busca un item por ID
     */
    Optional<InventoryProjection> findById(String id);

    /**
     * Busca un item por producto y tienda
     */
    Optional<InventoryProjection> findByProductIdAndStoreId(String productId, String storeId);

    /**
     * Busca todos los items de una tienda con paginación
     */
    Page<InventoryProjection> findByStoreId(String storeId, Pageable pageable);

    /**
     * Busca todos los items de una tienda
     */
    List<InventoryProjection> findByStoreId(String storeId);

    /**
     * Busca items por categoría en una tienda
     */
    List<InventoryProjection> findByStoreIdAndCategory(String storeId, String category);

    /**
     * Busca items con stock bajo en una tienda
     */
    List<InventoryProjection> findLowStockItems(String storeId);

    /**
     * Busca items sin stock en una tienda
     */
    List<InventoryProjection> findOutOfStockItems(String storeId);

    /**
     * Busca items disponibles para venta
     */
    List<InventoryProjection> findAvailableItems(String storeId);

    /**
     * Busca items por nombre (búsqueda parcial)
     */
    List<InventoryProjection> searchByProductName(String storeId, String searchTerm);

    /**
     * Obtiene todos los items con paginación
     */
    Page<InventoryProjection> findAll(Pageable pageable);

    /**
     * Cuenta items por tienda
     */
    long countByStoreId(String storeId);

    /**
     * Guarda o actualiza una proyección (para event consumer)
     */
    InventoryProjection save(InventoryProjection projection);

    /**
     * Elimina una proyección por ID (para event consumer)
     */
    void deleteById(String id);
}
