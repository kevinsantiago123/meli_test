package com.meli.inventory.command.service.infrastructure.rest.dto;

import com.meli.inventory.command.service.domain.port.in.UpdateStockCommand;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UpdateStockRequest - DTO Tests")
class UpdateStockRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Debe crear request válido")
    void shouldCreateValidRequest() {
        // Arrange & Act
        UpdateStockRequest request = UpdateStockRequest.builder()
                .quantity(50)
                .operation(UpdateStockCommand.StockOperation.ADD)
                .version(1L)
                .reason("Restock")
                .userId("USER-001")
                .build();

        Set<ConstraintViolation<UpdateStockRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Debe fallar cuando quantity es nulo")
    void shouldFailWhenQuantityIsNull() {
        // Arrange
        UpdateStockRequest request = UpdateStockRequest.builder()
                .operation(UpdateStockCommand.StockOperation.ADD)
                .version(1L)
                .reason("Restock")
                .build();

        // Act
        Set<ConstraintViolation<UpdateStockRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Quantity is required");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -10, -100})
    @DisplayName("Debe fallar cuando quantity es negativo")
    void shouldFailWhenQuantityIsNegative(int quantity) {
        // Arrange
        UpdateStockRequest request = UpdateStockRequest.builder()
                .quantity(quantity)
                .operation(UpdateStockCommand.StockOperation.ADD)
                .version(1L)
                .reason("Restock")
                .build();

        // Act
        Set<ConstraintViolation<UpdateStockRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Quantity cannot be negative");
    }

    @Test
    @DisplayName("Debe aceptar quantity en cero")
    void shouldAcceptZeroQuantity() {
        // Arrange
        UpdateStockRequest request = UpdateStockRequest.builder()
                .quantity(0)
                .operation(UpdateStockCommand.StockOperation.SET)
                .version(1L)
                .reason("Reset")
                .build();

        // Act
        Set<ConstraintViolation<UpdateStockRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Debe fallar cuando operation es nulo")
    void shouldFailWhenOperationIsNull() {
        // Arrange
        UpdateStockRequest request = UpdateStockRequest.builder()
                .quantity(50)
                .version(1L)
                .reason("Restock")
                .build();

        // Act
        Set<ConstraintViolation<UpdateStockRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Operation is required");
    }

    @Test
    @DisplayName("Debe soportar todas las operaciones")
    void shouldSupportAllOperations() {
        // Act & Assert
        for (UpdateStockCommand.StockOperation operation : UpdateStockCommand.StockOperation.values()) {
            UpdateStockRequest request = UpdateStockRequest.builder()
                    .quantity(50)
                    .operation(operation)
                    .version(1L)
                    .reason("Test")
                    .build();

            Set<ConstraintViolation<UpdateStockRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }
    }

    @Test
    @DisplayName("Debe fallar cuando version es nulo")
    void shouldFailWhenVersionIsNull() {
        // Arrange
        UpdateStockRequest request = UpdateStockRequest.builder()
                .quantity(50)
                .operation(UpdateStockCommand.StockOperation.ADD)
                .reason("Restock")
                .build();

        // Act
        Set<ConstraintViolation<UpdateStockRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Version is required");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    @DisplayName("Debe fallar cuando reason es inválido")
    void shouldFailWhenReasonIsInvalid(String reason) {
        // Arrange
        UpdateStockRequest request = UpdateStockRequest.builder()
                .quantity(50)
                .operation(UpdateStockCommand.StockOperation.ADD)
                .version(1L)
                .reason(reason)
                .build();

        // Act
        Set<ConstraintViolation<UpdateStockRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Reason is required");
    }

    @Test
    @DisplayName("Debe permitir userId opcional")
    void shouldAllowOptionalUserId() {
        // Arrange
        UpdateStockRequest request = UpdateStockRequest.builder()
                .quantity(50)
                .operation(UpdateStockCommand.StockOperation.ADD)
                .version(1L)
                .reason("Restock")
                .build();

        // Act
        Set<ConstraintViolation<UpdateStockRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
        assertThat(request.getUserId()).isNull();
    }
}