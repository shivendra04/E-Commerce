package com.scaler.userservicemwfeve.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaler.userservicemwfeve.dtos.LoginRequestDto;
import com.scaler.userservicemwfeve.dtos.LoginResponseDto;
import com.scaler.userservicemwfeve.dtos.SignUpRequestDto;
import com.scaler.userservicemwfeve.models.Role;
import com.scaler.userservicemwfeve.models.User;
import com.scaler.userservicemwfeve.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void signUp_returnsCreatedAndUserDto() throws Exception {
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");
        User user = new User();
        user.setId(1L);
        user.setName("John");
        user.setEmail("john@test.com");
        user.setRoles(List.of(role));

        SignUpRequestDto dto = new SignUpRequestDto();
        dto.setName("John");
        dto.setEmail("john@test.com");
        dto.setPassword("pass123");

        when(userService.signUp(eq("John"), eq("john@test.com"), eq("pass123"), any()))
                .thenReturn(user);

        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@test.com"));
    }

    @Test
    void login_returnsOkAndJwtToken() throws Exception {
        LoginResponseDto response = new LoginResponseDto("jwt-token-123");

        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("test@example.com");
        dto.setPassword("password");

        when(userService.login("test@example.com", "password")).thenReturn(response);

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"));
    }

    @Test
    void validateToken_returnsOkAndUserDto() throws Exception {
        Role role = new Role();
        role.setName("USER");
        User user = new User();
        user.setId(1L);
        user.setName("Test");
        user.setEmail("test@example.com");
        user.setRoles(List.of(role));

        when(userService.validateToken("valid-jwt-token")).thenReturn(user);

        mockMvc.perform(post("/users/validate/valid-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test"));
    }
}
