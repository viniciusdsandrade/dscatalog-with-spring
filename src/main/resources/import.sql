-- Insere algumas categorias
INSERT INTO tb_category (name) VALUES ('Eletrônicos');
INSERT INTO tb_category (name) VALUES ('Roupas');
INSERT INTO tb_category (name) VALUES ('Livros');

-- Busca todas as categorias
SELECT * FROM tb_category;

-- Busca uma categoria pelo ID
SELECT * FROM tb_category WHERE id = 1;

-- Busca uma categoria pelo nome
SELECT * FROM tb_category WHERE name = 'Roupas';

-- -- Tenta inserir uma categoria duplicada (deve gerar erro devido à restrição unique)
-- INSERT INTO tb_category (name) VALUES ('Eletrônicos');