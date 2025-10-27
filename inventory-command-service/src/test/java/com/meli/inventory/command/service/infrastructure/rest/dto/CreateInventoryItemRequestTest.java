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

@DisplayName("CreateInventoryItemRequest - DTO Tests")
class CreateInventoryItemRequestTest {

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
        CreateInventoryItemRequest request = CreateInventoryItemRequest.builder()
                .storeId("STORE-001")
                .productId("PROD-001")
                .productName("Test Product")
                .quantity(100)
                .minThreshold(10)
                .userId("USER-001")
                .build();

        Set<ConstraintViolation<CreateInventoryItemRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
        assertThat(request.getStoreId()).isEqualTo("STORE-001");
        assertThat(request.getProductId()).isEqualTo("PROD-001");
        assertThat(request.getProductName()).isEqualTo("Test Product");
        assertThat(request.getQuantity()).isEqualTo(100);
        assertThat(request.getMinThreshold()).isEqualTo(10);
        assertThat(request.getUserId()).isEqualTo("USER-001");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    @DisplayName("Debe fallar cuando storeId es inválido")
    void shouldFailWhenStoreIdIsInvalid(String storeId) {
        // Arrange
        CreateInventoryItemRequest request = CreateInventoryItemRequest.builder()
                .storeId(storeId)
                .productId("PROD-001")
                .productName("Test Product")
                .quantity(100)
                .minThreshold(10)
                .build();

        // Act
        Set<ConstraintViolation<CreateInventoryItemRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Store ID is required");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    @DisplayName("Debe fallar cuando productId es inválido")
    void shouldFailWhenProductIdIsInvalid(String productId) {
        // Arrange
        CreateInventoryItemRequest request = CreateInventoryItemRequest.builder()
                .storeId("STORE-001")
                .productId(productId)
                .productName("Test Product")
                .quantity(100)
                .minThreshold(10)
                .build();

        // Act
        Set<ConstraintViolation<CreateInventoryItemRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Product ID is required");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    @DisplayName("Debe fallar cuando productName es inválido")
    void shouldFailWhenProductNameIsInvalid(String productName) {
        // Arrange
        CreateInventoryItemRequest request = CreateInventoryItemRequest.builder()
                .storeId("STORE-001")
                .productId("PROD-001")
                .productName(productName)
                .quantity(100)
                .minThreshold(10)
                .build();

        // Act
        Set<ConstraintViolation<CreateInventoryItemRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Product name is required");
    }

    @Test
    @DisplayName("Debe fallar cuando quantity es nulo")
    void shouldFailWhenQuantityIsNull() {
        // Arrange
        CreateInventoryItemRequest request = CreateInventoryItemRequest.builder()
                .storeId("STORE-001")
                .productId("PROD-001")
                .productName("Test Product")
                .quantity(null)
                .minThreshold(10)
                .build();

        // Act
        Set<ConstraintViolation<CreateInventoryItemRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Quantity is required");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -10, -100})
    @DisplayName("Debe fallar cuando quantity es negativo")
    void shouldFailWhenQuantityIsNegative(int quantity) {
        // Arrange
        CreateInventoryItemRequest request = CreateInventoryItemRequest.builder()
                .storeId("STORE-001")
                .productId("PROD-001")
                .productName("Test Product")
                .quantity(quantity)
                .minThreshold(10)
                .build();

        // Act
        Set<ConstraintViolation<CreateInventoryItemRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Quantity cannot be negative");
    }

    @Test
    @DisplayName("Debe aceptar quantity en cero")
    void shouldAcceptZeroQuantity() {
        // Arrange
        CreateInventoryItemRequest request = CreateInventoryItemRequest.builder()
                .storeId("STORE-001")
                .productId("PROD-001")
                .productName("Test Product")
                .quantity(0)
                .minThreshold(10)
                .build();

        // Act
        Set<ConstraintViolation<CreateInventoryItemRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Debe fallar cuando minThreshold es nulo")
    void shouldFailWhenMinThresholdIsNull() {
        // Arrange
        CreateInventoryItemRequest request = CreateInventoryItemRequest.builder()
                .storeId("STORE-001")
                .productId("PROD-001")
                .productName("Test Product")
                .quantity(100)
                .minThreshold(null)
                .build();

        // Act
        Set<ConstraintViolation<CreateInventoryItemRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Min threshold is required");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -10})
    @DisplayName("Debe fallar cuando minThreshold es negativo")
    void shouldFailWhenMinThresholdIsNegative(int minThreshold) {
        // Arrange
        CreateInventoryItemRequest request = CreateInventoryItemRequest.builder()
                .storeId("STORE-001")
                .productId("PROD-001")
                .productName("Test Product")
                .quantity(100)
                .minThreshold(minThreshold)
                .build();

        // Act
        Set<ConstraintViolation<CreateInventoryItemRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Min threshold cannot be negative");
    }

    @Test
    @DisplayName("Debe aceptar minThreshold en cero")
    void shouldAcceptZeroMinThreshold() {
        // Arrange
        CreateInventoryItemRequest request = CreateInventoryItemRequest.builder()
                .storeId("STORE-001")
                .productId("PROD-001")
                .productName("Test Product")
                .quantity(100)
                .minThreshold(0)
                .build();

        // Act
        Set<ConstraintViolation<CreateInventoryItemRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Debe permitir userId opcional")
    void shouldAllowOptionalUserId() {
        // Arrange
        CreateInventoryItemRequest request = CreateInventoryItemRequest.builder()
                .storeId("STORE-001")
                .productId("PROD-001")
                .productName("Test Product")
                .quantity(100)
                .minThreshold(10)
                .build();

        // Act
        Set<ConstraintViolation<CreateInventoryItemRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
        assertThat(request.getUserId()).isNull();
    }
}