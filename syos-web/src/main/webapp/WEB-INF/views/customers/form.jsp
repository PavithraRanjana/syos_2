<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout pageTitle="${isEdit ? 'Edit Customer' : 'Add Customer'}" activeNav="customers">

    <div class="mb-6">
        <nav class="flex mb-4" aria-label="Breadcrumb">
            <ol class="inline-flex items-center space-x-1 md:space-x-3">
                <li><a href="${pageContext.request.contextPath}/customers" class="text-gray-500 hover:text-gray-700">Customers</a></li>
                <li class="flex items-center">
                    <svg class="w-4 h-4 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"/>
                    </svg>
                    <span class="ml-1 text-gray-700 font-medium">${isEdit ? 'Edit' : 'Add New'}</span>
                </li>
            </ol>
        </nav>
        <h1 class="text-2xl font-bold text-gray-900">${isEdit ? 'Edit Customer' : 'Add New Customer'}</h1>
    </div>

    <div class="card max-w-2xl">
        <form id="customerForm" class="space-y-6">
            <c:if test="${isEdit}">
                <input type="hidden" name="customerId" value="${customer.customerId}">
            </c:if>

            <div>
                <label for="name" class="input-label">Full Name *</label>
                <input type="text" id="name" name="name" value="${isEdit ? customer.customerName : ''}"
                       class="input-field" required maxlength="100" placeholder="Enter full name">
            </div>

            <div>
                <label for="email" class="input-label">Email Address *</label>
                <input type="email" id="email" name="email" value="${isEdit ? customer.email : ''}"
                       class="input-field" required ${isEdit ? 'readonly' : ''}
                       placeholder="email@example.com">
                <c:if test="${!isEdit}">
                    <p class="mt-1 text-xs text-gray-500">Email cannot be changed after registration</p>
                </c:if>
            </div>

            <div>
                <label for="phone" class="input-label">Phone Number</label>
                <input type="tel" id="phone" name="phone" value="${isEdit ? customer.phone : ''}"
                       class="input-field" placeholder="+94 XX XXX XXXX">
            </div>

            <div>
                <label for="address" class="input-label">Address</label>
                <textarea id="address" name="address" rows="3" class="input-field"
                          placeholder="Enter address">${isEdit ? customer.address : ''}</textarea>
            </div>

            <c:if test="${!isEdit}">
                <div>
                    <label for="password" class="input-label">Password *</label>
                    <input type="password" id="password" name="password"
                           class="input-field" required minlength="6"
                           placeholder="Minimum 6 characters">
                </div>
            </c:if>

            <div class="flex items-center justify-end space-x-3 pt-4 border-t">
                <a href="${pageContext.request.contextPath}/customers" class="btn-secondary">Cancel</a>
                <button type="submit" class="btn-primary" id="submitBtn">
                    <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                    </svg>
                    ${isEdit ? 'Update' : 'Register'} Customer
                </button>
            </div>
        </form>
    </div>

    <script>
        document.getElementById('customerForm').addEventListener('submit', async function(e) {
            e.preventDefault();

            const submitBtn = document.getElementById('submitBtn');
            submitBtn.disabled = true;

            const formData = new FormData(this);
            const isEdit = formData.get('customerId') !== null;
            const customerId = formData.get('customerId');

            try {
                if (isEdit) {
                    await apiFetch(`/customers/${customerId}`, {
                        method: 'PUT',
                        body: JSON.stringify({
                            name: formData.get('name'),
                            phone: formData.get('phone'),
                            address: formData.get('address')
                        })
                    });
                    showNotification('Customer updated successfully', 'success');
                } else {
                    await apiFetch('/customers/register', {
                        method: 'POST',
                        body: JSON.stringify({
                            name: formData.get('name'),
                            email: formData.get('email'),
                            phone: formData.get('phone'),
                            address: formData.get('address'),
                            password: formData.get('password')
                        })
                    });
                    showNotification('Customer registered successfully', 'success');
                }
                setTimeout(() => {
                    window.location.href = ctx + '/customers';
                }, 1000);
            } catch (error) {
                showNotification(error.message, 'error');
                submitBtn.disabled = false;
            }
        });
    </script>

</t:layout>
