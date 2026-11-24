package com.businessmgmt.model;

import java.time.LocalDateTime;

/**
 * Represents a stock movement event (PURCHASE, SALE, or ADJUSTMENT).
 */
public class StockMovement {

    private int id;
    private int productId;
    private String productName; // transient for display
    private String movementType; // PURCHASE, SALE, ADJUSTMENT
    private int quantity;
    private String referenceType;
    private int referenceId;
    private LocalDateTime movementDate;
    private String notes;

    public StockMovement() {
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public int getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(int referenceId) {
        this.referenceId = referenceId;
    }

    public LocalDateTime getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(LocalDateTime movementDate) {
        this.movementDate = movementDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "StockMovement{" +
                "id=" + id +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", movementType='" + movementType + '\'' +
                ", quantity=" + quantity +
                ", referenceType='" + referenceType + '\'' +
                ", referenceId=" + referenceId +
                ", movementDate=" + movementDate +
                ", notes='" + notes + '\'' +
                '}';
    }
}
