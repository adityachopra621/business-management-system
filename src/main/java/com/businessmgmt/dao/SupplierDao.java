package com.businessmgmt.dao;

import com.businessmgmt.exception.DatabaseException;
import com.businessmgmt.model.Supplier;
import com.businessmgmt.util.DatabaseConnection;
import com.businessmgmt.util.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class SupplierDao implements GenericDao<Supplier> {

    private static final Logger logger = LoggerFactory.getLogger(SupplierDao.class);

    @Override
    public List<Supplier> findAll() {
        String sql = "SELECT id, name, contact_person, email, phone, address, created_at FROM suppliers ORDER BY name";
        List<Supplier> suppliers = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                suppliers.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error finding all suppliers: " + e.getMessage());
            throw new DatabaseException("Error finding all suppliers", e);
        }
        return suppliers;
    }

    @Override
    public Supplier findById(int id) {
        String sql = "SELECT id, name, contact_person, email, phone, address, created_at FROM suppliers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.severe("Error finding supplier by id " + id + ": " + e.getMessage());
            throw new DatabaseException("Error finding supplier by id: " + id, e);
        }
        return null;
    }

    public List<Supplier> search(String keyword) {
        String sql = "SELECT id, name, contact_person, email, phone, address, created_at FROM suppliers " +
                     "WHERE LOWER(name) LIKE ? OR LOWER(contact_person) LIKE ? OR LOWER(email) LIKE ? OR LOWER(phone) LIKE ? " +
                     "ORDER BY name";
        List<Supplier> suppliers = new ArrayList<>();
        String pattern = "%" + keyword.toLowerCase() + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            stmt.setString(4, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    suppliers.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error searching suppliers: " + e.getMessage());
            throw new DatabaseException("Error searching suppliers", e);
        }
        return suppliers;
    }

    @Override
    public Supplier save(Supplier supplier) {
        String sql = "INSERT INTO suppliers (name, contact_person, email, phone, address) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, supplier.getName());
            stmt.setString(2, supplier.getContactPerson());
            stmt.setString(3, supplier.getEmail());
            stmt.setString(4, supplier.getPhone());
            stmt.setString(5, supplier.getAddress());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    supplier.setId(keys.getInt(1));
                }
            }
            logger.info("Saved supplier: " + supplier.getName());
        } catch (SQLException e) {
            logger.severe("Error saving supplier: " + e.getMessage());
            throw new DatabaseException("Error saving supplier", e);
        }
        return supplier;
    }

    @Override
    public Supplier update(Supplier supplier) {
        String sql = "UPDATE suppliers SET name = ?, contact_person = ?, email = ?, phone = ?, address = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, supplier.getName());
            stmt.setString(2, supplier.getContactPerson());
            stmt.setString(3, supplier.getEmail());
            stmt.setString(4, supplier.getPhone());
            stmt.setString(5, supplier.getAddress());
            stmt.setInt(6, supplier.getId());
            stmt.executeUpdate();
            logger.info("Updated supplier: " + supplier.getName());
        } catch (SQLException e) {
            logger.severe("Error updating supplier: " + e.getMessage());
            throw new DatabaseException("Error updating supplier", e);
        }
        return supplier;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM suppliers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                logger.info("Deleted supplier with id: " + id);
                return true;
            }
        } catch (SQLException e) {
            logger.severe("Error deleting supplier: " + e.getMessage());
            throw new DatabaseException("Error deleting supplier", e);
        }
        return false;
    }

    private Supplier mapRow(ResultSet rs) throws SQLException {
        Supplier supplier = new Supplier();
        supplier.setId(rs.getInt("id"));
        supplier.setName(rs.getString("name"));
        supplier.setContactPerson(rs.getString("contact_person"));
        supplier.setEmail(rs.getString("email"));
        supplier.setPhone(rs.getString("phone"));
        supplier.setAddress(rs.getString("address"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            supplier.setCreatedAt(createdAt.toLocalDateTime());
        }
        return supplier;
    }
}
