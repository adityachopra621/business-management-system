package com.businessmgmt.controller;

import com.businessmgmt.model.Expense;
import com.businessmgmt.service.ExpenseService;
import com.businessmgmt.util.AlertHelper;
import com.businessmgmt.util.LoggerFactory;
import com.businessmgmt.util.ValidationHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Controller for expense entry and modification.
 */
public class ExpenseFormController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseFormController.class);

    private final ExpenseService expenseService = new ExpenseService();
    private Expense existingExpense;

    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private TextField amountField;
    @FXML private DatePicker datePicker;
    @FXML private Button cancelBtn;
    @FXML private Button saveBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        datePicker.setValue(LocalDate.now());

        // Default standard expense categories
        categoryComboBox.getItems().setAll(
                "Rent", "Salaries", "Electricity & Utilities",
                "Transportation & Logistics", "Marketing & Advertising",
                "Office Supplies", "Maintenance & Repairs", "Insurance", "Taxes & Licenses"
        );

        try {
            List<String> existingCats = expenseService.getExpenseCategories();
            for (String cat : existingCats) {
                if (!categoryComboBox.getItems().contains(cat)) {
                    categoryComboBox.getItems().add(cat);
                }
            }
        } catch (Exception ignored) {}

        if (!categoryComboBox.getItems().isEmpty()) {
            categoryComboBox.getSelectionModel().select(0);
        }
    }

    public void setExpense(Expense expense) {
        this.existingExpense = expense;
        categoryComboBox.setValue(expense.getCategory());
        descriptionArea.setText(expense.getDescription());
        amountField.setText(expense.getAmount() != null ? expense.getAmount().toString() : "0.00");
        if (expense.getExpenseDate() != null) {
            datePicker.setValue(expense.getExpenseDate());
        }
    }

    @FXML
    public void onSave(ActionEvent event) {
        String category = categoryComboBox.getValue();
        if (ValidationHelper.isNullOrEmpty(category)) {
            AlertHelper.showWarning("Validation Error", "Please select or type an expense category.");
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountField.getText().trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                AlertHelper.showWarning("Validation Error", "Expense amount must be greater than zero.");
                return;
            }
        } catch (NumberFormatException e) {
            AlertHelper.showWarning("Validation Error", "Please enter a valid numeric amount.");
            return;
        }

        LocalDate date = datePicker.getValue();
        if (date == null) {
            AlertHelper.showWarning("Validation Error", "Please choose a valid expense date.");
            return;
        }

        try {
            if (existingExpense == null) {
                Expense exp = new Expense();
                exp.setCategory(category.trim());
                exp.setDescription(descriptionArea.getText());
                exp.setAmount(amount);
                exp.setExpenseDate(date);

                expenseService.saveExpense(exp);
                AlertHelper.showInfo("Success", "Expense recorded successfully.");
            } else {
                existingExpense.setCategory(category.trim());
                existingExpense.setDescription(descriptionArea.getText());
                existingExpense.setAmount(amount);
                existingExpense.setExpenseDate(date);

                expenseService.updateExpense(existingExpense);
                AlertHelper.showInfo("Success", "Expense updated successfully.");
            }
            closeDialog();
        } catch (Exception e) {
            logger.severe("Error saving expense: " + e.getMessage());
            AlertHelper.showError("Save Error", "Could not save expense: " + e.getMessage());
        }
    }

    @FXML
    public void onCancel(ActionEvent event) {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }
}

