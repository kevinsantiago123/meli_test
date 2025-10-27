package com.meli.inventory.query.service.domain.port.out;

import com.meli.inventory.query.service.domain.model.InventoryProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("InventoryQueryRepository - Interface Contract Tests")
class InventoryQueryRepositoryTest {

    @Test
    @DisplayName("Debe ser una interfaz")
    void shouldBeAnInterface() {
        // Assert
        assertThat(InventoryQueryRepository.class.isInterface()).isTrue();
    }

    @Test
    @DisplayName("Debe tener 13 métodos definidos")
    void shouldHaveThirteenMethods() {
        // Act
        Method[] methods = InventoryQueryRepository.class.getDeclaredMethods();

        // Assert
        assertThat(methods).hasSize(13);
    }

    @Test
    @DisplayName("Debe tener método findById con firma correcta")
    void shouldHaveFindByIdMethod() throws NoSuchMethodException {
        // Act
        Method method = InventoryQueryRepository.class.getMethod("findById", String.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(Optional.class);
        assertThat(method.getParameterCount()).isEqualTo(1);
        assertThat(method.getParameterTypes()[0]).isEqualTo(String.class);
    }

    @Test
    @DisplayName("Debe tener método findByProductIdAndStoreId con firma correcta")
    void shouldHaveFindByProductIdAndStoreIdMethod() throws NoSuchMethodException {
        // Act
        Method method = InventoryQueryRepository.class.getMethod(
                "findByProductIdAndStoreId", String.class, String.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(Optional.class);
        assertThat(method.getParameterCount()).isEqualTo(2);
        assertThat(method.getParameterTypes()[0]).isEqualTo(String.class);
        assertThat(method.getParameterTypes()[1]).isEqualTo(String.class);
    }

    @Test
    @DisplayName("Debe tener método findByStoreId con paginación")
    void shouldHaveFindByStoreIdWithPagination() throws NoSuchMethodException {
        // Act
        Method method = InventoryQueryRepository.class.getMethod(
                "findByStoreId", String.class, Pageable.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(Page.class);
        assertThat(method.getParameterCount()).isEqualTo(2);
        assertThat(method.getParameterTypes()[0]).isEqualTo(String.class);
        assertThat(method.getParameterTypes()[1]).isEqualTo(Pageable.class);
    }

    @Test
    @DisplayName("Debe tener método findByStoreId sin paginación")
    void shouldHaveFindByStoreIdWithoutPagination() throws NoSuchMethodException {
        // Act
        Method method = InventoryQueryRepository.class.getMethod("findByStoreId", String.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(List.class);
        assertThat(method.getParameterCount()).isEqualTo(1);
        assertThat(method.getParameterTypes()[0]).isEqualTo(String.class);
    }

    @Test
    @DisplayName("Debe tener método findByStoreIdAndCategory con firma correcta")
    void shouldHaveFindByStoreIdAndCategoryMethod() throws NoSuchMethodException {
        // Act
        Method method = InventoryQueryRepository.class.getMethod(
                "findByStoreIdAndCategory", String.class, String.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(List.class);
        assertThat(method.getParameterCount()).isEqualTo(2);
        assertThat(method.getParameterTypes()[0]).isEqualTo(String.class);
        assertThat(method.getParameterTypes()[1]).isEqualTo(String.class);
    }

    @Test
    @DisplayName("Debe tener método findLowStockItems con firma correcta")
    void shouldHaveFindLowStockItemsMethod() throws NoSuchMethodException {
        // Act
        Method method = InventoryQueryRepository.class.getMethod("findLowStockItems", String.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(List.class);
        assertThat(method.getParameterCount()).isEqualTo(1);
        assertThat(method.getParameterTypes()[0]).isEqualTo(String.class);
    }

    @Test
    @DisplayName("Debe tener método findOutOfStockItems con firma correcta")
    void shouldHaveFindOutOfStockItemsMethod() throws NoSuchMethodException {
        // Act
        Method method = InventoryQueryRepository.class.getMethod(
                "findOutOfStockItems", String.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(List.class);
        assertThat(method.getParameterCount()).isEqualTo(1);
        assertThat(method.getParameterTypes()[0]).isEqualTo(String.class);
    }

    @Test
    @DisplayName("Debe tener método findAvailableItems con firma correcta")
    void shouldHaveFindAvailableItemsMethod() throws NoSuchMethodException {
        // Act
        Method method = InventoryQueryRepository.class.getMethod(
                "findAvailableItems", String.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(List.class);
        assertThat(method.getParameterCount()).isEqualTo(1);
        assertThat(method.getParameterTypes()[0]).isEqualTo(String.class);
    }

    @Test
    @DisplayName("Debe tener método searchByProductName con firma correcta")
    void shouldHaveSearchByProductNameMethod() throws NoSuchMethodException {
        // Act
        Method method = InventoryQueryRepository.class.getMethod(
                "searchByProductName", String.class, String.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(List.class);
        assertThat(method.getParameterCount()).isEqualTo(2);
        assertThat(method.getParameterTypes()[0]).isEqualTo(String.class);
        assertThat(method.getParameterTypes()[1]).isEqualTo(String.class);
    }

    @Test
    @DisplayName("Debe tener método findAll con paginación")
    void shouldHaveFindAllWithPagination() throws NoSuchMethodException {
        // Act
        Method method = InventoryQueryRepository.class.getMethod("findAll", Pageable.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(Page.class);
        assertThat(method.getParameterCount()).isEqualTo(1);
        assertThat(method.getParameterTypes()[0]).isEqualTo(Pageable.class);
    }

    @Test
    @DisplayName("Debe tener método countByStoreId con firma correcta")
    void shouldHaveCountByStoreIdMethod() throws NoSuchMethodException {
        // Act
        Method method = InventoryQueryRepository.class.getMethod("countByStoreId", String.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(long.class);
        assertThat(method.getParameterCount()).isEqualTo(1);
        assertThat(method.getParameterTypes()[0]).isEqualTo(String.class);
    }

    @Test
    @DisplayName("Debe tener método save con firma correcta")
    void shouldHaveSaveMethod() throws NoSuchMethodException {
        // Act
        Method method = InventoryQueryRepository.class.getMethod(
                "save", InventoryProjection.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(InventoryProjection.class);
        assertThat(method.getParameterCount()).isEqualTo(1);
        assertThat(method.getParameterTypes()[0]).isEqualTo(InventoryProjection.class);
    }

    @Test
    @DisplayName("Debe tener método deleteById con firma correcta")
    void shouldHaveDeleteByIdMethod() throws NoSuchMethodException {
        // Act
        Method method = InventoryQueryRepository.class.getMethod("deleteById", String.class);

        // Assert
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(void.class);
        assertThat(method.getParameterCount()).isEqualTo(1);
        assertThat(method.getParameterTypes()[0]).isEqualTo(String.class);
    }

    @Test
    @DisplayName("Métodos de búsqueda deben retornar Optional para single items")
    void searchMethodsShouldReturnOptionalForSingleItems() throws NoSuchMethodException {
        // Assert
        assertThat(InventoryQueryRepository.class.getMethod("findById", String.class)
                .getReturnType()).isEqualTo(Optional.class);

        assertThat(InventoryQueryRepository.class.getMethod(
                        "findByProductIdAndStoreId", String.class, String.class)
                .getReturnType()).isEqualTo(Optional.class);
    }

    @Test
    @DisplayName("Métodos de búsqueda múltiple deben retornar List")
    void searchMethodsShouldReturnListForMultipleItems() throws NoSuchMethodException {
        // Assert
        assertThat(InventoryQueryRepository.class.getMethod("findByStoreId", String.class)
                .getReturnType()).isEqualTo(List.class);

        assertThat(InventoryQueryRepository.class.getMethod(
                        "findByStoreIdAndCategory", String.class, String.class)
                .getReturnType()).isEqualTo(List.class);

        assertThat(InventoryQueryRepository.class.getMethod("findLowStockItems", String.class)
                .getReturnType()).isEqualTo(List.class);

        assertThat(InventoryQueryRepository.class.getMethod("findOutOfStockItems", String.class)
                .getReturnType()).isEqualTo(List.class);

        assertThat(InventoryQueryRepository.class.getMethod("findAvailableItems", String.class)
                .getReturnType()).isEqualTo(List.class);

        assertThat(InventoryQueryRepository.class.getMethod(
                        "searchByProductName", String.class, String.class)
                .getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("Métodos con paginación deben retornar Page")
    void paginatedMethodsShouldReturnPage() throws NoSuchMethodException {
        // Assert
        assertThat(InventoryQueryRepository.class.getMethod(
                        "findByStoreId", String.class, Pageable.class)
                .getReturnType()).isEqualTo(Page.class);

        assertThat(InventoryQueryRepository.class.getMethod("findAll", Pageable.class)
                .getReturnType()).isEqualTo(Page.class);
    }

    @Test
    @DisplayName("Debe tener sobrecarga del método findByStoreId")
    void shouldHaveOverloadedFindByStoreIdMethod() {
        // Act
        Method[] methods = InventoryQueryRepository.class.getDeclaredMethods();
        long findByStoreIdCount = 0;

        for (Method method : methods) {
            if (method.getName().equals("findByStoreId")) {
                findByStoreIdCount++;
            }
        }

        // Assert
        assertThat(findByStoreIdCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Métodos de escritura solo deben ser save y deleteById")
    void writingMethodsShouldOnlyBeSaveAndDeleteById() throws NoSuchMethodException {
        // Assert - Solo estos dos métodos modifican datos
        Method saveMethod = InventoryQueryRepository.class.getMethod(
                "save", InventoryProjection.class);
        Method deleteMethod = InventoryQueryRepository.class.getMethod(
                "deleteById", String.class);

        assertThat(saveMethod).isNotNull();
        assertThat(deleteMethod).isNotNull();

        // Todos los demás métodos son de solo lectura
        Method[] allMethods = InventoryQueryRepository.class.getDeclaredMethods();
        long readOnlyMethods = 0;

        for (Method method : allMethods) {
            if (!method.getName().equals("save") && !method.getName().equals("deleteById")) {
                readOnlyMethods++;
            }
        }

        assertThat(readOnlyMethods).isEqualTo(11);
    }

    @Test
    @DisplayName("Método countByStoreId debe retornar tipo primitivo long")
    void countByStoreIdShouldReturnPrimitiveLong() throws NoSuchMethodException {
        // Act
        Method method = InventoryQueryRepository.class.getMethod("countByStoreId", String.class);

        // Assert
        assertThat(method.getReturnType()).isEqualTo(long.class);
        assertThat(method.getReturnType().isPrimitive()).isTrue();
    }

    @Test
    @DisplayName("Métodos de filtrado específico deben aceptar storeId")
    void filteringMethodsShouldAcceptStoreId() throws NoSuchMethodException {
        // Arrange
        String[] filterMethods = {
                "findLowStockItems",
                "findOutOfStockItems",
                "findAvailableItems"
        };

        // Act & Assert
        for (String methodName : filterMethods) {
            Method method = InventoryQueryRepository.class.getMethod(methodName, String.class);
            assertThat(method).isNotNull();
            assertThat(method.getParameterTypes()[0]).isEqualTo(String.class);
        }
    }

    @Test
    @DisplayName("Método searchByProductName debe aceptar storeId y searchTerm")
    void searchByProductNameShouldAcceptStoreIdAndSearchTerm() throws NoSuchMethodException {
        // Act
        Method method = InventoryQueryRepository.class.getMethod(
                "searchByProductName", String.class, String.class);

        // Assert
        assertThat(method.getParameterCount()).isEqualTo(2);
        assertThat(method.getParameterTypes()[0]).isEqualTo(String.class); // storeId
        assertThat(method.getParameterTypes()[1]).isEqualTo(String.class); // searchTerm
    }

    @Test
    @DisplayName("Repositorio debe estar optimizado para operaciones de lectura")
    void repositoryShouldBeOptimizedForReadOperations() {
        // Act
        Method[] methods = InventoryQueryRepository.class.getDeclaredMethods();
        long readMethods = 0;
        long writeMethods = 0;

        for (Method method : methods) {
            if (method.getName().equals("save") || method.getName().equals("deleteById")) {
                writeMethods++;
            } else {
                readMethods++;
            }
        }

        // Assert - Mayoría de métodos son de lectura (CQRS Query Side)
        assertThat(readMethods).isGreaterThan(writeMethods);
        assertThat(readMethods).isEqualTo(11);
        assertThat(writeMethods).isEqualTo(2);
    }

    @Test
    @DisplayName("Todos los métodos de búsqueda deben tener nombres descriptivos")
    void allSearchMethodsShouldHaveDescriptiveNames() {
        // Act
        Method[] methods = InventoryQueryRepository.class.getDeclaredMethods();

        // Assert
        for (Method method : methods) {
            String methodName = method.getName();
            assertThat(methodName).matches("find.*|search.*|count.*|save|deleteById");
        }
    }

    @Test
    @DisplayName("Métodos con Pageable deben estar al final de los parámetros")
    void pageableParameterShouldBeLastParameter() throws NoSuchMethodException {
        // Act
        Method findByStoreIdPaged = InventoryQueryRepository.class.getMethod(
                "findByStoreId", String.class, Pageable.class);
        Method findAllPaged = InventoryQueryRepository.class.getMethod(
                "findAll", Pageable.class);

        // Assert
        assertThat(findByStoreIdPaged.getParameterTypes()[1]).isEqualTo(Pageable.class);
        assertThat(findAllPaged.getParameterTypes()[0]).isEqualTo(Pageable.class);
    }
}