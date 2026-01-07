package com.syos.domain.models;

import com.syos.domain.enums.StoreType;
import com.syos.domain.enums.TransactionType;
import com.syos.domain.valueobjects.BillSerialNumber;
import com.syos.domain.valueobjects.Money;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entity representing a sales bill/invoice.
 * Supports both physical store (cash) and online transactions.
 */
public class Bill {

    private Integer billId;
    private BillSerialNumber serialNumber;
    private Integer customerId; // null for walk-in physical store customers
    private TransactionType transactionType;
    private StoreType storeType;
    private Money subtotal;
    private Money discountAmount;
    private Money taxAmount;
    private Money totalAmount;
    private Money tenderedAmount; // For cash transactions
    private Money changeAmount; // For cash transactions
    private String cashierId;
    private LocalDateTime billDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private final List<BillItem> items;

    // Additional display fields
    private String customerName;
    private String customerEmail;

    public Bill() {
        this.items = new ArrayList<>();
        this.subtotal = Money.ZERO;
        this.discountAmount = Money.ZERO;
        this.taxAmount = Money.ZERO;
        this.totalAmount = Money.ZERO;
        this.billDate = LocalDateTime.now();
    }

    public Bill(BillSerialNumber serialNumber, StoreType storeType, TransactionType transactionType) {
        this();
        this.serialNumber = serialNumber;
        this.storeType = storeType;
        this.transactionType = transactionType;
    }

    // Business Methods

    /**
     * Adds an item to the bill and recalculates totals.
     */
    public void addItem(BillItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Bill item cannot be null");
        }
        items.add(item);
        calculateTotals();
    }

    /**
     * Removes an item from the bill and recalculates totals.
     */
    public void removeItem(BillItem item) {
        items.remove(item);
        calculateTotals();
    }

    /**
     * Removes an item by product code and recalculates totals.
     */
    public void removeItemByProductCode(String productCode) {
        items.removeIf(item ->
            item.getProductCode() != null &&
            item.getProductCode().getCode().equals(productCode)
        );
        calculateTotals();
    }

    /**
     * Clears all items from the bill.
     */
    public void clearItems() {
        items.clear();
        calculateTotals();
    }

    /**
     * Recalculates subtotal and total amounts.
     */
    public void calculateTotals() {
        Money newSubtotal = Money.ZERO;
        for (BillItem item : items) {
            if (item.getTotalPrice() != null) {
                newSubtotal = newSubtotal.add(item.getTotalPrice());
            }
        }
        this.subtotal = newSubtotal;
        this.totalAmount = subtotal.subtract(
            discountAmount != null ? discountAmount : Money.ZERO
        );
    }

    /**
     * Applies a discount to the bill.
     */
    public void applyDiscount(Money discount) {
        if (discount != null && discount.isGreaterThan(subtotal)) {
            throw new IllegalArgumentException("Discount cannot exceed subtotal");
        }
        this.discountAmount = discount != null ? discount : Money.ZERO;
        calculateTotals();
    }

    /**
     * Processes a cash payment.
     */
    public void processCashPayment(Money tendered) {
        if (tendered == null) {
            throw new IllegalArgumentException("Cash tendered cannot be null");
        }
        if (tendered.isLessThan(totalAmount)) {
            throw new IllegalArgumentException(
                "Insufficient cash. Required: " + totalAmount.format() +
                ", Tendered: " + tendered.format()
            );
        }
        this.tenderedAmount = tendered;
        this.changeAmount = new Money(
            tendered.getAmount().subtract(totalAmount.getAmount())
        );
    }

    /**
     * Checks if the bill has any items.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Returns the number of items in the bill.
     */
    public int getItemCount() {
        return items.size();
    }

    /**
     * Returns the total quantity of all items.
     */
    public int getTotalQuantity() {
        return items.stream()
            .mapToInt(BillItem::getQuantity)
            .sum();
    }

    // Getters and Setters

    public Integer getBillId() {
        return billId;
    }

    public void setBillId(Integer billId) {
        this.billId = billId;
    }

    public BillSerialNumber getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(BillSerialNumber serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getSerialNumberString() {
        return serialNumber != null ? serialNumber.getValue() : null;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public StoreType getStoreType() {
        return storeType;
    }

    public void setStoreType(StoreType storeType) {
        this.storeType = storeType;
    }

    public Money getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Money subtotal) {
        this.subtotal = subtotal;
    }

    public Money getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Money discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Money totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Money getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(Money taxAmount) {
        this.taxAmount = taxAmount;
    }

    public Money getTenderedAmount() {
        return tenderedAmount;
    }

    public void setTenderedAmount(Money tenderedAmount) {
        this.tenderedAmount = tenderedAmount;
    }

    public Money getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(Money changeAmount) {
        this.changeAmount = changeAmount;
    }

    public String getCashierId() {
        return cashierId;
    }

    public void setCashierId(String cashierId) {
        this.cashierId = cashierId;
    }

    public LocalDateTime getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDateTime billDate) {
        this.billDate = billDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<BillItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void setItems(List<BillItem> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        calculateTotals();
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    @Override
    public String toString() {
        return "Bill{" +
                "billId=" + billId +
                ", serialNumber=" + serialNumber +
                ", storeType=" + storeType +
                ", transactionType=" + transactionType +
                ", totalAmount=" + totalAmount +
                ", itemCount=" + items.size() +
                '}';
    }
}
