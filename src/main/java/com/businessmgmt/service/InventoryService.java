package com.businessmgmt.service;

import com.businessmgmt.dao.CategoryDao;
import com.businessmgmt.dao.ProductDao;
import com.businessmgmt.dao.StockMovementDao;
import com.businessmgmt.exception.BusinessException;
import com.businessmgmt.exception.ValidationException;
import com.businessmgmt.model.Category;
import com.businessmgmt.model.Product;
import com.businessmgmt.model.StockMovement;
import com.businessmgmt.util.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service layer for inventory management operations.
 * Handles products, categories, and stock movements with validation.
 */
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    private final ProductDao productDao;
    private final CategoryDao categoryDao;
    private final StockMovementDao stockMovementDao;

    public InventoryService() {
        this.productDao = new ProductDao();
        this.categoryDao = new CategoryDao();
        this.stockMovementDao = new StockMovementDao();
    }

    // ==================== Product Operations ====================

    /**
     * Retrieves all products.
     */
    public List<Product> getAllProducts() {
        logger.info("Fetching all products");
        return productDao.findAll();
    }

    /**
     * Retrieves a product by its ID.
     */
    public Product getProduct(int id) {
        logger.info("Fetching product with id: " + id);
        Product product = productDao.findById(id);
        if (product == null) {
            throw new BusinessException("Product not found with id: " + id);
        }
        return product;
    }

    /**
     * Saves a new product after validation.
     */
    public void saveProduct(Product product) {
        logger.info("Saving new product: " + product.getName());
        validateProduct(product);
        productDao.save(product);
        logger.info("Product saved successfully: " + product.getName());
    }

    /**
     * Updates an existing product after validation.
     */
    public void updateProduct(Product product) {
        logger.info("Updating product id: " + product.getId());
        validateProduct(product);
        productDao.update(product);
        logger.info("Product updated successfully: " + product.getId());
    }

    /**
     * Deletes a product by its ID.
     */
    public void deleteProduct(int id) {
        logger.info("Deleting product id: " + id);
        productDao.delete(id);
        logger.info("Product deleted successfully: " + id);
    }

    /**
     * Retrieves products belonging to a specific category.
     */
    public List<Product> getProductsByCategory(int categoryId) {
        logger.info("Fetching products for category id: " + categoryId);
        return productDao.findByCategory(categoryId);
    }

    /**
     * Retrieves products whose stock is at or below the minimum threshold.
     */
    public List<Product> getLowStockProducts() {
        logger.info("Fetching low stock products");
        return productDao.findLowStock();
    }

    /**
     * Searches products by name or description.
     */
    public List<Product> searchProducts(String keyword) {
        logger.info("Searching products with keyword: " + keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }
        return productDao.search(keyword.trim());
    }

    // ==================== Category Operations ====================

    /**
     * Retrieves all categories.
     */
    public List<Category> getAllCategories() {
        logger.info("Fetching all categories");
        return categoryDao.findAll();
    }

    /**
     * Saves a new category after validation.
     */
    public void saveCategory(Category category) {
        logger.info("Saving new category: " + category.getName());
        validateCategory(category);
        categoryDao.save(category);
        logger.info("Category saved successfully: " + category.getName());
    }

    /**
     * Updates an existing category after validation.
     */
    public void updateCategory(Category category) {
        logger.info("Updating category id: " + category.getId());
        validateCategory(category);
        categoryDao.update(category);
        logger.info("Category updated successfully: " + category.getId());
    }

    /**
     * Deletes a category by its ID.
     */
    public void deleteCategory(int id) {
        logger.info("Deleting category id: " + id);
        categoryDao.delete(id);
        logger.info("Category deleted successfully: " + id);
    }

    // ==================== Stock Movement Operations ====================

    /**
     * Retrieves stock movements for a specific product.
     */
    public List<StockMovement> getStockMovements(int productId) {
        logger.info("Fetching stock movements for product id: " + productId);
        return stockMovementDao.findByProduct(productId);
    }

    // ==================== Validation ====================

    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new ValidationException("Product name cannot be empty");
        }
        if (product.getPurchasePrice() == null || product.getPurchasePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Purchase price must be positive");
        }
        if (product.getSellingPrice() == null || product.getSellingPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Selling price must be positive");
        }
        if (product.getSellingPrice().compareTo(product.getPurchasePrice()) < 0) {
            throw new ValidationException("Selling price cannot be less than purchase price");
        }
        if (product.getQuantity() < 0) {
            throw new ValidationException("Quantity cannot be negative");
        }
    }

    private void validateCategory(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new ValidationException("Category name cannot be empty");
        }
    }
}
