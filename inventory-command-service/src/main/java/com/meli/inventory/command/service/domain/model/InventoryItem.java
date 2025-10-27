package com.meli.inventory.command.service.domain.model;


import com.meli.inventory.command.service.domain.exception.InsufficientStockException;
import com.meli.inventory.command.service.domain.exception.OptimisticLockException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain Entity - Inventory Item
 * Representa un producto en el inventario de una tienda específica
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {

    /**
     * Identificador único del item de inventario
     */
    private String id;

    /**
     * Identificador de la tienda
     */
    private String storeId;

    /**
     * Identificador del producto
     */
    private String productId;

    /**
     * Nombre del producto
     */
    private String productName;

    /**
     * Cantidad disponible en stock
     */
    private Integer quantity;

    /**
     * Cantidad reservada (en proceso de compra)
     */
    private Integer reservedQuantity;

    /**
     * Umbral mínimo de stock (para alertas)
     */
    private Integer minThreshold;

    /**
     * Versión para Optimistic Locking
     * Se incrementa en cada actualización
     */
    private Long version;

    /**
     * Timestamp de última actualización
     */
    private LocalDateTime lastUpdated;

    /**
     * Usuario que realizó la última modificación
     */
    private String lastModifiedBy;

    /**
     * Crea un nuevo item de inventario con valores iniciales
     */
    public static InventoryItem create(String storeId, String productId,
                                       String productName, Integer quantity,
                                       Integer minThreshold, String createdBy) {
        return InventoryItem.builder()
                .id(UUID.randomUUID().toString())
                .storeId(storeId)
                .productId(productId)
                .productName(productName)
                .quantity(quantity)
                .reservedQuantity(0)
                .minThreshold(minThreshold)
                .version(1L)
                .lastUpdated(LocalDateTime.now())
                .lastModifiedBy(createdBy)
                .build();
    }

    /**
     * Actualiza la cantidad de stock
     * @throws InsufficientStockException si la cantidad resultante es negativa
     */
    public void updateQuantity(Integer newQuantity, String modifiedBy) {
        if (newQuantity < 0) {
            throw new InsufficientStockException(
                    "Stock cannot be negative. Attempted: " + newQuantity
            );
        }

        if (newQuantity < this.reservedQuantity) {
            throw new InsufficientStockException(
                    "Available stock cannot be less than reserved quantity. " +
                            "Reserved: " + this.reservedQuantity + ", Attempted: " + newQuantity
            );
        }

        this.quantity = newQuantity;
        this.version++;
        this.lastUpdated = LocalDateTime.now();
        this.lastModifiedBy = modifiedBy;
    }

    /**
     * Incrementa el stock (reabastecimiento)
     */
    public void addStock(Integer amount, String modifiedBy) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to add must be positive");
        }
        updateQuantity(this.quantity + amount, modifiedBy);
    }

    /**
     * Reduce el stock (venta)
     */
    public void reduceStock(Integer amount, String modifiedBy) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to reduce must be positive");
        }

        int availableStock = this.quantity - this.reservedQuantity;
        if (amount > availableStock) {
            throw new InsufficientStockException(
                    "Insufficient available stock. Available: " + availableStock +
                            ", Requested: " + amount
            );
        }

        updateQuantity(this.quantity - amount, modifiedBy);
    }

    /**
     * Reserva stock para una orden
     */
    public void reserveStock(Integer amount, String modifiedBy) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to reserve must be positive");
        }

        int availableStock = this.quantity - this.reservedQuantity;
        if (amount > availableStock) {
            throw new InsufficientStockException(
                    "Insufficient stock to reserve. Available: " + availableStock +
                            ", Requested: " + amount
            );
        }

        this.reservedQuantity += amount;
        this.version++;
        this.lastUpdated = LocalDateTime.now();
        this.lastModifiedBy = modifiedBy;
    }

    /**
     * Libera stock reservado (cancelación de orden)
     */
    public void releaseReservedStock(Integer amount, String modifiedBy) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to release must be positive");
        }

        if (amount > this.reservedQuantity) {
            throw new IllegalArgumentException(
                    "Cannot release more than reserved. Reserved: " + this.reservedQuantity +
                            ", Attempted: " + amount
            );
        }

        this.reservedQuantity -= amount;
        this.version++;
        this.lastUpdated = LocalDateTime.now();
        this.lastModifiedBy = modifiedBy;
    }

    /**
     * Confirma una reserva (convierte reservado en vendido)
     */
    public void confirmReservation(Integer amount, String modifiedBy) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to confirm must be positive");
        }

        if (amount > this.reservedQuantity) {
            throw new IllegalArgumentException(
                    "Cannot confirm more than reserved. Reserved: " + this.reservedQuantity +
                            ", Attempted: " + amount
            );
        }

        this.reservedQuantity -= amount;
        updateQuantity(this.quantity - amount, modifiedBy);
    }

    /**
     * Verifica si el stock está por debajo del umbral mínimo
     */
    public boolean isBelowThreshold() {
        return getAvailableStock() < this.minThreshold;
    }

    /**
     * Calcula el stock disponible para venta (no reservado)
     */
    public Integer getAvailableStock() {
        return this.quantity - this.reservedQuantity;
    }

    /**
     * Valida la versión para Optimistic Locking
     */
    public void validateVersion(Long expectedVersion) {
        if (!this.version.equals(expectedVersion)) {
            throw new OptimisticLockException(
                    "Version mismatch. Expected: " + expectedVersion +
                            ", Current: " + this.version
            );
        }
    }
}
