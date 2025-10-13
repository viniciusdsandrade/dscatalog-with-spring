SET @now = NOW(6);

INSERT INTO tb_category (name, created_at, updated_at)
SELECT 'Eletrônicos', @now, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_category WHERE name = 'Eletrônicos');

INSERT INTO tb_category (name, created_at, updated_at)
SELECT 'Roupas', @now, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_category WHERE name = 'Roupas');

INSERT INTO tb_category (name, created_at, updated_at)
SELECT 'Livros', @now, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_category WHERE name = 'Livros');

INSERT INTO tb_category (name, created_at, updated_at)
SELECT 'Informática', @now, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_category WHERE name = 'Informática');

INSERT INTO tb_category (name, created_at, updated_at)
SELECT 'Esportes', @now, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_category WHERE name = 'Esportes');

INSERT INTO tb_category (name, created_at, updated_at)
SELECT 'Casa e Jardim', @now, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_category WHERE name = 'Casa e Jardim');

INSERT INTO tb_category (name, created_at, updated_at)
SELECT 'Beleza', @now, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_category WHERE name = 'Beleza');

INSERT INTO tb_category (name, created_at, updated_at)
SELECT 'Brinquedos', @now, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_category WHERE name = 'Brinquedos');

INSERT INTO tb_category (name, created_at, updated_at)
SELECT 'Games', @now, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_category WHERE name = 'Games');

INSERT INTO tb_category (name, created_at, updated_at)
SELECT 'Acessórios', @now, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_category WHERE name = 'Acessórios');

-- =========================
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Smartphone XYZ', 'Android 14, 128GB', 1999.90, 'https://example.com/img/smartphone.png', @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Smartphone XYZ');

-- 2
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Camiseta Básica', '100% algodão', 49.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Camiseta Básica');

-- 3
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Livro Clean Code', 'Robert C. Martin', 139.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Livro Clean Code');

-- 4
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Notebook Ultra 14', 'Intel i7, 16GB, 512GB SSD', 5499.90, 'https://example.com/img/notebook.png', @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Notebook Ultra 14');

-- 5
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Fone Bluetooth Pro', 'ANC, 30h bateria', 499.90, 'https://example.com/img/fone.png', @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Fone Bluetooth Pro');

-- 6
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Smart TV 50 4K', 'Painel 4K, HDR10, 60Hz', 2399.90, 'https://example.com/img/tv.png', @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Smart TV 50 4K');

-- 7
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Tênis Running X', 'Amortecimento responsivo', 399.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Tênis Running X');

-- 8
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Mochila Daypack', '25L, compartimento para notebook', 189.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Mochila Daypack');

-- 9
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Cafeteira Espresso', '15 bar, reservatório 1.2L', 599.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Cafeteira Espresso');

-- 10
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Teclado Mecânico', 'Switch brown, ABNT2', 349.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Teclado Mecânico');

-- 11
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Mouse Gamer RGB', '16000 DPI, 6 botões', 229.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Mouse Gamer RGB');

-- 12
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Headset Surround 7.1', 'Microfone destacável', 379.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Headset Surround 7.1');

-- 13
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Monitor 27 QHD', '2560x1440, 75Hz', 1599.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Monitor 27 QHD');

-- 14
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Impressora Wi-Fi', 'Inkjet, duplex', 699.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Impressora Wi-Fi');

-- 15
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Tablet 10', 'Tela 10", 64GB', 1299.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Tablet 10');

-- 16
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'E-book Reader', 'E-ink 6.8", luz ajustável', 799.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'E-book Reader');

-- 17
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Liquidificador Turbo', '900W, 12 velocidades', 249.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Liquidificador Turbo');

-- 18
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Ventilador Silencioso', '40cm, 3 velocidades', 199.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Ventilador Silencioso');

-- 19
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Webcam Full HD', '1080p, autofoco', 289.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Webcam Full HD');

-- 20
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Microfone Condenser', 'USB, padrão cardioide', 499.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Microfone Condenser');

-- 21
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Cadeira Gamer', 'Apoio lombar, reclinável', 1199.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Cadeira Gamer');

-- 22
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Smartwatch Fit', 'GPS, batimentos', 899.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Smartwatch Fit');

-- 23
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Óculos de Sol', 'Proteção UV400', 149.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Óculos de Sol');

-- 24
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Perfume Classic', 'Eau de parfum 100ml', 299.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Perfume Classic');

-- 25
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Jogo de Panelas', 'Antiaderente, 5 peças', 349.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Jogo de Panelas');

