package com.meli.inventory.query.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Inventory Query Service - Main Application
 * Microservicio responsable de operaciones de lectura (CQRS Query Side)
 * Características:
 * - Arquitectura Hexagonal
 * - Proyecciones desnormalizadas
 * - Cache con Caffeine
 * - Event Consumer (Eventual Consistency)
 * - Circuit Breaker Pattern
 * - Optimizado para queries de alta frecuencia
 *
 * Puerto: 8082
 *
 * @author Sistema de Inventario Distribuido
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class InventoryQueryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryQueryServiceApplication.class, args);
	}
}