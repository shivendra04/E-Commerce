package com.scaler.productservicedecmwfeve.services;

import com.scaler.productservicedecmwfeve.dtos.ProductDto;
import com.scaler.productservicedecmwfeve.exceptions.ProductNotExistsException;
import com.scaler.productservicedecmwfeve.models.Product;
import org.springframework.data.domain.Page;

public interface ProductService {

    ProductDto getSingleProduct(Long id) throws ProductNotExistsException;

    Page<ProductDto> getAllProducts(int pageNo,
                                    int pageSize,
                                    String sortBy,
                                    String sortDir,
                                    String name,
                                    String category,
                                    Double minPrice,
                                    Double maxPrice);

    ProductDto updateProduct(Long id, Product product) throws ProductNotExistsException;

    ProductDto replaceProduct(Long id, Product product) throws ProductNotExistsException;

    ProductDto addNewProduct(Product product);

    boolean deleteProduct(Long id) throws ProductNotExistsException;
}
