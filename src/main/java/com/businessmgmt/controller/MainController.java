package com.businessmgmt.controller;

import com.businessmgmt.util.AlertHelper;
import com.businessmgmt.util.LoggerFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Controller for the main application shell and sidebar navigation.
 */
public class MainController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML private StackPane contentArea;
    @FXML private Button btnDashboard;
    @FXML private Button btnInventory;
    @FXML private Button btnSuppliers;
    @FXML private Button btnPurchases;
    @FXML private Button btnSales;
    @FXML private Button btnCustomers;
    @FXML private Button btnExpenses;
    @FXML private Button btnReports;
    @FXML private Button btnLogout;

    private Button activeButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        activeButton = btnDashboard;
        loadView("/fxml/DashboardView.fxml", btnDashboard);
    }

    @FXML
    public void onNavDashboard(ActionEvent event) {
        loadView("/fxml/DashboardView.fxml", btnDashboard);
    }

    @FXML
    public void onNavInventory(ActionEvent event) {
        loadView("/fxml/InventoryView.fxml", btnInventory);
    }

    @FXML
    public void onNavSuppliers(ActionEvent event) {
        loadView("/fxml/SupplierView.fxml", btnSuppliers);
    }

    @FXML
    public void onNavPurchases(ActionEvent event) {
        loadView("/fxml/PurchaseView.fxml", btnPurchases);
    }

    @FXML
    public void onNavSales(ActionEvent event) {
        loadView("/fxml/SalesView.fxml", btnSales);
    }

    @FXML
    public void onNavCustomers(ActionEvent event) {
        loadView("/fxml/CustomerView.fxml", btnCustomers);
    }

    @FXML
    public void onNavExpenses(ActionEvent event) {
        loadView("/fxml/ExpenseView.fxml", btnExpenses);
    }

    @FXML
    public void onNavReports(ActionEvent event) {
        loadView("/fxml/ReportsView.fxml", btnReports);
    }

    @FXML
    public void onLogout(ActionEvent event) {
        try {
            logger.info("Admin logging out...");
            Stage stage = (Stage) contentArea.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 950, 620);
            URL cssResource = getClass().getResource("/styles/styles.css");
            if (cssResource == null) {
                cssResource = getClass().getResource("/css/styles.css");
            }
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            }

            stage.setTitle("Business Management System - Login");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            logger.severe("Failed to logout: " + e.getMessage());
            AlertHelper.showError("Logout Error", "Could not return to login screen: " + e.getMessage());
        }
    }

    private void loadView(String fxmlPath, Button navButton) {
        try {
            logger.info("Loading view: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node node = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(node);

            // Update active button CSS class
            if (activeButton != null) {
                activeButton.getStyleClass().remove("sidebar-btn-active");
            }
            if (navButton != null) {
                if (!navButton.getStyleClass().contains("sidebar-btn-active")) {
                    navButton.getStyleClass().add("sidebar-btn-active");
                }
                activeButton = navButton;
            }
        } catch (Throwable e) {
            logger.severe("Failed to load view " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
            AlertHelper.showError("Navigation Error", "Could not load screen: " + fxmlPath + "\n" + e.getMessage());
        }
    }
}

