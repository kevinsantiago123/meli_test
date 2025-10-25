package com.meli.inventory.command.service.domain.port.out;

import com.meli.inventory.command.service.domain.model.InventoryItem;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida (Output Port) - Repositorio de Inventario
 *
 * Define el contrato para persistencia sin especificar implementación.
 * Implementado por adaptadores en la capa de infraestructura.
 */
public interface InventoryRepository {

    /**
     * Guarda o actualiza un item de inventario
     */
    InventoryItem save(InventoryItem item);

    /**
     * Busca un item por ID
     */
    Optional<InventoryItem> findById(String id);

    /**
     * Busca un item por producto y tienda
     */
    Optional<InventoryItem> findByProductIdAndStoreId(String productId, String storeId);

    /**
     * Busca todos los items de una tienda
     */
    List<InventoryItem> findByStoreId(String storeId);

    /**
     * Busca todos los items
     */
    List<InventoryItem> findAll();

    /**
     * Elimina un item por ID
     */
    void deleteById(String id);

    /**
     * Verifica si existe un item
     */
    boolean existsByProductIdAndStoreId(String productId, String storeId);
}
