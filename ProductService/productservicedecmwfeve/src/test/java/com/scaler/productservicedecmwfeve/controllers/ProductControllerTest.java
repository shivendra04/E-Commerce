package com.scaler.productservicedecmwfeve.controllers;

import com.scaler.productservicedecmwfeve.commons.AuthenticationCommons;
import com.scaler.productservicedecmwfeve.dtos.UserDto;
import com.scaler.productservicedecmwfeve.dtos.ProductDto;
import com.scaler.productservicedecmwfeve.exceptions.ProductNotExistsException;
import com.scaler.productservicedecmwfeve.services.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean(name = "selfProductService")
    private ProductService productService;

    @MockBean
    private AuthenticationCommons authenticationCommons;

    @MockBean
    private RestTemplate restTemplate;

    private static final String VALID_TOKEN = "Bearer valid-jwt-token";

    @Test
    void getAllProducts_WhenTokenValid_ReturnsOk() throws Exception {
        UserDto userDto = new UserDto();
        when(authenticationCommons.validateToken(any())).thenReturn(userDto);

        List<ProductDto> products = new ArrayList<>();
        ProductDto p = new ProductDto();
        p.setTitle("Test Product");
        products.add(p);
        when(productService.getAllProducts(anyInt(), anyInt(), anyString(), anyString(),
                any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(products));

        mockMvc.perform(get("/products")
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Test Product"));
    }

    @Test
    void getAllProducts_WhenTokenInvalid_ReturnsUnauthorized() throws Exception {
        when(authenticationCommons.validateToken(any())).thenReturn(null);

        mockMvc.perform(get("/products")
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getSingleProduct_WhenProductExists_ReturnsOk() throws Exception {
        UserDto userDto = new UserDto();
        when(authenticationCommons.validateToken(any())).thenReturn(userDto);

        ProductDto product = new ProductDto();
        product.setId(1L);
        product.setTitle("iPhone");
        when(productService.getSingleProduct(1L)).thenReturn(product);

        mockMvc.perform(get("/products/1")
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("iPhone"));
    }

    @Test
    void getSingleProduct_WhenProductNotExists_ReturnsNotFound() throws Exception {
        UserDto userDto = new UserDto();
        when(authenticationCommons.validateToken(any())).thenReturn(userDto);
        when(productService.getSingleProduct(999L)).thenThrow(new ProductNotExistsException("Product with id: 999 doesn't exist."));

        mockMvc.perform(get("/products/999")
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void addNewProduct_WhenTokenValid_ReturnsCreated() throws Exception {
        UserDto userDto = new UserDto();
        when(authenticationCommons.validateToken(any())).thenReturn(userDto);

        ProductDto product = new ProductDto();
        product.setTitle("New Product");
        product.setPrice(99.99);
        when(productService.addNewProduct(any(com.scaler.productservicedecmwfeve.models.Product.class))).thenReturn(product);

        mockMvc.perform(post("/products")
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New Product\",\"price\":99.99}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Product"));
    }

    @Test
    void updateProduct_WhenProductExists_ReturnsOk() throws Exception {
        UserDto userDto = new UserDto();
        when(authenticationCommons.validateToken(any())).thenReturn(userDto);

        ProductDto product = new ProductDto();
        product.setId(1L);
        product.setTitle("Updated");
        when(productService.updateProduct(anyLong(), any(com.scaler.productservicedecmwfeve.models.Product.class))).thenReturn(product);

        mockMvc.perform(patch("/products/1")
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteProduct_WhenProductExists_ReturnsNoContent() throws Exception {
        UserDto userDto = new UserDto();
        when(authenticationCommons.validateToken(any())).thenReturn(userDto);
        when(productService.deleteProduct(1L)).thenReturn(true);

        mockMvc.perform(delete("/products/1")
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAllProducts_WhenNoAuthorizationHeader_ReturnsError() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().is5xxServerError());
    }
}
