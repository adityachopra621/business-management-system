package com.businessmgmt.service;

import com.businessmgmt.dao.DashboardDao;
import com.businessmgmt.dao.ExpenseDao;
import com.businessmgmt.dao.ProductDao;
import com.businessmgmt.dao.SaleDao;
import com.businessmgmt.exception.ValidationException;
import com.businessmgmt.model.Expense;
import com.businessmgmt.model.Product;
import com.businessmgmt.model.Sale;
import com.businessmgmt.model.SaleItem;
import com.businessmgmt.util.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Service layer for generating financial report data.
 * Provides revenue, expense, profit, and product profitability reports.
 */
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private final DashboardDao dashboardDao;
    private final SaleDao saleDao;
    private final ExpenseDao expenseDao;
    private final ProductDao productDao;

    public ReportService() {
        this.dashboardDao = new DashboardDao();
        this.saleDao = new SaleDao();
        this.expenseDao = new ExpenseDao();
        this.productDao = new ProductDao();
    }

    /**
     * Generates revenue report data for the given date range.
     * Returns a list of arrays: [date, totalAmount] for each sale in the range.
     *
     * @param startDate start of the reporting period
     * @param endDate   end of the reporting period
     * @return list of revenue entries
     */
    public List<Object[]> getRevenueReport(LocalDate startDate, LocalDate endDate) {
        logger.info("Generating revenue report from " + startDate + " to " + endDate);
        validateDateRange(startDate, endDate);

        List<Sale> sales = saleDao.findByDateRange(startDate, endDate);
        List<Object[]> report = new ArrayList<>();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Sale sale : sales) {
            report.add(new Object[]{
                    sale.getSaleDate(),
                    sale.getId(),
                    sale.getTotalAmount()
            });
            totalRevenue = totalRevenue.add(sale.getTotalAmount());
        }

        // Add summary row
        report.add(new Object[]{"TOTAL", "", totalRevenue});

        logger.info("Revenue report generated: " + sales.size() + " sales, total: " + totalRevenue);
        return report;
    }

    /**
     * Generates expense report data for the given date range.
     * Groups expenses by category with subtotals.
     *
     * @param startDate start of the reporting period
     * @param endDate   end of the reporting period
     * @return list of expense report entries [category, description, amount, date]
     */
    public List<Object[]> getExpenseReport(LocalDate startDate, LocalDate endDate) {
        logger.info("Generating expense report from " + startDate + " to " + endDate);
        validateDateRange(startDate, endDate);

        List<Expense> expenses = expenseDao.findByDateRange(startDate, endDate);
        List<Object[]> report = new ArrayList<>();

        BigDecimal totalExpenses = BigDecimal.ZERO;
        for (Expense expense : expenses) {
            report.add(new Object[]{
                    expense.getCategory(),
                    expense.getDescription(),
                    expense.getAmount(),
                    expense.getExpenseDate()
            });
            totalExpenses = totalExpenses.add(expense.getAmount());
        }

        // Add summary row
        report.add(new Object[]{"TOTAL", "", totalExpenses, ""});

        logger.info("Expense report generated: " + expenses.size() + " expenses, total: " + totalExpenses);
        return report;
    }

    /**
     * Generates profit report data for the given date range.
     * Calculates revenue minus expenses for the period.
     *
     * @param startDate start of the reporting period
     * @param endDate   end of the reporting period
     * @return list of profit report entries [label, amount]
     */
    public List<Object[]> getProfitReport(LocalDate startDate, LocalDate endDate) {
        logger.info("Generating profit report from " + startDate + " to " + endDate);
        validateDateRange(startDate, endDate);

        List<Sale> sales = saleDao.findByDateRange(startDate, endDate);
        List<Expense> expenses = expenseDao.findByDateRange(startDate, endDate);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Sale sale : sales) {
            totalRevenue = totalRevenue.add(sale.getTotalAmount());
        }

        BigDecimal totalExpenses = BigDecimal.ZERO;
        for (Expense expense : expenses) {
            totalExpenses = totalExpenses.add(expense.getAmount());
        }

        BigDecimal grossProfit = totalRevenue.subtract(totalExpenses);
        BigDecimal profitMargin = BigDecimal.ZERO;
        if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            profitMargin = grossProfit.multiply(new BigDecimal("100"))
                    .divide(totalRevenue, 2, RoundingMode.HALF_UP);
        }

        List<Object[]> report = new ArrayList<>();
        report.add(new Object[]{"Total Revenue", totalRevenue});
        report.add(new Object[]{"Total Expenses", totalExpenses});
        report.add(new Object[]{"Gross Profit", grossProfit});
        report.add(new Object[]{"Profit Margin (%)", profitMargin});

        logger.info("Profit report generated: revenue=" + totalRevenue
                + ", expenses=" + totalExpenses + ", profit=" + grossProfit);
        return report;
    }

    /**
     * Calculates product-level profitability based on purchase vs selling prices
     * and quantities sold.
     *
     * @return list of product profitability entries
     *         [productName, purchasePrice, sellingPrice, margin, quantityInStock]
     */
    public List<Object[]> getProductProfitability() {
        logger.info("Generating product profitability report");

        List<Product> products = productDao.findAll();
        List<Object[]> report = new ArrayList<>();

        for (Product product : products) {
            BigDecimal margin = product.getSellingPrice()
                    .subtract(product.getPurchasePrice());
            BigDecimal marginPercent = BigDecimal.ZERO;
            if (product.getSellingPrice().compareTo(BigDecimal.ZERO) > 0) {
                marginPercent = margin.multiply(new BigDecimal("100"))
                        .divide(product.getSellingPrice(), 2, RoundingMode.HALF_UP);
            }

            report.add(new Object[]{
                    product.getName(),
                    product.getPurchasePrice(),
                    product.getSellingPrice(),
                    margin,
                    marginPercent,
                    product.getQuantity()
            });
        }

        logger.info("Product profitability report generated for " + products.size() + " products");
        return report;
    }

    // ==================== Validation ====================

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new ValidationException("Start date and end date are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date cannot be after end date");
        }
    }
}
