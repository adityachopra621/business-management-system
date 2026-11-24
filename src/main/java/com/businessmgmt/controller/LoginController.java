package com.businessmgmt.controller;

import com.businessmgmt.util.LoggerFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.logging.Logger;

/**
 * Controller for Admin Login.
 */
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private static final String VALID_USERNAME = "admin@123";
    private static final String VALID_PASSWORD = "12345";

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorMessageLabel;

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText().trim() : "";

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        if (VALID_USERNAME.equals(username) && VALID_PASSWORD.equals(password)) {
            logger.info("Admin login successful for: " + username);
            openMainDashboard();
        } else {
            logger.warning("Failed login attempt with username: " + username);
            showError("Invalid username or password. Please try again.");
            passwordField.clear();
        }
    }

    private void showError(String message) {
        errorMessageLabel.setText(message);
        errorMessageLabel.setVisible(true);
        errorMessageLabel.setManaged(true);
    }

    private void openMainDashboard() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1200, 800);
            URL cssResource = getClass().getResource("/styles/styles.css");
            if (cssResource == null) {
                cssResource = getClass().getResource("/css/styles.css");
            }
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            }

            stage.setTitle("Business Management System - Admin");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            logger.severe("Failed to load MainView: " + e.getMessage());
            showError("Failed to open application: " + e.getMessage());
        }
    }
}
