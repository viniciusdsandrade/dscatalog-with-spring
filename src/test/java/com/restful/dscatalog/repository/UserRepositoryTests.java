package com.restful.dscatalog.repository;

import com.restful.dscatalog.entity.Role;
import com.restful.dscatalog.entity.User;
import com.restful.dscatalog.projections.UserDetailsProjection;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.ANY;

@DataJpaTest
@ActiveProfiles("h2")
@AutoConfigureTestDatabase(replace = ANY)
class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager tem;

    private PersistenceUnitUtil puu;

    @BeforeEach
    void setUp() {
        puu = tem.getEntityManager()
                .getEntityManagerFactory()
                .getPersistenceUnitUtil();
    }

    @AfterEach
    void tearDown() {
        tem.clear();
    }

    private Role newRole(String authority) {
        Role role = new Role(authority);
        return tem.persistAndFlush(role);
    }

    private User newUser(
            String firstName,
            String lastName,
            String email,
            String rawPassword,
            Role... roles
    ) {
        User user = new User(
                firstName,
                lastName,
                email,
                rawPassword
        );

        for (Role r : roles) {
            user.getRoles().add(r);
        }

        return tem.persistAndFlush(user);
    }


    @Test
    @DisplayName("findByEmail: retorna Optional presente e carrega roles via @EntityGraph")
    void findByEmail_returnsPresent_andLoadsRoles() {
        var admin = newRole("ROLE_ADMIN");
        var op = newRole("ROLE_OPERATOR");
        var saved = newUser("John", "Doe", "john@example.com", "{noop}pwd", admin, op);

        tem.clear();

        var opt = userRepository.findByEmail("john@example.com");
        assertThat(opt).isPresent();

        var found = opt.orElseThrow();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getEmail()).isEqualTo("john@example.com");

        assertThat(puu.isLoaded(found, "roles")).isTrue();
        assertThat(found.getRoles())
                .extracting(Role::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_OPERATOR");
    }

    @Test
    @DisplayName("findByEmail: retorna Optional vazio quando o email não existe")
    void findByEmail_returnsEmpty_whenNotExists() {
        newUser("Jane", "Doe", "jane@example.com", "{noop}pwd");
        tem.clear();

        assertThat(userRepository.findByEmail("absent@example.com")).isEmpty();
    }

    @Test
    @DisplayName("existsByEmailIgnoreCaseAndIdNot: true quando outro usuário tem o mesmo email (case-insensitive)")
    void existsByEmailIgnoreCaseAndIdNot_returnsTrue_onCollisionDifferentId_caseInsensitive() {
        newUser("John", "Doe", "john@example.com", "{noop}pwd");
        var u2 = newUser("Jane", "Doe", "jane@example.com", "{noop}pwd");
        tem.clear();

        boolean exists = userRepository.existsByEmailIgnoreCaseAndIdNot("JOHN@EXAMPLE.COM", u2.getId());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByEmailIgnoreCaseAndIdNot: false quando só o próprio id tem o email")
    void existsByEmailIgnoreCaseAndIdNot_returnsFalse_whenOnlySelfHasEmail() {
        var user = newUser("John", "Doe", "john@example.com", "{noop}pwd");
        tem.clear();

        boolean exists = userRepository.existsByEmailIgnoreCaseAndIdNot("john@example.com", user.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByEmailIgnoreCaseAndIdNot: false quando email não existe")
    void existsByEmailIgnoreCaseAndIdNot_returnsFalse_whenEmailAbsent() {
        var user = newUser("John", "Doe", "john@example.com", "{noop}pwd");
        tem.clear();

        boolean exists = userRepository.existsByEmailIgnoreCaseAndIdNot("nobody@example.com", user.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("searchUserAndRolesByEmail: retorna uma linha por role com campos corretos")
    void searchUserAndRolesByEmail_returnsOneRowPerRole_withCorrectFields() {
        var admin = newRole("ROLE_ADMIN");
        var op = newRole("ROLE_OPERATOR");
        newUser("John", "Doe", "john@example.com", "{noop}pwd", admin, op);
        tem.clear();

        List<UserDetailsProjection> rows = userRepository.searchUserAndRolesByEmail("john@example.com");
        assertThat(rows).hasSize(2);

        assertThat(rows).allSatisfy(p -> {
            assertThat(p.getUsername()).isEqualTo("john@example.com");
            assertThat(p.getPassword()).isEqualTo("{noop}pwd");
            assertThat(p.getRoleId()).isNotNull();
            assertThat(p.getAuthority()).isIn("ROLE_ADMIN", "ROLE_OPERATOR");
        });

        assertThat(rows).extracting(UserDetailsProjection::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_OPERATOR");
    }

    @Test
    @DisplayName("searchUserAndRolesByEmail: retorna lista vazia quando não encontra usuário")
    void searchUserAndRolesByEmail_returnsEmpty_whenAbsent() {
        newUser("Jane", "Doe", "jane@example.com", "{noop}pwd");
        tem.clear();

        assertThat(userRepository.searchUserAndRolesByEmail("absent@example.com")).isEmpty();
    }

    @Test
    @DisplayName("findById: traduz IllegalArgumentException para InvalidDataAccessApiUsageException quando id é null")
    void findById_throwsInvalidDataAccessApiUsageException_whenIdIsNull() {
        var ex = assertThrows(InvalidDataAccessApiUsageException.class, () -> userRepository.findById(null));
        assertThat(ex.getMostSpecificCause())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be null");
    }
}
