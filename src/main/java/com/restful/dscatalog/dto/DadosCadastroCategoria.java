package com.restful.dscatalog.dto;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;


public record DadosCadastroCategoria(
        @NotNull(message = "O campo nome é obrigatório")
        @Length(min = 3, max = 50, message = "O campo nome deve ter entre 3 e 50 caracteres")
        String name
) {
}
