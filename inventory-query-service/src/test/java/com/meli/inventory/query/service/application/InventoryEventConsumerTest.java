package com.meli.inventory.query.service.application;

import com.meli.inventory.query.service.domain.model.InventoryProjection;
import com.meli.inventory.query.service.domain.port.out.InventoryQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryEventConsumer - Unit Tests")
class InventoryEventConsumerTest {

    @Mock
    private InventoryQueryRepository queryRepository;

    @InjectMocks
    private InventoryEventConsumer eventConsumer;

    private String testAggregateId;
    private String testStoreId;
    private String testProductId;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testAggregateId = "AGG-001";
        testStoreId = "STORE-001";
        testProductId = "PROD-001";
        testUserId = "USER-001";
    }

    @Nested
    @DisplayName("Tests de handleItemCreated()")
    class HandleItemCreatedTests {

        @Test
        @DisplayName("Debe crear proyección cuando se recibe evento ITEM_CREATED")
        void shouldCreateProjectionWhenItemCreatedEventReceived() {
            // Arrange
            Map<String, Object> payload = new HashMap<>();
            payload.put("productId", testProductId);
            payload.put("productName", "Test Product");
            payload.put("quantity", 100);
            payload.put("minThreshold", 10);

            InventoryEventConsumer.InventoryEventDto event = createEvent(
                    "ITEM_CREATED", testAggregateId, testStoreId, payload);

            ArgumentCaptor<InventoryProjection> projectionCaptor =
                    ArgumentCaptor.forClass(InventoryProjection.class);

            // Act
            ReflectionTestUtils.invokeMethod(eventConsumer, "handleItemCreated", event);

            // Assert
            verify(queryRepository).save(projectionCaptor.capture());
            InventoryProjection savedProjection = projectionCaptor.getValue();

            assertThat(savedProjection.getId()).isEqualTo(testAggregateId);
            assertThat(savedProjection.getStoreId()).isEqualTo(testStoreId);
            assertThat(savedProjection.getStoreName()).isEqualTo("Store Central");
            assertThat(savedProjection.getProductId()).isEqualTo(testProductId);
            assertThat(savedProjection.getProductName()).isEqualTo("Test Product");
            assertThat(savedProjection.getQuantity()).isEqualTo(100);
            assertThat(savedProjection.getReservedQuantity()).isEqualTo(0);
            assertThat(savedProjection.getMinThreshold()).isEqualTo(10);
            assertThat(savedProjection.getCategory()).isEqualTo("General");
        }

        @Test
        @DisplayName("Debe asignar storeName correcto según storeId")
        void shouldAssignCorrectStoreNameBasedOnStoreId() {
            // Arrange
            Map<String, Object> payload = new HashMap<>();
            payload.put("productId", testProductId);
            payload.put("productName", "Test Product");
            payload.put("quantity", 100);
            payload.put("minThreshold", 10);

            // Test para diferentes stores
            String[] storeIds = {"STORE-001", "STORE-002", "STORE-003", "STORE-999"};
            String[] expectedNames = {"Store Central", "Store North", "Store South", "Store STORE-999"};

            for (int i = 0; i < storeIds.length; i++) {
                InventoryEventConsumer.InventoryEventDto event = createEvent(
                        "ITEM_CREATED", testAggregateId, storeIds[i], payload);

                ArgumentCaptor<InventoryProjection> captor =
                        ArgumentCaptor.forClass(InventoryProjection.class);

                // Act
                ReflectionTestUtils.invokeMethod(eventConsumer, "handleItemCreated", event);

                // Assert
                verify(queryRepository, atLeastOnce()).save(captor.capture());
                assertThat(captor.getValue().getStoreName()).isEqualTo(expectedNames[i]);
            }
        }
    }

    @Nested
    @DisplayName("Tests de handleStockUpdated()")
    class HandleStockUpdatedTests {

        @Test
        @DisplayName("Debe actualizar cantidad cuando se recibe evento STOCK_UPDATED")
        void shouldUpdateQuantityWhenStockUpdatedEventReceived() {
            // Arrange
            InventoryProjection existingProjection = InventoryProjection.builder()
                    .id(testAggregateId)
                    .storeId(testStoreId)
                    .quantity(100)
                    .version(1L)
                    .build();

            Map<String, Object> payload = new HashMap<>();
            payload.put("newQuantity", 150);

            InventoryEventConsumer.InventoryEventDto event = createEvent(
                    "STOCK_UPDATED", testAggregateId, testStoreId, payload);
            event.setVersion(2L);

            when(queryRepository.findById(testAggregateId))
                    .thenReturn(Optional.of(existingProjection));

            // Act
            ReflectionTestUtils.invokeMethod(eventConsumer, "handleStockUpdated", event);

            // Assert
            verify(queryRepository).findById(testAggregateId);
            verify(queryRepository).save(any(InventoryProjection.class));
            assertThat(existingProjection.getQuantity()).isEqualTo(150);
            assertThat(existingProjection.getVersion()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Debe lanzar excepción si la proyección no existe")
        void shouldThrowExceptionWhenProjectionNotFound() {
            // Arrange
            Map<String, Object> payload = new HashMap<>();
            payload.put("newQuantity", 150);

            InventoryEventConsumer.InventoryEventDto event = createEvent(
                    "STOCK_UPDATED", testAggregateId, testStoreId, payload);

            when(queryRepository.findById(testAggregateId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() ->
                    ReflectionTestUtils.invokeMethod(eventConsumer, "handleStockUpdated", event))
                    .isInstanceOf(RuntimeException.class)  // Cambiado aquí
                    .hasMessageContaining("Projection not found");
        }
    }

    @Nested
    @DisplayName("Tests de handleStockReserved()")
    class HandleStockReservedTests {

        @Test
        @DisplayName("Debe actualizar cantidad reservada cuando se recibe evento STOCK_RESERVED")
        void shouldUpdateReservedQuantityWhenStockReservedEventReceived() {
            // Arrange
            InventoryProjection existingProjection = InventoryProjection.builder()
                    .id(testAggregateId)
                    .storeId(testStoreId)
                    .quantity(100)
                    .reservedQuantity(0)
                    .version(1L)
                    .build();

            Map<String, Object> payload = new HashMap<>();
            payload.put("newReserved", 30);

            InventoryEventConsumer.InventoryEventDto event = createEvent(
                    "STOCK_RESERVED", testAggregateId, testStoreId, payload);
            event.setVersion(2L);

            when(queryRepository.findById(testAggregateId))
                    .thenReturn(Optional.of(existingProjection));

            // Act
            ReflectionTestUtils.invokeMethod(eventConsumer, "handleStockReserved", event);

            // Assert
            verify(queryRepository).save(any(InventoryProjection.class));
            assertThat(existingProjection.getReservedQuantity()).isEqualTo(30);
            assertThat(existingProjection.getVersion()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("Tests de handleReservationReleased()")
    class HandleReservationReleasedTests {

        @Test
        @DisplayName("Debe liberar cantidad reservada cuando se recibe evento RESERVATION_RELEASED")
        void shouldReleaseReservedQuantityWhenReservationReleasedEventReceived() {
            // Arrange
            InventoryProjection existingProjection = InventoryProjection.builder()
                    .id(testAggregateId)
                    .storeId(testStoreId)
                    .quantity(100)
                    .reservedQuantity(30)
                    .version(2L)
                    .build();

            Map<String, Object> payload = new HashMap<>();
            payload.put("newReserved", 10);

            InventoryEventConsumer.InventoryEventDto event = createEvent(
                    "RESERVATION_RELEASED", testAggregateId, testStoreId, payload);
            event.setVersion(3L);

            when(queryRepository.findById(testAggregateId))
                    .thenReturn(Optional.of(existingProjection));

            // Act
            ReflectionTestUtils.invokeMethod(eventConsumer, "handleReservationReleased", event);

            // Assert
            verify(queryRepository).save(any(InventoryProjection.class));
            assertThat(existingProjection.getReservedQuantity()).isEqualTo(10);
            assertThat(existingProjection.getVersion()).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("Tests de handleReservationConfirmed()")
    class HandleReservationConfirmedTests {

        @Test
        @DisplayName("Debe confirmar reserva actualizando cantidad y reservado")
        void shouldConfirmReservationUpdatingQuantityAndReserved() {
            // Arrange
            InventoryProjection existingProjection = InventoryProjection.builder()
                    .id(testAggregateId)
                    .storeId(testStoreId)
                    .quantity(100)
                    .reservedQuantity(30)
                    .version(2L)
                    .build();

            Map<String, Object> payload = new HashMap<>();
            payload.put("newQuantity", 70);
            payload.put("newReserved", 0);

            InventoryEventConsumer.InventoryEventDto event = createEvent(
                    "RESERVATION_CONFIRMED", testAggregateId, testStoreId, payload);
            event.setVersion(3L);

            when(queryRepository.findById(testAggregateId))
                    .thenReturn(Optional.of(existingProjection));

            // Act
            ReflectionTestUtils.invokeMethod(eventConsumer, "handleReservationConfirmed", event);

            // Assert
            verify(queryRepository).save(any(InventoryProjection.class));
            assertThat(existingProjection.getQuantity()).isEqualTo(70);
            assertThat(existingProjection.getReservedQuantity()).isEqualTo(0);
            assertThat(existingProjection.getVersion()).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("Tests de handleItemDeleted()")
    class HandleItemDeletedTests {

        @Test
        @DisplayName("Debe eliminar proyección cuando se recibe evento ITEM_DELETED")
        void shouldDeleteProjectionWhenItemDeletedEventReceived() {
            // Arrange
            InventoryEventConsumer.InventoryEventDto event = createEvent(
                    "ITEM_DELETED", testAggregateId, testStoreId, new HashMap<>());

            // Act
            ReflectionTestUtils.invokeMethod(eventConsumer, "handleItemDeleted", event);

            // Assert
            verify(queryRepository).deleteById(testAggregateId);
        }
    }

    @Nested
    @DisplayName("Tests de processEvent()")
    class ProcessEventTests {

        @Test
        @DisplayName("Debe procesar evento ITEM_CREATED")
        void shouldProcessItemCreatedEvent() {
            // Arrange
            Map<String, Object> payload = new HashMap<>();
            payload.put("productId", testProductId);
            payload.put("productName", "Test Product");
            payload.put("quantity", 100);
            payload.put("minThreshold", 10);

            InventoryEventConsumer.InventoryEventDto event = createEvent(
                    "ITEM_CREATED", testAggregateId, testStoreId, payload);

            // Act
            ReflectionTestUtils.invokeMethod(eventConsumer, "processEvent", event);

            // Assert
            verify(queryRepository).save(any(InventoryProjection.class));
        }

        @Test
        @DisplayName("Debe procesar eventos STOCK_UPDATED, STOCK_ADDED, STOCK_REDUCED")
        void shouldProcessStockUpdateEvents() {
            // Arrange
            InventoryProjection existingProjection = InventoryProjection.builder()
                    .id(testAggregateId)
                    .quantity(100)
                    .build();

            Map<String, Object> payload = new HashMap<>();
            payload.put("newQuantity", 150);

            when(queryRepository.findById(testAggregateId))
                    .thenReturn(Optional.of(existingProjection));

            String[] eventTypes = {"STOCK_UPDATED", "STOCK_ADDED", "STOCK_REDUCED"};

            for (String eventType : eventTypes) {
                InventoryEventConsumer.InventoryEventDto event = createEvent(
                        eventType, testAggregateId, testStoreId, payload);

                // Act
                ReflectionTestUtils.invokeMethod(eventConsumer, "processEvent", event);
            }

            // Assert - Se llama 3 veces (una por cada tipo de evento)
            verify(queryRepository, times(3)).save(any(InventoryProjection.class));
        }

        @Test
        @DisplayName("Debe manejar evento desconocido sin lanzar excepción")
        void shouldHandleUnknownEventTypeWithoutException() {
            // Arrange
            InventoryEventConsumer.InventoryEventDto event = createEvent(
                    "UNKNOWN_EVENT", testAggregateId, testStoreId, new HashMap<>());

            // Act & Assert - No debe lanzar excepción
            assertThatNoException().isThrownBy(() ->
                    ReflectionTestUtils.invokeMethod(eventConsumer, "processEvent", event));

            verify(queryRepository, never()).save(any());
            verify(queryRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Tests de processEvents()")
    class ProcessEventsTests {

        @Test
        @DisplayName("Debe procesar múltiples eventos")
        void shouldProcessMultipleEvents() {
            // Arrange
            Map<String, Object> payload = new HashMap<>();
            payload.put("productId", testProductId);
            payload.put("productName", "Test Product");
            payload.put("quantity", 100);
            payload.put("minThreshold", 10);

            List<InventoryEventConsumer.InventoryEventDto> events = Arrays.asList(
                    createEvent("ITEM_CREATED", "AGG-001", testStoreId, payload),
                    createEvent("ITEM_CREATED", "AGG-002", testStoreId, payload),
                    createEvent("ITEM_CREATED", "AGG-003", testStoreId, payload)
            );

            // Act
            ReflectionTestUtils.invokeMethod(eventConsumer, "processEvents", events);

            // Assert
            verify(queryRepository, times(3)).save(any(InventoryProjection.class));
        }

        @Test
        @DisplayName("Debe continuar procesando eventos si uno falla")
        void shouldContinueProcessingEventsIfOneFails() {
            // Arrange
            InventoryProjection existingProjection = InventoryProjection.builder()
                    .id("AGG-002")
                    .quantity(100)
                    .build();

            Map<String, Object> payload1 = new HashMap<>();
            payload1.put("productId", testProductId);
            payload1.put("productName", "Test Product");
            payload1.put("quantity", 100);
            payload1.put("minThreshold", 10);

            Map<String, Object> payload2 = new HashMap<>();
            payload2.put("newQuantity", 150);

            when(queryRepository.findById("AGG-002"))
                    .thenReturn(Optional.of(existingProjection));

            List<InventoryEventConsumer.InventoryEventDto> events = Arrays.asList(
                    createEvent("ITEM_CREATED", "AGG-001", testStoreId, payload1),
                    createEvent("STOCK_UPDATED", "AGG-002", testStoreId, payload2),
                    createEvent("ITEM_CREATED", "AGG-003", testStoreId, payload1)
            );

            // Act
            ReflectionTestUtils.invokeMethod(eventConsumer, "processEvents", events);

            // Assert - Todos los eventos deben procesarse
            verify(queryRepository, times(3)).save(any(InventoryProjection.class));
        }
    }

    @Nested
    @DisplayName("Tests de getStoreName()")
    class GetStoreNameTests {

        @Test
        @DisplayName("Debe retornar nombres correctos para stores conocidas")
        void shouldReturnCorrectNamesForKnownStores() {
            // Act & Assert
            assertThat((String) ReflectionTestUtils.invokeMethod(
                    eventConsumer, "getStoreName", "STORE-001"))
                    .isEqualTo("Store Central");

            assertThat((String) ReflectionTestUtils.invokeMethod(
                    eventConsumer, "getStoreName", "STORE-002"))
                    .isEqualTo("Store North");

            assertThat((String) ReflectionTestUtils.invokeMethod(
                    eventConsumer, "getStoreName", "STORE-003"))
                    .isEqualTo("Store South");
        }

        @Test
        @DisplayName("Debe retornar nombre genérico para stores desconocidas")
        void shouldReturnGenericNameForUnknownStores() {
            // Act & Assert
            assertThat((String) ReflectionTestUtils.invokeMethod(
                    eventConsumer, "getStoreName", "STORE-999"))
                    .isEqualTo("Store STORE-999");

            assertThat((String) ReflectionTestUtils.invokeMethod(
                    eventConsumer, "getStoreName", "UNKNOWN"))
                    .isEqualTo("Store UNKNOWN");
        }
    }

    @Nested
    @DisplayName("Tests del DTO InventoryEventDto")
    class InventoryEventDtoTests {

        @Test
        @DisplayName("Debe crear y usar getters/setters correctamente")
        void shouldCreateAndUseGettersSettersCorrectly() {
            // Arrange
            InventoryEventConsumer.InventoryEventDto dto =
                    new InventoryEventConsumer.InventoryEventDto();
            LocalDateTime now = LocalDateTime.now();
            Map<String, Object> payload = new HashMap<>();
            payload.put("test", "value");

            // Act
            dto.setEventId("EVENT-001");
            dto.setEventType("ITEM_CREATED");
            dto.setAggregateId(testAggregateId);
            dto.setStoreId(testStoreId);
            dto.setTimestamp(now);
            dto.setUserId(testUserId);
            dto.setPayload(payload);
            dto.setVersion(1L);

            // Assert
            assertThat(dto.getEventId()).isEqualTo("EVENT-001");
            assertThat(dto.getEventType()).isEqualTo("ITEM_CREATED");
            assertThat(dto.getAggregateId()).isEqualTo(testAggregateId);
            assertThat(dto.getStoreId()).isEqualTo(testStoreId);
            assertThat(dto.getTimestamp()).isEqualTo(now);
            assertThat(dto.getUserId()).isEqualTo(testUserId);
            assertThat(dto.getPayload()).isEqualTo(payload);
            assertThat(dto.getVersion()).isEqualTo(1L);
        }
    }

    // Helper method
    private InventoryEventConsumer.InventoryEventDto createEvent(
            String eventType, String aggregateId, String storeId, Map<String, Object> payload) {
        InventoryEventConsumer.InventoryEventDto event =
                new InventoryEventConsumer.InventoryEventDto();
        event.setEventId("EVENT-" + UUID.randomUUID());
        event.setEventType(eventType);
        event.setAggregateId(aggregateId);
        event.setStoreId(storeId);
        event.setTimestamp(LocalDateTime.now());
        event.setUserId(testUserId);
        event.setPayload(payload);
        event.setVersion(1L);
        return event;
    }
}