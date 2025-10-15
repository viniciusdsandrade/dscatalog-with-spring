package com.restful.dscatalog.service;

import com.restful.dscatalog.dto.product.ProductDetailsDTO;
import com.restful.dscatalog.dto.product.ProductPostByNameDTO;
import com.restful.dscatalog.dto.product.ProductPostDTO;
import com.restful.dscatalog.entity.Category;
import com.restful.dscatalog.entity.Product;
import com.restful.dscatalog.repository.CategoryRepository;
import com.restful.dscatalog.repository.ProductRepository;
import com.restful.dscatalog.service.impl.ProductServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.ANY;

@DataJpaTest
@Import(ProductServiceImpl.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = ANY)
public class ProductServiceTest {

    @Autowired
    ProductService service;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    CategoryRepository categoryRepository;

    private Category cat(String name) {
        return categoryRepository.save(new Category(null, name, null, null));
    }

    @Test
    @DisplayName("create: persiste produto com categorias por ID")
    void create_persists_with_categoryIds() {
        Category c1 = cat("Eletrônicos");
        Category c2 = cat("Informática");

        var dto = new ProductPostDTO(
                "Notebook Ultra 14",
                "i7, 16GB",
                5499.90,
                "http://img",
                LocalDateTime.now(),
                List.of(c1.getId(), c2.getId())
        );

        Product saved = service.create(dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Notebook Ultra 14");
        assertThat(saved.getPrice()).isEqualByComparingTo(new BigDecimal("5499.90"));
        assertThat(saved.getCategories()).extracting(Category::getName)
                .containsExactlyInAnyOrder("Eletrônicos", "Informática");
    }

    @Test
    @DisplayName("create: lança EntityNotFoundException quando alguma categoria não existe")
    void create_throws_when_some_categoryId_missing() {
        Category c1 = cat("Games");
        Long missing = 999_999L;

        var dto = new ProductPostDTO(
                "Headset",
                "7.1",
                379.90,
                null,
                LocalDateTime.now(),
                List.of(c1.getId(), missing)
        );

        assertThrows(EntityNotFoundException.class, () -> service.create(dto));
    }

    @Test
    @DisplayName("createByCategoryNames: normaliza nomes (case-insensitive), de-duplica e cria categorias ausentes")
    void createByCategoryNames_createsOrReuses_categories_caseInsensitive() {
        cat("Acessórios");

        var dto = new ProductPostByNameDTO(
                "Mouse",
                "RGB",
                229.90,
                null,
                null,
                List.of("  acessórios ", "ELETRÔNICOS", "eletrônicos")
        );

        Product p = service.createByCategoryNames(dto);

        assertThat(p.getId()).isNotNull();
        assertThat(p.getCategories()).extracting(Category::getName)
                .containsExactlyInAnyOrder("Acessórios", "Eletrônicos");
        assertThat(categoryRepository.findAll())
                .extracting(Category::getName)
                .contains("Acessórios", "Eletrônicos");
    }

    @Test
    @DisplayName("findById: existente retorna entidade; inexistente lança EntityNotFound ao acessar proxy")
    void findById_proxy_behavior() {
        Product existing = service.create(new ProductPostDTO(
                "Webcam", "1080p", 289.90, null, LocalDateTime.now(), null
        ));
        Product found = service.findById(existing.getId());
        assertThat(found.getName()).isEqualTo("Webcam");

        Long missingId = existing.getId() + 9999;
        Product proxy = service.findById(missingId);
        assertThrows(EntityNotFoundException.class, proxy::getName); // força acesso ao estado do proxy
    }

    @Test
    @DisplayName("listAll: pagina resultados em DTO")
    void listAll_pages_in_dto() {
        service.create(new ProductPostDTO("P1", "d", 1.0, null, LocalDateTime.now(), null));
        service.create(new ProductPostDTO("P2", "d", 2.0, null, LocalDateTime.now(), null));

        var page = service.listAll(org.springframework.data.domain.PageRequest.of(0, 1));
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0)).isInstanceOf(ProductDetailsDTO.class);
    }

    @Test
    @DisplayName("listAllWithoutPagination: carrega categorias via query específica e mapeia em DTO")
    void listAllWithoutPagination_fetches_categories() {
        Category c = cat("Livros");
        var p = service.create(new ProductPostDTO("Livro Clean Code", "Uncle Bob", 139.90, null, LocalDateTime.now(), List.of(c.getId())));

        List<ProductDetailsDTO> dtos = service.listAllWithoutPagination();
        assertThat(dtos).extracting(ProductDetailsDTO::id).contains(p.getId());
    }

    @Test
    @DisplayName("update: atualiza campos escalares e substitui categorias quando IDs são fornecidos")
    void update_updates_scalars_and_categories() {
        Category a = cat("A");
        Category b = cat("B");
        Product base = service.create(new ProductPostDTO("Old", "x", 10.0, null, LocalDateTime.now(), List.of(a.getId())));

        var dto = new ProductPostDTO("New", "y", 20.0, "http://img", LocalDateTime.now(), List.of(b.getId()));
        ProductDetailsDTO out = service.update(base.getId(), dto);

        Product reloaded = productRepository.findById(base.getId()).orElseThrow();
        assertThat(out.name()).isEqualTo("New");
        assertThat(reloaded.getName()).isEqualTo("New");
        assertThat(reloaded.getPrice()).isEqualByComparingTo("20.0");
        assertThat(reloaded.getCategories()).extracting(Category::getName).containsExactly("B");
    }

    @Test
    @DisplayName("delete: remove e retorna DTO do removido")
    void delete_removes_and_returnsDTO() {
        Product base = service.create(new ProductPostDTO("ToDel", "d", 5.0, null, LocalDateTime.now(), null));
        Long id = base.getId();

        ProductDetailsDTO deleted = service.delete(id);

        assertThat(deleted.id()).isEqualTo(id);
        assertThat(productRepository.findById(id)).isEmpty();
    }
}
