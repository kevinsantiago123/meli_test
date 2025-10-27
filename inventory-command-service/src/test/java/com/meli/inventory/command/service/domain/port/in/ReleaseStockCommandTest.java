package com.meli.inventory.command.service.domain.port.in;

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

@DisplayName("ReleaseStockCommand - Validation Tests")
class ReleaseStockCommandTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Debe crear command válido con todos los campos")
    void shouldCreateValidCommand() {
        // Arrange & Act
        ReleaseStockCommand command = ReleaseStockCommand.builder()
                .itemId("ITEM-001")
                .quantity(10)
                .reservationId("RES-001")
                .version(1L)
                .userId("USER-001")
                .build();

        Set<ConstraintViolation<ReleaseStockCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isEmpty();
        assertThat(command.getItemId()).isEqualTo("ITEM-001");
        assertThat(command.getQuantity()).isEqualTo(10);
        assertThat(command.getReservationId()).isEqualTo("RES-001");
        assertThat(command.getVersion()).isEqualTo(1L);
        assertThat(command.getUserId()).isEqualTo("USER-001");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    @DisplayName("Debe fallar cuando itemId es nulo, vacío o blanco")
    void shouldFailWhenItemIdIsInvalid(String itemId) {
        // Arrange
        ReleaseStockCommand command = ReleaseStockCommand.builder()
                .itemId(itemId)
                .quantity(10)
                .reservationId("RES-001")
                .version(1L)
                .userId("USER-001")
                .build();

        // Act
        Set<ConstraintViolation<ReleaseStockCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Item ID is required");
    }

    @Test
    @DisplayName("Debe fallar cuando quantity es nulo")
    void shouldFailWhenQuantityIsNull() {
        // Arrange
        ReleaseStockCommand command = ReleaseStockCommand.builder()
                .itemId("ITEM-001")
                .quantity(null)
                .reservationId("RES-001")
                .version(1L)
                .userId("USER-001")
                .build();

        // Act
        Set<ConstraintViolation<ReleaseStockCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Quantity is required");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    @DisplayName("Debe fallar cuando quantity no es positivo")
    void shouldFailWhenQuantityIsNotPositive(int quantity) {
        // Arrange
        ReleaseStockCommand command = ReleaseStockCommand.builder()
                .itemId("ITEM-001")
                .quantity(quantity)
                .reservationId("RES-001")
                .version(1L)
                .userId("USER-001")
                .build();

        // Act
        Set<ConstraintViolation<ReleaseStockCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Quantity must be positive");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    @DisplayName("Debe fallar cuando reservationId es nulo, vacío o blanco")
    void shouldFailWhenReservationIdIsInvalid(String reservationId) {
        // Arrange
        ReleaseStockCommand command = ReleaseStockCommand.builder()
                .itemId("ITEM-001")
                .quantity(10)
                .reservationId(reservationId)
                .version(1L)
                .userId("USER-001")
                .build();

        // Act
        Set<ConstraintViolation<ReleaseStockCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Reservation ID is required");
    }

    @Test
    @DisplayName("Debe fallar cuando version es nulo")
    void shouldFailWhenVersionIsNull() {
        // Arrange
        ReleaseStockCommand command = ReleaseStockCommand.builder()
                .itemId("ITEM-001")
                .quantity(10)
                .reservationId("RES-001")
                .version(null)
                .userId("USER-001")
                .build();

        // Act
        Set<ConstraintViolation<ReleaseStockCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Version is required for optimistic locking");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    @DisplayName("Debe fallar cuando userId es nulo, vacío o blanco")
    void shouldFailWhenUserIdIsInvalid(String userId) {
        // Arrange
        ReleaseStockCommand command = ReleaseStockCommand.builder()
                .itemId("ITEM-001")
                .quantity(10)
                .reservationId("RES-001")
                .version(1L)
                .userId(userId)
                .build();

        // Act
        Set<ConstraintViolation<ReleaseStockCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("User ID is required");
    }

    @Test
    @DisplayName("Debe crear con @NoArgsConstructor")
    void shouldCreateWithNoArgsConstructor() {
        // Act
        ReleaseStockCommand command = new ReleaseStockCommand();

        // Assert
        assertThat(command).isNotNull();
    }

    @Test
    @DisplayName("Debe crear con @AllArgsConstructor")
    void shouldCreateWithAllArgsConstructor() {
        // Act
        ReleaseStockCommand command = new ReleaseStockCommand(
                "ITEM-001",
                10,
                "RES-001",
                1L,
                "USER-001"
        );

        // Assert
        assertThat(command.getItemId()).isEqualTo("ITEM-001");
        assertThat(command.getQuantity()).isEqualTo(10);
        assertThat(command.getReservationId()).isEqualTo("RES-001");
        assertThat(command.getVersion()).isEqualTo(1L);
        assertThat(command.getUserId()).isEqualTo("USER-001");
    }

    @Test
    @DisplayName("Debe usar setters correctamente")
    void shouldUseSettersCorrectly() {
        // Arrange
        ReleaseStockCommand command = new ReleaseStockCommand();

        // Act
        command.setItemId("ITEM-002");
        command.setQuantity(20);
        command.setReservationId("RES-002");
        command.setVersion(2L);
        command.setUserId("USER-002");

        // Assert
        assertThat(command.getItemId()).isEqualTo("ITEM-002");
        assertThat(command.getQuantity()).isEqualTo(20);
        assertThat(command.getReservationId()).isEqualTo("RES-002");
        assertThat(command.getVersion()).isEqualTo(2L);
        assertThat(command.getUserId()).isEqualTo("USER-002");
    }
}