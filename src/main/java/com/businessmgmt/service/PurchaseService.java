package com.businessmgmt.service;

import com.businessmgmt.dao.ProductDao;
import com.businessmgmt.dao.PurchaseDao;
import com.businessmgmt.exception.BusinessException;
import com.businessmgmt.exception.ValidationException;
import com.businessmgmt.model.Purchase;
import com.businessmgmt.model.PurchaseItem;
import com.businessmgmt.util.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service layer for purchase management operations.
 * Validates purchase data and delegates persistence to PurchaseDao.
 */
public class PurchaseService {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);

    private final PurchaseDao purchaseDao;
    private final ProductDao productDao;

    public PurchaseService() {
        this.purchaseDao = new PurchaseDao();
        this.productDao = new ProductDao();
    }

    /**
     * Retrieves all purchases.
     */
    public List<Purchase> getAllPurchases() {
        logger.info("Fetching all purchases");
        return purchaseDao.findAll();
    }

    /**
     * Retrieves a purchase by its ID.
     */
    public Purchase getPurchase(int id) {
        logger.info("Fetching purchase with id: " + id);
        Purchase purchase = purchaseDao.findById(id);
        if (purchase == null) {
            throw new BusinessException("Purchase not found with id: " + id);
        }
        return purchase;
    }

    /**
     * Creates a new purchase after validating items, quantities, and prices.
     * Delegates to PurchaseDao.save() which handles the transaction
     * (inserting purchase, items, and updating product stock).
     */
    public void createPurchase(Purchase purchase) {
        logger.info("Creating new purchase");
        validatePurchase(purchase);
        purchaseDao.save(purchase);
        logger.info("Purchase created successfully with id: " + purchase.getId());
    }

    /**
     * Retrieves purchases from a specific supplier.
     */
    public List<Purchase> getBySupplier(int supplierId) {
        logger.info("Fetching purchases for supplier id: " + supplierId);
        return purchaseDao.findBySupplier(supplierId);
    }

    public List<Purchase> getPurchasesBySupplier(int supplierId) {
        return getBySupplier(supplierId);
    }

    /**
     * Retrieves purchases within a date range.
     */
    public List<Purchase> getByDateRange(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching purchases from " + startDate + " to " + endDate);
        if (startDate == null || endDate == null) {
            throw new ValidationException("Start date and end date are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date cannot be after end date");
        }
        return purchaseDao.findByDateRange(startDate, endDate);
    }

    public List<Purchase> getPurchasesByDateRange(LocalDate startDate, LocalDate endDate) {
        return getByDateRange(startDate, endDate);
    }

    // ==================== Validation ====================

    private void validatePurchase(Purchase purchase) {
        List<PurchaseItem> items = purchase.getItems();
        if (items == null || items.isEmpty()) {
            throw new ValidationException("Purchase must have at least one item");
        }

        for (PurchaseItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new ValidationException("Item quantity must be positive");
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Item unit price must be positive");
            }
        }
    }
}
