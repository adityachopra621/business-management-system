package com.businessmgmt.model;

import java.math.BigDecimal;

/**
 * Holds aggregated dashboard statistics.
 * All values are computed from the database, not hardcoded.
 */
public class DashboardStats {

    private BigDecimal totalRevenue;
    private BigDecimal totalExpenses;
    private BigDecimal netProfit;
    private BigDecimal inventoryValue;
    private int lowStockCount;
    private int totalProducts;
    private int totalCustomers;
    private int totalSuppliers;

    public DashboardStats() {
        this.totalRevenue = BigDecimal.ZERO;
        this.totalExpenses = BigDecimal.ZERO;
        this.netProfit = BigDecimal.ZERO;
        this.inventoryValue = BigDecimal.ZERO;
    }

    // Getters and Setters

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getNetProfit() {
        return netProfit;
    }

    public void setNetProfit(BigDecimal netProfit) {
        this.netProfit = netProfit;
    }

    public BigDecimal getInventoryValue() {
        return inventoryValue;
    }

    public void setInventoryValue(BigDecimal inventoryValue) {
        this.inventoryValue = inventoryValue;
    }

    public int getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(int lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(int totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public int getTotalSuppliers() {
        return totalSuppliers;
    }

    public void setTotalSuppliers(int totalSuppliers) {
        this.totalSuppliers = totalSuppliers;
    }

    @Override
    public String toString() {
        return "DashboardStats{" +
                "totalRevenue=" + totalRevenue +
                ", totalExpenses=" + totalExpenses +
                ", netProfit=" + netProfit +
                ", inventoryValue=" + inventoryValue +
                ", lowStockCount=" + lowStockCount +
                ", totalProducts=" + totalProducts +
                ", totalCustomers=" + totalCustomers +
                ", totalSuppliers=" + totalSuppliers +
                '}';
    }
}
