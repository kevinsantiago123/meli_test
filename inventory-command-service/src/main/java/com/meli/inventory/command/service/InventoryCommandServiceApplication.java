package com.meli.inventory.command.service;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Inventory Command Service - Main Application
 *
 * Microservicio responsable de operaciones de escritura (CQRS Command Side)
 *
 * Características:
 * - Arquitectura Hexagonal
 * - Event Sourcing
 * - Optimistic Locking
 * - Circuit Breaker Pattern
 * - Persistencia CSV
 *
 * Puerto: 8081
 *
 * @author Sistema de Inventario Distribuido
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync
public class InventoryCommandServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryCommandServiceApplication.class, args);
	}
}
