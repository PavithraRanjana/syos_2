package com.syos.repository.interfaces;

import com.syos.domain.models.Brand;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Brand entity operations.
 */
public interface BrandRepository extends Repository<Brand, Integer> {

    /**
     * Finds a brand by its code.
     */
    Optional<Brand> findByCode(String brandCode);

    /**
     * Finds a brand by its name.
     */
    Optional<Brand> findByName(String brandName);

    /**
     * Returns all brands ordered by name.
     */
    List<Brand> findAllOrderByName();

    /**
     * Searches brands by name.
     */
    List<Brand> searchByName(String namePart);
}
