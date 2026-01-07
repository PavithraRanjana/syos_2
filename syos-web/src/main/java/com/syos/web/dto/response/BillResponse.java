package com.syos.web.dto.response;

import com.syos.domain.models.Bill;
import com.syos.domain.models.BillItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for bill API responses.
 */
public class BillResponse {

    private Integer billId;
    private String serialNumber;
    private String storeType;
    private String transactionType;
    private String customerName;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal tenderedAmount;
    private BigDecimal changeAmount;
    private LocalDateTime billDate;
    private List<BillItemResponse> items;
    private int itemCount;
    private int totalQuantity;

    public BillResponse() {
    }

    /**
     * Creates a BillResponse from a Bill entity.
     */
    public static BillResponse fromBill(Bill bill) {
        BillResponse response = new BillResponse();
        response.billId = bill.getBillId();
        response.serialNumber = bill.getSerialNumberString();
        response.storeType = bill.getStoreType() != null ? bill.getStoreType().name() : null;
        response.transactionType = bill.getTransactionType() != null ?
            bill.getTransactionType().name() : null;
        response.customerName = bill.getCustomerName();
        response.subtotal = bill.getSubtotal() != null ?
            bill.getSubtotal().getAmount() : null;
        response.discountAmount = bill.getDiscountAmount() != null ?
            bill.getDiscountAmount().getAmount() : null;
        response.totalAmount = bill.getTotalAmount() != null ?
            bill.getTotalAmount().getAmount() : null;
        response.tenderedAmount = bill.getTenderedAmount() != null ?
            bill.getTenderedAmount().getAmount() : null;
        response.changeAmount = bill.getChangeAmount() != null ?
            bill.getChangeAmount().getAmount() : null;
        response.billDate = bill.getBillDate();
        response.items = bill.getItems().stream()
            .map(BillItemResponse::fromBillItem)
            .collect(Collectors.toList());
        response.itemCount = bill.getItemCount();
        response.totalQuantity = bill.getTotalQuantity();
        return response;
    }

    // Inner class for bill items
    public static class BillItemResponse {
        private String productCode;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private Integer batchNumber;

        public static BillItemResponse fromBillItem(BillItem item) {
            BillItemResponse response = new BillItemResponse();
            response.productCode = item.getProductCodeString();
            response.productName = item.getProductName();
            response.quantity = item.getQuantity();
            response.unitPrice = item.getUnitPrice() != null ?
                item.getUnitPrice().getAmount() : null;
            response.totalPrice = item.getTotalPrice() != null ?
                item.getTotalPrice().getAmount() : null;
            response.batchNumber = item.getBatchNumber();
            return response;
        }

        // Getters and Setters
        public String getProductCode() { return productCode; }
        public void setProductCode(String productCode) { this.productCode = productCode; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
        public Integer getBatchNumber() { return batchNumber; }
        public void setBatchNumber(Integer batchNumber) { this.batchNumber = batchNumber; }
    }

    // Getters and Setters

    public Integer getBillId() {
        return billId;
    }

    public void setBillId(Integer billId) {
        this.billId = billId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTenderedAmount() {
        return tenderedAmount;
    }

    public void setTenderedAmount(BigDecimal tenderedAmount) {
        this.tenderedAmount = tenderedAmount;
    }

    public BigDecimal getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(BigDecimal changeAmount) {
        this.changeAmount = changeAmount;
    }

    public LocalDateTime getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDateTime billDate) {
        this.billDate = billDate;
    }

    public List<BillItemResponse> getItems() {
        return items;
    }

    public void setItems(List<BillItemResponse> items) {
        this.items = items;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}
