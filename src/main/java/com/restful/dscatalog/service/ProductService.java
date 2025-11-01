package com.restful.dscatalog.service;

import com.restful.dscatalog.dto.product.ProductPostDTO;
import com.restful.dscatalog.dto.product.ProductPostByNameDTO;
import com.restful.dscatalog.dto.product.ProductDetailsDTO;
import com.restful.dscatalog.entity.Product;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductService {

    @Transactional
    Product create(@Valid ProductPostDTO productPostDTO);

    @Transactional
    Product createByCategoryNames(ProductPostByNameDTO productPostByNameDTO);

    Product findById(Long id);

    Page<ProductDetailsDTO> listAll(Pageable paginacao);

    List<ProductDetailsDTO> listAllWithoutPagination();

    @Transactional
    @Valid
    ProductDetailsDTO update(Long id, @Valid ProductPostDTO productPostDTO);

    @Transactional
    ProductDetailsDTO delete(Long id);
}
