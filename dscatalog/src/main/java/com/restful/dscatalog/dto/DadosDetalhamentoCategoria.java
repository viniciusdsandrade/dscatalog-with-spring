package com.restful.dscatalog.dto;

import com.restful.dscatalog.entity.Category;

public record DadosDetalhamentoCategoria(
        Long id,
        String name
) {
    public DadosDetalhamentoCategoria(Category categoria) {
        this(
                categoria.getId(),
                categoria.getName()
        );
    }
}
