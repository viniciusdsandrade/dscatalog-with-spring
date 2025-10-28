package com.restful.dscatalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restful.dscatalog.dto.categoria.CategoryDetailsDTO;
import com.restful.dscatalog.dto.categoria.CategoryPostDTO;
import com.restful.dscatalog.entity.Category;
import com.restful.dscatalog.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    private String baseUrl;

    @BeforeEach
    void setup() {
        this.baseUrl = "/api/v1/categories";
    }

    private static Category withId(Category category, long id) {
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }

    private static Category newCategory(String name) {
        return new Category(name);
    }

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} -> 200 e corpo com id e name")
    void getById_ok() throws Exception {
        Category category = withId(newCategory("Eletrônicos"), 10L);
        given(categoryService.findById(10L)).willReturn(category);

        mockMvc.perform(get(baseUrl + "/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("Eletrônicos"));
    }

    @Test
    @DisplayName("GET /api/v1/categories -> 200, paginação e cabeçalhos de paginação")
    void getAll_ok_withPaginationHeaders() throws Exception {
        var categoryDetailsDTO = new CategoryDetailsDTO(1L, "Informática");
        var page = new PageImpl<>(List.of(categoryDetailsDTO), PageRequest.of(0, 5), 1);
        given(categoryService.listAll(any(Pageable.class))).willReturn(page);

        mockMvc.perform(get(baseUrl)
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(header().string("X-Page-Number", "0"))
                .andExpect(header().string("X-Page-Size", "5"))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Informática"));
    }

    @Test
    @DisplayName("POST /api/v1/categories -> 201 e Location apontando para o recurso criado")
    void create_created_withLocation() throws Exception {
        Category saved = withId(newCategory("Acessórios"), 99L);
        given(categoryService.create(any(CategoryPostDTO.class))).willReturn(saved);

        String body = json(new CategoryPostDTO("Acessórios"));

        mockMvc.perform(post(baseUrl)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(baseUrl + "/99")))
                .andExpect(jsonPath("$.id").value(99L))
                .andExpect(jsonPath("$.name").value("Acessórios"));

        verify(categoryService).create(any(CategoryPostDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/categories -> 400 quando violar validação (name muito curto)")
    void create_badRequest_onValidationError() throws Exception {
        String invalidBody = """
                  { "name": "ab" }
                """;

        mockMvc.perform(post(baseUrl)
                        .contentType(APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).create(any(CategoryPostDTO.class));
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} -> 200 e corpo atualizado")
    void update_ok() throws Exception {
        var updated = new CategoryDetailsDTO(7L, "Livros");
        given(categoryService.update(eq(7L), any(CategoryPostDTO.class))).willReturn(updated);

        String body = json(new CategoryPostDTO("Livros"));

        mockMvc.perform(put(baseUrl + "/{id}", 7L)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.name").value("Livros"));

        verify(categoryService).update(eq(7L), any(CategoryPostDTO.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} -> 200 e retorna DTO do removido")
    void delete_ok() throws Exception {
        var categoryDetailsDTO = new CategoryDetailsDTO(33L, "Excluir");
        given(categoryService.delete(33L)).willReturn(categoryDetailsDTO);

        mockMvc.perform(delete(baseUrl + "/{id}", 33L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(33L))
                .andExpect(jsonPath("$.name").value("Excluir"));

        verify(categoryService).delete(33L);
    }
}
