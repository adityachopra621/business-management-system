package com.businessmgmt.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a purchase order from a supplier.
 */
public class Purchase {

    private int id;
    private int supplierId;
    private String supplierName; // transient for display
    private LocalDate purchaseDate;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String notes;
    private LocalDateTime createdAt;
    private List<PurchaseItem> items;

    public Purchase() {
        this.totalAmount = BigDecimal.ZERO;
        this.paymentStatus = "PENDING";
        this.purchaseDate = LocalDate.now();
        this.items = new ArrayList<>();
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
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

    public List<PurchaseItem> getItems() {
        return items;
    }

    public void setItems(List<PurchaseItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    /**
     * Calculates the total amount by summing the totalPrice of all items.
     * Each item's totalPrice should be calculated first via calculateTotalPrice().
     */
    public void calculateTotal() {
        this.totalAmount = BigDecimal.ZERO;
        if (items != null) {
            for (PurchaseItem item : items) {
                if (item.getTotalPrice() != null) {
                    this.totalAmount = this.totalAmount.add(item.getTotalPrice());
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Purchase{" +
                "id=" + id +
                ", supplierId=" + supplierId +
                ", supplierName='" + supplierName + '\'' +
                ", purchaseDate=" + purchaseDate +
                ", totalAmount=" + totalAmount +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", notes='" + notes + '\'' +
                ", createdAt=" + createdAt +
                ", items=" + (items != null ? items.size() : 0) + " item(s)" +
                '}';
    }
}
