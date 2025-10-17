DROP SCHEMA IF EXISTS db_ds_catalog;
CREATE SCHEMA IF NOT EXISTS db_ds_catalog;
USE db_ds_catalog;
/*----------------------------------------------------------------------------------------------------*/
SHOW TABLES FROM db_ds_catalog;
SHOW FULL TABLES FROM db_ds_catalog;
/*----------------------------------------------------------------------------------------------------*/
DESCRIBE tb_category;
DESCRIBE tb_product;
DESCRIBE tb_product_category;
DESCRIBE tb_user;
DESCRIBE tb_role;
DESCRIBE tb_user_role;
/*----------------------------------------------------------------------------------------------------*/
SELECT * FROM tb_category;
SELECT * FROM tb_product;
SELECT * FROM tb_product_category;
SELECT * FROM tb_user;
SELECT * FROM tb_role;
SELECT * FROM tb_user_role;
/*----------------------------------------------------------------------------------------------------*/
SELECT p.name                                                       AS produto,
       GROUP_CONCAT(DISTINCT c.name ORDER BY c.name SEPARATOR ', ') AS categorias
FROM tb_product_category pc
         JOIN tb_product p ON p.id = pc.product_id
         JOIN tb_category c ON c.id = pc.category_id
GROUP BY p.id, p.name
ORDER BY p.name;
/*----------------------------------------------------------------------------------------------------*/
SELECT
    u.id,
    u.first_name  AS primeiro_nome,
    u.last_name   AS ultimo_nome,
    u.email,
    GROUP_CONCAT(DISTINCT r.authority ORDER BY r.authority SEPARATOR ', ') AS roles
FROM tb_user u
         LEFT JOIN tb_user_role ur ON ur.user_id = u.id
         LEFT JOIN tb_role r       ON r.id = ur.role_id
GROUP BY u.id, u.first_name, u.last_name, u.email
ORDER BY u.first_name, u.last_name;
/*----------------------------------------------------------------------------------------------------*/
SELECT u.email     AS username,
       u.password  AS password,
       r.id        AS roleId,
       r.authority AS authority
FROM tb_user u
         JOIN tb_user_role ur ON u.id = ur.user_id
         JOIN tb_role r       ON r.id = ur.role_id
WHERE u.email = :email