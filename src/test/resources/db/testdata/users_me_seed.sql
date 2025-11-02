DELETE FROM tb_user_role;
DELETE FROM tb_role;
DELETE FROM tb_user;

INSERT INTO tb_user (id, first_name, last_name, email, password)
VALUES (1, 'Maria', 'Brown', 'maria@gmail.com', '{noop}123456');

INSERT INTO tb_role (id, authority) VALUES (1, 'ROLE_ADMIN');
INSERT INTO tb_user_role (user_id, role_id) VALUES (1, 1);