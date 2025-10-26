package com.meli.inventory.command.service.infrastructure.persistence.csv;

import com.meli.inventory.command.service.domain.model.InventoryItem;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CsvInventoryRepository - Unit Tests")
class CsvInventoryRepositoryTest {

    @TempDir
    Path tempDir;

    private CsvInventoryRepository repository;
    private String testFilename;

    @BeforeEach
    void setUp() throws IOException {
        repository = new CsvInventoryRepository();
        testFilename = "test-inventory.csv";

        ReflectionTestUtils.setField(repository, "storagePath", tempDir.toString());
        ReflectionTestUtils.setField(repository, "filename", testFilename);

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
                assertThat(lines.get(0)).contains("id", "storeId", "productId", "quantity");
            } catch (IOException e) {
                fail("Failed to read file", e);
            }
        }

        @Test
        @DisplayName("Debe crear directorio si no existe")
        void shouldCreateDirectoryIfNotExists() throws IOException {
            // Arrange
            Path newTempDir = tempDir.resolve("new-directory");
            CsvInventoryRepository newRepository = new CsvInventoryRepository();
            ReflectionTestUtils.setField(newRepository, "storagePath", newTempDir.toString());
            ReflectionTestUtils.setField(newRepository, "filename", "inventory.csv");

            // Act
            newRepository.init();

            // Assert
            assertThat(Files.exists(newTempDir)).isTrue();
            assertThat(Files.isDirectory(newTempDir)).isTrue();
        }

        @Test
        @DisplayName("Debe cargar datos existentes al inicializar")
        void shouldLoadExistingDataOnInit() throws IOException {
            // Arrange
            InventoryItem item = createTestItem("ITEM-001", "STORE-001", "PROD-001");
            repository.save(item);

            // Act - Crear nueva instancia
            CsvInventoryRepository newRepository = new CsvInventoryRepository();
            ReflectionTestUtils.setField(newRepository, "storagePath", tempDir.toString());
            ReflectionTestUtils.setField(newRepository, "filename", testFilename);
            newRepository.init();

            // Assert
            Optional<InventoryItem> loaded = newRepository.findById("ITEM-001");
            assertThat(loaded).isPresent();
            assertThat(loaded.get().getProductId()).isEqualTo("PROD-001");
        }
    }

    @Nested
    @DisplayName("Tests de save()")
    class SaveTests {

        @Test
        @DisplayName("Debe guardar item correctamente")
        void shouldSaveItemSuccessfully() {
            // Arrange
            InventoryItem item = createTestItem("ITEM-001", "STORE-001", "PROD-001");

            // Act
            InventoryItem saved = repository.save(item);

            // Assert
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isEqualTo("ITEM-001");

            Optional<InventoryItem> found = repository.findById("ITEM-001");
            assertThat(found).isPresent();
            assertThat(found.get().getProductId()).isEqualTo("PROD-001");
        }

        @Test
        @DisplayName("Debe actualizar item existente")
        void shouldUpdateExistingItem() {
            // Arrange
            InventoryItem item = createTestItem("ITEM-001", "STORE-001", "PROD-001");
            repository.save(item);

            // Act - Actualizar cantidad
            item.updateQuantity(200, "USER-002");
            InventoryItem updated = repository.save(item);

            // Assert
            Optional<InventoryItem> found = repository.findById("ITEM-001");
            assertThat(found).isPresent();
            assertThat(found.get().getQuantity()).isEqualTo(200);
            assertThat(found.get().getVersion()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Debe persistir item en archivo CSV")
        void shouldPersistItemToCsvFile() throws IOException {
            // Arrange
            InventoryItem item = createTestItem("ITEM-001", "STORE-001", "PROD-001");

            // Act
            repository.save(item);

            // Assert
            Path filePath = tempDir.resolve(testFilename);
            List<String> lines = Files.readAllLines(filePath);
            assertThat(lines).hasSizeGreaterThan(1); // Header + item
            assertThat(lines.get(1)).contains("ITEM-001", "STORE-001", "PROD-001");
        }

        @Test
        @DisplayName("Debe guardar múltiples items")
        void shouldSaveMultipleItems() {
            // Arrange & Act
            repository.save(createTestItem("ITEM-001", "STORE-001", "PROD-001"));
            repository.save(createTestItem("ITEM-002", "STORE-001", "PROD-002"));
            repository.save(createTestItem("ITEM-003", "STORE-002", "PROD-003"));

            // Assert
            List<InventoryItem> all = repository.findAll();
            assertThat(all).hasSize(3);
        }

        @Test
        @DisplayName("Debe mantener integridad del caché")
        void shouldMaintainCacheIntegrity() {
            // Arrange
            InventoryItem item = createTestItem("ITEM-001", "STORE-001", "PROD-001");
            repository.save(item);

            // Act - Modificar objeto original
            item.updateQuantity(500, "USER-002");

            // Assert - El caché debe tener el valor actualizado
            Optional<InventoryItem> cached = repository.findById("ITEM-001");
            assertThat(cached).isPresent();
            assertThat(cached.get().getQuantity()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("Tests de findById()")
    class FindByIdTests {

        @Test
        @DisplayName("Debe encontrar item por ID")
        void shouldFindItemById() {
            // Arrange
            InventoryItem item = createTestItem("ITEM-001", "STORE-001", "PROD-001");
            repository.save(item);

            // Act
            Optional<InventoryItem> found = repository.findById("ITEM-001");

            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo("ITEM-001");
            assertThat(found.get().getProductId()).isEqualTo("PROD-001");
        }

        @Test
        @DisplayName("Debe retornar Optional.empty() si no encuentra el item")
        void shouldReturnEmptyWhenItemNotFound() {
            // Act
            Optional<InventoryItem> found = repository.findById("NON-EXISTENT");

            // Assert
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Debe leer desde caché sin acceder al archivo")
        void shouldReadFromCacheWithoutFileAccess() {
            // Arrange
            InventoryItem item = createTestItem("ITEM-001", "STORE-001", "PROD-001");
            repository.save(item);

            // Act - Múltiples lecturas deben ser rápidas
            for (int i = 0; i < 100; i++) {
                Optional<InventoryItem> found = repository.findById("ITEM-001");
                assertThat(found).isPresent();
            }
        }
    }

    @Nested
    @DisplayName("Tests de findByProductIdAndStoreId()")
    class FindByProductIdAndStoreIdTests {

        @Test
        @DisplayName("Debe encontrar item por productId y storeId")
        void shouldFindItemByProductIdAndStoreId() {
            // Arrange
            repository.save(createTestItem("ITEM-001", "STORE-001", "PROD-001"));
            repository.save(createTestItem("ITEM-002", "STORE-002", "PROD-001"));

            // Act
            Optional<InventoryItem> found = repository.findByProductIdAndStoreId("PROD-001", "STORE-001");

            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo("ITEM-001");
            assertThat(found.get().getStoreId()).isEqualTo("STORE-001");
        }

        @Test
        @DisplayName("Debe retornar empty si no encuentra combinación")
        void shouldReturnEmptyWhenCombinationNotFound() {
            // Arrange
            repository.save(createTestItem("ITEM-001", "STORE-001", "PROD-001"));

            // Act
            Optional<InventoryItem> found = repository.findByProductIdAndStoreId("PROD-001", "STORE-999");

            // Assert
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Debe diferenciar entre tiendas diferentes")
        void shouldDifferentiateBetweenDifferentStores() {
            // Arrange
            repository.save(createTestItem("ITEM-001", "STORE-001", "PROD-001"));
            repository.save(createTestItem("ITEM-002", "STORE-002", "PROD-001"));

            // Act & Assert
            Optional<InventoryItem> store1 = repository.findByProductIdAndStoreId("PROD-001", "STORE-001");
            Optional<InventoryItem> store2 = repository.findByProductIdAndStoreId("PROD-001", "STORE-002");

            assertThat(store1).isPresent();
            assertThat(store2).isPresent();
            assertThat(store1.get().getId()).isNotEqualTo(store2.get().getId());
        }
    }

    @Nested
    @DisplayName("Tests de findByStoreId()")
    class FindByStoreIdTests {

        @Test
        @DisplayName("Debe encontrar todos los items de una tienda")
        void shouldFindAllItemsByStoreId() {
            // Arrange
            repository.save(createTestItem("ITEM-001", "STORE-001", "PROD-001"));
            repository.save(createTestItem("ITEM-002", "STORE-001", "PROD-002"));
            repository.save(createTestItem("ITEM-003", "STORE-002", "PROD-003"));

            // Act
            List<InventoryItem> items = repository.findByStoreId("STORE-001");

            // Assert
            assertThat(items).hasSize(2);
            assertThat(items).extracting(InventoryItem::getStoreId)
                    .containsOnly("STORE-001");
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no encuentra items para la tienda")
        void shouldReturnEmptyListWhenNoItemsForStore() {
            // Act
            List<InventoryItem> items = repository.findByStoreId("NON-EXISTENT-STORE");

            // Assert
            assertThat(items).isEmpty();
        }

        @Test
        @DisplayName("Debe retornar todos los items de la tienda correcta")
        void shouldReturnAllItemsForCorrectStore() {
            // Arrange
            for (int i = 0; i < 5; i++) {
                repository.save(createTestItem("ITEM-00" + i, "STORE-001", "PROD-00" + i));
            }
            repository.save(createTestItem("ITEM-999", "STORE-002", "PROD-999"));

            // Act
            List<InventoryItem> items = repository.findByStoreId("STORE-001");

            // Assert
            assertThat(items).hasSize(5);
        }
    }

    @Nested
    @DisplayName("Tests de findAll()")
    class FindAllTests {

        @Test
        @DisplayName("Debe retornar todos los items")
        void shouldReturnAllItems() {
            // Arrange
            repository.save(createTestItem("ITEM-001", "STORE-001", "PROD-001"));
            repository.save(createTestItem("ITEM-002", "STORE-002", "PROD-002"));
            repository.save(createTestItem("ITEM-003", "STORE-003", "PROD-003"));

            // Act
            List<InventoryItem> all = repository.findAll();

            // Assert
            assertThat(all).hasSize(3);
            assertThat(all).extracting(InventoryItem::getId)
                    .containsExactlyInAnyOrder("ITEM-001", "ITEM-002", "ITEM-003");
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay items")
        void shouldReturnEmptyListWhenNoItems() {
            // Act
            List<InventoryItem> all = repository.findAll();

            // Assert
            assertThat(all).isEmpty();
        }

        @Test
        @DisplayName("Debe retornar nueva lista cada vez")
        void shouldReturnNewListEachTime() {
            // Arrange
            repository.save(createTestItem("ITEM-001", "STORE-001", "PROD-001"));

            // Act
            List<InventoryItem> list1 = repository.findAll();
            List<InventoryItem> list2 = repository.findAll();

            // Assert
            assertThat(list1).isNotSameAs(list2);
        }
    }

    @Nested
    @DisplayName("Tests de deleteById()")
    class DeleteByIdTests {

        @Test
        @DisplayName("Debe eliminar item correctamente")
        void shouldDeleteItemSuccessfully() {
            // Arrange
            repository.save(createTestItem("ITEM-001", "STORE-001", "PROD-001"));
            assertThat(repository.findById("ITEM-001")).isPresent();

            // Act
            repository.deleteById("ITEM-001");

            // Assert
            assertThat(repository.findById("ITEM-001")).isEmpty();
        }

        @Test
        @DisplayName("Debe persistir eliminación en archivo")
        void shouldPersistDeletionToFile() throws IOException {
            // Arrange
            repository.save(createTestItem("ITEM-001", "STORE-001", "PROD-001"));
            repository.save(createTestItem("ITEM-002", "STORE-001", "PROD-002"));

            // Act
            repository.deleteById("ITEM-001");

            // Assert - Recargar desde archivo
            CsvInventoryRepository newRepository = new CsvInventoryRepository();
            ReflectionTestUtils.setField(newRepository, "storagePath", tempDir.toString());
            ReflectionTestUtils.setField(newRepository, "filename", testFilename);
            newRepository.init();

            assertThat(newRepository.findById("ITEM-001")).isEmpty();
            assertThat(newRepository.findById("ITEM-002")).isPresent();
        }

        @Test
        @DisplayName("No debe lanzar excepción al eliminar item inexistente")
        void shouldNotThrowExceptionWhenDeletingNonExistentItem() {
            // Act & Assert
            assertThatNoException().isThrownBy(() -> repository.deleteById("NON-EXISTENT"));
        }

        @Test
        @DisplayName("Debe actualizar findAll después de eliminar")
        void shouldUpdateFindAllAfterDelete() {
            // Arrange
            repository.save(createTestItem("ITEM-001", "STORE-001", "PROD-001"));
            repository.save(createTestItem("ITEM-002", "STORE-001", "PROD-002"));
            assertThat(repository.findAll()).hasSize(2);

            // Act
            repository.deleteById("ITEM-001");

            // Assert
            assertThat(repository.findAll()).hasSize(1);
            assertThat(repository.findAll().get(0).getId()).isEqualTo("ITEM-002");
        }
    }

    @Nested
    @DisplayName("Tests de existsByProductIdAndStoreId()")
    class ExistsByProductIdAndStoreIdTests {

        @Test
        @DisplayName("Debe retornar true si existe el item")
        void shouldReturnTrueWhenItemExists() {
            // Arrange
            repository.save(createTestItem("ITEM-001", "STORE-001", "PROD-001"));

            // Act
            boolean exists = repository.existsByProductIdAndStoreId("PROD-001", "STORE-001");

            // Assert
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false si no existe el item")
        void shouldReturnFalseWhenItemDoesNotExist() {
            // Act
            boolean exists = repository.existsByProductIdAndStoreId("PROD-999", "STORE-999");

            // Assert
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Debe diferenciar entre diferentes combinaciones")
        void shouldDifferentiateBetweenDifferentCombinations() {
            // Arrange
            repository.save(createTestItem("ITEM-001", "STORE-001", "PROD-001"));

            // Act & Assert
            assertThat(repository.existsByProductIdAndStoreId("PROD-001", "STORE-001")).isTrue();
            assertThat(repository.existsByProductIdAndStoreId("PROD-001", "STORE-002")).isFalse();
            assertThat(repository.existsByProductIdAndStoreId("PROD-002", "STORE-001")).isFalse();
        }
    }

    @Nested
    @DisplayName("Tests de persistencia y recarga")
    class PersistenceAndReloadTests {

        @Test
        @DisplayName("Debe mantener datos después de reinicializar")
        void shouldMaintainDataAfterReinitialization() throws IOException {
            // Arrange
            repository.save(createTestItem("ITEM-001", "STORE-001", "PROD-001"));
            repository.save(createTestItem("ITEM-002", "STORE-002", "PROD-002"));

            // Act - Nueva instancia
            CsvInventoryRepository newRepository = new CsvInventoryRepository();
            ReflectionTestUtils.setField(newRepository, "storagePath", tempDir.toString());
            ReflectionTestUtils.setField(newRepository, "filename", testFilename);
            newRepository.init();

            // Assert
            assertThat(newRepository.findAll()).hasSize(2);
            assertThat(newRepository.findById("ITEM-001")).isPresent();
            assertThat(newRepository.findById("ITEM-002")).isPresent();
        }

        @Test
        @DisplayName("Debe mantener todos los atributos del item")
        void shouldMaintainAllItemAttributes() throws IOException {
            // Arrange
            InventoryItem original = createTestItem("ITEM-001", "STORE-001", "PROD-001");
            original.reserveStock(30, "USER-001");
            repository.save(original);

            // Act - Recargar
            CsvInventoryRepository newRepository = new CsvInventoryRepository();
            ReflectionTestUtils.setField(newRepository, "storagePath", tempDir.toString());
            ReflectionTestUtils.setField(newRepository, "filename", testFilename);
            newRepository.init();

            // Assert
            Optional<InventoryItem> loaded = newRepository.findById("ITEM-001");
            assertThat(loaded).isPresent();
            assertThat(loaded.get().getReservedQuantity()).isEqualTo(30);
            assertThat(loaded.get().getVersion()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("Tests de thread-safety")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Debe manejar operaciones concurrentes")
        void shouldHandleConcurrentOperations() throws InterruptedException {
            // Arrange
            int threadCount = 10;
            int operationsPerThread = 10;
            Thread[] threads = new Thread[threadCount];

            // Act
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String itemId = "ITEM-T" + threadId + "-" + j;
                        InventoryItem item = createTestItem(itemId, "STORE-" + threadId, "PROD-" + j);
                        repository.save(item);
                    }
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // Assert
            List<InventoryItem> all = repository.findAll();
            assertThat(all).hasSize(threadCount * operationsPerThread);
        }

        @Test
        @DisplayName("Debe manejar lecturas y escrituras concurrentes")
        void shouldHandleConcurrentReadsAndWrites() throws InterruptedException {
            // Arrange
            repository.save(createTestItem("ITEM-001", "STORE-001", "PROD-001"));

            Thread writer = new Thread(() -> {
                for (int i = 0; i < 50; i++) {
                    InventoryItem item = createTestItem("ITEM-W" + i, "STORE-001", "PROD-" + i);
                    repository.save(item);
                }
            });

            Thread reader = new Thread(() -> {
                for (int i = 0; i < 50; i++) {
                    repository.findById("ITEM-001");
                    repository.findAll();
                }
            });

            // Act
            writer.start();
            reader.start();
            writer.join();
            reader.join();

            // Assert - No debe haber excepciones
            assertThat(repository.findById("ITEM-001")).isPresent();
        }
    }

    // Helper method
    private InventoryItem createTestItem(String id, String storeId, String productId) {
        return InventoryItem.builder()
                .id(id)
                .storeId(storeId)
                .productId(productId)
                .productName("Test Product")
                .quantity(100)
                .reservedQuantity(0)
                .minThreshold(10)
                .version(1L)
                .lastUpdated(LocalDateTime.now())
                .lastModifiedBy("USER-001")
                .build();
    }
}
