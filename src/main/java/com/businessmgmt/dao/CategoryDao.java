package com.businessmgmt.dao;

import com.businessmgmt.exception.DatabaseException;
import com.businessmgmt.model.Category;
import com.businessmgmt.util.DatabaseConnection;
import com.businessmgmt.util.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class CategoryDao implements GenericDao<Category> {

    private static final Logger logger = LoggerFactory.getLogger(CategoryDao.class);

    @Override
    public List<Category> findAll() {
        String sql = "SELECT id, name, description FROM categories ORDER BY name";
        List<Category> categories = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                categories.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error finding all categories: " + e.getMessage());
            throw new DatabaseException("Error finding all categories", e);
        }
        return categories;
    }

    @Override
    public Category findById(int id) {
        String sql = "SELECT id, name, description FROM categories WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.severe("Error finding category by id " + id + ": " + e.getMessage());
            throw new DatabaseException("Error finding category by id: " + id, e);
        }
        return null;
    }

    public Optional<Category> findByName(String name) {
        String sql = "SELECT id, name, description FROM categories WHERE name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error finding category by name '" + name + "': " + e.getMessage());
            throw new DatabaseException("Error finding category by name: " + name, e);
        }
        return Optional.empty();
    }

    @Override
    public Category save(Category category) {
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    category.setId(keys.getInt(1));
                }
            }
            logger.info("Saved category: " + category.getName());
        } catch (SQLException e) {
            logger.severe("Error saving category: " + e.getMessage());
            throw new DatabaseException("Error saving category", e);
        }
        return category;
    }

    @Override
    public Category update(Category category) {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setInt(3, category.getId());
            stmt.executeUpdate();
            logger.info("Updated category: " + category.getName());
        } catch (SQLException e) {
            logger.severe("Error updating category: " + e.getMessage());
            throw new DatabaseException("Error updating category", e);
        }
        return category;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                logger.info("Deleted category with id: " + id);
                return true;
            }
        } catch (SQLException e) {
            logger.severe("Error deleting category: " + e.getMessage());
            throw new DatabaseException("Error deleting category", e);
        }
        return false;
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        return category;
    }
}
