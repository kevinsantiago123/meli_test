package com.meli.inventory.command.service.infrastructure.rest.dto;

import com.meli.inventory.command.service.domain.model.InventoryItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("InventoryItemResponse - DTO Tests")
class InventoryItemResponseTest {

    @Test
    @DisplayName("Debe crear response desde InventoryItem")
    void shouldCreateResponseFromInventoryItem() {
        // Arrange
        InventoryItem item = InventoryItem.create(
                "STORE-001",
                "PROD-001",
                "Test Product",
                100,
                10,
                "USER-001"
        );
        String message = "Success";

        // Act
        InventoryItemResponse response = InventoryItemResponse.from(item, message);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(item.getId());
        assertThat(response.getStoreId()).isEqualTo("STORE-001");
        assertThat(response.getProductId()).isEqualTo("PROD-001");
        assertThat(response.getProductName()).isEqualTo("Test Product");
        assertThat(response.getQuantity()).isEqualTo(100);
        assertThat(response.getReservedQuantity()).isEqualTo(0);
        assertThat(response.getAvailableStock()).isEqualTo(100);
        assertThat(response.getMinThreshold()).isEqualTo(10);
        assertThat(response.getVersion()).isEqualTo(1L);
        assertThat(response.getLastModifiedBy()).isEqualTo("USER-001");
        assertThat(response.getMessage()).isEqualTo("Success");
        assertThat(response.getBelowThreshold()).isFalse();
    }

    @Test
    @DisplayName("Debe incluir availableStock calculado")
    void shouldIncludeCalculatedAvailableStock() {
        // Arrange
        InventoryItem item = InventoryItem.create(
                "STORE-001",
                "PROD-001",
                "Test Product",
                100,
                10,
                "USER-001"
        );
        item.reserveStock(30, "USER-001");

        // Act
        InventoryItemResponse response = InventoryItemResponse.from(item, "Test");

        // Assert
        assertThat(response.getQuantity()).isEqualTo(100);
        assertThat(response.getReservedQuantity()).isEqualTo(30);
        assertThat(response.getAvailableStock()).isEqualTo(70);
    }

    @Test
    @DisplayName("Debe indicar cuando está por debajo del threshold")
    void shouldIndicateWhenBelowThreshold() {
        // Arrange
        InventoryItem item = InventoryItem.create(
                "STORE-001",
                "PROD-001",
                "Test Product",
                5,
                50,
                "USER-001"
        );

        // Act
        InventoryItemResponse response = InventoryItemResponse.from(item, "Test");

        // Assert
        assertThat(response.getBelowThreshold()).isTrue();
    }

    @Test
    @DisplayName("Debe crear con builder")
    void shouldCreateWithBuilder() {
        // Act
        InventoryItemResponse response = InventoryItemResponse.builder()
                .id("ITEM-001")
                .storeId("STORE-001")
                .productId("PROD-001")
                .productName("Test")
                .quantity(100)
                .reservedQuantity(20)
                .availableStock(80)
                .minThreshold(10)
                .version(1L)
                .message("Success")
                .belowThreshold(false)
                .build();

        // Assert
        assertThat(response.getId()).isEqualTo("ITEM-001");
        assertThat(response.getAvailableStock()).isEqualTo(80);
        assertThat(response.getMessage()).isEqualTo("Success");
    }

    @Test
    @DisplayName("Debe usar getters y setters correctamente")
    void shouldUseGettersAndSetters() {
        // Arrange
        InventoryItemResponse response = new InventoryItemResponse();

        // Act
        response.setId("ITEM-001");
        response.setStoreId("STORE-001");
        response.setQuantity(100);
        response.setMessage("Test message");

        // Assert
        assertThat(response.getId()).isEqualTo("ITEM-001");
        assertThat(response.getStoreId()).isEqualTo("STORE-001");
        assertThat(response.getQuantity()).isEqualTo(100);
        assertThat(response.getMessage()).isEqualTo("Test message");
    }
}