package com.restful.dscatalog.controller;

import com.restful.dscatalog.dto.product.ProductPostDTO;
import com.restful.dscatalog.dto.product.ProductPostByNameDTO;
import com.restful.dscatalog.dto.product.ProductDetailsDTO;
import com.restful.dscatalog.entity.Product;
import com.restful.dscatalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping(value = {"/api/v1/products", "/products"})
@Tag(name = "Product Controller", description = "Controller para gerenciamento de produtos")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailsDTO> findById(@PathVariable Long id) {
        Product product = productService.findById(id);
        return ok(new ProductDetailsDTO(product));
    }

    @GetMapping
    public ResponseEntity<Page<ProductDetailsDTO>> findAll(
            @PageableDefault(size = 5, sort = {"id"}) Pageable paginacao
    ) {
        Page<ProductDetailsDTO> products = productService.listAll(paginacao);
        return ok(products);
    }

    @GetMapping("/without-pagination")
    public ResponseEntity<List<ProductDetailsDTO>> findAllWithoutPagination() {
        List<ProductDetailsDTO> products = productService.listAllWithoutPagination();
        return ok(products);
    }

    @PostMapping
    @Transactional
    @Operation(
            summary = "Cria um produto",
            description = "Cria um produto a partir de um DTO",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Produto criado"),
                    @ApiResponse(responseCode = "400", description = "Erro de validação")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDetailsDTO> create(
            @RequestBody @Valid ProductPostDTO productPostDTO,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        Product product = productService.create(productPostDTO);
        URI uri = uriComponentsBuilder
                .path("/api/v1/products/{id}")
                .buildAndExpand(product.getId())
                .toUri();
        return created(uri).body(new ProductDetailsDTO(product));
    }

    @PostMapping("/by-names")
    @Transactional
    @Operation(
            summary = "Cria produto por nomes de categorias (case-insensitive)",
            description = "Se a categoria não existir, será criada. Nomes são normalizados (trim) e deduplicados.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Produto criado"),
                    @ApiResponse(responseCode = "400", description = "Erro de validação")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDetailsDTO> createByNames(
            @RequestBody @Valid ProductPostByNameDTO productPostByNameDTO,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        Product product = productService.createByCategoryNames(productPostByNameDTO);
        URI uri = uriComponentsBuilder
                .path("/api/v1/products/{id}")
                .buildAndExpand(product.getId())
                .toUri();
        return created(uri).body(new ProductDetailsDTO(product));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDetailsDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid ProductPostDTO productPostDTO
    ) {
        ProductDetailsDTO updated = productService.update(id, productPostDTO);
        return ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDetailsDTO> delete(@PathVariable Long id) {
        ProductDetailsDTO dto = productService.delete(id);
        return ok(dto);
    }
}
