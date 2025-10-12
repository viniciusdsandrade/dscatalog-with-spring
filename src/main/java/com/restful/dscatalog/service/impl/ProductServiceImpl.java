package com.restful.dscatalog.service.impl;

import com.restful.dscatalog.dto.DadosCadastroProduto;
import com.restful.dscatalog.dto.DadosDetalhamentoProduto;
import com.restful.dscatalog.entity.Product;
import com.restful.dscatalog.exception.DuplicateEntryException;
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

@Service("productService")
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
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
    public Page<DadosDetalhamentoProduto> listar(Pageable paginacao) {
        return productRepository.findAll(paginacao).map(DadosDetalhamentoProduto::new);
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
}
