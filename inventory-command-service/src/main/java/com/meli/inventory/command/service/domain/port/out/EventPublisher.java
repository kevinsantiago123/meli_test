package com.meli.inventory.command.service.domain.port.out;

import com.meli.inventory.command.service.domain.model.InventoryEvent;

import java.util.List;

/**
 * Puerto de salida (Output Port) - Publicador de Eventos
 * Publica eventos a otros servicios (Query Service, Sync Service).
 */
public interface EventPublisher {

    /**
     * Publica un evento de inventario
     */
    void publish(InventoryEvent event);

    /**
     * Publica múltiples eventos en batch
     */
    void publishBatch(List<InventoryEvent> events);
}
