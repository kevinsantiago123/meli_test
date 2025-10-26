package com.meli.inventory.command.service.infrastructure.persistence.csv;

import com.meli.inventory.command.service.domain.model.InventoryEvent;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CsvEventRepository - Unit Tests")
class CsvEventRepositoryTest {

    @TempDir
    Path tempDir;

    private CsvEventRepository repository;
    private String testFilename;

    @BeforeEach
    void setUp() throws IOException {
        repository = new CsvEventRepository();
        testFilename = "test-events.csv";

        // Configurar usando reflection para inyectar valores de las propiedades
        ReflectionTestUtils.setField(repository, "storagePath", tempDir.toString());
        ReflectionTestUtils.setField(repository, "filename", testFilename);

        // Inicializar el repositorio
        repository.init();
    }

    @Nested
    @DisplayName("Tests de inicialización")
    class InitializationTests {

        @Test
        @DisplayName("Debe crear archivo CSV con headers si no existe")
        void shouldCreateCsvFileWithHeadersIfNotExists() {
            // Assert
            Path filePath = tempDir.resolve(testFilename);
            assertThat(Files.exists(filePath)).isTrue();

            try {
                List<String> lines = Files.readAllLines(filePath);
                assertThat(lines).isNotEmpty();
                assertThat(lines.get(0)).contains("eventId", "eventType", "aggregateId");
            } catch (IOException e) {
                fail("Failed to read file", e);
            }
        }

        @Test
        @DisplayName("Debe crear directorio si no existe")
        void shouldCreateDirectoryIfNotExists() throws IOException {
            // Arrange
            Path newTempDir = tempDir.resolve("new-directory");
            CsvEventRepository newRepository = new CsvEventRepository();
            ReflectionTestUtils.setField(newRepository, "storagePath", newTempDir.toString());
            ReflectionTestUtils.setField(newRepository, "filename", "events.csv");

            // Act
            newRepository.init();

            // Assert
            assertThat(Files.exists(newTempDir)).isTrue();
            assertThat(Files.isDirectory(newTempDir)).isTrue();
        }
    }

    @Nested
    @DisplayName("Tests de save()")
    class SaveTests {

        @Test
        @DisplayName("Debe guardar evento correctamente")
        void shouldSaveEventSuccessfully() {
            // Arrange
            Map<String, Object> payload = new HashMap<>();
            payload.put("productId", "PROD-001");
            payload.put("quantity", 100);

            InventoryEvent event = InventoryEvent.builder()
                    .eventId("EVENT-001")
                    .eventType(InventoryEvent.EventType.ITEM_CREATED)
                    .aggregateId("AGG-001")
                    .storeId("STORE-001")
                    .timestamp(LocalDateTime.now())
                    .userId("USER-001")
                    .payload(payload)
                    .version(1L)
                    .build();

            // Act
            repository.save(event);

            // Assert
            List<InventoryEvent> events = repository.findByAggregateId("AGG-001");
            assertThat(events).hasSize(1);
            assertThat(events.get(0).getEventId()).isEqualTo("EVENT-001");
            assertThat(events.get(0).getEventType()).isEqualTo(InventoryEvent.EventType.ITEM_CREATED);
        }

        @Test
        @DisplayName("Debe persistir evento en archivo CSV")
        void shouldPersistEventToCsvFile() throws IOException {
            // Arrange
            Map<String, Object> payload = new HashMap<>();
            payload.put("test", "value");

            InventoryEvent event = InventoryEvent.builder()
                    .eventId("EVENT-001")
                    .eventType(InventoryEvent.EventType.STOCK_UPDATED)
                    .aggregateId("AGG-001")
                    .storeId("STORE-001")
                    .timestamp(LocalDateTime.now())
                    .userId("USER-001")
                    .payload(payload)
                    .version(1L)
                    .build();

            // Act
            repository.save(event);

            // Assert
            Path filePath = tempDir.resolve(testFilename);
            List<String> lines = Files.readAllLines(filePath);
            assertThat(lines).hasSizeGreaterThan(1); // Header + at least one event
            assertThat(lines.get(1)).contains("EVENT-001", "STOCK_UPDATED", "AGG-001");
        }

        @Test
        @DisplayName("Debe guardar múltiples eventos")
        void shouldSaveMultipleEvents() {
            // Arrange
            InventoryEvent event1 = createTestEvent("EVENT-001", "AGG-001");
            InventoryEvent event2 = createTestEvent("EVENT-002", "AGG-002");
            InventoryEvent event3 = createTestEvent("EVENT-003", "AGG-001");

            // Act
            repository.save(event1);
            repository.save(event2);
            repository.save(event3);

            // Assert
            List<InventoryEvent> eventsForAgg1 = repository.findByAggregateId("AGG-001");
            assertThat(eventsForAgg1).hasSize(2);

            List<InventoryEvent> eventsForAgg2 = repository.findByAggregateId("AGG-002");
            assertThat(eventsForAgg2).hasSize(1);
        }

