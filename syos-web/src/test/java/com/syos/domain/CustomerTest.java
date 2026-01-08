package com.syos.domain;

import com.syos.domain.models.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Customer domain model.
 */
class CustomerTest {

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer("John Doe", "john@example.com", "+94771234567", "123 Test Street");
        customer.setCustomerId(1);
    }

    @Nested
    @DisplayName("Password management tests")
    class PasswordManagementTests {

        @Test
        @DisplayName("Should set password and hash it")
        void shouldSetPasswordAndHashIt() {
            customer.setPassword("password123");
            assertNotNull(customer.getPasswordHash());
            assertNotEquals("password123", customer.getPasswordHash());
            assertTrue(customer.getPasswordHash().startsWith("$2a$") || customer.getPasswordHash().startsWith("$2b$"));
        }

        @Test
        @DisplayName("Should verify correct password")
        void shouldVerifyCorrectPassword() {
            customer.setPassword("password123");
            assertTrue(customer.verifyPassword("password123"));
        }

        @Test
        @DisplayName("Should reject incorrect password")
        void shouldRejectIncorrectPassword() {
            customer.setPassword("password123");
            assertFalse(customer.verifyPassword("wrongpassword"));
        }

        @Test
        @DisplayName("Should return false for null password verification")
        void shouldReturnFalseForNullPasswordVerification() {
            customer.setPassword("password123");
            assertFalse(customer.verifyPassword(null));
        }

        @Test
        @DisplayName("Should return false when no password hash set")
        void shouldReturnFalseWhenNoPasswordHashSet() {
            assertFalse(customer.verifyPassword("password123"));
        }

        @Test
        @DisplayName("Should throw exception for short password")
        void shouldThrowForShortPassword() {
            assertThrows(IllegalArgumentException.class, () -> customer.setPassword("12345"));
        }

        @Test
        @DisplayName("Should throw exception for null password")
        void shouldThrowForNullPassword() {
            assertThrows(IllegalArgumentException.class, () -> customer.setPassword(null));
        }

        @Test
        @DisplayName("Static hashPassword should hash correctly")
        void staticHashPasswordShouldHashCorrectly() {
            String hash = Customer.hashPassword("password123");
            assertNotNull(hash);
            assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"));
        }

        @Test
        @DisplayName("Static hashPassword should throw for short password")
        void staticHashPasswordShouldThrowForShortPassword() {
            assertThrows(IllegalArgumentException.class, () -> Customer.hashPassword("12345"));
        }
    }

    @Nested
    @DisplayName("Account status tests")
    class AccountStatusTests {

        @Test
        @DisplayName("New customer should be active by default")
        void newCustomerShouldBeActiveByDefault() {
            assertTrue(customer.isActive());
        }

        @Test
        @DisplayName("Should activate customer")
        void shouldActivateCustomer() {
            customer.setActive(false);
            customer.activate();
            assertTrue(customer.isActive());
        }

        @Test
        @DisplayName("Should deactivate customer")
        void shouldDeactivateCustomer() {
            customer.deactivate();
            assertFalse(customer.isActive());
        }
    }

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Full constructor should set all fields")
        void fullConstructorShouldSetAllFields() {
            Customer newCustomer = new Customer("Jane Doe", "jane@example.com", "+94779876543", "456 New Street");
            assertEquals("Jane Doe", newCustomer.getCustomerName());
            assertEquals("jane@example.com", newCustomer.getEmail());
            assertEquals("+94779876543", newCustomer.getPhone());
            assertEquals("456 New Street", newCustomer.getAddress());
            assertTrue(newCustomer.isActive());
            assertEquals(LocalDate.now(), newCustomer.getRegistrationDate());
        }

        @Test
        @DisplayName("Default constructor should create active customer")
        void defaultConstructorShouldCreateActiveCustomer() {
            Customer empty = new Customer();
            assertTrue(empty.isActive());
            assertEquals(LocalDate.now(), empty.getRegistrationDate());
        }
    }

    @Nested
    @DisplayName("Getter/Setter tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get customer name")
        void shouldSetAndGetCustomerName() {
            customer.setCustomerName("Jane Doe");
            assertEquals("Jane Doe", customer.getCustomerName());
        }

        @Test
        @DisplayName("Should set and get email")
        void shouldSetAndGetEmail() {
            customer.setEmail("jane@example.com");
            assertEquals("jane@example.com", customer.getEmail());
        }

        @Test
        @DisplayName("Should set and get phone")
        void shouldSetAndGetPhone() {
            customer.setPhone("+94779876543");
            assertEquals("+94779876543", customer.getPhone());
        }

        @Test
        @DisplayName("Should set and get address")
        void shouldSetAndGetAddress() {
            customer.setAddress("New Address");
            assertEquals("New Address", customer.getAddress());
        }

        @Test
        @DisplayName("Should set and get customer ID")
        void shouldSetAndGetCustomerId() {
            customer.setCustomerId(100);
            assertEquals(100, customer.getCustomerId());
        }

        @Test
        @DisplayName("Should set and get registration date")
        void shouldSetAndGetRegistrationDate() {
            LocalDate date = LocalDate.of(2024, 1, 15);
            customer.setRegistrationDate(date);
            assertEquals(date, customer.getRegistrationDate());
        }

        @Test
        @DisplayName("Should set and get password hash directly")
        void shouldSetAndGetPasswordHashDirectly() {
            customer.setPasswordHash("somehash");
            assertEquals("somehash", customer.getPasswordHash());
        }
    }

    @Nested
    @DisplayName("toString tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should contain customer ID")
        void toStringShouldContainCustomerId() {
            String str = customer.toString();
            assertTrue(str.contains("customerId=1"));
        }

        @Test
        @DisplayName("toString should contain customer name")
        void toStringShouldContainCustomerName() {
            String str = customer.toString();
            assertTrue(str.contains("John Doe"));
        }

        @Test
        @DisplayName("toString should contain email")
        void toStringShouldContainEmail() {
            String str = customer.toString();
            assertTrue(str.contains("john@example.com"));
        }

        @Test
        @DisplayName("toString should not contain password")
        void toStringShouldNotContainPassword() {
            customer.setPassword("password123");
            String str = customer.toString();
            assertFalse(str.contains("password123"));
            assertFalse(str.contains("passwordHash"));
        }
    }
}
