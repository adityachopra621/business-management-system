package com.businessmgmt.controller;

import com.businessmgmt.model.Supplier;
import com.businessmgmt.service.SupplierService;
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
 * Controller for supplier creation and editing dialog.
 */
public class SupplierFormController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(SupplierFormController.class);

    private final SupplierService supplierService = new SupplierService();
    private Supplier existingSupplier;

    @FXML private TextField nameField;
    @FXML private TextField contactPersonField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressArea;
    @FXML private Button cancelBtn;
    @FXML private Button saveBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setSupplier(Supplier supplier) {
        this.existingSupplier = supplier;
        nameField.setText(supplier.getName());
        contactPersonField.setText(supplier.getContactPerson());
        emailField.setText(supplier.getEmail());
        phoneField.setText(supplier.getPhone());
        addressArea.setText(supplier.getAddress());
    }

    @FXML
    public void onSave(ActionEvent event) {
        String name = nameField.getText();
        if (ValidationHelper.isNullOrEmpty(name)) {
            AlertHelper.showWarning("Validation Error", "Supplier name cannot be empty.");
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
            if (existingSupplier == null) {
                Supplier s = new Supplier();
                s.setName(name.trim());
                s.setContactPerson(contactPersonField.getText());
                s.setEmail(email != null ? email.trim() : "");
                s.setPhone(phoneField.getText());
                s.setAddress(addressArea.getText());

                supplierService.saveSupplier(s);
                AlertHelper.showInfo("Success", "Supplier '" + name + "' added successfully.");
            } else {
                existingSupplier.setName(name.trim());
                existingSupplier.setContactPerson(contactPersonField.getText());
                existingSupplier.setEmail(email != null ? email.trim() : "");
                existingSupplier.setPhone(phoneField.getText());
                existingSupplier.setAddress(addressArea.getText());

                supplierService.updateSupplier(existingSupplier);
                AlertHelper.showInfo("Success", "Supplier updated successfully.");
            }
            closeDialog();
        } catch (Exception e) {
            logger.severe("Error saving supplier: " + e.getMessage());
            AlertHelper.showError("Save Failed", "Could not save supplier: " + e.getMessage());
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

