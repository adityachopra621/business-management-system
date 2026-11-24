package com.businessmgmt.service;

import com.businessmgmt.dao.ExpenseDao;
import com.businessmgmt.exception.BusinessException;
import com.businessmgmt.exception.ValidationException;
import com.businessmgmt.model.Expense;
import com.businessmgmt.util.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service layer for expense management operations.
 */
public class ExpenseService {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseService.class);

    private final ExpenseDao expenseDao;

    public ExpenseService() {
        this.expenseDao = new ExpenseDao();
    }

    /**
     * Retrieves all expenses.
     */
    public List<Expense> getAllExpenses() {
        logger.info("Fetching all expenses");
        return expenseDao.findAll();
    }

    /**
     * Retrieves an expense by its ID.
     */
    public Expense getExpense(int id) {
        logger.info("Fetching expense with id: " + id);
        Expense expense = expenseDao.findById(id);
        if (expense == null) {
            throw new BusinessException("Expense not found with id: " + id);
        }
        return expense;
    }

    /**
     * Saves a new expense after validation.
     */
    public void saveExpense(Expense expense) {
        logger.info("Saving new expense");
        validateExpense(expense);
        expenseDao.save(expense);
        logger.info("Expense saved successfully");
    }

    /**
     * Updates an existing expense after validation.
     */
    public void updateExpense(Expense expense) {
        logger.info("Updating expense id: " + expense.getId());
        validateExpense(expense);
        expenseDao.update(expense);
        logger.info("Expense updated successfully: " + expense.getId());
    }

    /**
     * Deletes an expense by its ID.
     */
    public void deleteExpense(int id) {
        logger.info("Deleting expense id: " + id);
        expenseDao.delete(id);
        logger.info("Expense deleted successfully: " + id);
    }

    /**
     * Retrieves expenses in a specific category.
     */
    public List<Expense> getByCategory(String category) {
        logger.info("Fetching expenses for category: " + category);
        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Category cannot be empty");
        }
        return expenseDao.findByCategory(category.trim());
    }

    public List<Expense> getExpensesByCategory(String category) {
        return getByCategory(category);
    }

    /**
     * Retrieves expenses within a date range.
     */
    public List<Expense> getByDateRange(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching expenses from " + startDate + " to " + endDate);
        if (startDate == null || endDate == null) {
            throw new ValidationException("Start date and end date are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date cannot be after end date");
        }
        return expenseDao.findByDateRange(startDate, endDate);
    }

    public List<Expense> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        return getByDateRange(startDate, endDate);
    }

    /**
     * Retrieves the list of distinct expense categories.
     */
    public List<String> getExpenseCategories() {
        logger.info("Fetching expense categories");
        return expenseDao.findDistinctCategories();
    }

    // ==================== Validation ====================

    private void validateExpense(Expense expense) {
        if (expense.getAmount() == null || expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Expense amount must be positive");
        }
        if (expense.getCategory() == null || expense.getCategory().trim().isEmpty()) {
            throw new ValidationException("Expense category cannot be empty");
        }
    }
}
