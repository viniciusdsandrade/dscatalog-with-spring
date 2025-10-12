package com.restful.dscatalog.dto;

import com.restful.dscatalog.entity.Category;
import com.restful.dscatalog.entity.Product;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class DadosCadastroProduto {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private String imgUrl;

    @Column(columnDefinition = "DATETIME(6)")
    private LocalDateTime date;

    private List<DadosCadastroCategoria> categories = new ArrayList<>();

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
        this.price = product.getPrice().doubleValue();
        this.imgUrl = product.getImgUrl();
    }

    public DadosCadastroProduto(Product product, Set<Category> categories) {
        this(product);
        categories.forEach(cat -> this.categories.add(new DadosCadastroCategoria(categories.toString())));
    }
}
