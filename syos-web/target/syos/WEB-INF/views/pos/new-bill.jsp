<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout pageTitle="New Sale" activeNav="pos">

    <!-- Page Header -->
    <div class="mb-6">
        <nav class="flex mb-4" aria-label="Breadcrumb">
            <ol class="inline-flex items-center space-x-1 md:space-x-3">
                <li><a href="${pageContext.request.contextPath}/pos" class="text-gray-500 hover:text-gray-700">POS</a></li>
                <li class="flex items-center">
                    <svg class="w-4 h-4 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"/>
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
            <!-- Bill Setup -->
            <div class="card" id="setupCard">
                <h2 class="card-header">Bill Setup</h2>
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div>
                        <label class="input-label">Store Type *</label>
                        <select id="storeType" class="input-field" required>
                            <option value="PHYSICAL">Physical Store</option>
                            <option value="ONLINE">Online Store</option>
                        </select>
                    </div>
                    <div>
                        <label class="input-label">Transaction Type *</label>
                        <select id="transactionType" class="input-field" required>
                            <option value="CASH">Cash</option>
                            <option value="CREDIT">Credit/Card</option>
                        </select>
                    </div>
                    <div id="customerIdGroup" class="hidden">
                        <label class="input-label">Customer ID *</label>
                        <input type="number" id="customerId" class="input-field" placeholder="Enter customer ID">
                    </div>
                    <div>
                        <label class="input-label">Cashier ID</label>
                        <input type="text" id="cashierId" class="input-field" placeholder="Optional">
                    </div>
                </div>
                <div class="mt-4">
                    <button onclick="createBill()" class="btn-primary" id="createBillBtn">
                        <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"/>
                        </svg>
                        Start Bill
                    </button>
                </div>
            </div>

            <!-- Add Items (shown after bill created) -->
            <div class="card hidden" id="addItemsCard">
                <h2 class="card-header">Add Items</h2>
                <div class="flex gap-4">
                    <div class="flex-grow">
                        <input type="text" id="productSearch" class="input-field"
                               placeholder="Search product by code or name..."
                               onkeyup="searchProducts(this.value)">
                        <div id="searchResults" class="absolute z-10 w-full mt-1 bg-white border rounded-lg shadow-lg hidden max-h-60 overflow-y-auto"></div>
                    </div>
                    <input type="number" id="itemQuantity" class="input-field w-24" value="1" min="1" placeholder="Qty">
                    <button onclick="addItem()" class="btn-success">Add</button>
                </div>

                <!-- Quick Add Products -->
                <c:if test="${not empty products}">
                    <div class="mt-4">
                        <p class="text-sm text-gray-500 mb-2">Quick Add:</p>
                        <div class="flex flex-wrap gap-2">
                            <c:forEach var="product" items="${products}" begin="0" end="9">
                                <button onclick="quickAddProduct('${product.productCodeString}', '${product.productName}')"
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
                <span id="billSerialNumber">Bill Summary</span>
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
                    <span id="discount">Rs. 0.00</span>
                </div>
                <div class="flex justify-between text-sm">
                    <span class="text-gray-500">Tax</span>
                    <span id="tax">Rs. 0.00</span>
                </div>
                <div class="flex justify-between text-lg font-bold border-t pt-2">
                    <span>Total</span>
                    <span id="total">Rs. 0.00</span>
                </div>
            </div>

            <!-- Payment Section -->
            <div id="paymentSection" class="mt-4 pt-4 border-t hidden">
                <div class="space-y-4">
                    <div id="cashPaymentDiv">
                        <label class="input-label">Cash Tendered (Rs.)</label>
                        <input type="number" id="tenderedAmount" class="input-field" min="0" step="0.01"
                               placeholder="Enter amount" onchange="calculateChange()">
                        <p class="mt-1 text-sm">Change: <span id="changeAmount" class="font-bold">Rs. 0.00</span></p>
                    </div>

                    <div class="flex gap-2">
                        <button onclick="applyDiscount()" class="btn-secondary flex-1">
                            Apply Discount
                        </button>
                    </div>

                    <button onclick="processPayment()" class="btn-success w-full" id="payBtn" disabled>
                        <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z"/>
                        </svg>
                        Process Payment
                    </button>

                    <button onclick="finalizeBill()" class="btn-primary w-full hidden" id="finalizeBtn">
                        <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                        </svg>
                        Finalize Bill
                    </button>
                </div>
            </div>

            <div class="mt-4">
                <button onclick="cancelBill()" class="btn-danger w-full hidden" id="cancelBtn">
                    Cancel Bill
                </button>
            </div>
        </div>
    </div>

    <script>
        let currentBillId = null;
        let billItems = [];
        let selectedProductCode = null;

        // Show customer ID for online orders
        document.getElementById('storeType').addEventListener('change', function() {
            const customerGroup = document.getElementById('customerIdGroup');
            customerGroup.classList.toggle('hidden', this.value !== 'ONLINE');
        });

        async function createBill() {
            const storeType = document.getElementById('storeType').value;
            const transactionType = document.getElementById('transactionType').value;
            const customerId = document.getElementById('customerId').value;
            const cashierId = document.getElementById('cashierId').value;

            if (storeType === 'ONLINE' && !customerId) {
                showNotification('Customer ID is required for online orders', 'error');
                return;
            }

            try {
                const response = await apiFetch('/billing', {
                    method: 'POST',
                    body: JSON.stringify({
                        storeType,
                        transactionType,
                        customerId: customerId ? parseInt(customerId) : null,
                        cashierId: cashierId || null
                    })
                });

                currentBillId = response.data.billId;
                document.getElementById('billSerialNumber').textContent = 'Bill: ' + response.data.serialNumber;

                // Update UI
                document.getElementById('setupCard').classList.add('hidden');
                document.getElementById('addItemsCard').classList.remove('hidden');
                document.getElementById('paymentSection').classList.remove('hidden');
                document.getElementById('cancelBtn').classList.remove('hidden');

                // Update payment section based on transaction type
                const cashDiv = document.getElementById('cashPaymentDiv');
                cashDiv.classList.toggle('hidden', transactionType !== 'CASH');

                showNotification('Bill created successfully', 'success');
            } catch (error) {
                showNotification(error.message, 'error');
            }
        }

        function quickAddProduct(code, name) {
            selectedProductCode = code;
            document.getElementById('productSearch').value = name;
            addItem();
        }

        async function addItem() {
            if (!currentBillId) {
                showNotification('Please create a bill first', 'error');
                return;
            }

            const productCode = selectedProductCode || document.getElementById('productSearch').value;
            const quantity = parseInt(document.getElementById('itemQuantity').value) || 1;

            if (!productCode) {
                showNotification('Please enter a product code', 'error');
                return;
            }

            try {
                const response = await apiFetch(`/billing/${currentBillId}/items`, {
                    method: 'POST',
                    body: JSON.stringify({ productCode, quantity })
                });

                updateBillDisplay(response.data.bill);
                document.getElementById('productSearch').value = '';
                document.getElementById('itemQuantity').value = '1';
                selectedProductCode = null;

                showNotification('Item added', 'success');
            } catch (error) {
                showNotification(error.message, 'error');
            }
        }

        function updateBillDisplay(bill) {
            // Update items list
            const itemsList = document.getElementById('itemsList');
            if (bill.items && bill.items.length > 0) {
                itemsList.innerHTML = bill.items.map(item => `
                    <div class="flex justify-between items-center p-2 bg-gray-50 rounded">
                        <div class="flex-grow">
                            <div class="font-medium text-sm">\${item.productName}</div>
                            <div class="text-xs text-gray-500">\${item.quantity} x Rs. \${parseFloat(item.unitPrice).toFixed(2)}</div>
                        </div>
                        <div class="text-right">
                            <div class="font-medium">Rs. \${parseFloat(item.lineTotal).toFixed(2)}</div>
                            <button onclick="removeItem(\${item.billItemId})" class="text-red-500 text-xs hover:underline">Remove</button>
                        </div>
                    </div>
                `).join('');
            } else {
                itemsList.innerHTML = '<p class="text-gray-500 text-center py-4">No items added yet</p>';
            }

            // Update totals
            document.getElementById('subtotal').textContent = formatCurrency(bill.subtotal);
            document.getElementById('discount').textContent = formatCurrency(bill.discount);
            document.getElementById('tax').textContent = formatCurrency(bill.tax);
            document.getElementById('total').textContent = formatCurrency(bill.total);

            // Enable payment if items exist
            document.getElementById('payBtn').disabled = !bill.items || bill.items.length === 0;
        }

        async function removeItem(itemId) {
            try {
                const response = await apiFetch(`/billing/${currentBillId}/items/${itemId}`, {
                    method: 'DELETE'
                });
                updateBillDisplay(response.data);
                showNotification('Item removed', 'success');
            } catch (error) {
                showNotification(error.message, 'error');
            }
        }

        function calculateChange() {
            const total = parseFloat(document.getElementById('total').textContent.replace(/[^0-9.-]+/g, '')) || 0;
            const tendered = parseFloat(document.getElementById('tenderedAmount').value) || 0;
            const change = tendered - total;
            document.getElementById('changeAmount').textContent = formatCurrency(Math.max(0, change));
        }

        async function applyDiscount() {
            const amount = prompt('Enter discount amount (Rs.):');
            if (amount === null) return;

            try {
                const response = await apiFetch(`/billing/${currentBillId}/discount`, {
                    method: 'POST',
                    body: JSON.stringify({ amount: parseFloat(amount) })
                });
                updateBillDisplay(response.data);
                showNotification('Discount applied', 'success');
            } catch (error) {
                showNotification(error.message, 'error');
            }
        }

        async function processPayment() {
            const transactionType = document.getElementById('transactionType').value;

            try {
                if (transactionType === 'CASH') {
                    const tendered = parseFloat(document.getElementById('tenderedAmount').value);
                    if (!tendered || tendered <= 0) {
                        showNotification('Please enter tendered amount', 'error');
                        return;
                    }
                    await apiFetch(`/billing/${currentBillId}/payment/cash`, {
                        method: 'POST',
                        body: JSON.stringify({ tenderedAmount: tendered })
                    });
                } else {
                    await apiFetch(`/billing/${currentBillId}/payment/online`, {
                        method: 'POST'
                    });
                }

                document.getElementById('payBtn').classList.add('hidden');
                document.getElementById('finalizeBtn').classList.remove('hidden');
                showNotification('Payment processed', 'success');
            } catch (error) {
                showNotification(error.message, 'error');
            }
        }

        async function finalizeBill() {
            try {
                await apiFetch(`/billing/${currentBillId}/finalize`, { method: 'POST' });
                showNotification('Bill finalized successfully!', 'success');
                setTimeout(() => {
                    window.location.href = ctx + '/pos/receipt/' + currentBillId;
                }, 1000);
            } catch (error) {
                showNotification(error.message, 'error');
            }
        }

        async function cancelBill() {
            if (!confirm('Are you sure you want to cancel this bill?')) return;

            try {
                await apiFetch(`/billing/${currentBillId}/cancel`, { method: 'POST' });
                showNotification('Bill cancelled', 'success');
                setTimeout(() => {
                    window.location.href = ctx + '/pos';
                }, 1000);
            } catch (error) {
                showNotification(error.message, 'error');
            }
        }

        // Set store type from URL parameter
        const urlParams = new URLSearchParams(window.location.search);
        const storeTypeParam = urlParams.get('storeType');
        if (storeTypeParam) {
            document.getElementById('storeType').value = storeTypeParam;
            document.getElementById('storeType').dispatchEvent(new Event('change'));
        }
    </script>

</t:layout>
