SET @now = NOW(6);

INSERT INTO tb_category (name, created_at, updated_at)
SELECT 'Eletrônicos', @now, @now
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_category WHERE name = 'Eletrônicos');

INSERT INTO tb_category (name, created_at, updated_at)
SELECT 'Roupas', @now, @now
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_category WHERE name = 'Roupas');

INSERT INTO tb_category (name, created_at, updated_at)
SELECT 'Livros', @now, @now
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_category WHERE name = 'Livros');

INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Smartphone XYZ', 'Android 14, 128GB', 1999.90, 'https://example.com/img/smartphone.png', @now
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Smartphone XYZ');

INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Camiseta Básica', '100% algodão', 49.90, NULL, @now
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Camiseta Básica');

INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Livro Clean Code', 'Robert C. Martin', 139.90, NULL, @now
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Livro Clean Code');

INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id
FROM tb_product p
         JOIN tb_category c ON c.name = 'Eletrônicos'
WHERE p.name = 'Smartphone XYZ'
  AND NOT EXISTS (SELECT 1
                  FROM tb_product_category pc
                  WHERE pc.product_id = p.id
                    AND pc.category_id = c.id);

INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id
FROM tb_product p
         JOIN tb_category c ON c.name = 'Roupas'
WHERE p.name = 'Camiseta Básica'
  AND NOT EXISTS (SELECT 1
                  FROM tb_product_category pc
                  WHERE pc.product_id = p.id
                    AND pc.category_id = c.id);

INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id
FROM tb_product p
         JOIN tb_category c ON c.name = 'Livros'
WHERE p.name = 'Livro Clean Code'
  AND NOT EXISTS (SELECT 1
                  FROM tb_product_category pc
                  WHERE pc.product_id = p.id
                    AND pc.category_id = c.id);
