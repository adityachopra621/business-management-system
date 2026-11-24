package com.businessmgmt.dao;

import com.businessmgmt.exception.DatabaseException;
import com.businessmgmt.model.Customer;
import com.businessmgmt.util.DatabaseConnection;
import com.businessmgmt.util.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Data Access Object for Customer entities.
 */
public class CustomerDao implements GenericDao<Customer> {

    private static final Logger logger = LoggerFactory.getLogger(CustomerDao.class);

    @Override
    public List<Customer> findAll() {
        String sql = "SELECT id, name, email, phone, address, created_at FROM customers ORDER BY name";
        List<Customer> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error finding all customers: " + e.getMessage());
            throw new DatabaseException("Error finding all customers", e);
        }
        return list;
    }

    @Override
    public Customer findById(int id) {
        String sql = "SELECT id, name, email, phone, address, created_at FROM customers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.severe("Error finding customer by id " + id + ": " + e.getMessage());
            throw new DatabaseException("Error finding customer by id: " + id, e);
        }
        return null;
    }

    @Override
    public Customer save(Customer customer) {
        String sql = "INSERT INTO customers (name, email, phone, address) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPhone());
            stmt.setString(4, customer.getAddress());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        customer.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("Error saving customer: " + e.getMessage());
            throw new DatabaseException("Error saving customer", e);
        }
        return customer;
    }

    @Override
    public Customer update(Customer customer) {
        String sql = "UPDATE customers SET name = ?, email = ?, phone = ?, address = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPhone());
            stmt.setString(4, customer.getAddress());
            stmt.setInt(5, customer.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Error updating customer " + customer.getId() + ": " + e.getMessage());
            throw new DatabaseException("Error updating customer", e);
        }
        return customer;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Error deleting customer " + id + ": " + e.getMessage());
            throw new DatabaseException("Error deleting customer: " + id, e);
        }
    }

    public List<Customer> search(String keyword) {
        String sql = "SELECT id, name, email, phone, address, created_at FROM customers " +
                     "WHERE LOWER(name) LIKE ? OR LOWER(email) LIKE ? OR LOWER(phone) LIKE ? OR LOWER(address) LIKE ? " +
                     "ORDER BY name";
        List<Customer> list = new ArrayList<>();
        String pattern = "%" + keyword.toLowerCase() + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            stmt.setString(4, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error searching customers with keyword '" + keyword + "': " + e.getMessage());
            throw new DatabaseException("Error searching customers", e);
        }
        return list;
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            c.setCreatedAt(ts.toLocalDateTime());
        }
        return c;
    }
}

