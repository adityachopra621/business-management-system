package com.businessmgmt.controller;

import com.businessmgmt.model.Category;
import com.businessmgmt.model.Product;
import com.businessmgmt.service.InventoryService;
import com.businessmgmt.util.AlertHelper;
import com.businessmgmt.util.LoggerFactory;
import com.businessmgmt.util.ValidationHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Controller for product creation and editing dialog.
 */
public class ProductFormController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ProductFormController.class);

    private final InventoryService inventoryService = new InventoryService();
    private Product existingProduct;

    @FXML private TextField nameField;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private TextField purchasePriceField;
    @FXML private TextField sellingPriceField;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Spinner<Integer> minStockSpinner;
    @FXML private Button cancelBtn;
    @FXML private Button saveBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup spinners with value factories
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100000, 0));
        minStockSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 5));

        loadCategories();
    }

    private void loadCategories() {
        try {
            List<Category> categories = inventoryService.getAllCategories();
            categoryComboBox.getItems().setAll(categories);
            if (!categories.isEmpty()) {
                categoryComboBox.getSelectionModel().select(0);
            }
        } catch (Exception e) {
            logger.severe("Error loading categories: " + e.getMessage());
        }
    }

    public void setProduct(Product product) {
        this.existingProduct = product;
        nameField.setText(product.getName());
        descriptionArea.setText(product.getDescription());
        purchasePriceField.setText(product.getPurchasePrice() != null ? product.getPurchasePrice().toString() : "0.00");
        sellingPriceField.setText(product.getSellingPrice() != null ? product.getSellingPrice().toString() : "0.00");
        quantitySpinner.getValueFactory().setValue(product.getQuantity());
        minStockSpinner.getValueFactory().setValue(product.getMinStock());

        // Match category
        for (Category cat : categoryComboBox.getItems()) {
            if (cat.getId() == product.getCategoryId()) {
                categoryComboBox.getSelectionModel().select(cat);
                break;
            }
        }
    }

    @FXML
    public void onSave(ActionEvent event) {
        String name = nameField.getText();
        if (ValidationHelper.isNullOrEmpty(name)) {
            AlertHelper.showWarning("Validation Error", "Product name cannot be empty.");
            return;
        }

        Category category = categoryComboBox.getSelectionModel().getSelectedItem();
        if (category == null) {
            AlertHelper.showWarning("Validation Error", "Please select a product category.");
            return;
        }

        BigDecimal purchasePrice;
        BigDecimal sellingPrice;
        try {
            purchasePrice = new BigDecimal(purchasePriceField.getText().trim());
            sellingPrice = new BigDecimal(sellingPriceField.getText().trim());
            if (purchasePrice.compareTo(BigDecimal.ZERO) <= 0 || sellingPrice.compareTo(BigDecimal.ZERO) <= 0) {
                AlertHelper.showWarning("Validation Error", "Purchase price and selling price must be greater than zero.");
                return;
            }
            if (sellingPrice.compareTo(purchasePrice) < 0) {
                AlertHelper.showWarning("Validation Error", "Selling price cannot be less than purchase price (business loss prevention).");
                return;
            }
        } catch (NumberFormatException e) {
            AlertHelper.showWarning("Validation Error", "Invalid price format. Please enter valid numeric values.");
            return;
        }

        int quantity = quantitySpinner.getValue();
        int minStock = minStockSpinner.getValue();
        if (quantity < 0 || minStock < 0) {
            AlertHelper.showWarning("Validation Error", "Quantity and minimum stock cannot be negative.");
            return;
        }

        try {
            if (existingProduct == null) {
                Product p = new Product();
                p.setName(name.trim());
                p.setCategoryId(category.getId());
                p.setDescription(descriptionArea.getText());
                p.setPurchasePrice(purchasePrice);
                p.setSellingPrice(sellingPrice);
                p.setQuantity(quantity);
                p.setMinStock(minStock);

                inventoryService.saveProduct(p);
                AlertHelper.showInfo("Success", "Product '" + name + "' added successfully.");
            } else {
                existingProduct.setName(name.trim());
                existingProduct.setCategoryId(category.getId());
                existingProduct.setDescription(descriptionArea.getText());
                existingProduct.setPurchasePrice(purchasePrice);
                existingProduct.setSellingPrice(sellingPrice);
                existingProduct.setQuantity(quantity);
                existingProduct.setMinStock(minStock);

                inventoryService.updateProduct(existingProduct);
                AlertHelper.showInfo("Success", "Product updated successfully.");
            }
            closeDialog();
        } catch (Exception e) {
            logger.severe("Error saving product: " + e.getMessage());
            AlertHelper.showError("Save Failed", "Could not save product: " + e.getMessage());
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

