# 🚀 Guía de Ejecución - Sistema de Gestión de Inventario

Esta guía te llevará paso a paso para ejecutar el proyecto completo del sistema de gestión de inventario distribuido con arquitectura CQRS.

---

## 📋 Pre-requisitos

### Software Requerido

| Herramienta | Versión Mínima | Verificación |
|-------------|----------------|--------------|
| **Java JDK** | 17+ | `java -version` |
| **Maven** | 3.8+ | `mvn -version` |
| **Git** (opcional) | 2.x | `git --version` |

### Instalación Rápida de Pre-requisitos

#### Windows (PowerShell como Administrador)
```powershell
# Instalar con Chocolatey
choco install openjdk17 maven git

# Verificar instalación
java -version
mvn -version
git --version
```

#### macOS
```bash
# Instalar con Homebrew
brew install openjdk@17 maven git

# Verificar instalación
java -version
mvn -version
git --version
```

#### Linux (Ubuntu/Debian)
```bash
# Actualizar repositorios
sudo apt update

# Instalar dependencias
sudo apt install openjdk-17-jdk maven git

# Verificar instalación
java -version
mvn -version
git --version
```

---

## 📦 Paso 1: Obtener el Código

### Opción A: Clonar desde GitHub (Recomendado)

```bash
# Clonar el repositorio
git clone https://github.com/KevinSantiago123/meli_test.git

# Navegar al directorio del proyecto
cd meli_test
```

### Opción B: Descargar ZIP

1. Ir al adjunto de la prueba HackerRank
2. Click en `Code` → `Download ZIP`
3. Descomprimir el archivo
4. Abrir terminal en la carpeta descomprimida

---

## 🏗️ Paso 2: Compilar el Proyecto

### Compilar Ambos Servicios

```bash
# Navegar a la raíz del proyecto
cd meli_test

# Compilar Command Service
cd inventory-command-service
mvn clean install
cd ..

# Compilar Query Service
cd inventory-query-service
mvn clean install
cd ..
```

**Salida esperada de cada servicio:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 30-45 seconds
```

---

## 🎮 Paso 3: Ejecutar los Servicios

### ⚠️ IMPORTANTE: Orden de Ejecución

**SIEMPRE ejecutar en este orden:**
1. **Primero:** Command Service (Puerto 8081)
2. **Segundo:** Query Service (Puerto 8082)

El Query Service depende del Command Service para sincronización de eventos.

---

### Método 1: Ejecución con Maven (Recomendado para Desarrollo)

#### Terminal 1 - Command Service

```bash
# Navegar al Command Service
cd inventory-command-service

# Ejecutar
mvn spring-boot:run
```

**Espera a ver este mensaje:**
```
INFO  InventoryCommandServiceApplication - Started in X seconds
INFO  TomcatWebServer - Tomcat started on port(s): 8081
```

#### Terminal 2 - Query Service

```bash
# Abrir NUEVA terminal
# Navegar al Query Service
cd inventory-query-service

# Ejecutar
mvn spring-boot:run
```

**Espera a ver este mensaje:**
```
INFO  InventoryQueryServiceApplication - Started in X seconds
INFO  TomcatWebServer - Tomcat started on port(s): 8082
INFO  InventoryEventConsumer - Event consumer initialized
```

---

### Método 2: Ejecución con JAR (Recomendado para Producción)

#### Paso 1: Compilar JARs

```bash
# Command Service
cd inventory-command-service
mvn clean package
cd ..

