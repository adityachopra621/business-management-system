package com.businessmgmt.controller;

import com.businessmgmt.model.Product;
import com.businessmgmt.model.Purchase;
import com.businessmgmt.model.PurchaseItem;
import com.businessmgmt.model.Supplier;
import com.businessmgmt.service.InventoryService;
import com.businessmgmt.service.PurchaseService;
import com.businessmgmt.service.SupplierService;
import com.businessmgmt.util.AlertHelper;
import com.businessmgmt.util.LoggerFactory;
import com.businessmgmt.util.ValidationHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Controller for creating a new purchase transaction with line items.
 */
public class PurchaseFormController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseFormController.class);
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    private final PurchaseService purchaseService = new PurchaseService();
    private final SupplierService supplierService = new SupplierService();
    private final InventoryService inventoryService = new InventoryService();

    @FXML private ComboBox<Supplier> supplierComboBox;
    @FXML private DatePicker purchaseDatePicker;
    @FXML private ComboBox<String> paymentStatusComboBox;
    @FXML private TextField notesField;

    @FXML private ComboBox<Product> productComboBox;
    @FXML private TextField itemQuantityField;
    @FXML private TextField itemUnitPriceField;

    @FXML private TableView<PurchaseItem> itemsTable;
    @FXML private TableColumn<PurchaseItem, String> colItemProduct;
    @FXML private TableColumn<PurchaseItem, Integer> colItemQuantity;
    @FXML private TableColumn<PurchaseItem, String> colItemUnitPrice;
    @FXML private TableColumn<PurchaseItem, String> colItemTotalPrice;
    @FXML private TableColumn<PurchaseItem, Void> colItemAction;

    @FXML private Label totalAmountLabel;
    @FXML private Button cancelBtn;
    @FXML private Button saveBtn;

    private final ObservableList<PurchaseItem> itemsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        purchaseDatePicker.setValue(LocalDate.now());

        paymentStatusComboBox.getItems().setAll("PENDING", "PAID");
        paymentStatusComboBox.getSelectionModel().select("PENDING");

        loadSuppliers();
        loadProducts();
        setupTable();
    }

    private void loadSuppliers() {
        try {
            List<Supplier> suppliers = supplierService.getAllSuppliers();
            supplierComboBox.getItems().setAll(suppliers);
            if (!suppliers.isEmpty()) {
                supplierComboBox.getSelectionModel().select(0);
            }
        } catch (Exception e) {
            logger.severe("Error loading suppliers: " + e.getMessage());
        }
    }

    private void loadProducts() {
        try {
            List<Product> products = inventoryService.getAllProducts();
            productComboBox.getItems().setAll(products);
            productComboBox.setOnAction(e -> {
                Product p = productComboBox.getSelectionModel().getSelectedItem();
                if (p != null) {
                    itemUnitPriceField.setText(p.getPurchasePrice().toString());
                }
            });
        } catch (Exception e) {
            logger.severe("Error loading products: " + e.getMessage());
        }
    }

    private void setupTable() {
        colItemProduct.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getProductName() != null ? data.getValue().getProductName() : "Product #" + data.getValue().getProductId()));
        colItemQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colItemUnitPrice.setCellValueFactory(data ->
                new SimpleStringProperty(CURRENCY_FORMAT.format(data.getValue().getUnitPrice())));
        colItemTotalPrice.setCellValueFactory(data ->
                new SimpleStringProperty(CURRENCY_FORMAT.format(data.getValue().getTotalPrice())));

        // Action column for removing line items
        colItemAction.setCellFactory(col -> new TableCell<PurchaseItem, Void>() {
            private final Button removeBtn = new Button("Remove");
            {
                removeBtn.getStyleClass().addAll("action-btn", "danger-btn");
                removeBtn.setOnAction(e -> {
                    PurchaseItem item = getTableView().getItems().get(getIndex());
                    itemsList.remove(item);
                    calculateTotal();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });

        itemsTable.setItems(itemsList);
    }

    @FXML
    public void onAddItem(ActionEvent event) {
        Product product = productComboBox.getSelectionModel().getSelectedItem();
        if (product == null) {
            AlertHelper.showWarning("Item Error", "Please select a product.");
            return;
        }

        int quantity;
        BigDecimal unitPrice;
        try {
            quantity = Integer.parseInt(itemQuantityField.getText().trim());
            if (quantity <= 0) {
                AlertHelper.showWarning("Item Error", "Quantity must be greater than zero.");
                return;
            }
        } catch (NumberFormatException e) {
            AlertHelper.showWarning("Item Error", "Invalid quantity. Please enter a whole number.");
            return;
        }

        try {
            unitPrice = new BigDecimal(itemUnitPriceField.getText().trim());
            if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                AlertHelper.showWarning("Item Error", "Unit price must be non-negative.");
                return;
            }
        } catch (NumberFormatException e) {
            AlertHelper.showWarning("Item Error", "Invalid unit price. Please enter a valid decimal.");
            return;
        }

        PurchaseItem item = new PurchaseItem();
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.calculateTotalPrice();

        itemsList.add(item);
        calculateTotal();

        // Clear item inputs
        itemQuantityField.clear();
    }

    private void calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseItem it : itemsList) {
            total = total.add(it.getTotalPrice());
        }
        totalAmountLabel.setText(CURRENCY_FORMAT.format(total));
    }

    @FXML
    public void onSave(ActionEvent event) {
        Supplier supplier = supplierComboBox.getSelectionModel().getSelectedItem();
        if (supplier == null) {
            AlertHelper.showWarning("Validation Error", "Please select a supplier.");
            return;
        }

        LocalDate date = purchaseDatePicker.getValue();
        if (date == null) {
            AlertHelper.showWarning("Validation Error", "Please select a purchase date.");
            return;
        }

        if (itemsList.isEmpty()) {
            AlertHelper.showWarning("Validation Error", "Please add at least one line item.");
            return;
        }

        try {
            Purchase purchase = new Purchase();
            purchase.setSupplierId(supplier.getId());
            purchase.setPurchaseDate(date);
            purchase.setPaymentStatus(paymentStatusComboBox.getValue());
            purchase.setNotes(notesField.getText());
            purchase.getItems().addAll(itemsList);
            purchase.calculateTotal();

            purchaseService.createPurchase(purchase);
            AlertHelper.showInfo("Purchase Recorded", "Purchase order successfully created! Inventory has been automatically increased.");
            closeDialog();
        } catch (Exception e) {
            logger.severe("Failed to create purchase: " + e.getMessage());
            AlertHelper.showError("Save Error", "Could not complete purchase: " + e.getMessage());
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

