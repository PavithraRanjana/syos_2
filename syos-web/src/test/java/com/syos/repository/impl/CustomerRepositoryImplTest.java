package com.syos.repository.impl;

import com.syos.domain.enums.UserRole;
import com.syos.domain.models.Customer;
import com.syos.exception.RepositoryException;
import com.syos.repository.interfaces.CustomerRepository.CustomerStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomerRepositoryImpl using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerRepositoryImplTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private CustomerRepositoryImpl repository;

    @BeforeEach
    void setUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        repository = new CustomerRepositoryImpl(dataSource);
    }

    private void mockCustomerResultSet(int id, String name, String email, UserRole role) throws SQLException {
        when(resultSet.getInt("customer_id")).thenReturn(id);
        when(resultSet.getString("customer_name")).thenReturn(name);
        when(resultSet.getString("email")).thenReturn(email);
        when(resultSet.getString("phone")).thenReturn("1234567890");
        when(resultSet.getString("address")).thenReturn("123 Main St");
        when(resultSet.getString("password_hash")).thenReturn("hashedpassword");
        when(resultSet.getString("role")).thenReturn(role.name());
        when(resultSet.getDate("registration_date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(resultSet.getBoolean("is_active")).thenReturn(true);
        when(resultSet.getTimestamp("created_at")).thenReturn(null);
        when(resultSet.getTimestamp("updated_at")).thenReturn(null);
    }

    @Nested
    @DisplayName("save tests")
    class SaveTests {

        @Test
        @DisplayName("Should insert new customer")
        void shouldInsertNewCustomer() throws Exception {
            Customer customer = new Customer();
            customer.setCustomerName("John Doe");
            customer.setEmail("john@test.com");
            customer.setPhone("1234567890");
            customer.setRole(UserRole.CUSTOMER);
            customer.setActive(true);
            customer.setRegistrationDate(LocalDate.now());

            when(preparedStatement.executeUpdate()).thenReturn(1);
            ResultSet generatedKeys = mock(ResultSet.class);
            when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
            when(generatedKeys.next()).thenReturn(true);
            when(generatedKeys.getInt(1)).thenReturn(1);

            Customer saved = repository.save(customer);

            assertEquals(1, saved.getCustomerId());
        }

        @Test
        @DisplayName("Should update existing customer")
        void shouldUpdateExistingCustomer() throws Exception {
            Customer customer = new Customer();
            customer.setCustomerId(1);
            customer.setCustomerName("John Doe Updated");
            customer.setEmail("john@test.com");
            customer.setRole(UserRole.CUSTOMER);
            customer.setActive(true);

            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(1);
            when(preparedStatement.executeUpdate()).thenReturn(1);

            Customer updated = repository.save(customer);

            assertEquals(1, updated.getCustomerId());
        }
    }

    @Nested
    @DisplayName("findById tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should find customer by ID")
        void shouldFindCustomerById() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockCustomerResultSet(1, "John Doe", "john@test.com", UserRole.CUSTOMER);

            Optional<Customer> result = repository.findById(1);

            assertTrue(result.isPresent());
            assertEquals("John Doe", result.get().getCustomerName());
        }

        @Test
        @DisplayName("Should return empty when customer not found")
        void shouldReturnEmptyWhenNotFound() throws Exception {
            when(resultSet.next()).thenReturn(false);

            Optional<Customer> result = repository.findById(999);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findByEmail tests")
    class FindByEmailTests {

        @Test
        @DisplayName("Should find customer by email")
        void shouldFindCustomerByEmail() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockCustomerResultSet(1, "John Doe", "john@test.com", UserRole.CUSTOMER);

            Optional<Customer> result = repository.findByEmail("john@test.com");

            assertTrue(result.isPresent());
            assertEquals("john@test.com", result.get().getEmail());
        }
    }

    @Nested
    @DisplayName("findByPhone tests")
    class FindByPhoneTests {

        @Test
        @DisplayName("Should find customer by phone")
        void shouldFindCustomerByPhone() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockCustomerResultSet(1, "John Doe", "john@test.com", UserRole.CUSTOMER);

            Optional<Customer> result = repository.findByPhone("1234567890");

            assertTrue(result.isPresent());
        }
    }

    @Nested
    @DisplayName("existsByEmail tests")
    class ExistsByEmailTests {

        @Test
        @DisplayName("Should return true when email exists")
        void shouldReturnTrueWhenEmailExists() throws Exception {
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(1);

            boolean exists = repository.existsByEmail("john@test.com");

            assertTrue(exists);
        }
    }

    @Nested
    @DisplayName("findAllActive tests")
    class FindAllActiveTests {

        @Test
        @DisplayName("Should find all active customers")
        void shouldFindAllActiveCustomers() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockCustomerResultSet(1, "John Doe", "john@test.com", UserRole.CUSTOMER);

            List<Customer> result = repository.findAllActive();

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("findByNameContaining tests")
    class FindByNameContainingTests {

        @Test
        @DisplayName("Should find customers by name part")
        void shouldFindCustomersByNamePart() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockCustomerResultSet(1, "John Doe", "john@test.com", UserRole.CUSTOMER);

            List<Customer> result = repository.findByNameContaining("John");

            assertEquals(1, result.size());
            verify(preparedStatement).setString(1, "%John%");
        }
    }

    @Nested
    @DisplayName("deactivate/activate tests")
    class DeactivateActivateTests {

        @Test
        @DisplayName("Should deactivate customer")
        void shouldDeactivateCustomer() throws Exception {
            when(preparedStatement.executeUpdate()).thenReturn(1);

            boolean result = repository.deactivate(1);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should activate customer")
        void shouldActivateCustomer() throws Exception {
            when(preparedStatement.executeUpdate()).thenReturn(1);

            boolean result = repository.activate(1);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("updatePassword tests")
    class UpdatePasswordTests {

        @Test
        @DisplayName("Should update password")
        void shouldUpdatePassword() throws Exception {
            when(preparedStatement.executeUpdate()).thenReturn(1);

            boolean result = repository.updatePassword(1, "newHashedPassword");

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("getStatistics tests")
    class GetStatisticsTests {

        @Test
        @DisplayName("Should return customer statistics")
        void shouldReturnCustomerStatistics() throws Exception {
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getLong("total")).thenReturn(100L);
            when(resultSet.getLong("active")).thenReturn(90L);
            when(resultSet.getLong("new_this_month")).thenReturn(10L);

            CustomerStatistics stats = repository.getStatistics();

            assertEquals(100L, stats.totalCustomers());
            assertEquals(90L, stats.activeCustomers());
            assertEquals(10L, stats.newCustomersThisMonth());
        }
    }

    @Nested
    @DisplayName("findByRole tests")
    class FindByRoleTests {

        @Test
        @DisplayName("Should find customers by role")
        void shouldFindCustomersByRole() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockCustomerResultSet(1, "Admin User", "admin@test.com", UserRole.ADMIN);

            List<Customer> result = repository.findByRole(UserRole.ADMIN);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("updateRole tests")
    class UpdateRoleTests {

        @Test
        @DisplayName("Should update customer role")
        void shouldUpdateCustomerRole() throws Exception {
            when(preparedStatement.executeUpdate()).thenReturn(1);

            boolean result = repository.updateRole(1, UserRole.MANAGER);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("error handling tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw RepositoryException on SQL error")
        void shouldThrowRepositoryExceptionOnSqlError() throws Exception {
            when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));

            assertThrows(RepositoryException.class, () -> repository.findAll());
        }
    }
}
