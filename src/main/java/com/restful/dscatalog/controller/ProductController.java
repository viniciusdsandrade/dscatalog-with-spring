package com.restful.dscatalog.controller;

import com.restful.dscatalog.dto.product.ProductPostDTO;
import com.restful.dscatalog.dto.product.ProductPostByNameDTO;
import com.restful.dscatalog.dto.product.ProductDetailsDTO;
import com.restful.dscatalog.entity.Product;
import com.restful.dscatalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/products")
@CrossOrigin(origins = "*", allowedHeaders = "*")
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
            @PageableDefault(size = 5, sort = {"id"}) Pageable paginacao,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        Page<ProductDetailsDTO> products = productService.listAll(paginacao);
        HttpHeaders headers = buildPaginationHeaders(products, paginacao, uriComponentsBuilder);
        return ok().headers(headers).body(products);
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

    private HttpHeaders buildPaginationHeaders(
            Page<?> page,
            Pageable pageable,
            UriComponentsBuilder uriBuilder
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
        headers.add("X-Page-Number", String.valueOf(pageable.getPageNumber()));
        headers.add("X-Page-Size", String.valueOf(pageable.getPageSize()));

        String link = buildLinkHeader(page, pageable, uriBuilder);
        if (!link.isEmpty()) {
            headers.add(HttpHeaders.LINK, link);
        }
        return headers;
    }

    private String buildLinkHeader(
            Page<?> page,
            Pageable pageable,
            UriComponentsBuilder uriBuilder
    ) {
        UriComponentsBuilder base = uriBuilder.cloneBuilder()
                .replaceQueryParam("size", pageable.getPageSize())
                .replaceQueryParam("sort");
        if (pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order ->
                    base.queryParam("sort", order.getProperty() + "," + order.getDirection().name().toLowerCase())
            );
        }

        StringBuilder sb = new StringBuilder();
        if (page.hasPrevious()) appendLink(sb, buildPageUrl(base, pageable.getPageNumber() - 1), "prev");
        if (page.hasNext()) appendLink(sb, buildPageUrl(base, pageable.getPageNumber() + 1), "next");

        if (page.getTotalPages() > 0) {
            appendLink(sb, buildPageUrl(base, 0), "first");
            appendLink(sb, buildPageUrl(base, page.getTotalPages() - 1), "last");
        }
        return sb.toString();
    }

    private static String buildPageUrl(UriComponentsBuilder base, int pageIndex) {
        return base.cloneBuilder()
                .replaceQueryParam("page", pageIndex)
                .build(true)
                .toUriString();
    }

    private static void appendLink(StringBuilder sb, String url, String rel) {
        if (url == null || url.isEmpty()) return;
        if (!sb.isEmpty()) sb.append(", ");
        sb.append("<").append(url).append(">; rel=\"").append(rel).append("\"");
    }
}
