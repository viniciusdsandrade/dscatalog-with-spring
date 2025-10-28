package com.restful.dscatalog.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority; // necessário para authorities do jwt

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt; // postprocessor de JWT para MockMvc

import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Sql(
        scripts = {
                "/sql/01-clean.sql",
                "/sql/02-seed-categories.sql",
                "/sql/03-seed-products.sql",
                "/sql/04-seed-relations.sql"
        },
        executionPhase = BEFORE_TEST_METHOD
)
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/products/{id} deve retornar 200 e o produto")
    void getById_ok() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", 1L).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Smartphone XYZ"))
                .andExpect(jsonPath("$.categories", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/v1/products com paginação deve retornar 200 + headers de paginação")
    void findAll_withPaginationHeaders_ok() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .param("size", "2")
                        .param("page", "0")
                        .param("sort", "id,asc")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", notNullValue()))
                .andExpect(header().string("X-Page-Number", "0"))
                .andExpect(header().string("X-Page-Size", "2"))
                .andExpect(header().string("Link", containsString("rel=\"first\"")))
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("POST /api/v1/products/by-names deve criar (201), retornar Location e o corpo")
    void createByNames_created() throws Exception {
        Map<String, Object> payload = Map.of(
                "name", "Mouse Gamer",
                "description", "Mouse com 6 botões e DPI ajustável",
                "price", 199.90,
                "imgUrl", "https://img.example/mouse.png",
                "categoryNames", List.of("Informática", "Acessórios")
        );

        mockMvc.perform(post("/api/v1/products/by-names")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/products/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Mouse Gamer"))
                    .andExpect(jsonPath("$.categories", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("PUT /api/v1/products/{id} deve atualizar (200) e refletir os dados")
    void update_ok() throws Exception {
        Map<String, Object> payload = Map.of(
                "name", "PC Gamer Atualizado",
                "description", "GPU melhor",
                "price", 6500.00,
                "imgUrl", "https://img.example/pc2.png",
                "categoryIds", List.of(1)
        );

        mockMvc.perform(put("/api/v1/products/{id}", 1L)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("PC Gamer Atualizado"));
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} deve remover (200) e retornar DTO do removido")
    void delete_ok() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", 1L)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Smartphone XYZ"));
    }
}
