package com.restful.dscatalog.repository;

import com.restful.dscatalog.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("productRepository")
public interface ProductRepository extends JpaRepository<Product, Long> {

    /*
      N+1 Consultas
      ao buscar N produtos e depois acessar p.getCategories() (LAZY),
      o ORM faz 1 SELECT dos produtos + N SELECTs das categorias

      Solução:
      "left join fetch p.categories" carrega Product e suas Category
      em UM único SELECT com JOIN, evitando as N consultas extras.
      Por que LEFT: garante que produtos sem categorias também venham (coleção vazia).
      Por que DISTINCT: o JOIN repete a linha do Product para cada Category;
      DISTINCT elimina duplicatas do Product no resultado.

      Product e Category em N:N (tabela product_category).
      Categorias típicas: "Mobile", "Computers", "Eletrônicos".

      EXEMPLO A — 1 produto com 3 categorias
      Dados:
        P1 = "Produto 1"
        Categorias de P1 = {Mobile, Computers, Eletrônicos}

      Sem JOIN FETCH (LAZY) ao acessar p.getCategories() em loop:
      Q1: SELECT p.* FROM product p WHERE p.id IN (1);
      Q2: SELECT c.*
          FROM category c
          JOIN product_category pc
          ON pc.category_id=c.id
          WHERE pc.product_id=1;
      Total = 2 idas ao banco (1 + N), mas explode para 1+N quando há vários produtos.

      Com JOIN FETCH + DISTINCT (uma única ida para produto+categorias):
        SELECT DISTINCT p.*, c.*
        FROM product p
        LEFT JOIN product_category pc ON pc.product_id=p.id
        LEFT JOIN category c ON c.id=pc.category_id
        WHERE p.id IN (1);
        Resultado em memória:
          p(1).categories = {Mobile, Computers, Eletrônicos}
        Acessos posteriores a p.getCategories() NÃO disparam novas queries.

      EXEMPLO B — 1 categoria associada a 3 produtos
      Dados:
        Categoria "Mobile"
        Produtos ligados a "Mobile": {
          P1="Produto 1",
          P2="Produto 2",
          P3="Produto 3"
      }

      Pegando a partir dos PRODUTOS com este metodo, em UMA consulta:

        SELECT DISTINCT p.*, c.* ... WHERE p.id IN (1,2,3);
        Em memória:
          p(1).categories contém "Mobile" (e possivelmente outras)
          p(2).categories contém "Mobile"
          p(3).categories contém "Mobile"
     */
    @Query("""
       SELECT DISTINCT p
       FROM Product p
       LEFT JOIN FETCH p.categories
       WHERE p.id IN :ids
       """)
    List<Product> findAllWithCategoriesByIdIn(@Param("ids") List<Long> ids);
}
