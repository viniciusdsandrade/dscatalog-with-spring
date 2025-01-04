package com.restful.dscatalog.controller;


import com.restful.dscatalog.dto.DadosCadastroCategoria;
import com.restful.dscatalog.dto.DadosDetalhamentoCategoria;
import com.restful.dscatalog.entity.Category;
import com.restful.dscatalog.service.CategoryService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/categories")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<DadosDetalhamentoCategoria> createCategory(
            @RequestBody @Valid DadosCadastroCategoria dto,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        Category category = categoryService.create(dto);
        URI uri = uriComponentsBuilder.path("/api/v1/categories/{id}").buildAndExpand(category.getId()).toUri();
        return created(uri).body(new DadosDetalhamentoCategoria(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoCategoria> getCategoria(@PathVariable Long id) {
        Category category = categoryService.buscarPorId(id);
        return ok(new DadosDetalhamentoCategoria(category));
    }
}
