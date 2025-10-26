package com.meli.inventory.command.service.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InventoryEvent - Domain Model Tests")
class InventoryEventTest {

    private Map<String, Object> testPayload;
    private String testAggregateId;
    private String testStoreId;
    private String testUserId;
    private Long testVersion;

    @BeforeEach
    void setUp() {
        // Arrange - Datos comunes para todos los tests
        testAggregateId = "INV-001";
        testStoreId = "STORE-001";
        testUserId = "USER-123";
        testVersion = 1L;

        testPayload = new HashMap<>();
        testPayload.put("productId", "PROD-001");
        testPayload.put("quantity", 100);
    }

    @Test
    @DisplayName("Debe crear un evento con el método builder")
    void shouldCreateEventUsingBuilder() {
        // Arrange
        String eventId = "EVENT-001";
        LocalDateTime timestamp = LocalDateTime.now();

        // Act
        InventoryEvent event = InventoryEvent.builder()
                .eventId(eventId)
                .eventType(InventoryEvent.EventType.ITEM_CREATED)
                .aggregateId(testAggregateId)
                .storeId(testStoreId)
                .timestamp(timestamp)
                .userId(testUserId)
                .payload(testPayload)
                .version(testVersion)
                .build();

        // Assert
        assertThat(event).isNotNull();
        assertThat(event.getEventId()).isEqualTo(eventId);
        assertThat(event.getEventType()).isEqualTo(InventoryEvent.EventType.ITEM_CREATED);
        assertThat(event.getAggregateId()).isEqualTo(testAggregateId);
        assertThat(event.getStoreId()).isEqualTo(testStoreId);
        assertThat(event.getTimestamp()).isEqualTo(timestamp);
        assertThat(event.getUserId()).isEqualTo(testUserId);
        assertThat(event.getPayload()).isEqualTo(testPayload);
        assertThat(event.getVersion()).isEqualTo(testVersion);
    }

