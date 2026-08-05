-- data.sql — Datos semilla para H2 en memoria

INSERT INTO products (name, price, stock, category_id, category_name) VALUES
    ('Laptop Pro 15',            1299.99,  25, 2, 'Electronics'),
    ('Teclado Mecanico RGB',       89.99,  50, 2, 'Electronics'),
    ('Monitor 4K 27"',            449.99,  10, 2, 'Electronics'),
    ('Auriculares Noise Cancel',  199.99,  30, 2, 'Electronics'),
    ('Silla Ergonomica',          599.99,   8, 3, 'Furniture'),
    ('Escritorio Ajustable',      349.99,   5, 3, 'Furniture'),
    ('Mouse Inalambrico',          45.99,  75, 2, 'Electronics'),
    ('Camiseta Deportiva',         29.99, 100, 1, 'Clothes');
