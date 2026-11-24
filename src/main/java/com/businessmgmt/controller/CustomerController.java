package com.businessmgmt.controller;

import com.businessmgmt.model.Customer;
import com.businessmgmt.service.CustomerService;
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
 * Controller for Customer Management screen.
 */
public class CustomerController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService = new CustomerService();

    @FXML private TextField searchField;
    @FXML private TableView<Customer> customerTable;

    @FXML private TableColumn<Customer, Integer> colId;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colEmail;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, String> colAddress;
    @FXML private TableColumn<Customer, Void> colActions;

    private ObservableList<Customer> customerList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        refreshTable();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));

        // Action column: Edit / Delete
        colActions.setCellFactory(col -> new TableCell<Customer, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("action-btn");
                deleteBtn.getStyleClass().addAll("action-btn", "danger-btn");

                editBtn.setOnAction(e -> {
                    Customer c = getTableView().getItems().get(getIndex());
                    openCustomerForm(c);
                });

                deleteBtn.setOnAction(e -> {
                    Customer c = getTableView().getItems().get(getIndex());
                    deleteCustomer(c);
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
            List<Customer> customers = customerService.getAllCustomers();
            customerList.setAll(customers);
            customerTable.setItems(customerList);
        } catch (Exception e) {
            logger.severe("Failed to load customers: " + e.getMessage());
            AlertHelper.showError("Database Error", "Failed to retrieve customers: " + e.getMessage());
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
            List<Customer> results = customerService.searchCustomers(keyword.trim());
            customerList.setAll(results);
            customerTable.setItems(customerList);
        } catch (Exception e) {
            logger.severe("Customer search error: " + e.getMessage());
        }
    }

    @FXML
    public void onAddCustomer(ActionEvent event) {
        openCustomerForm(null);
    }

    private void openCustomerForm(Customer customer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CustomerFormDialog.fxml"));
            Parent root = loader.load();

            CustomerFormController controller = loader.getController();
            if (customer != null) {
                controller.setCustomer(customer);
            }

            Stage stage = new Stage();
            stage.setTitle(customer == null ? "Add New Customer" : "Edit Customer: " + customer.getName());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            refreshTable();
        } catch (IOException e) {
            logger.severe("Failed to open customer form: " + e.getMessage());
            AlertHelper.showError("Error", "Could not open customer form: " + e.getMessage());
        }
    }

    private void deleteCustomer(Customer customer) {
        boolean confirmed = AlertHelper.showConfirmation("Delete Customer",
                "Are you sure you want to delete customer '" + customer.getName() + "'?");
        if (confirmed) {
            try {
                customerService.deleteCustomer(customer.getId());
                AlertHelper.showInfo("Deleted", "Customer deleted successfully.");
                refreshTable();
            } catch (Exception e) {
                logger.severe("Delete customer failed: " + e.getMessage());
                AlertHelper.showError("Delete Failed", "Cannot delete customer: " + e.getMessage());
            }
        }
    }
}

