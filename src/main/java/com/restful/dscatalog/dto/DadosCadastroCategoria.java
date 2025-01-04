package com.restful.dscatalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;


@Schema(name = "DadosCadastroCategoria")
public record DadosCadastroCategoria(
        @NotNull(message = "O campo nome é obrigatório")
        @Length(min = 3, max = 50, message = "O campo nome deve ter entre 3 e 50 caracteres")
        String name
) {
}
