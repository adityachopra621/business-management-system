package com.businessmgmt.controller;

import com.businessmgmt.model.Customer;
import com.businessmgmt.model.Sale;
import com.businessmgmt.service.CustomerService;
import com.businessmgmt.service.SalesService;
import com.businessmgmt.util.AlertHelper;
import com.businessmgmt.util.ExcelExporter;
import com.businessmgmt.util.LoggerFactory;
import com.businessmgmt.util.PdfExporter;
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
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Controller for Sales Management and Invoice screen.
 */
public class SalesController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(SalesController.class);
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    private final SalesService salesService = new SalesService();
    private final CustomerService customerService = new CustomerService();
    private final PdfExporter pdfExporter = new PdfExporter();
    private final ExcelExporter excelExporter = new ExcelExporter();

    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private ComboBox<Customer> customerFilter;
    @FXML private TableView<Sale> salesTable;

    @FXML private TableColumn<Sale, Integer> colId;
    @FXML private TableColumn<Sale, String> colCustomer;
    @FXML private TableColumn<Sale, String> colDate;
    @FXML private TableColumn<Sale, String> colSubtotal;
    @FXML private TableColumn<Sale, String> colDiscount;
    @FXML private TableColumn<Sale, String> colTotalAmount;
    @FXML private TableColumn<Sale, Void> colActions;

    private ObservableList<Sale> salesList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadCustomers();
        refreshTable();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCustomer.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCustomerName() != null ? data.getValue().getCustomerName() : "Walk-in"));
        colDate.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getSaleDate() != null ? data.getValue().getSaleDate().toString() : ""));
        colSubtotal.setCellValueFactory(data ->
                new SimpleStringProperty(CURRENCY_FORMAT.format(data.getValue().getSubtotal())));
        colDiscount.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDiscountPercent() + "%"));
        colTotalAmount.setCellValueFactory(data ->
                new SimpleStringProperty(CURRENCY_FORMAT.format(data.getValue().getTotalAmount())));

        // Action Column: Generate Invoice PDF
        colActions.setCellFactory(col -> new TableCell<Sale, Void>() {
            private final Button invoiceBtn = new Button("📄 PDF Invoice");
            private final HBox pane = new HBox(5, invoiceBtn);

            {
                invoiceBtn.getStyleClass().addAll("action-btn", "primary-btn");
                invoiceBtn.setOnAction(e -> {
                    Sale s = getTableView().getItems().get(getIndex());
                    generatePdfInvoice(s);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadCustomers() {
        try {
            List<Customer> customers = customerService.getAllCustomers();
            customerFilter.getItems().clear();
            Customer allCust = new Customer();
            allCust.setId(0);
            allCust.setName("All Customers");
            customerFilter.getItems().add(allCust);
            customerFilter.getItems().addAll(customers);
            customerFilter.getSelectionModel().select(allCust);
        } catch (Exception e) {
            logger.severe("Error loading customer filter: " + e.getMessage());
        }
    }

    public void refreshTable() {
        try {
            List<Sale> sales = salesService.getAllSales();
            salesList.setAll(sales);
            salesTable.setItems(salesList);
        } catch (Exception e) {
            logger.severe("Failed to load sales: " + e.getMessage());
            AlertHelper.showError("Database Error", "Failed to retrieve sales: " + e.getMessage());
        }
    }

    @FXML
    public void onFilterChanged(ActionEvent event) {
        LocalDate start = dateFrom.getValue();
        LocalDate end = dateTo.getValue();
        Customer selected = customerFilter.getSelectionModel().getSelectedItem();

        try {
            List<Sale> results;
            if (start != null && end != null) {
                results = salesService.getSalesByDateRange(start, end);
            } else if (selected != null && selected.getId() > 0) {
                results = salesService.getSalesByCustomer(selected.getId());
            } else {
                results = salesService.getAllSales();
            }
            salesList.setAll(results);
            salesTable.setItems(salesList);
        } catch (Exception e) {
            logger.severe("Filter error: " + e.getMessage());
        }
    }

    @FXML
    public void onNewSale(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SaleFormDialog.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("New Sales Invoice");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            refreshTable();
        } catch (IOException e) {
            logger.severe("Failed to open sale form: " + e.getMessage());
            AlertHelper.showError("Error", "Could not open sale form: " + e.getMessage());
        }
    }

    @FXML
    public void onExportExcel(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Sales to Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook (*.xlsx)", "*.xlsx"));
        fileChooser.setInitialFileName("Sales_Records.xlsx");
        File file = fileChooser.showSaveDialog(salesTable.getScene().getWindow());

        if (file != null) {
            try {
                excelExporter.exportSales(salesList, file.getAbsolutePath());
                AlertHelper.showInfo("Export Complete", "Sales exported successfully to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                logger.severe("Excel export error: " + e.getMessage());
                AlertHelper.showError("Export Failed", "Error exporting to Excel: " + e.getMessage());
            }
        }
    }

    private void generatePdfInvoice(Sale sale) {
        try {
            // Load full sale with items
            Sale fullSale = salesService.getSale(sale.getId());

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save PDF Invoice");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Document (*.pdf)", "*.pdf"));
            fileChooser.setInitialFileName("Invoice_" + fullSale.getId() + ".pdf");
            File file = fileChooser.showSaveDialog(salesTable.getScene().getWindow());

            if (file != null) {
                pdfExporter.exportInvoice(fullSale, file.getAbsolutePath());
                AlertHelper.showInfo("Invoice Generated", "PDF Invoice generated successfully:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.severe("PDF generation error: " + e.getMessage());
            AlertHelper.showError("PDF Export Failed", "Could not generate PDF: " + e.getMessage());
        }
    }
}

