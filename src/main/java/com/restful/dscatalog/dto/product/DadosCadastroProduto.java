package com.restful.dscatalog.dto.product;

import com.restful.dscatalog.entity.Category;
import com.restful.dscatalog.entity.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class DadosCadastroProduto {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private String imgUrl;

    private LocalDateTime date;

    private List<Long> categoryIds = new ArrayList<>();

    public DadosCadastroProduto(Long id, String name, String description, Double price, String imgUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imgUrl = imgUrl;
    }

    public DadosCadastroProduto(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice() != null ? product.getPrice().doubleValue() : null;
        this.imgUrl = product.getImgUrl();
        this.date = product.getDate();
        if (product.getCategories() != null) {
            this.categoryIds = product.getCategories().stream()
                    .map(Category::getId)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }
}
