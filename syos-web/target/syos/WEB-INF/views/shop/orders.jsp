<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Orders - SYOS</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        'syos-primary': '#2563eb',
                        'syos-secondary': '#7c3aed',
                        'syos-success': '#059669',
                        'syos-warning': '#d97706',
                        'syos-danger': '#dc2626',
                    }
                }
            }
        }
    </script>
</head>
<body class="bg-gray-50 min-h-screen">
    <!-- Navigation -->
    <nav class="bg-white shadow-sm sticky top-0 z-50">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex justify-between h-16">
                <div class="flex items-center">
                    <a href="${pageContext.request.contextPath}/shop" class="flex items-center">
                        <div class="h-10 w-10 bg-syos-primary rounded-lg flex items-center justify-center">
                            <svg class="h-6 w-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"/>
                            </svg>
                        </div>
                        <span class="ml-2 text-xl font-bold text-gray-900">SYOS Shop</span>
                    </a>
                </div>

                <div class="flex items-center space-x-4">
                    <a href="${pageContext.request.contextPath}/shop" class="text-gray-600 hover:text-syos-primary">
                        Continue Shopping
                    </a>
                    <a href="${pageContext.request.contextPath}/cart" class="relative p-2 text-gray-600 hover:text-syos-primary">
                        <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"/>
                        </svg>
                    </a>
                </div>
            </div>
        </div>
    </nav>

    <!-- Main Content -->
    <main class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <!-- Page Header -->
        <div class="mb-8">
            <h1 class="text-3xl font-bold text-gray-900">My Orders</h1>
            <p class="mt-2 text-gray-600">Track and manage your orders</p>
        </div>

        <!-- Loading State -->
        <div id="loadingState" class="flex justify-center items-center py-12">
            <svg class="animate-spin h-8 w-8 text-syos-primary" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <span class="ml-2 text-gray-600">Loading orders...</span>
        </div>

        <!-- Empty State -->
        <div id="emptyState" class="hidden text-center py-12">
            <svg class="mx-auto h-16 w-16 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
            </svg>
            <h3 class="mt-4 text-lg font-medium text-gray-900">No orders yet</h3>
            <p class="mt-2 text-gray-500">When you place an order, it will appear here.</p>
            <a href="${pageContext.request.contextPath}/shop"
               class="mt-6 inline-flex items-center px-6 py-3 bg-syos-primary text-white rounded-lg hover:bg-blue-700 transition-colors">
                Start Shopping
            </a>
        </div>

        <!-- Orders List -->
        <div id="ordersContent" class="hidden space-y-6">
            <!-- Orders will be loaded here -->
        </div>
    </main>

    <!-- Order Detail Modal -->
    <div id="orderModal" class="hidden fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white rounded-xl shadow-xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
            <div class="sticky top-0 bg-white border-b px-6 py-4 flex items-center justify-between">
                <h3 class="text-lg font-semibold text-gray-900">Order Details</h3>
                <button onclick="closeModal()" class="text-gray-400 hover:text-gray-600">
                    <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                    </svg>
                </button>
            </div>
            <div id="modalContent" class="p-6">
                <!-- Order details will be loaded here -->
            </div>
        </div>
    </div>

    <!-- Toast Notification -->
    <div id="toast" class="hidden fixed bottom-4 right-4 bg-syos-success text-white px-6 py-3 rounded-lg shadow-lg flex items-center space-x-2">
        <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
        </svg>
        <span id="toastMessage">Updated!</span>
    </div>

    <script>
        const ctx = '${pageContext.request.contextPath}';
        let orders = [];

        const statusColors = {
            'PENDING': 'bg-yellow-100 text-yellow-800',
            'CONFIRMED': 'bg-blue-100 text-blue-800',
            'PROCESSING': 'bg-purple-100 text-purple-800',
            'SHIPPED': 'bg-indigo-100 text-indigo-800',
            'DELIVERED': 'bg-green-100 text-green-800',
            'CANCELLED': 'bg-red-100 text-red-800',
            'REFUNDED': 'bg-gray-100 text-gray-800'
        };

        const statusIcons = {
            'PENDING': '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>',
            'CONFIRMED': '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>',
            'PROCESSING': '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>',
            'SHIPPED': '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16V6a1 1 0 00-1-1H4a1 1 0 00-1 1v10a1 1 0 001 1h1m8-1a1 1 0 01-1 1H9m4-1V8a1 1 0 011-1h2.586a1 1 0 01.707.293l3.414 3.414a1 1 0 01.293.707V16a1 1 0 01-1 1h-1m-6-1a1 1 0 001 1h1M5 17a2 2 0 104 0m-4 0a2 2 0 114 0m6 0a2 2 0 104 0m-4 0a2 2 0 114 0"/>',
            'DELIVERED': '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>',
            'CANCELLED': '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>',
            'REFUNDED': '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6"/>'
        };

        async function loadOrders() {
            try {
                const response = await fetch(ctx + '/api/orders');
                const data = await response.json();

                document.getElementById('loadingState').classList.add('hidden');

                if (!data.success) {
                    if (response.status === 401) {
                        window.location.href = ctx + '/login?redirect=' + encodeURIComponent(window.location.pathname);
                        return;
                    }
                    throw new Error(data.error);
                }

                orders = data.data?.orders || [];

                if (orders.length === 0) {
                    document.getElementById('emptyState').classList.remove('hidden');
                } else {
                    renderOrders();
                    document.getElementById('ordersContent').classList.remove('hidden');
                }
            } catch (error) {
                console.error('Failed to load orders:', error);
                document.getElementById('emptyState').classList.remove('hidden');
            }
        }

        function renderOrders() {
            const container = document.getElementById('ordersContent');
            container.innerHTML = orders.map(order => `
                <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
                    <div class="p-6">
                        <div class="flex items-center justify-between mb-4">
                            <div>
                                <h3 class="text-lg font-semibold text-gray-900">\${order.orderNumber}</h3>
                                <p class="text-sm text-gray-500">\${formatDate(order.orderDate)}</p>
                            </div>
                            <span class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium \${statusColors[order.status] || 'bg-gray-100 text-gray-800'}">
                                <svg class="h-4 w-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    \${statusIcons[order.status] || ''}
                                </svg>
                                \${order.status}
                            </span>
                        </div>

                        <div class="grid grid-cols-2 gap-4 mb-4 text-sm">
                            <div>
                                <span class="text-gray-500">Items:</span>
                                <span class="ml-2 font-medium text-gray-900">\${order.itemCount || order.items?.length || 0}</span>
                            </div>
                            <div>
                                <span class="text-gray-500">Total:</span>
                                <span class="ml-2 font-medium text-gray-900">\${formatCurrency(order.totalAmount)}</span>
                            </div>
                        </div>

                        <div class="flex items-center justify-between pt-4 border-t border-gray-200">
                            <button onclick="viewOrderDetails(\${order.orderId})"
                                    class="text-syos-primary hover:text-blue-700 font-medium text-sm">
                                View Details
                            </button>
                            \${canCancel(order.status) ? `
                                <button onclick="cancelOrder(\${order.orderId})"
                                        class="text-syos-danger hover:text-red-700 font-medium text-sm">
                                    Cancel Order
                                </button>
                            ` : ''}
                        </div>
                    </div>
                </div>
            `).join('');
        }

        async function viewOrderDetails(orderId) {
            try {
                const response = await fetch(ctx + '/api/orders/' + orderId);
                const data = await response.json();

                if (data.success) {
                    const order = data.data;
                    renderOrderModal(order);
                    document.getElementById('orderModal').classList.remove('hidden');
                }
            } catch (error) {
                console.error('Failed to load order details:', error);
            }
        }

        function renderOrderModal(order) {
            const content = document.getElementById('modalContent');
            content.innerHTML = `
                <div class="space-y-6">
                    <!-- Order Info -->
                    <div class="flex items-center justify-between">
                        <div>
                            <h4 class="text-xl font-bold text-gray-900">\${order.orderNumber}</h4>
                            <p class="text-sm text-gray-500">\${formatDate(order.orderDate)}</p>
                        </div>
                        <span class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium \${statusColors[order.status] || 'bg-gray-100 text-gray-800'}">
                            \${order.status}
                        </span>
                    </div>

                    <!-- Order Items -->
                    <div>
                        <h5 class="font-semibold text-gray-900 mb-3">Order Items</h5>
                        <div class="bg-gray-50 rounded-lg divide-y divide-gray-200">
                            \${order.items.map(item => `
                                <div class="p-4 flex items-center justify-between">
                                    <div>
                                        <p class="font-medium text-gray-900">\${item.productName}</p>
                                        <p class="text-sm text-gray-500">\${item.productCode} × \${item.quantity}</p>
                                    </div>
                                    <span class="font-medium text-gray-900">\${formatCurrency(item.lineTotal)}</span>
                                </div>
                            `).join('')}
                        </div>
                    </div>

                    <!-- Shipping Info -->
                    \${order.shippingAddress ? `
                        <div>
                            <h5 class="font-semibold text-gray-900 mb-2">Shipping Address</h5>
                            <p class="text-gray-600">\${order.shippingAddress}</p>
                            \${order.contactPhone ? `<p class="text-gray-600 mt-1">Phone: \${order.contactPhone}</p>` : ''}
                        </div>
                    ` : ''}

                    <!-- Notes -->
                    \${order.notes ? `
                        <div>
                            <h5 class="font-semibold text-gray-900 mb-2">Delivery Notes</h5>
                            <p class="text-gray-600">\${order.notes}</p>
                        </div>
                    ` : ''}

                    <!-- Order Summary -->
                    <div class="border-t border-gray-200 pt-4 space-y-2">
                        <div class="flex justify-between text-gray-600">
                            <span>Subtotal</span>
                            <span>\${formatCurrency(order.subtotal)}</span>
                        </div>
                        <div class="flex justify-between text-gray-600">
                            <span>Shipping</span>
                            <span class="text-syos-success">Free</span>
                        </div>
                        <div class="flex justify-between text-lg font-semibold text-gray-900 pt-2 border-t">
                            <span>Total</span>
                            <span>\${formatCurrency(order.totalAmount)}</span>
                        </div>
                    </div>

                    <!-- Actions -->
                    \${canCancel(order.status) ? `
                        <div class="pt-4 border-t border-gray-200">
                            <button onclick="cancelOrder(\${order.orderId}); closeModal();"
                                    class="w-full py-2 px-4 border border-syos-danger text-syos-danger rounded-lg hover:bg-red-50 transition-colors">
                                Cancel Order
                            </button>
                        </div>
                    ` : ''}
                </div>
            `;
        }

        function closeModal() {
            document.getElementById('orderModal').classList.add('hidden');
        }

        async function cancelOrder(orderId) {
            if (!confirm('Are you sure you want to cancel this order?')) return;

            try {
                const response = await fetch(ctx + '/api/orders/' + orderId + '/cancel', {
                    method: 'PUT'
                });

                const data = await response.json();
                if (data.success) {
                    showToast('Order cancelled successfully');
                    loadOrders();
                } else {
                    showToast(data.error || 'Failed to cancel order', true);
                }
            } catch (error) {
                showToast('Error cancelling order', true);
            }
        }

        function canCancel(status) {
            return status === 'PENDING' || status === 'CONFIRMED';
        }

        function formatCurrency(amount) {
            return 'Rs. ' + parseFloat(amount).toFixed(2);
        }

        function formatDate(dateString) {
            const date = new Date(dateString);
            return date.toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        }

        function showToast(message, isError = false) {
            const toast = document.getElementById('toast');
            const toastMessage = document.getElementById('toastMessage');
            toastMessage.textContent = message;
            toast.className = `fixed bottom-4 right-4 \${isError ? 'bg-syos-danger' : 'bg-syos-success'} text-white px-6 py-3 rounded-lg shadow-lg flex items-center space-x-2`;
            setTimeout(() => toast.classList.add('hidden'), 3000);
        }

        // Close modal on outside click
        document.getElementById('orderModal').addEventListener('click', (e) => {
            if (e.target === document.getElementById('orderModal')) {
                closeModal();
            }
        });

        // Initialize
        loadOrders();
    </script>
</body>
</html>
