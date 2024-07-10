package com.restful.dscatalog.service.impl;

import com.restful.dscatalog.dto.DadosCadastroCategoria;
import com.restful.dscatalog.entity.Category;
import com.restful.dscatalog.repository.CategoryRepository;
import com.restful.dscatalog.service.CategoryService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

@Service("categoryService")
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public Category create(@Valid DadosCadastroCategoria category) {
        Category novaCategoria = new Category(category.name());
        return categoryRepository.save(novaCategoria);
    }

    @Override
    public Category buscarPorId(Long id) {
        return categoryRepository.getReferenceById(id);
    }
}
