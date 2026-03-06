package com.scaler.userservicemwfeve.services;

import com.scaler.userservicemwfeve.dtos.LoginResponseDto;
import com.scaler.userservicemwfeve.exceptions.PasswordNotMatchingException;
import com.scaler.userservicemwfeve.exceptions.TokenNotFoundException;
import com.scaler.userservicemwfeve.exceptions.UserNotFoundException;
import com.scaler.userservicemwfeve.models.Role;
import com.scaler.userservicemwfeve.models.User;
import com.scaler.userservicemwfeve.repositories.RoleRepository;
import com.scaler.userservicemwfeve.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import io.jsonwebtoken.Claims;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    private User user;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setId(1L);
        userRole.setName("USER");

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setHashedPassword("hashed");
        user.setRoles(List.of(userRole));
    }

    @Test
    void signUp_withEmptyRoles_assignsDefaultUserRole() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });

        User result = userService.signUp("John", "john@test.com", "pass123", null);

        assertNotNull(result);
        assertNotNull(result.getRoles());
        assertTrue(result.getRoles().stream().anyMatch(r -> "USER".equals(r.getName())));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signUp_withRoles_assignsRequestedRoles() {
        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName("ADMIN");
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });

        User result = userService.signUp("Admin", "admin@test.com", "pass", List.of("ADMIN"));

        assertNotNull(result);
        assertTrue(result.getRoles().stream().anyMatch(r -> "ADMIN".equals(r.getName())));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_success_returnsJwtOnly_noDbPersistence() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches("password", "hashed")).thenReturn(true);
        when(jwtService.createToken(user)).thenReturn("jwt-token-string");

        LoginResponseDto result = userService.login("test@example.com", "password");

        assertNotNull(result);
        assertEquals("jwt-token-string", result.getToken());
        verify(jwtService).createToken(user);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void login_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                userService.login("unknown@example.com", "password"));
        verify(jwtService, never()).createToken(any());
    }

    @Test
    void login_wrongPassword_throwsPasswordNotMatchingException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(PasswordNotMatchingException.class, () ->
                userService.login("test@example.com", "wrong"));
        verify(jwtService, never()).createToken(any());
    }

    @Test
    void validateToken_success_returnsUser_fromJwtAndDb() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("1");
        when(jwtService.parseAndValidate("valid-jwt")).thenReturn(claims);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.validateToken("valid-jwt");

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        verify(jwtService).parseAndValidate("valid-jwt");
        verify(userRepository).findById(1L);
    }

    @Test
    void validateToken_userNotFoundForId_throwsTokenNotFoundException() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("999");
        when(jwtService.parseAndValidate("valid-jwt")).thenReturn(claims);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(TokenNotFoundException.class, () -> userService.validateToken("valid-jwt"));
        verify(userRepository).findById(999L);
    }
}
