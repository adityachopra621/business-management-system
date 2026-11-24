package com.businessmgmt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.businessmgmt.util.AppConfig;
import com.businessmgmt.util.DatabaseConnection;
import com.businessmgmt.util.LoggerFactory;

import java.net.URL;
import java.util.logging.Logger;

/**
 * Main JavaFX Application class for the Business Management System.
 */
public class App extends Application {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.info("Starting Business Management System...");

        // Initialize database
        DatabaseConnection.getInstance().initializeDatabase();

        // Load Login landing view first
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();

        // Create scene and apply styles
        Scene scene = new Scene(root, 950, 620);
        URL cssResource = getClass().getResource("/styles/styles.css");
        if (cssResource == null) {
            cssResource = getClass().getResource("/css/styles.css");
        }
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        }

        // Configure stage
        primaryStage.setTitle("Business Management System - Admin Portal");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(850);
        primaryStage.setMinHeight(550);
        primaryStage.centerOnScreen();
        primaryStage.show();

        logger.info("Application started successfully.");
    }

    @Override
    public void stop() throws Exception {
        logger.info("Shutting down Business Management System...");
        DatabaseConnection.getInstance().close();
        logger.info("Application stopped.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
