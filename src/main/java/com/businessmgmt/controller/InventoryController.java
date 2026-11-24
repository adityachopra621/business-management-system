package com.businessmgmt.controller;

import com.businessmgmt.model.Category;
import com.businessmgmt.model.Product;
import com.businessmgmt.service.InventoryService;
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
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Controller for Inventory Management screen.
 */
public class InventoryController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    private final InventoryService inventoryService = new InventoryService();
    private final ExcelExporter excelExporter = new ExcelExporter();

    @FXML private TextField searchField;
    @FXML private ComboBox<Category> categoryFilter;
    @FXML private TableView<Product> productTable;

    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, String> colPurchasePrice;
    @FXML private TableColumn<Product, String> colSellingPrice;
    @FXML private TableColumn<Product, Integer> colQuantity;
    @FXML private TableColumn<Product, Integer> colMinStock;
    @FXML private TableColumn<Product, String> colStatus;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private boolean showingLowStockOnly = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadCategories();
        refreshTable();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCategoryName() != null ? data.getValue().getCategoryName() : "Unassigned"));
        colPurchasePrice.setCellValueFactory(data ->
                new SimpleStringProperty(CURRENCY_FORMAT.format(data.getValue().getPurchasePrice())));
        colSellingPrice.setCellValueFactory(data ->
                new SimpleStringProperty(CURRENCY_FORMAT.format(data.getValue().getSellingPrice())));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colMinStock.setCellValueFactory(new PropertyValueFactory<>("minStock"));

        // Status column with visual badges
        colStatus.setCellFactory(column -> new TableCell<Product, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Product p = getTableRow().getItem();
                    if (p.isLowStock()) {
                        setText("LOW STOCK");
                        setTextFill(Color.WHITE);
                        setStyle("-fx-background-color: #e74c3c; -fx-alignment: center; -fx-background-radius: 4; -fx-font-weight: bold;");
                    } else {
                        setText("IN STOCK");
                        setTextFill(Color.WHITE);
                        setStyle("-fx-background-color: #27ae60; -fx-alignment: center; -fx-background-radius: 4;");
                    }
                }
            }
        });

        // Double-click row to edit
        productTable.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    openProductForm(row.getItem());
                }
            });
            return row;
        });

        // Context Menu for right-click edit/delete
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit Product");
        editItem.setOnAction(e -> {
            Product selected = productTable.getSelectionModel().getSelectedItem();
            if (selected != null) openProductForm(selected);
        });
        MenuItem deleteItem = new MenuItem("Delete Product");
        deleteItem.setOnAction(e -> {
            Product selected = productTable.getSelectionModel().getSelectedItem();
            if (selected != null) deleteProduct(selected);
        });
        contextMenu.getItems().addAll(editItem, deleteItem);
        productTable.setContextMenu(contextMenu);
    }

    private void loadCategories() {
        try {
            List<Category> categories = inventoryService.getAllCategories();
            categoryFilter.getItems().clear();
            Category allCat = new Category(0, "All Categories", "Show all");
            categoryFilter.getItems().add(allCat);
            categoryFilter.getItems().addAll(categories);
            categoryFilter.getSelectionModel().select(allCat);
        } catch (Exception e) {
            logger.severe("Failed to load categories: " + e.getMessage());
        }
    }

    public void refreshTable() {
        try {
            List<Product> products;
            if (showingLowStockOnly) {
                products = inventoryService.getLowStockProducts();
            } else {
                products = inventoryService.getAllProducts();
            }
            productList.setAll(products);
            productTable.setItems(productList);
        } catch (Exception e) {
            logger.severe("Failed to refresh product table: " + e.getMessage());
            AlertHelper.showError("Database Error", "Failed to retrieve products: " + e.getMessage());
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
            List<Product> results = inventoryService.searchProducts(keyword.trim());
            productList.setAll(results);
            productTable.setItems(productList);
        } catch (Exception e) {
            logger.severe("Search failed: " + e.getMessage());
        }
    }

    @FXML
    public void onCategoryFilter() {
        Category selected = categoryFilter.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getId() == 0) {
            refreshTable();
            return;
        }
        try {
            List<Product> results = inventoryService.getProductsByCategory(selected.getId());
            productList.setAll(results);
            productTable.setItems(productList);
        } catch (Exception e) {
            logger.severe("Category filter error: " + e.getMessage());
        }
    }

    @FXML
    public void onAddProduct(ActionEvent event) {
        openProductForm(null);
    }

    @FXML
    public void onLowStock(ActionEvent event) {
        showingLowStockOnly = !showingLowStockOnly;
        refreshTable();
    }

    @FXML
    public void onManageCategories(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add New Category");
        dialog.setHeaderText("Enter name for new product category:");
        dialog.setContentText("Category Name:");
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                try {
                    Category cat = new Category(0, name.trim(), "User created");
                    inventoryService.saveCategory(cat);
                    AlertHelper.showInfo("Success", "Category '" + name + "' created successfully.");
                    loadCategories();
                } catch (Exception e) {
                    AlertHelper.showError("Error", "Could not save category: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    public void onExportExcel(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Inventory to Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook (*.xlsx)", "*.xlsx"));
        fileChooser.setInitialFileName("Inventory_Report.xlsx");
        File file = fileChooser.showSaveDialog(productTable.getScene().getWindow());

        if (file != null) {
            try {
                excelExporter.exportProducts(productList, file.getAbsolutePath());
                AlertHelper.showInfo("Export Complete", "Inventory exported successfully to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                logger.severe("Export error: " + e.getMessage());
                AlertHelper.showError("Export Failed", "Error exporting to Excel: " + e.getMessage());
            }
        }
    }

    private void openProductForm(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProductFormDialog.fxml"));
            Parent root = loader.load();

            ProductFormController controller = loader.getController();
            if (product != null) {
                controller.setProduct(product);
            }

            Stage stage = new Stage();
            stage.setTitle(product == null ? "Add New Product" : "Edit Product: " + product.getName());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            refreshTable();
        } catch (IOException e) {
            logger.severe("Failed to open product form: " + e.getMessage());
            AlertHelper.showError("Error", "Could not open form: " + e.getMessage());
        }
    }

    private void deleteProduct(Product product) {
        boolean confirmed = AlertHelper.showConfirmation("Delete Product",
                "Are you sure you want to delete product '" + product.getName() + "'?");
        if (confirmed) {
            try {
                inventoryService.deleteProduct(product.getId());
                AlertHelper.showInfo("Deleted", "Product deleted successfully.");
                refreshTable();
            } catch (Exception e) {
                logger.severe("Delete product failed: " + e.getMessage());
                AlertHelper.showError("Delete Failed", "Cannot delete product (it may have active sales/purchases): " + e.getMessage());
            }
        }
    }
}

