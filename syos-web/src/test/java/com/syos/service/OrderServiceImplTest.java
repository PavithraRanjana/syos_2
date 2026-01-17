package com.syos.service;

import com.syos.domain.enums.OrderStatus;
import com.syos.domain.models.*;
import com.syos.domain.valueobjects.Money;
import com.syos.exception.ValidationException;
import com.syos.repository.interfaces.OrderRepository;
import com.syos.service.impl.OrderServiceImpl;
import com.syos.service.interfaces.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @Mock
    private CustomerService customerService;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private BillingService billingService;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(
                orderRepository,
                cartService,
                customerService,
                inventoryService,
                billingService);
    }

    private Order createTestOrder(Integer orderId, Integer customerId, OrderStatus status) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setCustomerId(customerId);
        order.setOrderNumber("ORD-" + String.format("%08d", orderId));
        order.setStatus(status);
        order.setTotalAmount(new Money(500.00));
        return order;
    }

    @Nested
    @DisplayName("findById tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should find order by ID")
        void shouldFindOrderById() {
            // Arrange
            Order order = createTestOrder(1, 1, OrderStatus.PENDING);
            when(orderRepository.findById(1)).thenReturn(Optional.of(order));

            // Act
            Optional<Order> result = orderService.findById(1);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(1, result.get().getOrderId());
        }

        @Test
        @DisplayName("Should return empty when order not found")
        void shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(orderRepository.findById(999)).thenReturn(Optional.empty());

            // Act
            Optional<Order> result = orderService.findById(999);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findByOrderNumber tests")
    class FindByOrderNumberTests {

        @Test
        @DisplayName("Should find order by order number")
        void shouldFindOrderByOrderNumber() {
            // Arrange
            Order order = createTestOrder(1, 1, OrderStatus.PENDING);
            when(orderRepository.findByOrderNumber("ORD-00000001")).thenReturn(Optional.of(order));

            // Act
            Optional<Order> result = orderService.findByOrderNumber("ORD-00000001");

            // Assert
            assertTrue(result.isPresent());
        }
    }

    @Nested
    @DisplayName("findByCustomerId tests")
    class FindByCustomerIdTests {

        @Test
        @DisplayName("Should find orders by customer ID")
        void shouldFindOrdersByCustomerId() {
            // Arrange
            List<Order> orders = List.of(
                    createTestOrder(1, 1, OrderStatus.PENDING),
                    createTestOrder(2, 1, OrderStatus.DELIVERED));
            when(orderRepository.findByCustomerId(1)).thenReturn(orders);

            // Act
            List<Order> result = orderService.findByCustomerId(1);

            // Assert
            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("findByStatus tests")
    class FindByStatusTests {

        @Test
        @DisplayName("Should find orders by status")
        void shouldFindOrdersByStatus() {
            // Arrange
            List<Order> orders = List.of(
                    createTestOrder(1, 1, OrderStatus.PENDING),
                    createTestOrder(2, 2, OrderStatus.PENDING));
            when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(orders);

            // Act
            List<Order> result = orderService.findByStatus(OrderStatus.PENDING);

            // Assert
            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("findActiveOrders tests")
    class FindActiveOrdersTests {

        @Test
        @DisplayName("Should find active orders")
        void shouldFindActiveOrders() {
            // Arrange
            List<Order> orders = List.of(
                    createTestOrder(1, 1, OrderStatus.PROCESSING),
                    createTestOrder(2, 2, OrderStatus.SHIPPED));
            when(orderRepository.findActiveOrders()).thenReturn(orders);

            // Act
            List<Order> result = orderService.findActiveOrders();

            // Assert
            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("confirmOrder tests")
    class ConfirmOrderTests {

        @Test
        @DisplayName("Should confirm pending order")
        void shouldConfirmPendingOrder() {
            // Arrange
            Order order = createTestOrder(1, 1, OrderStatus.PENDING);
            when(orderRepository.findById(1)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            // Act
            Order result = orderService.confirmOrder(1);

            // Assert
            assertNotNull(result);
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void shouldThrowWhenOrderNotFound() {
            // Arrange
            when(orderRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ValidationException.class,
                    () -> orderService.confirmOrder(999));
        }
    }

    @Nested
    @DisplayName("startProcessing tests")
    class StartProcessingTests {

        @Test
        @DisplayName("Should start processing confirmed order")
        void shouldStartProcessingConfirmedOrder() {
            // Arrange
            Order order = createTestOrder(1, 1, OrderStatus.CONFIRMED);
            when(orderRepository.findById(1)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            // Act
            Order result = orderService.startProcessing(1);

            // Assert
            assertNotNull(result);
            verify(orderRepository).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("shipOrder tests")
    class ShipOrderTests {

        @Test
        @DisplayName("Should ship processing order")
        void shouldShipProcessingOrder() {
            // Arrange
            Order order = createTestOrder(1, 1, OrderStatus.PROCESSING);
            when(orderRepository.findById(1)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            // Act
            Order result = orderService.shipOrder(1);

            // Assert
            assertNotNull(result);
            verify(orderRepository).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("deliverOrder tests")
    class DeliverOrderTests {

        @Test
        @DisplayName("Should deliver shipped order")
        void shouldDeliverShippedOrder() {
            // Arrange
            Order order = createTestOrder(1, 1, OrderStatus.SHIPPED);
            when(orderRepository.findById(1)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            // Act
            Order result = orderService.deliverOrder(1);

            // Assert
            assertNotNull(result);
            verify(orderRepository).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("cancelOrder tests")
    class CancelOrderTests {

        @Test
        @DisplayName("Should cancel pending order")
        void shouldCancelPendingOrder() {
            // Arrange
            Order order = createTestOrder(1, 1, OrderStatus.PENDING);
            when(orderRepository.findById(1)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            // Act
            Order result = orderService.cancelOrder(1, "Customer request");

            // Assert
            assertNotNull(result);
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw exception when cancelling delivered order")
        void shouldThrowWhenCancellingDeliveredOrder() {
            // Arrange
            Order order = createTestOrder(1, 1, OrderStatus.DELIVERED);
            when(orderRepository.findById(1)).thenReturn(Optional.of(order));

            // Act & Assert
            assertThrows(ValidationException.class,
                    () -> orderService.cancelOrder(1, "Customer request"));
        }
    }

    @Nested
    @DisplayName("updateShippingAddress tests")
    class UpdateShippingAddressTests {

        @Test
        @DisplayName("Should update shipping address for pending order")
        void shouldUpdateShippingAddressForPendingOrder() {
            // Arrange
            Order order = createTestOrder(1, 1, OrderStatus.PENDING);
            when(orderRepository.findById(1)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            // Act
            Order result = orderService.updateShippingAddress(1, "New Address", "0771234567");

            // Assert
            assertNotNull(result);
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw exception when order already shipped")
        void shouldThrowWhenOrderAlreadyShipped() {
            // Arrange
            Order order = createTestOrder(1, 1, OrderStatus.SHIPPED);
            when(orderRepository.findById(1)).thenReturn(Optional.of(order));

            // Act & Assert
            assertThrows(ValidationException.class,
                    () -> orderService.updateShippingAddress(1, "New Address", "0771234567"));
        }
    }

    @Nested
    @DisplayName("getCustomerOrderStats tests")
    class GetCustomerOrderStatsTests {

        @Test
        @DisplayName("Should return customer order statistics")
        void shouldReturnCustomerOrderStats() {
            // Arrange
            List<Order> orders = List.of(
                    createTestOrder(1, 1, OrderStatus.DELIVERED),
                    createTestOrder(2, 1, OrderStatus.DELIVERED),
                    createTestOrder(3, 1, OrderStatus.PENDING));
            when(orderRepository.findByCustomerId(1)).thenReturn(orders);

            // Act
            var stats = orderService.getCustomerOrderStats(1);

            // Assert
            assertNotNull(stats);
            assertEquals(3, stats.totalOrders());
        }

        @Test
        @DisplayName("Should return empty stats for customer with no orders")
        void shouldReturnEmptyStatsForNoOrders() {
            // Arrange
            when(orderRepository.findByCustomerId(999)).thenReturn(List.of());

            // Act
            var stats = orderService.getCustomerOrderStats(999);

            // Assert
            assertNotNull(stats);
            assertEquals(0, stats.totalOrders());
        }
    }
}
