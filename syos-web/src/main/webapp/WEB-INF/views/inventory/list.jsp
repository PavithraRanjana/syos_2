<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout pageTitle="Inventory" activeNav="inventory">

    <!-- Page Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-6">
        <div>
            <h1 class="text-2xl font-bold text-gray-900">Main Inventory</h1>
            <p class="mt-1 text-sm text-gray-500">
                <c:choose>
                    <c:when test="${not empty filterLabel}">${filterLabel}</c:when>
                    <c:otherwise>${totalBatches} total batches</c:otherwise>
                </c:choose>
            </p>
        </div>
        <div class="mt-4 sm:mt-0 flex space-x-3">
            <a href="${pageContext.request.contextPath}/inventory/summary" class="btn-secondary">
                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
                </svg>
                Summary
            </a>
            <a href="${pageContext.request.contextPath}/inventory/add" class="btn-primary">
                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"/>
                </svg>
                Add Batch
            </a>
        </div>
    </div>

    <!-- Quick Filters -->
    <div class="card mb-6">
        <div class="flex flex-wrap gap-3">
            <a href="${pageContext.request.contextPath}/inventory"
               class="px-4 py-2 rounded-lg ${empty filter ? 'bg-syos-primary text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'}">
                All Batches
            </a>
            <a href="${pageContext.request.contextPath}/inventory?filter=expiring&days=7"
               class="px-4 py-2 rounded-lg ${filter == 'expiring' ? 'bg-syos-warning text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'}">
                Expiring Soon
            </a>
            <a href="${pageContext.request.contextPath}/inventory?filter=expired"
               class="px-4 py-2 rounded-lg ${filter == 'expired' ? 'bg-syos-danger text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'}">
                Expired
            </a>
        </div>
    </div>

    <!-- Batches Table -->
    <div class="card">
        <c:choose>
            <c:when test="${not empty batches}">
                <div class="table-container">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th class="table-header">Batch ID</th>
                                <th class="table-header">Product</th>
                                <th class="table-header text-right">Received</th>
                                <th class="table-header text-right">Remaining</th>
                                <th class="table-header">Purchase Date</th>
                                <th class="table-header">Expiry Date</th>
                                <th class="table-header text-center">Status</th>
                                <th class="table-header text-right">Actions</th>
                            </tr>
                        </thead>
                        <tbody class="bg-white divide-y divide-gray-200">
                            <c:forEach var="batch" items="${batches}">
                                <tr class="table-row">
                                    <td class="table-cell">
                                        <span class="font-mono text-sm bg-gray-100 px-2 py-1 rounded">#${batch.mainInventoryId}</span>
                                    </td>
                                    <td class="table-cell">
                                        <div class="font-medium">${batch.productName}</div>
                                        <div class="text-xs text-gray-500">${batch.productCodeString}</div>
                                    </td>
                                    <td class="table-cell text-right">${batch.quantityReceived}</td>
                                    <td class="table-cell text-right font-medium">
                                        <span class="${batch.remainingQuantity <= 10 ? 'text-red-600' : ''}">${batch.remainingQuantity}</span>
                                    </td>
                                    <td class="table-cell">
                                        ${batch.purchaseDateFormatted}
                                    </td>
                                    <td class="table-cell">
                                        <c:if test="${not empty batch.expiryDate}">
                                            ${batch.expiryDateFormatted}
                                        </c:if>
                                        <c:if test="${empty batch.expiryDate}">-</c:if>
                                    </td>
                                    <td class="table-cell text-center">
                                        <c:choose>
                                            <c:when test="${batch.expired}">
                                                <span class="badge badge-danger">Expired</span>
                                            </c:when>
                                            <c:when test="${batch.expiringSoon}">
                                                <span class="badge badge-warning">Expiring Soon</span>
                                            </c:when>
                                            <c:when test="${batch.remainingQuantity == 0}">
                                                <span class="badge badge-danger">Depleted</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-success">Available</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="table-cell text-right">
                                        <a href="${pageContext.request.contextPath}/inventory/view/${batch.mainInventoryId}"
                                           class="text-blue-600 hover:text-blue-800" title="View Details">
                                            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                                            </svg>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>

                <!-- Pagination -->
                <c:if test="${totalPages > 1 && empty filter}">
                    <div class="mt-4 flex items-center justify-between border-t border-gray-200 pt-4">
                        <div class="text-sm text-gray-500">Page ${currentPage + 1} of ${totalPages}</div>
                        <div class="flex space-x-2">
                            <c:if test="${currentPage > 0}">
                                <a href="${pageContext.request.contextPath}/inventory?page=${currentPage - 1}&size=${pageSize}"
                                   class="btn-secondary btn-sm">Previous</a>
                            </c:if>
                            <c:if test="${currentPage < totalPages - 1}">
                                <a href="${pageContext.request.contextPath}/inventory?page=${currentPage + 1}&size=${pageSize}"
                                   class="btn-secondary btn-sm">Next</a>
                            </c:if>
                        </div>
                    </div>
                </c:if>
            </c:when>
            <c:otherwise>
                <div class="text-center py-12">
                    <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
                    </svg>
                    <h3 class="mt-2 text-sm font-medium text-gray-900">No batches found</h3>
                    <p class="mt-1 text-sm text-gray-500">Add your first inventory batch to get started.</p>
                    <div class="mt-6">
                        <a href="${pageContext.request.contextPath}/inventory/add" class="btn-primary">Add Batch</a>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

</t:layout>
