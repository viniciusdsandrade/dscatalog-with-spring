-- Smartphone X -> Eletrônicos
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id
FROM tb_product p
         JOIN tb_category c ON c.name = 'Eletrônicos'
WHERE p.name = 'Smartphone XYZ';

-- Travesseiro Ortopédico -> Casa e Jardim
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id
FROM tb_product p
         JOIN tb_category c ON c.name = 'Casa e Jardim'
WHERE p.name = 'Travesseiro Ortopédico';

-- Bola de Futebol Pro -> Esportes
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id
FROM tb_product p
         JOIN tb_category c ON c.name = 'Esportes'
WHERE p.name = 'Bola de Futebol Pro';
