package com.meli.inventory.command.service.infrastructure.rest.dto;

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

@DisplayName("ConfirmReservationRequest - DTO Tests")
class ConfirmReservationRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Debe crear request válido con todos los campos")
    void shouldCreateValidRequest() {
        // Arrange & Act
        ConfirmReservationRequest request = ConfirmReservationRequest.builder()
                .quantity(10)
                .reservationId("RES-001")
                .version(1L)
                .userId("USER-001")
                .build();

        Set<ConstraintViolation<ConfirmReservationRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
        assertThat(request.getQuantity()).isEqualTo(10);
        assertThat(request.getReservationId()).isEqualTo("RES-001");
        assertThat(request.getVersion()).isEqualTo(1L);
        assertThat(request.getUserId()).isEqualTo("USER-001");
    }

    @Test
    @DisplayName("Debe fallar cuando quantity es nulo")
    void shouldFailWhenQuantityIsNull() {
        // Arrange
        ConfirmReservationRequest request = ConfirmReservationRequest.builder()
                .quantity(null)
                .reservationId("RES-001")
                .version(1L)
                .build();

        // Act
        Set<ConstraintViolation<ConfirmReservationRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Quantity is required");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    @DisplayName("Debe fallar cuando quantity no es positivo")
    void shouldFailWhenQuantityIsNotPositive(int quantity) {
        // Arrange
        ConfirmReservationRequest request = ConfirmReservationRequest.builder()
                .quantity(quantity)
                .reservationId("RES-001")
                .version(1L)
                .build();

        // Act
        Set<ConstraintViolation<ConfirmReservationRequest>> violations = validator.validate(request);

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
        ConfirmReservationRequest request = ConfirmReservationRequest.builder()
                .quantity(10)
                .reservationId(reservationId)
                .version(1L)
                .build();

        // Act
        Set<ConstraintViolation<ConfirmReservationRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Reservation ID is required");
    }

    @Test
    @DisplayName("Debe fallar cuando version es nulo")
    void shouldFailWhenVersionIsNull() {
        // Arrange
        ConfirmReservationRequest request = ConfirmReservationRequest.builder()
                .quantity(10)
                .reservationId("RES-001")
                .version(null)
                .build();

        // Act
        Set<ConstraintViolation<ConfirmReservationRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Version is required");
    }

    @Test
    @DisplayName("Debe permitir userId opcional")
    void shouldAllowOptionalUserId() {
        // Arrange
        ConfirmReservationRequest request = ConfirmReservationRequest.builder()
                .quantity(10)
                .reservationId("RES-001")
                .version(1L)
                .build(); // Sin userId

        // Act
        Set<ConstraintViolation<ConfirmReservationRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
        assertThat(request.getUserId()).isNull();
    }
}