package com.syos.repository.interfaces;

import com.syos.domain.models.Category;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Category entity operations.
 */
public interface CategoryRepository extends Repository<Category, Integer> {

    /**
     * Finds a category by its code.
     */
    Optional<Category> findByCode(String categoryCode);

    /**
     * Finds a category by its name.
     */
    Optional<Category> findByName(String categoryName);

    /**
     * Returns all categories ordered by name.
     */
    List<Category> findAllOrderByName();
}
