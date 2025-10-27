package com.meli.inventory.command.service.infrastructure.event;


import com.meli.inventory.command.service.domain.model.InventoryEvent;
import com.meli.inventory.command.service.domain.port.out.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Publicador de eventos simplificado
 *
 * En un entorno de producción, esto sería reemplazado por:
 * - Apache Kafka
 * - RabbitMQ
 * - AWS SNS/SQS
 * Para este prototipo, simplemente mantiene una cola en memoria
 * que puede ser consumida por otros servicios.
 */
@Component
@Slf4j
public class SimpleEventPublisher implements EventPublisher {

    private final List<InventoryEvent> eventQueue = new CopyOnWriteArrayList<>();

    @Override
    public void publish(InventoryEvent event) {
        eventQueue.add(event);
        log.info("Published event: {} for aggregate: {} (Type: {})",
                event.getEventId(), event.getAggregateId(), event.getEventType());

        // TODO: En producción, enviar a message broker
        // kafkaTemplate.send("inventory-events", event);
    }

    @Override
    public void publishBatch(List<InventoryEvent> events) {
        events.forEach(this::publish);
        log.info("Published batch of {} events", events.size());
    }

    /**
     * Método para que otros servicios consuman eventos
     * (Query Service, Sync Service)
     */
    public List<InventoryEvent> consumeEvents() {
        List<InventoryEvent> consumed = new ArrayList<>(eventQueue);
        eventQueue.clear();
        return consumed;
    }

    /**
     * Obtiene eventos sin consumirlos (peek)
     */
    public List<InventoryEvent> peekEvents() {
        return new ArrayList<>(eventQueue);
    }

    /**
     * Obtiene el tamaño de la cola
     */
    public int getQueueSize() {
        return eventQueue.size();
    }
}