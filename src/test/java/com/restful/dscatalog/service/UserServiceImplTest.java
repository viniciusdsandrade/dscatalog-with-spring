package com.restful.dscatalog.service;

import com.restful.dscatalog.dto.user.UserDTO;
import com.restful.dscatalog.dto.user.UserInsertDTO;
import com.restful.dscatalog.dto.user.UserUpdateDTO;
import com.restful.dscatalog.entity.Role;
import com.restful.dscatalog.entity.User;
import com.restful.dscatalog.exception.DuplicateEntryException;
import com.restful.dscatalog.exception.ResourceNotFoundException;
import com.restful.dscatalog.exception.ValidationException;
import com.restful.dscatalog.projections.UserDetailsProjection;
import com.restful.dscatalog.repository.RoleRepository;
import com.restful.dscatalog.repository.UserRepository;
import com.restful.dscatalog.service.impl.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    private static <T> T withId(T target, long id) {
        setField(target, "id", id);
        return target;
    }

    private static User newUser(String first, String last, String email) {
        return new User(
                first,
                last,
                email,
                "h"
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("loadUserByUsername: retorna UserDetails com roles quando email existe")
    void loadUserByUsername_returnsUserWithRoles() {
        String email = "alice@example.com";
        String hash = "{bcrypt}xxx";
        var projectionStub1 = new ProjectionStub(email, hash, 1L, "ROLE_CLIENT");
        var projectionStub2 = new ProjectionStub(email, hash, 2L, "ROLE_ADMIN");
        given(userRepository.searchUserAndRolesByEmail(email))
                .willReturn(List.of(projectionStub1, projectionStub2));

        var userDetails = userServiceImpl.loadUserByUsername(email);

        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.getPassword()).isEqualTo(hash);
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_CLIENT", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("loadUserByUsername: lança UsernameNotFoundException quando vazio")
    void loadUserByUsername_throwsWhenNotFound() {
        given(userRepository.searchUserAndRolesByEmail("missing@example.com"))
                .willReturn(List.of());

        assertThrows(UsernameNotFoundException.class,
                () -> userServiceImpl.loadUserByUsername("missing@example.com"));
    }

    @Test
    @DisplayName("findAllPaged: pagina e mapeia para UserDTO")
    void findAllPaged_mapsToDTO() {
        User user1 = withId(newUser("A", "B", "a@x.com"), 1L);
        Page<User> page = new PageImpl<>(List.of(user1), PageRequest.of(0, 1), 2);
        given(userRepository.findAll(any(Pageable.class))).willReturn(page);

        Page<UserDTO> userDTOPage = userServiceImpl.findAllPaged(PageRequest.of(0, 1));

        assertThat(userDTOPage.getTotalElements()).isEqualTo(2);
        assertThat(userDTOPage.getContent()).hasSize(1);
        assertThat(userDTOPage.getContent().getFirst().getEmail()).isEqualTo("a@x.com");
    }

    @Test
    @DisplayName("findById: existente retorna DTO; inexistente lança ResourceNotFoundException")
    void findById_behavior() {
        User existing = withId(newUser("J", "K", "jk@x.com"), 5L);
        given(userRepository.findById(5L)).willReturn(Optional.of(existing));
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThat(userServiceImpl.findById(5L).getEmail()).isEqualTo("jk@x.com");
        assertThrows(ResourceNotFoundException.class, () -> userServiceImpl.findById(999L));
    }

    @Test
    @DisplayName("insert: normaliza email, encripta senha, associa ROLE_CLIENT e salva")
    void insert_persistsWithNormalizationAndRole() {
        var userInsertDTO = new UserInsertDTO(
                "  Alice  ",
                "  Doe  ",
                "  Alice@Example.COM  ",
                "plain"
        );
        String normalized = "alice@example.com";

        given(userRepository.findByEmail(normalized)).willReturn(Optional.empty());
        given(passwordEncoder.encode("plain")).willReturn("ENC");
        given(roleRepository.getReferenceById(2L)).willReturn(new Role(2L, "ROLE_CLIENT"));
        final ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        doAnswer(inv -> {
            User user = inv.getArgument(0);
            withId(user, 42L);
            return null;
        }).when(userRepository).saveAndFlush(captor.capture());

        UserDTO userDTO = userServiceImpl.insert(userInsertDTO);

        assertThat(userDTO.getId()).isEqualTo(42L);
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo(normalized);
        assertThat(saved.getPassword()).isEqualTo("ENC");
        assertThat(saved.getRoles())
                .extracting("authority")
                .containsExactly("ROLE_CLIENT");
        verify(roleRepository).getReferenceById(2L);
        verify(passwordEncoder).encode("plain");
        verify(userRepository).saveAndFlush(any(User.class));
    }

    @Test
    @DisplayName("insert: pré-checagem de duplicidade dispara DuplicateEntryException")
    void insert_precheckDuplicate_throws() {
        given(userRepository.findByEmail("x@y.com")).willReturn(Optional.of(new User()));
        var userInsertDTO = new UserInsertDTO("A", "B", "x@y.com", "pwd");

        assertThrows(DuplicateEntryException.class, () -> userServiceImpl.insert(userInsertDTO));
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("insert: DataIntegrityViolationException mapeada para DuplicateEntryException")
    void insert_violation_mappedToDuplicate() {
        var userInsertDTO = new UserInsertDTO("A", "B", "X@Y.com", "pwd");
        given(userRepository.findByEmail("x@y.com")).willReturn(Optional.empty());
        given(roleRepository.getReferenceById(2L)).willReturn(new Role(2L, "ROLE_CLIENT"));
        given(passwordEncoder.encode("pwd")).willReturn("ENC");
        doThrow(new DataIntegrityViolationException("uk_email"))
                .when(userRepository).saveAndFlush(any(User.class));

        assertThrows(DuplicateEntryException.class, () -> userServiceImpl.insert(userInsertDTO));
    }

    @Test
    @DisplayName("update: exige usuário autenticado")
    void update_requiresAuthentication() {
        User existing = withId(newUser("A", "B", "owner@example.com"), 7L);
        given(userRepository.findById(7L)).willReturn(Optional.of(existing));

        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> userServiceImpl.update(7L, new UserUpdateDTO("X", "Y", "z@x.com")));
    }

    @Test
    @DisplayName("update: nega quando requester != owner (AccessDeniedException)")
    void update_deniesWhenNotOwner() {
        User existing = withId(newUser("A", "B", "owner@example.com"), 7L);
        given(userRepository.findById(7L)).willReturn(Optional.of(existing));
        setPrincipal("intruder@example.com");

        assertThrows(AccessDeniedException.class,
                () -> userServiceImpl.update(7L, new UserUpdateDTO("X", "Y", "z@x.com")));
    }

    @Test
    @DisplayName("update: lança ValidationException quando email é null")
    void update_throwsWhenEmailNull() {
        User existing = withId(newUser("A", "B", "owner@example.com"), 7L);
        given(userRepository.findById(7L)).willReturn(Optional.of(existing));
        setPrincipal("owner@example.com");

        assertThrows(ValidationException.class,
                () -> userServiceImpl.update(7L, new UserUpdateDTO("X", "Y", null)));
    }

    @Test
    @DisplayName("update: lança DuplicateEntryException quando email novo = atual (case-insensitive)")
    void update_throwsWhenEmailSameAsCurrent() {
        User existing = withId(newUser("A", "B", "owner@example.com"), 7L);
        given(userRepository.findById(7L)).willReturn(Optional.of(existing));
        setPrincipal("owner@example.com");

        assertThrows(DuplicateEntryException.class,
                () -> userServiceImpl.update(7L, new UserUpdateDTO("X", "Y", "OWNER@EXAMPLE.COM")));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("update: lança DuplicateEntryException quando email já existe em outro id")
    void update_throwsWhenEmailTakenByOther() {
        User existing = withId(newUser("A", "B", "owner@example.com"), 7L);
        given(userRepository.findById(7L)).willReturn(Optional.of(existing));
        given(userRepository.existsByEmailIgnoreCaseAndIdNot("new@mail.com", 7L)).willReturn(false);
        given(userRepository.existsByEmailIgnoreCaseAndIdNot("new@mail.com", 7L)).willReturn(true);
        setPrincipal("owner@example.com");

        assertThrows(DuplicateEntryException.class,
                () -> userServiceImpl.update(7L, new UserUpdateDTO("X", "Y", "new@mail.com")));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("update: sucesso com principal simples (normaliza email e salva)")
    void update_success_withPrincipalName() {
        User existing = withId(newUser("A", "B", "owner@example.com"), 7L);
        given(userRepository.findById(7L)).willReturn(Optional.of(existing));
        given(userRepository.existsByEmailIgnoreCaseAndIdNot("new@mail.com", 7L)).willReturn(false);
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));
        setPrincipal("owner@example.com");

        UserDTO out = userServiceImpl.update(7L, new UserUpdateDTO("  NewFirst  ", " NewLast ", "  NEW@mail.com  "));

        assertThat(out.getFirstName()).isEqualTo("NewFirst");
        assertThat(out.getLastName()).isEqualTo("NewLast");
        assertThat(out.getEmail()).isEqualTo("new@mail.com");
        assertThat(existing.getEmail()).isEqualTo("new@mail.com");
        verify(userRepository).save(existing);
    }

    @Test
    @DisplayName("update: sucesso quando autenticado via JWT usando claim 'username'")
    void update_success_withJwtUsernameClaim() {
        User existing = withId(newUser("A", "B", "owner@example.com"), 7L);
        given(userRepository.findById(7L)).willReturn(Optional.of(existing));
        given(userRepository.existsByEmailIgnoreCaseAndIdNot("john@new.com", 7L)).willReturn(false);
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));
        setJwtWithUsernameClaim();

        UserDTO userDTO = userServiceImpl.update(7L, new UserUpdateDTO("John", "Smith", "john@new.com"));

        assertThat(userDTO.getEmail()).isEqualTo("john@new.com");
        verify(userRepository).save(existing);
    }

    @Test
    @DisplayName("update: falha se campos em branco forem passados para updateProfile (propaga IllegalArgumentException)")
    void update_propagatesIllegalArgument_whenBlankFields() {
        User existing = withId(newUser("A", "B", "owner@example.com"), 7L);
        given(userRepository.findById(7L)).willReturn(Optional.of(existing));
        setPrincipal("owner@example.com");

        assertThrows(IllegalArgumentException.class,
                () -> userServiceImpl.update(7L, new UserUpdateDTO("  ", "Y", "new@mail.com")));
    }

    @Test
    @DisplayName("authenticated: 401 quando não autenticado")
    void authenticated_unauthenticated_throws() {
        SecurityContextHolder.clearContext();

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> userServiceImpl.authenticated());
    }

    @Test
    @DisplayName("authenticated: 404 quando usuário não existe")
    void authenticated_userNotFound_throws() {
        setPrincipal("john@example.com");
        given(userRepository.findByEmail("john@example.com")).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userServiceImpl.authenticated());
    }

    @Test
    @DisplayName("authenticated: ok retorna entidade do repositório")
    void authenticated_ok_returnsEntity() {
        setPrincipal("john@example.com");
        User user = withId(newUser("John", "Doe", "john@example.com"), 55L);
        given(userRepository.findByEmail("john@example.com")).willReturn(Optional.of(user));

        User out = userServiceImpl.authenticated();

        assertThat(out).isSameAs(user);
        assertThat(out.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("getMe: 401 quando não autenticado")
    void getMe_unauthenticated_throws() {
        SecurityContextHolder.clearContext();

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> userServiceImpl.getMe());
    }

    @Test
    @DisplayName("getMe: 404 quando usuário não existe")
    void getMe_userNotFound_throws() {
        setPrincipal("alice@example.com");
        given(userRepository.findByEmail("alice@example.com")).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userServiceImpl.getMe());
    }

    @Test
    @DisplayName("getMe: ok retorna DTO com dados do usuário autenticado")
    void getMe_ok_returnsDTO() {
        setPrincipal("alice@example.com");
        User user = withId(newUser("Alice", "Smith", "alice@example.com"), 77L);
        given(userRepository.findByEmail("alice@example.com")).willReturn(Optional.of(user));

        UserDTO userDTO = userServiceImpl.getMe();

        assertThat(userDTO.getId()).isEqualTo(77L);
        assertThat(userDTO.getEmail()).isEqualTo("alice@example.com");
        assertThat(userDTO.getFirstName()).isEqualTo("Alice");
        assertThat(userDTO.getLastName()).isEqualTo("Smith");
    }

    private void setPrincipal(String name) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        name,
                        "pwd",
                        List.of()
                ));
    }

    private void setJwtWithUsernameClaim() {
        Map<String, Object> headers = Map.of("alg", "none");
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", "owner@example.com");
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                headers,
                claims
        );
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(
                        jwt,
                        List.of(),
                        "ignoredPrincipalName")
        );
    }

    private record ProjectionStub(String username, String password, Long roleId, String authority)
            implements UserDetailsProjection {
        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public Long getRoleId() {
            return roleId;
        }

        @Override
        public String getAuthority() {
            return authority;
        }
    }
}