        @Test
        @DisplayName("Debe guardar evento con payload complejo")
        void shouldSaveEventWithComplexPayload() {
            // Arrange
            Map<String, Object> payload = new HashMap<>();
            payload.put("productId", "PROD-001");
            payload.put("quantity", 100);
            payload.put("previousQuantity", 50);
            payload.put("operation", "ADD");

            Map<String, String> metadata = new HashMap<>();
            metadata.put("reason", "Restock");
            metadata.put("source", "Manual");
            payload.put("metadata", metadata);

            InventoryEvent event = InventoryEvent.builder()
                    .eventId("EVENT-001")
                    .eventType(InventoryEvent.EventType.STOCK_UPDATED)
                    .aggregateId("AGG-001")
                    .storeId("STORE-001")
                    .timestamp(LocalDateTime.now())
                    .userId("USER-001")
                    .payload(payload)
                    .version(1L)
                    .build();

            // Act
            repository.save(event);

            // Assert
            List<InventoryEvent> events = repository.findByAggregateId("AGG-001");
            assertThat(events).hasSize(1);
            assertThat(events.get(0).getPayload()).containsKeys("productId", "quantity", "metadata");
        }
    }

    @Nested
    @DisplayName("Tests de findByAggregateId()")
    class FindByAggregateIdTests {

        @Test
        @DisplayName("Debe encontrar eventos por aggregateId")
        void shouldFindEventsByAggregateId() {
            // Arrange
            repository.save(createTestEvent("EVENT-001", "AGG-001"));
            repository.save(createTestEvent("EVENT-002", "AGG-002"));
            repository.save(createTestEvent("EVENT-003", "AGG-001"));

            // Act
            List<InventoryEvent> events = repository.findByAggregateId("AGG-001");

            // Assert
            assertThat(events).hasSize(2);
            assertThat(events).extracting(InventoryEvent::getEventId)
                    .containsExactly("EVENT-001", "EVENT-003");
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no encuentra eventos")
        void shouldReturnEmptyListWhenNoEventsFound() {
            // Act
            List<InventoryEvent> events = repository.findByAggregateId("NON-EXISTENT");

            // Assert
            assertThat(events).isEmpty();
        }

        @Test
        @DisplayName("Debe retornar todos los eventos del mismo aggregate")
        void shouldReturnAllEventsForSameAggregate() {
            // Arrange
            String aggregateId = "AGG-001";
            for (int i = 0; i < 5; i++) {
                repository.save(createTestEvent("EVENT-" + i, aggregateId));
            }

            // Act
            List<InventoryEvent> events = repository.findByAggregateId(aggregateId);

            // Assert
            assertThat(events).hasSize(5);
        }
    }

    @Nested
    @DisplayName("Tests de findByStoreId()")
    class FindByStoreIdTests {

