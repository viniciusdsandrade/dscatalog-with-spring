package com.restful.dscatalog.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.restful.dscatalog.entity.Category;
import com.restful.dscatalog.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.stream.Collectors.toCollection;

public record ProductDetailsDTO(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String imgUrl,
        LocalDateTime date,
        @JsonProperty("categories")
        Set<String> categoryNames
) {
    public ProductDetailsDTO(Product product) {
        this(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getImgUrl(),
                product.getDate(),
                product.getCategories().stream()
                        .map(Category::getName)
                        .collect(toCollection(LinkedHashSet::new))
        );
    }
}
