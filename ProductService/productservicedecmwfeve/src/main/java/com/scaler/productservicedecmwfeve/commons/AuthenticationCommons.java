package com.scaler.productservicedecmwfeve.commons;

import com.scaler.productservicedecmwfeve.dtos.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthenticationCommons {
    private final RestTemplate restTemplate;

    @Value("${USER_SERVICE_URL}")
    private String userServiceUrl;

    public AuthenticationCommons(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public UserDto validateToken(String token) {
        String url = userServiceUrl + "/users/validate/" + token;
        ResponseEntity<UserDto> userDtoResponse = restTemplate.postForEntity(
                url,
                null,
                UserDto.class
        );

        if (userDtoResponse.getBody() == null) {
            return null;
        }

        return userDtoResponse.getBody();
    }
}
