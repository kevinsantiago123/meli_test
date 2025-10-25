package com.meli.inventory.command.service.domain.exception;

/**
 * Excepción lanzada cuando no hay suficiente stock disponible
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}

