package com.syos.util;

import com.syos.config.DataSourceConfig;
import com.syos.domain.enums.UserRole;
import com.syos.domain.models.Customer;
import com.syos.repository.impl.CustomerRepositoryImpl;
import com.syos.repository.interfaces.CustomerRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class ManagerPromoterTest {

    @Test
    public void promoteUserToManager() {
        CustomerRepository customerRepository = new CustomerRepositoryImpl(DataSourceConfig.getDataSource());
        Optional<Customer> customerOpt = customerRepository.findByEmail("manager_test@syos.com");

        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            customer.setRole(UserRole.MANAGER);
            customerRepository.save(customer);
            System.out.println("User " + customer.getEmail() + " promoted to " + customer.getRole());
        } else {
            System.err.println("User manager_test@syos.com not found!");
        }
    }
}