-- 26
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Travesseiro Ortopédico', 'Espuma viscoelástica', 159.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Travesseiro Ortopédico');

-- 27
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Lego 500 Peças', 'Blocos de montar', 219.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Lego 500 Peças');

-- 28
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Console X Series', '4K, 1TB SSD', 4399.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Console X Series');

-- 29
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Controle Sem Fio', 'Bluetooth, vibração', 349.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Controle Sem Fio');

-- 30
INSERT INTO tb_product (name, description, price, img_url, date)
SELECT 'Álbum de Fotos', 'Capa dura, 200 fotos', 89.90, NULL, @now FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tb_product WHERE name = 'Álbum de Fotos');

-- =========================
-- RELAÇÕES N:N (tb_product_category)
-- Regra: pelo menos 10 produtos devem ter 4 ou 5 categorias.
-- Abaixo: produtos 1..10 com 4–5 categorias cada; demais com 1–2.
-- =========================

-- Helper macro (padrão idempotente):
-- INSERT INTO tb_product_category (product_id, category_id)
-- SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = '<CATEGORIA>'
-- WHERE p.name = '<PRODUTO>'
--   AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id = p.id AND pc.category_id = c.id);

-- -------- Produtos 1..10 (4–5 categorias cada) --------
-- 1 Smartphone XYZ -> Eletrônicos, Informática, Games, Acessórios
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Eletrônicos'
WHERE p.name = 'Smartphone XYZ'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Informática'
WHERE p.name = 'Smartphone XYZ'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Games'
WHERE p.name = 'Smartphone XYZ'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Acessórios'
WHERE p.name = 'Smartphone XYZ'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 2 Camiseta Básica -> Roupas, Esportes, Acessórios, Casa e Jardim
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Roupas'
WHERE p.name = 'Camiseta Básica'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Esportes'
WHERE p.name = 'Camiseta Básica'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Acessórios'
WHERE p.name = 'Camiseta Básica'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Casa e Jardim'
WHERE p.name = 'Camiseta Básica'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 3 Livro Clean Code -> Livros, Informática, Eletrônicos, Games
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Livros'
WHERE p.name = 'Livro Clean Code'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Informática'
WHERE p.name = 'Livro Clean Code'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Eletrônicos'
WHERE p.name = 'Livro Clean Code'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Games'
WHERE p.name = 'Livro Clean Code'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 4 Notebook Ultra 14 -> Eletrônicos, Informática, Games, Acessórios, Livros
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Eletrônicos'
WHERE p.name = 'Notebook Ultra 14'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Informática'
WHERE p.name = 'Notebook Ultra 14'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Games'
WHERE p.name = 'Notebook Ultra 14'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Acessórios'
WHERE p.name = 'Notebook Ultra 14'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Livros'
WHERE p.name = 'Notebook Ultra 14'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 5 Fone Bluetooth Pro -> Eletrônicos, Acessórios, Esportes, Games
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Eletrônicos'
WHERE p.name = 'Fone Bluetooth Pro'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Acessórios'
WHERE p.name = 'Fone Bluetooth Pro'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Esportes'
WHERE p.name = 'Fone Bluetooth Pro'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Games'
WHERE p.name = 'Fone Bluetooth Pro'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 6 Smart TV 50 4K -> Eletrônicos, Casa e Jardim, Games, Informática
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Eletrônicos'
WHERE p.name = 'Smart TV 50 4K'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Casa e Jardim'
WHERE p.name = 'Smart TV 50 4K'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Games'
WHERE p.name = 'Smart TV 50 4K'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Informática'
WHERE p.name = 'Smart TV 50 4K'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 7 Tênis Running X -> Esportes, Roupas, Acessórios, Casa e Jardim
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Esportes'
WHERE p.name = 'Tênis Running X'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Roupas'
WHERE p.name = 'Tênis Running X'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Acessórios'
WHERE p.name = 'Tênis Running X'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Casa e Jardim'
WHERE p.name = 'Tênis Running X'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 8 Mochila Daypack -> Acessórios, Roupas, Esportes, Informática
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Acessórios'
WHERE p.name = 'Mochila Daypack'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Roupas'
WHERE p.name = 'Mochila Daypack'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Esportes'
WHERE p.name = 'Mochila Daypack'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Informática'
WHERE p.name = 'Mochila Daypack'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 9 Cafeteira Espresso -> Casa e Jardim, Eletrônicos, Acessórios, (opcional) Livros
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Casa e Jardim'
WHERE p.name = 'Cafeteira Espresso'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Eletrônicos'
WHERE p.name = 'Cafeteira Espresso'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Acessórios'
WHERE p.name = 'Cafeteira Espresso'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
-- (quarta categoria já atingida; mantenha 4)

