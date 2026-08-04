-- =============================================================================
-- data.sql — Datos semilla para H2 en memoria
-- Ejecutado automáticamente por Spring Boot al arrancar (spring.sql.init.mode=always)
-- después de que Hibernate crea el esquema (spring.jpa.defer-datasource-initialization=true)
-- =============================================================================

-- Insertamos productos de ejemplo con categorías de la API externa.
-- Los categoryId (1, 2, 3) corresponden a IDs reales de api.escuelajs.co.
-- Los categoryName son desnormalizados para ahorrar llamadas a la API.

INSERT INTO products (name, price, stock, category_id, category_name)
VALUES
    ('Laptop Pro 15',       1299.99, 25,  1, 'Clothes'),
    ('Teclado Mecánico RGB', 89.99,  50,  1, 'Clothes'),
    ('Monitor 4K 27"',      449.99,  10,  2, 'Electronics'),
    ('Auriculares Noise Cancel', 199.99, 30, 2, 'Electronics'),
    ('Silla Ergonómica',    599.99,   8,  3, 'Furniture'),
    ('Escritorio Ajustable', 349.99,  5,  3, 'Furniture'),
    ('Mouse Inalámbrico',    45.99,  75,  2, 'Electronics'),
    ('Webcam HD 1080p',      79.99,  40,  2, 'Electronics');
