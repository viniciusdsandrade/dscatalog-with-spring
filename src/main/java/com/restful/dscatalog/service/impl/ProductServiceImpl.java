package com.restful.dscatalog.service.impl;

import com.restful.dscatalog.dto.category.CategoryPostDTO;
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
import org.springframework.transaction.annotation.Transactional;
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

import static java.lang.Character.toUpperCase;
import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.*;

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

    @Override
    @Transactional
    public Product create(@Valid ProductPostDTO productPostDTO) {
        try {
            Product product = new Product();
            setProductScalarFields(
                    productPostDTO.name(),
                    productPostDTO.description(),
                    productPostDTO.price(),
                    productPostDTO.imgUrl(),
                    productPostDTO.date(),
                    product
            );
            setCategoriesFromIds(productPostDTO.categoryIds(), product);
            productRepository.saveAndFlush(product);
            return product;
        } catch (DataIntegrityViolationException dataIntegrityViolationException) {
            throw new DuplicateEntryException("Produto duplicado.");
        }
    }

    @Override
    @Transactional
    public Product createByCategoryNames(@Valid ProductPostByNameDTO productPostByNameDTO) {
        try {
            Product product = new Product();
            setProductScalarFields(
                    productPostByNameDTO.name(),
                    productPostByNameDTO.description(),
                    productPostByNameDTO.price(),
                    productPostByNameDTO.imgUrl(),
                    productPostByNameDTO.date() != null
                            ? productPostByNameDTO.date()
                            : now(), product
            );

            applyCategoriesByNames(productPostByNameDTO.categoryNames(), product);
            productRepository.saveAndFlush(product);
            return product;
        } catch (DataIntegrityViolationException dataIntegrityViolationException) {
            throw new DuplicateEntryException("Entrada duplicada para Produto.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDetailsDTO> listAll(Pageable pageable) {
        Page<Product> productsPage = productRepository.findAll(pageable);
        if (productsPage.isEmpty()) return productsPage.map(ProductDetailsDTO::new);

        List<Long> productsIds = productsPage.stream()
                .map(Product::getId)
                .toList();

        List<Product> productsWithCategories = productRepository.findAllWithCategoriesByIdIn(productsIds);

        Map<Long, Product> productsById = productsWithCategories.stream()
                .collect(toMap(Product::getId, p -> p));

        return productsPage.map(pagedProduct -> {
            Product product = productsById.getOrDefault(pagedProduct.getId(), pagedProduct);
            return new ProductDetailsDTO(product);
        });
    }

    @Override
    public List<ProductDetailsDTO> listAllWithoutPagination() {
        List<Long> productsIds = productRepository.findAll()
                .stream()
                .map(Product::getId)
                .toList();

        if (productsIds.isEmpty()) return List.of();

        return productRepository.findAllWithCategoriesByIdIn(productsIds)
                .stream()
                .map(ProductDetailsDTO::new)
                .toList();
    }

    @Override
    @Transactional
    public @Valid ProductDetailsDTO update(Long id, @Valid ProductPostDTO productPostDTO) {
        try {
            Product product = productRepository.getReferenceById(id);

            setProductScalarFields(
                    productPostDTO.name(),
                    productPostDTO.description(),
                    productPostDTO.price(),
                    productPostDTO.imgUrl(),
                    productPostDTO.date(),
                    product
            );

            if (productPostDTO.categoryIds() != null)
                setCategoriesFromIds(productPostDTO.categoryIds(), product);

            try {
                productRepository.save(product);
            } catch (DataIntegrityViolationException dataIntegrityViolationException) {
                throw new DuplicateEntryException("Entrada duplicada para Produto.");
            }

            return new ProductDetailsDTO(product);
        } catch (EntityNotFoundException entityNotFoundException) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
    }

    @Override
    @Transactional
    public ProductDetailsDTO delete(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Id not found " + id));
        var productDetailsDTO = new ProductDetailsDTO(product);
        try {
            productRepository.delete(product);
            productRepository.flush();
            return productDetailsDTO;
        } catch (DataIntegrityViolationException dataIntegrityViolationException) {
            throw new DatabaseException("Integrity violation");
        }
    }

    private void setProductScalarFields(
            String name,
            String description,
            Double price,
            String imgUrl,
            LocalDateTime date,
            Product product
    ) {
        if (name != null) product.setName(name.trim());
        if (description != null) product.setDescription(description.trim());
        if (price != null) product.setPrice(BigDecimal.valueOf(price));
        if (imgUrl != null) product.setImgUrl(imgUrl.trim());
        if (date != null) product.setDate(date);
    }

    private void setCategoriesFromIds(List<Long> requestedCategoryIds, Product product) {
        product.getCategories().clear();
        if (requestedCategoryIds == null || requestedCategoryIds.isEmpty()) return;
        product.getCategories().addAll(fetchCategoriesOrThrow(requestedCategoryIds));
    }

    private void applyCategoriesByNames(Collection<String> categoryNames, Product product) {
        product.getCategories().clear();
        if (categoryNames == null || categoryNames.isEmpty()) return;

        LinkedHashSet<String> normalizedNames = normalizeNames(categoryNames);
        for (String normalizedName : normalizedNames) {
            Category category = findOrCreateCategoryCaseInsensitive(normalizedName);
            product.getCategories().add(category);
        }
    }

    private LinkedHashSet<String> normalizeNames(Collection<String> names) {
        return names.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(toCollection(LinkedHashSet::new));
    }

    private Category findOrCreateCategoryCaseInsensitive(String normalizedName) {
        return categoryRepository.findByNameIgnoreCase(normalizedName)
                .orElseGet(() -> categoryRepository.saveAndFlush(
                        new Category(new CategoryPostDTO(capitalize(normalizedName)))
                ));
    }

    private Set<Category> fetchCategoriesOrThrow(List<Long> categoryIds) {
        if (categoryIds == null) return Set.of();

        Set<Category> categories = new HashSet<>(categoryRepository.findAllById(categoryIds));
        Set<Long> foundCategoryIds = categories.stream()
                .map(Category::getId)
                .collect(toSet());

        Set<Long> missingIds = new HashSet<>(categoryIds);
        missingIds.removeAll(foundCategoryIds);

        if (!missingIds.isEmpty())
            throw new EntityNotFoundException("Categorias inexistentes: " + missingIds);

        return categories;
    }

    private static String capitalize(String string) {
        if (string == null || string.isEmpty()) return string;
        if (string.length() == 1) return string.toUpperCase();
        return toUpperCase(string.charAt(0)) + string.substring(1);
    }
}
