package com.restful.dscatalog.service.impl;

import com.restful.dscatalog.dto.categoria.CategoryPostDTO;
import com.restful.dscatalog.dto.product.ProductionRegistrationDTO;
import com.restful.dscatalog.dto.product.ProductionPostByNameDTO;
import com.restful.dscatalog.dto.product.ProductDetailsDTO;
import com.restful.dscatalog.entity.Category;
import com.restful.dscatalog.entity.Product;
import com.restful.dscatalog.exception.DuplicateEntryException;
import com.restful.dscatalog.repository.CategoryRepository;
import com.restful.dscatalog.repository.ProductRepository;
import com.restful.dscatalog.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service("productService")
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    @Override
    public Product create(@Valid ProductionRegistrationDTO dto) {
        try {
            Product p = new Product();
            applyScalarFields(dto.name(), dto.description(), dto.price(), dto.imgUrl(), dto.date(), p);
            applyCategoriesByIds(dto.categoryIds(), p);
            productRepository.saveAndFlush(p);
            return p;
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEntryException("Entrada duplicada para Produto.");
        }
    }

    @Transactional
    @Override
    public Product createByCategoryNames(@Valid ProductionPostByNameDTO dto) {
        try {
            Product p = new Product();
            applyScalarFields(dto.name(), dto.description(), dto.price(), dto.imgUrl(),
                    dto.date() != null ? dto.date() : LocalDateTime.now(), p);
            applyCategoriesByNames(dto.categoryNames(), p);
            productRepository.saveAndFlush(p);
            return p;
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEntryException("Entrada duplicada para Produto.");
        }
    }

    @Override
    public Product findById(Long id) {
        return productRepository.getReferenceById(id);
    }

    @Override
    public Page<ProductDetailsDTO> listAll(Pageable paginacao) {
        return productRepository.findAll(paginacao).map(ProductDetailsDTO::new);
    }

    @Override
    public List<ProductDetailsDTO> listAllWithoutPagination() {
        List<Long> ids = productRepository.findAll()
                .stream()
                .map(Product::getId)
                .toList();

        if (ids.isEmpty()) return List.of();

        return productRepository.findAllWithCategoriesByIdIn(ids)
                .stream()
                .map(ProductDetailsDTO::new)
                .toList();
    }

    @Transactional
    @Override
    public @Valid ProductDetailsDTO update(Long id, @Valid ProductionRegistrationDTO dto) {
        Product p = productRepository.getReferenceById(id);

        applyScalarFields(dto.name(), dto.description(), dto.price(), dto.imgUrl(), dto.date(), p);

        if (dto.categoryIds() != null) {
            applyCategoriesByIds(dto.categoryIds(), p);
        }

        productRepository.save(p);
        return new ProductDetailsDTO(p);
    }

    @Transactional
    @Override
    public ProductDetailsDTO delete(Long id) {
        var entity = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
        productRepository.delete(entity);
        return new ProductDetailsDTO(entity);
    }

    private void applyScalarFields(String name,
                                   String description,
                                   Double price,
                                   String imgUrl,
                                   LocalDateTime date,
                                   Product entity) {
        if (name != null) entity.setName(name.trim());
        if (description != null) entity.setDescription(description);
        if (price != null) entity.setPrice(BigDecimal.valueOf(price));
        if (imgUrl != null) entity.setImgUrl(imgUrl);
        if (date != null) entity.setDate(date);
    }

    private void applyCategoriesByIds(List<Long> categoryIds, Product entity) {
        entity.getCategories().clear();
        if (categoryIds == null || categoryIds.isEmpty()) return;
        entity.getCategories().addAll(resolveCategories(categoryIds));
    }

    private void applyCategoriesByNames(Collection<String> rawNames, Product entity) {
        entity.getCategories().clear();
        if (rawNames == null || rawNames.isEmpty()) return;

        LinkedHashSet<String> normalized = normalizeNames(rawNames);
        for (String normalizedLower : normalized) {
            Category cat = findOrCreateCategoryCaseInsensitive(normalizedLower);
            entity.getCategories().add(cat);
        }
    }

    private LinkedHashSet<String> normalizeNames(Collection<String> names) {
        return names.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Category findOrCreateCategoryCaseInsensitive(String normalizedLower) {
        return categoryRepository.findByNameIgnoreCase(normalizedLower)
                .orElseGet(() -> categoryRepository.saveAndFlush(
                        new Category(new CategoryPostDTO(capitalize(normalizedLower)))
                ));
    }

    private Set<Category> resolveCategories(List<Long> categoryIds) {
        if (categoryIds == null) return Set.of();

        var found = new HashSet<>(categoryRepository.findAllById(categoryIds));
        var requested = new HashSet<>(categoryIds);
        var foundIds = found.stream().map(Category::getId).collect(Collectors.toSet());
        requested.removeAll(foundIds);
        if (!requested.isEmpty()) {
            throw new EntityNotFoundException("Categorias inexistentes: " + requested);
        }
        return found;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        if (s.length() == 1) return s.toUpperCase();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
