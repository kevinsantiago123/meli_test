package com.meli.inventory.command.service.domain.port.in;

import com.meli.inventory.command.service.domain.model.InventoryItem;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.transaction.annotation.Transactional;

/**
 * Puerto de entrada (Input Port) - Casos de uso de Inventario
 *
 * Define las operaciones disponibles en el dominio.
 * Implementado por servicios de aplicación.
 */
public interface InventoryCommandService {

    /**
     * Actualiza la cantidad de stock
     */
    @Transactional
    @CircuitBreaker(name = "inventoryCommand", fallbackMethod = "updateStockFallback")
    @Retry(name = "inventoryCommand")
    @Bulkhead(name = "inventoryCommand")
    InventoryItem updateStock(UpdateStockCommand command);

    /**
     * Crea un nuevo item de inventario
     */
    @Transactional
    @CircuitBreaker(name = "inventoryCommand", fallbackMethod = "createInventoryItemFallback")
    @Retry(name = "inventoryCommand")
    @Bulkhead(name = "inventoryCommand")
    InventoryItem createInventoryItem(CreateInventoryItemCommand command);


    /**
     * Reserva stock para una orden
     */
    @Transactional
    @CircuitBreaker(name = "inventoryCommand", fallbackMethod = "reserveStockFallback")
    @Retry(name = "inventoryCommand")
    @Bulkhead(name = "inventoryCommand")
    InventoryItem reserveStock(ReserveStockCommand command);

    /**
     * Libera stock reservado
     */
    @Transactional
    @CircuitBreaker(name = "inventoryCommand")
    @Retry(name = "inventoryCommand")
    InventoryItem releaseReservedStock(ReleaseStockCommand command);

    /**
     * Confirma una reserva
     */
    InventoryItem confirmReservation(ConfirmReservationCommand command);

    /**
     * Elimina un item de inventario
     */
    void deleteInventoryItem(String id, String userId);
}
