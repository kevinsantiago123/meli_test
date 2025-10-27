package com.meli.inventory.query.service.infrastructure.persistence.csv;


import com.meli.inventory.query.service.domain.model.InventoryProjection;
import com.meli.inventory.query.service.domain.port.out.InventoryQueryRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Repositorio CSV optimizado para consultas
 * Características:
 * - Cache en memoria para lecturas rápidas
 * - Thread-safe con ReadWriteLock
 * - Búsquedas optimizadas con índices
 */
@Repository
@Slf4j
public class CsvQueryRepository implements InventoryQueryRepository {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final String[] HEADERS = {
            "id", "storeId", "storeName", "productId", "productName", "category",
            "quantity", "reservedQuantity", "availableStock", "minThreshold",
            "belowThreshold", "version", "lastUpdated", "lastModifiedBy", "status", "unitPrice"
    };

    @Value("${csv.storage.path:src/main/resources/data}")
    private String storagePath;

    @Value("${csv.query.filename:inventory-query.csv}")
    private String filename;

    private Path filePath;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    // Cache con índices para búsquedas rápidas
    private final Map<String, InventoryProjection> cacheById = new ConcurrentHashMap<>();
    private final Map<String, Map<String, InventoryProjection>> cacheByStore = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() throws IOException {
        Path directory = Paths.get(storagePath);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
            log.info("Created storage directory: {}", directory);
        }

        filePath = directory.resolve(filename);

        if (!Files.exists(filePath)) {
            try (CSVWriter writer = new CSVWriter(new FileWriter(filePath.toFile()))) {
                writer.writeNext(HEADERS);
                log.info("Created query CSV file: {}", filePath);
            }
        }

