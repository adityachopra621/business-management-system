package com.businessmgmt.controller;

import com.businessmgmt.model.Supplier;
import com.businessmgmt.service.SupplierService;
import com.businessmgmt.util.AlertHelper;
import com.businessmgmt.util.LoggerFactory;
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
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Controller for Supplier Management screen.
 */
public class SupplierController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(SupplierController.class);

    private final SupplierService supplierService = new SupplierService();

    @FXML private TextField searchField;
    @FXML private TableView<Supplier> supplierTable;

    @FXML private TableColumn<Supplier, Integer> colId;
    @FXML private TableColumn<Supplier, String> colName;
    @FXML private TableColumn<Supplier, String> colContactPerson;
    @FXML private TableColumn<Supplier, String> colEmail;
    @FXML private TableColumn<Supplier, String> colPhone;
    @FXML private TableColumn<Supplier, Void> colActions;

    private ObservableList<Supplier> supplierList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        refreshTable();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colContactPerson.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        // Action Buttons Column
        colActions.setCellFactory(col -> new TableCell<Supplier, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("action-btn");
                deleteBtn.getStyleClass().addAll("action-btn", "danger-btn");

                editBtn.setOnAction(e -> {
                    Supplier s = getTableView().getItems().get(getIndex());
                    openSupplierForm(s);
                });

                deleteBtn.setOnAction(e -> {
                    Supplier s = getTableView().getItems().get(getIndex());
                    deleteSupplier(s);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    public void refreshTable() {
        try {
            List<Supplier> suppliers = supplierService.getAllSuppliers();
            supplierList.setAll(suppliers);
            supplierTable.setItems(supplierList);
        } catch (Exception e) {
            logger.severe("Failed to load suppliers: " + e.getMessage());
            AlertHelper.showError("Database Error", "Failed to retrieve suppliers: " + e.getMessage());
        }
    }

    @FXML
    public void onSearch() {
        String keyword = searchField.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            refreshTable();
            return;
        }
        try {
            List<Supplier> results = supplierService.searchSuppliers(keyword.trim());
            supplierList.setAll(results);
            supplierTable.setItems(supplierList);
        } catch (Exception e) {
            logger.severe("Supplier search failed: " + e.getMessage());
        }
    }

    @FXML
    public void onAddSupplier(ActionEvent event) {
        openSupplierForm(null);
    }

    private void openSupplierForm(Supplier supplier) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SupplierFormDialog.fxml"));
            Parent root = loader.load();

            SupplierFormController controller = loader.getController();
            if (supplier != null) {
                controller.setSupplier(supplier);
            }

            Stage stage = new Stage();
            stage.setTitle(supplier == null ? "Add New Supplier" : "Edit Supplier: " + supplier.getName());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            refreshTable();
        } catch (IOException e) {
            logger.severe("Failed to open supplier form: " + e.getMessage());
            AlertHelper.showError("Error", "Could not open supplier form: " + e.getMessage());
        }
    }

    private void deleteSupplier(Supplier supplier) {
        boolean confirmed = AlertHelper.showConfirmation("Delete Supplier",
                "Are you sure you want to delete supplier '" + supplier.getName() + "'?");
        if (confirmed) {
            try {
                supplierService.deleteSupplier(supplier.getId());
                AlertHelper.showInfo("Deleted", "Supplier deleted successfully.");
                refreshTable();
            } catch (Exception e) {
                logger.severe("Delete supplier failed: " + e.getMessage());
                AlertHelper.showError("Delete Failed", "Cannot delete supplier: " + e.getMessage());
            }
        }
    }
}

