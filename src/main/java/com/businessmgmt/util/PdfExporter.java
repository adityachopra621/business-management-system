package com.businessmgmt.util;

import com.businessmgmt.exception.BusinessException;
import com.businessmgmt.model.Sale;
import com.businessmgmt.model.SaleItem;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

/**
 * Utility class for exporting data to PDF format using Apache PDFBox.
 * Supports invoice generation and generic report export.
 */
public class PdfExporter {

    private static final Logger logger = LoggerFactory.getLogger(PdfExporter.class);

    private static final float MARGIN = 50;
    private static final float LINE_HEIGHT = 15;
    private static final float HEADER_LINE_HEIGHT = 20;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Exports a sale as a PDF invoice.
     *
     * @param sale     the sale to export
     * @param filePath the output file path
     */
    public void exportInvoice(Sale sale, String filePath) {
        logger.info("Exporting invoice for sale id: " + sale.getId() + " to " + filePath);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                float y = pageHeight - MARGIN;

                // ---- Company Header ----
                PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                y = drawText(cs, "Business Management System", fontBold, 18, MARGIN, y);
                y -= 5;
                y = drawText(cs, "INVOICE", fontBold, 14, MARGIN, y);
                y -= 10;

                // Horizontal line
                drawLine(cs, MARGIN, y, pageWidth - MARGIN, y);
                y -= HEADER_LINE_HEIGHT;

                // ---- Invoice Details ----
                y = drawText(cs, "Invoice #: INV-" + String.format("%05d", sale.getId()), fontRegular, 10, MARGIN, y);
                y = drawText(cs, "Date: " + (sale.getSaleDate() != null
                        ? sale.getSaleDate().format(DATE_FORMAT)
                        : LocalDate.now().format(DATE_FORMAT)), fontRegular, 10, MARGIN, y);
                y -= 10;

                // ---- Customer Info ----
                if (sale.getCustomerId() > 0) {
                    y = drawText(cs, "Customer ID: " + sale.getCustomerId(), fontRegular, 10, MARGIN, y);
                    y -= 5;
                }
                y -= 10;

                // ---- Items Table Header ----
                drawLine(cs, MARGIN, y, pageWidth - MARGIN, y);
                y -= LINE_HEIGHT;

                float col1 = MARGIN;
                float col2 = MARGIN + 40;
                float col3 = MARGIN + 250;
                float col4 = MARGIN + 340;
                float col5 = MARGIN + 430;

                drawTextAt(cs, "#", fontBold, 10, col1, y);
                drawTextAt(cs, "Product", fontBold, 10, col2, y);
                drawTextAt(cs, "Qty", fontBold, 10, col3, y);
                drawTextAt(cs, "Unit Price", fontBold, 10, col4, y);
                drawTextAt(cs, "Total", fontBold, 10, col5, y);
                y -= 5;
                drawLine(cs, MARGIN, y, pageWidth - MARGIN, y);
                y -= LINE_HEIGHT;

                // ---- Items ----
                List<SaleItem> items = sale.getItems();
                if (items != null) {
                    int index = 1;
                    for (SaleItem item : items) {
                        // Check if we need a new page
                        if (y < MARGIN + 80) {
                            cs.close();
                            PDPage newPage = new PDPage(PDRectangle.A4);
                            document.addPage(newPage);
                            // Re-open stream on new page would require restructuring;
                            // for simplicity, we keep items on one page
                            break;
                        }

                        drawTextAt(cs, String.valueOf(index), fontRegular, 9, col1, y);
                        String productName = item.getProductName() != null
                                ? item.getProductName() : "Product #" + item.getProductId();
                        // Truncate long names
                        if (productName.length() > 30) {
                            productName = productName.substring(0, 27) + "...";
                        }
                        drawTextAt(cs, productName, fontRegular, 9, col2, y);
                        drawTextAt(cs, String.valueOf(item.getQuantity()), fontRegular, 9, col3, y);
                        drawTextAt(cs, formatCurrency(item.getUnitPrice()), fontRegular, 9, col4, y);
                        drawTextAt(cs, formatCurrency(item.getTotalPrice()), fontRegular, 9, col5, y);

                        y -= LINE_HEIGHT;
                        index++;
                    }
                }

                y -= 5;
                drawLine(cs, MARGIN, y, pageWidth - MARGIN, y);
                y -= HEADER_LINE_HEIGHT;

                // ---- Totals ----
                y = drawText(cs, "Subtotal: " + formatCurrency(sale.getSubtotal()),
                        fontRegular, 10, col4, y);

                if (sale.getDiscountPercent() != null
                        && sale.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
                    y = drawText(cs, "Discount: " + sale.getDiscountPercent() + "%",
                            fontRegular, 10, col4, y);
                }

                y -= 5;
                y = drawText(cs, "TOTAL: " + formatCurrency(sale.getTotalAmount()),
                        fontBold, 12, col4, y);

                // ---- Footer ----
                y = MARGIN + 30;
                drawLine(cs, MARGIN, y, pageWidth - MARGIN, y);
                y -= LINE_HEIGHT;
                drawText(cs, "Thank you for your business!", fontRegular, 9, MARGIN, y);
            }

