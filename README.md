# Commerce API

API REST para la administración de productos y categorías de un comercio, desarrollada como desafío técnico de backend con Spring Boot 3 y Java 17.

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.3.4 |
| Persistencia | Spring Data JPA + Hibernate 6 + H2 (en memoria) |
| Filtros dinámicos | JPA Criteria API (Specifications) |
| Caché | Spring Cache + Caffeine (TTL 10 min, fallback stale-while-revalidate) |
| Cliente HTTP | `RestClient` (Spring Boot 3.2+) con timeouts configurados |
| Validaciones | Jakarta Bean Validation 3 |
| Documentación | SpringDoc OpenAPI 3 / Swagger UI |
| Build | Gradle 8.10 (Gradle Wrapper incluido) |
| Tests | JUnit 5 + Mockito + MockRestServiceServer |

---

## Requisitos de ejecución

- **JDK 17** — puede descargarse gratis desde [Adoptium Temurin](https://adoptium.net/temurin/releases/?version=17) 
  - Verificar con `java -version` (debe mostrar `17.x.x`).
  **Nota para macOS:** Descargar el paquete `.pkg` con arquitectura `aarch64` (para chips Apple Silicon M1/M2/M3/M4). Una vez instalado, activalo en la terminal ejecutando:
    ```bash
    export JAVA_HOME=$(/usr/libexec/java_home -v 17)
    ```
- **Sin instalación de Gradle**: el proyecto incluye **Gradle Wrapper** (`gradlew` / `gradlew.bat`), que descarga automáticamente Gradle 8.10 la primera vez.
- Conexión a internet al primer arranque (para descargar dependencias) y para consumir la API externa de categorías.

---

## Compilar y ejecutar

### Clonar el repositorio

```bash
git clone https://github.com/dollalfonsina/commerce-api.git
cd commerce-api
```

### Ejecutar la aplicación

```bash
# Windows (CMD / PowerShell)
.\gradlew.bat bootRun

# Windows (Git Bash)
./gradlew bootRun

# Linux / macOS
./gradlew bootRun
```

La aplicación arranca en `http://localhost:8080`.

### Ejecutar los tests

```bash
# Windows
.\gradlew.bat test

# Linux / macOS
./gradlew test
```

Reporte HTML generado en `build/reports/tests/test/index.html`.

### Solo compilar (sin ejecutar)

```bash
./gradlew build
```

---

## URLs disponibles

| URL | Descripción |
|-----|-------------|
| `http://localhost:8080/products` | CRUD de productos |
| `http://localhost:8080/categories` | Categorías desde API externa (cacheadas 10 min) |
| `http://localhost:8080/swagger-ui.html` | **Swagger UI** — probar todos los endpoints |
| `http://localhost:8080/v3/api-docs` | Especificación OpenAPI 3 (JSON) |
| `http://localhost:8080/h2-console` | Consola H2 |

**Parámetros de conexión H2:**
- JDBC URL: `jdbc:h2:mem:commercedb`
- User: `sa` / Password: *(vacío)*

---

## Probar la API con curl

### GET /products — listar con paginación y filtros

```bash
# Todos los productos (página 0, 20 por página por defecto)
curl -X GET "http://localhost:8080/products"

# Paginación: página 1, 5 registros, ordenado por precio descendente
curl -X GET "http://localhost:8080/products?page=0&size=5&sort=price,desc"

# Filtrar por nombre (parcial, case-insensitive)
curl -X GET "http://localhost:8080/products?name=laptop"

# Filtrar por rango de precio y mínimo de stock y categoría
curl -X GET "http://localhost:8080/products?name=pelota&minPrice=10&maxPrice=50&minStock=5&categoryId=4"

# Combinar múltiples filtros
curl -X GET "http://localhost:8080/products?name=monitor&minPrice=200&categoryId=2&page=0&size=10&sort=price,asc"
```

**Ejemplo de respuesta `Page<ProductResponse>`:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Laptop Pro 15",
      "price": 1299.99,
      "stock": 25,
      "categoryId": 2,
      "categoryName": "Electrónica"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": { "sorted": false, "empty": true, "unsorted": true }
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true,
  "numberOfElements": 1,
  "empty": false
}
```

### GET /products/{id}

```bash
curl -X GET "http://localhost:8080/products/1"
```

### POST /products — crear

```bash
curl -X POST "http://localhost:8080/products" \
  -H "Content-Type: application/json" \
  -d '{"name":"Escritorio Gamer","price":299.99,"stock":15,"categoryId":3}'
