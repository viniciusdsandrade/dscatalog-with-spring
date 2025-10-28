package com.restful.dscatalog.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record ProductPostDTO(
        @NotBlank(message = "name é obrigatório")
        String name,

        @NotBlank(message = "description é obrigatória")
        String description,

        @NotNull(message = "price é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "price deve ser > 0")
        Double price,

        String imgUrl,
        LocalDateTime date,
        List<Long> categoryIds
) {

    public ProductPostDTO {
        categoryIds = (categoryIds == null)
                ? List.of()
                : List.copyOf(categoryIds);
    }
}
