package com.businessmgmt.dao;

import com.businessmgmt.exception.DatabaseException;
import com.businessmgmt.model.DashboardStats;
import com.businessmgmt.model.Expense;
import com.businessmgmt.model.Purchase;
import com.businessmgmt.model.Sale;
import com.businessmgmt.util.DatabaseConnection;
import com.businessmgmt.util.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Data Access Object for dashboard metrics and analytical queries.
 */
public class DashboardDao {

    private static final Logger logger = LoggerFactory.getLogger(DashboardDao.class);

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();

        String sqlRevenue = "SELECT COALESCE(SUM(total_amount), 0) AS total_revenue FROM sales";
        String sqlExpenses = "SELECT COALESCE(SUM(amount), 0) AS total_expenses FROM expenses";
        String sqlInventory = "SELECT COALESCE(SUM(quantity * purchase_price), 0) AS inventory_value FROM products";
        String sqlLowStock = "SELECT COUNT(*) AS low_stock_count FROM products WHERE quantity <= min_stock";
        String sqlCounts = "SELECT " +
                "(SELECT COUNT(*) FROM products) AS total_products, " +
                "(SELECT COUNT(*) FROM customers) AS total_customers, " +
                "(SELECT COUNT(*) FROM suppliers) AS total_suppliers";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Revenue
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlRevenue)) {
                if (rs.next()) {
                    stats.setTotalRevenue(rs.getBigDecimal("total_revenue"));
                }
            }

            // Expenses
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlExpenses)) {
                if (rs.next()) {
                    stats.setTotalExpenses(rs.getBigDecimal("total_expenses"));
                }
            }

            // Net Profit = Revenue - Expenses
            stats.setNetProfit(stats.getTotalRevenue().subtract(stats.getTotalExpenses()));

            // Inventory Value
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlInventory)) {
                if (rs.next()) {
                    stats.setInventoryValue(rs.getBigDecimal("inventory_value"));
                }
            }

            // Low Stock Count
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlLowStock)) {
                if (rs.next()) {
                    stats.setLowStockCount(rs.getInt("low_stock_count"));
                }
            }

            // Entity Counts
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlCounts)) {
                if (rs.next()) {
                    stats.setTotalProducts(rs.getInt("total_products"));
                    stats.setTotalCustomers(rs.getInt("total_customers"));
                    stats.setTotalSuppliers(rs.getInt("total_suppliers"));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error retrieving dashboard stats: " + e.getMessage());
            throw new DatabaseException("Error retrieving dashboard stats", e);
        }
        return stats;
    }

    public List<Sale> getRecentSales(int limit) {
        String sql = "SELECT s.id, s.customer_id, c.name AS customer_name, s.sale_date, " +
                     "s.subtotal, s.discount_percent, s.total_amount, s.notes, s.created_at " +
                     "FROM sales s " +
                     "LEFT JOIN customers c ON s.customer_id = c.id " +
                     "ORDER BY s.sale_date DESC, s.id DESC LIMIT ?";
        List<Sale> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sale s = new Sale();
                    s.setId(rs.getInt("id"));
                    s.setCustomerId(rs.getInt("customer_id"));
                    s.setCustomerName(rs.getString("customer_name"));
                    Date d = rs.getDate("sale_date");
                    if (d != null) s.setSaleDate(d.toLocalDate());
                    s.setSubtotal(rs.getBigDecimal("subtotal"));
                    s.setDiscountPercent(rs.getBigDecimal("discount_percent"));
                    s.setTotalAmount(rs.getBigDecimal("total_amount"));
                    s.setNotes(rs.getString("notes"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) s.setCreatedAt(ts.toLocalDateTime());
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            logger.severe("Error fetching recent sales: " + e.getMessage());
            throw new DatabaseException("Error fetching recent sales", e);
        }
        return list;
    }

    public List<Purchase> getRecentPurchases(int limit) {
        String sql = "SELECT p.id, p.supplier_id, s.name AS supplier_name, p.purchase_date, " +
                     "p.total_amount, p.payment_status, p.notes, p.created_at " +
                     "FROM purchases p " +
                     "LEFT JOIN suppliers s ON p.supplier_id = s.id " +
                     "ORDER BY p.purchase_date DESC, p.id DESC LIMIT ?";
        List<Purchase> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Purchase p = new Purchase();
                    p.setId(rs.getInt("id"));
                    p.setSupplierId(rs.getInt("supplier_id"));
                    p.setSupplierName(rs.getString("supplier_name"));
                    Date d = rs.getDate("purchase_date");
                    if (d != null) p.setPurchaseDate(d.toLocalDate());
                    p.setTotalAmount(rs.getBigDecimal("total_amount"));
                    p.setPaymentStatus(rs.getString("payment_status"));
                    p.setNotes(rs.getString("notes"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) p.setCreatedAt(ts.toLocalDateTime());
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            logger.severe("Error fetching recent purchases: " + e.getMessage());
            throw new DatabaseException("Error fetching recent purchases", e);
        }
        return list;
    }

    public List<Expense> getRecentExpenses(int limit) {
        String sql = "SELECT id, category, description, amount, expense_date, created_at " +
                     "FROM expenses ORDER BY expense_date DESC, id DESC LIMIT ?";
        List<Expense> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Expense exp = new Expense();
                    exp.setId(rs.getInt("id"));
                    exp.setCategory(rs.getString("category"));
                    exp.setDescription(rs.getString("description"));
                    exp.setAmount(rs.getBigDecimal("amount"));
                    Date d = rs.getDate("expense_date");
                    if (d != null) exp.setExpenseDate(d.toLocalDate());
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) exp.setCreatedAt(ts.toLocalDateTime());
                    list.add(exp);
                }
            }
        } catch (SQLException e) {
            logger.severe("Error fetching recent expenses: " + e.getMessage());
            throw new DatabaseException("Error fetching recent expenses", e);
        }
        return list;
    }

    public LinkedHashMap<String, BigDecimal> getMonthlyRevenue() {
        String sql = "SELECT TO_CHAR(sale_date, 'YYYY-MM') AS month_label, SUM(total_amount) AS revenue " +
                     "FROM sales " +
                     "WHERE sale_date >= CURRENT_DATE - INTERVAL '6 months' " +
                     "GROUP BY TO_CHAR(sale_date, 'YYYY-MM') " +
                     "ORDER BY month_label ASC";
        LinkedHashMap<String, BigDecimal> map = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("month_label"), rs.getBigDecimal("revenue"));
            }
        } catch (SQLException e) {
            logger.severe("Error fetching monthly revenue: " + e.getMessage());
            throw new DatabaseException("Error fetching monthly revenue", e);
        }
        return map;
    }

    public LinkedHashMap<String, BigDecimal> getMonthlyExpenses() {
        String sql = "SELECT TO_CHAR(expense_date, 'YYYY-MM') AS month_label, SUM(amount) AS total_expense " +
                     "FROM expenses " +
                     "WHERE expense_date >= CURRENT_DATE - INTERVAL '6 months' " +
                     "GROUP BY TO_CHAR(expense_date, 'YYYY-MM') " +
                     "ORDER BY month_label ASC";
        LinkedHashMap<String, BigDecimal> map = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("month_label"), rs.getBigDecimal("total_expense"));
            }
        } catch (SQLException e) {
            logger.severe("Error fetching monthly expenses: " + e.getMessage());
            throw new DatabaseException("Error fetching monthly expenses", e);
        }
        return map;
    }

    public LinkedHashMap<String, BigDecimal> getTopSellingProducts(int limit) {
        String sql = "SELECT p.name AS product_name, SUM(si.total_price) AS total_revenue " +
                     "FROM sale_items si " +
                     "JOIN products p ON si.product_id = p.id " +
                     "GROUP BY p.id, p.name " +
                     "ORDER BY total_revenue DESC LIMIT ?";
        LinkedHashMap<String, BigDecimal> map = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("product_name"), rs.getBigDecimal("total_revenue"));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error fetching top selling products: " + e.getMessage());
            throw new DatabaseException("Error fetching top selling products", e);
        }
        return map;
    }
}

