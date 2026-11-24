package com.businessmgmt.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic Data Access Object interface providing standard CRUD operations.
 *
 * @param <T> the entity type this DAO manages
 */
public interface GenericDao<T> {

    /**
     * Retrieves all entities.
     *
     * @return a list of all entities
     */
    List<T> findAll();

    /**
     * Finds an entity by its ID.
     *
     * @param id the entity ID
     * @return the entity if found, or null
     */
    T findById(int id);

    /**
     * Finds an entity by its ID wrapped in an Optional.
     *
     * @param id the entity ID
     * @return Optional containing entity or empty
     */
    default Optional<T> findByIdOptional(int id) {
        return Optional.ofNullable(findById(id));
    }

    /**
     * Saves a new entity to the database.
     *
     * @param entity the entity to save
     * @return the saved entity with its generated ID set
     */
    T save(T entity);

    /**
     * Updates an existing entity in the database.
     *
     * @param entity the entity to update
     * @return the updated entity
     */
    T update(T entity);

    /**
     * Deletes an entity by its ID.
     *
     * @param id the entity ID
     * @return true if the entity was deleted, false if not found
     */
    boolean delete(int id);
}
