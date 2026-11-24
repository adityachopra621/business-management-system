package com.businessmgmt.util;

import com.businessmgmt.exception.BusinessException;
import com.businessmgmt.model.Expense;
import com.businessmgmt.model.Product;
import com.businessmgmt.model.Sale;
import com.businessmgmt.model.SaleItem;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

/**
 * Utility class for exporting data to Excel format using Apache POI.
 * Supports product, sales, expense, and generic data export.
 */
public class ExcelExporter {

    private static final Logger logger = LoggerFactory.getLogger(ExcelExporter.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Exports a list of products to an Excel file.
     *
     * @param products the products to export
     * @param filePath the output file path
     */
    public void exportProducts(List<Product> products, String filePath) {
        logger.info("Exporting " + products.size() + " products to " + filePath);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Products");
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Header row
            String[] headers = {"ID", "Name", "Category", "Purchase Price", "Selling Price",
                    "Quantity", "Min Stock", "Description"};
            createHeaderRow(sheet, headers, headerStyle);

            // Data rows
            int rowNum = 1;
            for (Product product : products) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getName());
                row.createCell(2).setCellValue(product.getCategoryId());
                row.createCell(3).setCellValue(product.getPurchasePrice() != null
                        ? product.getPurchasePrice().doubleValue() : 0);
                row.createCell(4).setCellValue(product.getSellingPrice() != null
                        ? product.getSellingPrice().doubleValue() : 0);
                row.createCell(5).setCellValue(product.getQuantity());
                row.createCell(6).setCellValue(product.getMinStock());
                row.createCell(7).setCellValue(product.getDescription() != null
                        ? product.getDescription() : "");
            }

            // Auto-size columns
            autoSizeColumns(sheet, headers.length);

