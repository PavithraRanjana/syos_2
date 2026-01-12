<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Checkout - SYOS</title>
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
            <nav class="bg-white shadow-sm">
                <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div class="flex justify-between h-16">
                        <div class="flex items-center">
                            <a href="${pageContext.request.contextPath}/shop" class="flex items-center">
                                <div class="h-10 w-10 bg-syos-primary rounded-lg flex items-center justify-center">
                                    <svg class="h-6 w-6 text-white" fill="none" stroke="currentColor"
                                        viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                                    </svg>
                                </div>
                                <span class="ml-2 text-xl font-bold text-gray-900">SYOS Checkout</span>
                            </a>
                        </div>

                        <div class="flex items-center">
                            <a href="${pageContext.request.contextPath}/cart"
                                class="text-gray-600 hover:text-syos-primary flex items-center">
                                <svg class="h-5 w-5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                                </svg>
                                Back to Cart
                            </a>
                        </div>
                    </div>
                </div>
            </nav>

            <!-- Progress Steps -->
            <div class="bg-white border-b">
                <div class="max-w-3xl mx-auto px-4 py-4">
                    <div class="flex items-center justify-center space-x-4">
                        <div class="flex items-center">
                            <div
                                class="h-8 w-8 bg-syos-success text-white rounded-full flex items-center justify-center">
                                <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M5 13l4 4L19 7" />
                                </svg>
                            </div>
                            <span class="ml-2 text-sm font-medium text-gray-900">Cart</span>
                        </div>
                        <div class="flex-1 h-0.5 bg-syos-primary"></div>
                        <div class="flex items-center">
                            <div
                                class="h-8 w-8 bg-syos-primary text-white rounded-full flex items-center justify-center">
                                2</div>
                            <span class="ml-2 text-sm font-medium text-gray-900">Checkout</span>
                        </div>
                        <div class="flex-1 h-0.5 bg-gray-200"></div>
                        <div class="flex items-center">
                            <div
                                class="h-8 w-8 bg-gray-200 text-gray-600 rounded-full flex items-center justify-center">
                                3</div>
                            <span class="ml-2 text-sm font-medium text-gray-500">Confirmation</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Main Content -->
            <main class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <!-- Loading State -->
                <div id="loadingState" class="flex justify-center items-center py-12">
                    <svg class="animate-spin h-8 w-8 text-syos-primary" fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4">
                        </circle>
                        <path class="opacity-75" fill="currentColor"
                            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                        </path>
                    </svg>
                    <span class="ml-2 text-gray-600">Loading checkout...</span>
                </div>

                <!-- Checkout Form -->
                <div id="checkoutContent" class="hidden">
                    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
                        <!-- Left Column - Form -->
                        <div class="lg:col-span-2 space-y-6">
                            <!-- Shipping Address -->
                            <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                                <h2 class="text-lg font-semibold text-gray-900 mb-4">Shipping Address</h2>
                                <div class="space-y-4">
                                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                                        <div>
                                            <label class="block text-sm font-medium text-gray-700 mb-1">First
                                                Name</label>
                                            <input type="text" id="firstName" required
                                                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-syos-primary focus:border-syos-primary">
                                        </div>
                                        <div>
                                            <label class="block text-sm font-medium text-gray-700 mb-1">Last
                                                Name</label>
                                            <input type="text" id="lastName" required
                                                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-syos-primary focus:border-syos-primary">
                                        </div>
                                    </div>
                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
                                        <input type="email" id="email" required
                                            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-syos-primary focus:border-syos-primary">
                                    </div>
                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">Phone</label>
                                        <input type="tel" id="phone" required
                                            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-syos-primary focus:border-syos-primary">
                                    </div>
                                    <div>
                                        <label class="block text-sm font-medium text-gray-700 mb-1">Street
                                            Address</label>
                                        <input type="text" id="streetAddress" required
                                            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-syos-primary focus:border-syos-primary">
                                    </div>
                                    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                                        <div>
                                            <label class="block text-sm font-medium text-gray-700 mb-1">City</label>
                                            <input type="text" id="city" required
                                                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-syos-primary focus:border-syos-primary">
                                        </div>
                                        <div>
                                            <label
                                                class="block text-sm font-medium text-gray-700 mb-1">State/Province</label>
                                            <input type="text" id="state"
                                                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-syos-primary focus:border-syos-primary">
                                        </div>
                                        <div>
                                            <label class="block text-sm font-medium text-gray-700 mb-1">Postal
                                                Code</label>
                                            <input type="text" id="postalCode"
                                                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-syos-primary focus:border-syos-primary">
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Delivery Notes -->
                            <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                                <h2 class="text-lg font-semibold text-gray-900 mb-4">Delivery Notes (Optional)</h2>
                                <textarea id="deliveryNotes" rows="3"
                                    placeholder="Any special instructions for delivery..."
                                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-syos-primary focus:border-syos-primary"></textarea>
                            </div>

                            <!-- Payment Method -->
                            <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                                <h2 class="text-lg font-semibold text-gray-900 mb-4">Payment Method</h2>
                                <div class="space-y-3">
                                    <label
                                        class="flex items-center p-4 border border-gray-300 rounded-lg cursor-pointer hover:border-syos-primary transition-colors">
                                        <input type="radio" name="paymentMethod" value="CASH_ON_DELIVERY" checked
                                            class="h-4 w-4 text-syos-primary focus:ring-syos-primary">
                                        <span class="ml-3">
                                            <span class="block font-medium text-gray-900">Cash on Delivery</span>
                                            <span class="block text-sm text-gray-500">Pay when you receive your
                                                order</span>
                                        </span>
                                    </label>
                                    <label
                                        class="flex items-center p-4 border border-gray-300 rounded-lg cursor-pointer hover:border-syos-primary transition-colors opacity-50">
                                        <input type="radio" name="paymentMethod" value="CARD" disabled
                                            class="h-4 w-4 text-syos-primary focus:ring-syos-primary">
                                        <span class="ml-3">
                                            <span class="block font-medium text-gray-900">Credit/Debit Card</span>
                                            <span class="block text-sm text-gray-500">Coming soon</span>
                                        </span>
                                    </label>
                                </div>
                            </div>
                        </div>

                        <!-- Right Column - Order Summary -->
                        <div class="lg:col-span-1">
                            <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 sticky top-24">
                                <h2 class="text-lg font-semibold text-gray-900 mb-4">Order Summary</h2>

                                <!-- Cart Items Preview -->
                                <div id="orderItems" class="space-y-3 max-h-64 overflow-y-auto mb-4">
                                    <!-- Items will be loaded here -->
                                </div>

                                <div class="border-t border-gray-200 pt-4 space-y-3">
                                    <div class="flex justify-between text-gray-600">
                                        <span>Subtotal</span>
                                        <span id="subtotal">Rs. 0.00</span>
                                    </div>
                                    <div class="flex justify-between text-gray-600">
                                        <span>Shipping</span>
                                        <span class="text-syos-success">Free</span>
                                    </div>
                                    <div class="border-t border-gray-200 pt-3">
                                        <div class="flex justify-between text-lg font-semibold text-gray-900">
                                            <span>Total</span>
                                            <span id="total">Rs. 0.00</span>
                                        </div>
                                    </div>
                                </div>

                                <button id="placeOrderBtn" onclick="placeOrder()"
                                    class="w-full mt-6 bg-syos-success text-white py-3 px-4 rounded-lg hover:bg-green-700 transition-colors flex items-center justify-center space-x-2 disabled:opacity-50 disabled:cursor-not-allowed">
                                    <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M5 13l4 4L19 7" />
                                    </svg>
                                    <span>Place Order</span>
                                </button>

                                <p class="mt-4 text-center text-xs text-gray-500">
                                    By placing this order, you agree to our Terms of Service and Privacy Policy.
                                </p>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Order Success Modal -->
                <div id="successModal"
                    class="hidden fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                    <div class="bg-white rounded-xl shadow-xl max-w-md w-full mx-4 p-8 text-center">
                        <div
                            class="h-16 w-16 bg-syos-success rounded-full flex items-center justify-center mx-auto mb-4">
                            <svg class="h-8 w-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M5 13l4 4L19 7" />
                            </svg>
                        </div>
                        <h3 class="text-2xl font-bold text-gray-900 mb-2">Order Placed!</h3>
                        <p class="text-gray-600 mb-4">Thank you for your order. We'll send you an email confirmation
                            shortly.</p>
                        <p class="text-sm text-gray-500 mb-6">Order Number: <span id="orderNumber"
                                class="font-semibold text-gray-900"></span></p>
                        <div class="space-y-3">
                            <a href="${pageContext.request.contextPath}/orders"
                                class="block w-full bg-syos-primary text-white py-3 px-4 rounded-lg hover:bg-blue-700 transition-colors">
                                View My Orders
                            </a>
                            <a href="${pageContext.request.contextPath}/shop"
                                class="block w-full border border-gray-300 text-gray-700 py-3 px-4 rounded-lg hover:bg-gray-50 transition-colors">
                                Continue Shopping
                            </a>
                        </div>
                    </div>
                </div>
            </main>

            <script>
                const ctx = '${pageContext.request.contextPath}';
                let cart = null;
                let customer = null;

                async function loadCheckoutData() {
                    try {
                        // Load cart data first
                        const cartResponse = await fetch(ctx + '/api/cart');
                        const cartData = await cartResponse.json();

                        document.getElementById('loadingState').classList.add('hidden');

                        if (!cartData.success || !cartData.data || !cartData.data.items || cartData.data.items.length === 0) {
                            window.location.href = ctx + '/cart';
                            return;
                        }

                        cart = cartData.data;
                        renderOrderSummary();

                        // Fetch full customer details (includes phone and address)
                        try {
                            const customerResponse = await fetch(ctx + '/api/auth/me');
                            const customerData = await customerResponse.json();
                            if (customerData.success && customerData.data) {
                                customer = customerData.data;
                                prefillCustomerInfo();
                            }
                        } catch (e) {
                            console.warn('Could not load customer data for prefill:', e);
                        }

                        document.getElementById('checkoutContent').classList.remove('hidden');

                    } catch (error) {
                        console.error('Failed to load checkout data:', error);
                        window.location.href = ctx + '/cart';
                    }
                }

                function renderOrderSummary() {
                    const container = document.getElementById('orderItems');
                    container.innerHTML = cart.items.map(item => `
                <div class="flex items-center space-x-3">
                    <div class="h-12 w-12 bg-gray-100 rounded flex items-center justify-center flex-shrink-0 overflow-hidden">
                        <img src="${ctx}/static/images/products/\${item.productCode}.png" 
                             alt="\${item.productName}"
                             class="h-full w-full object-contain"
                             onerror="this.onerror=null; this.parentElement.innerHTML='<svg class=\\'h-6 w-6 text-gray-400\\' fill=\\'none\\' stroke=\\'currentColor\\' viewBox=\\'0 0 24 24\\'><path stroke-linecap=\\'round\\' stroke-linejoin=\\'round\\' stroke-width=\\'1\\' d=\\'M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4\\'/></svg>';">
                    </div>
                    <div class="flex-1 min-w-0">
                        <p class="text-sm font-medium text-gray-900 truncate">\${item.productName}</p>
                        <p class="text-xs text-gray-500">Qty: \${item.quantity}</p>
                    </div>
                    <span class="text-sm font-medium text-gray-900">\${formatCurrency(item.lineTotal)}</span>
                </div>
            `).join('');

                    document.getElementById('subtotal').textContent = formatCurrency(cart.subtotal);
                    document.getElementById('total').textContent = formatCurrency(cart.subtotal);
                }

                function prefillCustomerInfo() {
                    if (customer) {
                        // Split customer name into first and last name (best effort)
                        const fullName = customer.name || '';
                        const nameParts = fullName.trim().split(/\s+/);
                        const firstName = nameParts[0] || '';
                        const lastName = nameParts.slice(1).join(' ') || '';

                        document.getElementById('firstName').value = firstName;
                        document.getElementById('lastName').value = lastName;
                        document.getElementById('email').value = customer.email || '';
                        document.getElementById('phone').value = customer.phone || '';

                        // Use the address field for street address
                        if (customer.address) {
                            document.getElementById('streetAddress').value = customer.address;
                        }
                    }
                }

                async function placeOrder() {
                    const btn = document.getElementById('placeOrderBtn');
                    btn.disabled = true;
                    btn.innerHTML = `
                <svg class="animate-spin h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                <span>Processing...</span>
            `;

                    // Validate form
                    const requiredFields = ['firstName', 'lastName', 'email', 'phone', 'streetAddress', 'city'];
                    for (const field of requiredFields) {
                        const input = document.getElementById(field);
                        if (!input.value.trim()) {
                            input.focus();
                            input.classList.add('border-syos-danger');
                            btn.disabled = false;
                            btn.innerHTML = `
                        <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                        </svg>
                        <span>Place Order</span>
                    `;
                            return;
                        }
                    }

                    // Build shipping address
                    const shippingAddress = [
                        document.getElementById('streetAddress').value,
                        document.getElementById('city').value,
                        document.getElementById('state').value,
                        document.getElementById('postalCode').value
                    ].filter(Boolean).join(', ');

                    const orderData = {
                        shippingAddress: shippingAddress,
                        contactPhone: document.getElementById('phone').value,
                        notes: document.getElementById('deliveryNotes').value || null
                    };

                    try {
                        const response = await fetch(ctx + '/api/orders', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify(orderData)
                        });

                        const data = await response.json();

                        if (data.success) {
                            document.getElementById('orderNumber').textContent = data.data.orderNumber;
                            document.getElementById('successModal').classList.remove('hidden');
                        } else {
                            alert(data.error || 'Failed to place order. Please try again.');
                            btn.disabled = false;
                            btn.innerHTML = `
                        <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                        </svg>
                        <span>Place Order</span>
                    `;
                        }
                    } catch (error) {
                        console.error('Order failed:', error);
                        alert('An error occurred. Please try again.');
                        btn.disabled = false;
                        btn.innerHTML = `
                    <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                    </svg>
                    <span>Place Order</span>
                `;
                    }
                }

                function formatCurrency(amount) {
                    return 'Rs. ' + parseFloat(amount).toFixed(2);
                }

                // Initialize
                loadCheckoutData();
            </script>
        </body>

        </html>