package com.meli.inventory.command.service.domain.exception;

/**
 * Excepción lanzada cuando ya existe un item duplicado
 */
public class DuplicateInventoryItemException extends RuntimeException {
    public DuplicateInventoryItemException(String message) {
        super(message);
    }

    public DuplicateInventoryItemException(String productId, String storeId) {
        super(String.format("Inventory item already exists. Product: %s, Store: %s",
                productId, storeId));
    }
}
