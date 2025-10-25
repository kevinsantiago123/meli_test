package com.meli.inventory.query.service.domain.exception;


public class InventoryItemNotFoundException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado
     *
     * @param message Mensaje de error
     */
    public InventoryItemNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor para item no encontrado por producto y tienda
     *
     * @param productId ID del producto
     * @param storeId ID de la tienda
     */
    public InventoryItemNotFoundException(String productId, String storeId) {
        super(String.format("Inventory item not found. Product: %s, Store: %s", productId, storeId));
    }
}
