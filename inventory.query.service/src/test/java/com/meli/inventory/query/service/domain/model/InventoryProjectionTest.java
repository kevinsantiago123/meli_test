package com.meli.inventory.query.service.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("InventoryProjection - Domain Model Tests")
class InventoryProjectionTest {

    @Nested
    @DisplayName("Tests de construcción")
    class ConstructionTests {

        @Test
        @DisplayName("Debe crear projection con builder")
        void shouldCreateProjectionWithBuilder() {
            // Arrange & Act
            InventoryProjection projection = InventoryProjection.builder()
                    .id("ITEM-001")
                    .storeId("STORE-001")
                    .storeName("Store Central")
                    .productId("PROD-001")
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
                    .unitPrice(99.99)
                    .build();

            // Assert
            assertThat(projection).isNotNull();
            assertThat(projection.getId()).isEqualTo("ITEM-001");
            assertThat(projection.getStoreId()).isEqualTo("STORE-001");
            assertThat(projection.getStoreName()).isEqualTo("Store Central");
            assertThat(projection.getProductId()).isEqualTo("PROD-001");
            assertThat(projection.getProductName()).isEqualTo("Test Product");
            assertThat(projection.getCategory()).isEqualTo("Electronics");
            assertThat(projection.getQuantity()).isEqualTo(100);
            assertThat(projection.getReservedQuantity()).isEqualTo(20);
            assertThat(projection.getAvailableStock()).isEqualTo(80);
            assertThat(projection.getMinThreshold()).isEqualTo(10);
            assertThat(projection.getBelowThreshold()).isFalse();
            assertThat(projection.getVersion()).isEqualTo(1L);
            assertThat(projection.getStatus()).isEqualTo(InventoryProjection.ItemStatus.ACTIVE);
            assertThat(projection.getUnitPrice()).isEqualTo(99.99);
        }

        @Test
        @DisplayName("Debe crear con @NoArgsConstructor")
        void shouldCreateWithNoArgsConstructor() {
            // Act
            InventoryProjection projection = new InventoryProjection();

            // Assert
            assertThat(projection).isNotNull();
            assertThat(projection.getId()).isNull();
        }

        @Test
        @DisplayName("Debe crear con @AllArgsConstructor")
        void shouldCreateWithAllArgsConstructor() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();

            // Act
            InventoryProjection projection = new InventoryProjection(
                    "ITEM-001",
                    "STORE-001",
                    "Store Central",
                    "PROD-001",
                    "Test Product",
                    "Electronics",
                    100,
                    20,
                    80,
                    10,
                    false,
                    1L,
                    now,
                    "USER-001",
                    InventoryProjection.ItemStatus.ACTIVE,
                    99.99
            );

            // Assert
            assertThat(projection.getId()).isEqualTo("ITEM-001");
            assertThat(projection.getQuantity()).isEqualTo(100);
        }

