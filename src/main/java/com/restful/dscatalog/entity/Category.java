package com.restful.dscatalog.entity;

import com.restful.dscatalog.dto.categoria.CategoryPostDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.Objects;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.NONE;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "Category")
@Table(
        name = "tb_category",
        uniqueConstraints = {
                @UniqueConstraint(name = "category_name_unique", columnNames = "name")
        }
)
public class Category implements Cloneable {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Setter(NONE)
    private Long id;
    private String name;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime updatedAt;

    public Category(CategoryPostDTO name) {
        this.name = name.name();
    }

    public Category(Category category) {
        this.id = category.id;
        this.name = category.name;
        this.createdAt = category.createdAt;
        this.updatedAt = category.updatedAt;
    }

    public Category(String capitalize) {
        this.name = capitalize;
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public Category clone() {
        Category clone = null;
        try {
            clone = new Category(this);
        } catch (Exception ignored) {
        }
        return clone;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hash = 1;

        hash *= prime + (id == 0 ? 0 : Long.hashCode(id));
        hash *= prime + ((name == null) ? 0 : name.hashCode());

        if (hash < 0) hash = -hash;

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;

        Category other = (Category) obj;

        return Objects.equals(this.id, other.id) &&
               Objects.equals(this.name, other.name);
    }

    @Override
    public String toString() {
        return "{\"Category\":{" +
               "\"id\":" + id +
               ", \"name\":\"" + name + '\"' +
               "}}";
    }
}
