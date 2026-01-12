<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Shopping Cart - SYOS</title>
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
                                    <svg class="h-6 w-6 text-white" fill="none" stroke="currentColor"
                                        viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                                    </svg>
                                </div>
                                <span class="ml-2 text-xl font-bold text-gray-900">SYOS Shop</span>
                            </a>
                        </div>

                        <div class="flex items-center space-x-4">
                            <a href="${pageContext.request.contextPath}/shop"
                                class="text-gray-600 hover:text-syos-primary">
                                Continue Shopping
                            </a>
                            <a href="${pageContext.request.contextPath}/orders"
                                class="text-gray-600 hover:text-syos-primary">
                                My Orders
                            </a>
                        </div>
                    </div>
                </div>
            </nav>

            <!-- Main Content -->
            <main class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <!-- Page Header -->
                <div class="mb-8">
                    <h1 class="text-3xl font-bold text-gray-900">Shopping Cart</h1>
                    <p class="mt-2 text-gray-600">Review your items before checkout</p>
                </div>

                <!-- Loading State -->
                <div id="loadingState" class="flex justify-center items-center py-12">
                    <svg class="animate-spin h-8 w-8 text-syos-primary" fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4">
                        </circle>
                        <path class="opacity-75" fill="currentColor"
                            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                        </path>
                    </svg>
                    <span class="ml-2 text-gray-600">Loading cart...</span>
                </div>

                <!-- Empty Cart State -->
                <div id="emptyState" class="hidden text-center py-12">
                    <svg class="mx-auto h-16 w-16 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                            d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                    </svg>
                    <h3 class="mt-4 text-lg font-medium text-gray-900">Your cart is empty</h3>
                    <p class="mt-2 text-gray-500">Looks like you haven't added any items yet.</p>
                    <a href="${pageContext.request.contextPath}/shop"
                        class="mt-6 inline-flex items-center px-6 py-3 bg-syos-primary text-white rounded-lg hover:bg-blue-700 transition-colors">
                        <svg class="h-5 w-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                        </svg>
                        Start Shopping
                    </a>
                </div>

                <!-- Cart Content -->
                <div id="cartContent" class="hidden">
                    <!-- Cart Items -->
                    <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden mb-6">
                        <div id="cartItems" class="divide-y divide-gray-200">
                            <!-- Items will be loaded here -->
                        </div>
                    </div>

                    <!-- Stock Validation Warning -->
                    <div id="stockWarning" class="hidden bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
                        <div class="flex">
                            <svg class="h-5 w-5 text-yellow-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                            </svg>
                            <div class="ml-3">
                                <h3 class="text-sm font-medium text-yellow-800">Stock availability issues</h3>
                                <div id="stockIssues" class="mt-2 text-sm text-yellow-700">
                                    <!-- Issues will be listed here -->
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Cart Summary -->
                    <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                        <h2 class="text-lg font-semibold text-gray-900 mb-4">Order Summary</h2>

                        <div class="space-y-3">
                            <div class="flex justify-between text-gray-600">
                                <span>Subtotal (<span id="itemCount">0</span> items)</span>
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

                        <button id="checkoutBtn" onclick="proceedToCheckout()"
                            class="w-full mt-6 bg-syos-primary text-white py-3 px-4 rounded-lg hover:bg-blue-700 transition-colors flex items-center justify-center space-x-2 disabled:opacity-50 disabled:cursor-not-allowed">
                            <span>Proceed to Checkout</span>
                            <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M14 5l7 7m0 0l-7 7m7-7H3" />
                            </svg>
                        </button>

                        <p class="mt-4 text-center text-sm text-gray-500">
                            Secure checkout powered by SYOS
                        </p>
                    </div>
                </div>
            </main>

            <!-- Toast Notification -->
            <div id="toast"
                class="hidden fixed bottom-4 right-4 bg-syos-success text-white px-6 py-3 rounded-lg shadow-lg flex items-center space-x-2">
                <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
                <span id="toastMessage">Updated!</span>
            </div>

            <script>
                const ctx = '${pageContext.request.contextPath}';
                let cart = null;

                async function loadCart() {
                    try {
                        const response = await fetch(ctx + '/api/cart');
                        const data = await response.json();

                        document.getElementById('loadingState').classList.add('hidden');

                        if (!data.success) {
                            if (response.status === 401) {
                                window.location.href = ctx + '/login?redirect=' + encodeURIComponent(window.location.pathname);
                                return;
                            }
                            throw new Error(data.error);
                        }

                        cart = data.data;

                        if (!cart || !cart.items || cart.items.length === 0) {
                            document.getElementById('emptyState').classList.remove('hidden');
                        } else {
                            renderCart();
                            document.getElementById('cartContent').classList.remove('hidden');
                            validateCart();
                        }
                    } catch (error) {
                        console.error('Failed to load cart:', error);
                        document.getElementById('emptyState').classList.remove('hidden');
                    }
                }

                function renderCart() {
                    const container = document.getElementById('cartItems');
                    container.innerHTML = cart.items.map(item => `
                <div class="p-6 flex items-center space-x-4" data-product="\${item.productCode}">
                    <div class="h-20 w-20 bg-gradient-to-br from-gray-100 to-gray-200 rounded-lg flex items-center justify-center flex-shrink-0 overflow-hidden">
                        <img src="${ctx}/static/images/products/\${item.productCode}.png" 
                             alt="\${item.productName}"
                             class="h-full w-full object-contain p-1"
                             onerror="this.onerror=null; this.parentElement.innerHTML='<svg class=\\'h-10 w-10 text-gray-400\\' fill=\\'none\\' stroke=\\'currentColor\\' viewBox=\\'0 0 24 24\\'><path stroke-linecap=\\'round\\' stroke-linejoin=\\'round\\' stroke-width=\\'1\\' d=\\'M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4\\'/></svg>';">
                    </div>
                    <div class="flex-1 min-w-0">
                        <h3 class="font-semibold text-gray-900">\${item.productName}</h3>
                        <p class="text-sm text-gray-500">\${item.productCode}</p>
                        <p class="text-syos-primary font-medium">\${formatCurrency(item.unitPrice)}</p>
                    </div>
                    <div class="flex items-center space-x-3">
                        <button onclick="updateQuantity('\${item.productCode}', \${item.quantity - 1})"
                                class="h-8 w-8 rounded-full border border-gray-300 flex items-center justify-center hover:bg-gray-100 transition-colors">
                            <svg class="h-4 w-4 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4"/>
                            </svg>
                        </button>
                        <span class="w-8 text-center font-medium">\${item.quantity}</span>
                        <button onclick="updateQuantity('\${item.productCode}', \${item.quantity + 1})"
                                class="h-8 w-8 rounded-full border border-gray-300 flex items-center justify-center hover:bg-gray-100 transition-colors">
                            <svg class="h-4 w-4 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"/>
                            </svg>
                        </button>
                    </div>
                    <div class="text-right">
                        <p class="font-semibold text-gray-900">\${formatCurrency(item.lineTotal)}</p>
                        <button onclick="removeItem('\${item.productCode}')"
                                class="mt-1 text-sm text-syos-danger hover:underline">
                            Remove
                        </button>
                    </div>
                </div>
            `).join('');

                    updateSummary();
                }

                function updateSummary() {
                    const itemCount = cart.items.reduce((sum, item) => sum + item.quantity, 0);
                    document.getElementById('itemCount').textContent = itemCount;
                    document.getElementById('subtotal').textContent = formatCurrency(cart.subtotal);
                    document.getElementById('total').textContent = formatCurrency(cart.subtotal);
                }

                async function updateQuantity(productCode, newQuantity) {
                    if (newQuantity < 1) {
                        removeItem(productCode);
                        return;
                    }

                    try {
                        const response = await fetch(ctx + '/api/cart/items/' + productCode, {
                            method: 'PUT',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({ quantity: newQuantity })
                        });

                        const data = await response.json();
                        if (data.success) {
                            cart = data.data;
                            renderCart();
                            validateCart();
                            showToast('Quantity updated');
                        } else {
                            showToast(data.error || 'Failed to update quantity', true);
                        }
                    } catch (error) {
                        showToast('Error updating quantity', true);
                    }
                }

                async function removeItem(productCode) {
                    try {
                        const response = await fetch(ctx + '/api/cart/items/' + productCode, {
                            method: 'DELETE'
                        });

                        const data = await response.json();
                        if (data.success) {
                            cart = data.data;
                            if (cart.items.length === 0) {
                                document.getElementById('cartContent').classList.add('hidden');
                                document.getElementById('emptyState').classList.remove('hidden');
                            } else {
                                renderCart();
                                validateCart();
                            }
                            showToast('Item removed');
                        } else {
                            showToast(data.error || 'Failed to remove item', true);
                        }
                    } catch (error) {
                        showToast('Error removing item', true);
                    }
                }

                async function validateCart() {
                    try {
                        const response = await fetch(ctx + '/api/cart/validate');
                        const data = await response.json();

                        const warningDiv = document.getElementById('stockWarning');
                        const issuesDiv = document.getElementById('stockIssues');
                        const checkoutBtn = document.getElementById('checkoutBtn');

                        if (data.data && data.data.valid === false) {
                            const issues = data.data.issues || [];
                            issuesDiv.innerHTML = issues.map(issue =>
                                `<p>• \${issue.productName}: Only \${issue.available} available (requested \${issue.requested})</p>`
                            ).join('');
                            warningDiv.classList.remove('hidden');
                            checkoutBtn.disabled = true;
                        } else {
                            warningDiv.classList.add('hidden');
                            checkoutBtn.disabled = false;
                        }
                    } catch (error) {
                        console.error('Failed to validate cart:', error);
                    }
                }

                function proceedToCheckout() {
                    window.location.href = ctx + '/checkout';
                }

                function formatCurrency(amount) {
                    if (amount == null || amount === '' || amount === undefined) return 'Rs. 0.00';
                    const parsed = parseFloat(amount);
                    return 'Rs. ' + (isNaN(parsed) ? '0.00' : parsed.toFixed(2));
                }

                function showToast(message, isError = false) {
                    const toast = document.getElementById('toast');
                    const toastMessage = document.getElementById('toastMessage');
                    toastMessage.textContent = message;
                    toast.className = `fixed bottom-4 right-4 \${isError ? 'bg-syos-danger' : 'bg-syos-success'} text-white px-6 py-3 rounded-lg shadow-lg flex items-center space-x-2`;
                    setTimeout(() => toast.classList.add('hidden'), 3000);
                }

                // Initialize
                loadCart();
            </script>
        </body>

        </html>