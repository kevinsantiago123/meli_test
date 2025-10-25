package com.meli.inventory.command.service.infrastructure.persistence.csv;


import com.meli.inventory.command.service.domain.model.InventoryItem;
import com.meli.inventory.command.service.domain.port.out.InventoryRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Implementación del repositorio usando archivos CSV
 *
 * Características:
 * - Thread-safe usando ReadWriteLock
 * - File locking para concurrencia multi-proceso
 * - Cache en memoria para performance
 * - Manejo robusto de errores
 */
@Repository
@Slf4j
public class CsvInventoryRepository implements InventoryRepository {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final String[] HEADERS = {
            "id", "storeId", "productId", "productName", "quantity",
            "reservedQuantity", "minThreshold", "version", "lastUpdated", "lastModifiedBy"
    };

    @Value("${csv.storage.path:src/main/resources/data}")
    private String storagePath;

    @Value("${csv.inventory.filename:inventory-items.csv}")
    private String filename;

    private Path filePath;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, InventoryItem> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() throws IOException {
        // Crear directorio si no existe
        Path directory = Paths.get(storagePath);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
            log.info("Created storage directory: {}", directory);
        }

        filePath = directory.resolve(filename);

        // Crear archivo con headers si no existe
        if (!Files.exists(filePath)) {
            try (CSVWriter writer = new CSVWriter(new FileWriter(filePath.toFile()))) {
                writer.writeNext(HEADERS);
                log.info("Created CSV file with headers: {}", filePath);
            }
        }

        // Cargar datos existentes al cache
        loadCache();
    }

    @Override
    public InventoryItem save(InventoryItem item) {
        lock.writeLock().lock();
        try {
            // Actualizar cache
            cache.put(item.getId(), item);

            // Persistir a CSV con file locking
            persistToFile();

            log.debug("Saved inventory item: {}", item.getId());
            return item;

        } catch (IOException e) {
            log.error("Failed to save inventory item: {}", item.getId(), e);
            throw new RuntimeException("Failed to persist inventory item", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<InventoryItem> findById(String id) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(cache.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<InventoryItem> findByProductIdAndStoreId(String productId, String storeId) {
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(item -> item.getProductId().equals(productId)
                            && item.getStoreId().equals(storeId))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<InventoryItem> findByStoreId(String storeId) {
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(item -> item.getStoreId().equals(storeId))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<InventoryItem> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(cache.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void deleteById(String id) {
        lock.writeLock().lock();
        try {
            cache.remove(id);
            persistToFile();
            log.debug("Deleted inventory item: {}", id);
        } catch (IOException e) {
            log.error("Failed to delete inventory item: {}", id, e);
            throw new RuntimeException("Failed to delete inventory item", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean existsByProductIdAndStoreId(String productId, String storeId) {
        return findByProductIdAndStoreId(productId, storeId).isPresent();
    }

    // Métodos privados

    private void loadCache() throws IOException {
        lock.writeLock().lock();
        try (CSVReader reader = new CSVReader(new FileReader(filePath.toFile()))) {
            List<String[]> records = reader.readAll();

            // Saltar header
            if (records.size() > 1) {
                for (int i = 1; i < records.size(); i++) {
                    String[] record = records.get(i);
                    InventoryItem item = parseRecord(record);
                    cache.put(item.getId(), item);
                }
            }

            log.info("Loaded {} inventory items from CSV", cache.size());

        } catch (CsvException e) {
            log.error("Failed to load CSV data", e);
            throw new IOException("Failed to load CSV data", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void persistToFile() throws IOException {
        // Usar file locking para concurrencia multi-proceso
        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "rw");
             FileChannel channel = raf.getChannel();
             FileLock fileLock = channel.lock()) {

            // Truncar archivo
            raf.setLength(0);

            // Escribir con CSVWriter
            try (CSVWriter writer = new CSVWriter(
                    new OutputStreamWriter(Files.newOutputStream(filePath,
                            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)))) {

                // Escribir headers
                writer.writeNext(HEADERS);

                // Escribir todos los items del cache
                for (InventoryItem item : cache.values()) {
                    writer.writeNext(toRecord(item));
                }

                writer.flush();
            }

            log.debug("Persisted {} items to CSV", cache.size());

        } catch (IOException e) {
            log.error("Failed to persist to CSV file", e);
            throw e;
        }
    }

    private InventoryItem parseRecord(String[] record) {
        return InventoryItem.builder()
                .id(record[0])
                .storeId(record[1])
                .productId(record[2])
                .productName(record[3])
                .quantity(Integer.parseInt(record[4]))
                .reservedQuantity(Integer.parseInt(record[5]))
                .minThreshold(Integer.parseInt(record[6]))
                .version(Long.parseLong(record[7]))
                .lastUpdated(LocalDateTime.parse(record[8], DATE_FORMATTER))
                .lastModifiedBy(record[9])
                .build();
    }

    private String[] toRecord(InventoryItem item) {
        return new String[] {
                item.getId(),
                item.getStoreId(),
                item.getProductId(),
                item.getProductName(),
                item.getQuantity().toString(),
                item.getReservedQuantity().toString(),
                item.getMinThreshold().toString(),
                item.getVersion().toString(),
                item.getLastUpdated().format(DATE_FORMATTER),
                item.getLastModifiedBy()
        };
    }
}