    @Test
    @DisplayName("Debe crear un evento usando el método create()")
    void shouldCreateEventUsingFactoryMethod() {
        // Act
        InventoryEvent event = InventoryEvent.create(
                InventoryEvent.EventType.STOCK_UPDATED,
                testAggregateId,
                testStoreId,
                testUserId,
                testPayload,
                testVersion
        );

        // Assert
        assertThat(event).isNotNull();
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getEventType()).isEqualTo(InventoryEvent.EventType.STOCK_UPDATED);
        assertThat(event.getAggregateId()).isEqualTo(testAggregateId);
        assertThat(event.getStoreId()).isEqualTo(testStoreId);
        assertThat(event.getUserId()).isEqualTo(testUserId);
        assertThat(event.getPayload()).isEqualTo(testPayload);
        assertThat(event.getVersion()).isEqualTo(testVersion);
        assertThat(event.getTimestamp()).isNotNull();
        assertThat(event.getTimestamp()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    @DisplayName("Debe generar un eventId único con UUID")
    void shouldGenerateUniqueEventId() {
        // Act
        InventoryEvent event1 = InventoryEvent.create(
                InventoryEvent.EventType.ITEM_CREATED,
                testAggregateId,
                testStoreId,
                testUserId,
                testPayload,
                testVersion
        );

        InventoryEvent event2 = InventoryEvent.create(
                InventoryEvent.EventType.ITEM_CREATED,
                testAggregateId,
                testStoreId,
                testUserId,
                testPayload,
                testVersion
        );

        // Assert
        assertThat(event1.getEventId()).isNotNull();
        assertThat(event2.getEventId()).isNotNull();
        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
    }

    @Test
    @DisplayName("Debe establecer el timestamp automáticamente al crear el evento")
    void shouldSetTimestampAutomatically() {
        // Arrange
        LocalDateTime beforeCreation = LocalDateTime.now();

        // Act
        InventoryEvent event = InventoryEvent.create(
                InventoryEvent.EventType.STOCK_ADDED,
                testAggregateId,
                testStoreId,
                testUserId,
                testPayload,
                testVersion
        );

        LocalDateTime afterCreation = LocalDateTime.now();

        // Assert
        assertThat(event.getTimestamp()).isNotNull();
        assertThat(event.getTimestamp()).isAfterOrEqualTo(beforeCreation);
        assertThat(event.getTimestamp()).isBeforeOrEqualTo(afterCreation);
    }

    @Test
    @DisplayName("Debe crear eventos para todos los tipos de EventType")
    void shouldCreateEventsForAllEventTypes() {
        // Arrange & Act & Assert
        for (InventoryEvent.EventType eventType : InventoryEvent.EventType.values()) {
            InventoryEvent event = InventoryEvent.create(
                    eventType,
                    testAggregateId,
                    testStoreId,
                    testUserId,
                    testPayload,
                    testVersion
            );

            assertThat(event).isNotNull();
            assertThat(event.getEventType()).isEqualTo(eventType);
        }
    }

    @Test
    @DisplayName("Debe permitir payload vacío")
    void shouldAllowEmptyPayload() {
        // Arrange
        Map<String, Object> emptyPayload = new HashMap<>();

        // Act
        InventoryEvent event = InventoryEvent.create(
                InventoryEvent.EventType.ITEM_DELETED,
                testAggregateId,
                testStoreId,
                testUserId,
                emptyPayload,
                testVersion
        );

        // Assert
        assertThat(event.getPayload()).isNotNull();
        assertThat(event.getPayload()).isEmpty();
    }

    @Test
    @DisplayName("Debe permitir payload con múltiples valores")
    void shouldAllowPayloadWithMultipleValues() {
        // Arrange
        Map<String, Object> complexPayload = new HashMap<>();
        complexPayload.put("productId", "PROD-001");
        complexPayload.put("quantity", 100);
        complexPayload.put("oldQuantity", 50);
        complexPayload.put("reason", "Restock");
        complexPayload.put("price", 99.99);

        // Act
        InventoryEvent event = InventoryEvent.create(
                InventoryEvent.EventType.STOCK_UPDATED,
                testAggregateId,
                testStoreId,
                testUserId,
                complexPayload,
                testVersion
        );

        // Assert
        assertThat(event.getPayload()).hasSize(5);
        assertThat(event.getPayload().get("productId")).isEqualTo("PROD-001");
        assertThat(event.getPayload().get("quantity")).isEqualTo(100);
        assertThat(event.getPayload().get("oldQuantity")).isEqualTo(50);
        assertThat(event.getPayload().get("reason")).isEqualTo("Restock");
        assertThat(event.getPayload().get("price")).isEqualTo(99.99);
    }

    @Test
    @DisplayName("Debe incrementar la versión del evento")
    void shouldIncrementEventVersion() {
        // Act
        InventoryEvent event1 = InventoryEvent.create(
                InventoryEvent.EventType.ITEM_CREATED,
                testAggregateId,
                testStoreId,
                testUserId,
                testPayload,
                1L
        );

        InventoryEvent event2 = InventoryEvent.create(
                InventoryEvent.EventType.STOCK_UPDATED,
                testAggregateId,
                testStoreId,
                testUserId,
                testPayload,
                2L
        );

        // Assert
        assertThat(event1.getVersion()).isEqualTo(1L);
        assertThat(event2.getVersion()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Debe usar @NoArgsConstructor correctamente")
    void shouldCreateEventWithNoArgsConstructor() {
        // Act
        InventoryEvent event = new InventoryEvent();

        // Assert
        assertThat(event).isNotNull();
        assertThat(event.getEventId()).isNull();
        assertThat(event.getEventType()).isNull();
        assertThat(event.getTimestamp()).isNull();
    }

    @Test
    @DisplayName("Debe usar @AllArgsConstructor correctamente")
    void shouldCreateEventWithAllArgsConstructor() {
        // Arrange
        String eventId = "EVENT-001";
        LocalDateTime timestamp = LocalDateTime.now();

        // Act
        InventoryEvent event = new InventoryEvent(
                eventId,
                InventoryEvent.EventType.STOCK_RESERVED,
                testAggregateId,
                testStoreId,
                timestamp,
                testUserId,
                testPayload,
                testVersion
        );

        // Assert
        assertThat(event.getEventId()).isEqualTo(eventId);
        assertThat(event.getEventType()).isEqualTo(InventoryEvent.EventType.STOCK_RESERVED);
        assertThat(event.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    @DisplayName("Debe permitir modificar propiedades con setters")
    void shouldAllowModifyingPropertiesWithSetters() {
        // Arrange
        InventoryEvent event = new InventoryEvent();

        // Act
        event.setEventId("EVENT-NEW");
        event.setEventType(InventoryEvent.EventType.LOW_STOCK_ALERT);
        event.setAggregateId("INV-002");

        // Assert
        assertThat(event.getEventId()).isEqualTo("EVENT-NEW");
        assertThat(event.getEventType()).isEqualTo(InventoryEvent.EventType.LOW_STOCK_ALERT);
        assertThat(event.getAggregateId()).isEqualTo("INV-002");
    }

    @Test
    @DisplayName("Debe verificar que todos los EventTypes estén definidos")
    void shouldHaveAllExpectedEventTypes() {
        // Arrange
        InventoryEvent.EventType[] expectedTypes = {
                InventoryEvent.EventType.ITEM_CREATED,
                InventoryEvent.EventType.STOCK_UPDATED,
                InventoryEvent.EventType.STOCK_ADDED,
                InventoryEvent.EventType.STOCK_REDUCED,
                InventoryEvent.EventType.STOCK_RESERVED,
                InventoryEvent.EventType.RESERVATION_RELEASED,
                InventoryEvent.EventType.RESERVATION_CONFIRMED,
                InventoryEvent.EventType.ITEM_DELETED,
                InventoryEvent.EventType.THRESHOLD_UPDATED,
                InventoryEvent.EventType.LOW_STOCK_ALERT
        };

        // Act
        InventoryEvent.EventType[] actualTypes = InventoryEvent.EventType.values();

        // Assert
        assertThat(actualTypes).containsExactlyInAnyOrder(expectedTypes);
        assertThat(actualTypes).hasSize(10);
    }
}