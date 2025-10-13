package com.restful.dscatalog.service;

import com.restful.dscatalog.dto.product.ProductionPostDTO;
import com.restful.dscatalog.dto.product.ProductionPostByNameDTO;
import com.restful.dscatalog.dto.product.ProductDetailsDTO;
import com.restful.dscatalog.entity.Product;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    @Transactional
    Product create(@Valid ProductionPostDTO dto);

    @Transactional
    Product createByCategoryNames(ProductionPostByNameDTO dto);

    Product findById(Long id);

    Page<ProductDetailsDTO> listAll(Pageable paginacao);

    List<ProductDetailsDTO> listAllWithoutPagination();

    @Transactional
    @Valid
    ProductDetailsDTO update(Long id, @Valid ProductionPostDTO dto);

    @Transactional
    ProductDetailsDTO delete(Long id);
}
