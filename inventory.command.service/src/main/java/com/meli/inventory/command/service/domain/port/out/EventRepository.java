package com.meli.inventory.command.service.domain.port.out;

import com.meli.inventory.command.service.domain.model.InventoryEvent;

import java.util.List;

/**
 * Puerto de salida (Output Port) - Repositorio de Eventos
 * Almacena eventos para Event Sourcing y auditoría.
 */
public interface EventRepository {

    /**
     * Guarda un evento
     */
    void save(InventoryEvent event);

    /**
     * Obtiene todos los eventos de un aggregate
     */
    List<InventoryEvent> findByAggregateId(String aggregateId);

    /**
     * Obtiene todos los eventos de una tienda
     */
    List<InventoryEvent> findByStoreId(String storeId);

    /**
     * Obtiene eventos por tipo
     */
    List<InventoryEvent> findByEventType(InventoryEvent.EventType eventType);
}

