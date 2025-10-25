package com.meli.inventory.command.service.infrastructure.rest.dto;

import com.meli.inventory.command.service.domain.model.InventoryItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemResponse {
    private String id;
    private String storeId;
    private String productId;
    private String productName;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableStock;
    private Integer minThreshold;
    private Long version;
    private LocalDateTime lastUpdated;
    private String lastModifiedBy;
    private String message;
    private Boolean belowThreshold;

    public static InventoryItemResponse from(InventoryItem item, String message) {
        return InventoryItemResponse.builder()
                .id(item.getId())
                .storeId(item.getStoreId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .reservedQuantity(item.getReservedQuantity())
                .availableStock(item.getAvailableStock())
                .minThreshold(item.getMinThreshold())
                .version(item.getVersion())
                .lastUpdated(item.getLastUpdated())
                .lastModifiedBy(item.getLastModifiedBy())
                .message(message)
                .belowThreshold(item.isBelowThreshold())
                .build();
    }
}
