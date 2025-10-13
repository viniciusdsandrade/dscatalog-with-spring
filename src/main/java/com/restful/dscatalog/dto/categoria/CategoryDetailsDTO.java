package com.restful.dscatalog.dto.categoria;

import com.restful.dscatalog.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DadosDetalhamentoCategoria")
public record CategoryDetailsDTO(
        Long id,
        String name
) {
    public CategoryDetailsDTO(Category categoria) {
        this(
                categoria.getId(),
                categoria.getName()
        );
    }
}
