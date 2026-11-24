package com.businessmgmt.service;

import com.businessmgmt.dao.CustomerDao;
import com.businessmgmt.exception.BusinessException;
import com.businessmgmt.exception.ValidationException;
import com.businessmgmt.model.Customer;
import com.businessmgmt.util.LoggerFactory;

import java.util.List;
import java.util.logging.Logger;

/**
 * Service layer for customer management operations.
 */
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerDao customerDao;

    public CustomerService() {
        this.customerDao = new CustomerDao();
    }

    /**
     * Retrieves all customers.
     */
    public List<Customer> getAllCustomers() {
        logger.info("Fetching all customers");
        return customerDao.findAll();
    }

    /**
     * Retrieves a customer by its ID.
     */
    public Customer getCustomer(int id) {
        logger.info("Fetching customer with id: " + id);
        Customer customer = customerDao.findById(id);
        if (customer == null) {
            throw new BusinessException("Customer not found with id: " + id);
        }
        return customer;
    }

    /**
     * Saves a new customer after validation.
     */
    public void saveCustomer(Customer customer) {
        logger.info("Saving new customer: " + customer.getName());
        validateCustomer(customer);
        customerDao.save(customer);
        logger.info("Customer saved successfully: " + customer.getName());
    }

    /**
     * Updates an existing customer after validation.
     */
    public void updateCustomer(Customer customer) {
        logger.info("Updating customer id: " + customer.getId());
        validateCustomer(customer);
        customerDao.update(customer);
        logger.info("Customer updated successfully: " + customer.getId());
    }

    /**
     * Deletes a customer by its ID.
     */
    public void deleteCustomer(int id) {
        logger.info("Deleting customer id: " + id);
        customerDao.delete(id);
        logger.info("Customer deleted successfully: " + id);
    }

    /**
     * Searches customers by name, email, or phone.
     */
    public List<Customer> searchCustomers(String keyword) {
        logger.info("Searching customers with keyword: " + keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCustomers();
        }
        return customerDao.search(keyword.trim());
    }

    // ==================== Validation ====================

    private void validateCustomer(Customer customer) {
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            throw new ValidationException("Customer name cannot be empty");
        }
        if (customer.getPhone() == null || !customer.getPhone().trim().matches("^[0-9]{10}$")) {
            throw new ValidationException("Phone number must be exactly 10 digits and numbers only");
        }
    }
}
