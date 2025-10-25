package com.meli.inventory.command.service.domain.exception;

/**
 * Excepción lanzada cuando hay un conflicto de versión (Optimistic Locking)
 */
public class OptimisticLockException extends RuntimeException {
    public OptimisticLockException(String message) {
        super(message);
    }
}
