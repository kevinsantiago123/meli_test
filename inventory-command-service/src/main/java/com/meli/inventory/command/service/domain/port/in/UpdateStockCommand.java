package com.meli.inventory.command.service.domain.port.in;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Command para actualizar stock
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStockCommand {

    @NotBlank(message = "Item ID is required")
    private String itemId;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @NotNull(message = "Operation is required")
    private StockOperation operation;

    @NotNull(message = "Version is required for optimistic locking")
    private Long version;

    @NotBlank(message = "Reason is required")
    private String reason;

    @NotBlank(message = "User ID is required")
    private String userId;

    public enum StockOperation {
        SET,      // Establecer cantidad exacta
        ADD,      // Incrementar cantidad
        SUBTRACT  // Decrementar cantidad
    }
}
