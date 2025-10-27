# Inventory Command Service

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.8+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Microservicio de **escritura** (Command Side) del sistema de gestión de inventario distribuido, implementado con arquitectura CQRS y patrones de resiliencia empresariales.

---

## 📋 Tabla de Contenidos

- [Descripción](#-descripción)
- [Características Principales](#-características-principales)
- [Arquitectura](#-arquitectura)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación](#-instalación)
- [Configuración](#-configuración)
- [Ejecución](#-ejecución)
- [Testing](#-testing)
- [API Documentation](#-api-documentation)
- [Patrones Implementados](#-patrones-implementados)
- [Monitoreo](#-monitoreo)
- [Troubleshooting](#-troubleshooting)
- [Contribución](#-contribución)

---

## 🎯 Descripción

El **Inventory Command Service** es responsable de todas las operaciones de **escritura** en el sistema de inventario distribuido. Implementa el lado Command de la arquitectura CQRS, manejando:

- ✅ Creación de productos en inventario
- ✅ Actualización de stock (incremento/decremento)
- ✅ Reserva de stock para órdenes
- ✅ Confirmación de ventas
- ✅ Gestión de concurrencia con Optimistic Locking
- ✅ Event Sourcing para auditoría completa

---

## ⭐ Características Principales

### Arquitectura y Diseño
- **Hexagonal Architecture** (Ports & Adapters)
- **Domain-Driven Design** (DDD)
- **Event Sourcing** para trazabilidad
- **CQRS** Command Side

### Patrones de Resiliencia
- **Circuit Breaker** - Resilience4j
- **Retry Pattern** - Con exponential backoff
- **Bulkhead** - Aislamiento de recursos
- **Rate Limiting** - Control de tráfico

### Control de Concurrencia
- **Optimistic Locking** - Campo version
- **Thread-Safe CSV Storage** - File locking + ReadWriteLock
- **Atomic Operations** - Garantía de consistencia

### Observabilidad
- **Spring Boot Actuator** - Health checks y métricas
- **Prometheus Metrics** - Exportación de métricas
- **Structured Logging** - SLF4J + Logback
- **OpenAPI 3.0** - Documentación automática

---

## 🏗️ Arquitectura

### Capas (Hexagonal Architecture)

```
┌─────────────────────────────────────────────┐
│         Infrastructure Layer                │
│  ┌──────────────┐      ┌─────────────────┐ │
│  │ REST API     │      │ CSV Persistence │ │
│  │ Controllers  │      │ Repositories    │ │
│  └──────┬───────┘      └────────┬────────┘ │
└─────────┼──────────────────────┼──────────┘
          │                      │
┌─────────▼──────────────────────▼──────────┐
│         Application Layer                  │
│  ┌──────────────────────────────────────┐ │
│  │  InventoryCommandServiceImpl         │ │
│  │  + Resilience4j Annotations          │ │
│  └──────────────┬───────────────────────┘ │
└─────────────────┼─────────────────────────┘
                  │
┌─────────────────▼─────────────────────────┐
│            Domain Layer                    │
│  ┌───────────────────────────────────┐    │
│  │ InventoryItem (Aggregate Root)    │    │
│  │ + Business Logic                  │    │
│  └───────────────────────────────────┘    │
│                                            │
│  ┌───────────────────────────────────┐    │
│  │ Ports (Interfaces)                │    │
│  │ - Input Ports  (Use Cases)        │    │
│  │ - Output Ports (Dependencies)     │    │
│  └───────────────────────────────────┘    │
└────────────────────────────────────────────┘
```

### Flujo de Datos

```
Cliente → REST Controller → Command DTO → Use Case Port
          ↓
    Application Service (+ Resilience4j)
          ↓
    Domain Logic (InventoryItem)
          ↓
    Repository Port → CSV Adapter
          ↓
    Event Store → Event Publisher
```

---

## 📦 Requisitos Previos

### Software Requerido

| Herramienta | Versión Mínima | Comando Verificación |
|-------------|----------------|---------------------|
| **Java JDK** | 17+ | `java -version` |
| **Maven** | 3.8+ | `mvn -version` |
| **Git** (opcional) | 2.x | `git --version` |

### Instalación de Requisitos

#### Windows
```powershell
# Instalar con Chocolatey
choco install openjdk17 maven

# Verificar
java -version
mvn -version
```

#### macOS
```bash
# Instalar con Homebrew
brew install openjdk@17 maven

# Verificar
java -version
mvn -version
```

#### Linux (Ubuntu/Debian)
```bash
# Instalar OpenJDK 17
sudo apt update
sudo apt install openjdk-17-jdk maven

# Verificar
java -version
mvn -version
```

---

## 🚀 Instalación

### Opción 1: Clonar desde Git

```bash
# Clonar repositorio
git clone https://github.com/KevinSantiago123/meli_test.git

# Navegar al servicio
cd inventory-management-system/inventory.command.service
```

### Opción 2: Descomprimir ZIP

```bash
# Descomprimir
unzip inventory-management-system.zip

# Navegar al servicio
cd inventory-management-system/inventory.command.service
```

### Compilar el Proyecto

```bash
# Descargar dependencias y compilar
mvn clean install

# Saltar tests (más rápido)
mvn clean install -DskipTests
```

**Salida esperada:**
```
[INFO] BUILD SUCCESS
[INFO] Total time:  45.123 s
```

---

## ⚙️ Configuración

### Variables de Entorno (Opcional)

```bash
# Puerto del servidor (default: 8081)
export SERVER_PORT=8081

# Ruta de almacenamiento CSV (default: src/main/resources/data)
export CSV_STORAGE_PATH=/ruta/custom/data

# Nivel de logging (default: INFO)
export LOGGING_LEVEL=DEBUG
```

### Archivo application.yml

Ubicación: `src/main/resources/application.yml`

```yaml
server:
  port: ${SERVER_PORT:8081}

csv:
  storage:
    path: ${CSV_STORAGE_PATH:src/main/resources/data}

logging:
  level:
    root: ${LOGGING_LEVEL:INFO}
    com.inventory: DEBUG
```

---

## 🎮 Ejecución

### Método 1: Maven Spring Boot Plugin (Desarrollo)

```bash
# Ejecutar directamente
mvn spring-boot:run

# Con perfil específico
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Con argumentos JVM
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx512m"
```

### Método 2: JAR Ejecutable (Producción)

```bash
# Compilar JAR
mvn clean package

# Ejecutar JAR
java -jar target/inventory-command-service-1.0.0.jar

# Con opciones
java -jar -Xmx512m -Dserver.port=8081 target/inventory-command-service-1.0.0.jar
```

### Método 3: IDE (IntelliJ IDEA / Eclipse)

#### IntelliJ IDEA
1. Abrir proyecto: `File → Open → Seleccionar pom.xml`
2. Esperar indexación de Maven
3. Buscar: `InventoryCommandServiceApplication.java`
4. Click derecho → `Run 'InventoryCommandServiceApplication'`

#### Eclipse
1. Importar: `File → Import → Maven → Existing Maven Projects`
2. Seleccionar carpeta del proyecto
3. Click derecho en proyecto → `Run As → Spring Boot App`

### Verificar que Está Corriendo

```bash
# Health check
curl http://localhost:8081/actuator/health

# Respuesta esperada
{
  "status": "UP"
}
```

**Logs esperados:**
```
INFO  InventoryCommandServiceApplication - Starting application...
INFO  TomcatWebServer - Tomcat started on port(s): 8081 (http)
INFO  InventoryCommandServiceApplication - Started in 5.234 seconds
```

---

## 🧪 Testing

### Ejecutar Todos los Tests

```bash
# Tests unitarios + integración
mvn test

# Con reporte de cobertura
mvn test jacoco:report

# Solo tests unitarios
mvn test -Dtest=*Test

# Solo tests de integración
mvn test -Dtest=*IT
```

### Tests Específicos

```bash
# Ejecutar una clase de test
mvn test -Dtest=InventoryItemTest

# Ejecutar un método específico
mvn test -Dtest=InventoryItemTest#shouldCreateInventoryItem
```

### Cobertura de Código

```bash
# Generar reporte Jacoco
mvn clean test jacoco:report

# Ver reporte en navegador
open target/site/jacoco/index.html
```

**Cobertura objetivo:** >80% en capa de dominio

### Tests Manuales con curl

```bash
# Crear producto
curl -X POST http://localhost:8081/api/v1/inventory \
  -H "Content-Type: application/json" \
  -d '{
    "storeId": "STORE-001",
    "productId": "PROD-001",
    "productName": "Laptop Dell",
    "quantity": 100,
    "minThreshold": 10,
    "userId": "admin"
  }'

# Actualizar stock
curl -X PUT http://localhost:8081/api/v1/inventory/{id}/stock \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 10,
    "operation": "SUBTRACT",
    "version": 1,
    "reason": "SALE",
    "userId": "cashier"
  }'

# Reservar stock
curl -X POST http://localhost:8081/api/v1/inventory/{id}/reserve \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 5,
    "reservationId": "ORDER-123",
    "version": 2,
    "userId": "system"
  }'
```

---

## 📚 API Documentation

### Swagger UI (Recomendado)

Accede a la documentación interactiva:

**URL:** http://localhost:8081/swagger-ui.html

Características:
- 📖 Documentación completa de endpoints
- 🧪 Probar APIs directamente desde el navegador
- 📝 Ejemplos de request/response
- 🔐 Soporte para autenticación (futuro)

### OpenAPI JSON/YAML

```bash
# Obtener especificación OpenAPI
curl http://localhost:8081/v3/api-docs

# Formato YAML
curl http://localhost:8081/v3/api-docs.yaml
```

### Endpoints Principales

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/inventory` | Crear producto |
| PUT | `/api/v1/inventory/{id}/stock` | Actualizar stock |
| POST | `/api/v1/inventory/{id}/reserve` | Reservar stock |
| POST | `/api/v1/inventory/{id}/release` | Liberar reserva |
| POST | `/api/v1/inventory/{id}/confirm` | Confirmar venta |
| DELETE | `/api/v1/inventory/{id}` | Eliminar producto |
| GET | `/api/v1/events/pending` | Obtener eventos |

---

## 🎨 Patrones Implementados

### Arquitectónicos

#### 1. Hexagonal Architecture (Ports & Adapters)
```
Domain (Core) ←→ Ports (Interfaces) ←→ Adapters (Infrastructure)
```

**Beneficios:**
- ✅ Lógica de negocio aislada
- ✅ Testeable sin infraestructura
- ✅ Adaptadores intercambiables

#### 2. CQRS (Command Query Responsibility Segregation)
```
Command Service (Escritura) ← → Query Service (Lectura)
```

**Beneficios:**
- ✅ Optimización independiente
- ✅ Escalabilidad horizontal
- ✅ Modelos especializados

#### 3. Event Sourcing
```
Comando → Cambio de Estado → Evento Inmutable → Event Store
```

**Beneficios:**
- ✅ Auditoría completa
- ✅ Reconstrucción de estado
- ✅ Trazabilidad temporal

### Resiliencia

#### 1. Circuit Breaker
```java
@CircuitBreaker(name = "inventoryCommand", fallbackMethod = "fallback")
public InventoryItem createInventoryItem(CreateInventoryItemCommand command) {
    // Lógica protegida
}
```

**Estados:**
- 🟢 CLOSED: Funcionando normal
- 🔴 OPEN: Fallo detectado, rechaza llamadas
- 🟡 HALF_OPEN: Permite llamadas de prueba

#### 2. Retry Pattern
```java
@Retry(name = "inventoryCommand")
public InventoryItem updateStock(UpdateStockCommand command) {
    // Se reintenta automáticamente si falla
}
```

**Configuración:**
- Max attempts: 3
- Backoff: Exponencial (500ms, 1s, 2s)

#### 3. Bulkhead
```java
@Bulkhead(name = "inventoryCommand")
```

**Limita:** 25 llamadas concurrentes máximo

#### 4. Rate Limiter
```yaml
limit-for-period: 100
limit-refresh-period: 1s
```

### Concurrencia

#### Optimistic Locking
```java
public class InventoryItem {
    private Long version;  // Incrementa en cada update
    
    public void validateVersion(Long expectedVersion) {
        if (!this.version.equals(expectedVersion)) {
            throw new OptimisticLockException();
        }
    }
}
```

**Flujo:**
1. Cliente lee item (version: 1)
2. Cliente modifica y envía (expectedVersion: 1)
3. Servidor valida que version actual == expectedVersion
4. Si coincide: actualiza y version++ (ahora 2)
5. Si NO coincide: lanza error 409 Conflict

---

## 📊 Monitoreo

### Spring Boot Actuator

#### Endpoints Disponibles

```bash
# Health check
curl http://localhost:8081/actuator/health

# Información de la aplicación
curl http://localhost:8081/actuator/info

# Métricas
curl http://localhost:8081/actuator/metrics

# Estado de Circuit Breakers
curl http://localhost:8081/actuator/circuitbreakers

# Métricas específicas
curl http://localhost:8081/actuator/metrics/http.server.requests

# Prometheus metrics
curl http://localhost:8081/actuator/prometheus
```

#### Health Check Detallado

```bash
curl http://localhost:8081/actuator/health | jq
```

**Respuesta:**
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 250000000000
      }
    },
    "ping": {
      "status": "UP"
    },
    "circuitBreakers": {
      "status": "UP"
    }
  }
}
```

### Logs

#### Ubicación de Logs

```bash
# Logs en consola
mvn spring-boot:run

# Logs en archivo
tail -f logs/inventory-command-service.log

# Filtrar por nivel
grep "ERROR" logs/inventory-command-service.log

# Filtrar por operación
grep "createInventoryItem" logs/inventory-command-service.log
```

#### Niveles de Log

```yaml
# application.yml
logging:
  level:
    root: INFO
    com.inventory: DEBUG
    org.springframework: INFO
    io.github.resilience4j: DEBUG
```

### Métricas Prometheus

#### Configurar Prometheus

**prometheus.yml:**
```yaml
scrape_configs:
  - job_name: 'inventory-command-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8081']
```

#### Métricas Clave

- `http_server_requests_seconds` - Latencia de requests
- `resilience4j_circuitbreaker_state` - Estado de circuit breaker
- `resilience4j_retry_calls` - Llamadas con retry
- `jvm_memory_used_bytes` - Uso de memoria

---

## 🤝 Contribución

### Estándares de Código

- **Java Code Style:** Google Java Style Guide
- **Naming:** camelCase para métodos, PascalCase para clases
- **Testing:** Cobertura mínima 80% en dominio
- **Documentation:** Javadoc en clases públicas

### Commits Semánticos

```
feat: Nueva funcionalidad
fix: Corrección de bug
docs: Cambios en documentación
refactor: Refactorización de código
test: Agregar o modificar tests
chore: Tareas de mantenimiento
```

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver archivo [LICENSE](LICENSE) para detalles.

---

## 👥 Equipo

- **Desarrollador Principal:** Kevin Santiago Castañeda Trujillo

---

## 📞 Soporte

- **Email:** kcastanedat@gmail.com

---

## 📈 Roadmap

- [x] Implementación básica CRUD
- [x] Event Sourcing
- [x] Resilience patterns
- [x] Optimistic Locking
- [ ] PostgreSQL adapter
- [ ] Kafka integration
- [ ] Kubernetes deployment
- [ ] GraphQL API

---

**Versión:** 1.0.0  
**Última Actualización:** Octubre 2025  
**Estado:** ✅ Producción Ready