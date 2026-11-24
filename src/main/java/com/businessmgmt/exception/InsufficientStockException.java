package com.businessmgmt.exception;

public class InsufficientStockException extends BusinessException {

    private final int productId;
    private final String productName;
    private final int requestedQuantity;
    private final int availableQuantity;

    public InsufficientStockException(String message) {
        super(message);
        this.productId = 0;
        this.productName = "";
        this.requestedQuantity = 0;
        this.availableQuantity = 0;
    }

    public InsufficientStockException(String productName, int requestedQuantity, int availableQuantity) {
        super(String.format("Insufficient stock for product '%s'. Requested: %d, Available: %d",
                productName, requestedQuantity, availableQuantity));
        this.productId = 0;
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public InsufficientStockException(int productId, String productName, int requestedQuantity, int availableQuantity) {
        super(String.format("Insufficient stock for product '%s' (ID: %d). Requested: %d, Available: %d",
                productName, productId, requestedQuantity, availableQuantity));
        this.productId = productId;
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }
}
