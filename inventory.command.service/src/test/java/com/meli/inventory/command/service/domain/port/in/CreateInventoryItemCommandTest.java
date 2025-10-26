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

@DisplayName("CreateInventoryItemCommand - Validation Tests")
class CreateInventoryItemCommandTest {

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
        CreateInventoryItemCommand command = CreateInventoryItemCommand.builder()
                .storeId("STORE-001")
                .productId("PROD-001")
                .productName("Test Product")
                .quantity(100)
                .minThreshold(10)
                .userId("USER-001")
                .build();

        Set<ConstraintViolation<CreateInventoryItemCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isEmpty();
        assertThat(command.getStoreId()).isEqualTo("STORE-001");
        assertThat(command.getProductId()).isEqualTo("PROD-001");
        assertThat(command.getProductName()).isEqualTo("Test Product");
        assertThat(command.getQuantity()).isEqualTo(100);
        assertThat(command.getMinThreshold()).isEqualTo(10);
        assertThat(command.getUserId()).isEqualTo("USER-001");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    @DisplayName("Debe fallar cuando storeId es nulo, vacío o blanco")
    void shouldFailWhenStoreIdIsInvalid(String storeId) {
        // Arrange
        CreateInventoryItemCommand command = CreateInventoryItemCommand.builder()
                .storeId(storeId)
                .productId("PROD-001")
                .productName("Test Product")
                .quantity(100)
                .minThreshold(10)
                .userId("USER-001")
                .build();

        // Act
        Set<ConstraintViolation<CreateInventoryItemCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Store ID is required");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    @DisplayName("Debe fallar cuando productId es nulo, vacío o blanco")
    void shouldFailWhenProductIdIsInvalid(String productId) {
        // Arrange
        CreateInventoryItemCommand command = CreateInventoryItemCommand.builder()
                .storeId("STORE-001")
                .productId(productId)
                .productName("Test Product")
                .quantity(100)
                .minThreshold(10)
                .userId("USER-001")
                .build();

        // Act
        Set<ConstraintViolation<CreateInventoryItemCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Product ID is required");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    @DisplayName("Debe fallar cuando productName es nulo, vacío o blanco")
    void shouldFailWhenProductNameIsInvalid(String productName) {
        // Arrange
        CreateInventoryItemCommand command = CreateInventoryItemCommand.builder()
                .storeId("STORE-001")
                .productId("PROD-001")
                .productName(productName)
                .quantity(100)
                .minThreshold(10)
                .userId("USER-001")
                .build();

        // Act
        Set<ConstraintViolation<CreateInventoryItemCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Product name is required");
    }

    @Test
    @DisplayName("Debe fallar cuando quantity es nulo")
    void shouldFailWhenQuantityIsNull() {
        // Arrange
        CreateInventoryItemCommand command = CreateInventoryItemCommand.builder()
                .storeId("STORE-001")
                .productId("PROD-001")
                .productName("Test Product")
                .quantity(null)
                .minThreshold(10)
                .userId("USER-001")
                .build();

        // Act
        Set<ConstraintViolation<CreateInventoryItemCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Quantity is required");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -10, -100})
    @DisplayName("Debe fallar cuando quantity es negativo")
    void shouldFailWhenQuantityIsNegative(int quantity) {
        // Arrange
        CreateInventoryItemCommand command = CreateInventoryItemCommand.builder()
                .storeId("STORE-001")
                .productId("PROD-001")
                .productName("Test Product")
                .quantity(quantity)
                .minThreshold(10)
                .userId("USER-001")
                .build();

        // Act
        Set<ConstraintViolation<CreateInventoryItemCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Quantity cannot be negative");
    }

    @Test
    @DisplayName("Debe aceptar quantity en cero")
    void shouldAcceptZeroQuantity() {
        // Arrange
        CreateInventoryItemCommand command = CreateInventoryItemCommand.builder()
                .storeId("STORE-001")
                .productId("PROD-001")
                .productName("Test Product")
                .quantity(0)
                .minThreshold(10)
                .userId("USER-001")
                .build();

        // Act
        Set<ConstraintViolation<CreateInventoryItemCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Debe fallar cuando minThreshold es nulo")
    void shouldFailWhenMinThresholdIsNull() {
        // Arrange
        CreateInventoryItemCommand command = CreateInventoryItemCommand.builder()
                .storeId("STORE-001")
                .productId("PROD-001")
                .productName("Test Product")
                .quantity(100)
                .minThreshold(null)
                .userId("USER-001")
                .build();

        // Act
        Set<ConstraintViolation<CreateInventoryItemCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Min threshold is required");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -10, -100})
    @DisplayName("Debe fallar cuando minThreshold es negativo")
    void shouldFailWhenMinThresholdIsNegative(int minThreshold) {
        // Arrange
        CreateInventoryItemCommand command = CreateInventoryItemCommand.builder()
                .storeId("STORE-001")
                .productId("PROD-001")
                .productName("Test Product")
                .quantity(100)
                .minThreshold(minThreshold)
                .userId("USER-001")
                .build();

        // Act
        Set<ConstraintViolation<CreateInventoryItemCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Min threshold cannot be negative");
    }

    @Test
    @DisplayName("Debe aceptar minThreshold en cero")
    void shouldAcceptZeroMinThreshold() {
        // Arrange
        CreateInventoryItemCommand command = CreateInventoryItemCommand.builder()
                .storeId("STORE-001")
                .productId("PROD-001")
                .productName("Test Product")
                .quantity(100)
                .minThreshold(0)
                .userId("USER-001")
                .build();

        // Act
        Set<ConstraintViolation<CreateInventoryItemCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    @DisplayName("Debe fallar cuando userId es nulo, vacío o blanco")
    void shouldFailWhenUserIdIsInvalid(String userId) {
        // Arrange
        CreateInventoryItemCommand command = CreateInventoryItemCommand.builder()
                .storeId("STORE-001")
                .productId("PROD-001")
                .productName("Test Product")
                .quantity(100)
                .minThreshold(10)
                .userId(userId)
                .build();

        // Act
        Set<ConstraintViolation<CreateInventoryItemCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("User ID is required");
    }

    @Test
    @DisplayName("Debe crear con @NoArgsConstructor")
    void shouldCreateWithNoArgsConstructor() {
        // Act
        CreateInventoryItemCommand command = new CreateInventoryItemCommand();

        // Assert
        assertThat(command).isNotNull();
    }

    @Test
    @DisplayName("Debe crear con @AllArgsConstructor")
    void shouldCreateWithAllArgsConstructor() {
        // Act
        CreateInventoryItemCommand command = new CreateInventoryItemCommand(
                "STORE-001",
                "PROD-001",
                "Test Product",
                100,
                10,
                "USER-001"
        );

        // Assert
        assertThat(command.getStoreId()).isEqualTo("STORE-001");
        assertThat(command.getProductId()).isEqualTo("PROD-001");
        assertThat(command.getProductName()).isEqualTo("Test Product");
        assertThat(command.getQuantity()).isEqualTo(100);
        assertThat(command.getMinThreshold()).isEqualTo(10);
        assertThat(command.getUserId()).isEqualTo("USER-001");
    }

    @Test
    @DisplayName("Debe usar setters correctamente")
    void shouldUseSettersCorrectly() {
        // Arrange
        CreateInventoryItemCommand command = new CreateInventoryItemCommand();

        // Act
        command.setStoreId("STORE-002");
        command.setProductId("PROD-002");
        command.setProductName("Another Product");
        command.setQuantity(200);
        command.setMinThreshold(20);
        command.setUserId("USER-002");

        // Assert
        assertThat(command.getStoreId()).isEqualTo("STORE-002");
        assertThat(command.getProductId()).isEqualTo("PROD-002");
        assertThat(command.getProductName()).isEqualTo("Another Product");
        assertThat(command.getQuantity()).isEqualTo(200);
        assertThat(command.getMinThreshold()).isEqualTo(20);
        assertThat(command.getUserId()).isEqualTo("USER-002");
    }
}