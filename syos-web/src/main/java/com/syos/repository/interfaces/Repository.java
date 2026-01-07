package com.syos.repository.interfaces;

import java.util.List;
import java.util.Optional;

/**
 * Base repository interface defining common CRUD operations.
 * Following the Repository pattern from Domain-Driven Design.
 *
 * @param <T> The entity type
 * @param <ID> The ID type
 */
public interface Repository<T, ID> {

    /**
     * Saves an entity (insert or update).
     * @return The saved entity with generated ID if applicable
     */
    T save(T entity);

    /**
     * Finds an entity by its ID.
     */
    Optional<T> findById(ID id);

    /**
     * Returns all entities.
     */
    List<T> findAll();

    /**
     * Returns a paginated list of entities.
     */
    List<T> findAll(int offset, int limit);

    /**
     * Deletes an entity by its ID.
     * @return true if deleted, false if not found
     */
    boolean deleteById(ID id);

    /**
     * Checks if an entity exists by its ID.
     */
    boolean existsById(ID id);

    /**
     * Returns the total count of entities.
     */
    long count();
}
