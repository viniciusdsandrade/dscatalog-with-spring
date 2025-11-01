package com.restful.dscatalog.controller;


import com.restful.dscatalog.dto.categoria.CategoryPostDTO;
import com.restful.dscatalog.dto.categoria.CategoryDetailsDTO;
import com.restful.dscatalog.entity.Category;
import com.restful.dscatalog.service.CategoryService;
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

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping(value = {"/api/v1/categories", "/categories"})
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
            @PageableDefault(size = 5, sort = {"id"}) Pageable paginacao
    ) {
        Page<CategoryDetailsDTO> categories = categoryService.listAll(paginacao);
        return ok(categories);
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDetailsDTO> create(
            @RequestBody @Valid CategoryPostDTO categoryPostDTO,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        Category category = categoryService.create(categoryPostDTO);
        URI uri = uriComponentsBuilder
                .path("/api/v1/categories/{id}")
                .buildAndExpand(category.getId())
                .toUri();
        return created(uri).body(new CategoryDetailsDTO(category));
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<CategoryDetailsDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid CategoryPostDTO categoryPostDTO
    ) {
        CategoryDetailsDTO updatedCategory = categoryService.update(id, categoryPostDTO);
        return ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDetailsDTO> delete(@PathVariable Long id) {
        var dto = categoryService.delete(id);
        return ok(dto);
    }
}
