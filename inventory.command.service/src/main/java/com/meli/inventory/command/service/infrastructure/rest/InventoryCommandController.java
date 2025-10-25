package com.meli.inventory.command.service.infrastructure.rest;


import com.meli.inventory.command.service.domain.model.InventoryItem;
import com.meli.inventory.command.service.domain.port.in.*;
import com.meli.inventory.command.service.domain.port.in.InventoryCommandService;
import com.meli.inventory.command.service.infrastructure.rest.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operaciones de escritura (Command)
 *
 * Endpoints:
 * - POST   /api/v1/inventory              - Crear item
 * - PUT    /api/v1/inventory/{id}/stock   - Actualizar stock
 * - POST   /api/v1/inventory/{id}/reserve - Reservar stock
 * - POST   /api/v1/inventory/{id}/release - Liberar reserva
 * - POST   /api/v1/inventory/{id}/confirm - Confirmar reserva
 * - DELETE /api/v1/inventory/{id}         - Eliminar item
 */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory Command", description = "Write operations for inventory management")
public class InventoryCommandController {

    private final InventoryCommandService commandService;

    @PostMapping
    @Operation(summary = "Create new inventory item",
            description = "Creates a new product entry in the inventory for a specific store")
    @ApiResponse(responseCode = "201", description = "Item created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "409", description = "Item already exists")
    public ResponseEntity<InventoryItemResponse> createInventoryItem(
            @Valid @RequestBody CreateInventoryItemRequest request) {

        log.info("REST: Creating inventory item - Product: {}, Store: {}",
                request.getProductId(), request.getStoreId());

        CreateInventoryItemCommand command = CreateInventoryItemCommand.builder()
                .storeId(request.getStoreId())
                .productId(request.getProductId())
                .productName(request.getProductName())
                .quantity(request.getQuantity())
                .minThreshold(request.getMinThreshold())
                .userId(request.getUserId() != null ? request.getUserId() : "system")
                .build();

        InventoryItem item = commandService.createInventoryItem(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(InventoryItemResponse.from(item, "Item created successfully"));
    }

    @PutMapping("/{id}/stock")
    @Operation(summary = "Update stock quantity",
            description = "Updates the stock quantity using SET, ADD, or SUBTRACT operations")
    @ApiResponse(responseCode = "200", description = "Stock updated successfully")
    @ApiResponse(responseCode = "404", description = "Item not found")
    @ApiResponse(responseCode = "409", description = "Version conflict (Optimistic Lock)")
    public ResponseEntity<InventoryItemResponse> updateStock(
            @PathVariable String id,
            @Valid @RequestBody UpdateStockRequest request) {

        log.info("REST: Updating stock - Item: {}, Operation: {}",
                id, request.getOperation());

        UpdateStockCommand command = UpdateStockCommand.builder()
                .itemId(id)
                .quantity(request.getQuantity())
                .operation(request.getOperation())
                .version(request.getVersion())
                .reason(request.getReason())
                .userId(request.getUserId() != null ? request.getUserId() : "system")
                .build();

        InventoryItem item = commandService.updateStock(command);

        return ResponseEntity.ok(
                InventoryItemResponse.from(item, "Stock updated successfully"));
    }

    @PostMapping("/{id}/reserve")
    @Operation(summary = "Reserve stock",
            description = "Reserves stock for an order or reservation")
    @ApiResponse(responseCode = "200", description = "Stock reserved successfully")
    public ResponseEntity<InventoryItemResponse> reserveStock(
            @PathVariable String id,
            @Valid @RequestBody ReserveStockRequest request) {

        log.info("REST: Reserving stock - Item: {}, Quantity: {}",
                id, request.getQuantity());

        ReserveStockCommand command = ReserveStockCommand.builder()
                .itemId(id)
                .quantity(request.getQuantity())
                .reservationId(request.getReservationId())
                .version(request.getVersion())
                .userId(request.getUserId() != null ? request.getUserId() : "system")
                .build();

        InventoryItem item = commandService.reserveStock(command);

        return ResponseEntity.ok(
                InventoryItemResponse.from(item, "Stock reserved successfully"));
    }

    @PostMapping("/{id}/release")
    @Operation(summary = "Release reserved stock",
            description = "Releases previously reserved stock (e.g., order cancellation)")
    @ApiResponse(responseCode = "200", description = "Reservation released successfully")
    public ResponseEntity<InventoryItemResponse> releaseReservedStock(
            @PathVariable String id,
            @Valid @RequestBody ReleaseStockRequest request) {

        log.info("REST: Releasing reserved stock - Item: {}", id);

        ReleaseStockCommand command = ReleaseStockCommand.builder()
                .itemId(id)
                .quantity(request.getQuantity())
                .reservationId(request.getReservationId())
                .version(request.getVersion())
                .userId(request.getUserId() != null ? request.getUserId() : "system")
                .build();

        InventoryItem item = commandService.releaseReservedStock(command);

        return ResponseEntity.ok(
                InventoryItemResponse.from(item, "Reservation released successfully"));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm reservation",
            description = "Confirms a reservation and reduces actual stock")
    @ApiResponse(responseCode = "200", description = "Reservation confirmed successfully")
    public ResponseEntity<InventoryItemResponse> confirmReservation(
            @PathVariable String id,
            @Valid @RequestBody ConfirmReservationRequest request) {

        log.info("REST: Confirming reservation - Item: {}", id);

        ConfirmReservationCommand command = ConfirmReservationCommand.builder()
                .itemId(id)
                .quantity(request.getQuantity())
                .reservationId(request.getReservationId())
                .version(request.getVersion())
                .userId(request.getUserId() != null ? request.getUserId() : "system")
                .build();

        InventoryItem item = commandService.confirmReservation(command);

        return ResponseEntity.ok(
                InventoryItemResponse.from(item, "Reservation confirmed successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete inventory item",
            description = "Deletes an inventory item")
    @ApiResponse(responseCode = "204", description = "Item deleted successfully")
    @ApiResponse(responseCode = "404", description = "Item not found")
    public ResponseEntity<Void> deleteInventoryItem(
            @PathVariable String id,
            @RequestParam(required = false) String userId) {

        log.info("REST: Deleting inventory item - ID: {}", id);

        commandService.deleteInventoryItem(id, userId != null ? userId : "system");

        return ResponseEntity.noContent().build();
    }
}
