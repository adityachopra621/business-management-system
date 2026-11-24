package com.businessmgmt.controller;

import com.businessmgmt.model.Expense;
import com.businessmgmt.service.ExpenseService;
import com.businessmgmt.util.AlertHelper;
import com.businessmgmt.util.ExcelExporter;
import com.businessmgmt.util.LoggerFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Controller for Business Expense Management screen.
 */
public class ExpenseController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    private final ExpenseService expenseService = new ExpenseService();
    private final ExcelExporter excelExporter = new ExcelExporter();

    @FXML private Label dailyTotalLabel;
    @FXML private Label monthlyTotalLabel;

    @FXML private ComboBox<String> categoryFilter;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private TableView<Expense> expenseTable;

    @FXML private TableColumn<Expense, Integer> colId;
    @FXML private TableColumn<Expense, String> colCategory;
    @FXML private TableColumn<Expense, String> colDescription;
    @FXML private TableColumn<Expense, String> colAmount;
    @FXML private TableColumn<Expense, String> colDate;
    @FXML private TableColumn<Expense, Void> colActions;

    private ObservableList<Expense> expenseList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadCategories();
        refreshTable();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colAmount.setCellValueFactory(data ->
                new SimpleStringProperty(CURRENCY_FORMAT.format(data.getValue().getAmount())));
        colDate.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getExpenseDate() != null ? data.getValue().getExpenseDate().toString() : ""));

        // Action column: Edit / Delete
        colActions.setCellFactory(col -> new TableCell<Expense, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("action-btn");
                deleteBtn.getStyleClass().addAll("action-btn", "danger-btn");

                editBtn.setOnAction(e -> {
                    Expense exp = getTableView().getItems().get(getIndex());
                    openExpenseForm(exp);
                });

                deleteBtn.setOnAction(e -> {
                    Expense exp = getTableView().getItems().get(getIndex());
                    deleteExpense(exp);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadCategories() {
        try {
            List<String> categories = expenseService.getExpenseCategories();
            categoryFilter.getItems().clear();
            categoryFilter.getItems().add("All Categories");
            categoryFilter.getItems().addAll(categories);
            categoryFilter.getSelectionModel().select("All Categories");
        } catch (Exception e) {
            logger.severe("Error loading expense categories: " + e.getMessage());
        }
    }

    public void refreshTable() {
        try {
            List<Expense> allExpenses = expenseService.getAllExpenses();
            expenseList.setAll(allExpenses);
            expenseTable.setItems(expenseList);

            // Compute summary metrics
            LocalDate today = LocalDate.now();
            BigDecimal dailySum = BigDecimal.ZERO;
            BigDecimal monthlySum = BigDecimal.ZERO;

            for (Expense exp : allExpenses) {
                if (exp.getExpenseDate() != null) {
                    if (exp.getExpenseDate().equals(today)) {
                        dailySum = dailySum.add(exp.getAmount());
                    }
                    if (exp.getExpenseDate().getYear() == today.getYear() &&
                        exp.getExpenseDate().getMonth() == today.getMonth()) {
                        monthlySum = monthlySum.add(exp.getAmount());
                    }
                }
            }

            dailyTotalLabel.setText(CURRENCY_FORMAT.format(dailySum));
            monthlyTotalLabel.setText(CURRENCY_FORMAT.format(monthlySum));

        } catch (Exception e) {
            logger.severe("Failed to load expenses: " + e.getMessage());
            AlertHelper.showError("Database Error", "Failed to retrieve expenses: " + e.getMessage());
        }
    }

    @FXML
    public void onFilterChanged(ActionEvent event) {
        String cat = categoryFilter.getSelectionModel().getSelectedItem();
        LocalDate start = dateFrom.getValue();
        LocalDate end = dateTo.getValue();

        try {
            List<Expense> results;
            if (start != null && end != null) {
                results = expenseService.getExpensesByDateRange(start, end);
            } else if (cat != null && !cat.equalsIgnoreCase("All Categories")) {
                results = expenseService.getExpensesByCategory(cat);
            } else {
                results = expenseService.getAllExpenses();
            }
            expenseList.setAll(results);
            expenseTable.setItems(expenseList);
        } catch (Exception e) {
            logger.severe("Expense filter error: " + e.getMessage());
        }
    }

    @FXML
    public void onAddExpense(ActionEvent event) {
        openExpenseForm(null);
    }

    @FXML
    public void onExportExcel(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Expenses to Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook (*.xlsx)", "*.xlsx"));
        fileChooser.setInitialFileName("Expense_Records.xlsx");
        File file = fileChooser.showSaveDialog(expenseTable.getScene().getWindow());

        if (file != null) {
            try {
                excelExporter.exportExpenses(expenseList, file.getAbsolutePath());
                AlertHelper.showInfo("Export Complete", "Expenses exported successfully to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                logger.severe("Excel export error: " + e.getMessage());
                AlertHelper.showError("Export Failed", "Error exporting expenses: " + e.getMessage());
            }
        }
    }

    private void openExpenseForm(Expense expense) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ExpenseFormDialog.fxml"));
            Parent root = loader.load();

            ExpenseFormController controller = loader.getController();
            if (expense != null) {
                controller.setExpense(expense);
            }

            Stage stage = new Stage();
            stage.setTitle(expense == null ? "Add Business Expense" : "Edit Expense");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadCategories();
            refreshTable();
        } catch (IOException e) {
            logger.severe("Failed to open expense form: " + e.getMessage());
            AlertHelper.showError("Error", "Could not open expense form: " + e.getMessage());
        }
    }

    private void deleteExpense(Expense expense) {
        boolean confirmed = AlertHelper.showConfirmation("Delete Expense",
                "Are you sure you want to delete this expense of " + CURRENCY_FORMAT.format(expense.getAmount()) + "?");
        if (confirmed) {
            try {
                expenseService.deleteExpense(expense.getId());
                AlertHelper.showInfo("Deleted", "Expense record deleted.");
                refreshTable();
            } catch (Exception e) {
                logger.severe("Delete expense failed: " + e.getMessage());
                AlertHelper.showError("Delete Failed", "Cannot delete expense: " + e.getMessage());
            }
        }
    }
}

