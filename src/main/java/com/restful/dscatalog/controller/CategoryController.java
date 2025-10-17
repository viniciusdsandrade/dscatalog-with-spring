package com.restful.dscatalog.controller;


import com.restful.dscatalog.dto.categoria.CategoryPostDTO;
import com.restful.dscatalog.dto.categoria.CategoryDetailsDTO;
import com.restful.dscatalog.entity.Category;
import com.restful.dscatalog.service.CategoryService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/categories")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "Consulta Controller", description = "Controller para gerenciamento de consultas")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDetailsDTO> findById(@PathVariable Long id) {
        Category category = categoryService.findById(id);
        return ok(new CategoryDetailsDTO(category));
    }

    @GetMapping
    public ResponseEntity<Page<CategoryDetailsDTO>> findAll(
            @PageableDefault(size = 5, sort = {"id"}) Pageable paginacao,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        Page<CategoryDetailsDTO> categories = categoryService.listAll(paginacao);
        HttpHeaders headers = buildPaginationHeaders(categories, paginacao, uriComponentsBuilder);
        return ok().headers(headers).body(categories);
    }

    @PostMapping
    @Transactional
    @Operation(
            summary = "Cria uma categoria",
            description = "Cria uma categoria a partir de um DTO",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Categoria criada"),
                    @ApiResponse(responseCode = "400", description = "Erro de validação")
            }
    )
    public ResponseEntity<CategoryDetailsDTO> create(
            @RequestBody @Valid CategoryPostDTO dto,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        Category category = categoryService.create(dto);
        URI uri = uriComponentsBuilder
                .path("/api/v1/categories/{id}")
                .buildAndExpand(category.getId())
                .toUri();
        return created(uri).body(new CategoryDetailsDTO(category));
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<CategoryDetailsDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid CategoryPostDTO dto
    ) {
        CategoryDetailsDTO updatedCategory = categoryService.update(id, dto);
        return ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CategoryDetailsDTO> delete(@PathVariable Long id) {
        var dto = categoryService.delete(id);
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
            headers.add(HttpHeaders.LINK, link); // RFC 8288
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
