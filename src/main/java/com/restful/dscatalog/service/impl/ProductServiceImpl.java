package com.restful.dscatalog.service.impl;

import com.restful.dscatalog.dto.categoria.CategoryPostDTO;
import com.restful.dscatalog.dto.product.ProductPostDTO;
import com.restful.dscatalog.dto.product.ProductPostByNameDTO;
import com.restful.dscatalog.dto.product.ProductDetailsDTO;
import com.restful.dscatalog.entity.Category;
import com.restful.dscatalog.entity.Product;
import com.restful.dscatalog.exception.DatabaseException;
import com.restful.dscatalog.exception.DuplicateEntryException;
import com.restful.dscatalog.exception.ResourceNotFoundException;
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service("productService")
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    @Override
    public Product create(@Valid ProductPostDTO productPostDTO) {
        try {
            Product product = new Product();
            applyScalarFields(
                    productPostDTO.name(),
                    productPostDTO.description(),
                    productPostDTO.price(),
                    productPostDTO.imgUrl(),
                    productPostDTO.date(), product
            );
            applyCategoriesByIds(productPostDTO.categoryIds(), product);
            productRepository.saveAndFlush(product);
            return product;
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEntryException("Entrada duplicada para Produto.");
        }
    }

    @Transactional
    @Override
    public Product createByCategoryNames(@Valid ProductPostByNameDTO productPostByNameDTO) {
        try {
            Product product = new Product();
            applyScalarFields(
                    productPostByNameDTO.name(),
                    productPostByNameDTO.description(),
                    productPostByNameDTO.price(),
                    productPostByNameDTO.imgUrl(),
                    productPostByNameDTO.date() != null
                            ? productPostByNameDTO.date()
                            : LocalDateTime.now(), product
            );

            applyCategoriesByNames(productPostByNameDTO.categoryNames(), product);
            productRepository.saveAndFlush(product);
            return product;
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEntryException("Entrada duplicada para Produto.");
        }
    }

    @Override
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    @Override
    public Page<ProductDetailsDTO> listAll(Pageable paginacao) {
        Page<Product> page = productRepository.findAll(paginacao);
        if (page.isEmpty()) return page.map(ProductDetailsDTO::new);

        List<Long> ids = page.stream().map(Product::getId).toList();

        List<Product> fetched = productRepository.findAllWithCategoriesByIdIn(ids);

        Map<Long, Product> byIdHydrated = fetched.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        return page.map(p -> {
            Product product = byIdHydrated.getOrDefault(p.getId(), p);
            return new ProductDetailsDTO(product);
        });
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
    public @Valid ProductDetailsDTO update(Long id, @Valid ProductPostDTO productPostDTO) {
        Product product = productRepository.getReferenceById(id);

        applyScalarFields(
                productPostDTO.name(),
                productPostDTO.description(),
                productPostDTO.price(),
                productPostDTO.imgUrl(),
                productPostDTO.date(),
                product
        );

        if (productPostDTO.categoryIds() != null) applyCategoriesByIds(productPostDTO.categoryIds(), product);

        productRepository.save(product);
        return new ProductDetailsDTO(product);
    }

    @Transactional
    @Override
    public ProductDetailsDTO delete(Long id) {
        var entity = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Id not found " + id));
        var dto = new ProductDetailsDTO(entity);
        try {
            productRepository.delete(entity);
            productRepository.flush();
            return dto;
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Integrity violation");
        }
    }

    private void applyScalarFields(
            String name,
            String description,
            Double price,
            String imgUrl,
            LocalDateTime date,
            Product entity
    ) {
        if (name != null) entity.setName(name.trim());
        if (description != null) entity.setDescription(description);
        if (price != null) entity.setPrice(BigDecimal.valueOf(price));
        if (imgUrl != null) entity.setImgUrl(imgUrl);
        if (date != null) entity.setDate(date);
    }

    private void applyCategoriesByIds(List<Long> categoryIds, Product product) {
        product.getCategories().clear();
        if (categoryIds == null || categoryIds.isEmpty()) return;
        product.getCategories().addAll(resolveCategories(categoryIds));
    }

    private void applyCategoriesByNames(Collection<String> rawNames, Product product) {
        product.getCategories().clear();
        if (rawNames == null || rawNames.isEmpty()) return;

        LinkedHashSet<String> normalized = normalizeNames(rawNames);
        for (String normalizedLower : normalized) {
            Category cat = findOrCreateCategoryCaseInsensitive(normalizedLower);
            product.getCategories().add(cat);
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

    private static String capitalize(String string) {
        if (string == null || string.isEmpty()) return string;
        if (string.length() == 1) return string.toUpperCase();
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }
}
