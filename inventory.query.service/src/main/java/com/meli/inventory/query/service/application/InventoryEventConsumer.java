package com.meli.inventory.query.service.application;

import com.meli.inventory.query.service.domain.model.InventoryProjection;
import com.meli.inventory.query.service.domain.port.out.InventoryQueryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Event Consumer - Sincroniza proyecciones de lectura
 *
 * Consume eventos del Command Service y actualiza las proyecciones de lectura.
 * Implementa Eventual Consistency.
 * En producción, esto sería reemplazado por:
 * - Kafka Consumer
 * - RabbitMQ Listener
 * - AWS SQS Consumer
 * Para este prototipo, hace polling periódico al Command Service.
 */
@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final InventoryQueryRepository queryRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String COMMAND_SERVICE_URL = "http://localhost:8081";
    private static final String EVENTS_ENDPOINT = COMMAND_SERVICE_URL + "/api/v1/events/pending";

    /**
     * Consume eventos cada 5 segundos
     * En producción, sería event-driven con Kafka/RabbitMQ
     */
    @Scheduled(fixedDelay = 5000, initialDelay = 10000)
    public void consumeEvents() {
        try {
            log.debug("Polling for new events from Command Service...");

            // Obtener eventos pendientes del Command Service
            ResponseEntity<List<InventoryEventDto>> response = restTemplate.exchange(
                    EVENTS_ENDPOINT,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<InventoryEventDto>>() {}
            );

            List<InventoryEventDto> events = response.getBody();

            if (events != null && !events.isEmpty()) {
                log.info("Received {} events from Command Service", events.size());
                processEvents(events);
            }

        } catch (Exception e) {
            log.error("Error consuming events: {}", e.getMessage());
            // En producción, implementar dead letter queue y retry logic
        }
    }

    /**
     * Procesa eventos y actualiza proyecciones
     */
    private void processEvents(List<InventoryEventDto> events) {
        for (InventoryEventDto event : events) {
            try {
                processEvent(event);
            } catch (Exception e) {
                log.error("Error processing event {}: {}", event.getEventId(), e.getMessage());
                // Continuar con el siguiente evento
            }
        }
    }

    /**
     * Procesa un evento individual
     */
    private void processEvent(InventoryEventDto event) {
        log.debug("Processing event: {} - Type: {}", event.getEventId(), event.getEventType());

        switch (event.getEventType()) {
            case "ITEM_CREATED" -> handleItemCreated(event);
            case "STOCK_UPDATED", "STOCK_ADDED", "STOCK_REDUCED" -> handleStockUpdated(event);
            case "STOCK_RESERVED" -> handleStockReserved(event);
            case "RESERVATION_RELEASED" -> handleReservationReleased(event);
            case "RESERVATION_CONFIRMED" -> handleReservationConfirmed(event);
            case "ITEM_DELETED" -> handleItemDeleted(event);
            default -> log.warn("Unknown event type: {}", event.getEventType());
        }
    }

    private void handleItemCreated(InventoryEventDto event) {
        Map<String, Object> payload = event.getPayload();

        InventoryProjection projection = InventoryProjection.builder()
                .id(event.getAggregateId())
                .storeId(event.getStoreId())
                .storeName(getStoreName(event.getStoreId()))
                .productId((String) payload.get("productId"))
                .productName((String) payload.get("productName"))
                .category("General") // Default category
                .quantity((Integer) payload.get("quantity"))
                .reservedQuantity(0)
                .minThreshold((Integer) payload.get("minThreshold"))
                .version(event.getVersion())
                .lastUpdated(event.getTimestamp())
                .lastModifiedBy(event.getUserId())
                .build();

        queryRepository.save(projection);
        log.info("Created projection for item: {}", projection.getId());
    }

    private void handleStockUpdated(InventoryEventDto event) {
        InventoryProjection projection = queryRepository.findById(event.getAggregateId())
                .orElseThrow(() -> new RuntimeException("Projection not found: " + event.getAggregateId()));

        Map<String, Object> payload = event.getPayload();
        projection.setQuantity((Integer) payload.get("newQuantity"));
        projection.setVersion(event.getVersion());
        projection.setLastUpdated(event.getTimestamp());
        projection.setLastModifiedBy(event.getUserId());

        queryRepository.save(projection);
        log.info("Updated stock for item: {} - New quantity: {}",
                projection.getId(), projection.getQuantity());
    }

    private void handleStockReserved(InventoryEventDto event) {
        InventoryProjection projection = queryRepository.findById(event.getAggregateId())
                .orElseThrow(() -> new RuntimeException("Projection not found: " + event.getAggregateId()));

        Map<String, Object> payload = event.getPayload();
        projection.setReservedQuantity((Integer) payload.get("newReserved"));
        projection.setVersion(event.getVersion());
        projection.setLastUpdated(event.getTimestamp());
        projection.setLastModifiedBy(event.getUserId());

        queryRepository.save(projection);
        log.info("Reserved stock for item: {} - Reserved: {}",
                projection.getId(), projection.getReservedQuantity());
    }

    private void handleReservationReleased(InventoryEventDto event) {
        InventoryProjection projection = queryRepository.findById(event.getAggregateId())
                .orElseThrow(() -> new RuntimeException("Projection not found: " + event.getAggregateId()));

        Map<String, Object> payload = event.getPayload();
        projection.setReservedQuantity((Integer) payload.get("newReserved"));
        projection.setVersion(event.getVersion());
        projection.setLastUpdated(event.getTimestamp());
        projection.setLastModifiedBy(event.getUserId());

        queryRepository.save(projection);
        log.info("Released reservation for item: {} - Reserved: {}",
                projection.getId(), projection.getReservedQuantity());
    }

    private void handleReservationConfirmed(InventoryEventDto event) {
        InventoryProjection projection = queryRepository.findById(event.getAggregateId())
                .orElseThrow(() -> new RuntimeException("Projection not found: " + event.getAggregateId()));

        Map<String, Object> payload = event.getPayload();
        projection.setQuantity((Integer) payload.get("newQuantity"));
        projection.setReservedQuantity((Integer) payload.get("newReserved"));
        projection.setVersion(event.getVersion());
        projection.setLastUpdated(event.getTimestamp());
        projection.setLastModifiedBy(event.getUserId());

        queryRepository.save(projection);
        log.info("Confirmed reservation for item: {} - Quantity: {}, Reserved: {}",
                projection.getId(), projection.getQuantity(), projection.getReservedQuantity());
    }

    private void handleItemDeleted(InventoryEventDto event) {
        queryRepository.deleteById(event.getAggregateId());
        log.info("Deleted projection for item: {}", event.getAggregateId());
    }

    /**
     * Helper para obtener nombre de tienda
     * En producción, esto vendría de un servicio de catálogo
     */
    private String getStoreName(String storeId) {
        return switch (storeId) {
            case "STORE-001" -> "Store Central";
            case "STORE-002" -> "Store North";
            case "STORE-003" -> "Store South";
            default -> "Store " + storeId;
        };
    }

    /**
     * DTO para eventos recibidos del Command Service
     */
    public static class InventoryEventDto {
        private String eventId;
        private String eventType;
        private String aggregateId;
        private String storeId;
        private LocalDateTime timestamp;
        private String userId;
        private Map<String, Object> payload;
        private Long version;

        // Getters y Setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }

        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public String getAggregateId() { return aggregateId; }
        public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }

        public String getStoreId() { return storeId; }
        public void setStoreId(String storeId) { this.storeId = storeId; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public Map<String, Object> getPayload() { return payload; }
        public void setPayload(Map<String, Object> payload) { this.payload = payload; }

        public Long getVersion() { return version; }
        public void setVersion(Long version) { this.version = version; }
    }
}
