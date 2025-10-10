-- Insere algumas categorias (MySQL: DATETIME(6) -> use NOW(6) p/ microssegundos)
INSERT INTO tb_category (name, created_at, updated_at) VALUES ('Eletrônicos', NOW(6), NOW(6));
INSERT INTO tb_category (name, created_at, updated_at) VALUES ('Roupas',      NOW(6), NOW(6));
INSERT INTO tb_category (name, created_at, updated_at) VALUES ('Livros',      NOW(6), NOW(6));

-- Busca todas as categorias
SELECT * FROM tb_category;

-- Busca uma categoria pelo ID
SELECT * FROM tb_category WHERE id = 1;

-- Busca uma categoria pelo nome
SELECT * FROM tb_category WHERE name = 'Roupas';