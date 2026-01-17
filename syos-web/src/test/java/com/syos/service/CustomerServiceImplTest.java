package com.syos.service;

import com.syos.domain.enums.UserRole;
import com.syos.domain.models.Customer;
import com.syos.exception.CustomerNotFoundException;
import com.syos.exception.DuplicateEmailException;
import com.syos.exception.ValidationException;
import com.syos.repository.interfaces.CustomerRepository;
import com.syos.repository.impl.CustomerRepositoryImpl;
import com.syos.service.impl.CustomerServiceImpl;
import com.syos.service.interfaces.CustomerService.AuthenticationResult;
import com.syos.service.interfaces.CustomerService.CustomerStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomerServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerServiceImpl(customerRepository);
    }

    private Customer createTestCustomer(String name, String email) {
        Customer customer = new Customer(name, email, "+94771234567", "123 Test Street");
        customer.setCustomerId(1);
        customer.setPassword("password123");
        return customer;
    }

    @Nested
    @DisplayName("register tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register customer successfully")
        void shouldRegisterCustomerSuccessfully() {
            // Arrange
            when(customerRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(customerRepository.existsByPhone("+94771234567")).thenReturn(false);
            when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
                Customer saved = invocation.getArgument(0);
                saved.setCustomerId(1);
                return saved;
            });

            // Act
            Customer result = customerService.register(
                    "John Doe",
                    "test@example.com",
                    "+94771234567",
                    "123 Test Street",
                    "password123");

            // Assert
            assertNotNull(result);
            assertEquals("John Doe", result.getCustomerName());
            assertEquals("test@example.com", result.getEmail());
            assertNotNull(result.getPasswordHash());
            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should throw exception for duplicate email")
        void shouldThrowForDuplicateEmail() {
            // Arrange
            when(customerRepository.existsByEmail("existing@example.com")).thenReturn(true);

            // Act & Assert
            assertThrows(DuplicateEmailException.class,
                    () -> customerService.register(
                            "John Doe",
                            "existing@example.com",
                            "+94771234567",
                            "123 Test Street",
                            "password123"));
            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception for duplicate phone")
        void shouldThrowForDuplicatePhone() {
            // Arrange
            when(customerRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(customerRepository.existsByPhone("+94771234567")).thenReturn(true);

            // Act & Assert
            assertThrows(ValidationException.class,
                    () -> customerService.register(
                            "John Doe",
                            "test@example.com",
                            "+94771234567",
                            "123 Test Street",
                            "password123"));
        }

        @Test
        @DisplayName("Should throw exception for invalid email format")
        void shouldThrowForInvalidEmailFormat() {
            // Act & Assert
            assertThrows(ValidationException.class,
                    () -> customerService.register(
                            "John Doe",
                            "invalid-email",
                            "+94771234567",
                            "123 Test Street",
                            "password123"));
        }

        @Test
        @DisplayName("Should throw exception for empty name")
        void shouldThrowForEmptyName() {
            // Act & Assert
            assertThrows(ValidationException.class,
                    () -> customerService.register(
                            "",
                            "test@example.com",
                            "+94771234567",
                            "123 Test Street",
                            "password123"));
        }

        @Test
        @DisplayName("Should throw exception for short name")
        void shouldThrowForShortName() {
            // Act & Assert
            assertThrows(ValidationException.class,
                    () -> customerService.register(
                            "J",
                            "test@example.com",
                            "+94771234567",
                            "123 Test Street",
                            "password123"));
        }

        @Test
        @DisplayName("Should throw exception for short password")
        void shouldThrowForShortPassword() {
            // Act & Assert
            assertThrows(ValidationException.class,
                    () -> customerService.register(
                            "John Doe",
                            "test@example.com",
                            "+94771234567",
                            "123 Test Street",
                            "12345"));
        }

        @Test
        @DisplayName("Should allow registration without phone")
        void shouldAllowRegistrationWithoutPhone() {
            // Arrange
            when(customerRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
                Customer saved = invocation.getArgument(0);
                saved.setCustomerId(1);
                return saved;
            });

            // Act
            Customer result = customerService.register(
                    "John Doe",
                    "test@example.com",
                    null,
                    "123 Test Street",
                    "password123");

            // Assert
            assertNotNull(result);
            verify(customerRepository, never()).existsByPhone(anyString());
        }
    }

    @Nested
    @DisplayName("authenticate tests")
    class AuthenticateTests {

        @Test
        @DisplayName("Should authenticate successfully with valid credentials")
        void shouldAuthenticateSuccessfully() {
            // Arrange
            Customer customer = createTestCustomer("John Doe", "test@example.com");
            when(customerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(customer));

            // Act
            AuthenticationResult result = customerService.authenticate("test@example.com", "password123");

            // Assert
            assertTrue(result.success());
            assertNotNull(result.customer());
            assertNull(result.errorMessage());
        }

        @Test
        @DisplayName("Should fail authentication with wrong password")
        void shouldFailWithWrongPassword() {
            // Arrange
            Customer customer = createTestCustomer("John Doe", "test@example.com");
            when(customerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(customer));

            // Act
            AuthenticationResult result = customerService.authenticate("test@example.com", "wrongpassword");

            // Assert
            assertFalse(result.success());
            assertNull(result.customer());
            assertNotNull(result.errorMessage());
        }

        @Test
        @DisplayName("Should fail authentication for non-existent email")
        void shouldFailForNonExistentEmail() {
            // Arrange
            when(customerRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

            // Act
            AuthenticationResult result = customerService.authenticate("nonexistent@example.com", "password123");

            // Assert
            assertFalse(result.success());
            assertNull(result.customer());
        }

        @Test
        @DisplayName("Should fail authentication for inactive account")
        void shouldFailForInactiveAccount() {
            // Arrange
            Customer customer = createTestCustomer("John Doe", "test@example.com");
            customer.setActive(false);
            when(customerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(customer));

            // Act
            AuthenticationResult result = customerService.authenticate("test@example.com", "password123");

            // Assert
            assertFalse(result.success());
            assertTrue(result.errorMessage().contains("inactive"));
        }

        @Test
        @DisplayName("Should fail authentication with null email")
        void shouldFailWithNullEmail() {
            // Act
            AuthenticationResult result = customerService.authenticate(null, "password123");

            // Assert
            assertFalse(result.success());
            assertEquals("Email is required", result.errorMessage());
        }

        @Test
        @DisplayName("Should fail authentication with null password")
        void shouldFailWithNullPassword() {
            // Act
            AuthenticationResult result = customerService.authenticate("test@example.com", null);

            // Assert
            assertFalse(result.success());
            assertEquals("Password is required", result.errorMessage());
        }
    }

    @Nested
    @DisplayName("findById tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should find customer by ID")
        void shouldFindCustomerById() {
            // Arrange
            Customer customer = createTestCustomer("John Doe", "test@example.com");
            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));

            // Act
            Optional<Customer> result = customerService.findById(1);

            // Assert
            assertTrue(result.isPresent());
            assertEquals("John Doe", result.get().getCustomerName());
        }

        @Test
        @DisplayName("Should return empty when customer not found")
        void shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(customerRepository.findById(999)).thenReturn(Optional.empty());

            // Act
            Optional<Customer> result = customerService.findById(999);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findByEmail tests")
    class FindByEmailTests {

        @Test
        @DisplayName("Should find customer by email")
        void shouldFindCustomerByEmail() {
            // Arrange
            Customer customer = createTestCustomer("John Doe", "test@example.com");
            when(customerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(customer));

            // Act
            Optional<Customer> result = customerService.findByEmail("test@example.com");

            // Assert
            assertTrue(result.isPresent());
            assertEquals("test@example.com", result.get().getEmail());
        }

        @Test
        @DisplayName("Should return empty when email not found")
        void shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(customerRepository.findByEmail("non@example.com")).thenReturn(Optional.empty());

            // Act
            Optional<Customer> result = customerService.findByEmail("non@example.com");

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("updateProfile tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update profile successfully")
        void shouldUpdateProfileSuccessfully() {
            // Arrange
            Customer customer = createTestCustomer("John Doe", "test@example.com");
            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(customerRepository.save(any(Customer.class))).thenReturn(customer);

            // Act
            Customer result = customerService.updateProfile(1, "Jane Doe", "+94779876543", "456 New Street");

            // Assert
            assertNotNull(result);
            assertEquals("Jane Doe", result.getCustomerName());
            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should throw exception when customer not found")
        void shouldThrowWhenCustomerNotFound() {
            // Arrange
            when(customerRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(CustomerNotFoundException.class,
                    () -> customerService.updateProfile(999, "Jane Doe", "+94779876543", "456 New Street"));
        }

        @Test
        @DisplayName("Should throw exception for duplicate phone on different customer")
        void shouldThrowForDuplicatePhoneOnDifferentCustomer() {
            // Arrange
            Customer customer = createTestCustomer("John Doe", "test@example.com");
            customer.setPhone("+94771111111");
            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(customerRepository.existsByPhone("+94772222222")).thenReturn(true);

            // Act & Assert
            assertThrows(ValidationException.class,
                    () -> customerService.updateProfile(1, "John Doe", "+94772222222", "123 Test Street"));
        }
    }

    @Nested
    @DisplayName("changePassword tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully")
        void shouldChangePasswordSuccessfully() {
            // Arrange
            Customer customer = createTestCustomer("John Doe", "test@example.com");
            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            when(customerRepository.updatePassword(eq(1), anyString())).thenReturn(true);

            // Act
            boolean result = customerService.changePassword(1, "password123", "newpassword123");

            // Assert
            assertTrue(result);
            verify(customerRepository).updatePassword(eq(1), anyString());
        }

        @Test
        @DisplayName("Should fail when current password is incorrect")
        void shouldFailWhenCurrentPasswordIncorrect() {
            // Arrange
            Customer customer = createTestCustomer("John Doe", "test@example.com");
            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));

            // Act
            boolean result = customerService.changePassword(1, "wrongpassword", "newpassword123");

            // Assert
            assertFalse(result);
            verify(customerRepository, never()).updatePassword(anyInt(), anyString());
        }

        @Test
        @DisplayName("Should throw exception for short new password")
        void shouldThrowForShortNewPassword() {
            // Arrange
            Customer customer = createTestCustomer("John Doe", "test@example.com");
            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));

            // Act & Assert
            assertThrows(ValidationException.class,
                    () -> customerService.changePassword(1, "password123", "12345"));
        }

        @Test
        @DisplayName("Should throw exception when customer not found")
        void shouldThrowWhenCustomerNotFound() {
            // Arrange
            when(customerRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(CustomerNotFoundException.class,
                    () -> customerService.changePassword(999, "any", "any"));
        }
    }

    @Nested
    @DisplayName("resetPassword tests")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should reset password successfully")
        void shouldResetPasswordSuccessfully() {
            // Arrange
            when(customerRepository.existsById(1)).thenReturn(true);
            when(customerRepository.updatePassword(eq(1), anyString())).thenReturn(true);

            // Act
            boolean result = customerService.resetPassword(1, "newpassword123");

            // Assert
            assertTrue(result);
            verify(customerRepository).updatePassword(eq(1), anyString());
        }

        @Test
        @DisplayName("Should throw exception when customer not found")
        void shouldThrowWhenCustomerNotFound() {
            // Arrange
            when(customerRepository.existsById(999)).thenReturn(false);

            // Act & Assert
            assertThrows(CustomerNotFoundException.class,
                    () -> customerService.resetPassword(999, "newpassword123"));
        }
    }

    @Nested
    @DisplayName("account activation tests")
    class AccountActivationTests {

        @Test
        @DisplayName("Should deactivate account successfully")
        void shouldDeactivateAccountSuccessfully() {
            // Arrange
            when(customerRepository.existsById(1)).thenReturn(true);
            when(customerRepository.deactivate(1)).thenReturn(true);

            // Act
            boolean result = customerService.deactivateAccount(1);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("Should activate account successfully")
        void shouldActivateAccountSuccessfully() {
            // Arrange
            when(customerRepository.existsById(1)).thenReturn(true);
            when(customerRepository.activate(1)).thenReturn(true);

            // Act
            boolean result = customerService.activateAccount(1);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("Should throw exception when deactivating non-existent customer")
        void shouldThrowWhenDeactivatingNonExistentCustomer() {
            // Arrange
            when(customerRepository.existsById(999)).thenReturn(false);

            // Act & Assert
            assertThrows(CustomerNotFoundException.class,
                    () -> customerService.deactivateAccount(999));
        }
    }

    @Nested
    @DisplayName("findAll tests")
    class FindAllTests {

        @Test
        @DisplayName("Should return all customers")
        void shouldReturnAllCustomers() {
            // Arrange
            List<Customer> customers = List.of(
                    createTestCustomer("John Doe", "john@example.com"),
                    createTestCustomer("Jane Doe", "jane@example.com"));
            when(customerRepository.findAll()).thenReturn(customers);

            // Act
            List<Customer> result = customerService.findAll();

            // Assert
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should return all active customers")
        void shouldReturnAllActiveCustomers() {
            // Arrange
            Customer activeCustomer = createTestCustomer("John Doe", "john@example.com");
            when(customerRepository.findAllActive()).thenReturn(List.of(activeCustomer));

            // Act
            List<Customer> result = customerService.findAllActive();

            // Assert
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("searchByName tests")
    class SearchByNameTests {

        @Test
        @DisplayName("Should search customers by name")
        void shouldSearchCustomersByName() {
            // Arrange
            Customer customer = createTestCustomer("John Doe", "john@example.com");
            when(customerRepository.findByNameContaining("John")).thenReturn(List.of(customer));

            // Act
            List<Customer> result = customerService.searchByName("John");

            // Assert
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return empty list for null search term")
        void shouldReturnEmptyForNullSearchTerm() {
            // Act
            List<Customer> result = customerService.searchByName(null);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("statistics tests")
    class StatisticsTests {

        @Test
        @DisplayName("Should return customer statistics")
        void shouldReturnCustomerStatistics() {
            // Arrange
            CustomerRepository.CustomerStatistics repoStats = new CustomerRepository.CustomerStatistics(100, 95, 10);
            when(customerRepository.getStatistics()).thenReturn(repoStats);

            // Act
            CustomerStatistics result = customerService.getStatistics();

            // Assert
            assertEquals(100, result.totalCustomers());
            assertEquals(95, result.activeCustomers());
            assertEquals(10, result.newCustomersThisMonth());
        }

        @Test
        @DisplayName("Should return customer count")
        void shouldReturnCustomerCount() {
            // Arrange
            when(customerRepository.count()).thenReturn(100L);

            // Act
            long result = customerService.getCustomerCount();

            // Assert
            assertEquals(100L, result);
        }
    }

    @Nested
    @DisplayName("availability checks")
    class AvailabilityCheckTests {

        @Test
        @DisplayName("Should return true when email is available")
        void shouldReturnTrueWhenEmailAvailable() {
            // Arrange
            when(customerRepository.existsByEmail("new@example.com")).thenReturn(false);

            // Act
            boolean result = customerService.isEmailAvailable("new@example.com");

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false when email is taken")
        void shouldReturnFalseWhenEmailTaken() {
            // Arrange
            when(customerRepository.existsByEmail("existing@example.com")).thenReturn(true);

            // Act
            boolean result = customerService.isEmailAvailable("existing@example.com");

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return true when phone is available")
        void shouldReturnTrueWhenPhoneAvailable() {
            // Arrange
            when(customerRepository.existsByPhone("+94771234567")).thenReturn(false);

            // Act
            boolean result = customerService.isPhoneAvailable("+94771234567");

            // Assert
            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("pagination tests")
    class PaginationTests {

        @Test
        @DisplayName("Should return paginated customers")
        void shouldReturnPaginatedCustomers() {
            // Arrange
            List<Customer> customers = List.of(createTestCustomer("John Doe", "john@example.com"));
            when(customerRepository.findAll(0, 10)).thenReturn(customers);

            // Act
            List<Customer> result = customerService.findAll(0, 10);

            // Assert
            assertEquals(1, result.size());
            verify(customerRepository).findAll(0, 10);
        }

        @Test
        @DisplayName("Should calculate correct offset for page")
        void shouldCalculateCorrectOffset() {
            // Arrange
            when(customerRepository.findAll(20, 10)).thenReturn(List.of());

            // Act
            customerService.findAll(2, 10);

            // Assert
            verify(customerRepository).findAll(20, 10);
        }
    }

    @Nested
    @DisplayName("role management tests")
    class RoleManagementTests {

        @Test
        @DisplayName("Should create user with role successfully")
        void shouldCreateUserWithRoleSuccessfully() {
            // Arrange
            when(customerRepository.existsByEmail("admin@example.com")).thenReturn(false);
            when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
                Customer saved = invocation.getArgument(0);
                saved.setCustomerId(1);
                return saved;
            });

            // Act
            Customer result = customerService.createUserWithRole(
                    "Admin User",
                    "admin@example.com",
                    "+94771234567",
                    "123 Admin St",
                    "password123",
                    UserRole.ADMIN);

            // Assert
            assertNotNull(result);
            assertEquals(UserRole.ADMIN, result.getRole());
            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should fail to create user with null role")
        void shouldFailToCreateUserWithNullRole() {
            // Act & Assert
            assertThrows(ValidationException.class,
                    () -> customerService.createUserWithRole(
                            "User",
                            "user@example.com",
                            null,
                            "Address",
                            "password",
                            null));
        }

        @Test
        @DisplayName("Should update user role successfully")
        void shouldUpdateUserRoleSuccessfully() {
            // Arrange
            Customer customer = createTestCustomer("John Doe", "john@example.com");
            customer.setRole(UserRole.CUSTOMER);
            when(customerRepository.existsById(1)).thenReturn(true);
            when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
            // Since mock is NOT CustomerRepositoryImpl, it goes to fallback.
            when(customerRepository.save(any(Customer.class))).thenReturn(customer);

            // Act
            boolean result = customerService.updateUserRole(1, UserRole.CASHIER);

            // Assert
            assertTrue(result);
            assertEquals(UserRole.CASHIER, customer.getRole());
            verify(customerRepository).save(customer);
        }

        @Test
        @DisplayName("Should fail update user role for non-existent customer")
        void shouldFailUpdateUserRoleForNonExistentCustomer() {
            when(customerRepository.existsById(999)).thenReturn(false);
            assertThrows(CustomerNotFoundException.class,
                    () -> customerService.updateUserRole(999, UserRole.ADMIN));
        }

        @Test
        @DisplayName("Should find users by role (fallback logic)")
        void shouldFindUsersByRole() {
            // Arrange
            Customer admin = createTestCustomer("Admin", "admin@example.com");
            admin.setRole(UserRole.ADMIN);
            Customer customer = createTestCustomer("User", "user@example.com");
            customer.setRole(UserRole.CUSTOMER);

            when(customerRepository.findAll()).thenReturn(List.of(admin, customer));

            // Act
            List<Customer> admins = customerService.findByRole(UserRole.ADMIN);
            List<Customer> customers = customerService.findByRole(UserRole.CUSTOMER);

            // Assert
            assertEquals(1, admins.size());
            assertEquals("Admin", admins.get(0).getCustomerName());

            assertEquals(1, customers.size());
            assertEquals("User", customers.get(0).getCustomerName());
        }

        @Test
        @DisplayName("Should return empty list for null role")
        void shouldReturnEmptyListForNullRole() {
            // Act
            List<Customer> result = customerService.findByRole(null);

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should fail update user role for null role")
        void shouldFailUpdateUserRoleForNullRole() {
            when(customerRepository.existsById(1)).thenReturn(true);
            assertThrows(ValidationException.class,
                    () -> customerService.updateUserRole(1, null));
        }

        @Test
        @DisplayName("Should update user role using RepositoryImpl optimization")
        void shouldUpdateUserRoleUsingRepositoryImpl() {
            // Arrange
            CustomerRepositoryImpl mockRepoImpl = mock(CustomerRepositoryImpl.class);
            CustomerServiceImpl serviceWithImpl = new CustomerServiceImpl(mockRepoImpl);

            when(mockRepoImpl.existsById(1)).thenReturn(true);
            when(mockRepoImpl.updateRole(1, UserRole.ADMIN)).thenReturn(true);

            // Act
            boolean result = serviceWithImpl.updateUserRole(1, UserRole.ADMIN);

            // Assert
            assertTrue(result);
            verify(mockRepoImpl).updateRole(1, UserRole.ADMIN);
            // Verify fallback not used
            verify(mockRepoImpl, never()).save(any(Customer.class));
        }
    }
}
