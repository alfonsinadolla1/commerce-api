-- data.sql — Datos semilla para H2 en memoria
-- category_id y category_name desnormalizados (Opción A adoptada).
-- Categorías de referencia usadas en el seed:
--   1 -> Ropa
--   2 -> Electrónica
--   3 -> Muebles
-- NOTA: estos nombres son del seed local. La API externa (api.escuelajs.co)
-- puede devolver nombres distintos; si los datos divergen, los productos
-- existentes mostrarán el nombre desnormalizado del momento de su creación.

INSERT INTO products (name, price, stock, category_id, category_name) VALUES
    ('Laptop Pro 15',            1299.99,  25, 2, 'Electrónica'),
    ('Teclado Mecánico RGB',       89.99,  50, 2, 'Electrónica'),
    ('Monitor 4K 27"',            449.99,  10, 2, 'Electrónica'),
    ('Auriculares Noise Cancel',  199.99,  30, 2, 'Electrónica'),
    ('Silla Ergonómica',          599.99,   8, 3, 'Muebles'),
    ('Escritorio Ajustable',      349.99,   5, 3, 'Muebles'),
    ('Mouse Inalámbrico',          45.99,  75, 2, 'Electrónica'),
    ('Camiseta Deportiva',         29.99, 100, 1, 'Ropa');
