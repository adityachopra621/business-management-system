package com.businessmgmt.service;

import com.businessmgmt.dao.SupplierDao;
import com.businessmgmt.exception.BusinessException;
import com.businessmgmt.exception.ValidationException;
import com.businessmgmt.model.Supplier;
import com.businessmgmt.util.LoggerFactory;

import java.util.List;
import java.util.logging.Logger;

/**
 * Service layer for supplier management operations.
 */
public class SupplierService {

    private static final Logger logger = LoggerFactory.getLogger(SupplierService.class);

    private final SupplierDao supplierDao;

    public SupplierService() {
        this.supplierDao = new SupplierDao();
    }

    /**
     * Retrieves all suppliers.
     */
    public List<Supplier> getAllSuppliers() {
        logger.info("Fetching all suppliers");
        return supplierDao.findAll();
    }

    /**
     * Retrieves a supplier by its ID.
     */
    public Supplier getSupplier(int id) {
        logger.info("Fetching supplier with id: " + id);
        Supplier supplier = supplierDao.findById(id);
        if (supplier == null) {
            throw new BusinessException("Supplier not found with id: " + id);
        }
        return supplier;
    }

    /**
     * Saves a new supplier after validation.
     */
    public void saveSupplier(Supplier supplier) {
        logger.info("Saving new supplier: " + supplier.getName());
        validateSupplier(supplier);
        supplierDao.save(supplier);
        logger.info("Supplier saved successfully: " + supplier.getName());
    }

    /**
     * Updates an existing supplier after validation.
     */
    public void updateSupplier(Supplier supplier) {
        logger.info("Updating supplier id: " + supplier.getId());
        validateSupplier(supplier);
        supplierDao.update(supplier);
        logger.info("Supplier updated successfully: " + supplier.getId());
    }

    /**
     * Deletes a supplier by its ID.
     */
    public void deleteSupplier(int id) {
        logger.info("Deleting supplier id: " + id);
        supplierDao.delete(id);
        logger.info("Supplier deleted successfully: " + id);
    }

    /**
     * Searches suppliers by name, contact person, email, or phone.
     */
    public List<Supplier> searchSuppliers(String keyword) {
        logger.info("Searching suppliers with keyword: " + keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllSuppliers();
        }
        return supplierDao.search(keyword.trim());
    }

    // ==================== Validation ====================

    private void validateSupplier(Supplier supplier) {
        if (supplier.getName() == null || supplier.getName().trim().isEmpty()) {
            throw new ValidationException("Supplier name cannot be empty");
        }
        if (supplier.getPhone() == null || !supplier.getPhone().trim().matches("^[0-9]{10}$")) {
            throw new ValidationException("Phone number must be exactly 10 digits and numbers only");
        }
    }
}
