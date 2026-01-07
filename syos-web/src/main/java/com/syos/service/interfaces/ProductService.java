package com.syos.service.interfaces;

import com.syos.domain.models.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Product management operations.
 */
public interface ProductService {

    /**
     * Creates a new product.
     */
    Product createProduct(Product product);

    /**
     * Updates an existing product.
     */
    Product updateProduct(Product product);

    /**
     * Finds a product by its ID.
     */
    Optional<Product> findById(Integer productId);

    /**
     * Finds a product by its product code.
     */
    Optional<Product> findByProductCode(String productCode);

    /**
     * Finds all products.
     */
    List<Product> findAll();

    /**
     * Finds all active products.
     */
    List<Product> findAllActive();

    /**
     * Finds products by category.
     */
    List<Product> findByCategory(Integer categoryId);

    /**
     * Finds products by subcategory.
     */
    List<Product> findBySubcategory(Integer subcategoryId);

    /**
     * Finds products by brand.
     */
    List<Product> findByBrand(Integer brandId);

    /**
     * Searches products by name.
     */
    List<Product> searchByName(String searchTerm);

    /**
     * Updates the selling price of a product.
     */
    Product updatePrice(String productCode, BigDecimal newPrice);

    /**
     * Activates a product.
     */
    boolean activateProduct(String productCode);

    /**
     * Deactivates a product.
     */
    boolean deactivateProduct(String productCode);

    /**
     * Checks if a product code exists.
     */
    boolean existsByProductCode(String productCode);

    /**
     * Gets the count of all products.
     */
    long getProductCount();

    /**
     * Gets the count of active products.
     */
    long getActiveProductCount();

    /**
     * Finds products with pagination.
     */
    List<Product> findAll(int page, int size);
}
