package com.meli.inventory.command.service.infrastructure.persistence.csv;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meli.inventory.command.service.domain.model.InventoryEvent;
import com.meli.inventory.command.service.domain.port.out.EventRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Repositorio de eventos usando CSV
 * Event Sourcing - Almacena todos los eventos de forma inmutable
 */
@Repository
@Slf4j
public class CsvEventRepository implements EventRepository {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final String[] HEADERS = {
            "eventId", "eventType", "aggregateId", "storeId",
            "timestamp", "userId", "payload", "version"
    };

    @Value("${csv.storage.path:src/main/resources/data}")
    private String storagePath;

    @Value("${csv.events.filename:events.csv}")
    private String filename;

    private Path filePath;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final List<InventoryEvent> events = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() throws IOException {
        Path directory = Paths.get(storagePath);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        filePath = directory.resolve(filename);

        if (!Files.exists(filePath)) {
            try (CSVWriter writer = new CSVWriter(new FileWriter(filePath.toFile()))) {
                writer.writeNext(HEADERS);
                log.info("Created events CSV file: {}", filePath);
            }
        }

        loadEvents();
    }

    @Override
    public void save(InventoryEvent event) {
        lock.writeLock().lock();
        try {
            events.add(event);
            appendToFile(event);
            log.debug("Saved event: {} for aggregate: {}",
                    event.getEventType(), event.getAggregateId());
        } catch (IOException e) {
            log.error("Failed to save event", e);
            throw new RuntimeException("Failed to persist event", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<InventoryEvent> findByAggregateId(String aggregateId) {
        lock.readLock().lock();
        try {
            return events.stream()
                    .filter(e -> e.getAggregateId().equals(aggregateId))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<InventoryEvent> findByStoreId(String storeId) {
        lock.readLock().lock();
        try {
            return events.stream()
                    .filter(e -> e.getStoreId().equals(storeId))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<InventoryEvent> findByEventType(InventoryEvent.EventType eventType) {
        lock.readLock().lock();
        try {
            return events.stream()
                    .filter(e -> e.getEventType() == eventType)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    private void loadEvents() throws IOException {
        lock.writeLock().lock();
        try (CSVReader reader = new CSVReader(new FileReader(filePath.toFile()))) {
            List<String[]> records = reader.readAll();

            if (records.size() > 1) {
                for (int i = 1; i < records.size(); i++) {
                    InventoryEvent event = parseRecord(records.get(i));
                    events.add(event);
                }
            }

            log.info("Loaded {} events from CSV", events.size());
        } catch (Exception e) {
            log.error("Failed to load events", e);
            throw new IOException("Failed to load events", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void appendToFile(InventoryEvent event) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath.toFile(), true))) {
            writer.writeNext(toRecord(event));
            writer.flush();
        }
    }

    private InventoryEvent parseRecord(String[] record) throws JsonProcessingException {
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = objectMapper.readValue(record[6], Map.class);

        return InventoryEvent.builder()
                .eventId(record[0])
                .eventType(InventoryEvent.EventType.valueOf(record[1]))
                .aggregateId(record[2])
                .storeId(record[3])
                .timestamp(LocalDateTime.parse(record[4], DATE_FORMATTER))
                .userId(record[5])
                .payload(payload)
                .version(Long.parseLong(record[7]))
                .build();
    }

    private String[] toRecord(InventoryEvent event) {
        try {
            return new String[] {
                    event.getEventId(),
                    event.getEventType().name(),
                    event.getAggregateId(),
                    event.getStoreId(),
                    event.getTimestamp().format(DATE_FORMATTER),
                    event.getUserId(),
                    objectMapper.writeValueAsString(event.getPayload()),
                    event.getVersion().toString()
            };
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event payload", e);
        }
    }
}
