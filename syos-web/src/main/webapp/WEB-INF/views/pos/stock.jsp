<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
            <%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

                <t:layout pageTitle="Physical Store Stock" activeNav="pos-stock">

                    <!-- Page Header -->
                    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-6">
                        <div>
                            <h1 class="text-2xl font-bold text-gray-900">Physical Store Stock</h1>
                            <p class="mt-1 text-sm text-gray-500">View product codes and stock quantities for in-store
                                sales</p>
                        </div>
                        <div class="mt-4 sm:mt-0">
                            <a href="${pageContext.request.contextPath}/pos/new" class="btn-primary">
                                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                                </svg>
                                New Sale
                            </a>
                        </div>
                    </div>

                    <!-- Summary Cards -->
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
                        <div class="stat-card border-syos-primary">
                            <div class="flex items-center justify-between">
                                <div>
                                    <p class="text-sm font-medium text-gray-500">Total Products In Stock</p>
                                    <p class="text-3xl font-bold text-gray-900">${stockSummary.size()}</p>
                                </div>
                                <div class="p-3 bg-blue-100 rounded-full">
                                    <svg class="w-8 h-8 text-syos-primary" fill="none" stroke="currentColor"
                                        viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                                    </svg>
                                </div>
                            </div>
                        </div>
                        <div class="stat-card border-syos-warning">
                            <div class="flex items-center justify-between">
                                <div>
                                    <p class="text-sm font-medium text-gray-500">Low Stock Items</p>
                                    <p class="text-3xl font-bold text-yellow-600">${lowStockCount}</p>
                                </div>
                                <div class="p-3 bg-yellow-100 rounded-full">
                                    <svg class="w-8 h-8 text-syos-warning" fill="none" stroke="currentColor"
                                        viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                                    </svg>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Stock Table -->
                    <div class="card">
                        <h2 class="card-header">Available Stock</h2>
                        <c:choose>
                            <c:when test="${not empty stockSummary}">
                                <div class="table-container">
                                    <table class="data-table">
                                        <thead>
                                            <tr>
                                                <th class="table-header">Product Code</th>
                                                <th class="table-header">Product Name</th>
                                                <th class="table-header text-right">Batches</th>
                                                <th class="table-header text-right">Quantity Available</th>
                                                <th class="table-header text-right">Status</th>
                                            </tr>
                                        </thead>
                                        <tbody class="bg-white divide-y divide-gray-200">
                                            <c:forEach var="item" items="${stockSummary}">
                                                <tr class="table-row hover:bg-gray-50">
                                                    <td class="table-cell">
                                                        <span
                                                            class="font-mono text-sm bg-gray-100 px-2 py-1 rounded">${item.productCode()}</span>
                                                    </td>
                                                    <td class="table-cell font-medium">${item.productName()}</td>
                                                    <td class="table-cell text-right">${item.batchCount()}</td>
                                                    <td class="table-cell text-right">
                                                        <span
                                                            class="${item.totalQuantity() <= 10 ? 'text-red-600 font-bold' : 'font-semibold'}">${item.totalQuantity()}</span>
                                                    </td>
                                                    <td class="table-cell text-right">
                                                        <c:choose>
                                                            <c:when test="${item.totalQuantity() <= 0}">
                                                                <span class="badge badge-danger">Out of Stock</span>
                                                            </c:when>
                                                            <c:when test="${item.totalQuantity() <= 10}">
                                                                <span class="badge badge-warning">Low Stock</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge badge-success">In Stock</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="text-center py-12 text-gray-500">
                                    <svg class="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" stroke="currentColor"
                                        viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                                    </svg>
                                    <p>No stock available in physical store</p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                </t:layout>