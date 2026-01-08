<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout pageTitle="Add Inventory Batch" activeNav="inventory">

    <!-- Page Header -->
    <div class="mb-6">
        <nav class="flex mb-4" aria-label="Breadcrumb">
            <ol class="inline-flex items-center space-x-1 md:space-x-3">
                <li><a href="${pageContext.request.contextPath}/inventory" class="text-gray-500 hover:text-gray-700">Inventory</a></li>
                <li class="flex items-center">
                    <svg class="w-4 h-4 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"/>
                    </svg>
                    <span class="ml-1 text-gray-700 font-medium">Add Batch</span>
                </li>
            </ol>
        </nav>
        <h1 class="text-2xl font-bold text-gray-900">Add New Inventory Batch</h1>
        <p class="mt-1 text-sm text-gray-500">Receive new stock into main inventory</p>
    </div>

    <!-- Form -->
    <div class="card max-w-2xl">
        <form id="batchForm" class="space-y-6">
            <!-- Product Code -->
            <div>
                <label for="productCode" class="input-label">Product Code *</label>
                <input type="text" id="productCode" name="productCode"
                       class="input-field font-mono" required
                       placeholder="Enter product code">
                <p class="mt-1 text-xs text-gray-500">Enter the product code for this batch</p>
            </div>

            <!-- Quantity -->
            <div>
                <label for="quantity" class="input-label">Quantity Received *</label>
                <input type="number" id="quantity" name="quantity"
                       class="input-field" required min="1"
                       placeholder="Enter quantity">
            </div>

            <!-- Purchase Price -->
            <div>
                <label for="purchasePrice" class="input-label">Purchase Price (Rs.)</label>
                <input type="number" id="purchasePrice" name="purchasePrice"
                       class="input-field" min="0" step="0.01"
                       placeholder="Cost per unit">
            </div>

            <!-- Dates -->
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                    <label for="purchaseDate" class="input-label">Purchase Date</label>
                    <input type="date" id="purchaseDate" name="purchaseDate"
                           class="input-field">
                </div>
                <div>
                    <label for="expiryDate" class="input-label">Expiry Date</label>
                    <input type="date" id="expiryDate" name="expiryDate"
                           class="input-field">
                </div>
            </div>

            <!-- Supplier -->
            <div>
                <label for="supplierName" class="input-label">Supplier Name</label>
                <input type="text" id="supplierName" name="supplierName"
                       class="input-field"
                       placeholder="Enter supplier name">
            </div>

            <!-- Form Actions -->
            <div class="flex items-center justify-end space-x-3 pt-4 border-t">
                <a href="${pageContext.request.contextPath}/inventory" class="btn-secondary">Cancel</a>
                <button type="submit" class="btn-success" id="submitBtn">
                    <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                    </svg>
                    Add Batch
                </button>
            </div>
        </form>
    </div>

    <script>
        // Set default purchase date to today
        document.getElementById('purchaseDate').valueAsDate = new Date();

        document.getElementById('batchForm').addEventListener('submit', async function(e) {
            e.preventDefault();

            const submitBtn = document.getElementById('submitBtn');
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<svg class="animate-spin h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>Adding...';

            const formData = new FormData(this);
            const data = {
                productCode: formData.get('productCode'),
                quantity: parseInt(formData.get('quantity')),
                purchasePrice: formData.get('purchasePrice') ? parseFloat(formData.get('purchasePrice')) : null,
                purchaseDate: formData.get('purchaseDate') || null,
                expiryDate: formData.get('expiryDate') || null,
                supplierName: formData.get('supplierName') || null
            };

            try {
                await apiFetch('/inventory', {
                    method: 'POST',
                    body: JSON.stringify(data)
                });
                showNotification('Batch added successfully', 'success');
                setTimeout(() => {
                    window.location.href = ctx + '/inventory';
                }, 1000);
            } catch (error) {
                showNotification(error.message, 'error');
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>Add Batch';
            }
        });
    </script>

</t:layout>
