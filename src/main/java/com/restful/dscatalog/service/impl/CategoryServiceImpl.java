package com.restful.dscatalog.service.impl;

import com.restful.dscatalog.dto.DadosCadastroCategoria;
import com.restful.dscatalog.dto.DadosDetalhamentoCategoria;
import com.restful.dscatalog.entity.Category;
import com.restful.dscatalog.repository.CategoryRepository;
import com.restful.dscatalog.service.CategoryService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        Category newCategory = new Category(category);
        categoryRepository.save(newCategory);
        return newCategory;
    }

    @Override
    public Category findById(Long id) {
        return categoryRepository.getReferenceById(id);
    }

    @Override
    public Page<DadosDetalhamentoCategoria> listar(Pageable paginacao) {
        return categoryRepository.findAll(paginacao).map(DadosDetalhamentoCategoria::new);
    }

    @Override
    @Transactional
    public @Valid DadosDetalhamentoCategoria update(Long id, DadosCadastroCategoria dto) {
        Category category = categoryRepository.getReferenceById(id);
        category.setName(dto.name());
        categoryRepository.save(category);
        return new DadosDetalhamentoCategoria(category);
    }

    @Override
    @Transactional
    public DadosDetalhamentoCategoria delete(Long id) {
        var entity = categoryRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Category not found: " + id));
        categoryRepository.delete(entity);
        return new DadosDetalhamentoCategoria(entity);
    }
}
