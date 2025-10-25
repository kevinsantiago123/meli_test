package com.meli.inventory.command.service.domain.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Domain Event - Representa un cambio en el inventario
 * Event Sourcing: Todos los cambios se registran como eventos
 * inmutables que pueden ser reproducidos para reconstruir el estado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEvent {

    /**
     * Identificador único del evento
     */
    private String eventId;

    /**
     * Tipo de evento
     */
    private EventType eventType;

    /**
     * ID del aggregate (InventoryItem) afectado
     */
    private String aggregateId;

    /**
     * ID de la tienda
     */
    private String storeId;

    /**
     * Timestamp del evento
     */
    private LocalDateTime timestamp;

    /**
     * Usuario que generó el evento
     */
    private String userId;

    /**
     * Payload con datos específicos del evento
     */
    private Map<String, Object> payload;

    /**
     * Versión del aggregate después del evento
     */
    private Long version;

    /**
     * Crea un nuevo evento
     */
    public static InventoryEvent create(EventType eventType, String aggregateId,
                                        String storeId, String userId,
                                        Map<String, Object> payload, Long version) {
        return InventoryEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .aggregateId(aggregateId)
                .storeId(storeId)
                .timestamp(LocalDateTime.now())
                .userId(userId)
                .payload(payload)
                .version(version)
                .build();
    }

    /**
     * Tipos de eventos de inventario
     */
    public enum EventType {
        ITEM_CREATED,
        STOCK_UPDATED,
        STOCK_ADDED,
        STOCK_REDUCED,
        STOCK_RESERVED,
        RESERVATION_RELEASED,
        RESERVATION_CONFIRMED,
        ITEM_DELETED,
        THRESHOLD_UPDATED,
        LOW_STOCK_ALERT
    }
}
