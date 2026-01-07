package com.syos.repository.interfaces;

import com.syos.domain.models.Subcategory;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Subcategory entity operations.
 */
public interface SubcategoryRepository extends Repository<Subcategory, Integer> {

    /**
     * Finds subcategories by category ID.
     */
    List<Subcategory> findByCategoryId(Integer categoryId);

    /**
     * Finds a subcategory by code within a category.
     */
    Optional<Subcategory> findByCodeAndCategoryId(String subcategoryCode, Integer categoryId);

    /**
     * Finds a subcategory by name within a category.
     */
    Optional<Subcategory> findByNameAndCategoryId(String subcategoryName, Integer categoryId);
}