```

**Respuesta 201 Created:**
```json
{
  "id": 9,
  "name": "Escritorio Gamer",
  "price": 299.99,
  "stock": 15,
  "categoryId": 3,
  "categoryName": "Muebles"
}
```

### PUT /products/{id} — actualizar

```bash
curl -X PUT "http://localhost:8080/products/1" \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop Pro 15 - Plus","price":1349.99,"stock":20,"categoryId":2}'
```

### DELETE /products/{id}

```bash
curl -X DELETE "http://localhost:8080/products/1"
# Respuesta: 204 No Content
```

### GET /categories

```bash
curl -X GET "http://localhost:8080/categories"
```

**Respuesta 200 OK (datos de la API externa, cacheados):**
```json
[
  { "id": 1, "name": "Ropa" },
  { "id": 2, "name": "Electrónica" },
  { "id": 3, "name": "Muebles" }
]
```

### Ejemplos de errores

```bash
# 400 — JSON malformado
curl -X POST "http://localhost:8080/products" \
  -H "Content-Type: application/json" \
  -d '{invalid-json}'

# 400 — Tipo de parámetro inválido
curl -X GET "http://localhost:8080/products/abc"

# 404 — Recurso inexistente
curl -X GET "http://localhost:8080/products/999"

# 404 — Ruta inexistente
curl -X GET "http://localhost:8080/ruta-que-no-existe"

# 405 — Método no soportado
curl -X PATCH "http://localhost:8080/products/1"

