package com.syos.service.impl;

import com.syos.domain.models.Customer;
import com.syos.exception.CustomerNotFoundException;
import com.syos.exception.DuplicateEmailException;
import com.syos.exception.ValidationException;
import com.syos.repository.interfaces.CustomerRepository;
import com.syos.service.interfaces.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Implementation of CustomerService.
 */
public class CustomerServiceImpl implements CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Customer register(String name, String email, String phone, String address, String password) {
        logger.debug("Registering new customer: {}", email);

        // Validate inputs
        validateName(name);
        validateEmail(email);
        validatePassword(password);

        // Check for duplicates
        if (customerRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }

        if (phone != null && !phone.isEmpty() && customerRepository.existsByPhone(phone)) {
            throw new ValidationException("Phone number already registered: " + phone);
        }

        // Create customer
        Customer customer = new Customer(name, email, phone, address);
        customer.setPassword(password);

        Customer saved = customerRepository.save(customer);
        logger.info("Customer registered: {} (ID: {})", email, saved.getCustomerId());
        return saved;
    }

    @Override
    public AuthenticationResult authenticate(String email, String password) {
        logger.debug("Authenticating customer: {}", email);

        if (email == null || email.isEmpty()) {
            return AuthenticationResult.failure("Email is required");
        }
        if (password == null || password.isEmpty()) {
            return AuthenticationResult.failure("Password is required");
        }

        Optional<Customer> customerOpt = customerRepository.findByEmail(email);
        if (customerOpt.isEmpty()) {
            logger.warn("Authentication failed: customer not found for email {}", email);
            return AuthenticationResult.failure("Invalid email or password");
        }

        Customer customer = customerOpt.get();

        if (!customer.isActive()) {
            logger.warn("Authentication failed: customer account inactive for {}", email);
            return AuthenticationResult.failure("Account is inactive");
        }

        if (!customer.verifyPassword(password)) {
            logger.warn("Authentication failed: invalid password for {}", email);
            return AuthenticationResult.failure("Invalid email or password");
        }

        logger.info("Customer authenticated: {} (ID: {})", email, customer.getCustomerId());
        return AuthenticationResult.success(customer);
    }

    @Override
    public Optional<Customer> findById(Integer customerId) {
        return customerRepository.findById(customerId);
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    @Override
    public Customer updateProfile(Integer customerId, String name, String phone, String address) {
        logger.debug("Updating customer profile: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));

        validateName(name);

        // Check phone uniqueness if changed
        if (phone != null && !phone.isEmpty() &&
            !phone.equals(customer.getPhone()) &&
            customerRepository.existsByPhone(phone)) {
            throw new ValidationException("Phone number already registered: " + phone);
        }

        customer.setCustomerName(name);
        customer.setPhone(phone);
        customer.setAddress(address);

        Customer updated = customerRepository.save(customer);
        logger.info("Customer profile updated: {}", customerId);
        return updated;
    }

    @Override
    public boolean changePassword(Integer customerId, String currentPassword, String newPassword) {
        logger.debug("Changing password for customer: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));

        // Verify current password
        if (!customer.verifyPassword(currentPassword)) {
            logger.warn("Password change failed: incorrect current password for customer {}", customerId);
            return false;
        }

        validatePassword(newPassword);

        String newHash = Customer.hashPassword(newPassword);
        boolean updated = customerRepository.updatePassword(customerId, newHash);

        if (updated) {
            logger.info("Password changed for customer: {}", customerId);
        }
        return updated;
    }

    @Override
    public boolean resetPassword(Integer customerId, String newPassword) {
        logger.debug("Resetting password for customer: {}", customerId);

        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }

        validatePassword(newPassword);

        String newHash = Customer.hashPassword(newPassword);
        boolean updated = customerRepository.updatePassword(customerId, newHash);

        if (updated) {
            logger.info("Password reset for customer: {}", customerId);
        }
        return updated;
    }

    @Override
    public boolean deactivateAccount(Integer customerId) {
        logger.debug("Deactivating customer account: {}", customerId);

        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }

        boolean deactivated = customerRepository.deactivate(customerId);
        if (deactivated) {
            logger.info("Customer account deactivated: {}", customerId);
        }
        return deactivated;
    }

    @Override
    public boolean activateAccount(Integer customerId) {
        logger.debug("Activating customer account: {}", customerId);

        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }

        boolean activated = customerRepository.activate(customerId);
        if (activated) {
            logger.info("Customer account activated: {}", customerId);
        }
        return activated;
    }

    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Override
    public List<Customer> findAllActive() {
        return customerRepository.findAllActive();
    }

    @Override
    public List<Customer> searchByName(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return List.of();
        }
        return customerRepository.findByNameContaining(searchTerm.trim());
    }

    @Override
    public List<Customer> findAll(int page, int size) {
        int offset = page * size;
        return customerRepository.findAll(offset, size);
    }

    @Override
    public long getCustomerCount() {
        return customerRepository.count();
    }

    @Override
    public CustomerStatistics getStatistics() {
        CustomerRepository.CustomerStatistics repoStats = customerRepository.getStatistics();
        return new CustomerStatistics(
            repoStats.totalCustomers(),
            repoStats.activeCustomers(),
            repoStats.newCustomersThisMonth()
        );
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !customerRepository.existsByEmail(email);
    }

    @Override
    public boolean isPhoneAvailable(String phone) {
        return !customerRepository.existsByPhone(phone);
    }

    // ==================== Validation Methods ====================

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Name is required");
        }
        if (name.trim().length() < 2) {
            throw new ValidationException("Name must be at least 2 characters");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email format");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
    }
}
