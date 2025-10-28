package com.restful.dscatalog.service;

import com.restful.dscatalog.dto.product.ProductDetailsDTO;
import com.restful.dscatalog.dto.product.ProductPostDTO;
import com.restful.dscatalog.entity.Category;
import com.restful.dscatalog.entity.Product;
import com.restful.dscatalog.exception.DatabaseException;
import com.restful.dscatalog.exception.ResourceNotFoundException;
import com.restful.dscatalog.repository.CategoryRepository;
import com.restful.dscatalog.repository.ProductRepository;
import com.restful.dscatalog.service.impl.ProductServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl service;

    private static <T> T withId(T entity, long id) {
        setField(entity, "id", id);
        return entity;
    }

    private static Product newProduct(String name, double price) {
        return new Product(
                name,
                name + " desc",
                BigDecimal.valueOf(price),
                now()
        );
    }

    private static Category newCategory(String name) {
        return new Category(name);
    }

    @Test
    @DisplayName("create: persiste com categorias por ID (sem setar id manualmente)")
    void create_persists_with_categoryIds() {
        Category category1 = withId(newCategory("Eletrônicos"), 1L);
        Category category2 = withId(newCategory("Informática"), 2L);
        given(categoryRepository.findAllById(List.of(1L, 2L)))
                .willReturn(List.of(category1, category2));

        given(productRepository.saveAndFlush(any(Product.class)))
                .willAnswer(inv -> {
                            Product p = inv.getArgument(0);
                            withId(p, 10L);
                            return p;
                        }
                );

        var productPostDTO = new ProductPostDTO(
                "Notebook Ultra 14",
                "i7, 16GB",
                5499.90,
                "http://img",
                now(),
                List.of(1L, 2L)
        );

        Product out = service.create(productPostDTO);

        assertThat(out.getId()).isEqualTo(10L);
        assertThat(out.getCategories()).extracting(Category::getName)
                .containsExactlyInAnyOrder("Eletrônicos", "Informática");
        verify(categoryRepository).findAllById(List.of(1L, 2L));
        verify(productRepository).saveAndFlush(any(Product.class));
    }

    @Test
    @DisplayName("create: lança EntityNotFoundException quando alguma categoria não existe")
    void create_throws_when_some_categoryId_missing() {
        Category onlyExisting = withId(newCategory("Games"), 1L);
        given(categoryRepository.findAllById(List.of(1L, 2L)))
                .willReturn(List.of(onlyExisting));

        var productPostDTO = new ProductPostDTO(
                "Headset",
                "7.1",
                379.90,
                null,
                now(),
                List.of(1L, 2L)
        );

        assertThrows(EntityNotFoundException.class, () -> service.create(productPostDTO));
        verify(productRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("findById: existente retorna entidade; inexistente propaga EntityNotFoundException")
    void findById_proxy_behavior() {
        Product existing = withId(newProduct("Webcam", 289.90), 10L);
        given(productRepository.findById(10L)).willReturn(Optional.of(existing));
        given(productRepository.findById(9999L)).willThrow(new EntityNotFoundException());

        assertThat(service.findById(10L).getName()).isEqualTo("Webcam");
        assertThrows(EntityNotFoundException.class, () -> service.findById(9999L));
    }

    @Test
    @DisplayName("listAll: pagina e mapeia em DTO")
    void listAll_pages_in_dto() {
        Product product1 = withId(newProduct("P1", 1.0), 1L);
        Page<Product> page = new PageImpl<>(List.of(product1), PageRequest.of(0, 1), 2);
        given(productRepository.findAll(any(Pageable.class))).willReturn(page);

        var out = service.listAll(PageRequest.of(0, 1));
        assertThat(out.getTotalElements()).isEqualTo(2);
        assertThat(out.getContent()).hasSize(1);
        assertThat(out.getContent().getFirst()).isInstanceOf(ProductDetailsDTO.class);
    }

    @Test
    @DisplayName("listAllWithoutPagination: usa fetch otimizado e mapeia DTO")
    void listAllWithoutPagination_fetches_categories() {
        Product product1 = withId(newProduct("A", 1.0), 1L);
        Product product2 = withId(newProduct("B", 2.0), 2L);

        given(productRepository.findAll()).willReturn(List.of(product1, product2));
        given(productRepository.findAllWithCategoriesByIdIn(List.of(1L, 2L)))
                .willReturn(List.of(product1, product2));

        var dtos = service.listAllWithoutPagination();
        assertThat(dtos).extracting(ProductDetailsDTO::id)
                .containsExactlyInAnyOrder(1L, 2L);
        verify(productRepository).findAllWithCategoriesByIdIn(List.of(1L, 2L));
    }

    @Test
    @DisplayName("update: atualiza campos e substitui categorias quando IDs são fornecidos")
    void update_updates_scalars_and_categories() {
        Category category1 = withId(newCategory("A"), 1L);
        Category category2 = withId(newCategory("B"), 2L);

        Product base = withId(newProduct("Old", 10.0), 100L);
        base.getCategories().add(category1);

        given(productRepository.getReferenceById(100L)).willReturn(base);
        given(categoryRepository.findAllById(List.of(2L))).willReturn(List.of(category2));
        given(productRepository.save(any(Product.class))).willAnswer(inv -> inv.getArgument(0));

        var productPostDTO = new ProductPostDTO(
                "New",
                "y",
                20.0,
                "http://img",
                now(),
                List.of(2L)
        );

        var out = service.update(100L, productPostDTO);

        assertThat(out.name()).isEqualTo("New");
        assertThat(base.getName()).isEqualTo("New");
        assertThat(base.getPrice()).isEqualByComparingTo("20.0");
        assertThat(base.getCategories()).extracting(Category::getName).containsExactly("B");
    }

    @Test
    @DisplayName("delete: remove e retorna DTO do removido")
    void delete_removes_and_returnsDTO() {
        Product product = withId(newProduct("ToDel", 5.0), 200L);
        given(productRepository.findById(200L)).willReturn(Optional.of(product));

        var dto = service.delete(200L);

        assertThat(dto.id()).isEqualTo(200L);
        verify(productRepository).delete(product);
    }


    @Test
    @DisplayName("deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist")
    void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        long missingId = 12345L;
        given(productRepository.findById(missingId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(missingId));
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    @DisplayName("deleteShouldDoNothingWhenIdExists")
    void deleteShouldDoNothingWhenIdExists() {
        long existingId = 7L;
        Product product = withId(newProduct("Existing", 10.0), existingId);
        given(productRepository.findById(existingId)).willReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        ProductDetailsDTO productDetailsDTO = service.delete(existingId);

        assertThat(productDetailsDTO.id()).isEqualTo(existingId);
        verify(productRepository).delete(product);
    }

    @Test
    @DisplayName("deleteShouldThrowDatabaseExceptionWhenDependentID")
    void deleteShouldThrowDatabaseExceptionWhenDependentID() {
        long dependentId = 9L;
        Product product = withId(newProduct("Dependent", 20.0), dependentId);
        given(productRepository.findById(dependentId)).willReturn(Optional.of(product));
        doThrow(new DataIntegrityViolationException("FK violation"))
                .when(productRepository).delete(product);
        assertThrows(DatabaseException.class, () -> service.delete(dependentId));
        verify(productRepository).delete(product);
    }
}
