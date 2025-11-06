package com.restful.dscatalog.repository;

import com.restful.dscatalog.entity.Role;
import com.restful.dscatalog.entity.User;
import com.restful.dscatalog.projections.UserDetailsProjection;
import jakarta.persistence.PersistenceUnitUtil;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.stat.Statistics;
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
    private TestEntityManager testEntityManager;

    private PersistenceUnitUtil persistenceUnitUtil;

    @BeforeEach
    void setUp() {
        persistenceUnitUtil = testEntityManager.getEntityManager()
                .getEntityManagerFactory()
                .getPersistenceUnitUtil();
    }

    @AfterEach
    void tearDown() {
        testEntityManager.clear();
    }

    private Role newRole(String authority) {
        Role role = new Role(authority);
        return testEntityManager.persistAndFlush(role);
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

        for (Role role : roles) {
            user.getRoles().add(role);
        }

        return testEntityManager.persistAndFlush(user);
    }

    @Test
    @DisplayName("findByEmail: retorna Optional presente e carrega roles via @EntityGraph")
    void findByEmail_returnsPresent_andLoadsRoles() {
        var admin = newRole("ROLE_ADMIN");
        var roleOperator = newRole("ROLE_OPERATOR");
        var saved = newUser("John", "Doe", "john@example.com", "{noop}pwd", admin, roleOperator);

        testEntityManager.clear();

        var optionalUser = userRepository.findByEmail("john@example.com");
        assertThat(optionalUser).isPresent();

        var found = optionalUser.orElseThrow();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getEmail()).isEqualTo("john@example.com");

        assertThat(persistenceUnitUtil.isLoaded(found, "roles")).isTrue();
        assertThat(found.getRoles())
                .extracting(Role::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_OPERATOR");
    }

    @Test
    @DisplayName("findByEmail: retorna Optional vazio quando o email não existe")
    void findByEmail_returnsEmpty_whenNotExists() {
        newUser("Jane", "Doe", "jane@example.com", "{noop}pwd");
        testEntityManager.clear();

        assertThat(userRepository.findByEmail("absent@example.com")).isEmpty();
    }

    @Test
    @DisplayName("existsByEmailIgnoreCaseAndIdNot: true quando outro usuário tem o mesmo email (case-insensitive)")
    void existsByEmailIgnoreCaseAndIdNot_returnsTrue_onCollisionDifferentId_caseInsensitive() {
        newUser("John", "Doe", "john@example.com", "{noop}pwd");
        var user2 = newUser("Jane", "Doe", "jane@example.com", "{noop}pwd");
        testEntityManager.clear();

        boolean exists = userRepository.existsByEmailIgnoreCaseAndIdNot("JOHN@EXAMPLE.COM", user2.getId());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByEmailIgnoreCaseAndIdNot: false quando só o próprio id tem o email")
    void existsByEmailIgnoreCaseAndIdNot_returnsFalse_whenOnlySelfHasEmail() {
        var user = newUser("John", "Doe", "john@example.com", "{noop}pwd");
        testEntityManager.clear();

        boolean exists = userRepository.existsByEmailIgnoreCaseAndIdNot("john@example.com", user.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByEmailIgnoreCaseAndIdNot: false quando email não existe")
    void existsByEmailIgnoreCaseAndIdNot_returnsFalse_whenEmailAbsent() {
        var user = newUser("John", "Doe", "john@example.com", "{noop}pwd");
        testEntityManager.clear();

        boolean exists = userRepository.existsByEmailIgnoreCaseAndIdNot("nobody@example.com", user.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("searchUserAndRolesByEmail: retorna uma linha por role com campos corretos")
    void searchUserAndRolesByEmail_returnsOneRowPerRole_withCorrectFields() {
        var admin = newRole("ROLE_ADMIN");
        var roleOperator = newRole("ROLE_OPERATOR");
        newUser("John", "Doe", "john@example.com", "{noop}pwd", admin, roleOperator);
        testEntityManager.clear();

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
        testEntityManager.clear();

        assertThat(userRepository.searchUserAndRolesByEmail("absent@example.com")).isEmpty();
    }

    @Test
    @DisplayName("findById: traduz IllegalArgumentException para InvalidDataAccessApiUsageException quando id é null")
    void findById_throwsInvalidDataAccessApiUsageException_whenIdIsNull() {
        var invalidDataAccessApiUsageException = assertThrows(InvalidDataAccessApiUsageException.class, () -> userRepository.findById(null));
        assertThat(invalidDataAccessApiUsageException.getMostSpecificCause())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    @DisplayName("searchUserAndRolesByEmail: retorna vazio quando usuário não tem roles (INNER JOIN)")
    void searchUserAndRolesByEmail_userWithoutRoles_returnsEmpty() {
        newUser("Mike", "NoRole", "mike@example.com", "{noop}pwd");
        testEntityManager.clear();

        List<UserDetailsProjection> rows = userRepository.searchUserAndRolesByEmail("mike@example.com");
        assertThat(rows).isEmpty();
    }

    @Test
    @DisplayName("existsByEmailIgnoreCaseAndIdNot: com id nulo o provedor gera 'id IS NOT NULL' e retorna true quando o email existe (documenta semântica)")
    void existsByEmailIgnoreCaseAndIdNot_withNullId_documentsProviderSemantics() {
        newUser("John", "Doe", "john@example.com", "{noop}pwd");
        newUser("Jane", "Roe", "jane@example.com", "{noop}pwd");
        testEntityManager.clear();

        boolean exists = userRepository.existsByEmailIgnoreCaseAndIdNot("JOHN@EXAMPLE.COM", null);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("findByEmail: carrega usuário e roles sem N+1 (estatísticas de queries habilitadas)")
    void findByEmail_usesEntityGraph_withoutNPlusOne() {
        var admin = newRole("ROLE_ADMIN");
        var roleOperator = newRole("ROLE_OPERATOR");
        newUser("John", "Doe", "john@example.com", "{noop}pwd", admin, roleOperator);
        testEntityManager.clear();

        var entityManagerFactory = testEntityManager.getEntityManager().getEntityManagerFactory();
        var sessionFactoryImplementor = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
        Statistics stats = sessionFactoryImplementor.getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        var optionalUser = userRepository.findByEmail("john@example.com");
        assertThat(optionalUser).isPresent();

        var found = optionalUser.orElseThrow();
        assertThat(persistenceUnitUtil.isLoaded(found, "roles")).isTrue();
        assertThat(found.getRoles()).extracting(Role::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_OPERATOR");

        long statements = stats.getPrepareStatementCount();
        assertThat(statements)
                .as("Esperava no máx. 2 statements para buscar usuário + roles sem N+1 (foi %s)", statements)
                .isLessThanOrEqualTo(2);
    }
}
