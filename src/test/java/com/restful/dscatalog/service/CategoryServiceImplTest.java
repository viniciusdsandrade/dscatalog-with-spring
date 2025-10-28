package com.restful.dscatalog.service;

import com.restful.dscatalog.dto.categoria.CategoryDetailsDTO;
import com.restful.dscatalog.dto.categoria.CategoryPostDTO;
import com.restful.dscatalog.entity.Category;
import com.restful.dscatalog.exception.DatabaseException;
import com.restful.dscatalog.exception.DuplicateEntryException;
import com.restful.dscatalog.exception.ResourceNotFoundException;
import com.restful.dscatalog.repository.CategoryRepository;
import com.restful.dscatalog.service.impl.CategoryServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private CategoryServiceImpl service;

    private static Category newCategory(String name) {
        Category c = new Category();
        c.setName(name);
        return c;
    }

    private static Category withId(Category c, long id) {
        setField(c, "id", id);
        return c;
    }

    @Test
    @DisplayName("create: salva e retorna entidade (id preenchido)")
    void create_persists_and_returns_entity() {
        var dto = new CategoryPostDTO("Eletrônicos");

        given(categoryRepository.saveAndFlush(any(Category.class)))
                .willAnswer(inv -> {
                    Category c = inv.getArgument(0);
                    withId(c, 10L);
                    return c;
                });

        Category out = service.create(dto);

        assertThat(out.getId()).isEqualTo(10L);
        assertThat(out.getName()).isEqualTo("Eletrônicos");
        verify(categoryRepository).saveAndFlush(any(Category.class));
    }

    @Test
    @DisplayName("create: lança DuplicateEntryException quando violar unicidade")
    void create_throwsDuplicateEntry_onConstraint() {
        var dto = new CategoryPostDTO("Eletrônicos");
        given(categoryRepository.saveAndFlush(any(Category.class)))
                .willThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(DuplicateEntryException.class, () -> service.create(dto));
    }

    @Test
    @DisplayName("findById: existente retorna entidade; inexistente lança ResourceNotFoundException")
    void findById_behaviour() {
        Category existing = withId(newCategory("Games"), 5L);

        // mocka o que o service realmente usa hoje
        given(categoryRepository.findById(5L)).willReturn(Optional.of(existing));
        given(categoryRepository.findById(9999L)).willReturn(Optional.empty());

        assertThat(service.findById(5L).getName()).isEqualTo("Games");
        assertThrows(ResourceNotFoundException.class, () -> service.findById(9999L));
    }

    @Test
    @DisplayName("listAll: pagina e mapeia para CategoryDetailsDTO")
    void listAll_pages_to_dto() {
        Category c = withId(newCategory("Livros"), 1L);
        Page<Category> page = new PageImpl<>(List.of(c), PageRequest.of(0, 1), 2);
        given(categoryRepository.findAll(any(Pageable.class))).willReturn(page);

        Page<CategoryDetailsDTO> out = service.listAll(PageRequest.of(0, 1));

        assertThat(out.getTotalElements()).isEqualTo(2);
        assertThat(out.getContent()).hasSize(1);
        assertThat(out.getContent().getFirst().name()).isEqualTo("Livros");
    }

    @Test
    @DisplayName("update: altera nome (trim aplicado) e devolve DTO")
    void update_updates_name_and_returns_dto() {
        Category managed = withId(newCategory(" Antigo "), 3L);
        given(categoryRepository.getReferenceById(3L)).willReturn(managed);
        given(categoryRepository.save(any(Category.class))).willAnswer(inv -> inv.getArgument(0));

        var dto = new CategoryPostDTO("  Novo  ");
        CategoryDetailsDTO out = service.update(3L, dto);

        assertThat(out.id()).isEqualTo(3L);
        assertThat(out.name()).isEqualTo("Novo");
        assertThat(managed.getName()).isEqualTo("Novo");
        verify(categoryRepository).save(managed);
    }


    @Test
    @DisplayName("deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist")
    void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        long missingId = 123L;
        given(categoryRepository.findById(missingId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(missingId));
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("deleteShouldDoNothingWhenIdExists")
    void deleteShouldDoNothingWhenIdExists() {
        long existingId = 7L;
        Category entity = withId(newCategory("Existente"), existingId);
        given(categoryRepository.findById(existingId)).willReturn(Optional.of(entity));
        doNothing().when(categoryRepository).delete(entity);

        CategoryDetailsDTO dto = service.delete(existingId);

        assertThat(dto.id()).isEqualTo(existingId);
        assertThat(dto.name()).isEqualTo("Existente");
        verify(categoryRepository).delete(entity);
    }

    @Test
    @DisplayName("deleteShouldThrowDatabaseExceptionWhenDependentID")
    void deleteShouldThrowDatabaseExceptionWhenDependentID() {
        long dependentId = 9L;
        Category entity = withId(newCategory("UsadaEmProduto"), dependentId);
        given(categoryRepository.findById(dependentId)).willReturn(Optional.of(entity));
        doThrow(new DataIntegrityViolationException("FK violation"))
                .when(categoryRepository).delete(entity);

        assertThrows(DatabaseException.class, () -> service.delete(dependentId));
        verify(categoryRepository).delete(entity);
    }
}
