package com.scaler.productservicedecmwfeve.services;

import com.scaler.productservicedecmwfeve.dtos.ProductDto;
import com.scaler.productservicedecmwfeve.exceptions.ProductNotExistsException;
import com.scaler.productservicedecmwfeve.models.Category;
import com.scaler.productservicedecmwfeve.models.Product;
import com.scaler.productservicedecmwfeve.repositories.CategoryRepository;
import com.scaler.productservicedecmwfeve.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SelfProductServiceTests {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private SelfProductService selfProductService;

    @Test
    void getSingleProduct_WhenProductExists_ReturnsProductDto() throws ProductNotExistsException {
        Product product = new Product();
        product.setId(1L);
        product.setTitle("Test");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDto result = selfProductService.getSingleProduct(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test", result.getTitle());
    }

    @Test
    void getSingleProduct_WhenProductNotExists_ThrowsProductNotExistsException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProductNotExistsException.class, () -> selfProductService.getSingleProduct(999L));
    }

    @Test
    void getAllProducts_ReturnsPageFromRepository() {
        List<Product> products = List.of(new Product(), new Product());
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(products, PageRequest.of(0, 10), 2));

        var result = selfProductService.getAllProducts(0, 10, "id", "asc",
                null, null, null, null);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void addNewProduct_SavesAndReturnsProductDto() {
        Product product = new Product();
        product.setTitle("New");
        product.setPrice(10.0);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        ProductDto result = selfProductService.addNewProduct(product);

        assertNotNull(result);
        assertEquals("New", result.getTitle());
        assertEquals(10.0, result.getPrice());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_WhenProductExists_UpdatesAndReturns() throws ProductNotExistsException {
        Product existing = new Product();
        existing.setId(1L);
        existing.setTitle("Old");
        existing.setPrice(5.0);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));

        Product update = new Product();
        update.setTitle("Updated");
        update.setPrice(15.0);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductDto result = selfProductService.updateProduct(1L, update);

        assertEquals("Updated", result.getTitle());
        assertEquals(15.0, result.getPrice());
        verify(productRepository).save(existing);
    }

    @Test
    void updateProduct_WhenProductNotExists_ThrowsProductNotExistsException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProductNotExistsException.class, () ->
                selfProductService.updateProduct(999L, new Product()));
    }

    @Test
    void replaceProduct_WhenProductExists_ReplacesAndReturns() throws ProductNotExistsException {
        Product existing = new Product();
        existing.setId(1L);
        existing.setTitle("Old");
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));

        Product replace = new Product();
        replace.setTitle("Replaced");
        replace.setPrice(20.0);
        replace.setNumberOfSales(10);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductDto result = selfProductService.replaceProduct(1L, replace);

        assertEquals("Replaced", result.getTitle());
        assertEquals(20.0, result.getPrice());
        verify(productRepository).save(existing);
    }

    @Test
    void replaceProduct_WhenProductNotExists_ThrowsProductNotExistsException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProductNotExistsException.class, () ->
                selfProductService.replaceProduct(999L, new Product()));
    }

    @Test
    void deleteProduct_WhenProductExists_DeletesAndReturnsTrue() throws ProductNotExistsException {
        when(productRepository.findById(1L)).thenReturn(Optional.of(new Product()));

        boolean result = selfProductService.deleteProduct(1L);

        assertTrue(result);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_WhenProductNotExists_ThrowsProductNotExistsException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProductNotExistsException.class, () -> selfProductService.deleteProduct(999L));
        verify(productRepository, never()).deleteById(anyLong());
    }
}
