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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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
    public Product create(@Valid DadosCadastroProduto dto) {
        try {
            Product p = new Product();
            p.setName(dto.getName());
            p.setDescription(dto.getDescription());
            p.setPrice(BigDecimal.valueOf(dto.getPrice()));
            p.setImgUrl(dto.getImgUrl());
            p.setDate(dto.getDate());

            var resolved = resolveCategories(dto.getCategoryIds());
            p.getCategories().clear();
            p.getCategories().addAll(resolved);

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

        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setPrice(BigDecimal.valueOf(dto.getPrice()));
        p.setImgUrl(dto.getImgUrl());
        p.setDate(dto.getDate());

        if (dto.getCategoryIds() != null) {
            var resolved = resolveCategories(dto.getCategoryIds());
            p.getCategories().clear();
            p.getCategories().addAll(resolved);
        }

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
}
