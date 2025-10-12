package com.restful.dscatalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.annotations.FetchMode.SUBSELECT;

@ToString
@EqualsAndHashCode(callSuper = false)
@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_product")
public class Product {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imgUrl;

    @Column(columnDefinition = "DATETIME(6)")
    private LocalDateTime date;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tb_product_category",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Fetch(SUBSELECT)
    @BatchSize(size = 50)
    private Set<Category> categories = new HashSet<>();
}
