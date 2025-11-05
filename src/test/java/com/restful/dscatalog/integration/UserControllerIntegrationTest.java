package com.restful.dscatalog.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /users -> 200 com página vazia (content.length=0)")
    @Sql(statements = {
            "DELETE FROM tb_user_role",
            "DELETE FROM tb_user",
            "DELETE FROM tb_role"
    }, executionPhase = BEFORE_TEST_METHOD)
    void getAllUsers_empty() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @DisplayName("GET /users?size=2 -> 200 com itens e paginação correta")
    @Sql(statements = {
            "DELETE FROM tb_user_role",
            "DELETE FROM tb_user",
            "DELETE FROM tb_role",
            "INSERT INTO tb_user(id, first_name, last_name, email, password) VALUES (1,'Ana','Silva','ana@example.com','{noop}123')",
            "INSERT INTO tb_user(id, first_name, last_name, email, password) VALUES (2,'Bruno','Souza','bruno@example.com','{noop}123')"
    }, executionPhase = BEFORE_TEST_METHOD)
    void getAllUsers_withContent() throws Exception {
        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].firstName").value("Ana"))
                .andExpect(jsonPath("$.content[0].email").value("ana@example.com"))
                .andExpect(jsonPath("$.content[1].firstName").value("Bruno"))
                .andExpect(jsonPath("$.content[1].email").value("bruno@example.com"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @DisplayName("GET /users/{id} -> 200 com id, firstName, email")
    @Sql(statements = {
            "DELETE FROM tb_user_role",
            "DELETE FROM tb_user",
            "DELETE FROM tb_role",
            "INSERT INTO tb_user(id, first_name, last_name, email, password) VALUES (5,'Carol','Brown','carol@example.com','{noop}123')"
    }, executionPhase = BEFORE_TEST_METHOD)
    void findById_ok() throws Exception {
        mockMvc.perform(get("/users/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.firstName").value("Carol"))
                .andExpect(jsonPath("$.email").value("carol@example.com"));
    }

    @Test
    @WithMockUser(username = "me@example.com", roles = {"USER"})
    @DisplayName("GET /users/me -> 200 quando autenticado")
    @Sql(statements = {
            "DELETE FROM tb_user_role",
            "DELETE FROM tb_user",
            "DELETE FROM tb_role",
            "INSERT INTO tb_user(id, first_name, last_name, email, password) VALUES (10,'Me','User','me@example.com','{noop}123')"
    }, executionPhase = BEFORE_TEST_METHOD)
    void getMe_ok_authenticated() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("me@example.com"))
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("GET /users/me -> 401 quando não autenticado")
    void getMe_unauthorized() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /users -> 201 Created, Location e corpo com id/firstName/email")
    @Sql(statements = {
            "DELETE FROM tb_user_role",
            "DELETE FROM tb_user",
            "DELETE FROM tb_role",
            "INSERT INTO tb_role(id, authority) VALUES (1, 'ROLE_ADMIN')",
            "INSERT INTO tb_role(id, authority) VALUES (2, 'ROLE_USER')"
    }, executionPhase = BEFORE_TEST_METHOD)
    void createUser_created() throws Exception {
        String requestJson = """
                {
                  "firstName": "Alex",
                  "lastName": "Blue",
                  "email": "alex.blue@example.com",
                  "password": "Str0ng_P@ss!"
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/users/\\d+$")))
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("Alex"))
                .andExpect(jsonPath("$.lastName").value("Blue"))
                .andExpect(jsonPath("$.email").value("alex.blue@example.com"));
    }

    @Test
    @DisplayName("POST /users -> 400 quando validação falha")
    @Sql(statements = {
            "DELETE FROM tb_user_role",
            "DELETE FROM tb_user",
            "DELETE FROM tb_role"
    }, executionPhase = BEFORE_TEST_METHOD)
    void createUser_badRequest_validation() throws Exception {
        String invalidJson = """
                {
                  "firstName": "A",
                  "lastName": "",
                  "email": "not-an-email",
                  "password": "123"
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /users/{id} inexistente -> 404")
    @Sql(statements = {
            "DELETE FROM tb_user_role",
            "DELETE FROM tb_user",
            "DELETE FROM tb_role"
    }, executionPhase = BEFORE_TEST_METHOD)
    void findById_notFound() throws Exception {
        mockMvc.perform(get("/users/{id}", 9999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /users -> 415 quando Content-Type inválido")
    @Sql(statements = {
            "DELETE FROM tb_user_role",
            "DELETE FROM tb_user",
            "DELETE FROM tb_role"
    }, executionPhase = BEFORE_TEST_METHOD)
    void createUser_unsupportedMediaType() throws Exception {
        String body = """
                {"firstName":"X","lastName":"Y","email":"x@example.com","password":"Str0ng_P@ss!"}
                """;
        mockMvc.perform(post("/users")
                        .contentType(TEXT_PLAIN)
                        .content(body))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("POST /users -> 406 quando Accept não suportado (XML)")
    @Sql(statements = {
            "DELETE FROM tb_user_role",
            "DELETE FROM tb_user",
            "DELETE FROM tb_role"
    }, executionPhase = BEFORE_TEST_METHOD)
    void createUser_notAcceptable_whenXmlRequested() throws Exception {
        String body = """
                {"firstName":"X","lastName":"Y","email":"x@example.com","password":"Str0ng_P@ss!"}
                """;
        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_XML)
                        .content(body))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    @DisplayName("POST /users -> 400 quando JSON é malformado")
    @Sql(statements = {
            "DELETE FROM tb_user_role",
            "DELETE FROM tb_user",
            "DELETE FROM tb_role"
    }, executionPhase = BEFORE_TEST_METHOD)
    void createUser_badRequest_malformedJson() throws Exception {
        String malformed = "{ \"firstName\": \"X\", ";
        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(malformed))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /users -> 409 quando email duplicado")
    @Sql(statements = {
            "DELETE FROM tb_user_role",
            "DELETE FROM tb_user",
            "DELETE FROM tb_role",
            "INSERT INTO tb_user(id, first_name, last_name, email, password) VALUES (1,'Ana','Silva','ana@example.com','{noop}123')"
    }, executionPhase = BEFORE_TEST_METHOD)
    void createUser_conflict_duplicateEmail() throws Exception {
        String body = """
                {
                  "firstName": "Outro",
                  "lastName": "Usuario",
                  "email": "ana@example.com",
                  "password": "Str0ng_P@ss!"
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST persiste e GET /users/{id} retorna dados essenciais (round-trip)")
    @Sql(statements = {
            "DELETE FROM tb_user_role",
            "DELETE FROM tb_user",
            "DELETE FROM tb_role",
            "INSERT INTO tb_role(id, authority) VALUES (1,'ROLE_ADMIN')",
            "INSERT INTO tb_role(id, authority) VALUES (2,'ROLE_USER')"
    }, executionPhase = BEFORE_TEST_METHOD)
    void createThenGetById_roundTrip() throws Exception {
        String body = """
                {
                  "firstName": "Round",
                  "lastName": "Trip",
                  "email": "round@example.com",
                  "password": "Str0ng_P@ss!"
                }
                """;

        var mvcResult = mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/users/\\d+$")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("Round"))
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(response);
        long id = root.path("id").asLong();

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.firstName").value("Round"))
                .andExpect(jsonPath("$.email").value("round@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("GET /users ordenado por firstName desc -> 'Bruno' antes de 'Ana'")
    @Sql(statements = {
            "DELETE FROM tb_user_role",
            "DELETE FROM tb_user",
            "DELETE FROM tb_role",
            "INSERT INTO tb_user(id, first_name, last_name, email, password) VALUES (1,'Ana','Silva','ana@example.com','{noop}123')",
            "INSERT INTO tb_user(id, first_name, last_name, email, password) VALUES (2,'Bruno','Souza','bruno@example.com','{noop}123')"
    }, executionPhase = BEFORE_TEST_METHOD)
    void getAllUsers_sortedByFirstName_desc() throws Exception {
        mockMvc.perform(get("/users")
                        .param("sort", "firstName,desc")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].firstName").value("Bruno"))
                .andExpect(jsonPath("$.content[1].firstName").value("Ana"));
    }

    @ParameterizedTest
    @MethodSource("invalidPayloads")
    @DisplayName("POST /users -> 400 (parametrizado por campo inválido)")
    @Sql(statements = {
            "DELETE FROM tb_user_role",
            "DELETE FROM tb_user",
            "DELETE FROM tb_role"
    }, executionPhase = BEFORE_TEST_METHOD)
    void createUser_badRequest_fieldErrors(String json, String expectedField) throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$[*].field", hasItem(expectedField)));
    }

    static Stream<Arguments> invalidPayloads() {
        return Stream.of(
                // firstName vazio
                arguments("""
                        {
                          "firstName": "",
                          "lastName": "Blue",
                          "email": "novo@example.com",
                          "password": "Str0ng_P@ss!"
                        }
                        """, "firstName"),
                // firstName curto
                arguments("""
                        {
                          "firstName": "A",
                          "lastName": "Blue",
                          "email": "novo@example.com",
                          "password": "Str0ng_P@ss!"
                        }
                        """, "firstName"),
                // lastName vazio
                arguments("""
                        {
                          "firstName": "Novo",
                          "lastName": "",
                          "email": "novo@example.com",
                          "password": "Str0ng_P@ss!"
                        }
                        """, "lastName"),
                // email inválido
                arguments("""
                        {
                          "firstName": "Novo",
                          "lastName": "Usuario",
                          "email": "not-an-email",
                          "password": "Str0ng_P@ss!"
                        }
                        """, "email"),
                // password fraca
                arguments("""
                        {
                          "firstName": "Novo",
                          "lastName": "Usuario",
                          "email": "novo@example.com",
                          "password": "123"
                        }
                        """, "password")
        );
    }
}
