# Commerce API

API REST de administración de productos y categorías desarrollada con **Spring Boot 3.3**, **Java 17** y **Gradle**.

## Tecnologías
- Java 17 + Spring Boot 3.3.4
- Spring Data JPA + H2 (en memoria)
- Spring Cache + Caffeine (TTL 10 min)
- RestClient (integración API externa de categorías)
- JPA Specifications (filtros dinámicos)
- SpringDoc OpenAPI / Swagger UI
- Jakarta Validation

## Requisitos
- Java 17+ (el proyecto usa Gradle Wrapper, no necesitas instalar Gradle)

## Ejecutar

```bash
# Windows
.\gradlew.bat bootRun

# Linux / macOS
./gradlew bootRun
```

## URLs disponibles

| URL | Descripción |
|-----|-------------|
| `http://localhost:8080/products` | CRUD de productos |
| `http://localhost:8080/categories` | Categorías desde API externa (con caché) |
| `http://localhost:8080/swagger-ui.html` | Swagger UI |
| `http://localhost:8080/h2-console` | Consola H2 (JDBC: `jdbc:h2:mem:commercedb`) |

## Endpoints

```
GET    /products                        # Listar (filtros: name, minPrice, maxPrice, minStock, maxStock, categoryId)
GET    /products/{id}                   # Obtener por ID
POST   /products                        # Crear producto
PUT    /products/{id}                   # Actualizar producto
DELETE /products/{id}                   # Eliminar producto
GET    /categories                      # Listar categorías (cacheadas 10 min)
```
