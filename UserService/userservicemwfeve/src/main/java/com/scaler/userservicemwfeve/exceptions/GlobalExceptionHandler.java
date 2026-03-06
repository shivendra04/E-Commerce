package com.scaler.userservicemwfeve.exceptions;

import com.scaler.userservicemwfeve.dtos.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(PasswordNotMatchingException.class)
    public ResponseEntity<ErrorResponseDto> handlePasswordNotMatching(PasswordNotMatchingException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleTokenNotFound(TokenNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponseDto> handleTokenExpired(TokenExpiredException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDto(ex.getMessage()));
    }
}
