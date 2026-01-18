package com.syos.domain;

import com.syos.domain.enums.OrderStatus;
import com.syos.domain.enums.TransactionType;
import com.syos.domain.models.Order;
import com.syos.domain.models.OrderItem;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Order model.
 */
class OrderTest {

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create order with default constructor")
        void shouldCreateOrderWithDefaultConstructor() {
            // Act
            Order order = new Order();

            // Assert
            assertEquals(OrderStatus.PENDING, order.getStatus());
            assertNotNull(order.getOrderDate());
            assertNotNull(order.getItems());
            assertTrue(order.getItems().isEmpty());
            assertEquals(0, order.getSubtotal().getAmount().intValue());
            assertEquals(0, order.getTotalAmount().getAmount().intValue());
        }

        @Test
        @DisplayName("Should create order with customer id")
        void shouldCreateOrderWithCustomerId() {
            // Act
            Order order = new Order(42);

            // Assert
            assertEquals(42, order.getCustomerId());
            assertEquals(OrderStatus.PENDING, order.getStatus());
        }
    }

    @Nested
    @DisplayName("State transition tests")
    class StateTransitionTests {

        @Test
        @DisplayName("Should confirm pending order")
        void shouldConfirmPendingOrder() {
            // Arrange
            Order order = new Order();
            assertEquals(OrderStatus.PENDING, order.getStatus());

            // Act
            order.confirm();

            // Assert
            assertEquals(OrderStatus.CONFIRMED, order.getStatus());
            assertNotNull(order.getConfirmedAt());
        }

        @Test
        @DisplayName("Should throw when confirming non-pending order")
        void shouldThrowWhenConfirmingNonPendingOrder() {
            // Arrange
            Order order = new Order();
            order.confirm();
            assertEquals(OrderStatus.CONFIRMED, order.getStatus());

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> order.confirm());
        }

        @Test
        @DisplayName("Should start processing confirmed order")
        void shouldStartProcessingConfirmedOrder() {
            // Arrange
            Order order = new Order();
            order.confirm();

            // Act
            order.startProcessing();

            // Assert
            assertEquals(OrderStatus.PROCESSING, order.getStatus());
        }

        @Test
        @DisplayName("Should throw when processing non-confirmed order")
        void shouldThrowWhenProcessingNonConfirmedOrder() {
            // Arrange
            Order order = new Order(); // PENDING

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> order.startProcessing());
        }

        @Test
        @DisplayName("Should ship processing order")
        void shouldShipProcessingOrder() {
            // Arrange
            Order order = new Order();
            order.confirm();
            order.startProcessing();

            // Act
            order.ship();

            // Assert
            assertEquals(OrderStatus.SHIPPED, order.getStatus());
            assertNotNull(order.getShippedAt());
        }

        @Test
        @DisplayName("Should throw when shipping non-processing order")
        void shouldThrowWhenShippingNonProcessingOrder() {
            // Arrange
            Order order = new Order();
            order.confirm();

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> order.ship());
        }

        @Test
        @DisplayName("Should deliver shipped order")
        void shouldDeliverShippedOrder() {
            // Arrange
            Order order = new Order();
            order.confirm();
            order.startProcessing();
            order.ship();

            // Act
            order.deliver();

            // Assert
            assertEquals(OrderStatus.DELIVERED, order.getStatus());
            assertNotNull(order.getDeliveredAt());
        }

        @Test
        @DisplayName("Should throw when delivering non-shipped order")
        void shouldThrowWhenDeliveringNonShippedOrder() {
            // Arrange
            Order order = new Order();
            order.confirm();
            order.startProcessing();

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> order.deliver());
        }

        @Test
        @DisplayName("Should cancel pending order")
        void shouldCancelPendingOrder() {
            // Arrange
            Order order = new Order();

            // Act
            order.cancel();

            // Assert
            assertEquals(OrderStatus.CANCELLED, order.getStatus());
            assertNotNull(order.getCancelledAt());
        }

        @Test
        @DisplayName("Should throw when cancelling delivered order")
        void shouldThrowWhenCancellingDeliveredOrder() {
            // Arrange
            Order order = new Order();
            order.confirm();
            order.startProcessing();
            order.ship();
            order.deliver();

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> order.cancel());
        }
    }

    @Nested
    @DisplayName("calculateTotals tests")
    class CalculateTotalsTests {

        @Test
        @DisplayName("Should calculate subtotal from items")
        void shouldCalculateSubtotalFromItems() {
            // Arrange
            Order order = new Order();
            order.addItem(createTestOrderItem("P1", 2, 50.00));
            order.addItem(createTestOrderItem("P2", 3, 30.00));

            // Act
            order.calculateTotals();

            // Assert - 2*50 + 3*30 = 100 + 90 = 190
            assertEquals(190, order.getSubtotal().getAmount().intValue());
            assertEquals(190, order.getTotalAmount().getAmount().intValue());
        }

        @Test
        @DisplayName("Should include shipping fee in total")
        void shouldIncludeShippingFeeInTotal() {
            // Arrange
            Order order = new Order();
            order.addItem(createTestOrderItem("P1", 1, 100.00));
            order.calculateTotals();

            // Act
            order.setShippingFee(new Money(BigDecimal.valueOf(15.00)));

            // Assert - 100 + 15 = 115
            assertEquals(115, order.getTotalAmount().getAmount().intValue());
        }

        @Test
        @DisplayName("Should subtract discount from total")
        void shouldSubtractDiscountFromTotal() {
            // Arrange
            Order order = new Order();
            order.addItem(createTestOrderItem("P1", 1, 100.00));
            order.calculateTotals();

            // Act
            order.setDiscountAmount(new Money(BigDecimal.valueOf(20.00)));

            // Assert - 100 - 20 = 80
            assertEquals(80, order.getTotalAmount().getAmount().intValue());
        }

        @Test
        @DisplayName("Should include tax in total")
        void shouldIncludeTaxInTotal() {
            // Arrange
            Order order = new Order();
            order.addItem(createTestOrderItem("P1", 1, 100.00));
            order.calculateTotals();

            // Act
            order.setTaxAmount(new Money(BigDecimal.valueOf(10.00)));

            // Assert - 100 + 10 = 110
            assertEquals(110, order.getTotalAmount().getAmount().intValue());
        }
    }

    @Nested
    @DisplayName("Item management tests")
    class ItemManagementTests {

        @Test
        @DisplayName("Should add item to order")
        void shouldAddItemToOrder() {
            // Arrange
            Order order = new Order();
            order.setOrderId(1);
            OrderItem item = createTestOrderItem("P1", 2, 25.00);

            // Act
            order.addItem(item);

            // Assert
            assertEquals(1, order.getItems().size());
            assertEquals(1, item.getOrderId()); // orderId propagated
        }

        @Test
        @DisplayName("Should get total item count")
        void shouldGetTotalItemCount() {
            // Arrange
            Order order = new Order();
            order.addItem(createTestOrderItem("P1", 3, 10.00));
            order.addItem(createTestOrderItem("P2", 5, 20.00));

            // Act
            int count = order.getTotalItemCount();

            // Assert - 3 + 5 = 8
            assertEquals(8, count);
        }

        @Test
        @DisplayName("Should set items list")
        void shouldSetItemsList() {
            // Arrange
            Order order = new Order();
            OrderItem item1 = createTestOrderItem("P1", 1, 10.00);
            OrderItem item2 = createTestOrderItem("P2", 2, 20.00);

            // Act
            order.setItems(java.util.List.of(item1, item2));

            // Assert
            assertEquals(2, order.getItems().size());
        }

        @Test
        @DisplayName("Should handle null items list")
        void shouldHandleNullItemsList() {
            // Arrange
            Order order = new Order();

            // Act
            order.setItems(null);

            // Assert
            assertNotNull(order.getItems());
            assertTrue(order.getItems().isEmpty());
        }
    }

    @Nested
    @DisplayName("Utility methods tests")
    class UtilityMethodsTests {

        @Test
        @DisplayName("canCancel should return true for pending order")
        void canCancelShouldReturnTrueForPendingOrder() {
            Order order = new Order();
            assertTrue(order.canCancel());
        }

        @Test
        @DisplayName("canCancel should return false for delivered order")
        void canCancelShouldReturnFalseForDeliveredOrder() {
            Order order = new Order();
            order.confirm();
            order.startProcessing();
            order.ship();
            order.deliver();
            assertFalse(order.canCancel());
        }

        @Test
        @DisplayName("isComplete should return true for delivered order")
        void isCompleteShouldReturnTrueForDeliveredOrder() {
            Order order = new Order();
            order.confirm();
            order.startProcessing();
            order.ship();
            order.deliver();
            assertTrue(order.isComplete());
        }

        @Test
        @DisplayName("isComplete should return false for pending order")
        void isCompleteShouldReturnFalseForPendingOrder() {
            Order order = new Order();
            assertFalse(order.isComplete());
        }
    }

    @Nested
    @DisplayName("toString tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return formatted string")
        void shouldReturnFormattedString() {
            // Arrange
            Order order = new Order(100);
            order.setOrderId(1);
            order.setOrderNumber("ORD-001");

            // Act
            String result = order.toString();

            // Assert
            assertTrue(result.contains("Order{"));
            assertTrue(result.contains("id=1"));
            assertTrue(result.contains("number=ORD-001"));
            assertTrue(result.contains("customer=100"));
            assertTrue(result.contains("status=PENDING"));
        }
    }

    // Helper method
    private OrderItem createTestOrderItem(String code, int quantity, double unitPrice) {
        OrderItem item = new OrderItem();
        item.setProductCode(new ProductCode(code));
        item.setProductName("Test Product " + code);
        item.setQuantity(quantity);
        item.setUnitPrice(new Money(BigDecimal.valueOf(unitPrice)));
        item.setLineTotal(new Money(BigDecimal.valueOf(unitPrice * quantity)));
        return item;
    }
}
