package com.businessmgmt.controller;

import com.businessmgmt.model.Purchase;
import com.businessmgmt.model.Supplier;
import com.businessmgmt.service.PurchaseService;
import com.businessmgmt.service.SupplierService;
import com.businessmgmt.util.AlertHelper;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Controller for Purchase Management screen.
 */
public class PurchaseController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseController.class);
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    private final PurchaseService purchaseService = new PurchaseService();
    private final SupplierService supplierService = new SupplierService();

    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private ComboBox<Supplier> supplierFilter;
    @FXML private TableView<Purchase> purchaseTable;

    @FXML private TableColumn<Purchase, Integer> colId;
    @FXML private TableColumn<Purchase, String> colSupplier;
    @FXML private TableColumn<Purchase, String> colDate;
    @FXML private TableColumn<Purchase, String> colTotalAmount;
    @FXML private TableColumn<Purchase, String> colPaymentStatus;
    @FXML private TableColumn<Purchase, Void> colActions;

    private ObservableList<Purchase> purchaseList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadSuppliers();
        refreshTable();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSupplier.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getSupplierName() != null ? data.getValue().getSupplierName() : "Unknown"));
        colDate.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPurchaseDate() != null ? data.getValue().getPurchaseDate().toString() : ""));
        colTotalAmount.setCellValueFactory(data ->
                new SimpleStringProperty(CURRENCY_FORMAT.format(data.getValue().getTotalAmount())));
        colPaymentStatus.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));

        // Status badge
        colPaymentStatus.setCellFactory(col -> new TableCell<Purchase, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if ("PAID".equalsIgnoreCase(status)) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Action column: View Details / Toggle Payment Status
        colActions.setCellFactory(col -> new TableCell<Purchase, Void>() {
            private final Button togglePayBtn = new Button("Mark Paid");
            private final HBox pane = new HBox(5, togglePayBtn);

            {
                togglePayBtn.getStyleClass().add("action-btn");
                togglePayBtn.setOnAction(e -> {
                    Purchase p = getTableView().getItems().get(getIndex());
                    togglePaymentStatus(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Purchase p = getTableRow().getItem();
                    if ("PAID".equalsIgnoreCase(p.getPaymentStatus())) {
                        togglePayBtn.setText("Mark Pending");
                    } else {
                        togglePayBtn.setText("Mark Paid");
                    }
                    setGraphic(pane);
                }
            }
        });
    }

    private void loadSuppliers() {
        try {
            List<Supplier> suppliers = supplierService.getAllSuppliers();
            supplierFilter.getItems().clear();
            Supplier allSup = new Supplier();
            allSup.setId(0);
            allSup.setName("All Suppliers");
            supplierFilter.getItems().add(allSup);
            supplierFilter.getItems().addAll(suppliers);
            supplierFilter.getSelectionModel().select(allSup);
        } catch (Exception e) {
            logger.severe("Error loading supplier filter: " + e.getMessage());
        }
    }

    public void refreshTable() {
        try {
            List<Purchase> purchases = purchaseService.getAllPurchases();
            purchaseList.setAll(purchases);
            purchaseTable.setItems(purchaseList);
        } catch (Exception e) {
            logger.severe("Failed to load purchases: " + e.getMessage());
            AlertHelper.showError("Database Error", "Failed to retrieve purchases: " + e.getMessage());
        }
    }

    @FXML
    public void onFilterChanged(ActionEvent event) {
        LocalDate start = dateFrom.getValue();
        LocalDate end = dateTo.getValue();
        Supplier selected = supplierFilter.getSelectionModel().getSelectedItem();

        try {
            List<Purchase> results;
            if (start != null && end != null) {
                results = purchaseService.getPurchasesByDateRange(start, end);
            } else if (selected != null && selected.getId() > 0) {
                results = purchaseService.getPurchasesBySupplier(selected.getId());
            } else {
                results = purchaseService.getAllPurchases();
            }
            purchaseList.setAll(results);
            purchaseTable.setItems(purchaseList);
        } catch (Exception e) {
            logger.severe("Filter error: " + e.getMessage());
        }
    }

    @FXML
    public void onAddPurchase(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PurchaseFormDialog.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("New Purchase Order");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            refreshTable();
        } catch (IOException e) {
            logger.severe("Failed to open purchase form: " + e.getMessage());
            AlertHelper.showError("Error", "Could not open purchase form: " + e.getMessage());
        }
    }

    private void togglePaymentStatus(Purchase purchase) {
        String newStatus = "PAID".equalsIgnoreCase(purchase.getPaymentStatus()) ? "PENDING" : "PAID";
        purchase.setPaymentStatus(newStatus);
        try {
            // Note: PurchaseDao.update updates payment_status and notes
            new com.businessmgmt.dao.PurchaseDao().update(purchase);
            refreshTable();
        } catch (Exception e) {
            logger.severe("Failed to update payment status: " + e.getMessage());
            AlertHelper.showError("Error", "Could not update payment status: " + e.getMessage());
        }
    }
}