# 502 — API externa de categorías no disponible (sin caché previo)
# (simular cortando internet o bloqueando api.escuelajs.co)
```

**Estructura de `ErrorResponse` (todos los errores tienen este formato):**
```json
{
  "timestamp": "2024-08-04T18:30:00.123",
  "status": 404,
  "error": "Not Found",
  "message": "Producto no encontrado/a con id: 999",
  "path": "/products/999"
}
```

---

## Decisiones técnicas de arquitectura

### 1. Gestión de Caché (Caffeine) con fallback ante caídas

Las categorías provienen de una API externa pública que puede presentar interrupciones. Para garantizar la disponibilidad del sistema, se implementó un manejo de caché manual en `CategoryServiceImpl`.

* **Manejo de caídas:** La app intenta consultar la API externa y guardar los datos en memoria por 10 minutos. Si la API externa se cae o no hay internet, el sistema captura el error y devuelve automáticamente la última lista de categorías que tenía guardada.
* **Por qué caché manual y no `@Cacheable`:** Se usó `CacheManager` en lugar de la anotación automática `@Cacheable` para tener control total dentro de un `try/catch`. Esto permite interceptar los fallos de la API externa y servir los datos del caché como alternativa, algo que la anotación automática no permite hacer.

### 2. Búsqueda y Filtros Dinámicos (JPA Specifications)

Se implementó una búsqueda flexible donde todos los parámetros de filtrado (nombre, precio mínimo/máximo, stock y categoría) son opcionales:

* **Filtros combinables e independientes:** Cada filtro se evalúa por separado. Si un parámetro no se envía en la petición, el sistema lo ignora automáticamente sin agregar condiciones innecesarias a la consulta SQL.
* **Búsquedas seguras y consistentes:** Los filtros de texto permiten buscar sin distinguir mayúsculas de minúsculas y limpian caracteres especiales de SQL (como `%` o `_`) para evitar errores en las búsquedas.

### 3. Desvinculación de Categorías (Sin `@Entity` propia)

Dado que las categorías pertenecen a un servicio externo, no se mapean como una tabla/entidad relacional propia en la base de datos local:

* **Almacenamiento directo:** La tabla `Product` guarda directamente los campos `categoryId` y `categoryName`. Esto evita tener que sincronizar bases de datos entre distintos sistemas.
* **Integración tolerante a cambios:** Se utilizó la anotación `@JsonIgnoreProperties(ignoreUnknown = true)` en los DTOs para procesar únicamente los datos necesarios de la API externa, ignorando campos secundarios (como imágenes o fechas) y evitando fallos si la API externa cambia su formato.

### 4. Consumo de API Externa con Protecciones y Timeouts (`RestClient`)

Para la comunicación con la API externa de categorías se configuraron límites de tiempo y manejo explícito de errores:

* **Límites de espera (Timeouts):** Se establece un tiempo máximo de 5 segundos para conectar y 10 segundos para recibir respuesta. Si la API externa se demora más de eso, la petición se interrumpe para no bloquear la aplicación.
* **Respuesta clara ante fallos (HTTP 502):** Si la API externa falla, no hay conexión o se supera el tiempo límite, el sistema captura el problema y lo traduce a una respuesta **502 Bad Gateway**, informando correctamente que el error proviene de un servicio de terceros.

### 5. Manejo de errores con `@RestControllerAdvice`

`GlobalExceptionHandler` cubre 11 casos con códigos HTTP correctos:

| Excepción | HTTP | Cuándo ocurre |
|---|---|---|
| `ResourceNotFoundException` | 404 | Producto o categoría no existe |
| `MethodArgumentNotValidException` | 400 | Falla `@Valid` en request body |
| `HttpMessageNotReadableException` | 400 | JSON malformado en el body |
| `MethodArgumentTypeMismatchException` | 400 | Tipo de parámetro inválido (ej: `/products/abc`) |
| `PropertyReferenceException` | 400 | Propiedad de ordenamiento inválida (ej: `sort=inexistente`) |
| `NoResourceFoundException` | 404 | Ruta no encontrada |
| `HttpRequestMethodNotSupportedException` | 405 | Método HTTP no soportado (ej: `PATCH`) |
| `HttpMediaTypeNotSupportedException` | 415 | Tipo de contenido no soportado (ej: `text/plain` en POST) |
| `HttpMediaTypeNotAcceptableException` | 406 | Formato de respuesta no aceptable por el cliente |
| `ExternalApiException` | 502 | API externa de categorías falla |
| `Exception` (fallback) | 500 | Error no contemplado — loguea stacktrace, no expone internals |

### 6. Base de Datos H2 en Memoria y Carga Inicial de Datos

Para simplificar la ejecución local y las pruebas del proyecto, se configuró una base de datos H2 en memoria:

* **Portabilidad e inspección inmediata:** No requiere la instalación de un servidor de base de datos externo. Los datos son volátiles (se reinician en cada ejecución) y se pueden inspeccionar visualmente desde la consola web en `/h2-console`.
* **Semilla de datos iniciales (`data.sql`):** La base de datos se puebla automáticamente al arrancar mediante el archivo `data.sql`. Esto garantiza disponer de datos de muestra listos para consultar y manipular sin pasos de configuración previos.

### 7. Validaciones de Negocio en Precio y Stock

Se aplican validaciones con Bean Validation (`jakarta.validation`) para asegurar la integridad de los datos de entrada:

* **Precio estrictamente positivo (`@Positive`):** Garantiza que todo producto tenga un precio mayor a $0.00, acorde a las reglas del dominio de comercio electrónico.
* **Límite máximo de stock (`@Max`):** Define un tope de 1.000.000 de unidades en stock para proteger la base de datos frente a valores desorbitados o inválidos.

