<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout pageTitle="Batch-wise Stock Report" activeNav="reports">

    <!-- Page Header -->
    <div class="mb-6">
        <div class="flex items-center justify-between">
            <div>
                <h1 class="text-2xl font-bold text-gray-900">Batch-wise Stock Report</h1>
                <p class="mt-1 text-sm text-gray-500">Current stock details by batch from main inventory</p>
            </div>
            <a href="${pageContext.request.contextPath}/reports" class="btn btn-secondary">
                Back to Reports
            </a>
        </div>
    </div>

    <!-- Summary Cards -->
    <div class="grid grid-cols-2 md:grid-cols-5 gap-4 mb-6">
        <div class="stat-card border-syos-primary">
            <p class="text-xs font-medium text-gray-500">Total Batches</p>
            <p class="text-2xl font-bold text-gray-900">${totalBatches}</p>
        </div>
        <div class="stat-card border-blue-400">
            <p class="text-xs font-medium text-gray-500">Original Qty</p>
            <p class="text-2xl font-bold text-gray-900">${totalOriginal}</p>
        </div>
        <div class="stat-card border-syos-secondary">
            <p class="text-xs font-medium text-gray-500">In Main Inventory</p>
            <p class="text-2xl font-bold text-gray-900">${totalRemaining}</p>
        </div>
        <div class="stat-card border-syos-success">
            <p class="text-xs font-medium text-gray-500">In Physical Store</p>
            <p class="text-2xl font-bold text-gray-900">${totalPhysical}</p>
        </div>
        <div class="stat-card border-syos-warning">
            <p class="text-xs font-medium text-gray-500">In Online Store</p>
            <p class="text-2xl font-bold text-gray-900">${totalOnline}</p>
        </div>
    </div>

    <!-- Batch Stock Table -->
    <div class="card">
        <h2 class="card-header">All Batches</h2>

        <c:choose>
            <c:when test="${not empty batchItems}">
                <div class="table-container">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th class="table-header">Product Code</th>
                                <th class="table-header">Product Name</th>
                                <th class="table-header text-center">Batch #</th>
                                <th class="table-header text-center">Purchase Date</th>
                                <th class="table-header text-center">Expiry Date</th>
                                <th class="table-header text-right">Original Qty</th>
                                <th class="table-header text-right">Main Inv.</th>
                                <th class="table-header text-right">Physical</th>
                                <th class="table-header text-right">Online</th>
                            </tr>
                        </thead>
                        <tbody class="bg-white divide-y divide-gray-200">
                            <c:forEach var="item" items="${batchItems}">
                                <tr class="table-row">
                                    <td class="table-cell font-mono text-sm">${item.productCode()}</td>
                                    <td class="table-cell font-medium">${item.productName()}</td>
                                    <td class="table-cell text-center">
                                        <span class="px-2 py-1 text-xs font-semibold rounded-full bg-gray-100 text-gray-800">
                                            ${item.batchNumber()}
                                        </span>
                                    </td>
                                    <td class="table-cell text-center text-sm">
                                        <c:if test="${item.purchaseDate() != null}">
                                            <fmt:parseDate value="${item.purchaseDate()}" pattern="yyyy-MM-dd" var="purchaseDate" type="date"/>
                                            <fmt:formatDate value="${purchaseDate}" pattern="dd MMM yyyy"/>
                                        </c:if>
                                        <c:if test="${item.purchaseDate() == null}">-</c:if>
                                    </td>
                                    <td class="table-cell text-center text-sm">
                                        <c:choose>
                                            <c:when test="${item.expiryDate() != null}">
                                                <fmt:parseDate value="${item.expiryDate()}" pattern="yyyy-MM-dd" var="expiryDate" type="date"/>
                                                <fmt:formatDate value="${expiryDate}" pattern="dd MMM yyyy"/>
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="table-cell text-right">${item.originalQuantity()}</td>
                                    <td class="table-cell text-right">
                                        <span class="${item.remainingInMain() == 0 ? 'text-red-600 font-bold' : item.remainingInMain() < 10 ? 'text-orange-600' : 'text-gray-900'}">
                                            ${item.remainingInMain()}
                                        </span>
                                    </td>
                                    <td class="table-cell text-right text-green-600">${item.quantityInPhysical()}</td>
                                    <td class="table-cell text-right text-blue-600">${item.quantityInOnline()}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                        <tfoot class="bg-gray-50 font-bold">
                            <tr>
                                <td colspan="5" class="table-cell text-right">Totals:</td>
                                <td class="table-cell text-right">${totalOriginal}</td>
                                <td class="table-cell text-right">${totalRemaining}</td>
                                <td class="table-cell text-right text-green-600">${totalPhysical}</td>
                                <td class="table-cell text-right text-blue-600">${totalOnline}</td>
                            </tr>
                        </tfoot>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <div class="text-center py-12">
                    <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"/>
                    </svg>
                    <h3 class="mt-2 text-lg font-medium text-gray-900">No Batches Found</h3>
                    <p class="mt-1 text-sm text-gray-500">There are no batches in the main inventory.</p>
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
