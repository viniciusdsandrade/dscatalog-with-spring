package com.restful.dscatalog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Entity(name = "Category")
@Table(
        name = "tb_category",
        schema = "db_ds_catalog",
        uniqueConstraints = {
                @UniqueConstraint(name = "category_name_unique", columnNames = "name")
        }
)
public class Category implements Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private long id;
    private String name;

    public Category() {
    }

    public Category(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Category(String name) {
        this.name = name;
    }

    // Copy constructor
    public Category(Category category) {
        this.id = category.id;
        this.name = category.name;
    }

    @Override
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

        if (hash < 0) hash = hash * -1;

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
