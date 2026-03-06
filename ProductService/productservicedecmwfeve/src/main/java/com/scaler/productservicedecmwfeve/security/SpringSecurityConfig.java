package com.scaler.productservicedecmwfeve.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Token validation for /products is done in the controller via AuthenticationCommons
 * (calling UserService /users/validate/{token}). We do NOT use OAuth2 resource server
 * here so that Spring Security does not try to decode the JWT (which would require
 * the same secret/keys as UserService and can cause "Another algorithm expected" 401).
 */
@Configuration
public class SpringSecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/products", "/products/**"));
        return http.build();
    }
}
