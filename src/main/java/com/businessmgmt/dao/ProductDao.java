package com.businessmgmt.dao;

import com.businessmgmt.exception.DatabaseException;
import com.businessmgmt.model.Product;
import com.businessmgmt.util.DatabaseConnection;
import com.businessmgmt.util.LoggerFactory;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class ProductDao implements GenericDao<Product> {

    private static final Logger logger = LoggerFactory.getLogger(ProductDao.class);

    private static final String SELECT_WITH_CATEGORY =
            "SELECT p.id, p.name, p.category_id, c.name AS category_name, p.description, " +
            "p.purchase_price, p.selling_price, p.quantity, p.min_stock, p.created_at, p.updated_at " +
            "FROM products p LEFT JOIN categories c ON p.category_id = c.id";

    @Override
    public List<Product> findAll() {
        String sql = SELECT_WITH_CATEGORY + " ORDER BY p.name";
        List<Product> products = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error finding all products: " + e.getMessage());
            throw new DatabaseException("Error finding all products", e);
        }
        return products;
    }

    @Override
    public Product findById(int id) {
        String sql = SELECT_WITH_CATEGORY + " WHERE p.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.severe("Error finding product by id " + id + ": " + e.getMessage());
            throw new DatabaseException("Error finding product by id: " + id, e);
        }
        return null;
    }

    public List<Product> findByCategory(int categoryId) {
        String sql = SELECT_WITH_CATEGORY + " WHERE p.category_id = ? ORDER BY p.name";
        List<Product> products = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error finding products by category " + categoryId + ": " + e.getMessage());
            throw new DatabaseException("Error finding products by category: " + categoryId, e);
        }
        return products;
    }

    public List<Product> findLowStock() {
        String sql = SELECT_WITH_CATEGORY + " WHERE p.quantity <= p.min_stock ORDER BY p.quantity ASC";
        List<Product> products = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error finding low stock products: " + e.getMessage());
            throw new DatabaseException("Error finding low stock products", e);
        }
        return products;
    }

    public void updateStock(int productId, int quantityChange) {
        String sql = "UPDATE products SET quantity = quantity + ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantityChange);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
            logger.info("Updated stock for product " + productId + " by " + quantityChange);
        } catch (SQLException e) {
            logger.severe("Error updating stock for product " + productId + ": " + e.getMessage());
            throw new DatabaseException("Error updating stock for product: " + productId, e);
        }
    }

    public List<Product> search(String keyword) {
        String sql = SELECT_WITH_CATEGORY + " WHERE LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ? ORDER BY p.name";
        List<Product> products = new ArrayList<>();
        String pattern = "%" + keyword.toLowerCase() + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error searching products: " + e.getMessage());
            throw new DatabaseException("Error searching products", e);
        }
        return products;
    }

    @Override
    public Product save(Product product) {
        String sql = "INSERT INTO products (name, category_id, description, purchase_price, selling_price, quantity, min_stock) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getName());
            stmt.setInt(2, product.getCategoryId());
            stmt.setString(3, product.getDescription());
            stmt.setBigDecimal(4, product.getPurchasePrice());
            stmt.setBigDecimal(5, product.getSellingPrice());
            stmt.setInt(6, product.getQuantity());
            stmt.setInt(7, product.getMinStock());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    product.setId(keys.getInt(1));
                }
            }
            logger.info("Saved product: " + product.getName());
        } catch (SQLException e) {
            logger.severe("Error saving product: " + e.getMessage());
            throw new DatabaseException("Error saving product", e);
        }
        return product;
    }

    @Override
    public Product update(Product product) {
        String sql = "UPDATE products SET name = ?, category_id = ?, description = ?, purchase_price = ?, " +
                     "selling_price = ?, quantity = ?, min_stock = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setInt(2, product.getCategoryId());
            stmt.setString(3, product.getDescription());
            stmt.setBigDecimal(4, product.getPurchasePrice());
            stmt.setBigDecimal(5, product.getSellingPrice());
            stmt.setInt(6, product.getQuantity());
            stmt.setInt(7, product.getMinStock());
            stmt.setInt(8, product.getId());
            stmt.executeUpdate();
            logger.info("Updated product: " + product.getName());
        } catch (SQLException e) {
            logger.severe("Error updating product: " + e.getMessage());
            throw new DatabaseException("Error updating product", e);
        }
        return product;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                logger.info("Deleted product with id: " + id);
                return true;
            }
        } catch (SQLException e) {
            logger.severe("Error deleting product: " + e.getMessage());
            throw new DatabaseException("Error deleting product", e);
        }
        return false;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setName(rs.getString("name"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setCategoryName(rs.getString("category_name"));
        product.setDescription(rs.getString("description"));
        product.setPurchasePrice(rs.getBigDecimal("purchase_price"));
        product.setSellingPrice(rs.getBigDecimal("selling_price"));
        product.setQuantity(rs.getInt("quantity"));
        product.setMinStock(rs.getInt("min_stock"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            product.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            product.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return product;
    }
}
