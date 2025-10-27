package com.meli.inventory.query.service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Proyección de lectura - InventoryProjection
 * Modelo optimizado para consultas (CQRS Query Side).
 * Contiene datos agregados y calculados para mejorar performance de lectura.
 * Diferencias con InventoryItem (Command Side):
 * - Sin lógica de negocio (solo lectura)
 * - Campos adicionales calculados (availableStock, belowThreshold)
 * - Optimizado para queries frecuentes
 * - Se actualiza mediante eventos del Command Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryProjection {

    /**
     * Identificador único del item
     */
    private String id;

    /**
     * Identificador de la tienda
     */
    private String storeId;

    /**
     * Nombre de la tienda (desnormalizado para evitar joins)
     */
    private String storeName;

    /**
     * Identificador del producto
     */
    private String productId;

    /**
     * Nombre del producto
     */
    private String productName;

    /**
     * Categoría del producto (opcional, para filtros)
     */
    private String category;

    /**
     * Cantidad total en inventario
     */
    private Integer quantity;

    /**
     * Cantidad reservada (en proceso de compra)
     */
    private Integer reservedQuantity;

    /**
     * Stock disponible para venta (calculado: quantity - reserved)
     */
    private Integer availableStock;

    /**
     * Umbral mínimo de stock
     */
    private Integer minThreshold;

    /**
     * Indica si está por debajo del umbral (calculado)
     */
    private Boolean belowThreshold;

    /**
     * Versión actual (Optimistic Locking)
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
     * Estado del item (ACTIVE, DISCONTINUED, OUT_OF_STOCK)
     */
    private ItemStatus status;

    /**
     * Precio unitario (opcional, para cálculos)
     */
    private Double unitPrice;

    /**
     * Calcula campos derivados
     */
    public void calculateDerivedFields() {
        this.availableStock = this.quantity - this.reservedQuantity;
        this.belowThreshold = this.availableStock < this.minThreshold;

        if (this.availableStock <= 0) {
            this.status = ItemStatus.OUT_OF_STOCK;
        } else if (this.belowThreshold) {
            this.status = ItemStatus.LOW_STOCK;
        } else {
            this.status = ItemStatus.ACTIVE;
        }
    }

    /**
     * Verifica si el item está disponible para venta
     */
    public boolean isAvailableForSale() {
        return this.availableStock > 0 && this.status == ItemStatus.ACTIVE;
    }

    /**
     * Estados posibles del item
     */
    public enum ItemStatus {
        ACTIVE,
        LOW_STOCK,
        OUT_OF_STOCK,
        DISCONTINUED
    }
}
