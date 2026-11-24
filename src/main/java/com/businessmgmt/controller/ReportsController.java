package com.businessmgmt.controller;

import com.businessmgmt.service.ReportService;
import com.businessmgmt.util.AlertHelper;
import com.businessmgmt.util.ExcelExporter;
import com.businessmgmt.util.LoggerFactory;
import com.businessmgmt.util.PdfExporter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Controller for Financial & Analytical Reports screen.
 */
public class ReportsController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ReportsController.class);
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    private final ReportService reportService = new ReportService();
    private final PdfExporter pdfExporter = new PdfExporter();
    private final ExcelExporter excelExporter = new ExcelExporter();

    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;

    @FXML private Label reportTitleLabel;
    @FXML private Label summaryValueLabel1;
    @FXML private Label summaryTitleLabel1;
    @FXML private Label summaryValueLabel2;
    @FXML private Label summaryTitleLabel2;
    @FXML private Label summaryValueLabel3;
    @FXML private Label summaryTitleLabel3;

    @FXML private BarChart<String, Number> reportChart;
    @FXML private CategoryAxis chartXAxis;
    @FXML private NumberAxis chartYAxis;

    @FXML private TableView<ObservableList<String>> reportTable;

    private String currentReportType = "REVENUE";
    private String[] currentHeaders = new String[0];
    private final List<Object[]> currentRawData = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Default date range: first day of current month to today
        LocalDate now = LocalDate.now();
        dateFrom.setValue(now.withDayOfMonth(1));
        dateTo.setValue(now);

        onRevenueReport(null);
    }

    @FXML
    public void onRevenueReport(ActionEvent event) {
        currentReportType = "REVENUE";
        reportTitleLabel.setText("Revenue Report (Sales Transactions)");
        summaryTitleLabel1.setText("Total Revenue");
        summaryTitleLabel2.setText("Total Orders");
        summaryTitleLabel3.setText("Avg Order Value");

        LocalDate start = dateFrom.getValue();
        LocalDate end = dateTo.getValue();

        try {
            List<Object[]> rows = reportService.getRevenueReport(start, end);
            currentRawData.clear();
            currentRawData.addAll(rows);

            currentHeaders = new String[]{"Date / Label", "Invoice #", "Amount ($)"};

            // Calculate KPIs
            BigDecimal totalRevenue = BigDecimal.ZERO;
            int orderCount = 0;
            for (Object[] r : rows) {
                if (!"TOTAL".equals(r[0])) {
                    orderCount++;
                    if (r[2] instanceof BigDecimal) {
                        totalRevenue = totalRevenue.add((BigDecimal) r[2]);
                    }
                }
            }

            BigDecimal avgOrder = orderCount > 0 ? totalRevenue.divide(new BigDecimal(orderCount), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;

            summaryValueLabel1.setText(CURRENCY_FORMAT.format(totalRevenue));
            summaryValueLabel2.setText(String.valueOf(orderCount));
            summaryValueLabel3.setText(CURRENCY_FORMAT.format(avgOrder));

            // Chart
            reportChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Revenue");
            for (Object[] r : rows) {
                if (!"TOTAL".equals(r[0]) && r[2] instanceof BigDecimal) {
                    series.getData().add(new XYChart.Data<>(r[0].toString(), (BigDecimal) r[2]));
                }
            }
            reportChart.getData().add(series);

            displayTable(currentHeaders, rows);

        } catch (Exception e) {
            logger.severe("Revenue report error: " + e.getMessage());
            AlertHelper.showError("Report Error", "Failed to load revenue report: " + e.getMessage());
        }
    }

    @FXML
    public void onExpenseReport(ActionEvent event) {
        currentReportType = "EXPENSE";
        reportTitleLabel.setText("Expense Report by Category");
        summaryTitleLabel1.setText("Total Expenses");
        summaryTitleLabel2.setText("Categories");
        summaryTitleLabel3.setText("Expense Entries");

        LocalDate start = dateFrom.getValue();
        LocalDate end = dateTo.getValue();

        try {
            List<Object[]> rows = reportService.getExpenseReport(start, end);
            currentRawData.clear();
            currentRawData.addAll(rows);

            currentHeaders = new String[]{"Category", "Description", "Amount ($)", "Date"};

            BigDecimal totalExp = BigDecimal.ZERO;
            java.util.Set<String> cats = new java.util.HashSet<>();
            int count = 0;

            for (Object[] r : rows) {
                if (!"TOTAL".equals(r[0])) {
                    count++;
                    cats.add(r[0].toString());
                    if (r[2] instanceof BigDecimal) {
                        totalExp = totalExp.add((BigDecimal) r[2]);
                    }
                }
            }

            summaryValueLabel1.setText(CURRENCY_FORMAT.format(totalExp));
            summaryValueLabel2.setText(String.valueOf(cats.size()));
            summaryValueLabel3.setText(String.valueOf(count));

            // Chart
            reportChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Expense by Category");
            java.util.Map<String, BigDecimal> catTotals = new java.util.HashMap<>();
            for (Object[] r : rows) {
                if (!"TOTAL".equals(r[0]) && r[2] instanceof BigDecimal) {
                    String cat = r[0].toString();
                    catTotals.put(cat, catTotals.getOrDefault(cat, BigDecimal.ZERO).add((BigDecimal) r[2]));
                }
            }
            catTotals.forEach((cat, val) -> series.getData().add(new XYChart.Data<>(cat, val)));
            reportChart.getData().add(series);

            displayTable(currentHeaders, rows);

        } catch (Exception e) {
            logger.severe("Expense report error: " + e.getMessage());
            AlertHelper.showError("Report Error", "Failed to load expense report: " + e.getMessage());
        }
    }

    @FXML
    public void onProfitReport(ActionEvent event) {
        currentReportType = "PROFIT";
        reportTitleLabel.setText("Profit & Loss Statement (P&L)");
        summaryTitleLabel1.setText("Total Revenue");
        summaryTitleLabel2.setText("Total Expenses");
        summaryTitleLabel3.setText("Net Profit");

        LocalDate start = dateFrom.getValue();
        LocalDate end = dateTo.getValue();

        try {
            List<Object[]> rows = reportService.getProfitReport(start, end);
            currentRawData.clear();
            currentRawData.addAll(rows);

            currentHeaders = new String[]{"Financial Metric", "Amount / Margin"};

            BigDecimal rev = (BigDecimal) rows.get(0)[1];
            BigDecimal exp = (BigDecimal) rows.get(1)[1];
            BigDecimal profit = (BigDecimal) rows.get(2)[1];

            summaryValueLabel1.setText(CURRENCY_FORMAT.format(rev));
            summaryValueLabel2.setText(CURRENCY_FORMAT.format(exp));
            summaryValueLabel3.setText(CURRENCY_FORMAT.format(profit));

            // Chart
            reportChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("P&L Summary");
            series.getData().add(new XYChart.Data<>("Revenue", rev));
            series.getData().add(new XYChart.Data<>("Expenses", exp));
            series.getData().add(new XYChart.Data<>("Net Profit", profit));
            reportChart.getData().add(series);

            displayTable(currentHeaders, rows);

        } catch (Exception e) {
            logger.severe("Profit report error: " + e.getMessage());
            AlertHelper.showError("Report Error", "Failed to load profit report: " + e.getMessage());
        }
    }

    @FXML
    public void onProductProfitability(ActionEvent event) {
        currentReportType = "PROFITABILITY";
        reportTitleLabel.setText("Product Margin & Profitability Analysis");
        summaryTitleLabel1.setText("Top Margin Product");
        summaryTitleLabel2.setText("Avg Margin %");
        summaryTitleLabel3.setText("Catalog Size");

        try {
            List<Object[]> rows = reportService.getProductProfitability();
            currentRawData.clear();
            currentRawData.addAll(rows);

            currentHeaders = new String[]{"Product Name", "Cost ($)", "Price ($)", "Margin ($)", "Margin %", "Stock Qty"};

            String topProd = "N/A";
            BigDecimal maxMargin = BigDecimal.ZERO;
            BigDecimal sumMarginPct = BigDecimal.ZERO;

            for (Object[] r : rows) {
                BigDecimal margin = (BigDecimal) r[3];
                BigDecimal marginPct = (BigDecimal) r[4];
                sumMarginPct = sumMarginPct.add(marginPct);
                if (margin.compareTo(maxMargin) > 0) {
                    maxMargin = margin;
                    topProd = r[0].toString();
                }
            }

            BigDecimal avgMarginPct = rows.isEmpty() ? BigDecimal.ZERO :
                    sumMarginPct.divide(new BigDecimal(rows.size()), 2, java.math.RoundingMode.HALF_UP);

            summaryValueLabel1.setText(topProd);
            summaryValueLabel2.setText(avgMarginPct + "%");
            summaryValueLabel3.setText(String.valueOf(rows.size()));

            // Chart
            reportChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Unit Profit Margin ($)");
            for (int i = 0; i < Math.min(rows.size(), 8); i++) {
                Object[] r = rows.get(i);
                series.getData().add(new XYChart.Data<>(r[0].toString(), (BigDecimal) r[3]));
            }
            reportChart.getData().add(series);

            displayTable(currentHeaders, rows);

        } catch (Exception e) {
            logger.severe("Profitability report error: " + e.getMessage());
            AlertHelper.showError("Report Error", "Failed to load profitability report: " + e.getMessage());
        }
    }

    private void displayTable(String[] headers, List<Object[]> data) {
        reportTable.getColumns().clear();

        for (int i = 0; i < headers.length; i++) {
            final int colIndex = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(headers[i]);
            col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(colIndex)));
            col.setPrefWidth(160);
            reportTable.getColumns().add(col);
        }

        ObservableList<ObservableList<String>> tableData = FXCollections.observableArrayList();
        for (Object[] row : data) {
            ObservableList<String> rowList = FXCollections.observableArrayList();
            for (Object cell : row) {
                if (cell instanceof BigDecimal) {
                    rowList.add(CURRENCY_FORMAT.format(cell));
                } else {
                    rowList.add(cell != null ? cell.toString() : "");
                }
            }
            tableData.add(rowList);
        }

        reportTable.setItems(tableData);
    }

    @FXML
    public void onExportPdf(ActionEvent event) {
        if (currentRawData.isEmpty()) {
            AlertHelper.showWarning("Export", "No data to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Report to PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Document (*.pdf)", "*.pdf"));
        fileChooser.setInitialFileName(currentReportType.toLowerCase() + "_report.pdf");
        File file = fileChooser.showSaveDialog(reportTable.getScene().getWindow());

        if (file != null) {
            try {
                List<String[]> headerList = new ArrayList<>();
                headerList.add(currentHeaders);

                List<List<String>> dataList = new ArrayList<>();
                for (Object[] r : currentRawData) {
                    List<String> row = new ArrayList<>();
                    for (Object c : r) {
                        row.add(c != null ? c.toString() : "");
                    }
                    dataList.add(row);
                }

                pdfExporter.exportReport(reportTitleLabel.getText(), headerList, dataList, file.getAbsolutePath());
                AlertHelper.showInfo("Export Complete", "PDF report exported successfully to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                logger.severe("PDF export error: " + e.getMessage());
                AlertHelper.showError("Export Failed", "Error exporting PDF: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onExportExcel(ActionEvent event) {
        if (currentRawData.isEmpty()) {
            AlertHelper.showWarning("Export", "No data to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Report to Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook (*.xlsx)", "*.xlsx"));
        fileChooser.setInitialFileName(currentReportType.toLowerCase() + "_report.xlsx");
        File file = fileChooser.showSaveDialog(reportTable.getScene().getWindow());

        if (file != null) {
            try {
                excelExporter.exportGeneric(currentReportType + " Report", currentHeaders, currentRawData, file.getAbsolutePath());
                AlertHelper.showInfo("Export Complete", "Excel report exported successfully to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                logger.severe("Excel export error: " + e.getMessage());
                AlertHelper.showError("Export Failed", "Error exporting Excel: " + e.getMessage());
            }
        }
    }
}

