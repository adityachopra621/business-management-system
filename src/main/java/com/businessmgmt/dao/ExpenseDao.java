package com.businessmgmt.dao;

import com.businessmgmt.exception.DatabaseException;
import com.businessmgmt.model.Expense;
import com.businessmgmt.util.DatabaseConnection;
import com.businessmgmt.util.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Data Access Object for Expense records.
 */
public class ExpenseDao implements GenericDao<Expense> {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseDao.class);

    @Override
    public List<Expense> findAll() {
        String sql = "SELECT id, category, description, amount, expense_date, created_at FROM expenses ORDER BY expense_date DESC, id DESC";
        List<Expense> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error finding all expenses: " + e.getMessage());
            throw new DatabaseException("Error finding all expenses", e);
        }
        return list;
    }

    @Override
    public Expense findById(int id) {
        String sql = "SELECT id, category, description, amount, expense_date, created_at FROM expenses WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.severe("Error finding expense by id " + id + ": " + e.getMessage());
            throw new DatabaseException("Error finding expense by id: " + id, e);
        }
        return null;
    }

    @Override
    public Expense save(Expense expense) {
        String sql = "INSERT INTO expenses (category, description, amount, expense_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, expense.getCategory());
            stmt.setString(2, expense.getDescription());
            stmt.setBigDecimal(3, expense.getAmount());
            stmt.setDate(4, Date.valueOf(expense.getExpenseDate()));

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        expense.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("Error saving expense: " + e.getMessage());
            throw new DatabaseException("Error saving expense", e);
        }
        return expense;
    }

    @Override
    public Expense update(Expense expense) {
        String sql = "UPDATE expenses SET category = ?, description = ?, amount = ?, expense_date = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, expense.getCategory());
            stmt.setString(2, expense.getDescription());
            stmt.setBigDecimal(3, expense.getAmount());
            stmt.setDate(4, Date.valueOf(expense.getExpenseDate()));
            stmt.setInt(5, expense.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Error updating expense " + expense.getId() + ": " + e.getMessage());
            throw new DatabaseException("Error updating expense", e);
        }
        return expense;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Error deleting expense " + id + ": " + e.getMessage());
            throw new DatabaseException("Error deleting expense: " + id, e);
        }
    }

    public List<Expense> findByCategory(String category) {
        String sql = "SELECT id, category, description, amount, expense_date, created_at FROM expenses WHERE LOWER(category) = LOWER(?) ORDER BY expense_date DESC";
        List<Expense> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error finding expenses by category: " + e.getMessage());
            throw new DatabaseException("Error finding expenses by category", e);
        }
        return list;
    }

    public List<Expense> findByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT id, category, description, amount, expense_date, created_at FROM expenses WHERE expense_date BETWEEN ? AND ? ORDER BY expense_date DESC";
        List<Expense> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error finding expenses by date range: " + e.getMessage());
            throw new DatabaseException("Error finding expenses by date range", e);
        }
        return list;
    }

    public List<String> findDistinctCategories() {
        String sql = "SELECT DISTINCT category FROM expenses ORDER BY category";
        List<String> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            logger.severe("Error finding distinct categories: " + e.getMessage());
            throw new DatabaseException("Error finding distinct categories", e);
        }
        return list;
    }

    private Expense mapRow(ResultSet rs) throws SQLException {
        Expense exp = new Expense();
        exp.setId(rs.getInt("id"));
        exp.setCategory(rs.getString("category"));
        exp.setDescription(rs.getString("description"));
        exp.setAmount(rs.getBigDecimal("amount"));
        Date d = rs.getDate("expense_date");
        if (d != null) {
            exp.setExpenseDate(d.toLocalDate());
        }
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            exp.setCreatedAt(ts.toLocalDateTime());
        }
        return exp;
    }
}

