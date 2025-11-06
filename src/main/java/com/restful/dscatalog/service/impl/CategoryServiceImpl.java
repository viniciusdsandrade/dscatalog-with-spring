package com.restful.dscatalog.service.impl;

import com.restful.dscatalog.dto.category.CategoryPostDTO;
import com.restful.dscatalog.dto.category.CategoryDetailsDTO;
import com.restful.dscatalog.entity.Category;
import com.restful.dscatalog.exception.DatabaseException;
import com.restful.dscatalog.exception.DuplicateEntryException;
import com.restful.dscatalog.exception.ResourceNotFoundException;
import com.restful.dscatalog.repository.CategoryRepository;
import com.restful.dscatalog.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
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
    public Category create(@Valid CategoryPostDTO categoryPostDTO) {
        try {
            Category category = new Category(categoryPostDTO);
            categoryRepository.saveAndFlush(category);
            return category;
        } catch (DataIntegrityViolationException dataIntegrityViolationException) {
            throw new DuplicateEntryException("Entrada duplicada para Categoria.");
        }
    }

    @Override
    public Category findById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDetailsDTO> listAll(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(CategoryDetailsDTO::new);
    }

    @Override
    @Transactional
    public @Valid CategoryDetailsDTO update(Long categoryId, CategoryPostDTO categoryPostDTO) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
        copyDtoToEntity(categoryPostDTO, category);
        try {
            categoryRepository.save(category);
        } catch (DataIntegrityViolationException dataIntegrityViolationException) {
            throw new DuplicateEntryException("Entrada duplicada para Categoria.");
        }
        return new CategoryDetailsDTO(category);
    }

    @Override
    @Transactional
    public CategoryDetailsDTO delete(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
        try {
            categoryRepository.delete(category);
        } catch (DataIntegrityViolationException dataIntegrityViolationException) {
            throw new DatabaseException("Integrity violation");
        }
        return new CategoryDetailsDTO(category);
    }

    private void copyDtoToEntity(CategoryPostDTO categoryPostDTO, Category category) {
        if (categoryPostDTO != null && categoryPostDTO.name() != null) {
            category.setName(categoryPostDTO.name().trim());
        }
    }
}
