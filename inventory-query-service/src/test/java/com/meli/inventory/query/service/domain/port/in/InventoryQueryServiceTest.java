package com.meli.inventory.query.service.domain.port.in;

import com.meli.inventory.query.service.domain.model.InventoryProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("InventoryQueryService - Interface Contract Tests")
class InventoryQueryServiceTest {

    @Test
    @DisplayName("Debe ser una interfaz")
    void shouldBeAnInterface() {
        // Assert
        assertThat(InventoryQueryService.class.isInterface()).isTrue();
    }

    @Test
    @DisplayName("Debe tener 8 métodos definidos")
    void shouldHaveEightMethods() {
        // Act
        Method[] methods = InventoryQueryService.class.getDeclaredMethods();

        // Assert
        assertThat(methods).hasSize(8);
    }

    @Test
    @DisplayName("Debe tener método getById con firma correcta")
    void shouldHaveGetByIdMethod() throws NoSuchMethodException {
        // Act
        Method method = InventoryQueryService.class.getMethod("getById", String.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(InventoryProjection.class);
        assertThat(method.getParameterCount()).isEqualTo(1);
        assertThat(method.getParameterTypes()[0]).isEqualTo(String.class);
    }

    @Test
    @DisplayName("Debe tener método checkAvailability con firma correcta")
    void shouldHaveCheckAvailabilityMethod() throws NoSuchMethodException {
        // Act
        Method method = InventoryQueryService.class.getMethod(
                "checkAvailability", String.class, String.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(InventoryQueryService.AvailabilityInfo.class);
        assertThat(method.getParameterCount()).isEqualTo(2);
        assertThat(method.getParameterTypes()[0]).isEqualTo(String.class);
        assertThat(method.getParameterTypes()[1]).isEqualTo(String.class);
    }

    @Test
    @DisplayName("Debe tener método getInventoryByStore con firma correcta")
    void shouldHaveGetInventoryByStoreMethod() throws NoSuchMethodException {
        // Act
        Method method = InventoryQueryService.class.getMethod(
                "getInventoryByStore", String.class, Pageable.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(Page.class);
        assertThat(method.getParameterCount()).isEqualTo(2);
        assertThat(method.getParameterTypes()[0]).isEqualTo(String.class);
        assertThat(method.getParameterTypes()[1]).isEqualTo(Pageable.class);
    }

    @Test
    @DisplayName("Debe tener método getLowStockItems con firma correcta")
    void shouldHaveGetLowStockItemsMethod() throws NoSuchMethodException {
        // Act
        Method method = InventoryQueryService.class.getMethod("getLowStockItems", String.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(List.class);
        assertThat(method.getParameterCount()).isEqualTo(1);
        assertThat(method.getParameterTypes()[0]).isEqualTo(String.class);
    }

    @Test
    @DisplayName("Debe tener método getOutOfStockItems con firma correcta")
    void shouldHaveGetOutOfStockItemsMethod() throws NoSuchMethodException {
        // Act
        Method method = InventoryQueryService.class.getMethod("getOutOfStockItems", String.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(List.class);
        assertThat(method.getParameterCount()).isEqualTo(1);
        assertThat(method.getParameterTypes()[0]).isEqualTo(String.class);
    }

    @Test
    @DisplayName("Debe tener método searchItems con firma correcta")
    void shouldHaveSearchItemsMethod() throws NoSuchMethodException {
        // Act
        Method method = InventoryQueryService.class.getMethod(
                "searchItems", String.class, String.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(List.class);
        assertThat(method.getParameterCount()).isEqualTo(2);
        assertThat(method.getParameterTypes()[0]).isEqualTo(String.class);
        assertThat(method.getParameterTypes()[1]).isEqualTo(String.class);
    }

    @Test
    @DisplayName("Debe tener método getItemsByCategory con firma correcta")
    void shouldHaveGetItemsByCategoryMethod() throws NoSuchMethodException {
        // Act
        Method method = InventoryQueryService.class.getMethod(
                "getItemsByCategory", String.class, String.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(List.class);
        assertThat(method.getParameterCount()).isEqualTo(2);
        assertThat(method.getParameterTypes()[0]).isEqualTo(String.class);
        assertThat(method.getParameterTypes()[1]).isEqualTo(String.class);
    }

    @Test
    @DisplayName("Debe tener método getInventoryStats con firma correcta")
    void shouldHaveGetInventoryStatsMethod() throws NoSuchMethodException {
        // Act
        Method method = InventoryQueryService.class.getMethod("getInventoryStats", String.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(InventoryQueryService.InventoryStats.class);
        assertThat(method.getParameterCount()).isEqualTo(1);
        assertThat(method.getParameterTypes()[0]).isEqualTo(String.class);
    }

    @Test
    @DisplayName("Debe tener record AvailabilityInfo con todos los campos")
    void shouldHaveAvailabilityInfoRecord() {
        // Act
        Class<?> recordClass = InventoryQueryService.AvailabilityInfo.class;

        // Assert
        assertThat(recordClass.isRecord()).isTrue();
        assertThat(recordClass.getRecordComponents()).hasSize(7);

        assertThat(recordClass.getRecordComponents()[0].getName()).isEqualTo("productId");
        assertThat(recordClass.getRecordComponents()[1].getName()).isEqualTo("storeId");
        assertThat(recordClass.getRecordComponents()[2].getName()).isEqualTo("available");
        assertThat(recordClass.getRecordComponents()[3].getName()).isEqualTo("quantity");
        assertThat(recordClass.getRecordComponents()[4].getName()).isEqualTo("reservedQuantity");
        assertThat(recordClass.getRecordComponents()[5].getName()).isEqualTo("availableStock");
        assertThat(recordClass.getRecordComponents()[6].getName()).isEqualTo("productName");
    }

    @Test
    @DisplayName("Debe tener record InventoryStats con todos los campos")
    void shouldHaveInventoryStatsRecord() {
        // Act
        Class<?> recordClass = InventoryQueryService.InventoryStats.class;

        // Assert
        assertThat(recordClass.isRecord()).isTrue();
        assertThat(recordClass.getRecordComponents()).hasSize(8);

        assertThat(recordClass.getRecordComponents()[0].getName()).isEqualTo("storeId");
        assertThat(recordClass.getRecordComponents()[1].getName()).isEqualTo("totalItems");
        assertThat(recordClass.getRecordComponents()[2].getName()).isEqualTo("lowStockItems");
        assertThat(recordClass.getRecordComponents()[3].getName()).isEqualTo("outOfStockItems");
        assertThat(recordClass.getRecordComponents()[4].getName()).isEqualTo("availableItems");
        assertThat(recordClass.getRecordComponents()[5].getName()).isEqualTo("totalQuantity");
        assertThat(recordClass.getRecordComponents()[6].getName()).isEqualTo("totalReserved");
        assertThat(recordClass.getRecordComponents()[7].getName()).isEqualTo("totalAvailable");
    }

    @Test
    @DisplayName("Debe poder crear instancia de AvailabilityInfo")
    void shouldCreateAvailabilityInfoInstance() {
        // Act
        InventoryQueryService.AvailabilityInfo info =
                new InventoryQueryService.AvailabilityInfo(
                        "PROD-001",
                        "STORE-001",
                        true,
                        100,
                        20,
                        80,
                        "Test Product"
                );

        // Assert
        assertThat(info).isNotNull();
        assertThat(info.productId()).isEqualTo("PROD-001");
        assertThat(info.storeId()).isEqualTo("STORE-001");
        assertThat(info.available()).isTrue();
        assertThat(info.quantity()).isEqualTo(100);
        assertThat(info.reservedQuantity()).isEqualTo(20);
        assertThat(info.availableStock()).isEqualTo(80);
        assertThat(info.productName()).isEqualTo("Test Product");
    }

    @Test
    @DisplayName("Debe poder crear instancia de InventoryStats")
    void shouldCreateInventoryStatsInstance() {
        // Act
        InventoryQueryService.InventoryStats stats =
                new InventoryQueryService.InventoryStats(
                        "STORE-001",
                        100L,
                        10L,
                        5L,
                        85L,
                        5000,
                        500,
                        4500
                );

        // Assert
        assertThat(stats).isNotNull();
        assertThat(stats.storeId()).isEqualTo("STORE-001");
        assertThat(stats.totalItems()).isEqualTo(100L);
        assertThat(stats.lowStockItems()).isEqualTo(10L);
        assertThat(stats.outOfStockItems()).isEqualTo(5L);
        assertThat(stats.availableItems()).isEqualTo(85L);
        assertThat(stats.totalQuantity()).isEqualTo(5000);
        assertThat(stats.totalReserved()).isEqualTo(500);
        assertThat(stats.totalAvailable()).isEqualTo(4500);
    }

    @Test
    @DisplayName("Records deben ser inmutables")
    void recordsShouldBeImmutable() {
        // Act
        InventoryQueryService.AvailabilityInfo info =
                new InventoryQueryService.AvailabilityInfo(
                        "PROD-001", "STORE-001", true, 100, 20, 80, "Test"
                );

        // Assert - No debe haber setters
        assertThat(info.getClass().getMethods())
                .noneMatch(m -> m.getName().startsWith("set"));
    }

    @Test
    @DisplayName("Records deben tener equals y hashCode automáticos")
    void recordsShouldHaveEqualsAndHashCode() {
        // Arrange
        InventoryQueryService.AvailabilityInfo info1 =
                new InventoryQueryService.AvailabilityInfo(
                        "PROD-001", "STORE-001", true, 100, 20, 80, "Test"
                );

        InventoryQueryService.AvailabilityInfo info2 =
                new InventoryQueryService.AvailabilityInfo(
                        "PROD-001", "STORE-001", true, 100, 20, 80, "Test"
                );

        // Assert
        assertThat(info1).isEqualTo(info2);
        assertThat(info1.hashCode()).isEqualTo(info2.hashCode());
    }

    @Test
    @DisplayName("Records diferentes deben ser distintos")
    void differentRecordsShouldNotBeEqual() {
        // Arrange
        InventoryQueryService.AvailabilityInfo info1 =
                new InventoryQueryService.AvailabilityInfo(
                        "PROD-001", "STORE-001", true, 100, 20, 80, "Test"
                );

        InventoryQueryService.AvailabilityInfo info2 =
                new InventoryQueryService.AvailabilityInfo(
                        "PROD-002", "STORE-001", true, 100, 20, 80, "Test"
                );

        // Assert
        assertThat(info1).isNotEqualTo(info2);
    }

    @Test
    @DisplayName("Records deben tener toString descriptivo")
    void recordsShouldHaveDescriptiveToString() {
        // Arrange
        InventoryQueryService.AvailabilityInfo info =
                new InventoryQueryService.AvailabilityInfo(
                        "PROD-001", "STORE-001", true, 100, 20, 80, "Test"
                );

        // Act
        String toString = info.toString();

        // Assert
        assertThat(toString).contains("PROD-001");
        assertThat(toString).contains("STORE-001");
        assertThat(toString).contains("100");
    }

    @Test
    @DisplayName("Métodos de consulta deben retornar tipos correctos")
    void queryMethodsShouldReturnCorrectTypes() throws NoSuchMethodException {
        // Assert - Métodos que retornan single item
        assertThat(InventoryQueryService.class.getMethod("getById", String.class)
                .getReturnType()).isEqualTo(InventoryProjection.class);

        // Assert - Métodos que retornan listas
        assertThat(InventoryQueryService.class.getMethod("getLowStockItems", String.class)
                .getReturnType()).isEqualTo(List.class);
        assertThat(InventoryQueryService.class.getMethod("getOutOfStockItems", String.class)
                .getReturnType()).isEqualTo(List.class);
        assertThat(InventoryQueryService.class.getMethod("searchItems", String.class, String.class)
                .getReturnType()).isEqualTo(List.class);

        // Assert - Método que retorna Page
        assertThat(InventoryQueryService.class.getMethod("getInventoryByStore", String.class, Pageable.class)
                .getReturnType()).isEqualTo(Page.class);

        // Assert - Métodos que retornan DTOs
        assertThat(InventoryQueryService.class.getMethod("checkAvailability", String.class, String.class)
                .getReturnType()).isEqualTo(InventoryQueryService.AvailabilityInfo.class);
        assertThat(InventoryQueryService.class.getMethod("getInventoryStats", String.class)
                .getReturnType()).isEqualTo(InventoryQueryService.InventoryStats.class);
    }

    @Test
    @DisplayName("AvailabilityInfo debe indicar disponibilidad correctamente")
    void availabilityInfoShouldIndicateAvailability() {
        // Arrange & Act
        InventoryQueryService.AvailabilityInfo available =
                new InventoryQueryService.AvailabilityInfo(
                        "PROD-001", "STORE-001", true, 100, 20, 80, "Available Product"
                );

        InventoryQueryService.AvailabilityInfo notAvailable =
                new InventoryQueryService.AvailabilityInfo(
                        "PROD-002", "STORE-001", false, 0, 0, 0, "Unavailable Product"
                );

        // Assert
        assertThat(available.available()).isTrue();
        assertThat(available.availableStock()).isGreaterThan(0);

        assertThat(notAvailable.available()).isFalse();
        assertThat(notAvailable.availableStock()).isEqualTo(0);
    }

    @Test
    @DisplayName("InventoryStats debe contener estadísticas agregadas")
    void inventoryStatsShouldContainAggregatedData() {
        // Act
        InventoryQueryService.InventoryStats stats =
                new InventoryQueryService.InventoryStats(
                        "STORE-001",
                        100L,
                        10L,
                        5L,
                        85L,
                        5000,
                        500,
                        4500
                );

        // Assert
        assertThat(stats.totalItems()).isEqualTo(100L);
        assertThat(stats.lowStockItems() + stats.outOfStockItems() + stats.availableItems())
                .isEqualTo(stats.totalItems());
        assertThat(stats.totalQuantity() - stats.totalReserved())
                .isEqualTo(stats.totalAvailable());
    }
}