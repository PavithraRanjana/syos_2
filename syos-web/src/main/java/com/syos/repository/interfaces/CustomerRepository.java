package com.syos.repository.interfaces;

import com.syos.domain.models.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Customer entity operations.
 */
public interface CustomerRepository extends Repository<Customer, Integer> {

    /**
     * Finds a customer by email.
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Finds a customer by phone number.
     */
    Optional<Customer> findByPhone(String phone);

    /**
     * Checks if an email is already registered.
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a phone number is already registered.
     */
    boolean existsByPhone(String phone);

    /**
     * Finds all active customers.
     */
    List<Customer> findAllActive();

    /**
     * Finds customers by name (partial match).
     */
    List<Customer> findByNameContaining(String name);

    /**
     * Finds customers registered within a date range.
     */
    List<Customer> findByRegistrationDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate);

    /**
     * Gets customers with recent orders.
     */
    List<Customer> findCustomersWithRecentOrders(int days);

    /**
     * Deactivates a customer account.
     */
    boolean deactivate(Integer customerId);

    /**
     * Activates a customer account.
     */
    boolean activate(Integer customerId);

    /**
     * Updates customer password hash.
     */
    boolean updatePassword(Integer customerId, String passwordHash);

    /**
     * Gets customer statistics.
     */
    CustomerStatistics getStatistics();

    /**
     * Customer statistics DTO.
     */
    record CustomerStatistics(
        long totalCustomers,
        long activeCustomers,
        long newCustomersThisMonth
    ) {}
}
