package com.syos.web.dto.request;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for billing API requests.
 */
public class BillingRequest {

    private String storeType;
    private Integer customerId;
    private List<BillItemRequest> items;
    private BigDecimal cashTendered;
    private BigDecimal discount;

    public BillingRequest() {
    }

    // Inner class for bill items
    public static class BillItemRequest {
        private String productCode;
        private int quantity;

        public BillItemRequest() {
        }

        public String getProductCode() {
            return productCode;
        }

        public void setProductCode(String productCode) {
            this.productCode = productCode;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    // Getters and Setters

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public List<BillItemRequest> getItems() {
        return items;
    }

    public void setItems(List<BillItemRequest> items) {
        this.items = items;
    }

    public BigDecimal getCashTendered() {
        return cashTendered;
    }

    public void setCashTendered(BigDecimal cashTendered) {
        this.cashTendered = cashTendered;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }
}
