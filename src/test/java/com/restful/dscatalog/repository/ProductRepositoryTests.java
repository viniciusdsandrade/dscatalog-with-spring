package com.restful.dscatalog.repository;

import com.restful.dscatalog.entity.Category;
import com.restful.dscatalog.entity.Product;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.PersistenceUnitUtil;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.ANY;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = ANY)
class ProductRepositoryTests {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private PersistenceUnitUtil persistenceUnitUtil;

    @BeforeEach
    void setUp() {
        persistenceUnitUtil = testEntityManager
                .getEntityManager()
                .getEntityManagerFactory()
                .getPersistenceUnitUtil();
    }

    @AfterEach
    void tearDown() {
        testEntityManager.clear();
    }

    private Category newCategory(String name) {
        return testEntityManager.persistAndFlush(new Category(name));
    }

    private Product newProduct(String name, BigDecimal price, Category... categories) {
        var product = new Product(
                name,
                name + " desc",
                price,
                now()
        );
        for (Category c : categories) {
            product.getCategories().add(c);
        }
        return testEntityManager.persistAndFlush(product);
    }

    @Test
    @DisplayName("findAllWithCategoriesByIdIn: DISTINCT, fetch das categorias e cardinalidade correta")
    void findAllWithCategoriesByIdIn_fetchesCategories_and_isDistinct() {
        var eletronicos = newCategory("Eletrônicos");
        var info = newCategory("Informática");
        var games = newCategory("Games");

        var product1 = newProduct("Notebook", new BigDecimal("5499.90"), eletronicos, info, games);
        var product2 = newProduct("Mouse", new BigDecimal("229.90"), info);
        var product3 = newProduct("Cafeteira", new BigDecimal("599.90")); // sem categorias

        testEntityManager.clear();

        var ids = List.of(product1.getId(), product2.getId(), product3.getId());
        var out = productRepository.findAllWithCategoriesByIdIn(ids);

        Map<Long, Product> byId = out.stream().collect(Collectors.toMap(Product::getId, Function.identity()));
        assertThat(out).hasSize(3);
        assertThat(byId.keySet()).containsExactlyInAnyOrderElementsOf(ids);

        assertThat(persistenceUnitUtil.isLoaded(byId.get(product1.getId()), "categories")).isTrue();
        assertThat(persistenceUnitUtil.isLoaded(byId.get(product2.getId()), "categories")).isTrue();
        assertThat(persistenceUnitUtil.isLoaded(byId.get(product3.getId()), "categories")).isTrue();

        assertThat(byId.get(product1.getId()).getCategories()).hasSize(3);
        assertThat(byId.get(product2.getId()).getCategories()).hasSize(1);
        assertThat(byId.get(product3.getId()).getCategories()).isEmpty();
    }

    @Test
    @DisplayName("findAllWithCategoriesByIdIn: ids inexistentes são ignorados")
    void findAllWithCategoriesByIdIn_ignoresMissingIds() {
        var acc = newCategory("Acessórios");
        var product1 = newProduct("Headset", new BigDecimal("379.90"), acc);

        testEntityManager.clear();

        var out = productRepository.findAllWithCategoriesByIdIn(List.of(product1.getId(), -1L, -2L));
        assertThat(out).extracting(Product::getId).containsExactly(product1.getId());
        assertThat(out.getFirst().getCategories()).hasSize(1);
    }

    @Test
    @DisplayName("findById: retorna Optional presente quando o id existe")
    void findById_returnsPresentOptional_whenIdExists() {
        var info = newCategory("Informática");
        var saved = newProduct("Teclado ABNT2", new BigDecimal("299.90"), info);

        testEntityManager.clear();

        var opt = productRepository.findById(saved.getId());
        assertThat(opt).isPresent();
        var found = opt.orElseThrow();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getName()).isEqualTo("Teclado ABNT2");
        assertThat(found.getPrice()).isEqualByComparingTo("299.90");
    }

    @Test
    @DisplayName("findById: retorna Optional vazio quando o id não existe")
    void findById_returnsEmptyOptional_whenIdDoesNotExist() {
        var saved = newProduct("Mouse USB", new BigDecimal("79.90"));
        long missingId = saved.getId() + 9999L;

        testEntityManager.clear();

        assertThat(productRepository.findById(missingId)).isEmpty();
    }

    @Test
    @DisplayName("findById: traduz IllegalArgumentException para InvalidDataAccessApiUsageException quando id é null")
    void findById_throwsInvalidDataAccessApiUsageException_whenIdIsNull() {
        var exception = assertThrows(InvalidDataAccessApiUsageException.class,
                () -> productRepository.findById(null));
        assertThat(exception.getMostSpecificCause())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be null");
    }
}
