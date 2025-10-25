package com.meli.inventory.command.service.domain.port.in;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command para reservar stock
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReserveStockCommand {

    @NotBlank(message = "Item ID is required")
    private String itemId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be positive")
    private Integer quantity;

    @NotBlank(message = "Reservation ID is required")
    private String reservationId;

    @NotNull(message = "Version is required for optimistic locking")
    private Long version;

    @NotBlank(message = "User ID is required")
    private String userId;
}
