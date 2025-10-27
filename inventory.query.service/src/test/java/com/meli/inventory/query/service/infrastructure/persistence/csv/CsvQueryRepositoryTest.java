package com.meli.inventory.query.service.infrastructure.persistence.csv;

import com.meli.inventory.query.service.domain.model.InventoryProjection;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CsvQueryRepository - Unit Tests")
class CsvQueryRepositoryTest {

    @TempDir
    Path tempDir;

    private CsvQueryRepository repository;
    private String testFilename;

    @BeforeEach
    void setUp() throws IOException {
        repository = new CsvQueryRepository();
        testFilename = "test-inventory-query.csv";

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
            CsvQueryRepository newRepository = new CsvQueryRepository();
            ReflectionTestUtils.setField(newRepository, "storagePath", newTempDir.toString());
            ReflectionTestUtils.setField(newRepository, "filename", "inventory.csv");

            // Act
            newRepository.init();

            // Assert
            assertThat(Files.exists(newTempDir)).isTrue();
            assertThat(Files.isDirectory(newTempDir)).isTrue();
        }
    }

    @Nested
    @DisplayName("Tests de findById()")
    class FindByIdTests {

        @Test
        @DisplayName("Debe encontrar projection por ID")
        void shouldFindProjectionById() {
            // Arrange
            InventoryProjection projection = createTestProjection("ITEM-001", "STORE-001", "PROD-001");
            repository.save(projection);

            // Act
            Optional<InventoryProjection> found = repository.findById("ITEM-001");

            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo("ITEM-001");
            assertThat(found.get().getProductId()).isEqualTo("PROD-001");
        }

        @Test
        @DisplayName("Debe retornar Optional.empty() si no encuentra el projection")
        void shouldReturnEmptyWhenProjectionNotFound() {
            // Act
            Optional<InventoryProjection> found = repository.findById("NON-EXISTENT");

            // Assert
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tests de findByProductIdAndStoreId()")
    class FindByProductIdAndStoreIdTests {

        @Test
        @DisplayName("Debe encontrar projection por productId y storeId")
        void shouldFindProjectionByProductIdAndStoreId() {
            // Arrange
            repository.save(createTestProjection("ITEM-001", "STORE-001", "PROD-001"));
            repository.save(createTestProjection("ITEM-002", "STORE-002", "PROD-001"));

            // Act
            Optional<InventoryProjection> found = repository.findByProductIdAndStoreId("PROD-001", "STORE-001");

            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo("ITEM-001");
            assertThat(found.get().getStoreId()).isEqualTo("STORE-001");
        }

        @Test
        @DisplayName("Debe retornar empty si no encuentra combinación")
        void shouldReturnEmptyWhenCombinationNotFound() {
            // Arrange
            repository.save(createTestProjection("ITEM-001", "STORE-001", "PROD-001"));

            // Act
            Optional<InventoryProjection> found = repository.findByProductIdAndStoreId("PROD-999", "STORE-001");

            // Assert
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Debe retornar empty si store no existe")
        void shouldReturnEmptyWhenStoreNotExists() {
            // Act
            Optional<InventoryProjection> found = repository.findByProductIdAndStoreId("PROD-001", "STORE-999");

            // Assert
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tests de findByStoreId()")
    class FindByStoreIdTests {

        @Test
        @DisplayName("Debe encontrar todos los projections de una tienda")
        void shouldFindAllProjectionsForStore() {
            // Arrange
            repository.save(createTestProjection("ITEM-001", "STORE-001", "PROD-001"));
            repository.save(createTestProjection("ITEM-002", "STORE-001", "PROD-002"));
            repository.save(createTestProjection("ITEM-003", "STORE-002", "PROD-003"));

            // Act
            List<InventoryProjection> items = repository.findByStoreId("STORE-001");

            // Assert
            assertThat(items).hasSize(2);
            assertThat(items).extracting(InventoryProjection::getStoreId)
                    .containsOnly("STORE-001");
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no encuentra items para la tienda")
        void shouldReturnEmptyListWhenNoItemsForStore() {
            // Act
            List<InventoryProjection> items = repository.findByStoreId("NON-EXISTENT-STORE");

            // Assert
            assertThat(items).isEmpty();
        }

        @Test
        @DisplayName("Debe retornar página de projections con paginación")
        void shouldReturnPageOfProjectionsWithPagination() {
            // Arrange
            for (int i = 1; i <= 15; i++) {
                repository.save(createTestProjection("ITEM-00" + i, "STORE-001", "PROD-00" + i));
            }

            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<InventoryProjection> page = repository.findByStoreId("STORE-001", pageable);

            // Assert
            assertThat(page.getContent()).hasSize(10);
            assertThat(page.getTotalElements()).isEqualTo(15);
            assertThat(page.getTotalPages()).isEqualTo(2);
        }

        @Test
        @DisplayName("Debe retornar página vacía cuando offset es mayor que total")
        void shouldReturnEmptyPageWhenOffsetGreaterThanTotal() {
            // Arrange
            repository.save(createTestProjection("ITEM-001", "STORE-001", "PROD-001"));
            Pageable pageable = PageRequest.of(5, 10);

            // Act
            Page<InventoryProjection> page = repository.findByStoreId("STORE-001", pageable);

            // Assert
            assertThat(page.getContent()).isEmpty();
            assertThat(page.getTotalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Tests de findByStoreIdAndCategory()")
    class FindByStoreIdAndCategoryTests {

        @Test
        @DisplayName("Debe encontrar items por tienda y categoría")
        void shouldFindItemsByStoreAndCategory() {
            // Arrange
            InventoryProjection item1 = createTestProjection("ITEM-001", "STORE-001", "PROD-001");
            item1.setCategory("Electronics");
            repository.save(item1);

            InventoryProjection item2 = createTestProjection("ITEM-002", "STORE-001", "PROD-002");
            item2.setCategory("Books");
            repository.save(item2);

            InventoryProjection item3 = createTestProjection("ITEM-003", "STORE-001", "PROD-003");
            item3.setCategory("Electronics");
            repository.save(item3);

            // Act
            List<InventoryProjection> items = repository.findByStoreIdAndCategory("STORE-001", "Electronics");

            // Assert
            assertThat(items).hasSize(2);
            assertThat(items).extracting(InventoryProjection::getCategory)
                    .containsOnly("Electronics");
        }

        @Test
        @DisplayName("Debe ser case-insensitive en búsqueda por categoría")
        void shouldBeCaseInsensitiveForCategorySearch() {
            // Arrange
            InventoryProjection item = createTestProjection("ITEM-001", "STORE-001", "PROD-001");
            item.setCategory("Electronics");
            repository.save(item);

            // Act
            List<InventoryProjection> items = repository.findByStoreIdAndCategory("STORE-001", "electronics");

            // Assert
            assertThat(items).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Tests de findLowStockItems()")
    class FindLowStockItemsTests {

        @Test
        @DisplayName("Debe encontrar items con stock bajo")
        void shouldFindLowStockItems() {
            // Arrange
            InventoryProjection lowStock = createTestProjection("ITEM-001", "STORE-001", "PROD-001");
            lowStock.setQuantity(5);
            lowStock.setReservedQuantity(0);
            lowStock.setMinThreshold(10);
            lowStock.calculateDerivedFields();
            repository.save(lowStock);

            InventoryProjection normalStock = createTestProjection("ITEM-002", "STORE-001", "PROD-002");
            normalStock.setQuantity(100);
            normalStock.setReservedQuantity(0);
            normalStock.setMinThreshold(10);
            normalStock.calculateDerivedFields();
            repository.save(normalStock);

            // Act
            List<InventoryProjection> items = repository.findLowStockItems("STORE-001");

            // Assert
            assertThat(items).hasSize(1);
            assertThat(items.get(0).getId()).isEqualTo("ITEM-001");
            assertThat(items.get(0).getBelowThreshold()).isTrue();
        }

        @Test
        @DisplayName("Debe excluir items sin stock de low stock")
        void shouldExcludeOutOfStockItemsFromLowStock() {
            // Arrange
            InventoryProjection outOfStock = createTestProjection("ITEM-001", "STORE-001", "PROD-001");
            outOfStock.setQuantity(0);
            outOfStock.setReservedQuantity(0);
            outOfStock.setMinThreshold(10);
            outOfStock.calculateDerivedFields();
            repository.save(outOfStock);

            // Act
            List<InventoryProjection> items = repository.findLowStockItems("STORE-001");

            // Assert
            assertThat(items).isEmpty();
        }

        @Test
        @DisplayName("Debe ordenar items por availableStock ascendente")
        void shouldSortItemsByAvailableStockAscending() {
            // Arrange
            InventoryProjection item1 = createTestProjection("ITEM-001", "STORE-001", "PROD-001");
            item1.setQuantity(8);
            item1.setReservedQuantity(0);
            item1.setMinThreshold(10);
            item1.calculateDerivedFields();
            repository.save(item1);

            InventoryProjection item2 = createTestProjection("ITEM-002", "STORE-001", "PROD-002");
            item2.setQuantity(3);
            item2.setReservedQuantity(0);
            item2.setMinThreshold(10);
            item2.calculateDerivedFields();
            repository.save(item2);

            // Act
            List<InventoryProjection> items = repository.findLowStockItems("STORE-001");

            // Assert
            assertThat(items).hasSize(2);
            assertThat(items.get(0).getAvailableStock()).isLessThan(items.get(1).getAvailableStock());
        }
    }

    @Nested
    @DisplayName("Tests de findOutOfStockItems()")
    class FindOutOfStockItemsTests {

        @Test
        @DisplayName("Debe encontrar items sin stock")
        void shouldFindOutOfStockItems() {
            // Arrange
            InventoryProjection outOfStock = createTestProjection("ITEM-001", "STORE-001", "PROD-001");
            outOfStock.setQuantity(0);
            outOfStock.setReservedQuantity(0);
            outOfStock.calculateDerivedFields();
            repository.save(outOfStock);

            InventoryProjection inStock = createTestProjection("ITEM-002", "STORE-001", "PROD-002");
            inStock.setQuantity(100);
            inStock.setReservedQuantity(0);
            inStock.calculateDerivedFields();
            repository.save(inStock);

            // Act
            List<InventoryProjection> items = repository.findOutOfStockItems("STORE-001");

            // Assert
            assertThat(items).hasSize(1);
            assertThat(items.get(0).getId()).isEqualTo("ITEM-001");
            assertThat(items.get(0).getAvailableStock()).isEqualTo(0);
        }

        @Test
        @DisplayName("Debe incluir items con availableStock negativo")
        void shouldIncludeItemsWithNegativeAvailableStock() {
            // Arrange
            InventoryProjection item = createTestProjection("ITEM-001", "STORE-001", "PROD-001");
            item.setQuantity(10);
            item.setReservedQuantity(20);
            item.calculateDerivedFields();
            repository.save(item);

            // Act
            List<InventoryProjection> items = repository.findOutOfStockItems("STORE-001");

            // Assert
            assertThat(items).hasSize(1);
            assertThat(items.get(0).getAvailableStock()).isLessThan(0);
        }
    }

    @Nested
    @DisplayName("Tests de findAvailableItems()")
    class FindAvailableItemsTests {

        @Test
        @DisplayName("Debe encontrar items disponibles para venta")
        void shouldFindAvailableItems() {
            // Arrange
            InventoryProjection available = createTestProjection("ITEM-001", "STORE-001", "PROD-001");
            available.setQuantity(100);
            available.setReservedQuantity(20);
            available.setStatus(InventoryProjection.ItemStatus.ACTIVE);
            available.calculateDerivedFields();
            repository.save(available);

            InventoryProjection unavailable = createTestProjection("ITEM-002", "STORE-001", "PROD-002");
            unavailable.setQuantity(0);
            unavailable.setReservedQuantity(0);
            unavailable.setStatus(InventoryProjection.ItemStatus.OUT_OF_STOCK);
            unavailable.calculateDerivedFields();
            repository.save(unavailable);

            // Act
            List<InventoryProjection> items = repository.findAvailableItems("STORE-001");

            // Assert
            assertThat(items).hasSize(1);
            assertThat(items.get(0).getId()).isEqualTo("ITEM-001");
        }
    }

    @Nested
    @DisplayName("Tests de searchByProductName()")
    class SearchByProductNameTests {

        @Test
        @DisplayName("Debe buscar por nombre de producto")
        void shouldSearchByProductName() {
            // Arrange
            InventoryProjection item1 = createTestProjection("ITEM-001", "STORE-001", "PROD-001");
            item1.setProductName("Laptop Dell");
            repository.save(item1);

            InventoryProjection item2 = createTestProjection("ITEM-002", "STORE-001", "PROD-002");
            item2.setProductName("Mouse Logitech");
            repository.save(item2);

            // Act
            List<InventoryProjection> items = repository.searchByProductName("STORE-001", "laptop");

            // Assert
            assertThat(items).hasSize(1);
            assertThat(items.get(0).getProductName()).contains("Laptop");
        }

        @Test
        @DisplayName("Debe buscar de forma case-insensitive")
        void shouldSearchCaseInsensitive() {
            // Arrange
            InventoryProjection item = createTestProjection("ITEM-001", "STORE-001", "PROD-001");
            item.setProductName("Laptop Dell");
            repository.save(item);

            // Act
            List<InventoryProjection> items = repository.searchByProductName("STORE-001", "LAPTOP");

            // Assert
            assertThat(items).hasSize(1);
        }

        @Test
        @DisplayName("Debe buscar por coincidencia parcial")
        void shouldSearchByPartialMatch() {
            // Arrange
            InventoryProjection item = createTestProjection("ITEM-001", "STORE-001", "PROD-001");
            item.setProductName("Laptop Dell Inspiron");
            repository.save(item);

            // Act
            List<InventoryProjection> items = repository.searchByProductName("STORE-001", "dell");

            // Assert
            assertThat(items).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Tests de findAll()")
    class FindAllTests {

        @Test
        @DisplayName("Debe retornar todos los projections con paginación")
        void shouldReturnAllProjectionsWithPagination() {
            // Arrange
            for (int i = 1; i <= 25; i++) {
                repository.save(createTestProjection("ITEM-00" + i, "STORE-001", "PROD-00" + i));
            }

            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<InventoryProjection> page = repository.findAll(pageable);

            // Assert
            assertThat(page.getContent()).hasSize(10);
            assertThat(page.getTotalElements()).isEqualTo(25);
            assertThat(page.getTotalPages()).isEqualTo(3);
        }

        @Test
        @DisplayName("Debe retornar página vacía cuando no hay items")
        void shouldReturnEmptyPageWhenNoItems() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<InventoryProjection> page = repository.findAll(pageable);

            // Assert
            assertThat(page.getContent()).isEmpty();
            assertThat(page.getTotalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Tests de countByStoreId()")
    class CountByStoreIdTests {

        @Test
        @DisplayName("Debe contar items por tienda")
        void shouldCountItemsByStore() {
            // Arrange
            repository.save(createTestProjection("ITEM-001", "STORE-001", "PROD-001"));
            repository.save(createTestProjection("ITEM-002", "STORE-001", "PROD-002"));
            repository.save(createTestProjection("ITEM-003", "STORE-002", "PROD-003"));

            // Act
            long count = repository.countByStoreId("STORE-001");

            // Assert
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Debe retornar 0 para tienda sin items")
        void shouldReturn0ForStoreWithNoItems() {
            // Act
            long count = repository.countByStoreId("NON-EXISTENT");

            // Assert
            assertThat(count).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Tests de save()")
    class SaveTests {

        @Test
        @DisplayName("Debe guardar projection correctamente")
        void shouldSaveProjectionCorrectly() {
            // Arrange
            InventoryProjection projection = createTestProjection("ITEM-001", "STORE-001", "PROD-001");

            // Act
            InventoryProjection saved = repository.save(projection);

            // Assert
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isEqualTo("ITEM-001");

            Optional<InventoryProjection> found = repository.findById("ITEM-001");
            assertThat(found).isPresent();
        }

        @Test
        @DisplayName("Debe calcular campos derivados al guardar")
        void shouldCalculateDerivedFieldsOnSave() {
            // Arrange
            InventoryProjection projection = createTestProjection("ITEM-001", "STORE-001", "PROD-001");
            projection.setQuantity(100);
            projection.setReservedQuantity(20);
            projection.setMinThreshold(10);

            // Act
            InventoryProjection saved = repository.save(projection);

            // Assert
            assertThat(saved.getAvailableStock()).isEqualTo(80);
            assertThat(saved.getBelowThreshold()).isFalse();
            assertThat(saved.getStatus()).isEqualTo(InventoryProjection.ItemStatus.ACTIVE);
        }

        @Test
        @DisplayName("Debe actualizar projection existente")
        void shouldUpdateExistingProjection() {
            // Arrange
            InventoryProjection projection = createTestProjection("ITEM-001", "STORE-001", "PROD-001");
            repository.save(projection);

            // Act
            projection.setQuantity(200);
            InventoryProjection updated = repository.save(projection);

            // Assert
            Optional<InventoryProjection> found = repository.findById("ITEM-001");
            assertThat(found).isPresent();
            assertThat(found.get().getQuantity()).isEqualTo(200);
        }

        @Test
        @DisplayName("Debe persistir en archivo CSV")
        void shouldPersistToCsvFile() throws IOException {
            // Arrange
            InventoryProjection projection = createTestProjection("ITEM-001", "STORE-001", "PROD-001");

            // Act
            repository.save(projection);

            // Assert
            Path filePath = tempDir.resolve(testFilename);
            List<String> lines = Files.readAllLines(filePath);
            assertThat(lines).hasSizeGreaterThan(1);
            assertThat(lines.get(1)).contains("ITEM-001", "STORE-001", "PROD-001");
        }
    }

    @Nested
    @DisplayName("Tests de deleteById()")
    class DeleteByIdTests {

        @Test
        @DisplayName("Debe eliminar projection correctamente")
        void shouldDeleteProjectionCorrectly() {
            // Arrange
            InventoryProjection projection = createTestProjection("ITEM-001", "STORE-001", "PROD-001");
            repository.save(projection);
            assertThat(repository.findById("ITEM-001")).isPresent();

            // Act
            repository.deleteById("ITEM-001");

            // Assert
            assertThat(repository.findById("ITEM-001")).isEmpty();
        }

        @Test
        @DisplayName("Debe actualizar índice de tienda al eliminar")
        void shouldUpdateStoreIndexOnDelete() {
            // Arrange
            repository.save(createTestProjection("ITEM-001", "STORE-001", "PROD-001"));
            repository.save(createTestProjection("ITEM-002", "STORE-001", "PROD-002"));

            // Act
            repository.deleteById("ITEM-001");

            // Assert
            List<InventoryProjection> items = repository.findByStoreId("STORE-001");
            assertThat(items).hasSize(1);
            assertThat(items.get(0).getId()).isEqualTo("ITEM-002");
        }

        @Test
        @DisplayName("No debe lanzar excepción al eliminar item inexistente")
        void shouldNotThrowExceptionWhenDeletingNonExistentItem() {
            // Act & Assert
            assertThatNoException().isThrownBy(() -> repository.deleteById("NON-EXISTENT"));
        }
    }

    @Nested
    @DisplayName("Tests de persistencia")
    class PersistenceTests {

        @Test
        @DisplayName("Debe recargar datos después de reinicializar")
        void shouldReloadDataAfterReinitialization() throws IOException {
            // Arrange
            repository.save(createTestProjection("ITEM-001", "STORE-001", "PROD-001"));
            repository.save(createTestProjection("ITEM-002", "STORE-002", "PROD-002"));

            // Act - Nueva instancia
            CsvQueryRepository newRepository = new CsvQueryRepository();
            ReflectionTestUtils.setField(newRepository, "storagePath", tempDir.toString());
            ReflectionTestUtils.setField(newRepository, "filename", testFilename);
            newRepository.init();

            // Assert
            assertThat(newRepository.findById("ITEM-001")).isPresent();
            assertThat(newRepository.findById("ITEM-002")).isPresent();
        }

        @Test
        @DisplayName("Debe mantener todos los campos al persistir y recargar")
        void shouldMaintainAllFieldsOnPersistAndReload() throws IOException {
            // Arrange
            InventoryProjection original = createTestProjection("ITEM-001", "STORE-001", "PROD-001");
            original.setStoreName("Store Central");
            original.setCategory("Electronics");
            original.setQuantity(100);
            original.setReservedQuantity(20);
            original.setUnitPrice(99.99);
            original.calculateDerivedFields();
            repository.save(original);

            // Act
            CsvQueryRepository newRepository = new CsvQueryRepository();
            ReflectionTestUtils.setField(newRepository, "storagePath", tempDir.toString());
            ReflectionTestUtils.setField(newRepository, "filename", testFilename);
            newRepository.init();

            // Assert
            Optional<InventoryProjection> loaded = newRepository.findById("ITEM-001");
            assertThat(loaded).isPresent();
            assertThat(loaded.get().getStoreName()).isEqualTo("Store Central");
            assertThat(loaded.get().getCategory()).isEqualTo("Electronics");
            assertThat(loaded.get().getQuantity()).isEqualTo(100);
            assertThat(loaded.get().getReservedQuantity()).isEqualTo(20);
            assertThat(loaded.get().getUnitPrice()).isEqualTo(99.99);
        }
    }

    // Helper method
    private InventoryProjection createTestProjection(String id, String storeId, String productId) {
        return InventoryProjection.builder()
                .id(id)
                .storeId(storeId)
                .storeName("Store Central")
                .productId(productId)
                .productName("Test Product")
                .category("General")
                .quantity(100)
                .reservedQuantity(0)
                .availableStock(100)
                .minThreshold(10)
                .belowThreshold(false)
                .version(1L)
                .lastUpdated(LocalDateTime.now())
                .lastModifiedBy("USER-001")
                .status(InventoryProjection.ItemStatus.ACTIVE)
                .unitPrice(99.99)
                .build();
    }
}