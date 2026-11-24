package com.businessmgmt.controller;

import com.businessmgmt.model.Customer;
import com.businessmgmt.service.CustomerService;
import com.businessmgmt.util.AlertHelper;
import com.businessmgmt.util.LoggerFactory;
import com.businessmgmt.util.ValidationHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Controller for customer details entry and editing dialog.
 */
public class CustomerFormController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(CustomerFormController.class);

    private final CustomerService customerService = new CustomerService();
    private Customer existingCustomer;

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressArea;
    @FXML private Button cancelBtn;
    @FXML private Button saveBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setCustomer(Customer customer) {
        this.existingCustomer = customer;
        nameField.setText(customer.getName());
        emailField.setText(customer.getEmail());
        phoneField.setText(customer.getPhone());
        addressArea.setText(customer.getAddress());
    }

    @FXML
    public void onSave(ActionEvent event) {
        String name = nameField.getText();
        if (ValidationHelper.isNullOrEmpty(name)) {
            AlertHelper.showWarning("Validation Error", "Customer name cannot be empty.");
            return;
        }

        String email = emailField.getText();
        if (!ValidationHelper.isNullOrEmpty(email) && !ValidationHelper.isValidEmail(email)) {
            AlertHelper.showWarning("Validation Error", "Please enter a valid email address.");
            return;
        }

        String phone = phoneField.getText();
        if (!ValidationHelper.isValidPhone(phone)) {
            AlertHelper.showWarning("Validation Error", "Phone number must be exactly 10 digits and cannot contain letters or special characters.");
            return;
        }

        try {
            if (existingCustomer == null) {
                Customer c = new Customer();
                c.setName(name.trim());
                c.setEmail(email != null ? email.trim() : "");
                c.setPhone(phoneField.getText());
                c.setAddress(addressArea.getText());

                customerService.saveCustomer(c);
                AlertHelper.showInfo("Success", "Customer '" + name + "' added successfully.");
            } else {
                existingCustomer.setName(name.trim());
                existingCustomer.setEmail(email != null ? email.trim() : "");
                existingCustomer.setPhone(phoneField.getText());
                existingCustomer.setAddress(addressArea.getText());

                customerService.updateCustomer(existingCustomer);
                AlertHelper.showInfo("Success", "Customer updated successfully.");
            }
            closeDialog();
        } catch (Exception e) {
            logger.severe("Error saving customer: " + e.getMessage());
            AlertHelper.showError("Save Failed", "Could not save customer: " + e.getMessage());
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

