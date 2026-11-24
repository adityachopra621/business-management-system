package com.businessmgmt.service;

import com.businessmgmt.dao.DashboardDao;
import com.businessmgmt.model.DashboardStats;
import com.businessmgmt.model.Expense;
import com.businessmgmt.model.Purchase;
import com.businessmgmt.model.Sale;
import com.businessmgmt.util.LoggerFactory;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service layer for dashboard and analytics operations.
 * Provides aggregated statistics and recent activity data.
 */
public class DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    private final DashboardDao dashboardDao;

    public DashboardService() {
        this.dashboardDao = new DashboardDao();
    }

    /**
     * Retrieves overall dashboard statistics including totals and counts.
     */
    public DashboardStats getDashboardStats() {
        logger.info("Fetching dashboard statistics");
        return dashboardDao.getDashboardStats();
    }

    /**
     * Retrieves the most recent sales.
     *
     * @param limit maximum number of records to return
     */
    public List<Sale> getRecentSales(int limit) {
        logger.info("Fetching recent sales, limit: " + limit);
        return dashboardDao.getRecentSales(limit);
    }

    /**
     * Retrieves the most recent purchases.
     *
     * @param limit maximum number of records to return
     */
    public List<Purchase> getRecentPurchases(int limit) {
        logger.info("Fetching recent purchases, limit: " + limit);
        return dashboardDao.getRecentPurchases(limit);
    }

    /**
     * Retrieves the most recent expenses.
     *
     * @param limit maximum number of records to return
     */
    public List<Expense> getRecentExpenses(int limit) {
        logger.info("Fetching recent expenses, limit: " + limit);
        return dashboardDao.getRecentExpenses(limit);
    }

    /**
     * Retrieves monthly revenue totals for the last 6 months.
     *
     * @return ordered map of month labels to revenue amounts
     */
    public LinkedHashMap<String, BigDecimal> getMonthlyRevenue() {
        logger.info("Fetching monthly revenue");
        return dashboardDao.getMonthlyRevenue();
    }

    /**
     * Retrieves monthly expense totals for the last 6 months.
     *
     * @return ordered map of month labels to expense amounts
     */
    public LinkedHashMap<String, BigDecimal> getMonthlyExpenses() {
        logger.info("Fetching monthly expenses");
        return dashboardDao.getMonthlyExpenses();
    }

    /**
     * Retrieves the top-selling products by total revenue.
     *
     * @param limit maximum number of products to return
     * @return ordered map of product names to revenue amounts
     */
    public LinkedHashMap<String, BigDecimal> getTopSellingProducts(int limit) {
        logger.info("Fetching top selling products, limit: " + limit);
        return dashboardDao.getTopSellingProducts(limit);
    }
}