            writeWorkbook(workbook, filePath);
            logger.info("Products exported successfully to " + filePath);

        } catch (IOException e) {
            logger.severe("Failed to export products: " + e.getMessage());
            throw new BusinessException("Failed to export products: " + e.getMessage());
        }
    }

    /**
     * Exports a list of sales to an Excel file.
     *
     * @param sales    the sales to export
     * @param filePath the output file path
     */
    public void exportSales(List<Sale> sales, String filePath) {
        logger.info("Exporting " + sales.size() + " sales to " + filePath);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // Sales summary sheet
            XSSFSheet summarySheet = workbook.createSheet("Sales Summary");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] summaryHeaders = {"Sale ID", "Date", "Customer ID", "Subtotal",
                    "Discount %", "Total Amount", "Notes"};
            createHeaderRow(summarySheet, summaryHeaders, headerStyle);

            int rowNum = 1;
            for (Sale sale : sales) {
                Row row = summarySheet.createRow(rowNum++);
                row.createCell(0).setCellValue(sale.getId());
                row.createCell(1).setCellValue(sale.getSaleDate() != null
                        ? sale.getSaleDate().format(DATE_FORMAT) : "");
                row.createCell(2).setCellValue(sale.getCustomerId());
                row.createCell(3).setCellValue(sale.getSubtotal() != null
                        ? sale.getSubtotal().doubleValue() : 0);
                row.createCell(4).setCellValue(sale.getDiscountPercent() != null
                        ? sale.getDiscountPercent().doubleValue() : 0);
                row.createCell(5).setCellValue(sale.getTotalAmount() != null
                        ? sale.getTotalAmount().doubleValue() : 0);
                row.createCell(6).setCellValue(sale.getNotes() != null ? sale.getNotes() : "");
            }
            autoSizeColumns(summarySheet, summaryHeaders.length);

            // Sale items detail sheet
            XSSFSheet detailSheet = workbook.createSheet("Sale Items");
            String[] itemHeaders = {"Sale ID", "Product ID", "Product Name", "Quantity",
                    "Unit Price", "Total Price"};
            createHeaderRow(detailSheet, itemHeaders, headerStyle);

            int detailRowNum = 1;
            for (Sale sale : sales) {
                List<SaleItem> items = sale.getItems();
                if (items != null) {
                    for (SaleItem item : items) {
                        Row row = detailSheet.createRow(detailRowNum++);
                        row.createCell(0).setCellValue(sale.getId());
                        row.createCell(1).setCellValue(item.getProductId());
                        row.createCell(2).setCellValue(item.getProductName() != null
                                ? item.getProductName() : "");
                        row.createCell(3).setCellValue(item.getQuantity());
                        row.createCell(4).setCellValue(item.getUnitPrice() != null
                                ? item.getUnitPrice().doubleValue() : 0);
                        row.createCell(5).setCellValue(item.getTotalPrice() != null
                                ? item.getTotalPrice().doubleValue() : 0);
                    }
                }
            }
            autoSizeColumns(detailSheet, itemHeaders.length);

            writeWorkbook(workbook, filePath);
            logger.info("Sales exported successfully to " + filePath);

        } catch (IOException e) {
            logger.severe("Failed to export sales: " + e.getMessage());
            throw new BusinessException("Failed to export sales: " + e.getMessage());
        }
    }

    /**
     * Exports a list of expenses to an Excel file.
     *
     * @param expenses the expenses to export
     * @param filePath the output file path
     */
    public void exportExpenses(List<Expense> expenses, String filePath) {
        logger.info("Exporting " + expenses.size() + " expenses to " + filePath);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Expenses");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {"ID", "Category", "Description", "Amount", "Date"};
            createHeaderRow(sheet, headers, headerStyle);

            int rowNum = 1;
            BigDecimal total = BigDecimal.ZERO;
            for (Expense expense : expenses) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(expense.getId());
                row.createCell(1).setCellValue(expense.getCategory() != null
                        ? expense.getCategory() : "");
                row.createCell(2).setCellValue(expense.getDescription() != null
                        ? expense.getDescription() : "");
                row.createCell(3).setCellValue(expense.getAmount() != null
                        ? expense.getAmount().doubleValue() : 0);
                row.createCell(4).setCellValue(expense.getExpenseDate() != null
                        ? expense.getExpenseDate().format(DATE_FORMAT) : "");

                if (expense.getAmount() != null) {
                    total = total.add(expense.getAmount());
                }
            }

            // Total row
            Row totalRow = sheet.createRow(rowNum);
            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            Cell labelCell = totalRow.createCell(2);
            labelCell.setCellValue("TOTAL");
            labelCell.setCellStyle(boldStyle);

            Cell totalCell = totalRow.createCell(3);
            totalCell.setCellValue(total.doubleValue());
            totalCell.setCellStyle(boldStyle);

            autoSizeColumns(sheet, headers.length);

            writeWorkbook(workbook, filePath);
            logger.info("Expenses exported successfully to " + filePath);

        } catch (IOException e) {
            logger.severe("Failed to export expenses: " + e.getMessage());
            throw new BusinessException("Failed to export expenses: " + e.getMessage());
        }
    }

    /**
     * Exports generic data to an Excel file.
     *
     * @param sheetName the name of the sheet
     * @param headers   column headers
     * @param data      the data rows
     * @param filePath  the output file path
     */
    public void exportGeneric(String sheetName, String[] headers, List<Object[]> data, String filePath) {
        logger.info("Exporting generic data to " + filePath);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet(sheetName);
            CellStyle headerStyle = createHeaderStyle(workbook);

            createHeaderRow(sheet, headers, headerStyle);

            int rowNum = 1;
            for (Object[] rowData : data) {
                Row row = sheet.createRow(rowNum++);
                for (int col = 0; col < rowData.length; col++) {
                    Cell cell = row.createCell(col);
                    setCellValue(cell, rowData[col]);
                }
            }

            autoSizeColumns(sheet, headers.length);

            writeWorkbook(workbook, filePath);
            logger.info("Generic data exported successfully to " + filePath);

        } catch (IOException e) {
            logger.severe("Failed to export data: " + e.getMessage());
            throw new BusinessException("Failed to export data: " + e.getMessage());
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Creates a styled header cell style with bold font and background color.
     */
    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setFontHeightInPoints((short) 11);
        style.setFont(headerFont);

        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    /**
     * Creates the header row with the given column names and style.
     */
    private void createHeaderRow(XSSFSheet sheet, String[] headers, CellStyle style) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    /**
     * Auto-sizes all columns in the sheet.
     */
    private void autoSizeColumns(XSSFSheet sheet, int numColumns) {
        for (int i = 0; i < numColumns; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Sets a cell value based on the object type.
     */
    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * Writes the workbook to a file.
     */
    private void writeWorkbook(XSSFWorkbook workbook, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        }
    }
}
