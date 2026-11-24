package com.businessmgmt.dao;

import com.businessmgmt.exception.DatabaseException;
import com.businessmgmt.exception.InsufficientStockException;
import com.businessmgmt.model.Sale;
import com.businessmgmt.model.SaleItem;
import com.businessmgmt.model.StockMovement;
import com.businessmgmt.util.DatabaseConnection;
import com.businessmgmt.util.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Data Access Object for Sale transactions.
 * Manages database transactions for sales, line items, stock validation, and inventory deduction.
 */
public class SaleDao implements GenericDao<Sale> {

    private static final Logger logger = LoggerFactory.getLogger(SaleDao.class);
    private final StockMovementDao stockMovementDao = new StockMovementDao();

    private static final String SELECT_WITH_CUSTOMER =
            "SELECT s.id, s.customer_id, c.name AS customer_name, s.sale_date, " +
            "s.subtotal, s.discount_percent, s.total_amount, s.notes, s.created_at " +
            "FROM sales s " +
            "LEFT JOIN customers c ON s.customer_id = c.id";

    @Override
    public List<Sale> findAll() {
        String sql = SELECT_WITH_CUSTOMER + " ORDER BY s.sale_date DESC, s.id DESC";
        List<Sale> sales = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                sales.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error finding all sales: " + e.getMessage());
            throw new DatabaseException("Error finding all sales", e);
        }
        return sales;
    }

    @Override
    public Sale findById(int id) {
        String sql = SELECT_WITH_CUSTOMER + " WHERE s.id = ?";
        Sale sale = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    sale = mapRow(rs);
                    sale.setItems(findItemsBySaleId(conn, id));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error finding sale by id " + id + ": " + e.getMessage());
            throw new DatabaseException("Error finding sale by id: " + id, e);
        }
        return sale;
    }

    @Override
    public Sale save(Sale sale) {
        String checkStockSql = "SELECT name, quantity FROM products WHERE id = ? FOR UPDATE";
        String insertSaleSql = "INSERT INTO sales (customer_id, sale_date, subtotal, discount_percent, total_amount, notes) " +
                               "VALUES (?, ?, ?, ?, ?, ?)";
        String insertItemSql = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, total_price) " +
                               "VALUES (?, ?, ?, ?, ?)";
        String updateStockSql = "UPDATE products SET quantity = quantity - ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Verify stock availability for all items with row locking
            try (PreparedStatement checkStmt = conn.prepareStatement(checkStockSql)) {
                for (SaleItem item : sale.getItems()) {
                    checkStmt.setInt(1, item.getProductId());
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            String productName = rs.getString("name");
                            int currentStock = rs.getInt("quantity");
                            if (currentStock < item.getQuantity()) {
                                throw new InsufficientStockException(productName, item.getQuantity(), currentStock);
                            }
                        } else {
                            throw new DatabaseException("Product not found with id: " + item.getProductId());
                        }
                    }
                }
            }

            // 2. Insert Sale
            try (PreparedStatement stmt = conn.prepareStatement(insertSaleSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, sale.getCustomerId());
                stmt.setDate(2, Date.valueOf(sale.getSaleDate()));
                stmt.setBigDecimal(3, sale.getSubtotal());
                stmt.setBigDecimal(4, sale.getDiscountPercent());
                stmt.setBigDecimal(5, sale.getTotalAmount());
                stmt.setString(6, sale.getNotes());

                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        sale.setId(rs.getInt(1));
                    }
                }
            }

            // 3. Insert items, deduct inventory, record stock movements
            try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement stockStmt = conn.prepareStatement(updateStockSql)) {

                for (SaleItem item : sale.getItems()) {
                    item.setSaleId(sale.getId());

                    itemStmt.setInt(1, item.getSaleId());
                    itemStmt.setInt(2, item.getProductId());
                    itemStmt.setInt(3, item.getQuantity());
                    itemStmt.setBigDecimal(4, item.getUnitPrice());
                    itemStmt.setBigDecimal(5, item.getTotalPrice());
                    itemStmt.executeUpdate();

                    try (ResultSet rs = itemStmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            item.setId(rs.getInt(1));
                        }
                    }

                    // Decrement product quantity
                    stockStmt.setInt(1, item.getQuantity());
                    stockStmt.setInt(2, item.getProductId());
                    stockStmt.executeUpdate();

                    // Record stock movement
                    StockMovement sm = new StockMovement();
                    sm.setProductId(item.getProductId());
                    sm.setMovementType("SALE");
                    sm.setQuantity(-item.getQuantity());
                    sm.setReferenceType("SALE");
                    sm.setReferenceId(sale.getId());
                    sm.setNotes("Sale #" + sale.getId());
                    stockMovementDao.save(sm, conn);
                }
            }

            conn.commit(); // Commit transaction
            logger.info("Sale transaction committed successfully: #" + sale.getId());
        } catch (InsufficientStockException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.warning("Sale transaction rolled back due to insufficient stock: " + e.getMessage());
                } catch (SQLException ex) {
                    logger.severe("Rollback error: " + ex.getMessage());
                }
            }
            throw e;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.warning("Sale transaction rolled back due to database error");
                } catch (SQLException ex) {
                    logger.severe("Rollback error: " + ex.getMessage());
                }
            }
            logger.severe("Error saving sale: " + e.getMessage());
            throw new DatabaseException("Error saving sale transaction", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.severe("Error resetting auto-commit: " + e.getMessage());
                }
            }
        }
        return sale;
    }

    @Override
    public Sale update(Sale sale) {
        String sql = "UPDATE sales SET notes = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sale.getNotes());
            stmt.setInt(2, sale.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Error updating sale " + sale.getId() + ": " + e.getMessage());
            throw new DatabaseException("Error updating sale", e);
        }
        return sale;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM sales WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Error deleting sale " + id + ": " + e.getMessage());
            throw new DatabaseException("Error deleting sale: " + id, e);
        }
    }

    public List<Sale> findByCustomer(int customerId) {
        String sql = SELECT_WITH_CUSTOMER + " WHERE s.customer_id = ? ORDER BY s.sale_date DESC";
        List<Sale> sales = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sales.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error finding sales by customer: " + e.getMessage());
            throw new DatabaseException("Error finding sales by customer", e);
        }
        return sales;
    }

    public List<Sale> findByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = SELECT_WITH_CUSTOMER + " WHERE s.sale_date BETWEEN ? AND ? ORDER BY s.sale_date DESC";
        List<Sale> sales = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sales.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error finding sales by date range: " + e.getMessage());
            throw new DatabaseException("Error finding sales by date range", e);
        }
        return sales;
    }

    private List<SaleItem> findItemsBySaleId(Connection conn, int saleId) throws SQLException {
        String sql = "SELECT si.id, si.sale_id, si.product_id, p.name AS product_name, " +
                     "si.quantity, si.unit_price, si.total_price " +
                     "FROM sale_items si " +
                     "LEFT JOIN products p ON si.product_id = p.id " +
                     "WHERE si.sale_id = ?";
        List<SaleItem> items = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, saleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SaleItem item = new SaleItem();
                    item.setId(rs.getInt("id"));
                    item.setSaleId(rs.getInt("sale_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setTotalPrice(rs.getBigDecimal("total_price"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private Sale mapRow(ResultSet rs) throws SQLException {
        Sale s = new Sale();
        s.setId(rs.getInt("id"));
        s.setCustomerId(rs.getInt("customer_id"));
        s.setCustomerName(rs.getString("customer_name"));
        Date d = rs.getDate("sale_date");
        if (d != null) {
            s.setSaleDate(d.toLocalDate());
        }
        s.setSubtotal(rs.getBigDecimal("subtotal"));
        s.setDiscountPercent(rs.getBigDecimal("discount_percent"));
        s.setTotalAmount(rs.getBigDecimal("total_amount"));
        s.setNotes(rs.getString("notes"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            s.setCreatedAt(ts.toLocalDateTime());
        }
        return s;
    }
}

