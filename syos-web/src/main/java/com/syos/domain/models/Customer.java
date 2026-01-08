package com.syos.domain.models;

import com.syos.domain.enums.UserRole;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entity representing a system user (customer, staff, manager, admin).
 */
public class Customer {

    private Integer customerId;
    private String customerName;
    private String email;
    private String phone;
    private String address;
    private String passwordHash;
    private UserRole role;
    private LocalDate registrationDate;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Customer() {
        this.active = true;
        this.role = UserRole.CUSTOMER;
        this.registrationDate = LocalDate.now();
    }

    public Customer(String customerName, String email, String phone, String address) {
        this.customerName = customerName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.role = UserRole.CUSTOMER;
        this.active = true;
        this.registrationDate = LocalDate.now();
    }

    // Business Methods

    /**
     * Sets the password by hashing it with BCrypt.
     */
    public void setPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        this.passwordHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    /**
     * Verifies a plain password against the stored hash.
     */
    public boolean verifyPassword(String plainPassword) {
        if (passwordHash == null || plainPassword == null) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, passwordHash);
    }

    /**
     * Hashes a password using BCrypt (static method for registration).
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    // Getters and Setters

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    /**
     * Returns the registration date formatted as MMM d, yyyy for display.
     */
    public String getRegistrationDateFormatted() {
        return registrationDate != null ? registrationDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) : "";
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UserRole getRole() {
        return role != null ? role : UserRole.CUSTOMER;
    }

    public void setRole(UserRole role) {
        this.role = role != null ? role : UserRole.CUSTOMER;
    }

    /**
     * Get the role name as a string (for session storage).
     */
    public String getRoleName() {
        return getRole().name();
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", customerName='" + customerName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", active=" + active +
                '}';
    }
}
