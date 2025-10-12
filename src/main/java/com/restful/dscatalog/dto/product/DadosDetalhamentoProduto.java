package com.restful.dscatalog.dto.product;

import com.restful.dscatalog.entity.Category;
import com.restful.dscatalog.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public record DadosDetalhamentoProduto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String imgUrl,
        LocalDateTime date,
        Set<String> categoryNames
) {
    public DadosDetalhamentoProduto(Product p) {
        this(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getImgUrl(),
                p.getDate(),
                p.getCategories().stream()
                        .map(Category::getName)
                        .collect(Collectors.toCollection(LinkedHashSet::new))
        );
    }
}