        @Test
        @DisplayName("Debe usar setters correctamente")
        void shouldUseSettersCorrectly() {
            // Arrange
            InventoryProjection projection = new InventoryProjection();

            // Act
            projection.setId("ITEM-001");
            projection.setStoreId("STORE-001");
            projection.setQuantity(100);
            projection.setReservedQuantity(20);

            // Assert
            assertThat(projection.getId()).isEqualTo("ITEM-001");
            assertThat(projection.getStoreId()).isEqualTo("STORE-001");
            assertThat(projection.getQuantity()).isEqualTo(100);
            assertThat(projection.getReservedQuantity()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("Tests de calculateDerivedFields()")
    class CalculateDerivedFieldsTests {

        @Test
        @DisplayName("Debe calcular availableStock correctamente")
        void shouldCalculateAvailableStockCorrectly() {
            // Arrange
            InventoryProjection projection = InventoryProjection.builder()
                    .quantity(100)
                    .reservedQuantity(30)
                    .minThreshold(10)
                    .build();

            // Act
            projection.calculateDerivedFields();

            // Assert
            assertThat(projection.getAvailableStock()).isEqualTo(70);
        }

        @Test
        @DisplayName("Debe establecer belowThreshold en true cuando está por debajo del umbral")
        void shouldSetBelowThresholdTrueWhenBelowMinThreshold() {
            // Arrange
            InventoryProjection projection = InventoryProjection.builder()
                    .quantity(15)
                    .reservedQuantity(10)
                    .minThreshold(10)
                    .build();

            // Act
            projection.calculateDerivedFields();

            // Assert
            assertThat(projection.getAvailableStock()).isEqualTo(5);
            assertThat(projection.getBelowThreshold()).isTrue();
        }

        @Test
        @DisplayName("Debe establecer belowThreshold en false cuando está por encima del umbral")
        void shouldSetBelowThresholdFalseWhenAboveMinThreshold() {
            // Arrange
            InventoryProjection projection = InventoryProjection.builder()
                    .quantity(100)
                    .reservedQuantity(20)
                    .minThreshold(10)
                    .build();

            // Act
            projection.calculateDerivedFields();

            // Assert
            assertThat(projection.getAvailableStock()).isEqualTo(80);
            assertThat(projection.getBelowThreshold()).isFalse();
        }

        @Test
        @DisplayName("Debe establecer status como OUT_OF_STOCK cuando availableStock es cero")
        void shouldSetStatusOutOfStockWhenAvailableStockIsZero() {
            // Arrange
            InventoryProjection projection = InventoryProjection.builder()
                    .quantity(50)
                    .reservedQuantity(50)
                    .minThreshold(10)
                    .build();

            // Act
            projection.calculateDerivedFields();

            // Assert
            assertThat(projection.getAvailableStock()).isEqualTo(0);
            assertThat(projection.getStatus()).isEqualTo(InventoryProjection.ItemStatus.OUT_OF_STOCK);
        }

        @Test
        @DisplayName("Debe establecer status como OUT_OF_STOCK cuando availableStock es negativo")
        void shouldSetStatusOutOfStockWhenAvailableStockIsNegative() {
            // Arrange
            InventoryProjection projection = InventoryProjection.builder()
                    .quantity(30)
                    .reservedQuantity(50)
                    .minThreshold(10)
                    .build();

            // Act
            projection.calculateDerivedFields();

            // Assert
            assertThat(projection.getAvailableStock()).isEqualTo(-20);
            assertThat(projection.getStatus()).isEqualTo(InventoryProjection.ItemStatus.OUT_OF_STOCK);
        }

        @Test
        @DisplayName("Debe establecer status como LOW_STOCK cuando está por debajo del umbral pero disponible")
        void shouldSetStatusLowStockWhenBelowThreshold() {
            // Arrange
            InventoryProjection projection = InventoryProjection.builder()
                    .quantity(15)
                    .reservedQuantity(10)
                    .minThreshold(10)
                    .build();

            // Act
            projection.calculateDerivedFields();

            // Assert
            assertThat(projection.getAvailableStock()).isEqualTo(5);
            assertThat(projection.getBelowThreshold()).isTrue();
            assertThat(projection.getStatus()).isEqualTo(InventoryProjection.ItemStatus.LOW_STOCK);
        }

        @Test
        @DisplayName("Debe establecer status como ACTIVE cuando hay suficiente stock")
        void shouldSetStatusActiveWhenSufficientStock() {
            // Arrange
            InventoryProjection projection = InventoryProjection.builder()
                    .quantity(100)
                    .reservedQuantity(20)
                    .minThreshold(10)
                    .build();

            // Act
            projection.calculateDerivedFields();

            // Assert
            assertThat(projection.getAvailableStock()).isEqualTo(80);
            assertThat(projection.getBelowThreshold()).isFalse();
            assertThat(projection.getStatus()).isEqualTo(InventoryProjection.ItemStatus.ACTIVE);
        }

        @Test
        @DisplayName("Debe calcular correctamente cuando no hay stock reservado")
        void shouldCalculateCorrectlyWhenNoReservedStock() {
            // Arrange
            InventoryProjection projection = InventoryProjection.builder()
                    .quantity(100)
                    .reservedQuantity(0)
                    .minThreshold(10)
                    .build();

            // Act
            projection.calculateDerivedFields();

            // Assert
            assertThat(projection.getAvailableStock()).isEqualTo(100);
            assertThat(projection.getBelowThreshold()).isFalse();
            assertThat(projection.getStatus()).isEqualTo(InventoryProjection.ItemStatus.ACTIVE);
        }

        @Test
        @DisplayName("Debe manejar caso límite cuando availableStock es igual al threshold")
        void shouldHandleEdgeCaseWhenAvailableStockEqualsThreshold() {
            // Arrange
            InventoryProjection projection = InventoryProjection.builder()
                    .quantity(50)
                    .reservedQuantity(40)
                    .minThreshold(10)
                    .build();

            // Act
            projection.calculateDerivedFields();

            // Assert
            assertThat(projection.getAvailableStock()).isEqualTo(10);
            assertThat(projection.getBelowThreshold()).isFalse();
            assertThat(projection.getStatus()).isEqualTo(InventoryProjection.ItemStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("Tests de isAvailableForSale()")
    class IsAvailableForSaleTests {

        @Test
        @DisplayName("Debe retornar true cuando hay stock disponible y status es ACTIVE")
        void shouldReturnTrueWhenAvailableAndActive() {
            // Arrange
            InventoryProjection projection = InventoryProjection.builder()
                    .quantity(100)
                    .reservedQuantity(20)
                    .minThreshold(10)
                    .build();
            projection.calculateDerivedFields();

            // Act
            boolean isAvailable = projection.isAvailableForSale();

            // Assert
            assertThat(isAvailable).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false cuando availableStock es cero")
        void shouldReturnFalseWhenAvailableStockIsZero() {
            // Arrange
            InventoryProjection projection = InventoryProjection.builder()
                    .quantity(50)
                    .reservedQuantity(50)
                    .minThreshold(10)
                    .build();
            projection.calculateDerivedFields();

            // Act
            boolean isAvailable = projection.isAvailableForSale();

            // Assert
            assertThat(isAvailable).isFalse();
        }

        @Test
        @DisplayName("Debe retornar false cuando status es OUT_OF_STOCK")
        void shouldReturnFalseWhenStatusIsOutOfStock() {
            // Arrange
            InventoryProjection projection = InventoryProjection.builder()
                    .quantity(0)
                    .reservedQuantity(0)
                    .minThreshold(10)
                    .build();
            projection.calculateDerivedFields();

            // Act
            boolean isAvailable = projection.isAvailableForSale();

            // Assert
            assertThat(isAvailable).isFalse();
            assertThat(projection.getStatus()).isEqualTo(InventoryProjection.ItemStatus.OUT_OF_STOCK);
        }

        @Test
        @DisplayName("Debe retornar false cuando status es LOW_STOCK pero hay stock disponible")
        void shouldReturnFalseWhenStatusIsLowStock() {
            // Arrange
            InventoryProjection projection = InventoryProjection.builder()
                    .quantity(15)
                    .reservedQuantity(10)
                    .minThreshold(10)
                    .status(InventoryProjection.ItemStatus.LOW_STOCK)
                    .availableStock(5)
                    .build();

            // Act
            boolean isAvailable = projection.isAvailableForSale();

            // Assert
            assertThat(isAvailable).isFalse();
        }

        @Test
        @DisplayName("Debe retornar false cuando status es DISCONTINUED")
        void shouldReturnFalseWhenStatusIsDiscontinued() {
            // Arrange
            InventoryProjection projection = InventoryProjection.builder()
                    .quantity(100)
                    .reservedQuantity(0)
                    .minThreshold(10)
                    .status(InventoryProjection.ItemStatus.DISCONTINUED)
                    .availableStock(100)
                    .build();

            // Act
            boolean isAvailable = projection.isAvailableForSale();

            // Assert
            assertThat(isAvailable).isFalse();
        }

        @Test
        @DisplayName("Debe retornar false cuando availableStock es negativo")
        void shouldReturnFalseWhenAvailableStockIsNegative() {
            // Arrange
            InventoryProjection projection = InventoryProjection.builder()
                    .quantity(20)
                    .reservedQuantity(30)
                    .minThreshold(10)
                    .availableStock(-10)
                    .status(InventoryProjection.ItemStatus.OUT_OF_STOCK)
                    .build();

            // Act
            boolean isAvailable = projection.isAvailableForSale();

            // Assert
            assertThat(isAvailable).isFalse();
        }

        @Test
        @DisplayName("Debe retornar true incluso con stock bajo si status es ACTIVE")
        void shouldReturnTrueWithLowStockIfActive() {
            // Arrange
            InventoryProjection projection = InventoryProjection.builder()
                    .quantity(5)
                    .reservedQuantity(0)
                    .minThreshold(10)
                    .availableStock(5)
                    .status(InventoryProjection.ItemStatus.ACTIVE)
                    .build();

            // Act
            boolean isAvailable = projection.isAvailableForSale();

            // Assert
            assertThat(isAvailable).isTrue();
        }
    }

    @Nested
    @DisplayName("Tests de ItemStatus enum")
    class ItemStatusTests {

        @Test
        @DisplayName("Debe tener todos los estados definidos")
        void shouldHaveAllStatusesDefined() {
            // Arrange & Act
            InventoryProjection.ItemStatus[] statuses = InventoryProjection.ItemStatus.values();

            // Assert
            assertThat(statuses).hasSize(4);
            assertThat(statuses).containsExactlyInAnyOrder(
                    InventoryProjection.ItemStatus.ACTIVE,
                    InventoryProjection.ItemStatus.LOW_STOCK,
                    InventoryProjection.ItemStatus.OUT_OF_STOCK,
                    InventoryProjection.ItemStatus.DISCONTINUED
            );
        }

        @Test
        @DisplayName("Debe poder obtener status por nombre")
        void shouldGetStatusByName() {
            // Act & Assert
            assertThat(InventoryProjection.ItemStatus.valueOf("ACTIVE"))
                    .isEqualTo(InventoryProjection.ItemStatus.ACTIVE);
            assertThat(InventoryProjection.ItemStatus.valueOf("LOW_STOCK"))
                    .isEqualTo(InventoryProjection.ItemStatus.LOW_STOCK);
            assertThat(InventoryProjection.ItemStatus.valueOf("OUT_OF_STOCK"))
                    .isEqualTo(InventoryProjection.ItemStatus.OUT_OF_STOCK);
            assertThat(InventoryProjection.ItemStatus.valueOf("DISCONTINUED"))
                    .isEqualTo(InventoryProjection.ItemStatus.DISCONTINUED);
        }
    }

    @Nested
    @DisplayName("Tests de escenarios complejos")
    class ComplexScenariosTests {

        @Test
        @DisplayName("Debe manejar múltiples recalculaciones")
        void shouldHandleMultipleRecalculations() {
            // Arrange
            InventoryProjection projection = InventoryProjection.builder()
                    .quantity(100)
                    .reservedQuantity(20)
                    .minThreshold(10)
                    .build();

            // Act - Primera calculación
            projection.calculateDerivedFields();
            assertThat(projection.getStatus()).isEqualTo(InventoryProjection.ItemStatus.ACTIVE);

            // Act - Cambiar valores y recalcular
            projection.setQuantity(10);
            projection.setReservedQuantity(5);
            projection.calculateDerivedFields();

            // Assert
            assertThat(projection.getAvailableStock()).isEqualTo(5);
            assertThat(projection.getBelowThreshold()).isTrue();
            assertThat(projection.getStatus()).isEqualTo(InventoryProjection.ItemStatus.LOW_STOCK);
        }

        @Test
        @DisplayName("Debe manejar transición de estados")
        void shouldHandleStatusTransition() {
            // Arrange
            InventoryProjection projection = InventoryProjection.builder()
                    .quantity(100)
                    .reservedQuantity(0)
                    .minThreshold(50)
                    .build();

            // ACTIVE
            projection.calculateDerivedFields();
            assertThat(projection.getStatus()).isEqualTo(InventoryProjection.ItemStatus.ACTIVE);

            // LOW_STOCK
            projection.setReservedQuantity(60);
            projection.calculateDerivedFields();
            assertThat(projection.getStatus()).isEqualTo(InventoryProjection.ItemStatus.LOW_STOCK);

            // OUT_OF_STOCK
            projection.setReservedQuantity(100);
            projection.calculateDerivedFields();
            assertThat(projection.getStatus()).isEqualTo(InventoryProjection.ItemStatus.OUT_OF_STOCK);
        }

        @Test
        @DisplayName("Debe calcular correctamente con valores extremos")
        void shouldCalculateCorrectlyWithExtremeValues() {
            // Arrange
            InventoryProjection projection = InventoryProjection.builder()
                    .quantity(Integer.MAX_VALUE)
                    .reservedQuantity(0)
                    .minThreshold(100)
                    .build();

            // Act
            projection.calculateDerivedFields();

            // Assert
            assertThat(projection.getAvailableStock()).isEqualTo(Integer.MAX_VALUE);
            assertThat(projection.getBelowThreshold()).isFalse();
            assertThat(projection.getStatus()).isEqualTo(InventoryProjection.ItemStatus.ACTIVE);
        }
    }
}