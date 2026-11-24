package com.businessmgmt.dao;

import com.businessmgmt.exception.DatabaseException;
import com.businessmgmt.model.StockMovement;
import com.businessmgmt.util.DatabaseConnection;
import com.businessmgmt.util.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Data Access Object for StockMovement entities.
 */
public class StockMovementDao {

    private static final Logger logger = LoggerFactory.getLogger(StockMovementDao.class);

    private static final String SELECT_WITH_PRODUCT =
            "SELECT sm.id, sm.product_id, p.name AS product_name, sm.movement_type, " +
            "sm.quantity, sm.reference_type, sm.reference_id, sm.movement_date, sm.notes " +
            "FROM stock_movements sm " +
            "LEFT JOIN products p ON sm.product_id = p.id";

    public List<StockMovement> findAll() {
        String sql = SELECT_WITH_PRODUCT + " ORDER BY sm.movement_date DESC";
        List<StockMovement> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error finding all stock movements: " + e.getMessage());
            throw new DatabaseException("Error finding all stock movements", e);
        }
        return list;
    }

    public List<StockMovement> findByProduct(int productId) {
        String sql = SELECT_WITH_PRODUCT + " WHERE sm.product_id = ? ORDER BY sm.movement_date DESC";
        List<StockMovement> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error finding stock movements for product " + productId + ": " + e.getMessage());
            throw new DatabaseException("Error finding stock movements for product: " + productId, e);
        }
        return list;
    }

    public StockMovement save(StockMovement movement, Connection conn) throws SQLException {
        String sql = "INSERT INTO stock_movements (product_id, movement_type, quantity, reference_type, reference_id, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, movement.getProductId());
            stmt.setString(2, movement.getMovementType());
            stmt.setInt(3, movement.getQuantity());
            stmt.setString(4, movement.getReferenceType());
            stmt.setInt(5, movement.getReferenceId());
            stmt.setString(6, movement.getNotes());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        movement.setId(rs.getInt(1));
                    }
                }
            }
        }
        return movement;
    }

    public StockMovement save(StockMovement movement) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return save(movement, conn);
        } catch (SQLException e) {
            logger.severe("Error saving stock movement: " + e.getMessage());
            throw new DatabaseException("Error saving stock movement", e);
        }
    }

    private StockMovement mapRow(ResultSet rs) throws SQLException {
        StockMovement sm = new StockMovement();
        sm.setId(rs.getInt("id"));
        sm.setProductId(rs.getInt("product_id"));
        sm.setProductName(rs.getString("product_name"));
        sm.setMovementType(rs.getString("movement_type"));
        sm.setQuantity(rs.getInt("quantity"));
        sm.setReferenceType(rs.getString("reference_type"));
        sm.setReferenceId(rs.getInt("reference_id"));
        Timestamp ts = rs.getTimestamp("movement_date");
        if (ts != null) {
            sm.setMovementDate(ts.toLocalDateTime());
        }
        sm.setNotes(rs.getString("notes"));
        return sm;
    }
}

