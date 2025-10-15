package com.restful.dscatalog.dto.product;

import java.time.LocalDateTime;
import java.util.List;

public record ProductPostDTO(
        Long id,
        String name,
        String description,
        Double price,
        String imgUrl,
        LocalDateTime date,
        List<Long> categoryIds
) {

    public ProductPostDTO {
        categoryIds = (categoryIds == null) ? List.of() : List.copyOf(categoryIds);
    }
}