        loadCache();
    }

    @Override
    public Optional<InventoryProjection> findById(String id) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(cacheById.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<InventoryProjection> findByProductIdAndStoreId(String productId, String storeId) {
        lock.readLock().lock();
        try {
            Map<String, InventoryProjection> storeItems = cacheByStore.get(storeId);
            if (storeItems == null) {
                return Optional.empty();
            }

            return storeItems.values().stream()
                    .filter(item -> item.getProductId().equals(productId))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Page<InventoryProjection> findByStoreId(String storeId, Pageable pageable) {
        lock.readLock().lock();
        try {
            List<InventoryProjection> items = findByStoreId(storeId);

            return getInventoryProjections(pageable, items);

        } finally {
            lock.readLock().unlock();
        }
    }

    private Page<InventoryProjection> getInventoryProjections(Pageable pageable, List<InventoryProjection> items) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());

        if (start > items.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, items.size());
        }

        List<InventoryProjection> pageItems = items.subList(start, end);
        return new PageImpl<>(pageItems, pageable, items.size());
    }

    @Override
    public List<InventoryProjection> findByStoreId(String storeId) {
        lock.readLock().lock();
        try {
            Map<String, InventoryProjection> storeItems = cacheByStore.get(storeId);
            if (storeItems == null) {
                return Collections.emptyList();
            }
            return new ArrayList<>(storeItems.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<InventoryProjection> findByStoreIdAndCategory(String storeId, String category) {
        lock.readLock().lock();
        try {
            return findByStoreId(storeId).stream()
                    .filter(item -> category.equalsIgnoreCase(item.getCategory()))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<InventoryProjection> findLowStockItems(String storeId) {
        lock.readLock().lock();
        try {
            return findByStoreId(storeId).stream()
                    .filter(InventoryProjection::getBelowThreshold)
                    .filter(item -> item.getAvailableStock() > 0) // Excluir sin stock
                    .sorted(Comparator.comparing(InventoryProjection::getAvailableStock))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<InventoryProjection> findOutOfStockItems(String storeId) {
        lock.readLock().lock();
        try {
            return findByStoreId(storeId).stream()
                    .filter(item -> item.getAvailableStock() <= 0)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<InventoryProjection> findAvailableItems(String storeId) {
        lock.readLock().lock();
        try {
            return findByStoreId(storeId).stream()
                    .filter(InventoryProjection::isAvailableForSale)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<InventoryProjection> searchByProductName(String storeId, String searchTerm) {
        lock.readLock().lock();
        try {
            String lowerSearchTerm = searchTerm.toLowerCase();
            return findByStoreId(storeId).stream()
                    .filter(item -> item.getProductName().toLowerCase().contains(lowerSearchTerm))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Page<InventoryProjection> findAll(Pageable pageable) {
        lock.readLock().lock();
        try {
            List<InventoryProjection> allItems = new ArrayList<>(cacheById.values());

            return getInventoryProjections(pageable, allItems);

        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public long countByStoreId(String storeId) {
        lock.readLock().lock();
        try {
            Map<String, InventoryProjection> storeItems = cacheByStore.get(storeId);
            return storeItems != null ? storeItems.size() : 0;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public InventoryProjection save(InventoryProjection projection) {
        lock.writeLock().lock();
        try {
            // Calcular campos derivados
            projection.calculateDerivedFields();

            // Actualizar cache
            cacheById.put(projection.getId(), projection);

            cacheByStore.computeIfAbsent(projection.getStoreId(), k -> new ConcurrentHashMap<>())
                    .put(projection.getId(), projection);

            // Persistir
            persistToFile();

            log.debug("Saved projection: {}", projection.getId());
            return projection;

        } catch (IOException e) {
            log.error("Failed to save projection: {}", projection.getId(), e);
            throw new RuntimeException("Failed to persist projection", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deleteById(String id) {
        lock.writeLock().lock();
        try {
            InventoryProjection removed = cacheById.remove(id);
            if (removed != null) {
                Map<String, InventoryProjection> storeItems = cacheByStore.get(removed.getStoreId());
                if (storeItems != null) {
                    storeItems.remove(id);
                }
            }

            persistToFile();
            log.debug("Deleted projection: {}", id);

        } catch (IOException e) {
            log.error("Failed to delete projection: {}", id, e);
            throw new RuntimeException("Failed to delete projection", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Métodos privados

    private void loadCache() throws IOException {
        lock.writeLock().lock();
        try (CSVReader reader = new CSVReader(new FileReader(filePath.toFile()))) {
            List<String[]> records = reader.readAll();

            if (records.size() > 1) {
                for (int i = 1; i < records.size(); i++) {
                    InventoryProjection projection = parseRecord(records.get(i));
                    cacheById.put(projection.getId(), projection);

                    cacheByStore.computeIfAbsent(projection.getStoreId(), k -> new ConcurrentHashMap<>())
                            .put(projection.getId(), projection);
                }
            }

            log.info("Loaded {} projections from CSV", cacheById.size());

        } catch (Exception e) {
            log.error("Failed to load CSV data", e);
            throw new IOException("Failed to load CSV data", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void persistToFile() throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath.toFile()))) {
            writer.writeNext(HEADERS);

            for (InventoryProjection projection : cacheById.values()) {
                writer.writeNext(toRecord(projection));
            }

            writer.flush();
            log.debug("Persisted {} projections to CSV", cacheById.size());

        } catch (IOException e) {
            log.error("Failed to persist to CSV file", e);
            throw e;
        }
    }

    private InventoryProjection parseRecord(String[] record) {

        return InventoryProjection.builder()
                .id(record[0])
                .storeId(record[1])
                .storeName(record[2])
                .productId(record[3])
                .productName(record[4])
                .category(record[5])
                .quantity(Integer.parseInt(record[6]))
                .reservedQuantity(Integer.parseInt(record[7]))
                .availableStock(Integer.parseInt(record[8]))
                .minThreshold(Integer.parseInt(record[9]))
                .belowThreshold(Boolean.parseBoolean(record[10]))
                .version(Long.parseLong(record[11]))
                .lastUpdated(LocalDateTime.parse(record[12], DATE_FORMATTER))
                .lastModifiedBy(record[13])
                .status(InventoryProjection.ItemStatus.valueOf(record[14]))
                .unitPrice(record[15] != null && !record[15].isEmpty() ? Double.parseDouble(record[15]) : null)
                .build();
    }

    private String[] toRecord(InventoryProjection projection) {
        return new String[] {
                projection.getId(),
                projection.getStoreId(),
                projection.getStoreName() != null ? projection.getStoreName() : "",
                projection.getProductId(),
                projection.getProductName(),
                projection.getCategory() != null ? projection.getCategory() : "",
                projection.getQuantity().toString(),
                projection.getReservedQuantity().toString(),
                projection.getAvailableStock().toString(),
                projection.getMinThreshold().toString(),
                projection.getBelowThreshold().toString(),
                projection.getVersion().toString(),
                projection.getLastUpdated().format(DATE_FORMATTER),
                projection.getLastModifiedBy(),
                projection.getStatus().name(),
                projection.getUnitPrice() != null ? projection.getUnitPrice().toString() : ""
        };
    }
}
