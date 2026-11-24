package com.businessmgmt.dao;

import com.businessmgmt.exception.DatabaseException;
import com.businessmgmt.model.Purchase;
import com.businessmgmt.model.PurchaseItem;
import com.businessmgmt.model.StockMovement;
import com.businessmgmt.util.DatabaseConnection;
import com.businessmgmt.util.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Data Access Object for Purchase transactions.
 * Manages database transactions for purchases, items, and inventory updates.
 */
public class PurchaseDao implements GenericDao<Purchase> {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseDao.class);
    private final StockMovementDao stockMovementDao = new StockMovementDao();

    private static final String SELECT_WITH_SUPPLIER =
            "SELECT p.id, p.supplier_id, s.name AS supplier_name, p.purchase_date, " +
            "p.total_amount, p.payment_status, p.notes, p.created_at " +
            "FROM purchases p " +
            "LEFT JOIN suppliers s ON p.supplier_id = s.id";

    @Override
    public List<Purchase> findAll() {
        String sql = SELECT_WITH_SUPPLIER + " ORDER BY p.purchase_date DESC, p.id DESC";
        List<Purchase> purchases = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                purchases.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error finding all purchases: " + e.getMessage());
            throw new DatabaseException("Error finding all purchases", e);
        }
        return purchases;
    }

    @Override
    public Purchase findById(int id) {
        String sql = SELECT_WITH_SUPPLIER + " WHERE p.id = ?";
        Purchase purchase = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    purchase = mapRow(rs);
                    purchase.setItems(findItemsByPurchaseId(conn, id));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error finding purchase by id " + id + ": " + e.getMessage());
            throw new DatabaseException("Error finding purchase by id: " + id, e);
        }
        return purchase;
    }

    @Override
    public Purchase save(Purchase purchase) {
        String insertPurchaseSql = "INSERT INTO purchases (supplier_id, purchase_date, total_amount, payment_status, notes) " +
                                   "VALUES (?, ?, ?, ?, ?)";
        String insertItemSql = "INSERT INTO purchase_items (purchase_id, product_id, quantity, unit_price, total_price) " +
                               "VALUES (?, ?, ?, ?, ?)";
        String updateStockSql = "UPDATE products SET quantity = quantity + ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // 1. Insert Purchase
            try (PreparedStatement stmt = conn.prepareStatement(insertPurchaseSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, purchase.getSupplierId());
                stmt.setDate(2, Date.valueOf(purchase.getPurchaseDate()));
                stmt.setBigDecimal(3, purchase.getTotalAmount());
                stmt.setString(4, purchase.getPaymentStatus());
                stmt.setString(5, purchase.getNotes());

                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        purchase.setId(rs.getInt(1));
                    }
                }
            }

            // 2. Insert items, update inventory, record stock movements
            try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement stockStmt = conn.prepareStatement(updateStockSql)) {

                for (PurchaseItem item : purchase.getItems()) {
                    item.setPurchaseId(purchase.getId());

                    itemStmt.setInt(1, item.getPurchaseId());
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

                    // Increase product quantity
                    stockStmt.setInt(1, item.getQuantity());
                    stockStmt.setInt(2, item.getProductId());
                    stockStmt.executeUpdate();

                    // Record stock movement
                    StockMovement sm = new StockMovement();
                    sm.setProductId(item.getProductId());
                    sm.setMovementType("PURCHASE");
                    sm.setQuantity(item.getQuantity());
                    sm.setReferenceType("PURCHASE");
                    sm.setReferenceId(purchase.getId());
                    sm.setNotes("Purchase #" + purchase.getId());
                    stockMovementDao.save(sm, conn);
                }
            }

            conn.commit(); // Commit transaction
            logger.info("Purchase transaction committed successfully: #" + purchase.getId());
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.warning("Purchase transaction rolled back due to error");
                } catch (SQLException ex) {
                    logger.severe("Error rolling back purchase transaction: " + ex.getMessage());
                }
            }
            logger.severe("Error saving purchase: " + e.getMessage());
            throw new DatabaseException("Error saving purchase transaction", e);
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
        return purchase;
    }

    @Override
    public Purchase update(Purchase purchase) {
        String sql = "UPDATE purchases SET payment_status = ?, notes = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, purchase.getPaymentStatus());
            stmt.setString(2, purchase.getNotes());
            stmt.setInt(3, purchase.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Error updating purchase " + purchase.getId() + ": " + e.getMessage());
            throw new DatabaseException("Error updating purchase", e);
        }
        return purchase;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM purchases WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Error deleting purchase " + id + ": " + e.getMessage());
            throw new DatabaseException("Error deleting purchase: " + id, e);
        }
    }

    public List<Purchase> findBySupplier(int supplierId) {
        String sql = SELECT_WITH_SUPPLIER + " WHERE p.supplier_id = ? ORDER BY p.purchase_date DESC";
        List<Purchase> purchases = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, supplierId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    purchases.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error finding purchases by supplier: " + e.getMessage());
            throw new DatabaseException("Error finding purchases by supplier", e);
        }
        return purchases;
    }

    public List<Purchase> findByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = SELECT_WITH_SUPPLIER + " WHERE p.purchase_date BETWEEN ? AND ? ORDER BY p.purchase_date DESC";
        List<Purchase> purchases = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    purchases.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error finding purchases by date range: " + e.getMessage());
            throw new DatabaseException("Error finding purchases by date range", e);
        }
        return purchases;
    }

    private List<PurchaseItem> findItemsByPurchaseId(Connection conn, int purchaseId) throws SQLException {
        String sql = "SELECT pi.id, pi.purchase_id, pi.product_id, p.name AS product_name, " +
                     "pi.quantity, pi.unit_price, pi.total_price " +
                     "FROM purchase_items pi " +
                     "LEFT JOIN products p ON pi.product_id = p.id " +
                     "WHERE pi.purchase_id = ?";
        List<PurchaseItem> items = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, purchaseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PurchaseItem item = new PurchaseItem();
                    item.setId(rs.getInt("id"));
                    item.setPurchaseId(rs.getInt("purchase_id"));
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

    private Purchase mapRow(ResultSet rs) throws SQLException {
        Purchase p = new Purchase();
        p.setId(rs.getInt("id"));
        p.setSupplierId(rs.getInt("supplier_id"));
        p.setSupplierName(rs.getString("supplier_name"));
        Date d = rs.getDate("purchase_date");
        if (d != null) {
            p.setPurchaseDate(d.toLocalDate());
        }
        p.setTotalAmount(rs.getBigDecimal("total_amount"));
        p.setPaymentStatus(rs.getString("payment_status"));
        p.setNotes(rs.getString("notes"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            p.setCreatedAt(ts.toLocalDateTime());
        }
        return p;
    }
}

