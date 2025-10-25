package com.meli.inventory.command.service.application;

import com.meli.inventory.command.service.domain.exception.DuplicateInventoryItemException;
import com.meli.inventory.command.service.domain.exception.InventoryItemNotFoundException;
import com.meli.inventory.command.service.domain.model.InventoryEvent;
import com.meli.inventory.command.service.domain.model.InventoryItem;
import com.meli.inventory.command.service.domain.port.in.*;
import com.meli.inventory.command.service.domain.port.in.InventoryCommandService;
import com.meli.inventory.command.service.domain.port.out.EventPublisher;
import com.meli.inventory.command.service.domain.port.out.EventRepository;
import com.meli.inventory.command.service.domain.port.out.InventoryRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio de Aplicación - Implementa los casos de uso de inventario
 *
 * Responsabilidades:
 * - Orquestar llamadas entre dominio y adaptadores
 * - Aplicar patrones de resiliencia (Circuit Breaker, Retry)
 * - Publicar eventos
 * - Logging y métricas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryCommandServiceImpl implements InventoryCommandService {

    private final InventoryRepository inventoryRepository;
    private final EventRepository eventRepository;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    @CircuitBreaker(name = "inventoryCommand", fallbackMethod = "createInventoryItemFallback")
    @Retry(name = "inventoryCommand")
    @Bulkhead(name = "inventoryCommand")
    public InventoryItem createInventoryItem(CreateInventoryItemCommand command) {
        log.info("Creating inventory item - Product: {}, Store: {}",
                command.getProductId(), command.getStoreId());

        // Verificar duplicados
        if (inventoryRepository.existsByProductIdAndStoreId(
                command.getProductId(), command.getStoreId())) {
            throw new DuplicateInventoryItemException(
                    command.getProductId(), command.getStoreId());
        }

        // Crear item usando lógica de dominio
        InventoryItem item = InventoryItem.create(
                command.getStoreId(),
                command.getProductId(),
                command.getProductName(),
                command.getQuantity(),
                command.getMinThreshold(),
                command.getUserId()
        );

        // Persistir
        InventoryItem savedItem = inventoryRepository.save(item);

        // Crear y publicar evento
        Map<String, Object> payload = new HashMap<>();
        payload.put("productId", item.getProductId());
        payload.put("productName", item.getProductName());
        payload.put("quantity", item.getQuantity());
        payload.put("minThreshold", item.getMinThreshold());

        InventoryEvent event = InventoryEvent.create(
                InventoryEvent.EventType.ITEM_CREATED,
                item.getId(),
                item.getStoreId(),
                command.getUserId(),
                payload,
                item.getVersion()
        );

        eventRepository.save(event);
        eventPublisher.publish(event);

        log.info("Inventory item created successfully - ID: {}", savedItem.getId());
        return savedItem;
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "inventoryCommand", fallbackMethod = "updateStockFallback")
    @Retry(name = "inventoryCommand")
    @Bulkhead(name = "inventoryCommand")
    public InventoryItem updateStock(UpdateStockCommand command) {
        log.info("Updating stock - Item: {}, Operation: {}, Quantity: {}",
                command.getItemId(), command.getOperation(), command.getQuantity());

        // Buscar item
        InventoryItem item = inventoryRepository.findById(command.getItemId())
                .orElseThrow(() -> new InventoryItemNotFoundException(
                        "Item not found: " + command.getItemId()));

        // Validar versión (Optimistic Locking)
        item.validateVersion(command.getVersion());

        // Guardar valores anteriores para el evento
        Integer previousQuantity = item.getQuantity();

        // Aplicar operación según tipo
        switch (command.getOperation()) {
            case SET:
                item.updateQuantity(command.getQuantity(), command.getUserId());
                break;
            case ADD:
                item.addStock(command.getQuantity(), command.getUserId());
                break;
            case SUBTRACT:
                item.reduceStock(command.getQuantity(), command.getUserId());
                break;
        }

        // Persistir
        InventoryItem updatedItem = inventoryRepository.save(item);

        // Crear y publicar evento
        Map<String, Object> payload = new HashMap<>();
        payload.put("previousQuantity", previousQuantity);
        payload.put("newQuantity", updatedItem.getQuantity());
        payload.put("operation", command.getOperation().name());
        payload.put("reason", command.getReason());

        InventoryEvent event = InventoryEvent.create(
                InventoryEvent.EventType.STOCK_UPDATED,
                updatedItem.getId(),
                updatedItem.getStoreId(),
                command.getUserId(),
                payload,
                updatedItem.getVersion()
        );

        eventRepository.save(event);
        eventPublisher.publish(event);

        // Verificar y publicar alerta de stock bajo si aplica
        if (updatedItem.isBelowThreshold()) {
            publishLowStockAlert(updatedItem, command.getUserId());
        }

        log.info("Stock updated successfully - ID: {}, New quantity: {}",
                updatedItem.getId(), updatedItem.getQuantity());

        return updatedItem;
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "inventoryCommand", fallbackMethod = "reserveStockFallback")
    @Retry(name = "inventoryCommand")
    @Bulkhead(name = "inventoryCommand")
    public InventoryItem reserveStock(ReserveStockCommand command) {
        log.info("Reserving stock - Item: {}, Quantity: {}, Reservation: {}",
                command.getItemId(), command.getQuantity(), command.getReservationId());

        InventoryItem item = inventoryRepository.findById(command.getItemId())
                .orElseThrow(() -> new InventoryItemNotFoundException(
                        "Item not found: " + command.getItemId()));

        item.validateVersion(command.getVersion());

        Integer previousReserved = item.getReservedQuantity();
        item.reserveStock(command.getQuantity(), command.getUserId());

        InventoryItem updatedItem = inventoryRepository.save(item);

        Map<String, Object> payload = new HashMap<>();
        payload.put("reservationId", command.getReservationId());
        payload.put("quantity", command.getQuantity());
        payload.put("previousReserved", previousReserved);
        payload.put("newReserved", updatedItem.getReservedQuantity());

        InventoryEvent event = InventoryEvent.create(
                InventoryEvent.EventType.STOCK_RESERVED,
                updatedItem.getId(),
                updatedItem.getStoreId(),
                command.getUserId(),
                payload,
                updatedItem.getVersion()
        );

        eventRepository.save(event);
        eventPublisher.publish(event);

        log.info("Stock reserved successfully - ID: {}, Reserved: {}",
                updatedItem.getId(), updatedItem.getReservedQuantity());

        return updatedItem;
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "inventoryCommand")
    @Retry(name = "inventoryCommand")
    public InventoryItem releaseReservedStock(ReleaseStockCommand command) {
        log.info("Releasing reserved stock - Item: {}, Quantity: {}",
                command.getItemId(), command.getQuantity());

        InventoryItem item = inventoryRepository.findById(command.getItemId())
                .orElseThrow(() -> new InventoryItemNotFoundException(
                        "Item not found: " + command.getItemId()));

        item.validateVersion(command.getVersion());

        Integer previousReserved = item.getReservedQuantity();
        item.releaseReservedStock(command.getQuantity(), command.getUserId());

        InventoryItem updatedItem = inventoryRepository.save(item);

        Map<String, Object> payload = new HashMap<>();
        payload.put("reservationId", command.getReservationId());
        payload.put("quantity", command.getQuantity());
        payload.put("previousReserved", previousReserved);
        payload.put("newReserved", updatedItem.getReservedQuantity());

        InventoryEvent event = InventoryEvent.create(
                InventoryEvent.EventType.RESERVATION_RELEASED,
                updatedItem.getId(),
                updatedItem.getStoreId(),
                command.getUserId(),
                payload,
                updatedItem.getVersion()
        );

        eventRepository.save(event);
        eventPublisher.publish(event);

        log.info("Reserved stock released - ID: {}", updatedItem.getId());
        return updatedItem;
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "inventoryCommand")
    @Retry(name = "inventoryCommand")
    public InventoryItem confirmReservation(ConfirmReservationCommand command) {
        log.info("Confirming reservation - Item: {}, Quantity: {}",
                command.getItemId(), command.getQuantity());

        InventoryItem item = inventoryRepository.findById(command.getItemId())
                .orElseThrow(() -> new InventoryItemNotFoundException(
                        "Item not found: " + command.getItemId()));

        item.validateVersion(command.getVersion());

        Integer previousQuantity = item.getQuantity();
        Integer previousReserved = item.getReservedQuantity();

        item.confirmReservation(command.getQuantity(), command.getUserId());

        InventoryItem updatedItem = inventoryRepository.save(item);

        Map<String, Object> payload = new HashMap<>();
        payload.put("reservationId", command.getReservationId());
        payload.put("quantity", command.getQuantity());
        payload.put("previousQuantity", previousQuantity);
        payload.put("previousReserved", previousReserved);
        payload.put("newQuantity", updatedItem.getQuantity());
        payload.put("newReserved", updatedItem.getReservedQuantity());

        InventoryEvent event = InventoryEvent.create(
                InventoryEvent.EventType.RESERVATION_CONFIRMED,
                updatedItem.getId(),
                updatedItem.getStoreId(),
                command.getUserId(),
                payload,
                updatedItem.getVersion()
        );

        eventRepository.save(event);
        eventPublisher.publish(event);

        log.info("Reservation confirmed - ID: {}", updatedItem.getId());
        return updatedItem;
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "inventoryCommand")
    public void deleteInventoryItem(String id, String userId) {
        log.info("Deleting inventory item - ID: {}", id);

        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryItemNotFoundException("Item not found: " + id));

        inventoryRepository.deleteById(id);

        Map<String, Object> payload = new HashMap<>();
        payload.put("productId", item.getProductId());
        payload.put("storeId", item.getStoreId());

        InventoryEvent event = InventoryEvent.create(
                InventoryEvent.EventType.ITEM_DELETED,
                id,
                item.getStoreId(),
                userId,
                payload,
                item.getVersion()
        );

        eventRepository.save(event);
        eventPublisher.publish(event);

        log.info("Inventory item deleted - ID: {}", id);
    }

    // Métodos privados auxiliares

    private void publishLowStockAlert(InventoryItem item, String userId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("productId", item.getProductId());
        payload.put("productName", item.getProductName());
        payload.put("currentQuantity", item.getQuantity());
        payload.put("minThreshold", item.getMinThreshold());
        payload.put("deficit", item.getMinThreshold() - item.getAvailableStock());

        InventoryEvent alertEvent = InventoryEvent.create(
                InventoryEvent.EventType.LOW_STOCK_ALERT,
                item.getId(),
                item.getStoreId(),
                userId,
                payload,
                item.getVersion()
        );

        eventRepository.save(alertEvent);
        eventPublisher.publish(alertEvent);

        log.warn("Low stock alert - Item: {}, Available: {}, Threshold: {}",
                item.getId(), item.getAvailableStock(), item.getMinThreshold());
    }

    // Fallback methods para Circuit Breaker

    private InventoryItem createInventoryItemFallback(CreateInventoryItemCommand command, Exception ex) {
        log.error("Fallback: Failed to create inventory item - {}", ex.getMessage());
        throw new RuntimeException("Service temporarily unavailable. Please try again later.", ex);
    }

    private InventoryItem updateStockFallback(UpdateStockCommand command, Exception ex) {
        log.error("Fallback: Failed to update stock - {}", ex.getMessage());
        throw new RuntimeException("Service temporarily unavailable. Please try again later.", ex);
    }

    private InventoryItem reserveStockFallback(ReserveStockCommand command, Exception ex) {
        log.error("Fallback: Failed to reserve stock - {}", ex.getMessage());
        throw new RuntimeException("Service temporarily unavailable. Please try again later.", ex);
    }

}
