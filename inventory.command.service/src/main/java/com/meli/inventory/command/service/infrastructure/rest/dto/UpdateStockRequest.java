package com.meli.inventory.command.service.infrastructure.rest.dto;

import com.meli.inventory.command.service.domain.port.in.UpdateStockCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStockRequest {
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @NotNull(message = "Operation is required")
    private UpdateStockCommand.StockOperation operation;

    @NotNull(message = "Version is required")
    private Long version;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String userId;
}