            document.save(filePath);
            logger.info("Invoice exported successfully to " + filePath);

        } catch (IOException e) {
            logger.severe("Failed to export invoice: " + e.getMessage());
            throw new BusinessException("Failed to export invoice: " + e.getMessage());
        }
    }

    /**
     * Exports a generic report to PDF.
     *
     * @param title    the report title
     * @param headers  column headers (each String[] is a header row)
     * @param data     the report data rows
     * @param filePath the output file path
     */
    public void exportReport(String title, List<String[]> headers, List<List<String>> data, String filePath) {
        logger.info("Exporting report '" + title + "' to " + filePath);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                float y = pageHeight - MARGIN;

                PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                // ---- Title ----
                y = drawText(cs, title, fontBold, 16, MARGIN, y);
                y -= 5;
                y = drawText(cs, "Generated: " + LocalDate.now().format(DATE_FORMAT),
                        fontRegular, 9, MARGIN, y);
                y -= 10;
                drawLine(cs, MARGIN, y, pageWidth - MARGIN, y);
                y -= HEADER_LINE_HEIGHT;

                // ---- Headers ----
                float availableWidth = pageWidth - 2 * MARGIN;
                if (headers != null && !headers.isEmpty()) {
                    String[] headerRow = headers.get(0);
                    int numCols = headerRow.length;
                    float colWidth = availableWidth / numCols;

                    for (int i = 0; i < numCols; i++) {
                        drawTextAt(cs, headerRow[i], fontBold, 9, MARGIN + i * colWidth, y);
                    }
                    y -= 5;
                    drawLine(cs, MARGIN, y, pageWidth - MARGIN, y);
                    y -= LINE_HEIGHT;

                    // ---- Data Rows ----
                    if (data != null) {
                        for (List<String> row : data) {
                            if (y < MARGIN + 30) {
                                // Simple page break handling — stop writing
                                drawText(cs, "... continued on next page (data truncated) ...",
                                        fontRegular, 8, MARGIN, y);
                                break;
                            }

                            for (int i = 0; i < Math.min(row.size(), numCols); i++) {
                                String cellText = row.get(i) != null ? row.get(i) : "";
                                // Truncate long cell text
                                if (cellText.length() > 25) {
                                    cellText = cellText.substring(0, 22) + "...";
                                }
                                drawTextAt(cs, cellText, fontRegular, 8, MARGIN + i * colWidth, y);
                            }
                            y -= LINE_HEIGHT;
                        }
                    }
                }

                // ---- Footer ----
                y = MARGIN + 15;
                drawLine(cs, MARGIN, y, pageWidth - MARGIN, y);
                y -= LINE_HEIGHT;
                drawText(cs, "Business Management System - Report", fontRegular, 8, MARGIN, y);
            }

            document.save(filePath);
            logger.info("Report exported successfully to " + filePath);

        } catch (IOException e) {
            logger.severe("Failed to export report: " + e.getMessage());
            throw new BusinessException("Failed to export report: " + e.getMessage());
        }
    }

    // ==================== Helper Methods ====================

    private float drawText(PDPageContentStream cs, String text, PDType1Font font,
                           float fontSize, float x, float y) throws IOException {
        if (text == null) text = "";
        text = sanitizePdfText(text);
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
        return y - LINE_HEIGHT;
    }

    private void drawTextAt(PDPageContentStream cs, String text, PDType1Font font,
                            float fontSize, float x, float y) throws IOException {
        if (text == null) text = "";
        text = sanitizePdfText(text);
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    private String sanitizePdfText(String text) {
        // Replace non-ascii or problematic characters with safe ascii equivalents
        return text.replace("—", "-")
                   .replace("–", "-")
                   .replace("“", "\"")
                   .replace("”", "\"")
                   .replace("’", "'")
                   .replaceAll("[\\r\\n\\t]", " ");
    }

    private void drawLine(PDPageContentStream cs, float x1, float y1,
                          float x2, float y2) throws IOException {
        cs.moveTo(x1, y1);
        cs.lineTo(x2, y2);
        cs.stroke();
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return String.format("%,.2f", amount);
    }
}
