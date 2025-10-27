package com.meli.inventory.command.service.domain.port.in;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command para crear un nuevo item de inventario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInventoryItemCommand {

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

    @NotBlank(message = "User ID is required")
    private String userId;
}
