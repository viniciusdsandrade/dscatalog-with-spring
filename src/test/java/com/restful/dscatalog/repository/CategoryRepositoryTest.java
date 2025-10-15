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
class CategoryRepositoryTests {

    @Autowired
    CategoryRepository repository;

    @Autowired
    TestEntityManager em;

    @AfterEach
    void clear() {
        em.clear();
    }

    private Category cat(String name) {
        var c = new Category();
        c.setName(name);
        return em.persistAndFlush(c);
    }


    @Test
    @DisplayName("findByNameIgnoreCase: retorna presente (case-insensitive) quando existe")
    void findByNameIgnoreCase_returnsPresent_whenExists_caseInsensitive() {
        var saved = cat("Eletrônicos");
        em.clear();

        Optional<Category> opt = repository.findByNameIgnoreCase("eLeTrÔnIcOs");

        assertThat(opt).isPresent();
        assertThat(opt.get().getId()).isEqualTo(saved.getId());
        // mantemos o casing salvo no banco
        assertThat(opt.get().getName()).isEqualTo("Eletrônicos");
    }

    @Test
    @DisplayName("findByNameIgnoreCase: retorna vazio quando não existe")
    void findByNameIgnoreCase_returnsEmpty_whenNotExists() {
        cat("Informática");
        em.clear();

        assertThat(repository.findByNameIgnoreCase("Livros")).isEmpty();
    }


    @Test
    @DisplayName("findById: retorna Optional presente quando o id existe")
    void findById_returnsPresentOptional_whenIdExists() {
        var saved = cat("Games");
        em.clear();

        var opt = repository.findById(saved.getId());

        assertThat(opt).isPresent();
        assertThat(opt.get().getId()).isEqualTo(saved.getId());
        assertThat(opt.get().getName()).isEqualTo("Games");
    }

    @Test
    @DisplayName("findById: retorna Optional vazio quando o id não existe")
    void findById_returnsEmptyOptional_whenIdDoesNotExist() {
        var saved = cat("Acessórios");
        long missing = saved.getId() + 10_000L;
        em.clear();

        assertThat(repository.findById(missing)).isEmpty();
    }

    @Test
    @DisplayName("findById: traduz IllegalArgumentException para InvalidDataAccessApiUsageException quando id é null")
    void findById_throwsInvalidDataAccessApiUsageException_whenIdIsNull() {
        var ex = assertThrows(InvalidDataAccessApiUsageException.class, () -> repository.findById(null));
        assertThat(ex.getMostSpecificCause())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    @DisplayName("save: lança DataIntegrityViolationException quando 'name' viola unique constraint")
    void save_throwsDataIntegrityViolation_whenNameIsDuplicated() {
        cat("Únicos");
        var dup = new Category();
        dup.setName("Únicos");

        assertThrows(ConstraintViolationException.class, () -> em.persistAndFlush(dup));
    }
}
