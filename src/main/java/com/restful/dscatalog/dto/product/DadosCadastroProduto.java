package com.restful.dscatalog.dto.product;

import com.restful.dscatalog.entity.Category;
import com.restful.dscatalog.entity.Product;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record DadosCadastroProduto(
        Long id,
        String name,
        String description,
        Double price,
        String imgUrl,
        LocalDateTime date,
        List<Long> categoryIds
) {

    public DadosCadastroProduto {
        categoryIds = (categoryIds == null) ? List.of() : List.copyOf(categoryIds);
    }

    public DadosCadastroProduto(Long id, String name, String description, Double price, String imgUrl) {
        this(id, name, description, price, imgUrl, null, List.of());
    }

    public DadosCadastroProduto(Product product) {
        this(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice() != null ? product.getPrice().doubleValue() : null,
                product.getImgUrl(),
                product.getDate(),
                product.getCategories() != null
                        ? product.getCategories().stream()
                        .map(Category::getId)
                        .collect(Collectors.toCollection(ArrayList::new))
                        : List.of()
        );
    }
}
