package com.meli.inventory.command.service.application;

import com.meli.inventory.command.service.domain.exception.DuplicateInventoryItemException;
import com.meli.inventory.command.service.domain.exception.InventoryItemNotFoundException;
import com.meli.inventory.command.service.domain.exception.OptimisticLockException;
import com.meli.inventory.command.service.domain.model.InventoryEvent;
import com.meli.inventory.command.service.domain.model.InventoryItem;
import com.meli.inventory.command.service.domain.port.in.*;
import com.meli.inventory.command.service.domain.port.out.EventPublisher;
import com.meli.inventory.command.service.domain.port.out.EventRepository;
import com.meli.inventory.command.service.domain.port.out.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryCommandServiceImpl - Unit Tests")
class InventoryCommandServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private InventoryCommandServiceImpl service;

    private String testItemId;
    private String testStoreId;
    private String testProductId;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testItemId = "ITEM-001";
        testStoreId = "STORE-001";
        testProductId = "PROD-001";
        testUserId = "USER-001";
    }

    @Nested
    @DisplayName("Tests de createInventoryItem()")
    class CreateInventoryItemTests {

        @Test
        @DisplayName("Debe crear item exitosamente")
        void shouldCreateInventoryItemSuccessfully() {
            // Arrange
            CreateInventoryItemCommand command = CreateInventoryItemCommand.builder()
                    .storeId(testStoreId)
                    .productId(testProductId)
                    .productName("Test Product")
                    .quantity(100)
                    .minThreshold(10)
                    .userId(testUserId)
                    .build();

            when(inventoryRepository.existsByProductIdAndStoreId(testProductId, testStoreId))
                    .thenReturn(false);

            InventoryItem savedItem = InventoryItem.create(
                    testStoreId, testProductId, "Test Product", 100, 10, testUserId);
            when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(savedItem);

            // Act
            InventoryItem result = service.createInventoryItem(command);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getStoreId()).isEqualTo(testStoreId);
            assertThat(result.getProductId()).isEqualTo(testProductId);
            assertThat(result.getQuantity()).isEqualTo(100);

            verify(inventoryRepository).existsByProductIdAndStoreId(testProductId, testStoreId);
            verify(inventoryRepository).save(any(InventoryItem.class));
            verify(eventRepository).save(any(InventoryEvent.class));
            verify(eventPublisher).publish(any(InventoryEvent.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción si el item ya existe")
        void shouldThrowExceptionWhenItemAlreadyExists() {
            // Arrange
            CreateInventoryItemCommand command = CreateInventoryItemCommand.builder()
                    .storeId(testStoreId)
                    .productId(testProductId)
                    .productName("Test Product")
                    .quantity(100)
                    .minThreshold(10)
                    .userId(testUserId)
                    .build();

            when(inventoryRepository.existsByProductIdAndStoreId(testProductId, testStoreId))
                    .thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> service.createInventoryItem(command))
                    .isInstanceOf(DuplicateInventoryItemException.class);

            verify(inventoryRepository).existsByProductIdAndStoreId(testProductId, testStoreId);
            verify(inventoryRepository, never()).save(any());
            verify(eventRepository, never()).save(any());
            verify(eventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("Debe publicar evento ITEM_CREATED")
        void shouldPublishItemCreatedEvent() {
            // Arrange
            CreateInventoryItemCommand command = CreateInventoryItemCommand.builder()
                    .storeId(testStoreId)
                    .productId(testProductId)
                    .productName("Test Product")
                    .quantity(100)
                    .minThreshold(10)
                    .userId(testUserId)
                    .build();

            when(inventoryRepository.existsByProductIdAndStoreId(any(), any())).thenReturn(false);
            when(inventoryRepository.save(any(InventoryItem.class)))
                    .thenReturn(InventoryItem.create(testStoreId, testProductId,
                            "Test Product", 100, 10, testUserId));

            ArgumentCaptor<InventoryEvent> eventCaptor = ArgumentCaptor.forClass(InventoryEvent.class);

            // Act
            service.createInventoryItem(command);

            // Assert
            verify(eventPublisher).publish(eventCaptor.capture());
            InventoryEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.getEventType()).isEqualTo(InventoryEvent.EventType.ITEM_CREATED);
            assertThat(capturedEvent.getStoreId()).isEqualTo(testStoreId);
        }
    }

    @Nested
    @DisplayName("Tests de updateStock()")
    class UpdateStockTests {

        @Test
        @DisplayName("Debe actualizar stock con operación SET")
        void shouldUpdateStockWithSetOperation() {
            // Arrange
            UpdateStockCommand command = UpdateStockCommand.builder()
                    .itemId(testItemId)
                    .quantity(150)
                    .operation(UpdateStockCommand.StockOperation.SET)
                    .version(1L)
                    .userId(testUserId)
                    .reason("Manual adjustment")
                    .build();

            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    "Test Product", 100, 10, testUserId);
            when(inventoryRepository.findById(testItemId)).thenReturn(Optional.of(item));
            when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(item);

            // Act
            InventoryItem result = service.updateStock(command);

            // Assert
            assertThat(result.getQuantity()).isEqualTo(150);
            verify(inventoryRepository).findById(testItemId);
            verify(inventoryRepository).save(any(InventoryItem.class));
            verify(eventPublisher).publish(any(InventoryEvent.class));
        }

        @Test
        @DisplayName("Debe actualizar stock con operación ADD")
        void shouldUpdateStockWithAddOperation() {
            // Arrange
            UpdateStockCommand command = UpdateStockCommand.builder()
                    .itemId(testItemId)
                    .quantity(50)
                    .operation(UpdateStockCommand.StockOperation.ADD)
                    .version(1L)
                    .userId(testUserId)
                    .reason("Restock")
                    .build();

            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    "Test Product", 100, 10, testUserId);
            when(inventoryRepository.findById(testItemId)).thenReturn(Optional.of(item));
            when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(item);

            // Act
            InventoryItem result = service.updateStock(command);

            // Assert
            assertThat(result.getQuantity()).isEqualTo(150);
            verify(inventoryRepository).save(any(InventoryItem.class));
        }

        @Test
        @DisplayName("Debe actualizar stock con operación SUBTRACT")
        void shouldUpdateStockWithSubtractOperation() {
            // Arrange
            UpdateStockCommand command = UpdateStockCommand.builder()
                    .itemId(testItemId)
                    .quantity(30)
                    .operation(UpdateStockCommand.StockOperation.SUBTRACT)
                    .version(1L)
                    .userId(testUserId)
                    .reason("Sale")
                    .build();

            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    "Test Product", 100, 10, testUserId);
            when(inventoryRepository.findById(testItemId)).thenReturn(Optional.of(item));
            when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(item);

            // Act
            InventoryItem result = service.updateStock(command);

            // Assert
            assertThat(result.getQuantity()).isEqualTo(70);
            verify(inventoryRepository).save(any(InventoryItem.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción si el item no existe")
        void shouldThrowExceptionWhenItemNotFound() {
            // Arrange
            UpdateStockCommand command = UpdateStockCommand.builder()
                    .itemId(testItemId)
                    .quantity(150)
                    .operation(UpdateStockCommand.StockOperation.SET)
                    .version(1L)
                    .userId(testUserId)
                    .reason("Test")
                    .build();

            when(inventoryRepository.findById(testItemId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> service.updateStock(command))
                    .isInstanceOf(InventoryItemNotFoundException.class)
                    .hasMessageContaining("Item not found");

            verify(inventoryRepository).findById(testItemId);
            verify(inventoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar excepción si la versión no coincide")
        void shouldThrowExceptionWhenVersionMismatch() {
            // Arrange
            UpdateStockCommand command = UpdateStockCommand.builder()
                    .itemId(testItemId)
                    .quantity(150)
                    .operation(UpdateStockCommand.StockOperation.SET)
                    .version(5L)
                    .userId(testUserId)
                    .reason("Test")
                    .build();

            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    "Test Product", 100, 10, testUserId);
            when(inventoryRepository.findById(testItemId)).thenReturn(Optional.of(item));

            // Act & Assert
            assertThatThrownBy(() -> service.updateStock(command))
                    .isInstanceOf(OptimisticLockException.class);
        }

        @Test
        @DisplayName("Debe publicar alerta de stock bajo cuando aplique")
        void shouldPublishLowStockAlert() {
            // Arrange
            UpdateStockCommand command = UpdateStockCommand.builder()
                    .itemId(testItemId)
                    .quantity(5)
                    .operation(UpdateStockCommand.StockOperation.SET)
                    .version(1L)
                    .userId(testUserId)
                    .reason("Test")
                    .build();

            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    "Test Product", 100, 50, testUserId);
            when(inventoryRepository.findById(testItemId)).thenReturn(Optional.of(item));
            when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(item);

            // Act
            service.updateStock(command);

            // Assert
            verify(eventPublisher, times(2)).publish(any(InventoryEvent.class)); // STOCK_UPDATED + LOW_STOCK_ALERT
        }

        @Test
        @DisplayName("Debe publicar evento STOCK_UPDATED con payload correcto")
        void shouldPublishStockUpdatedEventWithCorrectPayload() {
            // Arrange
            UpdateStockCommand command = UpdateStockCommand.builder()
                    .itemId(testItemId)
                    .quantity(150)
                    .operation(UpdateStockCommand.StockOperation.SET)
                    .version(1L)
                    .userId(testUserId)
                    .reason("Manual adjustment")
                    .build();

            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    "Test Product", 100, 10, testUserId);
            when(inventoryRepository.findById(testItemId)).thenReturn(Optional.of(item));
            when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(item);

            ArgumentCaptor<InventoryEvent> eventCaptor = ArgumentCaptor.forClass(InventoryEvent.class);

            // Act
            service.updateStock(command);

            // Assert
            verify(eventPublisher).publish(eventCaptor.capture());
            InventoryEvent event = eventCaptor.getValue();
            assertThat(event.getEventType()).isEqualTo(InventoryEvent.EventType.STOCK_UPDATED);
            assertThat(event.getPayload()).containsKeys("previousQuantity", "newQuantity", "operation", "reason");
        }
    }

    @Nested
    @DisplayName("Tests de reserveStock()")
    class ReserveStockTests {

        @Test
        @DisplayName("Debe reservar stock exitosamente")
        void shouldReserveStockSuccessfully() {
            // Arrange
            ReserveStockCommand command = ReserveStockCommand.builder()
                    .itemId(testItemId)
                    .quantity(30)
                    .reservationId("RES-001")
                    .version(1L)
                    .userId(testUserId)
                    .build();

            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    "Test Product", 100, 10, testUserId);
            when(inventoryRepository.findById(testItemId)).thenReturn(Optional.of(item));
            when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(item);

            // Act
            InventoryItem result = service.reserveStock(command);

            // Assert
            assertThat(result.getReservedQuantity()).isEqualTo(30);
            verify(inventoryRepository).findById(testItemId);
            verify(inventoryRepository).save(any(InventoryItem.class));
            verify(eventRepository).save(any(InventoryEvent.class));
            verify(eventPublisher).publish(any(InventoryEvent.class));
        }

        @Test
        @DisplayName("Debe publicar evento STOCK_RESERVED")
        void shouldPublishStockReservedEvent() {
            // Arrange
            ReserveStockCommand command = ReserveStockCommand.builder()
                    .itemId(testItemId)
                    .quantity(30)
                    .reservationId("RES-001")
                    .version(1L)
                    .userId(testUserId)
                    .build();

            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    "Test Product", 100, 10, testUserId);
            when(inventoryRepository.findById(testItemId)).thenReturn(Optional.of(item));
            when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(item);

            ArgumentCaptor<InventoryEvent> eventCaptor = ArgumentCaptor.forClass(InventoryEvent.class);

            // Act
            service.reserveStock(command);

            // Assert
            verify(eventPublisher).publish(eventCaptor.capture());
            InventoryEvent event = eventCaptor.getValue();
            assertThat(event.getEventType()).isEqualTo(InventoryEvent.EventType.STOCK_RESERVED);
            assertThat(event.getPayload()).containsKey("reservationId");
            assertThat(event.getPayload().get("reservationId")).isEqualTo("RES-001");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el item no existe")
        void shouldThrowExceptionWhenItemNotFound() {
            // Arrange
            ReserveStockCommand command = ReserveStockCommand.builder()
                    .itemId(testItemId)
                    .quantity(30)
                    .reservationId("RES-001")
                    .version(1L)
                    .userId(testUserId)
                    .build();

            when(inventoryRepository.findById(testItemId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> service.reserveStock(command))
                    .isInstanceOf(InventoryItemNotFoundException.class);

            verify(inventoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tests de releaseReservedStock()")
    class ReleaseReservedStockTests {

        @Test
        @DisplayName("Debe liberar stock reservado exitosamente")
        void shouldReleaseReservedStockSuccessfully() {
            // Arrange
            ReleaseStockCommand command = ReleaseStockCommand.builder()
                    .itemId(testItemId)
                    .quantity(20)
                    .reservationId("RES-001")
                    .version(2L)
                    .userId(testUserId)
                    .build();

            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    "Test Product", 100, 10, testUserId);
            item.reserveStock(50, testUserId);
            when(inventoryRepository.findById(testItemId)).thenReturn(Optional.of(item));
            when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(item);

            // Act
            InventoryItem result = service.releaseReservedStock(command);

            // Assert
            assertThat(result.getReservedQuantity()).isEqualTo(30);
            verify(inventoryRepository).save(any(InventoryItem.class));
            verify(eventPublisher).publish(any(InventoryEvent.class));
        }

        @Test
        @DisplayName("Debe publicar evento RESERVATION_RELEASED")
        void shouldPublishReservationReleasedEvent() {
            // Arrange
            ReleaseStockCommand command = ReleaseStockCommand.builder()
                    .itemId(testItemId)
                    .quantity(20)
                    .reservationId("RES-001")
                    .version(2L)
                    .userId(testUserId)
                    .build();

            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    "Test Product", 100, 10, testUserId);
            item.reserveStock(50, testUserId);
            when(inventoryRepository.findById(testItemId)).thenReturn(Optional.of(item));
            when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(item);

            ArgumentCaptor<InventoryEvent> eventCaptor = ArgumentCaptor.forClass(InventoryEvent.class);

            // Act
            service.releaseReservedStock(command);

            // Assert
            verify(eventPublisher).publish(eventCaptor.capture());
            InventoryEvent event = eventCaptor.getValue();
            assertThat(event.getEventType()).isEqualTo(InventoryEvent.EventType.RESERVATION_RELEASED);
        }
    }

    @Nested
    @DisplayName("Tests de confirmReservation()")
    class ConfirmReservationTests {

        @Test
        @DisplayName("Debe confirmar reserva exitosamente")
        void shouldConfirmReservationSuccessfully() {
            // Arrange
            ConfirmReservationCommand command = ConfirmReservationCommand.builder()
                    .itemId(testItemId)
                    .quantity(30)
                    .reservationId("RES-001")
                    .version(2L)
                    .userId(testUserId)
                    .build();

            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    "Test Product", 100, 10, testUserId);
            item.reserveStock(30, testUserId);
            when(inventoryRepository.findById(testItemId)).thenReturn(Optional.of(item));
            when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(item);

            // Act
            InventoryItem result = service.confirmReservation(command);

            // Assert
            assertThat(result.getQuantity()).isEqualTo(70);
            assertThat(result.getReservedQuantity()).isEqualTo(0);
            verify(inventoryRepository).save(any(InventoryItem.class));
            verify(eventPublisher).publish(any(InventoryEvent.class));
        }

        @Test
        @DisplayName("Debe publicar evento RESERVATION_CONFIRMED")
        void shouldPublishReservationConfirmedEvent() {
            // Arrange
            ConfirmReservationCommand command = ConfirmReservationCommand.builder()
                    .itemId(testItemId)
                    .quantity(30)
                    .reservationId("RES-001")
                    .version(2L)
                    .userId(testUserId)
                    .build();

            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    "Test Product", 100, 10, testUserId);
            item.reserveStock(30, testUserId);
            when(inventoryRepository.findById(testItemId)).thenReturn(Optional.of(item));
            when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(item);

            ArgumentCaptor<InventoryEvent> eventCaptor = ArgumentCaptor.forClass(InventoryEvent.class);

            // Act
            service.confirmReservation(command);

            // Assert
            verify(eventPublisher).publish(eventCaptor.capture());
            InventoryEvent event = eventCaptor.getValue();
            assertThat(event.getEventType()).isEqualTo(InventoryEvent.EventType.RESERVATION_CONFIRMED);
        }
    }

    @Nested
    @DisplayName("Tests de deleteInventoryItem()")
    class DeleteInventoryItemTests {

        @Test
        @DisplayName("Debe eliminar item exitosamente")
        void shouldDeleteInventoryItemSuccessfully() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    "Test Product", 100, 10, testUserId);
            when(inventoryRepository.findById(testItemId)).thenReturn(Optional.of(item));

            // Act
            service.deleteInventoryItem(testItemId, testUserId);

            // Assert
            verify(inventoryRepository).findById(testItemId);
            verify(inventoryRepository).deleteById(testItemId);
            verify(eventRepository).save(any(InventoryEvent.class));
            verify(eventPublisher).publish(any(InventoryEvent.class));
        }

        @Test
        @DisplayName("Debe publicar evento ITEM_DELETED")
        void shouldPublishItemDeletedEvent() {
            // Arrange
            InventoryItem item = InventoryItem.create(testStoreId, testProductId,
                    "Test Product", 100, 10, testUserId);
            when(inventoryRepository.findById(testItemId)).thenReturn(Optional.of(item));

            ArgumentCaptor<InventoryEvent> eventCaptor = ArgumentCaptor.forClass(InventoryEvent.class);

            // Act
            service.deleteInventoryItem(testItemId, testUserId);

            // Assert
            verify(eventPublisher).publish(eventCaptor.capture());
            InventoryEvent event = eventCaptor.getValue();
            assertThat(event.getEventType()).isEqualTo(InventoryEvent.EventType.ITEM_DELETED);
        }

        @Test
        @DisplayName("Debe lanzar excepción si el item no existe")
        void shouldThrowExceptionWhenItemNotFound() {
            // Arrange
            when(inventoryRepository.findById(testItemId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> service.deleteInventoryItem(testItemId, testUserId))
                    .isInstanceOf(InventoryItemNotFoundException.class);

            verify(inventoryRepository, never()).deleteById(any());
        }
    }
}