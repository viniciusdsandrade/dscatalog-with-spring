DROP SCHEMA IF EXISTS db_ds_catalog;
CREATE SCHEMA IF NOT EXISTS db_ds_catalog;
USE db_ds_catalog;


SHOW TABLES FROM db_ds_catalog;
SHOW FULL TABLES FROM db_ds_catalog;
DESCRIBE tb_category;
DESCRIBE tb_product;
DESCRIBE tb_product_category;


SELECT p.name                                                       AS produto,
       GROUP_CONCAT(DISTINCT c.name ORDER BY c.name SEPARATOR ', ') AS categorias
FROM tb_product_category pc
         JOIN tb_product p ON p.id = pc.product_id
         JOIN tb_category c ON c.id = pc.category_id
GROUP BY p.id, p.name
ORDER BY p.name;