-- 10 Teclado Mecânico -> Informática, Eletrônicos, Games, Acessórios, Livros
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Informática'
WHERE p.name = 'Teclado Mecânico'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Eletrônicos'
WHERE p.name = 'Teclado Mecânico'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Games'
WHERE p.name = 'Teclado Mecânico'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Acessórios'
WHERE p.name = 'Teclado Mecânico'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name = 'Livros'
WHERE p.name = 'Teclado Mecânico'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- -------- Produtos 11..30 (1–2 categorias) --------
-- 11 Mouse Gamer RGB -> Games, Informática
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Games'
WHERE p.name='Mouse Gamer RGB'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Informática'
WHERE p.name='Mouse Gamer RGB'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 12 Headset Surround 7.1 -> Games, Eletrônicos
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Games'
WHERE p.name='Headset Surround 7.1'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Eletrônicos'
WHERE p.name='Headset Surround 7.1'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 13 Monitor 27 QHD -> Informática
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Informática'
WHERE p.name='Monitor 27 QHD'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 14 Impressora Wi-Fi -> Informática, Eletrônicos
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Informática'
WHERE p.name='Impressora Wi-Fi'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Eletrônicos'
WHERE p.name='Impressora Wi-Fi'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 15 Tablet 10 -> Eletrônicos, Informática
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Eletrônicos'
WHERE p.name='Tablet 10'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Informática'
WHERE p.name='Tablet 10'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 16 E-book Reader -> Eletrônicos, Livros
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Eletrônicos'
WHERE p.name='E-book Reader'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Livros'
WHERE p.name='E-book Reader'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 17 Liquidificador Turbo -> Casa e Jardim
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Casa e Jardim'
WHERE p.name='Liquidificador Turbo'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 18 Ventilador Silencioso -> Casa e Jardim, Eletrônicos
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Casa e Jardim'
WHERE p.name='Ventilador Silencioso'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Eletrônicos'
WHERE p.name='Ventilador Silencioso'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 19 Webcam Full HD -> Informática, Acessórios
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Informática'
WHERE p.name='Webcam Full HD'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Acessórios'
WHERE p.name='Webcam Full HD'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 20 Microfone Condenser -> Eletrônicos, Informática
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Eletrônicos'
WHERE p.name='Microfone Condenser'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Informática'
WHERE p.name='Microfone Condenser'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 21 Cadeira Gamer -> Games, Casa e Jardim
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Games'
WHERE p.name='Cadeira Gamer'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Casa e Jardim'
WHERE p.name='Cadeira Gamer'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 22 Smartwatch Fit -> Eletrônicos, Esportes
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Eletrônicos'
WHERE p.name='Smartwatch Fit'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Esportes'
WHERE p.name='Smartwatch Fit'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 23 Óculos de Sol -> Acessórios, Beleza
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Acessórios'
WHERE p.name='Óculos de Sol'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Beleza'
WHERE p.name='Óculos de Sol'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 24 Perfume Classic -> Beleza
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Beleza'
WHERE p.name='Perfume Classic'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 25 Jogo de Panelas -> Casa e Jardim
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Casa e Jardim'
WHERE p.name='Jogo de Panelas'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 26 Travesseiro Ortopédico -> Casa e Jardim, Beleza
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Casa e Jardim'
WHERE p.name='Travesseiro Ortopédico'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Beleza'
WHERE p.name='Travesseiro Ortopédico'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 27 Lego 500 Peças -> Brinquedos
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Brinquedos'
WHERE p.name='Lego 500 Peças'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 28 Console X Series -> Games, Eletrônicos
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Games'
WHERE p.name='Console X Series'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Eletrônicos'
WHERE p.name='Console X Series'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 29 Controle Sem Fio -> Games, Acessórios
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Games'
WHERE p.name='Controle Sem Fio'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Acessórios'
WHERE p.name='Controle Sem Fio'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);

-- 30 Álbum de Fotos -> Casa e Jardim, Livros
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Casa e Jardim'
WHERE p.name='Álbum de Fotos'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
INSERT INTO tb_product_category (product_id, category_id)
SELECT p.id, c.id FROM tb_product p JOIN tb_category c ON c.name='Livros'
WHERE p.name='Álbum de Fotos'
  AND NOT EXISTS (SELECT 1 FROM tb_product_category pc WHERE pc.product_id=p.id AND pc.category_id=c.id);
