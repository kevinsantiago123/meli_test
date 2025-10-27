package com.meli.inventory.command.service.domain.port.in;

import com.meli.inventory.command.service.domain.model.InventoryItem;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

@DisplayName("InventoryCommandService - Interface Contract Tests")
class InventoryCommandServiceTest {

    @Test
    @DisplayName("Debe ser una interfaz")
    void shouldBeAnInterface() {
        // Assert
        assertThat(InventoryCommandService.class.isInterface()).isTrue();
    }

    @Test
    @DisplayName("Debe tener 6 métodos definidos")
    void shouldHaveSixMethods() {
        // Act
        Method[] methods = InventoryCommandService.class.getDeclaredMethods();

        // Assert
        assertThat(methods).hasSize(6);
    }

    @Test
    @DisplayName("Debe tener método updateStock con anotaciones de resiliencia")
    void shouldHaveUpdateStockMethodWithAnnotations() throws NoSuchMethodException {
        // Act
        Method method = InventoryCommandService.class.getMethod("updateStock", UpdateStockCommand.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(InventoryItem.class);
        assertThat(method.isAnnotationPresent(Transactional.class)).isTrue();
        assertThat(method.isAnnotationPresent(CircuitBreaker.class)).isTrue();
        assertThat(method.isAnnotationPresent(Retry.class)).isTrue();
        assertThat(method.isAnnotationPresent(Bulkhead.class)).isTrue();

        CircuitBreaker circuitBreaker = method.getAnnotation(CircuitBreaker.class);
        assertThat(circuitBreaker.name()).isEqualTo("inventoryCommand");
        assertThat(circuitBreaker.fallbackMethod()).isEqualTo("updateStockFallback");
    }

    @Test
    @DisplayName("Debe tener método createInventoryItem con anotaciones de resiliencia")
    void shouldHaveCreateInventoryItemMethodWithAnnotations() throws NoSuchMethodException {
        // Act
        Method method = InventoryCommandService.class.getMethod("createInventoryItem", CreateInventoryItemCommand.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(InventoryItem.class);
        assertThat(method.isAnnotationPresent(Transactional.class)).isTrue();
        assertThat(method.isAnnotationPresent(CircuitBreaker.class)).isTrue();
        assertThat(method.isAnnotationPresent(Retry.class)).isTrue();
        assertThat(method.isAnnotationPresent(Bulkhead.class)).isTrue();

        CircuitBreaker circuitBreaker = method.getAnnotation(CircuitBreaker.class);
        assertThat(circuitBreaker.name()).isEqualTo("inventoryCommand");
        assertThat(circuitBreaker.fallbackMethod()).isEqualTo("createInventoryItemFallback");
    }

    @Test
    @DisplayName("Debe tener método reserveStock con anotaciones de resiliencia")
    void shouldHaveReserveStockMethodWithAnnotations() throws NoSuchMethodException {
        // Act
        Method method = InventoryCommandService.class.getMethod("reserveStock", ReserveStockCommand.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(InventoryItem.class);
        assertThat(method.isAnnotationPresent(Transactional.class)).isTrue();
        assertThat(method.isAnnotationPresent(CircuitBreaker.class)).isTrue();
        assertThat(method.isAnnotationPresent(Retry.class)).isTrue();
        assertThat(method.isAnnotationPresent(Bulkhead.class)).isTrue();

        CircuitBreaker circuitBreaker = method.getAnnotation(CircuitBreaker.class);
        assertThat(circuitBreaker.name()).isEqualTo("inventoryCommand");
        assertThat(circuitBreaker.fallbackMethod()).isEqualTo("reserveStockFallback");
    }

    @Test
    @DisplayName("Debe tener método releaseReservedStock con anotaciones de resiliencia")
    void shouldHaveReleaseReservedStockMethodWithAnnotations() throws NoSuchMethodException {
        // Act
        Method method = InventoryCommandService.class.getMethod("releaseReservedStock", ReleaseStockCommand.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(InventoryItem.class);
        assertThat(method.isAnnotationPresent(Transactional.class)).isTrue();
        assertThat(method.isAnnotationPresent(CircuitBreaker.class)).isTrue();
        assertThat(method.isAnnotationPresent(Retry.class)).isTrue();

        CircuitBreaker circuitBreaker = method.getAnnotation(CircuitBreaker.class);
        assertThat(circuitBreaker.name()).isEqualTo("inventoryCommand");
        assertThat(circuitBreaker.fallbackMethod()).isEmpty(); // No tiene fallback configurado
    }

    @Test
    @DisplayName("Debe tener método confirmReservation sin anotaciones de resiliencia")
    void shouldHaveConfirmReservationMethod() throws NoSuchMethodException {
        // Act
        Method method = InventoryCommandService.class.getMethod("confirmReservation", ConfirmReservationCommand.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(InventoryItem.class);
        assertThat(method.isAnnotationPresent(Transactional.class)).isFalse();
        assertThat(method.isAnnotationPresent(CircuitBreaker.class)).isFalse();
        assertThat(method.isAnnotationPresent(Retry.class)).isFalse();
        assertThat(method.isAnnotationPresent(Bulkhead.class)).isFalse();
    }

    @Test
    @DisplayName("Debe tener método deleteInventoryItem sin anotaciones")
    void shouldHaveDeleteInventoryItemMethod() throws NoSuchMethodException {
        // Act
        Method method = InventoryCommandService.class.getMethod("deleteInventoryItem", String.class, String.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(void.class);
        assertThat(method.getParameterCount()).isEqualTo(2);
        assertThat(method.getParameterTypes()[0]).isEqualTo(String.class);
        assertThat(method.getParameterTypes()[1]).isEqualTo(String.class);
        assertThat(method.isAnnotationPresent(Transactional.class)).isFalse();
    }

    @Test
    @DisplayName("Todos los métodos con CircuitBreaker deben usar el mismo nombre")
    void allCircuitBreakersShouldUseSameName() throws NoSuchMethodException {
        // Arrange
        Method[] methods = {
                InventoryCommandService.class.getMethod("updateStock", UpdateStockCommand.class),
                InventoryCommandService.class.getMethod("createInventoryItem", CreateInventoryItemCommand.class),
                InventoryCommandService.class.getMethod("reserveStock", ReserveStockCommand.class),
                InventoryCommandService.class.getMethod("releaseReservedStock", ReleaseStockCommand.class)
        };

        // Act & Assert
        for (Method method : methods) {
            if (method.isAnnotationPresent(CircuitBreaker.class)) {
                CircuitBreaker cb = method.getAnnotation(CircuitBreaker.class);
                assertThat(cb.name()).isEqualTo("inventoryCommand");
            }
        }
    }

    @Test
    @DisplayName("Todos los métodos con Retry deben usar el mismo nombre")
    void allRetriesShouldUseSameName() throws NoSuchMethodException {
        // Arrange
        Method[] methods = {
                InventoryCommandService.class.getMethod("updateStock", UpdateStockCommand.class),
                InventoryCommandService.class.getMethod("createInventoryItem", CreateInventoryItemCommand.class),
                InventoryCommandService.class.getMethod("reserveStock", ReserveStockCommand.class),
                InventoryCommandService.class.getMethod("releaseReservedStock", ReleaseStockCommand.class)
        };

        // Act & Assert
        for (Method method : methods) {
            if (method.isAnnotationPresent(Retry.class)) {
                Retry retry = method.getAnnotation(Retry.class);
                assertThat(retry.name()).isEqualTo("inventoryCommand");
            }
        }
    }

    @Test
    @DisplayName("Todos los métodos con Bulkhead deben usar el mismo nombre")
    void allBulkheadsShouldUseSameName() throws NoSuchMethodException {
        // Arrange
        Method[] methods = {
                InventoryCommandService.class.getMethod("updateStock", UpdateStockCommand.class),
                InventoryCommandService.class.getMethod("createInventoryItem", CreateInventoryItemCommand.class),
                InventoryCommandService.class.getMethod("reserveStock", ReserveStockCommand.class)
        };

        // Act & Assert
        for (Method method : methods) {
            if (method.isAnnotationPresent(Bulkhead.class)) {
                Bulkhead bulkhead = method.getAnnotation(Bulkhead.class);
                assertThat(bulkhead.name()).isEqualTo("inventoryCommand");
            }
        }
    }

    @Test
    @DisplayName("Métodos que retornan InventoryItem deben estar presentes")
    void shouldHaveMethodsReturningInventoryItem() throws NoSuchMethodException {
        // Act
        Method[] methods = {
                InventoryCommandService.class.getMethod("updateStock", UpdateStockCommand.class),
                InventoryCommandService.class.getMethod("createInventoryItem", CreateInventoryItemCommand.class),
                InventoryCommandService.class.getMethod("reserveStock", ReserveStockCommand.class),
                InventoryCommandService.class.getMethod("releaseReservedStock", ReleaseStockCommand.class),
                InventoryCommandService.class.getMethod("confirmReservation", ConfirmReservationCommand.class)
        };

        // Assert
        for (Method method : methods) {
            assertThat(method.getReturnType()).isEqualTo(InventoryItem.class);
        }
    }
}