        @Test
        @DisplayName("Debe encontrar eventos por storeId")
        void shouldFindEventsByStoreId() {
            // Arrange
            repository.save(createTestEventWithStore("EVENT-001", "STORE-001"));
            repository.save(createTestEventWithStore("EVENT-002", "STORE-002"));
            repository.save(createTestEventWithStore("EVENT-003", "STORE-001"));

            // Act
            List<InventoryEvent> events = repository.findByStoreId("STORE-001");

            // Assert
            assertThat(events).hasSize(2);
            assertThat(events).extracting(InventoryEvent::getStoreId)
                    .containsOnly("STORE-001");
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no encuentra eventos para la tienda")
        void shouldReturnEmptyListWhenNoEventsForStore() {
            // Act
            List<InventoryEvent> events = repository.findByStoreId("NON-EXISTENT-STORE");

            // Assert
            assertThat(events).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tests de findByEventType()")
    class FindByEventTypeTests {

        @Test
        @DisplayName("Debe encontrar eventos por tipo")
        void shouldFindEventsByEventType() {
            // Arrange
            repository.save(createTestEventWithType("EVENT-001", InventoryEvent.EventType.ITEM_CREATED));
            repository.save(createTestEventWithType("EVENT-002", InventoryEvent.EventType.STOCK_UPDATED));
            repository.save(createTestEventWithType("EVENT-003", InventoryEvent.EventType.ITEM_CREATED));

            // Act
            List<InventoryEvent> events = repository.findByEventType(InventoryEvent.EventType.ITEM_CREATED);

            // Assert
            assertThat(events).hasSize(2);
            assertThat(events).extracting(InventoryEvent::getEventType)
                    .containsOnly(InventoryEvent.EventType.ITEM_CREATED);
        }

        @Test
        @DisplayName("Debe filtrar correctamente por cada tipo de evento")
        void shouldFilterCorrectlyByEachEventType() {
            // Arrange
            repository.save(createTestEventWithType("EVENT-001", InventoryEvent.EventType.ITEM_CREATED));
            repository.save(createTestEventWithType("EVENT-002", InventoryEvent.EventType.STOCK_RESERVED));
            repository.save(createTestEventWithType("EVENT-003", InventoryEvent.EventType.STOCK_UPDATED));

            // Act & Assert
            assertThat(repository.findByEventType(InventoryEvent.EventType.ITEM_CREATED)).hasSize(1);
            assertThat(repository.findByEventType(InventoryEvent.EventType.STOCK_RESERVED)).hasSize(1);
            assertThat(repository.findByEventType(InventoryEvent.EventType.STOCK_UPDATED)).hasSize(1);
            assertThat(repository.findByEventType(InventoryEvent.EventType.ITEM_DELETED)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tests de persistencia y recarga")
    class PersistenceAndReloadTests {

        @Test
        @DisplayName("Debe recargar eventos después de reinicializar")
        void shouldReloadEventsAfterReinitialization() throws IOException {
            // Arrange
            repository.save(createTestEvent("EVENT-001", "AGG-001"));
            repository.save(createTestEvent("EVENT-002", "AGG-002"));

            // Act - Crear nueva instancia del repositorio
            CsvEventRepository newRepository = new CsvEventRepository();
            ReflectionTestUtils.setField(newRepository, "storagePath", tempDir.toString());
            ReflectionTestUtils.setField(newRepository, "filename", testFilename);
            newRepository.init();

            // Assert
            List<InventoryEvent> events = newRepository.findByAggregateId("AGG-001");
            assertThat(events).hasSize(1);
            assertThat(events.get(0).getEventId()).isEqualTo("EVENT-001");
        }

        @Test
        @DisplayName("Debe mantener el orden de los eventos")
        void shouldMaintainEventOrder() throws IOException {
            // Arrange
            for (int i = 1; i <= 5; i++) {
                repository.save(createTestEvent("EVENT-00" + i, "AGG-001"));
            }

            // Act
            CsvEventRepository newRepository = new CsvEventRepository();
            ReflectionTestUtils.setField(newRepository, "storagePath", tempDir.toString());
            ReflectionTestUtils.setField(newRepository, "filename", testFilename);
            newRepository.init();

            List<InventoryEvent> events = newRepository.findByAggregateId("AGG-001");

            // Assert
            assertThat(events).hasSize(5);
            assertThat(events).extracting(InventoryEvent::getEventId)
                    .containsExactly("EVENT-001", "EVENT-002", "EVENT-003", "EVENT-004", "EVENT-005");
        }
    }

    @Nested
    @DisplayName("Tests de thread-safety")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Debe manejar escrituras concurrentes correctamente")
        void shouldHandleConcurrentWritesCorrectly() throws InterruptedException {
            // Arrange
            int threadCount = 10;
            int eventsPerThread = 10;
            Thread[] threads = new Thread[threadCount];

            // Act
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < eventsPerThread; j++) {
                        String eventId = "EVENT-T" + threadId + "-" + j;
                        repository.save(createTestEvent(eventId, "AGG-" + threadId));
                    }
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // Assert
            int totalEvents = 0;
            for (int i = 0; i < threadCount; i++) {
                totalEvents += repository.findByAggregateId("AGG-" + i).size();
            }
            assertThat(totalEvents).isEqualTo(threadCount * eventsPerThread);
        }
    }

    @Nested
    @DisplayName("Tests de manejo de errores")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Debe manejar payload con caracteres especiales")
        void shouldHandlePayloadWithSpecialCharacters() {
            // Arrange
            Map<String, Object> payload = new HashMap<>();
            payload.put("description", "Product with \"quotes\" and, commas");
            payload.put("note", "Line1\nLine2");

            InventoryEvent event = InventoryEvent.builder()
                    .eventId("EVENT-001")
                    .eventType(InventoryEvent.EventType.ITEM_CREATED)
                    .aggregateId("AGG-001")
                    .storeId("STORE-001")
                    .timestamp(LocalDateTime.now())
                    .userId("USER-001")
                    .payload(payload)
                    .version(1L)
                    .build();

            // Act
            repository.save(event);

            // Assert
            List<InventoryEvent> events = repository.findByAggregateId("AGG-001");
            assertThat(events).hasSize(1);
            assertThat(events.get(0).getPayload()).containsKeys("description", "note");
        }
    }

    // Helper methods
    private InventoryEvent createTestEvent(String eventId, String aggregateId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("test", "value");

        return InventoryEvent.builder()
                .eventId(eventId)
                .eventType(InventoryEvent.EventType.ITEM_CREATED)
                .aggregateId(aggregateId)
                .storeId("STORE-001")
                .timestamp(LocalDateTime.now())
                .userId("USER-001")
                .payload(payload)
                .version(1L)
                .build();
    }

    private InventoryEvent createTestEventWithStore(String eventId, String storeId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("test", "value");

        return InventoryEvent.builder()
                .eventId(eventId)
                .eventType(InventoryEvent.EventType.ITEM_CREATED)
                .aggregateId("AGG-001")
                .storeId(storeId)
                .timestamp(LocalDateTime.now())
                .userId("USER-001")
                .payload(payload)
                .version(1L)
                .build();
    }

    private InventoryEvent createTestEventWithType(String eventId, InventoryEvent.EventType eventType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("test", "value");

        return InventoryEvent.builder()
                .eventId(eventId)
                .eventType(eventType)
                .aggregateId("AGG-001")
                .storeId("STORE-001")
                .timestamp(LocalDateTime.now())
                .userId("USER-001")
                .payload(payload)
                .version(1L)
                .build();
    }
}