package com.businessmgmt.controller;

import com.businessmgmt.model.DashboardStats;
import com.businessmgmt.model.Expense;
import com.businessmgmt.model.Purchase;
import com.businessmgmt.model.Sale;
import com.businessmgmt.service.DashboardService;
import com.businessmgmt.util.AlertHelper;
import com.businessmgmt.util.LoggerFactory;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Controller for the executive dashboard view.
 */
public class DashboardController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    private final DashboardService dashboardService = new DashboardService();

    @FXML private Label totalRevenue;
    @FXML private Label totalExpenses;
    @FXML private Label netProfit;
    @FXML private Label inventoryValue;
    @FXML private Label lowStockCount;
    @FXML private Label totalProducts;

    @FXML private BarChart<String, Number> revenueChart;
    @FXML private PieChart topProductsChart;

    @FXML private TableView<Sale> recentSalesTable;
    @FXML private TableColumn<Sale, String> recentSaleCustomerCol;
    @FXML private TableColumn<Sale, String> recentSaleDateCol;
    @FXML private TableColumn<Sale, String> recentSaleAmountCol;

    @FXML private TableView<Purchase> recentPurchasesTable;
    @FXML private TableColumn<Purchase, String> recentPurchaseSupplierCol;
    @FXML private TableColumn<Purchase, String> recentPurchaseDateCol;
    @FXML private TableColumn<Purchase, String> recentPurchaseAmountCol;

    @FXML private TableView<Expense> recentExpensesTable;
    @FXML private TableColumn<Expense, String> recentExpenseCategoryCol;
    @FXML private TableColumn<Expense, String> recentExpenseDateCol;
    @FXML private TableColumn<Expense, String> recentExpenseAmountCol;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadDashboardData();
    }

    private void setupTableColumns() {
        // Recent Sales
        recentSaleCustomerCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCustomerName() != null ? data.getValue().getCustomerName() : "Walk-in"));
        recentSaleDateCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getSaleDate() != null ? data.getValue().getSaleDate().toString() : ""));
        recentSaleAmountCol.setCellValueFactory(data ->
                new SimpleStringProperty(formatCurrency(data.getValue().getTotalAmount())));

        // Recent Purchases
        recentPurchaseSupplierCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getSupplierName() != null ? data.getValue().getSupplierName() : "N/A"));
        recentPurchaseDateCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPurchaseDate() != null ? data.getValue().getPurchaseDate().toString() : ""));
        recentPurchaseAmountCol.setCellValueFactory(data ->
                new SimpleStringProperty(formatCurrency(data.getValue().getTotalAmount())));

        // Recent Expenses
        recentExpenseCategoryCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCategory()));
        recentExpenseDateCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getExpenseDate() != null ? data.getValue().getExpenseDate().toString() : ""));
        recentExpenseAmountCol.setCellValueFactory(data ->
                new SimpleStringProperty(formatCurrency(data.getValue().getAmount())));
    }

    private void loadDashboardData() {
        try {
            // KPI Summary Cards
            DashboardStats stats = dashboardService.getDashboardStats();
            totalRevenue.setText(formatCurrency(stats.getTotalRevenue()));
            totalExpenses.setText(formatCurrency(stats.getTotalExpenses()));
            netProfit.setText(formatCurrency(stats.getNetProfit()));
            inventoryValue.setText(formatCurrency(stats.getInventoryValue()));
            lowStockCount.setText(String.valueOf(stats.getLowStockCount()));
            totalProducts.setText(String.valueOf(stats.getTotalProducts()));

            // Monthly Revenue vs Expenses BarChart
            revenueChart.getData().clear();
            LinkedHashMap<String, BigDecimal> monthlyRev = dashboardService.getMonthlyRevenue();
            LinkedHashMap<String, BigDecimal> monthlyExp = dashboardService.getMonthlyExpenses();

            XYChart.Series<String, Number> revSeries = new XYChart.Series<>();
            revSeries.setName("Revenue");
            monthlyRev.forEach((month, val) -> revSeries.getData().add(new XYChart.Data<>(month, val)));

            XYChart.Series<String, Number> expSeries = new XYChart.Series<>();
            expSeries.setName("Expenses");
            monthlyExp.forEach((month, val) -> expSeries.getData().add(new XYChart.Data<>(month, val)));

            revenueChart.getData().addAll(revSeries, expSeries);

            // Top Products PieChart
            topProductsChart.getData().clear();
            LinkedHashMap<String, BigDecimal> topProds = dashboardService.getTopSellingProducts(5);
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            topProds.forEach((prodName, amount) -> pieData.add(new PieChart.Data(prodName, amount.doubleValue())));
            topProductsChart.setData(pieData);

            // Recent Tables
            recentSalesTable.setItems(FXCollections.observableArrayList(dashboardService.getRecentSales(5)));
            recentPurchasesTable.setItems(FXCollections.observableArrayList(dashboardService.getRecentPurchases(5)));
            recentExpensesTable.setItems(FXCollections.observableArrayList(dashboardService.getRecentExpenses(5)));

        } catch (Exception e) {
            logger.severe("Error loading dashboard data: " + e.getMessage());
            AlertHelper.showError("Database Error", "Unable to load dashboard data: " + e.getMessage());
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "$0.00";
        return CURRENCY_FORMAT.format(amount);
    }
}

