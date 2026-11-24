package com.businessmgmt.controller;

import com.businessmgmt.exception.InsufficientStockException;
import com.businessmgmt.model.Customer;
import com.businessmgmt.model.Product;
import com.businessmgmt.model.Sale;
import com.businessmgmt.model.SaleItem;
import com.businessmgmt.service.CustomerService;
import com.businessmgmt.service.InventoryService;
import com.businessmgmt.service.SalesService;
import com.businessmgmt.util.AlertHelper;
import com.businessmgmt.util.LoggerFactory;
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
import java.math.RoundingMode;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Controller for creating a new sales invoice with automatic stock checks and total calculations.
 */
public class SaleFormController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(SaleFormController.class);
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    private final SalesService salesService = new SalesService();
    private final CustomerService customerService = new CustomerService();
    private final InventoryService inventoryService = new InventoryService();

    @FXML private ComboBox<Customer> customerComboBox;
    @FXML private DatePicker saleDatePicker;
    @FXML private TextField discountPercentField;
    @FXML private TextField notesField;

    @FXML private ComboBox<Product> productComboBox;
    @FXML private Label availableStockLabel;
    @FXML private TextField itemQuantityField;
    @FXML private TextField itemUnitPriceField;

    @FXML private TableView<SaleItem> itemsTable;
    @FXML private TableColumn<SaleItem, String> colItemProduct;
    @FXML private TableColumn<SaleItem, Integer> colItemQuantity;
    @FXML private TableColumn<SaleItem, String> colItemUnitPrice;
    @FXML private TableColumn<SaleItem, String> colItemTotalPrice;
    @FXML private TableColumn<SaleItem, Void> colItemAction;

    @FXML private Label subtotalLabel;
    @FXML private Label discountAmountLabel;
    @FXML private Label totalAmountLabel;

    @FXML private Button cancelBtn;
    @FXML private Button saveBtn;

    private final ObservableList<SaleItem> itemsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        saleDatePicker.setValue(LocalDate.now());

        loadCustomers();
        loadProducts();
        setupTable();
    }

    private void loadCustomers() {
        try {
            List<Customer> customers = customerService.getAllCustomers();
            customerComboBox.getItems().setAll(customers);
            if (!customers.isEmpty()) {
                customerComboBox.getSelectionModel().select(0);
            }
        } catch (Exception e) {
            logger.severe("Error loading customers: " + e.getMessage());
        }
    }

    private void loadProducts() {
        try {
            List<Product> products = inventoryService.getAllProducts();
            productComboBox.getItems().setAll(products);
        } catch (Exception e) {
            logger.severe("Error loading products: " + e.getMessage());
        }
    }

    @FXML
    public void onProductSelected(ActionEvent event) {
        Product p = productComboBox.getSelectionModel().getSelectedItem();
        if (p != null) {
            availableStockLabel.setText("Stock: " + p.getQuantity());
            itemUnitPriceField.setText(p.getSellingPrice().toString());
            if (p.getQuantity() <= 0) {
                availableStockLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            } else {
                availableStockLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
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

        // Remove item button
        colItemAction.setCellFactory(col -> new TableCell<SaleItem, Void>() {
            private final Button removeBtn = new Button("Remove");
            {
                removeBtn.getStyleClass().addAll("action-btn", "danger-btn");
                removeBtn.setOnAction(e -> {
                    SaleItem item = getTableView().getItems().get(getIndex());
                    itemsList.remove(item);
                    recalculateTotals();
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

        // Verify currently available stock before adding
        int alreadyAddedQty = 0;
        for (SaleItem existing : itemsList) {
            if (existing.getProductId() == product.getId()) {
                alreadyAddedQty += existing.getQuantity();
            }
        }

        if ((alreadyAddedQty + quantity) > product.getQuantity()) {
            AlertHelper.showWarning("Insufficient Stock",
                    "Cannot add " + quantity + " units of '" + product.getName() + "'. Available stock: " + product.getQuantity() +
                    (alreadyAddedQty > 0 ? " (" + alreadyAddedQty + " already in cart)" : ""));
            return;
        }

        try {
            unitPrice = new BigDecimal(itemUnitPriceField.getText().trim());
            if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                AlertHelper.showWarning("Item Error", "Unit price must be non-negative.");
                return;
            }
        } catch (NumberFormatException e) {
            AlertHelper.showWarning("Item Error", "Invalid unit price.");
            return;
        }

        SaleItem item = new SaleItem();
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.calculateTotalPrice();

        itemsList.add(item);
        recalculateTotals();

        itemQuantityField.clear();
    }

    @FXML
    public void onDiscountChanged() {
        recalculateTotals();
    }

    private void recalculateTotals() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (SaleItem item : itemsList) {
            subtotal = subtotal.add(item.getTotalPrice());
        }

        BigDecimal discountPercent = BigDecimal.ZERO;
        try {
            String discText = discountPercentField.getText();
            if (discText != null && !discText.trim().isEmpty()) {
                discountPercent = new BigDecimal(discText.trim());
                if (discountPercent.compareTo(BigDecimal.ZERO) < 0 || discountPercent.compareTo(new BigDecimal(100)) > 0) {
                    discountPercent = BigDecimal.ZERO;
                }
            }
        } catch (NumberFormatException ignored) {}

        BigDecimal discountAmount = subtotal.multiply(discountPercent)
                .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.subtract(discountAmount);

        subtotalLabel.setText(CURRENCY_FORMAT.format(subtotal));
        discountAmountLabel.setText("-" + CURRENCY_FORMAT.format(discountAmount) + " (" + discountPercent + "%)");
        totalAmountLabel.setText(CURRENCY_FORMAT.format(total));
    }

    @FXML
    public void onSave(ActionEvent event) {
        Customer customer = customerComboBox.getSelectionModel().getSelectedItem();
        if (customer == null) {
            AlertHelper.showWarning("Validation Error", "Please select a customer.");
            return;
        }

        LocalDate date = saleDatePicker.getValue();
        if (date == null) {
            AlertHelper.showWarning("Validation Error", "Please select a sale date.");
            return;
        }

        if (itemsList.isEmpty()) {
            AlertHelper.showWarning("Validation Error", "Please add at least one line item.");
            return;
        }

        BigDecimal discountPercent = BigDecimal.ZERO;
        try {
            String discText = discountPercentField.getText();
            if (discText != null && !discText.trim().isEmpty()) {
                discountPercent = new BigDecimal(discText.trim());
                if (discountPercent.compareTo(BigDecimal.ZERO) < 0 || discountPercent.compareTo(new BigDecimal(100)) > 0) {
                    AlertHelper.showWarning("Validation Error", "Discount percentage must be between 0% and 100%.");
                    return;
                }
            }
        } catch (NumberFormatException e) {
            AlertHelper.showWarning("Validation Error", "Invalid discount percentage. Please enter numbers only.");
            return;
        }

        try {
            Sale sale = new Sale();
            sale.setCustomerId(customer.getId());
            sale.setSaleDate(date);
            sale.setDiscountPercent(discountPercent);
            sale.setNotes(notesField.getText());
            sale.getItems().addAll(itemsList);
            sale.calculateTotals();

            salesService.createSale(sale);
            AlertHelper.showInfo("Sale Completed", "Invoice #" + sale.getId() + " created successfully! Stock has been deducted.");
            closeDialog();
        } catch (InsufficientStockException e) {
            logger.warning("Sale aborted due to insufficient stock: " + e.getMessage());
            AlertHelper.showError("Insufficient Stock", e.getMessage());
        } catch (Exception e) {
            logger.severe("Failed to process sale: " + e.getMessage());
            AlertHelper.showError("Sale Failed", "Could not process transaction: " + e.getMessage());
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

