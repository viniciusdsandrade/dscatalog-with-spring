package com.restful.dscatalog.service;

import com.restful.dscatalog.dto.categoria.CategoryPostDTO;
import com.restful.dscatalog.dto.categoria.CategoryDetailsDTO;
import com.restful.dscatalog.entity.Category;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface CategoryService {

    @Transactional
    Category create(CategoryPostDTO category);

    Category findById(Long id);

    Page<CategoryDetailsDTO> listAll(Pageable paginacao);

    @Transactional
    @Valid
    CategoryDetailsDTO update(Long id, @Valid CategoryPostDTO dto);

    @Transactional
    CategoryDetailsDTO delete(Long id);
}
