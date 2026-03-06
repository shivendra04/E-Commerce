package com.scaler.userservicemwfeve.controllers;

import com.scaler.userservicemwfeve.dtos.LoginRequestDto;
import com.scaler.userservicemwfeve.dtos.LoginResponseDto;
import com.scaler.userservicemwfeve.dtos.SignUpRequestDto;
import com.scaler.userservicemwfeve.dtos.UserDto;
import com.scaler.userservicemwfeve.models.User;
import com.scaler.userservicemwfeve.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signUp(@RequestBody SignUpRequestDto request) {
        User user = userService.signUp(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getRoles()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
        LoginResponseDto response = userService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate/{token}")
    public ResponseEntity<UserDto> validateToken(@PathVariable("token") @NonNull String token) {
        User user = userService.validateToken(token);
        return ResponseEntity.ok(UserDto.from(user));
    }
}
