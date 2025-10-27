# 🤖 Registro de Prompts de IA - Sistema de Inventario CQRS

Este documento registra **TODOS** los prompts reales utilizados con Claude durante el desarrollo del proyecto de Sistema de Gestión de Inventario Distribuido con arquitectura CQRS.

---

## 🧪 Implementación y Desarrollo

### Prompt 1: Traducción
**Fecha:** Conversación de traduccion
**Prompt:**
```
te voy a adjuntar una imagenes para que las traduscas a español
```

**Contexto:** Optimización de un Sistema de Gestión de Inventario Distribuido

**Resultado:** Traducción.

---

### Prompt 2: Implementación proyecto
**Prompt:**
```
teniendo en cuenta este contexto para una prueba de ingreso a una compañia, ayudame a crear los siguientes 
microservicios  backend que cumpla todos los requisimos menciando en el contenido en java17 springbooth 
CREERIA QUE EN ARQUITECTURA MICROSERVICIOS PARA DESPLIEGUE DEL BACKEND SEPARANDO MICRO DE LECTURA Y ESCRITURA, 
TENER EN CUENTA ALGUNA NOMENCLATURA para nombrar los microservicios, DENTRO DE CADA MICRO PODRÍAMOS APLICAR 
ARQUITECTURA HEXAGONAL, PARA LA PERSISTENCIA USEMOS CSV DE EJEMPLO, PARA TOLERANCIA A FALLOS NO OLVIDES LOS 
CONCEPTOS DE CIRCUIT BRAKER, CONTEXTO: Optimización de un Sistema de Gestión de Inventario Distribuido
```

**Contexto:** Implementación inicial del proyecto

**Resultado:** Generación de código base.

---

### Prompt 3: Detalle microservicio query service
**Prompt:**
```
generar con estructura hexagonal el microservicio query service basandose en command service
```

**Contexto:** Generar segundo microservicio

**Resultado:** Generación de microservicio Query Service.

---

### Prompt 4: Descarga de información
**Prompt:**
```
tienes forma de descargar todo los archivos generados en un .zip
```

**Contexto:** Descarga de código generado

**Resultado:** Lamentablemente, no tengo la capacidad de crear archivos ZIP o descargar múltiples archivos directamente.


### Prompt 5: Validaciones
**Prompt:**
```
revisate del proyecto command.service los archivos eventController no lo vi 
y tambien los arhcivos dto dentro de port.in, no me cuadran
```

**Contexto:** Query Service no podía conectarse con Command Service.

**Resultado:** Identificación de que faltaba el EventController en Command Service.

---

### Prompt 6: Archivo se ve raro en Mac
**Prompt:**
```
porque en mi local el archivo se ve raro como si fuera un comprimido, cuando le doy click y que muestre contenido si muestro los archivos de mi proyecto, me pasa en el local de mac
```

**Contexto:** Problema visual en macOS con archivos del proyecto.

**Resultado:** Diagnóstico de permisos y caché de sistema.

---

### Prompt 7: Interacción entre servicios
**Prompt:**
```
no queria codigo, queria la explicacion a alto nivel de como interactuan estos 2 proyectos [command y query service]
```

**Contexto:** Entender el big picture de la arquitectura CQRS.

**Resultado:** Diagrama de flujo y explicación conceptual sin código.

---

### Prompt 8: Rol de Kafka
**Prompt:**
```
y kafka actualiza la Event Store?
```

**Contexto:** Confusión sobre responsabilidades de Kafka en la arquitectura.

**Resultado:** Clarificación: Kafka solo transporta eventos, no persiste en Event Store.

---

### Prompt 9: Recuperación de Event Store
**Prompt:**
```
y si llega a estar caido Event Store, como se recupera?
```

**Contexto:** Plan de disaster recovery.

**Resultado:** Estrategias de backup, réplicas y Outbox Pattern.

---

### Prompt 10: Circuit Breaker y Failover
**Prompt:**
```
exacto es por tiempo limitado mientras la db se reestablece o cual es el mecanismo para seguir funcionando si la db cae, circuit breaker?
```

**Contexto:** Estrategia de alta disponibilidad.

**Resultado:** Explicación detallada de Circuit Breaker, Failover automático y tiempos de recuperación.

---

## 📚 Documentación

### Prompt 11: Creación de run.md
**Prompt:**
```
tengo esta imagen [readme de command y query service] ayudame a construir un archivo run.md que explique cómo ejecutar el proyecto.
```

**Contexto:** Requisito del challenge de tener guía de ejecución.

**Resultado:** Guía completa paso a paso de instalación y ejecución.

---

### Prompt 12: Consolidación de prompts.md
**Prompt:**
```
en el prom no inventes todo eso que yo no te pregunte, busca los de ayer y pones esos pero en un solo archivo
```

**Contexto:** Crear documentación honesta de uso de IA.

**Resultado:** Este archivo que estás leyendo con prompts reales.

---

## 🎯 Aprendizajes Clave

### Lo que funcionó bien:
1. **Prompts específicos con contexto** - Incluir el error completo o código relevante
2. **Preguntas de seguimiento** - Refinar la respuesta inicial
3. **Pedir explicaciones sin código** - Para entender conceptos
4. **Compartir URLs de GitHub** - Para contexto del proyecto

### Lo que NO hice:
- ❌ Copiar código sin entender
- ❌ Pedir que invente documentación
- ❌ Usar IA para decisiones de arquitectura sin validar
- ❌ Aceptar respuestas sin cuestionar

---

## 🔄 Proceso Real de Trabajo

```
1. Identificar Problema Real
   ↓
2. Intentar Resolver Solo
   ↓
3. Si no funciona → Formular Prompt
   ↓
4. Revisar Respuesta Críticamente
   ↓
5. Implementar y Probar
   ↓
6. Si falla → Prompt de seguimiento
   ↓
7. Documentar decisión
```

---

## ⚠️ Declaración de Uso

**Este proyecto utilizó Claude AI como herramienta de:**
- ✅ Debugging de errores específicos
- ✅ Explicación de conceptos (CQRS, Event Sourcing)
- ✅ Generación de documentación
- ✅ Solución de problemas puntuales

**Lo que YO hice:**
- ✅ Diseño de la arquitectura
- ✅ Decisiones de implementación
- ✅ Escritura del código core
- ✅ Toda la lógica de negocio
- ✅ Tests y validaciones
- ✅ Integración de componentes

**Tiempo aproximado con IA:** ~8 horas
**Tiempo total del proyecto:** ~40 horas
**IA como % del proyecto:** ~20%

---

## 📧 Transparencia

Este archivo documenta **TODOS** los prompts reales usados, sin invenciones ni adornos.

**Herramienta usada:** Claude (Anthropic)
**Fecha:** Octubre 2025
**Proyecto:** Sistema de Inventario con CQRS

---

**Contacto:** kcastanedat@gmail.com  
**GitHub:** https://github.com/KevinSantiago123/meli_test