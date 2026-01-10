<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
            <%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

                <t:layout pageTitle="New Sale" activeNav="pos">

                    <!-- Page Header -->
                    <div class="mb-6">
                        <nav class="flex mb-4" aria-label="Breadcrumb">
                            <ol class="inline-flex items-center space-x-1 md:space-x-3">
                                <li><a href="${pageContext.request.contextPath}/pos"
                                        class="text-gray-500 hover:text-gray-700">POS</a></li>
                                <li class="flex items-center">
                                    <svg class="w-4 h-4 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                                        <path fill-rule="evenodd"
                                            d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"
                                            clip-rule="evenodd" />
                                    </svg>
                                    <span class="ml-1 text-gray-700 font-medium">New Sale</span>
                                </li>
                            </ol>
                        </nav>
                        <h1 class="text-2xl font-bold text-gray-900">New Sale Transaction</h1>
                    </div>

                    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
                        <!-- Product Search & Add -->
                        <div class="lg:col-span-2 space-y-6">
                            <!-- Transaction Setup -->
                            <div class="card" id="setupCard">
                                <h2 class="card-header">Transaction Setup</h2>
                                <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                    <div>
                                        <label class="input-label">Store Type</label>
                                        <input type="text" class="input-field bg-gray-100" value="Physical Store"
                                            readonly>
                                        <input type="hidden" id="storeType" value="PHYSICAL">
                                    </div>
                                    <div>
                                        <label class="input-label">Payment Type</label>
                                        <input type="text" class="input-field bg-gray-100" value="Cash" readonly>
                                        <input type="hidden" id="transactionType" value="CASH">
                                    </div>
                                    <div>
                                        <label class="input-label">Cashier ID</label>
                                        <input type="text" id="cashierId" class="input-field" placeholder="Optional">
                                    </div>
                                    <input type="hidden" id="customerId" value="">
                                </div>
                            </div>

                            <!-- Add Items -->
                            <div class="card" id="addItemsCard">
                                <h2 class="card-header">Add Items</h2>
                                <div class="flex gap-4">
                                    <div class="flex-grow relative">
                                        <input type="text" id="productCode" class="input-field"
                                            placeholder="Enter product code..."
                                            onkeydown="if(event.key === 'Enter') addItem()">
                                    </div>
                                    <input type="number" id="itemQuantity" class="input-field w-24" value="1" min="1"
                                        placeholder="Qty">
                                    <button onclick="addItem()" class="btn-success">Add</button>
                                </div>

                                <!-- Quick Add Products -->
                                <c:if test="${not empty products}">
                                    <div class="mt-4">
                                        <p class="text-sm text-gray-500 mb-2">Quick Add:</p>
                                        <div class="flex flex-wrap gap-2">
                                            <c:forEach var="product" items="${products}" begin="0" end="9">
                                                <button onclick="quickAddProduct('${product.productCodeString}')"
                                                    class="px-3 py-1 text-sm bg-gray-100 hover:bg-gray-200 rounded-lg transition">
                                                    ${product.productName}
                                                </button>
                                            </c:forEach>
                                        </div>
                                    </div>
                                </c:if>
                            </div>
                        </div>

                        <!-- Bill Summary -->
                        <div class="card" id="billSummary">
                            <h2 class="card-header">
                                <span>Cart Summary</span>
                            </h2>

                            <!-- Items List -->
                            <div id="itemsList" class="space-y-2 mb-4 max-h-64 overflow-y-auto">
                                <p class="text-gray-500 text-center py-4">No items added yet</p>
                            </div>

                            <div class="border-t pt-4 space-y-2">
                                <div class="flex justify-between text-sm">
                                    <span class="text-gray-500">Subtotal</span>
                                    <span id="subtotal">Rs. 0.00</span>
                                </div>
                                <div class="flex justify-between text-sm">
                                    <span class="text-gray-500">Discount</span>
                                    <span id="discountDisplay">Rs. 0.00</span>
                                </div>
                                <div class="flex justify-between text-lg font-bold border-t pt-2">
                                    <span>Total</span>
                                    <span id="total">Rs. 0.00</span>
                                </div>
                            </div>

                            <!-- Payment Section -->
                            <div id="paymentSection" class="mt-4 pt-4 border-t">
                                <div class="space-y-4">
                                    <div id="cashPaymentDiv">
                                        <label class="input-label">Cash Tendered (Rs.)</label>
                                        <input type="number" id="tenderedAmount" class="input-field" min="0" step="0.01"
                                            placeholder="Enter amount" onchange="calculateChange()">
                                        <p class="mt-1 text-sm">Change: <span id="changeAmount" class="font-bold">Rs.
                                                0.00</span></p>
                                    </div>

                                    <div class="flex gap-2">
                                        <button onclick="applyDiscount()" class="btn-secondary flex-1">
                                            Apply Discount
                                        </button>
                                    </div>

                                    <button onclick="processCheckout()" class="btn-success w-full" id="checkoutBtn"
                                        disabled>
                                        <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                                d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z" />
                                        </svg>
                                        Process Payment
                                    </button>
                                </div>
                            </div>

                            <div class="mt-4 space-y-2">
                                <button onclick="clearCart()" class="btn-secondary w-full" id="clearCartBtn"
                                    style="display: none;">
                                    <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                    </svg>
                                    Clear Cart
                                </button>
                                <button onclick="cancelTransaction()" class="btn-danger w-full">
                                    <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M6 18L18 6M6 6l12 12" />
                                    </svg>
                                    Cancel Transaction
                                </button>
                            </div>
                        </div>
                    </div>

                    <script>
                        // ==================== Client-Side Cart State ====================
                        let cartItems = [];  // {productCode, productName, quantity, unitPrice, lineTotal}
                        let cartDiscount = 0;

                        function quickAddProduct(code) {
                            document.getElementById('productCode').value = code;
                            addItem();
                        }

                        async function addItem() {
                            const productCode = document.getElementById('productCode').value.trim();
                            const quantity = parseInt(document.getElementById('itemQuantity').value) || 1;
                            const storeType = document.getElementById('storeType').value;

                            if (!productCode) {
                                showNotification('Please enter a product code', 'error');
                                return;
                            }

                            if (quantity <= 0) {
                                showNotification('Quantity must be positive', 'error');
                                return;
                            }

                            try {
                                // Check stock and get product info
                                const response = await apiFetch('/billing/stock/' + productCode + '?quantity=' + quantity + '&storeType=' + storeType);

                                if (!response.success) {
                                    showNotification(response.error || 'Stock check failed', 'error');
                                    return;
                                }

                                const stockData = response.data;

                                // Check if product already in cart
                                const existingIndex = cartItems.findIndex(item => item.productCode === productCode);
                                if (existingIndex >= 0) {
                                    // Increase quantity
                                    const newQty = cartItems[existingIndex].quantity + quantity;

                                    // Re-check stock for new total quantity
                                    const recheck = await apiFetch('/billing/stock/' + productCode + '?quantity=' + newQty + '&storeType=' + storeType);
                                    if (!recheck.success) {
                                        showNotification(recheck.error || 'Insufficient stock for additional quantity', 'error');
                                        return;
                                    }

                                    cartItems[existingIndex].quantity = newQty;
                                    cartItems[existingIndex].lineTotal = cartItems[existingIndex].unitPrice * newQty;
                                } else {
                                    // Add new item
                                    cartItems.push({
                                        productCode: stockData.productCode,
                                        productName: stockData.productName,
                                        quantity: quantity,
                                        unitPrice: parseFloat(stockData.unitPrice),
                                        lineTotal: parseFloat(stockData.unitPrice) * quantity
                                    });
                                }

                                updateCartDisplay();
                                document.getElementById('productCode').value = '';
                                document.getElementById('itemQuantity').value = '1';
                                showNotification('Added ' + stockData.productName + ' x ' + quantity, 'success');

                            } catch (error) {
                                showNotification(error.message || 'Failed to add item', 'error');
                            }
                        }

                        function removeItem(index) {
                            const item = cartItems[index];
                            cartItems.splice(index, 1);
                            updateCartDisplay();
                            showNotification('Removed ' + item.productName, 'success');
                        }

                        function updateCartDisplay() {
                            const itemsList = document.getElementById('itemsList');

                            if (cartItems.length === 0) {
                                itemsList.innerHTML = '<p class="text-gray-500 text-center py-4">No items added yet</p>';
                            } else {
                                itemsList.innerHTML = cartItems.map((item, index) => `
                    <div class="flex justify-between items-center p-2 bg-gray-50 rounded">
                        <div class="flex-grow">
                            <div class="font-medium text-sm">\${item.productName}</div>
                            <div class="text-xs text-gray-500">\${item.quantity} x Rs. \${item.unitPrice.toFixed(2)}</div>
                        </div>
                        <div class="text-right">
                            <div class="font-medium">Rs. \${item.lineTotal.toFixed(2)}</div>
                            <button onclick="removeItem(\${index})" class="text-red-500 text-xs hover:underline">Remove</button>
                        </div>
                    </div>
                `).join('');
                            }

                            // Calculate totals
                            const subtotal = cartItems.reduce((sum, item) => sum + item.lineTotal, 0);
                            const total = Math.max(0, subtotal - cartDiscount);

                            document.getElementById('subtotal').textContent = formatCurrency(subtotal);
                            document.getElementById('discountDisplay').textContent = formatCurrency(cartDiscount);
                            document.getElementById('total').textContent = formatCurrency(total);

                            // Enable checkout button if we have items
                            document.getElementById('checkoutBtn').disabled = cartItems.length === 0;

                            // Show/hide Clear Cart button based on cart state
                            document.getElementById('clearCartBtn').style.display = cartItems.length > 0 ? 'flex' : 'none';

                            // Update change calculation
                            calculateChange();
                        }

                        function calculateChange() {
                            const subtotal = cartItems.reduce((sum, item) => sum + item.lineTotal, 0);
                            const total = Math.max(0, subtotal - cartDiscount);
                            const tendered = parseFloat(document.getElementById('tenderedAmount').value) || 0;
                            const change = tendered - total;
                            document.getElementById('changeAmount').textContent = formatCurrency(Math.max(0, change));
                        }

                        function applyDiscount() {
                            const amount = prompt('Enter discount amount (Rs.):');
                            if (amount === null) return;

                            const discountValue = parseFloat(amount);
                            if (isNaN(discountValue) || discountValue < 0) {
                                showNotification('Invalid discount amount', 'error');
                                return;
                            }

                            const subtotal = cartItems.reduce((sum, item) => sum + item.lineTotal, 0);
                            if (discountValue > subtotal) {
                                showNotification('Discount cannot exceed subtotal of Rs. ' + subtotal.toFixed(2), 'error');
                                return;
                            }

                            cartDiscount = discountValue;
                            updateCartDisplay();
                            showNotification('Discount applied: Rs. ' + discountValue.toFixed(2), 'success');
                        }

                        async function processCheckout() {
                            if (cartItems.length === 0) {
                                showNotification('Cart is empty', 'error');
                                return;
                            }

                            const storeType = document.getElementById('storeType').value;
                            const transactionType = document.getElementById('transactionType').value;
                            const cashierId = document.getElementById('cashierId').value;
                            const tenderedAmount = parseFloat(document.getElementById('tenderedAmount').value) || 0;

                            // Validate cash payment
                            const subtotal = cartItems.reduce((sum, item) => sum + item.lineTotal, 0);
                            const total = Math.max(0, subtotal - cartDiscount);

                            if (transactionType === 'CASH' && tenderedAmount < total) {
                                showNotification('Insufficient cash. Required: Rs. ' + total.toFixed(2) + ', Tendered: Rs. ' + tenderedAmount.toFixed(2), 'error');
                                return;
                            }

                            // Build checkout request
                            const checkoutRequest = {
                                storeType: storeType,
                                transactionType: transactionType,
                                customerId: null, // Physical store sales don't require customer ID
                                cashierId: cashierId || null,
                                items: cartItems.map(item => ({
                                    productCode: item.productCode,
                                    quantity: item.quantity
                                })),
                                discount: cartDiscount > 0 ? cartDiscount : null,
                                cashTendered: transactionType === 'CASH' ? tenderedAmount : null
                            };

                            try {
                                const response = await apiFetch('/billing/checkout', {
                                    method: 'POST',
                                    body: JSON.stringify(checkoutRequest)
                                });

                                if (response.success) {
                                    showNotification('Checkout successful! Bill: ' + response.data.serialNumber, 'success');

                                    // Redirect to receipt after short delay
                                    setTimeout(() => {
                                        window.location.href = ctx + '/pos/receipt/' + response.data.billId;
                                    }, 1000);
                                } else {
                                    showNotification(response.error || 'Checkout failed', 'error');
                                }
                            } catch (error) {
                                showNotification(error.message || 'Checkout failed', 'error');
                            }
                        }

                        function clearCart() {
                            if (cartItems.length === 0) return;

                            if (!confirm('Are you sure you want to clear the cart?')) return;

                            cartItems = [];
                            cartDiscount = 0;
                            document.getElementById('tenderedAmount').value = '';
                            updateCartDisplay();
                            showNotification('Cart cleared', 'success');
                        }

                        function cancelTransaction() {
                            if (cartItems.length > 0) {
                                // Scenario 1: Cart has items - ask for confirmation
                                if (!confirm('You have items in your cart. Are you sure you want to cancel this transaction?')) {
                                    return;
                                }
                            }
                            // Both scenarios: Navigate back to POS home
                            window.location.href = ctx + '/pos';
                        }
                    </script>

                </t:layout>