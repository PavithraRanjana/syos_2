<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout pageTitle="Restock Store" activeNav="store-stock">

    <div class="mb-6">
        <nav class="flex mb-4" aria-label="Breadcrumb">
            <ol class="inline-flex items-center space-x-1 md:space-x-3">
                <li><a href="${pageContext.request.contextPath}/store-stock" class="text-gray-500 hover:text-gray-700">Store Stock</a></li>
                <li class="flex items-center">
                    <svg class="w-4 h-4 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"/>
                    </svg>
                    <span class="ml-1 text-gray-700 font-medium">Restock</span>
                </li>
            </ol>
        </nav>
        <h1 class="text-2xl font-bold text-gray-900">Restock Store Shelves</h1>
        <p class="mt-1 text-sm text-gray-500">Transfer inventory from main warehouse to store</p>
    </div>

    <div class="card max-w-2xl">
        <form id="restockForm" class="space-y-6">
            <div>
                <label class="input-label">Store Type *</label>
                <select id="storeType" name="storeType" class="input-field" required>
                    <option value="physical">Physical Store</option>
                    <option value="online">Online Store</option>
                </select>
            </div>

            <div>
                <label for="productCode" class="input-label">Product Code *</label>
                <input type="text" id="productCode" name="productCode" class="input-field font-mono" required
                       placeholder="Enter product code">
            </div>

            <div>
                <label for="quantity" class="input-label">Quantity to Restock *</label>
                <input type="number" id="quantity" name="quantity" class="input-field" required min="1"
                       placeholder="Enter quantity">
            </div>

            <div>
                <label for="batchId" class="input-label">Specific Batch ID (Optional)</label>
                <input type="number" id="batchId" name="batchId" class="input-field"
                       placeholder="Leave empty for FIFO allocation">
                <p class="mt-1 text-xs text-gray-500">If empty, stock will be allocated from oldest batches first (FIFO)</p>
            </div>

            <div class="flex items-center justify-end space-x-3 pt-4 border-t">
                <a href="${pageContext.request.contextPath}/store-stock" class="btn-secondary">Cancel</a>
                <button type="submit" class="btn-success" id="submitBtn">
                    <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
                    </svg>
                    Restock
                </button>
            </div>
        </form>
    </div>

    <script>
        document.getElementById('restockForm').addEventListener('submit', async function(e) {
            e.preventDefault();

            const submitBtn = document.getElementById('submitBtn');
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<svg class="animate-spin h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>Processing...';

            const formData = new FormData(this);
            const storeType = formData.get('storeType');
            const data = {
                productCode: formData.get('productCode'),
                quantity: parseInt(formData.get('quantity')),
                batchId: formData.get('batchId') ? parseInt(formData.get('batchId')) : null
            };

            try {
                await apiFetch(`/store-inventory/${storeType}/restock`, {
                    method: 'POST',
                    body: JSON.stringify(data)
                });
                showNotification('Restock successful!', 'success');
                setTimeout(() => {
                    window.location.href = ctx + '/store-stock';
                }, 1000);
            } catch (error) {
                showNotification(error.message, 'error');
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/></svg>Restock';
            }
        });
    </script>

</t:layout>
