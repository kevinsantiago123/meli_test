package com.meli.inventory.command.service.domain.model;

import com.meli.inventory.command.service.domain.exception.InsufficientStockException;
import com.meli.inventory.command.service.domain.exception.OptimisticLockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InventoryItem - Domain Model Tests")
class InventoryItemTest {

    private String testStoreId;
    private String testProductId;
    private String testProductName;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testStoreId = "STORE-001";
        testProductId = "PROD-001";
        testProductName = "Test Product";
        testUserId = "USER-123";
    }

    @Nested
    @DisplayName("Tests de Creación")
    class CreationTests {

        @Test
        @DisplayName("Debe crear un InventoryItem con el método create()")
        void shouldCreateInventoryItemWithFactoryMethod() {
            // Arrange
            Integer quantity = 100;
            Integer minThreshold = 10;

            // Act
            InventoryItem item = InventoryItem.create(
                    testStoreId,
                    testProductId,
                    testProductName,
                    quantity,
                    minThreshold,
                    testUserId
            );

            // Assert
            assertThat(item).isNotNull();
            assertThat(item.getId()).isNotNull();
            assertThat(item.getStoreId()).isEqualTo(testStoreId);
            assertThat(item.getProductId()).isEqualTo(testProductId);
            assertThat(item.getProductName()).isEqualTo(testProductName);
            assertThat(item.getQuantity()).isEqualTo(quantity);
            assertThat(item.getReservedQuantity()).isEqualTo(0);
            assertThat(item.getMinThreshold()).isEqualTo(minThreshold);
            assertThat(item.getVersion()).isEqualTo(1L);
            assertThat(item.getLastUpdated()).isNotNull();
            assertThat(item.getLastModifiedBy()).isEqualTo(testUserId);
        }

        @Test
        @DisplayName("Debe generar IDs únicos para diferentes items")
        void shouldGenerateUniqueIds() {
            // Act
            InventoryItem item1 = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            InventoryItem item2 = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);

            // Assert
            assertThat(item1.getId()).isNotEqualTo(item2.getId());
        }

        @Test
        @DisplayName("Debe inicializar con reservedQuantity en 0")
        void shouldInitializeWithZeroReservedQuantity() {
            // Act
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);

            // Assert
            assertThat(item.getReservedQuantity()).isEqualTo(0);
        }

        @Test
        @DisplayName("Debe crear con version 1L inicial")
        void shouldCreateWithInitialVersion() {
            // Act
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);

            // Assert
            assertThat(item.getVersion()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Debe establecer timestamp de creación")
        void shouldSetCreationTimestamp() {
            // Arrange
            LocalDateTime before = LocalDateTime.now();

            // Act
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);

            LocalDateTime after = LocalDateTime.now();

            // Assert
            assertThat(item.getLastUpdated()).isBetween(before, after);
        }
    }

    @Nested
    @DisplayName("Tests de updateQuantity()")
    class UpdateQuantityTests {

        @Test
        @DisplayName("Debe actualizar la cantidad correctamente")
        void shouldUpdateQuantity() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            Long initialVersion = item.getVersion();

            // Act
            item.updateQuantity(150, testUserId);

            // Assert
            assertThat(item.getQuantity()).isEqualTo(150);
            assertThat(item.getVersion()).isEqualTo(initialVersion + 1);
            assertThat(item.getLastModifiedBy()).isEqualTo(testUserId);
        }

        @Test
        @DisplayName("Debe lanzar excepción si la cantidad es negativa")
        void shouldThrowExceptionWhenQuantityIsNegative() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);

            // Act & Assert
            assertThatThrownBy(() -> item.updateQuantity(-10, testUserId))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("Stock cannot be negative");
        }

        @Test
        @DisplayName("Debe lanzar excepción si la cantidad es menor que la reservada")
        void shouldThrowExceptionWhenQuantityLessThanReserved() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            item.reserveStock(50, testUserId);

            // Act & Assert
            assertThatThrownBy(() -> item.updateQuantity(30, testUserId))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("Available stock cannot be less than reserved quantity");
        }

        @Test
        @DisplayName("Debe permitir actualizar a cantidad igual a la reservada")
        void shouldAllowUpdateToQuantityEqualToReserved() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            item.reserveStock(50, testUserId);

            // Act & Assert
            assertThatNoException().isThrownBy(() -> item.updateQuantity(50, testUserId));
            assertThat(item.getQuantity()).isEqualTo(50);
        }

        @Test
        @DisplayName("Debe incrementar la versión al actualizar")
        void shouldIncrementVersionWhenUpdating() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            Long version1 = item.getVersion();

            // Act
            item.updateQuantity(120, testUserId);
            Long version2 = item.getVersion();
            item.updateQuantity(150, testUserId);
            Long version3 = item.getVersion();

            // Assert
            assertThat(version2).isEqualTo(version1 + 1);
            assertThat(version3).isEqualTo(version2 + 1);
        }
    }

    @Nested
    @DisplayName("Tests de addStock()")
    class AddStockTests {

        @Test
        @DisplayName("Debe agregar stock correctamente")
        void shouldAddStock() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);

            // Act
            item.addStock(50, testUserId);

            // Assert
            assertThat(item.getQuantity()).isEqualTo(150);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10, -100})
        @DisplayName("Debe lanzar excepción si el monto no es positivo")
        void shouldThrowExceptionWhenAmountNotPositive(int amount) {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);

            // Act & Assert
            assertThatThrownBy(() -> item.addStock(amount, testUserId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount to add must be positive");
        }

        @Test
        @DisplayName("Debe incrementar versión al agregar stock")
        void shouldIncrementVersionWhenAddingStock() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            Long initialVersion = item.getVersion();

            // Act
            item.addStock(50, testUserId);

            // Assert
            assertThat(item.getVersion()).isEqualTo(initialVersion + 1);
        }
    }

    @Nested
    @DisplayName("Tests de reduceStock()")
    class ReduceStockTests {

        @Test
        @DisplayName("Debe reducir stock correctamente")
        void shouldReduceStock() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);

            // Act
            item.reduceStock(30, testUserId);

            // Assert
            assertThat(item.getQuantity()).isEqualTo(70);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10})
        @DisplayName("Debe lanzar excepción si el monto no es positivo")
        void shouldThrowExceptionWhenAmountNotPositive(int amount) {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);

            // Act & Assert
            assertThatThrownBy(() -> item.reduceStock(amount, testUserId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount to reduce must be positive");
        }

        @Test
        @DisplayName("Debe lanzar excepción si no hay suficiente stock disponible")
        void shouldThrowExceptionWhenInsufficientAvailableStock() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            item.reserveStock(50, testUserId); // 50 disponible

            // Act & Assert
            assertThatThrownBy(() -> item.reduceStock(60, testUserId))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("Insufficient available stock");
        }

        @Test
        @DisplayName("Debe permitir reducir hasta el stock disponible exacto")
        void shouldAllowReducingExactAvailableStock() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            item.reserveStock(50, testUserId);

            // Act & Assert
            assertThatNoException().isThrownBy(() -> item.reduceStock(50, testUserId));
            assertThat(item.getQuantity()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("Tests de reserveStock()")
    class ReserveStockTests {

        @Test
        @DisplayName("Debe reservar stock correctamente")
        void shouldReserveStock() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);

            // Act
            item.reserveStock(30, testUserId);

            // Assert
            assertThat(item.getReservedQuantity()).isEqualTo(30);
            assertThat(item.getQuantity()).isEqualTo(100);
            assertThat(item.getAvailableStock()).isEqualTo(70);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -5})
        @DisplayName("Debe lanzar excepción si el monto no es positivo")
        void shouldThrowExceptionWhenAmountNotPositive(int amount) {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);

            // Act & Assert
            assertThatThrownBy(() -> item.reserveStock(amount, testUserId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount to reserve must be positive");
        }

        @Test
        @DisplayName("Debe lanzar excepción si no hay suficiente stock disponible")
        void shouldThrowExceptionWhenInsufficientStock() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);

            // Act & Assert
            assertThatThrownBy(() -> item.reserveStock(150, testUserId))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("Insufficient stock to reserve");
        }

        @Test
        @DisplayName("Debe permitir múltiples reservas hasta el límite")
        void shouldAllowMultipleReservationsUpToLimit() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);

            // Act
            item.reserveStock(30, testUserId);
            item.reserveStock(40, testUserId);
            item.reserveStock(30, testUserId);

            // Assert
            assertThat(item.getReservedQuantity()).isEqualTo(100);
            assertThat(item.getAvailableStock()).isEqualTo(0);
        }

        @Test
        @DisplayName("Debe incrementar versión al reservar")
        void shouldIncrementVersionWhenReserving() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            Long initialVersion = item.getVersion();

            // Act
            item.reserveStock(30, testUserId);

            // Assert
            assertThat(item.getVersion()).isEqualTo(initialVersion + 1);
        }
    }

    @Nested
    @DisplayName("Tests de releaseReservedStock()")
    class ReleaseReservedStockTests {

        @Test
        @DisplayName("Debe liberar stock reservado correctamente")
        void shouldReleaseReservedStock() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            item.reserveStock(50, testUserId);

            // Act
            item.releaseReservedStock(20, testUserId);

            // Assert
            assertThat(item.getReservedQuantity()).isEqualTo(30);
            assertThat(item.getAvailableStock()).isEqualTo(70);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10})
        @DisplayName("Debe lanzar excepción si el monto no es positivo")
        void shouldThrowExceptionWhenAmountNotPositive(int amount) {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            item.reserveStock(50, testUserId);

            // Act & Assert
            assertThatThrownBy(() -> item.releaseReservedStock(amount, testUserId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount to release must be positive");
        }

        @Test
        @DisplayName("Debe lanzar excepción si se intenta liberar más de lo reservado")
        void shouldThrowExceptionWhenReleasingMoreThanReserved() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            item.reserveStock(30, testUserId);

            // Act & Assert
            assertThatThrownBy(() -> item.releaseReservedStock(50, testUserId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot release more than reserved");
        }

        @Test
        @DisplayName("Debe permitir liberar toda la cantidad reservada")
        void shouldAllowReleasingAllReserved() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            item.reserveStock(50, testUserId);

            // Act
            item.releaseReservedStock(50, testUserId);

            // Assert
            assertThat(item.getReservedQuantity()).isEqualTo(0);
            assertThat(item.getAvailableStock()).isEqualTo(100);
        }

        @Test
        @DisplayName("Debe incrementar versión al liberar")
        void shouldIncrementVersionWhenReleasing() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            item.reserveStock(50, testUserId);
            Long versionAfterReserve = item.getVersion();

            // Act
            item.releaseReservedStock(20, testUserId);

            // Assert
            assertThat(item.getVersion()).isEqualTo(versionAfterReserve + 1);
        }
    }

    @Nested
    @DisplayName("Tests de confirmReservation()")
    class ConfirmReservationTests {

        @Test
        @DisplayName("Debe confirmar reserva correctamente")
        void shouldConfirmReservation() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            item.reserveStock(30, testUserId);

            // Act
            item.confirmReservation(30, testUserId);

            // Assert
            assertThat(item.getQuantity()).isEqualTo(70);
            assertThat(item.getReservedQuantity()).isEqualTo(0);
            assertThat(item.getAvailableStock()).isEqualTo(70);
        }

        @Test
        @DisplayName("Debe confirmar reserva parcial")
        void shouldConfirmPartialReservation() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            item.reserveStock(50, testUserId);

            // Act
            item.confirmReservation(20, testUserId);

            // Assert
            assertThat(item.getQuantity()).isEqualTo(80);
            assertThat(item.getReservedQuantity()).isEqualTo(30);
            assertThat(item.getAvailableStock()).isEqualTo(50);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -5})
        @DisplayName("Debe lanzar excepción si el monto no es positivo")
        void shouldThrowExceptionWhenAmountNotPositive(int amount) {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            item.reserveStock(50, testUserId);

            // Act & Assert
            assertThatThrownBy(() -> item.confirmReservation(amount, testUserId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount to confirm must be positive");
        }

        @Test
        @DisplayName("Debe lanzar excepción si se confirma más de lo reservado")
        void shouldThrowExceptionWhenConfirmingMoreThanReserved() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            item.reserveStock(30, testUserId);

            // Act & Assert
            assertThatThrownBy(() -> item.confirmReservation(50, testUserId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot confirm more than reserved");
        }

        @Test
        @DisplayName("Debe incrementar versión dos veces al confirmar (reserva + cantidad)")
        void shouldIncrementVersionTwiceWhenConfirming() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            item.reserveStock(30, testUserId);
            Long versionAfterReserve = item.getVersion();

            // Act
            item.confirmReservation(30, testUserId);

            // Assert - se incrementa una vez en confirmReservation y otra en updateQuantity
            assertThat(item.getVersion()).isEqualTo(versionAfterReserve + 1);
        }
    }

    @Nested
    @DisplayName("Tests de isBelowThreshold()")
    class BelowThresholdTests {

        @Test
        @DisplayName("Debe retornar true cuando el stock disponible está por debajo del umbral")
        void shouldReturnTrueWhenBelowThreshold() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 50, testUserId);
            item.reserveStock(60, testUserId); // disponible: 40

            // Act & Assert
            assertThat(item.isBelowThreshold()).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false cuando el stock disponible está por encima del umbral")
        void shouldReturnFalseWhenAboveThreshold() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 30, testUserId);

            // Act & Assert
            assertThat(item.isBelowThreshold()).isFalse();
        }

        @Test
        @DisplayName("Debe retornar false cuando el stock disponible es igual al umbral")
        void shouldReturnFalseWhenEqualToThreshold() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 50, testUserId);
            item.reserveStock(50, testUserId); // disponible: 50

            // Act & Assert
            assertThat(item.isBelowThreshold()).isFalse();
        }
    }

    @Nested
    @DisplayName("Tests de getAvailableStock()")
    class AvailableStockTests {

        @Test
        @DisplayName("Debe calcular correctamente el stock disponible")
        void shouldCalculateAvailableStock() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            item.reserveStock(30, testUserId);

            // Act
            Integer availableStock = item.getAvailableStock();

            // Assert
            assertThat(availableStock).isEqualTo(70);
        }

        @Test
        @DisplayName("Debe retornar 0 cuando todo está reservado")
        void shouldReturnZeroWhenFullyReserved() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            item.reserveStock(100, testUserId);

            // Act & Assert
            assertThat(item.getAvailableStock()).isEqualTo(0);
        }

        @Test
        @DisplayName("Debe retornar cantidad total cuando no hay reservas")
        void shouldReturnTotalWhenNoReservations() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);

            // Act & Assert
            assertThat(item.getAvailableStock()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("Tests de validateVersion() - Optimistic Locking")
    class ValidateVersionTests {

        @Test
        @DisplayName("Debe validar versión correcta sin lanzar excepción")
        void shouldValidateCorrectVersion() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            Long currentVersion = item.getVersion();

            // Act & Assert
            assertThatNoException().isThrownBy(() -> item.validateVersion(currentVersion));
        }

        @Test
        @DisplayName("Debe lanzar OptimisticLockException cuando las versiones no coinciden")
        void shouldThrowExceptionWhenVersionMismatch() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            item.updateQuantity(120, testUserId); // version = 2

            // Act & Assert
            assertThatThrownBy(() -> item.validateVersion(1L))
                    .isInstanceOf(OptimisticLockException.class)
                    .hasMessageContaining("Version mismatch")
                    .hasMessageContaining("Expected: 1")
                    .hasMessageContaining("Current: 2");
        }

        @Test
        @DisplayName("Debe detectar versión desactualizada después de múltiples operaciones")
        void shouldDetectStaleVersionAfterMultipleOperations() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            Long originalVersion = item.getVersion();

            item.addStock(50, testUserId);
            item.reserveStock(30, testUserId);
            item.releaseReservedStock(10, testUserId);

            // Act & Assert
            assertThatThrownBy(() -> item.validateVersion(originalVersion))
                    .isInstanceOf(OptimisticLockException.class);
        }
    }

    @Nested
    @DisplayName("Tests de Constructores Lombok")
    class LombokConstructorTests {

        @Test
        @DisplayName("Debe crear con @NoArgsConstructor")
        void shouldCreateWithNoArgsConstructor() {
            // Act
            InventoryItem item = new InventoryItem();

            // Assert
            assertThat(item).isNotNull();
        }

        @Test
        @DisplayName("Debe crear con @AllArgsConstructor")
        void shouldCreateWithAllArgsConstructor() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();

            // Act
            InventoryItem item = new InventoryItem(
                    "ID-001",
                    testStoreId,
                    testProductId,
                    testProductName,
                    100,
                    20,
                    10,
                    1L,
                    now,
                    testUserId
            );

            // Assert
            assertThat(item.getId()).isEqualTo("ID-001");
            assertThat(item.getQuantity()).isEqualTo(100);
            assertThat(item.getReservedQuantity()).isEqualTo(20);
        }

        @Test
        @DisplayName("Debe crear con @Builder")
        void shouldCreateWithBuilder() {
            // Act
            InventoryItem item = InventoryItem.builder()
                    .id("ID-001")
                    .storeId(testStoreId)
                    .productId(testProductId)
                    .productName(testProductName)
                    .quantity(100)
                    .reservedQuantity(0)
                    .minThreshold(10)
                    .version(1L)
                    .lastUpdated(LocalDateTime.now())
                    .lastModifiedBy(testUserId)
                    .build();

            // Assert
            assertThat(item).isNotNull();
            assertThat(item.getId()).isEqualTo("ID-001");
        }
    }

    @Nested
    @DisplayName("Tests de Escenarios Complejos")
    class ComplexScenarioTests {

        @Test
        @DisplayName("Escenario: Flujo completo de reserva y confirmación")
        void shouldHandleCompleteReservationFlow() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);

            // Act - Reservar
            item.reserveStock(30, testUserId);
            assertThat(item.getQuantity()).isEqualTo(100);
            assertThat(item.getReservedQuantity()).isEqualTo(30);
            assertThat(item.getAvailableStock()).isEqualTo(70);

            // Act - Confirmar
            item.confirmReservation(30, testUserId);

            // Assert
            assertThat(item.getQuantity()).isEqualTo(70);
            assertThat(item.getReservedQuantity()).isEqualTo(0);
            assertThat(item.getAvailableStock()).isEqualTo(70);
        }

        @Test
        @DisplayName("Escenario: Flujo de reserva y cancelación")
        void shouldHandleReservationCancellationFlow() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);

            // Act - Reservar
            item.reserveStock(40, testUserId);

            // Act - Cancelar
            item.releaseReservedStock(40, testUserId);

            // Assert
            assertThat(item.getQuantity()).isEqualTo(100);
            assertThat(item.getReservedQuantity()).isEqualTo(0);
            assertThat(item.getAvailableStock()).isEqualTo(100);
        }

        @Test
        @DisplayName("Escenario: Múltiples operaciones con verificación de versión")
        void shouldTrackVersionThroughMultipleOperations() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 10, testUserId);
            Long v1 = item.getVersion(); // v1 = 1

            // Act & Assert
            item.addStock(50, testUserId);
            assertThat(item.getVersion()).isEqualTo(v1 + 1); // v2 = 2

            item.reserveStock(30, testUserId);
            assertThat(item.getVersion()).isEqualTo(v1 + 2); // v3 = 3

            item.confirmReservation(20, testUserId);
            assertThat(item.getVersion()).isEqualTo(v1 + 3); // v4 = 4

            item.releaseReservedStock(10, testUserId);
            assertThat(item.getVersion()).isEqualTo(v1 + 4); // v5 = 5
        }

        @Test
        @DisplayName("Escenario: Alerta de stock bajo después de múltiples operaciones")
        void shouldTriggerLowStockAlert() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    testProductName, 100, 40, testUserId);

            // Act
            item.reserveStock(50, testUserId); // disponible: 50
            assertThat(item.isBelowThreshold()).isFalse();

            item.confirmReservation(50, testUserId); // disponible: 50 -> 0 (reserva) y quantity: 50
            assertThat(item.isBelowThreshold()).isFalse();

            item.reduceStock(20, testUserId); // disponible: 30

            // Assert
            assertThat(item.isBelowThreshold()).isTrue();
            assertThat(item.getAvailableStock()).isEqualTo(30);
        }
    }
}