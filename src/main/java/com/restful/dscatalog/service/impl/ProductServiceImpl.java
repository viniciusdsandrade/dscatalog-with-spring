package com.restful.dscatalog.service.impl;

import com.restful.dscatalog.dto.categoria.DadosCadastroCategoria;
import com.restful.dscatalog.dto.product.DadosCadastroProduto;
import com.restful.dscatalog.dto.product.DadosCadastroProdutoPorNome;
import com.restful.dscatalog.dto.product.DadosDetalhamentoProduto;
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
import java.util.*;
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
    public Product create(@Valid DadosCadastroProduto dto) {
        try {
            Product p = new Product();
            copyDtoToEntity(dto, p); // mapeamento whitelist
            productRepository.saveAndFlush(p);
            return p;
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEntryException("Entrada duplicada para Produto.");
        }
    }

    @Transactional
    @Override
    public Product createByCategoryNames(@Valid DadosCadastroProdutoPorNome dto) {
        List<String> normalized = dto.categoryNames().stream()
                .map(s -> s == null ? "" : s.trim())
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        Set<Category> categorias = new LinkedHashSet<>();
        for (String name : normalized) {
            Category cat = categoryRepository.findByNameIgnoreCase(name).orElse(null);
            if (cat == null) {
                try {
                    cat = new Category(new DadosCadastroCategoria(name));
                    cat = categoryRepository.saveAndFlush(cat);
                } catch (DataIntegrityViolationException dup) {
                    cat = categoryRepository.findByNameIgnoreCase(name)
                            .orElseThrow(() -> dup);
                }
            }
            categorias.add(cat);
        }

        Product p = new Product();
        p.setName(dto.name());
        p.setDescription(dto.description());
        p.setPrice(BigDecimal.valueOf(dto.price()));
        p.setImgUrl(dto.imgUrl());
        p.setDate(dto.date() != null ? dto.date() : LocalDateTime.now());
        p.setCategories(categorias);

        productRepository.saveAndFlush(p);
        return p;
    }

    @Override
    public Product findById(Long id) {
        return productRepository.getReferenceById(id);
    }

    @Override
    @Transactional
    public Page<DadosDetalhamentoProduto> listar(Pageable paginacao) {
        Page<Product> page = productRepository.findAll(paginacao);
        if (page.isEmpty()) return page.map(DadosDetalhamentoProduto::new);

        List<Long> ids = page.getContent().stream()
                .map(Product::getId)
                .toList();

        var fetched = productRepository.findAllWithCategoriesByIdIn(ids);

        var byId = fetched.stream().collect(
                java.util.stream.Collectors.toMap(
                        Product::getId, p -> p, (a, b) -> a, java.util.LinkedHashMap::new
                )
        );

        return page.map(p -> new DadosDetalhamentoProduto(byId.getOrDefault(p.getId(), p)));
    }

    @Transactional
    @Override
    public @Valid DadosDetalhamentoProduto update(Long id, @Valid DadosCadastroProduto dto) {
        Product p = productRepository.getReferenceById(id);
        copyDtoToEntity(dto, p); // mapeamento whitelist
        productRepository.save(p);
        return new DadosDetalhamentoProduto(p);
    }

    @Transactional
    @Override
    public DadosDetalhamentoProduto delete(Long id) {
        var entity = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
        productRepository.delete(entity);
        return new DadosDetalhamentoProduto(entity);
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

    private void copyDtoToEntity(DadosCadastroProduto dto, Product entity) {
        // escalares
        if (dto.getName() != null)        entity.setName(dto.getName().trim()); // whitelist
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getPrice() != null)       entity.setPrice(BigDecimal.valueOf(dto.getPrice()));
        if (dto.getImgUrl() != null)      entity.setImgUrl(dto.getImgUrl());
        if (dto.getDate() != null)        entity.setDate(dto.getDate());

        // associação N:N: valida ids e repovoa mantendo semântica de erro 404
        entity.getCategories().clear();
        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            entity.getCategories().addAll(resolveCategories(dto.getCategoryIds()));
        }

    }

    private void copyDtoToEntityByNames(DadosCadastroProdutoPorNome dto, Product entity) {
        if (dto.name() != null)        entity.setName(dto.name().trim());
        if (dto.description() != null) entity.setDescription(dto.description());
        if (dto.price() != null)       entity.setPrice(BigDecimal.valueOf(dto.price()));
        if (dto.imgUrl() != null)      entity.setImgUrl(dto.imgUrl());
        if (dto.date() != null)        entity.setDate(dto.date());

        entity.getCategories().clear();
        if (dto.categoryNames() != null && !dto.categoryNames().isEmpty()) {
            // normaliza, deduplica
            LinkedHashSet<String> names = dto.categoryNames().stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(String::toLowerCase)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            for (String normalized : names) {
                Category cat = categoryRepository
                        .findByNameIgnoreCase(normalized)
                        .orElseGet(() -> categoryRepository.save(new Category(capitalize(normalized))));
                entity.getCategories().add(cat);
            }
        }
    }

    // util local (se preferir, extraia)
    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
