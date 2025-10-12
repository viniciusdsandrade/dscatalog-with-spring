package com.restful.dscatalog.repository;

import com.restful.dscatalog.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("productRepository")
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
       select distinct p
       from Product p
       left join fetch p.categories
       where p.id in :ids
       """)
    List<Product> findAllWithCategoriesByIdIn(@Param("ids") List<Long> ids);
}
