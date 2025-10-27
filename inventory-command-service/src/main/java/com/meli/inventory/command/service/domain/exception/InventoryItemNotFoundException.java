package com.meli.inventory.command.service.domain.exception;

/**
 * Excepción lanzada cuando no se encuentra un item de inventario
 */
public class InventoryItemNotFoundException extends RuntimeException {
    public InventoryItemNotFoundException(String message) {
        super(message);
    }

    public InventoryItemNotFoundException(String id, String storeId) {
        super(String.format("Inventory item not found. ID: %s, Store: %s", id, storeId));
    }
}
