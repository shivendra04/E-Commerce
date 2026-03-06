package com.scaler.productservicedecmwfeve;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;

@SpringBootApplication(exclude = { OAuth2ResourceServerAutoConfiguration.class })
public class ProductservicedecmwfeveApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductservicedecmwfeveApplication.class, args);
//        Product product = new Product();
//        product.getId();
//        product.setId(123L);

    }

}
