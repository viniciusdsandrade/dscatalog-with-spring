package com.restful.dscatalog.dto;

import com.restful.dscatalog.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DadosDetalhamentoCategoria")
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
