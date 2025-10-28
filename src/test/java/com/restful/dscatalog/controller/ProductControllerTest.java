package com.restful.dscatalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restful.dscatalog.dto.product.ProductDetailsDTO;
import com.restful.dscatalog.dto.product.ProductPostDTO;
import com.restful.dscatalog.entity.Product;
import com.restful.dscatalog.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    private String baseUrl;
    private LocalDateTime now;

    @BeforeEach
    void setup() {
        this.baseUrl = "/api/v1/products";
        this.now = LocalDateTime.of(2025, 10, 15, 15, 15, 15);
    }

    private static Product withId(Product product, long id) {
        setField(product, "id", id);
        return product;
    }

    private Product newProduct(String name, double price) {
        return new Product(
                name,
                name + " desc",
                BigDecimal.valueOf(price),
                now
        );
    }

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} -> 200 e corpo com id e name")
    void getById_returnsOk() throws Exception {
        Product product = withId(newProduct("Notebook", 5499.90), 1L);
        given(productService.findById(1L)).willReturn(product);

        mockMvc.perform(get(baseUrl + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Notebook")));
    }

    @Test
    @DisplayName("GET /api/v1/products -> 200, paginação em headers e conteúdo em DTO")
    void getAll_returnsOkWithHeaders() throws Exception {
        Product product = withId(newProduct("P1", 1.0), 10L);
        var productDetailsDTO = new ProductDetailsDTO(product);
        var page = new PageImpl<>(
                List.of(productDetailsDTO),
                PageRequest.of(0, 5),
                1
        );
        given(productService.listAll(any())).willReturn(page);

        mockMvc.perform(get(baseUrl))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(header().string("X-Page-Number", "0"))
                .andExpect(header().string("X-Page-Size", "5"))
                .andExpect(jsonPath("$.content[0].id", is(10)))
                .andExpect(jsonPath("$.content[0].name", is("P1")));
    }

    @Test
    @DisplayName("POST /api/v1/products -> 201, Location e corpo com DTO")
    void create_returnsCreated() throws Exception {
        var productPostDTO = new ProductPostDTO(
                "Novo Produto",
                "desc",
                99.9,
                "http://img",
                now,
                List.of()
        );

        Product saved = withId(newProduct("Novo Produto", 99.9), 100L);
        given(productService.create(any(ProductPostDTO.class))).willReturn(saved);

        mockMvc.perform(post(baseUrl)
                        .contentType(APPLICATION_JSON)
                        .content(json(productPostDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/products/100")))
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.name", is("Novo Produto")));
    }

    @Test
    @DisplayName("POST /api/v1/products -> 400 quando validação falha (ex.: name ausente)")
    void create_returnsBadRequest_onValidationError() throws Exception {
        String invalidJson = """
                    {"price":10.0,"imgUrl":"http://img","date":"2024-01-01T10:00:00"}
                """;

        mockMvc.perform(post(baseUrl)
                        .contentType(APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(productService, never()).create(any());
    }

    @Test
    @DisplayName("PUT /api/v1/products/{id} -> 200 com DTO atualizado")
    void update_returnsOk() throws Exception {
        var req = new ProductPostDTO("Editado", "d", 123.0, null, now, List.of());

        Product edited = withId(newProduct("Editado", 123.0), 5L);
        var productDetailsDTO = new ProductDetailsDTO(edited);

        given(productService.update(eq(5L), any(ProductPostDTO.class))).willReturn(productDetailsDTO);

        mockMvc.perform(put(baseUrl + "/{id}", 5L)
                        .contentType(APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.name", is("Editado")));
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} -> 200 com DTO do removido")
    void delete_returnsOk() throws Exception {
        Product removed = withId(newProduct("ToDel", 5.0), 7L);
        var productDetailsDTO = new ProductDetailsDTO(removed);
        given(productService.delete(7L)).willReturn(productDetailsDTO);

        mockMvc.perform(delete(baseUrl + "/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(7)))
                .andExpect(jsonPath("$.name", is("ToDel")));
    }
}