# Query Service
cd inventory-query-service
mvn clean package
cd ..
```

#### Paso 2: Ejecutar JARs

**Terminal 1 - Command Service:**
```bash
cd inventory-command-service/target
java -jar inventory-command-service-1.0.0.jar
```

**Terminal 2 - Query Service:**
```bash
cd inventory-query-service/target
java -jar inventory-query-service-1.0.0.jar
```

---

### Método 3: Ejecución desde IDE

#### IntelliJ IDEA

1. **Abrir Proyecto:**
   - `File` → `Open`
   - Seleccionar carpeta `meli_test`
   - Esperar a que Maven indexe

2. **Ejecutar Command Service:**
   - Navegar a: `inventory-command-service/src/main/java/com/meli/inventory/command/service/InventoryCommandServiceApplication.java`
   - Click derecho → `Run 'InventoryCommandServiceApplication'`
   - Esperar a que inicie (ver consola)

3. **Ejecutar Query Service:**
   - Navegar a: `inventory-query-service/src/main/java/com/meli/inventory/query/service/InventoryQueryServiceApplication.java`
   - Click derecho → `Run 'InventoryQueryServiceApplication'`

#### Eclipse

1. **Importar Proyecto:**
   - `File` → `Import` → `Maven` → `Existing Maven Projects`
   - Seleccionar carpeta `meli_test`
   - Click `Finish`

2. **Ejecutar Servicios:**
   - Click derecho en `InventoryCommandServiceApplication.java`
   - `Run As` → `Spring Boot App`
   - Repetir para `InventoryQueryServiceApplication.java`

---

## ✅ Paso 4: Verificar que Todo Funciona

### 1. Verificar Command Service

```bash
# Health check
curl http://localhost:8081/actuator/health

# Respuesta esperada
{
  "status": "UP"
}
```

### 2. Verificar Query Service

```bash
# Health check
curl http://localhost:8082/actuator/health

# Respuesta esperada
{
  "status": "UP",
  "components": {
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

### 3. Acceder a Swagger UI

**Command Service (Escritura):**
- URL: http://localhost:8081/swagger-ui.html
- Aquí puedes crear, actualizar y eliminar inventario

**Query Service (Lectura):**
- URL: http://localhost:8082/swagger-ui.html
- Aquí puedes consultar el inventario

---

## 🧪 Paso 5: Probar el Sistema

### Flujo Completo de Prueba

#### 1. Crear un Producto (Command Service)

```bash
curl -X POST http://localhost:8081/api/v1/inventory \
  -H "Content-Type: application/json" \
  -d '{
    "storeId": "STORE-001",
    "productId": "PROD-001",
    "productName": "Laptop Dell XPS 15",
    "quantity": 100,
    "minThreshold": 10,
    "userId": "admin"
  }'
```

**Respuesta esperada:**
```json
{
  "id": "abc-123-def",
  "storeId": "STORE-001",
  "productId": "PROD-001",
  "productName": "Laptop Dell XPS 15",
  "quantity": 100,
  "version": 1
}
```

**⏰ Espera 10 segundos** para que el Query Service sincronice el evento.

#### 2. Consultar el Producto (Query Service)

```bash
# Por disponibilidad
curl "http://localhost:8082/api/v1/inventory/availability?productId=PROD-001&storeId=STORE-001"

# Listar inventario de la tienda
curl "http://localhost:8082/api/v1/inventory/store/STORE-001"
```

**Respuesta esperada:**
```json
{
  "productId": "PROD-001",
  "storeId": "STORE-001",
  "available": true,
  "quantity": 100,
  "availableStock": 100,
  "productName": "Laptop Dell XPS 15"
}
```

#### 3. Actualizar Stock (Command Service)

```bash
# Reemplaza {id} con el ID que obtuviste al crear
curl -X PUT http://localhost:8081/api/v1/inventory/{id}/stock \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 10,
    "operation": "SUBTRACT",
    "version": 1,
    "reason": "SALE",
    "userId": "cashier"
  }'
```

**⏰ Espera 10 segundos** y vuelve a consultar en Query Service para ver el cambio.

#### 4. Reservar Stock (Command Service)

```bash
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

## 🎯 Puertos y URLs Importantes

| Servicio | Puerto | Swagger UI | Health Check |
|----------|--------|------------|--------------|
| **Command Service** | 8081 | http://localhost:8081/swagger-ui.html | http://localhost:8081/actuator/health |
| **Query Service** | 8082 | http://localhost:8082/swagger-ui.html | http://localhost:8082/actuator/health |

**¡Listo! Tu sistema de inventario distribuido está corriendo. 🎉**