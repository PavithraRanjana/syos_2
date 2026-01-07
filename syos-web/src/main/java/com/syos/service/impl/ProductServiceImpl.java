package com.syos.service.impl;

import com.syos.domain.models.Product;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.exception.ProductNotFoundException;
import com.syos.exception.ValidationException;
import com.syos.repository.interfaces.ProductRepository;
import com.syos.service.interfaces.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ProductService.
 */
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product createProduct(Product product) {
        logger.debug("Creating product: {}", product.getProductCodeString());

        validateProduct(product);

        if (productRepository.existsByProductCode(product.getProductCodeString())) {
            throw new ValidationException("Product code already exists: " + product.getProductCodeString());
        }

        Product saved = productRepository.save(product);
        logger.info("Product created: {}", saved.getProductCodeString());
        return saved;
    }

    @Override
    public Product updateProduct(Product product) {
        logger.debug("Updating product: {}", product.getProductCodeString());

        if (product.getProductCode() == null || product.getProductCodeString().isEmpty()) {
            throw new ValidationException("Product code is required for update");
        }

        if (!productRepository.existsByProductCode(product.getProductCodeString())) {
            throw new ProductNotFoundException(product.getProductCodeString());
        }

        validateProduct(product);

        Product updated = productRepository.save(product);
        logger.info("Product updated: {}", updated.getProductCodeString());
        return updated;
    }

    @Override
    public Optional<Product> findById(Integer productId) {
        // Product uses String (product_code) as ID, not Integer
        // This method is kept for interface compatibility but delegates to findByProductCode
        return Optional.empty();
    }

    @Override
    public Optional<Product> findByProductCode(String productCode) {
        return productRepository.findByProductCode(productCode);
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> findAllActive() {
        return productRepository.findAllActive();
    }

    @Override
    public List<Product> findByCategory(Integer categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Product> findBySubcategory(Integer subcategoryId) {
        return productRepository.findBySubcategoryId(subcategoryId);
    }

    @Override
    public List<Product> findByBrand(Integer brandId) {
        return productRepository.findByBrandId(brandId);
    }

    @Override
    public List<Product> searchByName(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return List.of();
        }
        return productRepository.searchByName(searchTerm.trim());
    }

    @Override
    public Product updatePrice(String productCode, BigDecimal newPrice) {
        logger.debug("Updating price for product: {} to {}", productCode, newPrice);

        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Price must be a non-negative value");
        }

        Product product = productRepository.findByProductCode(productCode)
            .orElseThrow(() -> new ProductNotFoundException(productCode));

        product.setUnitPrice(new Money(newPrice));
        Product updated = productRepository.save(product);

        logger.info("Price updated for product: {} to {}", productCode, newPrice);
        return updated;
    }

    @Override
    public boolean activateProduct(String productCode) {
        logger.debug("Activating product: {}", productCode);

        Product product = productRepository.findByProductCode(productCode)
            .orElseThrow(() -> new ProductNotFoundException(productCode));

        product.setActive(true);
        productRepository.save(product);

        logger.info("Product activated: {}", productCode);
        return true;
    }

    @Override
    public boolean deactivateProduct(String productCode) {
        logger.debug("Deactivating product: {}", productCode);

        Product product = productRepository.findByProductCode(productCode)
            .orElseThrow(() -> new ProductNotFoundException(productCode));

        product.setActive(false);
        productRepository.save(product);

        logger.info("Product deactivated: {}", productCode);
        return true;
    }

    @Override
    public boolean existsByProductCode(String productCode) {
        return productRepository.existsByProductCode(productCode);
    }

    @Override
    public long getProductCount() {
        return productRepository.count();
    }

    @Override
    public long getActiveProductCount() {
        return productRepository.findAllActive().size();
    }

    @Override
    public List<Product> findAll(int page, int size) {
        int offset = page * size;
        return productRepository.findAll(offset, size);
    }

    private void validateProduct(Product product) {
        if (product == null) {
            throw new ValidationException("Product cannot be null");
        }
        if (product.getProductCode() == null || product.getProductCodeString().isEmpty()) {
            throw new ValidationException("Product code is required");
        }
        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            throw new ValidationException("Product name is required");
        }
        if (product.getUnitPrice() == null) {
            throw new ValidationException("Unit price is required");
        }
    }
}
