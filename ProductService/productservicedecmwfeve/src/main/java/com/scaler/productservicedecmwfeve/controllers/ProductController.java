package com.scaler.productservicedecmwfeve.controllers;

import com.scaler.productservicedecmwfeve.commons.AuthenticationCommons;
import com.scaler.productservicedecmwfeve.dtos.ProductDto;
import com.scaler.productservicedecmwfeve.dtos.UserDto;
import com.scaler.productservicedecmwfeve.exceptions.ProductNotExistsException;
import com.scaler.productservicedecmwfeve.exceptions.UserNotFoundException;
import com.scaler.productservicedecmwfeve.models.Product;
import com.scaler.productservicedecmwfeve.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    private final RestTemplate restTemplate;
    private final AuthenticationCommons authenticationCommons;

    @Autowired
    public ProductController(@Qualifier("selfProductService") ProductService productService,
                             RestTemplate restTemplate,
                             AuthenticationCommons authenticationCommons) {
        this.productService = productService;
        this.restTemplate = restTemplate;
        this.authenticationCommons = authenticationCommons;
    }

    private UserDto validateUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UserNotFoundException("Missing or invalid Authorization header.");
        }
        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new UserNotFoundException("Token is empty.");
        }
        UserDto userDto = authenticationCommons.validateToken(token);
        if (userDto == null) {
            throw new UserNotFoundException("Invalid or expired token. User not found.");
        }
        return userDto;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestHeader(value = "Authorization", required = true) String authHeader,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice)
    {
        validateUser(authHeader);
        Page<ProductDto> page = productService.getAllProducts(
                pageNo, pageSize, sortBy, sortDir, name, category, minPrice, maxPrice);

        Map<String, Object> response = new HashMap<>();
        response.put("content", page.getContent());
        response.put("pageNo", page.getNumber());
        response.put("pageSize", page.getSize());
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("sort", page.getSort().toString());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getSingleProduct(
            @RequestHeader(value = "Authorization", required = true) String authHeader,
            @PathVariable("id") Long id) throws ProductNotExistsException {
        validateUser(authHeader);
        ProductDto product = productService.getSingleProduct(id);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ProductDto> addNewProduct(
            @RequestHeader(value = "Authorization", required = true) String authHeader,
            @RequestBody Product product) {
        validateUser(authHeader);
        ProductDto saved = productService.addNewProduct(product);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @RequestHeader(value = "Authorization", required = true) String authHeader,
            @PathVariable("id") Long id,
            @RequestBody Product product) throws ProductNotExistsException {
        validateUser(authHeader);
        ProductDto updated = productService.updateProduct(id, product);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> replaceProduct(
            @RequestHeader(value = "Authorization", required = true) String authHeader,
            @PathVariable("id") Long id,
            @RequestBody Product product) throws ProductNotExistsException {
        validateUser(authHeader);
        ProductDto replaced = productService.replaceProduct(id, product);
        return new ResponseEntity<>(replaced, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @RequestHeader(value = "Authorization", required = true) String authHeader,
            @PathVariable("id") Long id) throws ProductNotExistsException {
        validateUser(authHeader);
        productService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
