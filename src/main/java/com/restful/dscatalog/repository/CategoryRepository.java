package com.restful.dscatalog.repository;

import com.restful.dscatalog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("categoryRepository")
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
