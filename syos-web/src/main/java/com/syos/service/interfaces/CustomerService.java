package com.syos.service.interfaces;

import com.syos.domain.enums.UserRole;
import com.syos.domain.models.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Customer management and authentication operations.
 */
public interface CustomerService {

    /**
     * Registers a new customer.
     */
    Customer register(String name, String email, String phone, String address, String password);

    /**
     * Authenticates a customer with email and password.
     */
    AuthenticationResult authenticate(String email, String password);

    /**
     * Finds a customer by ID.
     */
    Optional<Customer> findById(Integer customerId);

    /**
     * Finds a customer by email.
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Updates customer profile information.
     */
    Customer updateProfile(Integer customerId, String name, String phone, String address);

    /**
     * Changes customer password.
     */
    boolean changePassword(Integer customerId, String currentPassword, String newPassword);

    /**
     * Resets customer password (admin function).
     */
    boolean resetPassword(Integer customerId, String newPassword);

    /**
     * Deactivates a customer account.
     */
    boolean deactivateAccount(Integer customerId);

    /**
     * Activates a customer account.
     */
    boolean activateAccount(Integer customerId);

    /**
     * Finds all customers.
     */
    List<Customer> findAll();

    /**
     * Finds all active customers.
     */
    List<Customer> findAllActive();

    /**
     * Searches customers by name.
     */
    List<Customer> searchByName(String searchTerm);

    /**
     * Gets customers with pagination.
     */
    List<Customer> findAll(int page, int size);

    /**
     * Gets the total customer count.
     */
    long getCustomerCount();

    /**
     * Gets customer statistics.
     */
    CustomerStatistics getStatistics();

    /**
     * Validates if an email is available for registration.
     */
    boolean isEmailAvailable(String email);

    /**
     * Validates if a phone number is available for registration.
     */
    boolean isPhoneAvailable(String phone);

    // ==================== Role Management (Admin) ====================

    /**
     * Updates a user's role (admin function).
     */
    boolean updateUserRole(Integer customerId, UserRole role);

    /**
     * Finds all users with a specific role.
     */
    List<Customer> findByRole(UserRole role);

    /**
     * Creates a new user with a specific role (admin function).
     */
    Customer createUserWithRole(String name, String email, String phone, String address, String password, UserRole role);

    /**
     * Authentication result.
     */
    record AuthenticationResult(
        boolean success,
        Customer customer,
        String errorMessage
    ) {
        public static AuthenticationResult success(Customer customer) {
            return new AuthenticationResult(true, customer, null);
        }

        public static AuthenticationResult failure(String message) {
            return new AuthenticationResult(false, null, message);
        }
    }

    /**
     * Customer statistics.
     */
    record CustomerStatistics(
        long totalCustomers,
        long activeCustomers,
        long newCustomersThisMonth
    ) {}
}
