package com.meli.inventory.command.service.infrastructure.rest;

import com.meli.inventory.command.service.domain.model.InventoryEvent;
import com.meli.inventory.command.service.infrastructure.event.SimpleEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final SimpleEventPublisher eventPublisher;

    @GetMapping("/pending")
    public ResponseEntity<List<InventoryEvent>> getPendingEvents() {
        List<InventoryEvent> events = eventPublisher.consumeEvents();
        log.info("Returning {} events", events.size());
        return ResponseEntity.ok(events);
    }
}
