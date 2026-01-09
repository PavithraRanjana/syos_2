<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout pageTitle="Reshelve Report" activeNav="reports">

    <!-- Page Header -->
    <div class="mb-6">
        <div class="flex items-center justify-between">
            <div>
                <h1 class="text-2xl font-bold text-gray-900">End of Day Reshelve Report</h1>
                <p class="mt-1 text-sm text-gray-500">Items below minimum stock that need to be reshelved</p>
            </div>
            <a href="${pageContext.request.contextPath}/reports" class="btn btn-secondary">
                Back to Reports
            </a>
        </div>
    </div>

    <!-- Filters -->
    <div class="card mb-6">
        <form method="get" class="flex items-center gap-4">
            <div class="flex items-center gap-2">
                <label for="storeType" class="text-sm font-medium text-gray-700">Store Type:</label>
                <select id="storeType" name="storeType" class="input-field w-auto" onchange="this.form.submit()">
                    <option value="PHYSICAL" ${storeType == 'PHYSICAL' ? 'selected' : ''}>Physical Store</option>
                    <option value="ONLINE" ${storeType == 'ONLINE' ? 'selected' : ''}>Online Store</option>
                </select>
            </div>
        </form>
    </div>

    <!-- Summary Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
        <div class="stat-card border-syos-warning">
            <p class="text-sm font-medium text-gray-500">Products to Reshelve</p>
            <p class="text-3xl font-bold text-gray-900">${totalItems}</p>
            <p class="text-xs text-gray-500 mt-1">items below minimum stock</p>
        </div>
        <div class="stat-card border-syos-primary">
            <p class="text-sm font-medium text-gray-500">Total Quantity Needed</p>
            <p class="text-3xl font-bold text-gray-900">${totalQuantityToReshelve}</p>
            <p class="text-xs text-gray-500 mt-1">units to reach minimum levels</p>
        </div>
    </div>

    <!-- Reshelve Items Table -->
    <div class="card">
        <h2 class="card-header">Items to Reshelve - ${storeType == 'PHYSICAL' ? 'Physical Store' : 'Online Store'}</h2>

        <c:choose>
            <c:when test="${not empty reshelveItems}">
                <div class="table-container">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th class="table-header">#</th>
                                <th class="table-header">Product Code</th>
                                <th class="table-header">Product Name</th>
                                <th class="table-header text-right">Current Stock</th>
                                <th class="table-header text-right">Minimum Stock</th>
                                <th class="table-header text-right">Quantity to Reshelve</th>
                                <th class="table-header text-center">Status</th>
                            </tr>
                        </thead>
                        <tbody class="bg-white divide-y divide-gray-200">
                            <c:forEach var="item" items="${reshelveItems}" varStatus="status">
                                <tr class="table-row">
                                    <td class="table-cell text-gray-500">${status.index + 1}</td>
                                    <td class="table-cell font-mono text-sm">${item.productCode()}</td>
                                    <td class="table-cell font-medium">${item.productName()}</td>
                                    <td class="table-cell text-right">
                                        <span class="${item.currentStock() == 0 ? 'text-red-600 font-bold' : 'text-yellow-600'}">
                                            ${item.currentStock()}
                                        </span>
                                    </td>
                                    <td class="table-cell text-right">${item.minimumStock()}</td>
                                    <td class="table-cell text-right font-bold text-syos-primary">${item.quantityToReshelve()}</td>
                                    <td class="table-cell text-center">
                                        <c:choose>
                                            <c:when test="${item.currentStock() == 0}">
                                                <span class="px-2 py-1 text-xs font-semibold rounded-full bg-red-100 text-red-800">
                                                    Out of Stock
                                                </span>
                                            </c:when>
                                            <c:when test="${item.currentStock() < item.minimumStock() / 2}">
                                                <span class="px-2 py-1 text-xs font-semibold rounded-full bg-orange-100 text-orange-800">
                                                    Critical
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="px-2 py-1 text-xs font-semibold rounded-full bg-yellow-100 text-yellow-800">
                                                    Low Stock
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                        <tfoot class="bg-gray-50">
                            <tr>
                                <td colspan="5" class="table-cell text-right font-bold">Total:</td>
                                <td class="table-cell text-right font-bold text-syos-primary">${totalQuantityToReshelve}</td>
                                <td class="table-cell"></td>
                            </tr>
                        </tfoot>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <div class="text-center py-12">
                    <svg class="mx-auto h-12 w-12 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                    </svg>
                    <h3 class="mt-2 text-lg font-medium text-gray-900">All Products Stocked</h3>
                    <p class="mt-1 text-sm text-gray-500">All products are above their minimum stock levels.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Print Button -->
    <div class="mt-6 flex justify-end">
        <button onclick="window.print()" class="btn btn-secondary">
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 17h2a2 2 0 002-2v-4a2 2 0 00-2-2H5a2 2 0 00-2 2v4a2 2 0 002 2h2m2 4h6a2 2 0 002-2v-4a2 2 0 00-2-2H9a2 2 0 00-2 2v4a2 2 0 002 2zm8-12V5a2 2 0 00-2-2H9a2 2 0 00-2 2v4h10z"/>
            </svg>
            Print Report
        </button>
    </div>

</t:layout>
