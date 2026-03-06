package com.scaler.productservicedecmwfeve.services;

import com.scaler.productservicedecmwfeve.exceptions.ProductNotExistsException;
import com.scaler.productservicedecmwfeve.models.Category;
import com.scaler.productservicedecmwfeve.models.Product;
import com.scaler.productservicedecmwfeve.repositories.CategoryRepository;
import com.scaler.productservicedecmwfeve.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.scaler.productservicedecmwfeve.dtos.ProductDto;

import java.util.Date;
import java.util.Optional;

@Primary
@Service("selfProductService")
public class SelfProductService implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    
    @Autowired
    public SelfProductService(ProductRepository productRepository,
                              CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public ProductDto getSingleProduct(Long id) throws ProductNotExistsException {
        Optional<Product> productOptional = productRepository.findById(id);

        if (productOptional.isEmpty()) {
            throw new ProductNotExistsException("Product with id: " + id + " doesn't exist.");
        }

        return productToDto(productOptional.get());
    }

    @Override
    public Page<ProductDto> getAllProducts(int pageNo,
                                           int pageSize,
                                           String sortBy,
                                           String sortDir,
                                           String name,
                                           String category,
                                           Double minPrice,
                                           Double maxPrice) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String property = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        Sort sort = Sort.by(direction, property);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Specification<Product> spec = Specification.where(null);

        if (name != null && !name.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("title")), "%" + name.toLowerCase() + "%"));
        }
        if (category != null && !category.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.join("category").get("name")), category.toLowerCase()));
        }
        if (minPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        Page<Product> page = productRepository.findAll(spec, pageable);
        return page.map(this::productToDto);
    }

    @Override
    public ProductDto updateProduct(Long id, Product product) throws ProductNotExistsException {
        Product existing = getProductEntity(id);
        Date now = new Date();

        if (product.getTitle() != null) {
            existing.setTitle(product.getTitle());
        }
        if (product.getDescription() != null) {
            existing.setDescription(product.getDescription());
        }
        if (product.getImageUrl() != null) {
            existing.setImageUrl(product.getImageUrl());
        }
        existing.setPrice(product.getPrice());
        existing.setNumberOfSales(product.getNumberOfSales());
        if (product.getCategory() != null) {
            existing.setCategory(getOrCreateCategory(product.getCategory()));
        }

        existing.setLastUpdatedAt(now);
        return productToDto(productRepository.save(existing));
    }

    @Override
    public ProductDto replaceProduct(Long id, Product product) throws ProductNotExistsException {
        Product existing = getProductEntity(id);
        Date now = new Date();

        existing.setTitle(product.getTitle());
        existing.setDescription(product.getDescription());
        existing.setImageUrl(product.getImageUrl());
        existing.setPrice(product.getPrice());
        existing.setNumberOfSales(product.getNumberOfSales());
        if (product.getCategory() != null) {
            existing.setCategory(getOrCreateCategory(product.getCategory()));
        }
        existing.setLastUpdatedAt(now);

        return productToDto(productRepository.save(existing));
    }

    @Override
    public ProductDto addNewProduct(Product product) {

        
        Date now = new Date();
        product.setCreatedAt(now);
        product.setLastUpdatedAt(now);

        if (product.getCategory() != null) {
            product.setCategory(getOrCreateCategory(product.getCategory()));
        }

        Product savedProduct = productRepository.save(product);
        return productToDto(savedProduct);
    }

    @Override
    public boolean deleteProduct(Long id) throws ProductNotExistsException {
        if (productRepository.findById(id).isEmpty()) {
            throw new ProductNotExistsException("Product with id: " + id + " doesn't exist.");
        }
        productRepository.deleteById(id);
        return true;
    }

    private Category getOrCreateCategory(Category category) {
        if (category.getName() == null || category.getName().isBlank()) {
            return category;
        }
        Optional<Category> existingCategory = categoryRepository.findByName(category.getName());
        if (existingCategory.isPresent()) {
            return existingCategory.get();
        }
        Date now = new Date();
        category.setCreatedAt(now);
        category.setLastUpdatedAt(now);
        return categoryRepository.save(category);
    }

    /** Fetches the Product entity by id (for internal use in update/replace). */
    private Product getProductEntity(Long id) throws ProductNotExistsException {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotExistsException("Product with id: " + id + " doesn't exist."));
    }

    public ProductDto productToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setPrice(product.getPrice());
        dto.setCategory(product.getCategory() != null ? product.getCategory().getName() : null);
        dto.setDescription(product.getDescription());
        dto.setImageUrl(product.getImageUrl());
        return dto;
    }
}
