<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout pageTitle="${isEdit ? 'Edit Product' : 'Add Product'}" activeNav="products">

    <!-- Page Header -->
    <div class="mb-6">
        <nav class="flex mb-4" aria-label="Breadcrumb">
            <ol class="inline-flex items-center space-x-1 md:space-x-3">
                <li><a href="${pageContext.request.contextPath}/products" class="text-gray-500 hover:text-gray-700">Products</a></li>
                <li class="flex items-center">
                    <svg class="w-4 h-4 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"/>
                    </svg>
                    <span class="ml-1 text-gray-700 font-medium">${isEdit ? 'Edit' : 'Add New'}</span>
                </li>
            </ol>
        </nav>
        <h1 class="text-2xl font-bold text-gray-900">${isEdit ? 'Edit Product' : 'Add New Product'}</h1>
    </div>

    <!-- Form -->
    <div class="card max-w-2xl">
        <form id="productForm" class="space-y-6">
            <c:if test="${isEdit}">
                <input type="hidden" name="existingCode" value="${product.productCodeString}">
            </c:if>

            <!-- Product Code -->
            <div>
                <label for="productCode" class="input-label">Product Code *</label>
                <input type="text" id="productCode" name="productCode"
                       value="${isEdit ? product.productCodeString : ''}"
                       class="input-field font-mono" required
                       pattern="[A-Za-z0-9-]+" maxlength="20"
                       ${isEdit ? 'readonly' : ''}
                       placeholder="e.g., PRD-001">
                <p class="mt-1 text-xs text-gray-500">Alphanumeric characters and hyphens only</p>
            </div>

            <!-- Product Name -->
            <div>
                <label for="productName" class="input-label">Product Name *</label>
                <input type="text" id="productName" name="productName"
                       value="${isEdit ? product.productName : ''}"
                       class="input-field" required maxlength="100"
                       placeholder="Enter product name">
            </div>

            <!-- Unit Price -->
            <div>
                <label for="unitPrice" class="input-label">Unit Price (Rs.) *</label>
                <input type="number" id="unitPrice" name="unitPrice"
                       value="${isEdit ? product.unitPrice.amount : ''}"
                       class="input-field" required
                       min="0" step="0.01"
                       placeholder="0.00">
            </div>

            <!-- Description -->
            <div>
                <label for="description" class="input-label">Description</label>
                <textarea id="description" name="description" rows="3"
                          class="input-field" maxlength="500"
                          placeholder="Enter product description">${isEdit ? product.description : ''}</textarea>
            </div>

            <!-- Unit of Measure -->
            <div>
                <label for="unitOfMeasure" class="input-label">Unit of Measure</label>
                <select id="unitOfMeasure" name="unitOfMeasure" class="input-field">
                    <option value="">Select unit</option>
                    <option value="PIECE" ${isEdit && product.unitOfMeasure.name() == 'PIECE' ? 'selected' : ''}>Piece</option>
                    <option value="KG" ${isEdit && product.unitOfMeasure.name() == 'KG' ? 'selected' : ''}>Kilogram (KG)</option>
                    <option value="GRAM" ${isEdit && product.unitOfMeasure.name() == 'GRAM' ? 'selected' : ''}>Gram</option>
                    <option value="LITER" ${isEdit && product.unitOfMeasure.name() == 'LITER' ? 'selected' : ''}>Liter</option>
                    <option value="ML" ${isEdit && product.unitOfMeasure.name() == 'ML' ? 'selected' : ''}>Milliliter (ML)</option>
                    <option value="PACK" ${isEdit && product.unitOfMeasure.name() == 'PACK' ? 'selected' : ''}>Pack</option>
                    <option value="BOX" ${isEdit && product.unitOfMeasure.name() == 'BOX' ? 'selected' : ''}>Box</option>
                    <option value="DOZEN" ${isEdit && product.unitOfMeasure.name() == 'DOZEN' ? 'selected' : ''}>Dozen</option>
                </select>
            </div>

            <!-- Category & Brand (Optional) -->
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                    <label for="categoryId" class="input-label">Category ID</label>
                    <input type="number" id="categoryId" name="categoryId"
                           value="${isEdit ? product.categoryId : ''}"
                           class="input-field" min="1"
                           placeholder="Optional">
                </div>
                <div>
                    <label for="brandId" class="input-label">Brand ID</label>
                    <input type="number" id="brandId" name="brandId"
                           value="${isEdit ? product.brandId : ''}"
                           class="input-field" min="1"
                           placeholder="Optional">
                </div>
            </div>

            <!-- Active Status (Edit only) -->
            <c:if test="${isEdit}">
                <div class="flex items-center">
                    <input type="checkbox" id="active" name="active"
                           ${product.active ? 'checked' : ''}
                           class="h-4 w-4 text-syos-primary focus:ring-syos-primary border-gray-300 rounded">
                    <label for="active" class="ml-2 block text-sm text-gray-700">
                        Active (available for sale)
                    </label>
                </div>
            </c:if>

            <!-- Form Actions -->
            <div class="flex items-center justify-end space-x-3 pt-4 border-t">
                <a href="${pageContext.request.contextPath}/products" class="btn-secondary">Cancel</a>
                <button type="submit" class="btn-primary" id="submitBtn">
                    <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                    </svg>
                    ${isEdit ? 'Update Product' : 'Create Product'}
                </button>
            </div>
        </form>
    </div>

    <script>
        document.getElementById('productForm').addEventListener('submit', async function(e) {
            e.preventDefault();

            const submitBtn = document.getElementById('submitBtn');
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<svg class="animate-spin h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>Saving...';

            const formData = new FormData(this);
            const isEdit = formData.get('existingCode') !== null;

            const data = {
                productCode: formData.get('productCode'),
                productName: formData.get('productName'),
                unitPrice: parseFloat(formData.get('unitPrice')),
                description: formData.get('description') || null,
                unitOfMeasure: formData.get('unitOfMeasure') || null,
                categoryId: formData.get('categoryId') ? parseInt(formData.get('categoryId')) : null,
                brandId: formData.get('brandId') ? parseInt(formData.get('brandId')) : null
            };

            try {
                if (isEdit) {
                    await apiFetch(`/products/${formData.get('existingCode')}`, {
                        method: 'PUT',
                        body: JSON.stringify(data)
                    });
                    showNotification('Product updated successfully', 'success');
                } else {
                    await apiFetch('/products', {
                        method: 'POST',
                        body: JSON.stringify(data)
                    });
                    showNotification('Product created successfully', 'success');
                }
                setTimeout(() => {
                    window.location.href = ctx + '/products';
                }, 1000);
            } catch (error) {
                showNotification(error.message, 'error');
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>${isEdit ? 'Update Product' : 'Create Product'}';
            }
        });
    </script>

</t:layout>
