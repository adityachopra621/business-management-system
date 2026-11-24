package com.businessmgmt.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a sale transaction to a customer.
 */
public class Sale {

    private int id;
    private int customerId;
    private String customerName; // transient for display
    private LocalDate saleDate;
    private BigDecimal subtotal;
    private BigDecimal discountPercent;
    private BigDecimal totalAmount;
    private String notes;
    private LocalDateTime createdAt;
    private List<SaleItem> items;

    public Sale() {
        this.subtotal = BigDecimal.ZERO;
        this.discountPercent = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.saleDate = LocalDate.now();
        this.items = new ArrayList<>();
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<SaleItem> getItems() {
        return items;
    }

    public void setItems(List<SaleItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    /**
     * Calculates the subtotal by summing item totalPrices,
     * then applies the discount percentage to compute the totalAmount.
     * Each item's totalPrice should be calculated first via calculateTotalPrice().
     */
    public void calculateTotals() {
        // Sum subtotal from items
        this.subtotal = BigDecimal.ZERO;
        if (items != null) {
            for (SaleItem item : items) {
                if (item.getTotalPrice() != null) {
                    this.subtotal = this.subtotal.add(item.getTotalPrice());
                }
            }
        }

        // Apply discount
        if (discountPercent != null && discountPercent.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountAmount = subtotal.multiply(discountPercent)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            this.totalAmount = subtotal.subtract(discountAmount);
        } else {
            this.totalAmount = subtotal;
        }
    }

    @Override
    public String toString() {
        return "Sale{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", customerName='" + customerName + '\'' +
                ", saleDate=" + saleDate +
                ", subtotal=" + subtotal +
                ", discountPercent=" + discountPercent +
                ", totalAmount=" + totalAmount +
                ", notes='" + notes + '\'' +
                ", createdAt=" + createdAt +
                ", items=" + (items != null ? items.size() : 0) + " item(s)" +
                '}';
    }
}
