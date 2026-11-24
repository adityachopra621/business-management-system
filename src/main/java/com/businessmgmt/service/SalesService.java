package com.businessmgmt.service;

import com.businessmgmt.dao.ProductDao;
import com.businessmgmt.dao.SaleDao;
import com.businessmgmt.exception.BusinessException;
import com.businessmgmt.exception.InsufficientStockException;
import com.businessmgmt.exception.ValidationException;
import com.businessmgmt.model.Product;
import com.businessmgmt.model.Sale;
import com.businessmgmt.model.SaleItem;
import com.businessmgmt.util.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service layer for sales management operations.
 * Validates sales data, checks stock availability, and delegates persistence to SaleDao.
 */
public class SalesService {

    private static final Logger logger = LoggerFactory.getLogger(SalesService.class);

    private final SaleDao saleDao;
    private final ProductDao productDao;

    public SalesService() {
        this.saleDao = new SaleDao();
        this.productDao = new ProductDao();
    }

    /**
     * Retrieves all sales.
     */
    public List<Sale> getAllSales() {
        logger.info("Fetching all sales");
        return saleDao.findAll();
    }

    /**
     * Retrieves a sale by its ID.
     */
    public Sale getSale(int id) {
        logger.info("Fetching sale with id: " + id);
        Sale sale = saleDao.findById(id);
        if (sale == null) {
            throw new BusinessException("Sale not found with id: " + id);
        }
        return sale;
    }

    /**
     * Creates a new sale after validating items and checking stock availability.
     * Delegates to SaleDao.save() which handles the transaction
     * (inserting sale, items, updating stock, and recording stock movements).
     */
    public void createSale(Sale sale) {
        logger.info("Creating new sale");
        validateSale(sale);
        checkStockAvailability(sale);
        saleDao.save(sale);
        logger.info("Sale created successfully with id: " + sale.getId());
    }

    /**
     * Retrieves sales for a specific customer.
     */
    public List<Sale> getByCustomer(int customerId) {
        logger.info("Fetching sales for customer id: " + customerId);
        return saleDao.findByCustomer(customerId);
    }

    public List<Sale> getSalesByCustomer(int customerId) {
        return getByCustomer(customerId);
    }

    /**
     * Retrieves sales within a date range.
     */
    public List<Sale> getByDateRange(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching sales from " + startDate + " to " + endDate);
        if (startDate == null || endDate == null) {
            throw new ValidationException("Start date and end date are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date cannot be after end date");
        }
        return saleDao.findByDateRange(startDate, endDate);
    }

    public List<Sale> getSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        return getByDateRange(startDate, endDate);
    }

    // ==================== Validation ====================

    private void validateSale(Sale sale) {
        List<SaleItem> items = sale.getItems();
        if (items == null || items.isEmpty()) {
            throw new ValidationException("Sale must have at least one item");
        }

        for (SaleItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new ValidationException("Item quantity must be positive");
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Item unit price must be positive");
            }
        }
    }

    /**
     * Checks that sufficient stock is available for all sale items.
     */
    private void checkStockAvailability(Sale sale) {
        for (SaleItem item : sale.getItems()) {
            Product product = productDao.findById(item.getProductId());
            if (product == null) {
                throw new BusinessException("Product not found with id: " + item.getProductId());
            }
            if (product.getQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product '" + product.getName()
                                + "'. Available: " + product.getQuantity()
                                + ", Requested: " + item.getQuantity());
            }
        }
    }
}
