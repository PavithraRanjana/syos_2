package com.syos.repository.interfaces;

import com.syos.domain.models.Product;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity operations.
 */
public interface ProductRepository extends Repository<Product, String> {

    /**
     * Finds a product by its product code.
     */
    Optional<Product> findByProductCode(String productCode);

    /**
     * Finds all products with full catalog information (category, subcategory, brand names).
     */
    List<Product> findAllWithCatalogInfo();

    /**
     * Finds all active products.
     */
    List<Product> findAllActive();

    /**
     * Finds products by category ID.
     */
    List<Product> findByCategoryId(Integer categoryId);

    /**
     * Finds products by subcategory ID.
     */
    List<Product> findBySubcategoryId(Integer subcategoryId);

    /**
     * Finds products by brand ID.
     */
    List<Product> findByBrandId(Integer brandId);

    /**
     * Searches products by name, code, category, subcategory, or brand.
     */
    List<Product> search(String searchTerm);

    /**
     * Searches products with pagination.
     */
    List<Product> search(String searchTerm, int offset, int limit);

    /**
     * Generates the next product code for a given category, subcategory, and brand.
     */
    String generateProductCode(Integer categoryId, Integer subcategoryId, Integer brandId);

    /**
     * Updates a product's price.
     */
    boolean updatePrice(String productCode, java.math.BigDecimal newPrice);

    /**
     * Activates a product.
     */
    boolean activate(String productCode);

    /**
     * Deactivates a product.
     */
    boolean deactivate(String productCode);

    /**
     * Checks if a product with the given code exists.
     */
    boolean existsByProductCode(String productCode);

    /**
     * Searches products by name (partial match).
     */
    List<Product> searchByName(String searchTerm);
}
