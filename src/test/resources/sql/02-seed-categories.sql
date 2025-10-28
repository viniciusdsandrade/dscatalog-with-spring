INSERT INTO tb_category (name)
SELECT 'Eletrônicos'
WHERE NOT EXISTS (SELECT 1 FROM tb_category WHERE name = 'Eletrônicos');

INSERT INTO tb_category (name)
SELECT 'Informática'
WHERE NOT EXISTS (SELECT 1 FROM tb_category WHERE name = 'Informática');

INSERT INTO tb_category (name)
SELECT 'Casa & Jardim'
WHERE NOT EXISTS (SELECT 1 FROM tb_category WHERE name = 'Casa & Jardim');