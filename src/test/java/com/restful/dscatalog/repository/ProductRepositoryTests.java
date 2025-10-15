package com.restful.dscatalog.repository;

import com.restful.dscatalog.entity.Category;
import com.restful.dscatalog.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.ANY;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = ANY)
class ProductRepositoryTests {

    @Autowired
    ProductRepository repository;

    @Autowired
    EntityManager em;

    private Category cat(String name) {
        Category c = new Category();
        c.setName(name);
        em.persist(c);
        return c;
    }

    private Product prod(String name, BigDecimal price, Category... categories) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(name + " desc");
        p.setPrice(price);
        p.setImgUrl(null);
        p.setDate(LocalDateTime.now());
        for (Category c : categories) {
            p.getCategories().add(c);
        }
        em.persist(p);
        return p;
    }

    @Test
    @DisplayName("findAllWithCategoriesByIdIn: DISTINCT, fetch das categorias e cardinalidade correta")
    void findAllWithCategoriesByIdIn_fetchesCategories_and_isDistinct() {
        Category eletronicos = cat("Eletrônicos");
        Category info        = cat("Informática");
        Category games       = cat("Games");

        Product p1 = prod("Notebook", new BigDecimal("5499.90"), eletronicos, info, games);
        Product p2 = prod("Mouse", new BigDecimal("229.90"), info);
        Product p3 = prod("Cafeteira", new BigDecimal("599.90"));

        em.flush();
        em.clear();

        List<Long> ids = List.of(p1.getId(), p2.getId(), p3.getId());
        List<Product> out = repository.findAllWithCategoriesByIdIn(ids);

        Map<Long, Product> byId = out.stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        assertThat(out).hasSize(3);
        assertThat(byId.keySet()).containsExactlyInAnyOrderElementsOf(ids);

        PersistenceUnitUtil util = em.getEntityManagerFactory().getPersistenceUnitUtil();
        assertThat(util.isLoaded(byId.get(p1.getId()), "categories")).isTrue();
        assertThat(util.isLoaded(byId.get(p2.getId()), "categories")).isTrue();
        assertThat(util.isLoaded(byId.get(p3.getId()), "categories")).isTrue();

        assertThat(byId.get(p1.getId()).getCategories()).hasSize(3);
        assertThat(byId.get(p2.getId()).getCategories()).hasSize(1);
        assertThat(byId.get(p3.getId()).getCategories()).isEmpty();
    }

    @Test
    @DisplayName("findAllWithCategoriesByIdIn: ids inexistentes são ignorados e não quebram o resultado")
    void findAllWithCategoriesByIdIn_ignoresMissingIds() {
        Category cat = cat("Acessórios");
        Product p1 = prod("Headset", new BigDecimal("379.90"), cat);
        em.flush();
        em.clear();

        List<Product> out = repository.findAllWithCategoriesByIdIn(List.of(p1.getId(), -1L, -2L));
        assertThat(out).extracting(Product::getId).containsExactly(p1.getId());
        assertThat(out.get(0).getCategories()).hasSize(1);
    }

    @Test
    @DisplayName("findById: retorna Optional<Product> presente quando o id existe")
    void findById_returnsPresentOptional_whenIdExists() {
        Category info = cat("Informática");
        Product saved = prod("Teclado ABNT2", new BigDecimal("299.90"), info);

        em.flush();
        em.clear();

        var opt = repository.findById(saved.getId());

        assertThat(opt).isPresent();
        Product found = opt.orElseThrow();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getName()).isEqualTo("Teclado ABNT2");
        assertThat(found.getPrice()).isEqualByComparingTo("299.90");
    }

    @Test
    @DisplayName("findById: retorna Optional<Product> vazio quando o id não existe")
    void findById_returnsEmptyOptional_whenIdDoesNotExist() {
        Product saved = prod("Mouse USB", new BigDecimal("79.90"));
        em.flush();
        em.clear();

        long missingId = saved.getId() + 9999L;

        var opt = repository.findById(missingId);

        assertThat(opt).isEmpty();
    }

    @DisplayName("findById: lança InvalidDataAccessApiUsageException quando id é null (exception translation)")
    @Test
    void findById_throwsInvalidDataAccessApiUsageException_whenIdIsNull() {
        var ex = assertThrows(InvalidDataAccessApiUsageException.class,
                () -> repository.findById(null));

        assertThat(ex.getMostSpecificCause())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The given id must not be null");
    }
}
