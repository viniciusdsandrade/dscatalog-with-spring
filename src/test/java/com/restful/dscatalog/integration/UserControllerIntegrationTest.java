package com.restful.dscatalog.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static java.lang.System.currentTimeMillis;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(statements = {
        "DELETE FROM tb_user_role",
        "DELETE FROM tb_role",
        "DELETE FROM tb_user",

        "INSERT INTO tb_role(id, authority) VALUES (1,'ROLE_ADMIN'), (2,'ROLE_USER')",

        "INSERT INTO tb_user(id, email, first_name, last_name, password) VALUES (1,'maria@gmail.com','Maria','Brown','{noop}123')",
        "INSERT INTO tb_user(id, email, first_name, last_name, password) VALUES (2,'alex@gmail.com','Alex','Green','{noop}123')",

        "INSERT INTO tb_user_role(user_id, role_id) VALUES (1,1)",
        "INSERT INTO tb_user_role(user_id, role_id) VALUES (1,2)",
        "INSERT INTO tb_user_role(user_id, role_id) VALUES (2,2)"
}, executionPhase = BEFORE_TEST_METHOD)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/users -> 200 e Page<UserDTO> com conteúdo")
    void getAllUsers_ok() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "id,asc")
                        .with(jwt().jwt(j -> j.subject("maria@gmail.com"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} -> 200 e corpo correto")
    void findById_ok() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", 1L)
                        .with(jwt().jwt(j -> j.subject("maria@gmail.com"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email", not(emptyOrNullString())));
    }

    @Sql("/db/testdata/users_me_seed.sql")
    @Test
    @DisplayName("GET /api/v1/users/me -> 200 quando autenticado com claim 'sub'")
    void getMe_ok_authenticated() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .with(jwt().jwt(j -> j.subject("maria@gmail.com"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("maria@gmail.com"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/users/me -> 401 quando sem JWT")
    void getMe_unauthorized_whenNoJwt() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/users -> 201 cria usuário e retorna Location + corpo")
    @Sql(statements = {
            "DELETE FROM tb_user_role",
            "DELETE FROM tb_user",
            "DELETE FROM tb_role",
            "INSERT INTO tb_role(id, authority) VALUES (1, 'ROLE_ADMIN')",
            "INSERT INTO tb_role(id, authority) VALUES (2, 'ROLE_USER')"
    }, executionPhase = BEFORE_TEST_METHOD)
    void createUser_created() throws Exception {
        var uniqueEmail = "alex.blue+" + currentTimeMillis() + "@example.com";
        var payload = Map.of(
                "firstName", "Alex",
                "lastName", "Blue",
                "email", uniqueEmail,
                "password", "P@ssw0rd123!"
        );

        mockMvc.perform(post("/api/v1/users")
                        .with(jwt().jwt(jwt -> jwt
                                        .subject("admin@example.com")
                                        .claim("username", "admin@example.com"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/users/")))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(uniqueEmail))
                .andExpect(jsonPath("$.firstName").value("Alex"))
                .andExpect(jsonPath("$.lastName").value("Blue"));
    }

    @Test
    @DisplayName("POST /api/v1/users -> 400 quando payload inválido (email malformado)")
    void createUser_badRequest_onInvalidPayload() throws Exception {
        var bad = Map.of(
                "firstName", "A",
                "lastName", "B",
                "email", "not-an-email",
                "password", "weak"
        );

        mockMvc.perform(post("/api/v1/users")
                        .with(jwt()
                                .jwt(jwt -> jwt.subject("maria@gmail.com"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} -> 200 quando o 'requester' é o dono")
    void updateUser_ok_whenOwner() throws Exception {
        var update = Map.of(
                "firstName", "Maria",
                "lastName", "Updated",
                "email", "maria.updated@example.com"
        );

        mockMvc.perform(put("/api/v1/users/{id}", 1L)
                        .with(jwt().jwt(jwt -> jwt
                                        .subject("maria@gmail.com")
                                        .claim("username", "maria@gmail.com"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("maria.updated@example.com"))
                .andExpect(jsonPath("$.lastName").value("Updated"));
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} -> 403 quando NÃO é o dono")
    void updateUser_forbidden_whenNotOwner() throws Exception {
        var update = Map.of(
                "firstName", "Intruder",
                "lastName", "ShouldFail",
                "email", "intruder@example.com"
        );

        mockMvc.perform(put("/api/v1/users/{id}", 1L)
                        .with(jwt().jwt(jwt -> jwt
                                        .subject("evil@attacker.com")
                                        .claim("username", "evil@attacker.com"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isForbidden());
    }
}