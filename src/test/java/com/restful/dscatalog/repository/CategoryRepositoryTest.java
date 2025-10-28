package com.restful.dscatalog.repository;

import com.restful.dscatalog.entity.Category;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.ANY;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = ANY)
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @AfterEach
    void clear() {
        testEntityManager.clear();
    }

    private Category newCategory(String name) {
        return testEntityManager.persistAndFlush(new Category(name));
    }

    @Test
    @DisplayName("findByNameIgnoreCase: retorna presente (case-insensitive) quando existe")
    void findByNameIgnoreCase_returnsPresent_whenExists_caseInsensitive() {
        var saved = newCategory("Eletrônicos");
        testEntityManager.clear();

        Optional<Category> opt = categoryRepository.findByNameIgnoreCase("eLeTrÔnIcOs");

        assertThat(opt).isPresent();
        assertThat(opt.get().getId()).isEqualTo(saved.getId());
        assertThat(opt.get().getName()).isEqualTo("Eletrônicos");
    }

    @Test
    @DisplayName("findByNameIgnoreCase: retorna vazio quando não existe")
    void findByNameIgnoreCase_returnsEmpty_whenNotExists() {
        newCategory("Informática");
        testEntityManager.clear();

        assertThat(categoryRepository.findByNameIgnoreCase("Livros")).isEmpty();
    }
    
    @Test
    @DisplayName("findById: retorna Optional presente quando o id existe")
    void findById_returnsPresentOptional_whenIdExists() {
        var saved = newCategory("Games");
        testEntityManager.clear();

        var opt = categoryRepository.findById(saved.getId());

        assertThat(opt).isPresent();
        assertThat(opt.get().getId()).isEqualTo(saved.getId());
        assertThat(opt.get().getName()).isEqualTo("Games");
    }

    @Test
    @DisplayName("findById: retorna Optional vazio quando o id não existe")
    void findById_returnsEmptyOptional_whenIdDoesNotExist() {
        var saved = newCategory("Acessórios");
        long missing = saved.getId() + 10_000L;
        testEntityManager.clear();

        assertThat(categoryRepository.findById(missing)).isEmpty();
    }

    @Test
    @DisplayName("findById: traduz IllegalArgumentException para InvalidDataAccessApiUsageException quando id é null")
    void findById_throwsInvalidDataAccessApiUsageException_whenIdIsNull() {
        var exception = assertThrows(InvalidDataAccessApiUsageException.class, () -> categoryRepository.findById(null));
        assertThat(exception.getMostSpecificCause())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    @DisplayName("save: lança DataIntegrityViolationException quando 'name' viola unique constraint")
    void save_throwsDataIntegrityViolation_whenNameIsDuplicated() {
        newCategory("Únicos");
        var dup = new Category();
        dup.setName("Únicos");

        assertThrows(ConstraintViolationException.class, () -> testEntityManager.persistAndFlush(dup));
    }
}
