package com.restful.dscatalog.controller;

import com.restful.dscatalog.dto.role.RoleDTO;
import com.restful.dscatalog.dto.user.UserDTO;
import com.restful.dscatalog.dto.user.UserInsertDTO;
import com.restful.dscatalog.dto.user.UserUpdateDTO;
import com.restful.dscatalog.service.UserService;
import com.restful.dscatalog.util.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private String baseUrl;

    @BeforeEach
    void setup() {
        this.baseUrl = "/api/v1/users";
    }

    private static UserDTO userDto(long id, String firstName, String lastName, String email) {
        return new UserDTO(id, firstName, lastName, email);
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} -> 200 e corpo com id, firstName, lastName e email")
    void findById_ok() throws Exception {
        var dto = userDto(10L, "Alice", "Silva", "alice@example.com");
        given(userService.findById(10L)).willReturn(dto);

        mockMvc.perform(get(baseUrl + "/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Silva"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    @DisplayName("GET /api/v1/users -> 200 e paginação (content)")
    void getAll_ok_paged() throws Exception {
        var user1 = userDto(1L, "Alice", "Silva", "alice@example.com");
        var user2 = userDto(2L, "Bob", "Souza", "bob@example.com");
        var page = new PageImpl<>(List.of(user1, user2), PageRequest.of(0, 5), 2);
        given(userService.findAllPaged(any(Pageable.class))).willReturn(page);

        mockMvc.perform(get(baseUrl)
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].firstName").value("Alice"))
                .andExpect(jsonPath("$.content[0].lastName").value("Silva"))
                .andExpect(jsonPath("$.content[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].firstName").value("Bob"))
                .andExpect(jsonPath("$.content[1].lastName").value("Souza"))
                .andExpect(jsonPath("$.content[1].email").value("bob@example.com"));
    }

    @Test
    @DisplayName("GET /api/v1/users -> 200 com metadados de paginação (number, size, totalElements, totalPages)")
    void getAll_ok_paged_metadata() throws Exception {
        Page<UserDTO> page = new PageImpl<>(List.of(), PageRequest.of(2, 50), 0);
        given(userService.findAllPaged(any(Pageable.class))).willReturn(page);

        mockMvc.perform(get(baseUrl)
                        .param("page", "2")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.number").value(2))
                .andExpect(jsonPath("$.size").value(50))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    @DisplayName("POST /api/v1/users -> 201 e Location apontando para o recurso criado")
    void create_created_withLocation() throws Exception {
        var created = userDto(99L, "Carol", "Lima", "carol@example.com");
        given(userService.insert(any(UserInsertDTO.class))).willReturn(created);

        String body = """
                {
                  "firstName": "Carol",
                  "lastName": "Lima",
                  "email": "carol@example.com",
                  "password": "Strong#Pass123"
                }
                """;

        mockMvc.perform(post(baseUrl)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(baseUrl + "/99")))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(99L))
                .andExpect(jsonPath("$.firstName").value("Carol"))
                .andExpect(jsonPath("$.lastName").value("Lima"))
                .andExpect(jsonPath("$.email").value("carol@example.com"));

        verify(userService).insert(any(UserInsertDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users -> 400 quando violar validação (payload vazio)")
    void create_badRequest_onValidationError() throws Exception {
        String invalidBody = "{}";

        mockMvc.perform(post(baseUrl)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());

        verify(userService, never()).insert(any(UserInsertDTO.class));
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} -> 200 e corpo atualizado")
    void update_ok() throws Exception {
        var updated = userDto(7L, "Dan", "Ramos", "dan@example.com");
        given(userService.update(eq(7L), any(UserUpdateDTO.class))).willReturn(updated);

        String body = """
                {
                  "firstName": "Dan",
                  "lastName": "Ramos",
                  "email": "dan@example.com"
                }
                """;

        mockMvc.perform(put(baseUrl + "/{id}", 7L)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.firstName").value("Dan"))
                .andExpect(jsonPath("$.lastName").value("Ramos"))
                .andExpect(jsonPath("$.email").value("dan@example.com"));

        verify(userService).update(eq(7L), any(UserUpdateDTO.class));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} -> 404 quando não encontrado")
    void findById_notFound() throws Exception {
        given(userService.findById(999L)).willThrow(new ResponseStatusException(NOT_FOUND, "User 999"));
        mockMvc.perform(get(baseUrl + "/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/users -> 409 quando e-mail já existe")
    void create_conflict_onDuplicateEmail() throws Exception {
        given(userService.insert(any(UserInsertDTO.class)))
                .willThrow(new ResponseStatusException(CONFLICT, "email already exists"));

        String body = """
                {
                  "firstName": "Carol",
                  "lastName": "Lima",
                  "email": "carol@example.com",
                  "password": "Strong#Pass123"
                }
                """;

        mockMvc.perform(post(baseUrl)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/v1/users -> 400 com payload inválido (violação @Valid)")
    void create_badRequest_validationDetails() throws Exception {
        String invalid = """
                 {\s
                 "firstName": "",\s
                 "lastName": "",\s
                 "email": "not-an-email",\s
                 "password": ""\s
                 }
                \s""";

        mockMvc.perform(post(baseUrl)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest());

        verify(userService, never()).insert(any(UserInsertDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users -> 415 quando Content-Type não é application/json")
    void create_unsupportedMediaType() throws Exception {
        mockMvc.perform(post(baseUrl)
                        .with(csrf())
                        .contentType("text/plain")
                        .content("not json"))
                .andExpect(status().isUnsupportedMediaType());

        verify(userService, never()).insert(any(UserInsertDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users -> 406 quando Accept não é suportado")
    void create_notAcceptable() throws Exception {
        String body = """
                {
                  "firstName": "Eve",
                  "lastName": "Nunes",
                  "email": "eve@example.com",
                  "password": "Strong#Pass123"
                }
                """;

        mockMvc.perform(post(baseUrl)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .header("Accept", "application/xml")
                        .content(body))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} -> 400 quando id não numérico (type mismatch)")
    void findById_badRequest_onTypeMismatch() throws Exception {
        mockMvc.perform(get(baseUrl + "/{id}", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/users -> Pageable com page=2,size=50,sort=lastName,asc é repassado ao service")
    void getAll_capturesPageable() throws Exception {
        Page<UserDTO> page = new PageImpl<>(List.of(), PageRequest.of(2, 50), 0);
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        given(userService.findAllPaged(any(Pageable.class))).willReturn(page);

        mockMvc.perform(get(baseUrl)
                        .param("page", "2")
                        .param("size", "50")
                        .param("sort", "lastName,asc"))
                .andExpect(status().isOk());

        verify(userService).findAllPaged(captor.capture());
        Pageable p = captor.getValue();
        assertNotNull(p);
        assertEquals(2, p.getPageNumber());
        assertEquals(50, p.getPageSize());
        assertNotNull(p.getSort().getOrderFor("lastName"));
        assertTrue(Objects.requireNonNull(p.getSort().getOrderFor("lastName")).isAscending());
    }

    @Test
    @DisplayName("GET /api/v1/users -> Pageable default quando sem parâmetros")
    void getAll_defaultPageable() throws Exception {
        given(userService.findAllPaged(any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get(baseUrl))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/users -> 400 quando JSON malformado (string literal)")
    void create_badRequest_onMalformedJson() throws Exception {
        mockMvc.perform(post(baseUrl)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("\"not-json\""))
                .andExpect(status().isBadRequest());

        verify(userService, never()).insert(any(UserInsertDTO.class));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} -> corpo contém 'roles' como array")
    void findById_containsRolesArray() throws Exception {
        var dto = userDto(10L, "Alice", "Silva", "alice@example.com");
        dto.getRoles().add(new RoleDTO(1L, "ROLE_ADMIN"));
        given(userService.findById(10L)).willReturn(dto);

        mockMvc.perform(get(baseUrl + "/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0].authority").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("GET /api/v1/users/me -> 401 quando não autenticado")
    void getMe_unauthenticated() throws Exception {
        mockMvc.perform(get(baseUrl + "/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/users/me -> 200 quando autenticado")
    void getMe_authenticated_ok() throws Exception {
        var me = userDto(123L, "Zoe", "Pereira", "zoe@example.com");
        given(userService.getMe()).willReturn(me);

        mockMvc.perform(get(baseUrl + "/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(123L))
                .andExpect(jsonPath("$.email").value("zoe@example.com"));
    }

    @Test
    @DisplayName("GET /users/{id} -> 200 usando alias de rota")
    void findById_ok_viaAlias() throws Exception {
        var dto = userDto(5L, "Ana", "Costa", "ana@example.com");
        given(userService.findById(5L)).willReturn(dto);

        mockMvc.perform(get("/users/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(5L));
    }

    @Test
    @DisplayName("POST /users -> 201 e Location sempre em /api/v1/users/{id}")
    void create_created_withLocation_viaAlias() throws Exception {
        var created = userDto(77L, "Rick", "Dias", "rick@example.com");
        given(userService.insert(any(UserInsertDTO.class))).willReturn(created);

        String body = """
                {
                   "firstName": "Rick",
                  "lastName": "Dias",
                  "email": "rick@example.com",
                  "password": "Abc#12345"
                }
                """;

        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/users/77")));
    }

    @Test
    @DisplayName("GET /api/v1/users -> 406 quando Accept não suportado")
    void getAll_notAcceptable() throws Exception {
        given(userService.findAllPaged(any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get(baseUrl)
                        .header("Accept", "application/xml"))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} -> 415 quando Content-Type não suportado")
    void update_unsupportedMediaType() throws Exception {
        mockMvc.perform(put(baseUrl + "/{id}", 10L)
                        .with(csrf())
                        .contentType("text/plain")
                        .content("not json"))
                .andExpect(status().isUnsupportedMediaType());

        verify(userService, never()).update(eq(10L), any(UserUpdateDTO.class));
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} -> 406 quando Accept não suportado")
    void update_notAcceptable() throws Exception {
        String body = """
                 {\s
                     "firstName": "New",\s
                     "lastName": "Name",\s
                     "email": "new@example.com"\s
                 }
                \s""";
        mockMvc.perform(put(baseUrl + "/{id}", 10L)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .header("Accept", "application/xml")
                        .content(body))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} -> 400 com payload inválido (violação @Valid)")
    void update_badRequest_validationDetails() throws Exception {
        String invalid = """
                 {\s
                 "firstName": "",\s
                 "lastName": "",\s
                 "email": "invalid"\s
                 }
                \s""";

        mockMvc.perform(put(baseUrl + "/{id}", 10L)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest());

        verify(userService, never()).update(eq(10L), any(UserUpdateDTO.class));
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} -> 404 quando usuário não existe (payload válido)")
    void update_notFound() throws Exception {
        given(userService.update(eq(1234L), any(UserUpdateDTO.class)))
                .willThrow(new ResponseStatusException(NOT_FOUND, "User 1234"));

        String body = """
                 {\s
                 "firstName": "Ana",\s
                 "lastName": "Barros",\s
                 "email": "a.b@example.com"\s
                 }
                \s""";

        mockMvc.perform(put(baseUrl + "/{id}", 1234L)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());

        verify(userService).update(eq(1234L), any(UserUpdateDTO.class));
    }


    @Test
    @DisplayName("PUT /api/v1/users/{id} -> 409 quando e-mail conflita com outro usuário (payload válido)")
    void update_conflict() throws Exception {
        given(userService.update(eq(20L), any(UserUpdateDTO.class)))
                .willThrow(new ResponseStatusException(CONFLICT, "email already exists"));

        String body = """
                 {\s
                 "firstName": "Ana",\s
                 "lastName": "Barros",\s
                 "email": "duplicated@example.com"
                 }
                \s""";

        mockMvc.perform(put(baseUrl + "/{id}", 20L)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());

        verify(userService).update(eq(20L), any(UserUpdateDTO.class));
    }
}
