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

- **JDK 17** — descargalo gratis desde [Adoptium Temurin](https://adoptium.net/temurin/releases/?version=17) (elegí tu SO; en Mac con chip Apple Silicon usá la arquitectura `aarch64`).
  - Alternativas: macOS `brew install --cask temurin@17` · Ubuntu/Debian `sudo apt install openjdk-17-jdk`
  - Verificar con `java -version` (debe mostrar `17.x.x`).
  - Nota: si ya tenés Java 17 o superior, no hace falta instalar nada — el proyecto usa Gradle Toolchains y descarga el JDK 17 automáticamente al compilar (ver `settings.gradle`).
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
| `http://localhost:8080/h2-console` | Consola H2 _(solo demo — no usar en producción)_ |

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

### 1. Caché Caffeine con fallback stale-while-revalidate

Las categorías provienen de una API pública externa que puede estar caída o lenta. Se implementó una estrategia de caché manual en `CategoryServiceImpl`:

```java
public List<CategoryResponse> fetchAllCategories() {
    Cache cache = cacheManager.getCache("categories");
    try {
        List<CategoryResponse> fresh = categoryApiClient.fetchCategories();
        if (cache != null) cache.put(SimpleKey.EMPTY, fresh);
        return List.copyOf(fresh);
    } catch (ExternalApiException e) {
        // Fallback: servir la última versión conocida si existe
        List<CategoryResponse> stale = cache.get(SimpleKey.EMPTY, List.class);
        if (stale != null && !stale.isEmpty()) return List.copyOf(stale);
        throw e;
    }
}
```

**TTL de 10 minutos** (configurable en `CacheConfig`): equilibra frescura de datos con reducción de llamadas a la API externa. El fallback garantiza que `GET /categories` y `POST/PUT /products` sigan funcionando aunque la API externa esté caída, siempre que el caché tenga al menos una entrada válida.

**Por qué el caché está en el service y no en el client con `@Cacheable`**: `@Cacheable` en un método público del mismo bean no intercepta las llamadas internas (auto-invocación sin pasar por el proxy AOP de Spring). El caché manual en el service da control total sobre el comportamiento de fallback.

### 2. Filtros dinámicos con JPA Criteria API (`ProductSpecification`)

Cada filtro opcional es una `Specification<Product>` independiente que retorna `null` si el parámetro no se envía. `null` en JPA Specifications equivale a `TRUE` — Spring Data lo ignora y no agrega esa cláusula al `WHERE`. La composición es:

```java
Specification.where(nameContains(name))
    .and(priceGreaterThanOrEqual(minPrice))
    ...
```

Los filtros de texto escapan los caracteres especiales de SQL LIKE (`%`, `_`, `\`) y usan `Locale.ROOT` para consistencia de mayúsculas/minúsculas entre entornos.

**Por qué Specifications y no queries JPQL hardcodeadas**: escalabilidad (agregar un filtro nuevo = agregar un método estático), testabilidad unitaria de cada predicado, y composición declarativa sin `if/else` en el Service.

### 3. `Category` no es `@Entity` — desnormalización de `categoryId` y `categoryName`

Las categorías son administradas por un servicio externo. Tratarlas como entidades JPA locales requeriría sincronización con la API externa (problema difícil). La entidad `Product` almacena `categoryId` y `categoryName` como columnas simples. El `categoryName` puede quedar desactualizado si la API externa lo cambia; para este dominio es un trade-off aceptado.

**`@JsonIgnoreProperties(ignoreUnknown = true)` en `CategoryResponse`**: la API externa retorna campos adicionales (`image`, `creationAt`, `updatedAt`). Esta anotación hace la integración resiliente a cambios en el contrato externo.

### 4. `RestClient` con timeouts y manejo de resiliencia

```java
factory.setConnectTimeout(5_000);  // falla rápido si no conecta
factory.setReadTimeout(10_000);    // falla si la API no responde en 10s
```

`CategoryApiClient` captura `RestClientResponseException` (4xx/5xx HTTP) y `ResourceAccessException` (timeout, conexión rechazada) y los envuelve en `ExternalApiException`. `GlobalExceptionHandler` lo traduce a **HTTP 502 Bad Gateway**, comunicando que el error está en un servicio upstream.

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

**H2 en memoria con `ddl-auto=create-drop`**

**Por qué H2**: portabilidad total (no requiere instalación de BD), datos volátiles a propósito (ideal para demos), arranque instantáneo y la consola web integrada facilita la inspección. En un entorno real se usaría PostgreSQL o MySQL con `ddl-auto=validate` y migraciones Flyway/Liquibase.

**`data.sql` cargado automáticamente**: Spring ejecuta `data.sql` después de que Hibernate crea el esquema (`defer-datasource-initialization=true`), garantizando el orden correcto. Los datos iniciales de data.sql sirven como semilla local estática para pruebas. Al realizar peticiones POST o PUT, la aplicación valida y actualiza el nombre de la categoría en vivo consultando la API externa.

### 7. `@Positive` vs `@DecimalMin("0.00")` en precio

Se usa `@Positive` (precio > 0) porque un producto con precio $0.00 no tiene sentido en el dominio del comercio. Si el negocio necesitara productos gratuitos o muestras, se cambiaría a `@DecimalMin(value = "0.00", inclusive = true)`.

`@Max(1_000_000)` en stock es un límite de negocio razonable que evita inputs absurdos y protege la columna de base de datos.
