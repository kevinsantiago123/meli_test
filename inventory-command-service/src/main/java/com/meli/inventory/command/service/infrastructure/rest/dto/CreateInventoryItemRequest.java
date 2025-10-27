package com.meli.inventory.command.service.infrastructure.rest.dto;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInventoryItemRequest {
    @NotBlank(message = "Store ID is required")
    private String storeId;

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @NotNull(message = "Min threshold is required")
    @Min(value = 0, message = "Min threshold cannot be negative")
    private Integer minThreshold;

    private String userId;
}
