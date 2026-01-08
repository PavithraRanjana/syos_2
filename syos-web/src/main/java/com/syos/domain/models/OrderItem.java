package com.syos.domain.models;

import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;

import java.math.BigDecimal;

/**
 * Represents a line item in an order.
 * Contains product details, quantity, and pricing at time of order.
 */
public class OrderItem {

    private Integer orderItemId;
    private Integer orderId;
    private ProductCode productCode;
    private String productName;
    private Integer mainInventoryId;
    private int quantity;
    private Money unitPrice;
    private Money lineTotal;

    public OrderItem() {
    }

    public OrderItem(ProductCode productCode, String productName, int quantity, Money unitPrice) {
        this.productCode = productCode;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public OrderItem(CartItem cartItem) {
        this(cartItem.getProductCode(), cartItem.getProductName(),
             cartItem.getQuantity(), cartItem.getUnitPrice());
    }

    public Integer getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Integer orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public ProductCode getProductCode() {
        return productCode;
    }

    public String getProductCodeString() {
        return productCode != null ? productCode.getCode() : null;
    }

    public void setProductCode(ProductCode productCode) {
        this.productCode = productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = new ProductCode(productCode);
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getMainInventoryId() {
        return mainInventoryId;
    }

    public void setMainInventoryId(Integer mainInventoryId) {
        this.mainInventoryId = mainInventoryId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        recalculateLineTotal();
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Money unitPrice) {
        this.unitPrice = unitPrice;
        recalculateLineTotal();
    }

    public Money getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(Money lineTotal) {
        this.lineTotal = lineTotal;
    }

    private void recalculateLineTotal() {
        if (unitPrice != null && quantity > 0) {
            this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    @Override
    public String toString() {
        return String.format("OrderItem{id=%d, product=%s, qty=%d, total=%s}",
            orderItemId, productCode, quantity, lineTotal);
    }
}
