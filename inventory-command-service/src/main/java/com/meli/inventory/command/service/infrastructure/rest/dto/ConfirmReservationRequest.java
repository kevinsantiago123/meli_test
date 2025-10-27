package com.meli.inventory.command.service.infrastructure.rest.dto;

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
public class ConfirmReservationRequest {
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be positive")
    private Integer quantity;

    @NotBlank(message = "Reservation ID is required")
    private String reservationId;

    @NotNull(message = "Version is required")
    private Long version;

    private String userId;
}
