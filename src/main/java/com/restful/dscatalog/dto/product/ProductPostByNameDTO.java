package com.restful.dscatalog.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public record ProductPostByNameDTO(
        @NotBlank(message = "name é obrigatório")
        String name,

        @NotBlank(message = "description é obrigatória")
        String description,

        @NotNull(message = "price é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "price deve ser > 0")
        Double price,

        String imgUrl,
        LocalDateTime date,

        @NotNull(message = "categoryNames é obrigatório (pode ser vazio)")
        List<@NotBlank String> categoryNames
) {
}
