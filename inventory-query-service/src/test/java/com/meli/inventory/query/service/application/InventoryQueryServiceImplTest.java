package com.meli.inventory.query.service.application;

import com.meli.inventory.query.service.domain.model.InventoryProjection;
import com.meli.inventory.query.service.domain.port.in.InventoryQueryService;
import com.meli.inventory.query.service.domain.port.out.InventoryQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryQueryServiceImpl - Unit Tests")
class InventoryQueryServiceImplTest {

    @Mock
    private InventoryQueryRepository repository;

    @InjectMocks
    private InventoryQueryServiceImpl service;

    private String testItemId;
    private String testStoreId;
    private String testProductId;

    @BeforeEach
    void setUp() {
        testItemId = "ITEM-001";
        testStoreId = "STORE-001";
        testProductId = "PROD-001";
    }

    @Nested
    @DisplayName("Tests de getById()")
    class GetByIdTests {

        @Test
        @DisplayName("Debe retornar item cuando existe")
        void shouldReturnItemWhenExists() {
            // Arrange
            InventoryProjection projection = createTestProjection(testItemId, testStoreId, testProductId);
            when(repository.findById(testItemId)).thenReturn(Optional.of(projection));

            // Act
            InventoryProjection result = service.getById(testItemId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testItemId);
            verify(repository).findById(testItemId);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando item no existe")
        void shouldThrowExceptionWhenItemNotFound() {
            // Arrange
            when(repository.findById(testItemId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> service.getById(testItemId))
                    .isInstanceOf(InventoryQueryServiceImpl.InventoryItemNotFoundException.class)
                    .hasMessageContaining("Inventory item not found");

            verify(repository).findById(testItemId);
        }

        @Test
        @DisplayName("Debe retornar item con todos sus campos")
        void shouldReturnItemWithAllFields() {
            // Arrange
            InventoryProjection projection = InventoryProjection.builder()
                    .id(testItemId)
                    .storeId(testStoreId)
                    .storeName("Store Central")
                    .productId(testProductId)
                    .productName("Test Product")
                    .category("Electronics")
                    .quantity(100)
                    .reservedQuantity(20)
                    .availableStock(80)
                    .minThreshold(10)
                    .version(1L)
                    .lastUpdated(LocalDateTime.now())
                    .lastModifiedBy("USER-001")
                    .build();

            when(repository.findById(testItemId)).thenReturn(Optional.of(projection));

            // Act
            InventoryProjection result = service.getById(testItemId);

            // Assert
            assertThat(result.getId()).isEqualTo(testItemId);
            assertThat(result.getStoreId()).isEqualTo(testStoreId);
            assertThat(result.getStoreName()).isEqualTo("Store Central");
            assertThat(result.getProductName()).isEqualTo("Test Product");
            assertThat(result.getQuantity()).isEqualTo(100);
            assertThat(result.getReservedQuantity()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("Tests de checkAvailability()")
    class CheckAvailabilityTests {

        @Test
        @DisplayName("Debe retornar información de disponibilidad cuando item existe")
        void shouldReturnAvailabilityInfoWhenItemExists() {
            // Arrange
            InventoryProjection projection = createTestProjection(testItemId, testStoreId, testProductId);
            projection.setQuantity(100);
            projection.setReservedQuantity(20);
            projection.setAvailableStock(80);
            projection.setStatus(InventoryProjection.ItemStatus.ACTIVE);

            when(repository.findByProductIdAndStoreId(testProductId, testStoreId))
                    .thenReturn(Optional.of(projection));

            // Act
            InventoryQueryService.AvailabilityInfo result =
                    service.checkAvailability(testProductId, testStoreId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.productId()).isEqualTo(testProductId);
            assertThat(result.storeId()).isEqualTo(testStoreId);
            assertThat(result.quantity()).isEqualTo(100);
            assertThat(result.reservedQuantity()).isEqualTo(20);
            assertThat(result.availableStock()).isEqualTo(80);
            verify(repository).findByProductIdAndStoreId(testProductId, testStoreId);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando producto no existe en la tienda")
        void shouldThrowExceptionWhenProductNotFoundInStore() {
            // Arrange
            when(repository.findByProductIdAndStoreId(testProductId, testStoreId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> service.checkAvailability(testProductId, testStoreId))
                    .isInstanceOf(InventoryQueryServiceImpl.InventoryItemNotFoundException.class)
                    .hasMessageContaining("Product")
                    .hasMessageContaining("not found in store");

            verify(repository).findByProductIdAndStoreId(testProductId, testStoreId);
        }

        @Test
        @DisplayName("Debe indicar disponibilidad correctamente")
        void shouldIndicateAvailabilityCorrectly() {
            // Arrange
            InventoryProjection projection = createTestProjection(testItemId, testStoreId, testProductId);
            projection.setQuantity(100);
            projection.setReservedQuantity(20);
            projection.setAvailableStock(80);
            projection.setStatus(InventoryProjection.ItemStatus.ACTIVE);

            when(repository.findByProductIdAndStoreId(testProductId, testStoreId))
                    .thenReturn(Optional.of(projection));

            // Act
            InventoryQueryService.AvailabilityInfo result =
                    service.checkAvailability(testProductId, testStoreId);

            // Assert
            assertThat(result.available()).isTrue(); // Asumiendo que isAvailableForSale() retorna true
        }
    }

    @Nested
    @DisplayName("Tests de getInventoryByStore()")
    class GetInventoryByStoreTests {

        @Test
        @DisplayName("Debe retornar página de items para una tienda")
        void shouldReturnPageOfItemsForStore() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<InventoryProjection> items = Arrays.asList(
                    createTestProjection("ITEM-001", testStoreId, "PROD-001"),
                    createTestProjection("ITEM-002", testStoreId, "PROD-002")
            );
            Page<InventoryProjection> page = new PageImpl<>(items, pageable, items.size());

            when(repository.findByStoreId(testStoreId, pageable)).thenReturn(page);

            // Act
            Page<InventoryProjection> result = service.getInventoryByStore(testStoreId, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            verify(repository).findByStoreId(testStoreId, pageable);
        }

        @Test
        @DisplayName("Debe retornar página vacía cuando no hay items")
        void shouldReturnEmptyPageWhenNoItems() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<InventoryProjection> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(repository.findByStoreId(testStoreId, pageable)).thenReturn(emptyPage);

            // Act
            Page<InventoryProjection> result = service.getInventoryByStore(testStoreId, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("Debe respetar la configuración de paginación")
        void shouldRespectPaginationConfiguration() {
            // Arrange
            Pageable pageable = PageRequest.of(1, 5);
            List<InventoryProjection> items = Collections.singletonList(
                    createTestProjection(testItemId, testStoreId, testProductId));
            Page<InventoryProjection> page = new PageImpl<>(items, pageable, 10);

            when(repository.findByStoreId(testStoreId, pageable)).thenReturn(page);

            // Act
            Page<InventoryProjection> result = service.getInventoryByStore(testStoreId, pageable);

            // Assert
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(5);
            assertThat(result.getTotalElements()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Tests de getLowStockItems()")
    class GetLowStockItemsTests {

        @Test
        @DisplayName("Debe retornar items con stock bajo")
        void shouldReturnLowStockItems() {
            // Arrange
            List<InventoryProjection> lowStockItems = Arrays.asList(
                    createTestProjection("ITEM-001", testStoreId, "PROD-001"),
                    createTestProjection("ITEM-002", testStoreId, "PROD-002")
            );

            when(repository.findLowStockItems(testStoreId)).thenReturn(lowStockItems);

            // Act
            List<InventoryProjection> result = service.getLowStockItems(testStoreId);

            // Assert
            assertThat(result).hasSize(2);
            verify(repository).findLowStockItems(testStoreId);
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay items con stock bajo")
        void shouldReturnEmptyListWhenNoLowStockItems() {
            // Arrange
            when(repository.findLowStockItems(testStoreId)).thenReturn(Collections.emptyList());

            // Act
            List<InventoryProjection> result = service.getLowStockItems(testStoreId);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tests de getOutOfStockItems()")
    class GetOutOfStockItemsTests {

        @Test
        @DisplayName("Debe retornar items sin stock")
        void shouldReturnOutOfStockItems() {
            // Arrange
            List<InventoryProjection> outOfStockItems = Collections.singletonList(
                    createTestProjection(testItemId, testStoreId, testProductId)
            );

            when(repository.findOutOfStockItems(testStoreId)).thenReturn(outOfStockItems);

            // Act
            List<InventoryProjection> result = service.getOutOfStockItems(testStoreId);

            // Assert
            assertThat(result).hasSize(1);
            verify(repository).findOutOfStockItems(testStoreId);
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay items sin stock")
        void shouldReturnEmptyListWhenNoOutOfStockItems() {
            // Arrange
            when(repository.findOutOfStockItems(testStoreId)).thenReturn(Collections.emptyList());

            // Act
            List<InventoryProjection> result = service.getOutOfStockItems(testStoreId);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tests de searchItems()")
    class SearchItemsTests {

        @Test
        @DisplayName("Debe buscar items por nombre")
        void shouldSearchItemsByName() {
            // Arrange
            String searchTerm = "laptop";
            List<InventoryProjection> items = Collections.singletonList(
                    createTestProjection(testItemId, testStoreId, testProductId)
            );

            when(repository.searchByProductName(testStoreId, searchTerm.toLowerCase()))
                    .thenReturn(items);

            // Act
            List<InventoryProjection> result = service.searchItems(testStoreId, searchTerm);

            // Assert
            assertThat(result).hasSize(1);
            verify(repository).searchByProductName(testStoreId, searchTerm.toLowerCase());
        }

        @Test
        @DisplayName("Debe convertir término de búsqueda a minúsculas")
        void shouldConvertSearchTermToLowerCase() {
            // Arrange
            String searchTerm = "LAPTOP";
            when(repository.searchByProductName(testStoreId, "laptop"))
                    .thenReturn(Collections.emptyList());

            // Act
            service.searchItems(testStoreId, searchTerm);

            // Assert
            verify(repository).searchByProductName(testStoreId, "laptop");
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando término de búsqueda es nulo")
        void shouldThrowExceptionWhenSearchTermIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> service.searchItems(testStoreId, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Search term cannot be empty");

            verify(repository, never()).searchByProductName(any(), any());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando término de búsqueda está vacío")
        void shouldThrowExceptionWhenSearchTermIsEmpty() {
            // Act & Assert
            assertThatThrownBy(() -> service.searchItems(testStoreId, ""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Search term cannot be empty");

            verify(repository, never()).searchByProductName(any(), any());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando término de búsqueda solo contiene espacios")
        void shouldThrowExceptionWhenSearchTermIsBlank() {
            // Act & Assert
            assertThatThrownBy(() -> service.searchItems(testStoreId, "   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Search term cannot be empty");

            verify(repository, never()).searchByProductName(any(), any());
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no encuentra resultados")
        void shouldReturnEmptyListWhenNoResults() {
            // Arrange
            String searchTerm = "nonexistent";
            when(repository.searchByProductName(testStoreId, searchTerm))
                    .thenReturn(Collections.emptyList());

            // Act
            List<InventoryProjection> result = service.searchItems(testStoreId, searchTerm);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tests de getItemsByCategory()")
    class GetItemsByCategoryTests {

        @Test
        @DisplayName("Debe retornar items por categoría")
        void shouldReturnItemsByCategory() {
            // Arrange
            String category = "Electronics";
            List<InventoryProjection> items = Arrays.asList(
                    createTestProjection("ITEM-001", testStoreId, "PROD-001"),
                    createTestProjection("ITEM-002", testStoreId, "PROD-002")
            );

            when(repository.findByStoreIdAndCategory(testStoreId, category))
                    .thenReturn(items);

            // Act
            List<InventoryProjection> result = service.getItemsByCategory(testStoreId, category);

            // Assert
            assertThat(result).hasSize(2);
            verify(repository).findByStoreIdAndCategory(testStoreId, category);
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay items en la categoría")
        void shouldReturnEmptyListWhenNoItemsInCategory() {
            // Arrange
            String category = "NonExistent";
            when(repository.findByStoreIdAndCategory(testStoreId, category))
                    .thenReturn(Collections.emptyList());

            // Act
            List<InventoryProjection> result = service.getItemsByCategory(testStoreId, category);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tests de getInventoryStats()")
    class GetInventoryStatsTests {

        @Test
        @DisplayName("Debe calcular estadísticas correctamente")
        void shouldCalculateStatsCorrectly() {
            // Arrange
            List<InventoryProjection> items = Arrays.asList(
                    createProjectionWithDetails("ITEM-001", 100, 20, 80, true, false),
                    createProjectionWithDetails("ITEM-002", 50, 10, 40, true, false),
                    createProjectionWithDetails("ITEM-003", 5, 0, 5, false, true),
                    createProjectionWithDetails("ITEM-004", 0, 0, 0, false, false)
            );

            when(repository.findByStoreId(testStoreId)).thenReturn(items);

            // Act
            InventoryQueryService.InventoryStats result = service.getInventoryStats(testStoreId);

            // Assert
            assertThat(result.storeId()).isEqualTo(testStoreId);
            assertThat(result.totalItems()).isEqualTo(4);
            assertThat(result.lowStockItems()).isEqualTo(1);
            assertThat(result.outOfStockItems()).isEqualTo(1);
            assertThat(result.availableItems()).isEqualTo(2);
            assertThat(result.totalQuantity()).isEqualTo(155);
            assertThat(result.totalReserved()).isEqualTo(30);
            assertThat(result.totalAvailable()).isEqualTo(125);
        }

        @Test
        @DisplayName("Debe retornar estadísticas vacías cuando no hay items")
        void shouldReturnEmptyStatsWhenNoItems() {
            // Arrange
            when(repository.findByStoreId(testStoreId)).thenReturn(Collections.emptyList());

            // Act
            InventoryQueryService.InventoryStats result = service.getInventoryStats(testStoreId);

            // Assert
            assertThat(result.totalItems()).isEqualTo(0);
            assertThat(result.lowStockItems()).isEqualTo(0);
            assertThat(result.outOfStockItems()).isEqualTo(0);
            assertThat(result.availableItems()).isEqualTo(0);
            assertThat(result.totalQuantity()).isEqualTo(0);
            assertThat(result.totalReserved()).isEqualTo(0);
            assertThat(result.totalAvailable()).isEqualTo(0);
        }

        @Test
        @DisplayName("Debe calcular correctamente con solo un item")
        void shouldCalculateCorrectlyWithSingleItem() {
            // Arrange
            List<InventoryProjection> items = Collections.singletonList(
                    createProjectionWithDetails("ITEM-001", 100, 20, 80, true, false)
            );

            when(repository.findByStoreId(testStoreId)).thenReturn(items);

            // Act
            InventoryQueryService.InventoryStats result = service.getInventoryStats(testStoreId);

            // Assert
            assertThat(result.totalItems()).isEqualTo(1);
            assertThat(result.totalQuantity()).isEqualTo(100);
            assertThat(result.totalReserved()).isEqualTo(20);
            assertThat(result.totalAvailable()).isEqualTo(80);
        }
    }

    @Nested
    @DisplayName("Tests de InventoryItemNotFoundException")
    class InventoryItemNotFoundExceptionTests {

        @Test
        @DisplayName("Debe crear excepción con mensaje")
        void shouldCreateExceptionWithMessage() {
            // Arrange
            String message = "Test error message";

            // Act
            InventoryQueryServiceImpl.InventoryItemNotFoundException exception =
                    new InventoryQueryServiceImpl.InventoryItemNotFoundException(message);

            // Assert
            assertThat(exception).isInstanceOf(RuntimeException.class);
            assertThat(exception.getMessage()).isEqualTo(message);
        }
    }

    // Helper methods
    private InventoryProjection createTestProjection(String id, String storeId, String productId) {
        return InventoryProjection.builder()
                .id(id)
                .storeId(storeId)
                .storeName("Store Central")
                .productId(productId)
                .productName("Test Product")
                .category("Electronics")
                .quantity(100)
                .reservedQuantity(20)
                .availableStock(80)
                .minThreshold(10)
                .belowThreshold(false)
                .version(1L)
                .lastUpdated(LocalDateTime.now())
                .lastModifiedBy("USER-001")
                .status(InventoryProjection.ItemStatus.ACTIVE)
                .build();
    }

    private InventoryProjection createProjectionWithDetails(
            String id, int quantity, int reserved, int available,
            boolean availableForSale, boolean belowThreshold) {
        InventoryProjection projection = InventoryProjection.builder()
                .id(id)
                .storeId(testStoreId)
                .productId("PROD-" + id)
                .productName("Product " + id)
                .quantity(quantity)
                .reservedQuantity(reserved)
                .availableStock(available)
                .minThreshold(10)
                .belowThreshold(belowThreshold)
                .status(availableForSale ? InventoryProjection.ItemStatus.ACTIVE :
                        InventoryProjection.ItemStatus.OUT_OF_STOCK)
                .build();

        return projection;
    }
}