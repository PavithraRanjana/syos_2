<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout pageTitle="Reports" activeNav="reports">

    <!-- Page Header -->
    <div class="mb-6">
        <h1 class="text-2xl font-bold text-gray-900">Reports & Analytics</h1>
        <p class="mt-1 text-sm text-gray-500">Business insights and performance metrics</p>
    </div>

    <!-- Quick Summary Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div class="stat-card border-syos-primary">
            <p class="text-sm font-medium text-gray-500">Today's Sales</p>
            <p class="text-2xl font-bold text-gray-900">
                Rs. <fmt:formatNumber value="${summary.todaySales()}" pattern="#,##0.00"/>
            </p>
            <p class="text-xs text-gray-500 mt-1">${summary.todayBillCount()} transactions</p>
        </div>
        <div class="stat-card border-syos-success">
            <p class="text-sm font-medium text-gray-500">This Week</p>
            <p class="text-2xl font-bold text-gray-900">
                Rs. <fmt:formatNumber value="${summary.weekSales()}" pattern="#,##0.00"/>
            </p>
        </div>
        <div class="stat-card border-syos-secondary">
            <p class="text-sm font-medium text-gray-500">This Month</p>
            <p class="text-2xl font-bold text-gray-900">
                Rs. <fmt:formatNumber value="${summary.monthSales()}" pattern="#,##0.00"/>
            </p>
        </div>
        <div class="stat-card border-syos-warning">
            <p class="text-sm font-medium text-gray-500">Low Stock Items</p>
            <p class="text-2xl font-bold text-gray-900">${summary.lowStockProductCount()}</p>
            <p class="text-xs text-red-500 mt-1">${summary.expiringProductCount()} expiring soon</p>
        </div>
    </div>

    <!-- Report Links -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- Sales Reports -->
        <div class="card">
            <h2 class="card-header">Sales Reports</h2>
            <div class="space-y-3">
                <a href="${pageContext.request.contextPath}/reports/sales" class="flex items-center p-4 bg-blue-50 rounded-lg hover:bg-blue-100 transition-colors">
                    <div class="p-3 bg-syos-primary rounded-lg">
                        <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
                        </svg>
                    </div>
                    <div class="ml-4">
                        <p class="font-medium text-gray-900">Daily Sales Report</p>
                        <p class="text-sm text-gray-500">View sales by date and store type</p>
                    </div>
                </a>

                <a href="${pageContext.request.contextPath}/reports/top-products" class="flex items-center p-4 bg-green-50 rounded-lg hover:bg-green-100 transition-colors">
                    <div class="p-3 bg-syos-success rounded-lg">
                        <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"/>
                        </svg>
                    </div>
                    <div class="ml-4">
                        <p class="font-medium text-gray-900">Top Selling Products</p>
                        <p class="text-sm text-gray-500">Best performers by revenue</p>
                    </div>
                </a>
            </div>
        </div>

        <!-- Inventory Reports -->
        <div class="card">
            <h2 class="card-header">Inventory Reports</h2>
            <div class="space-y-3">
                <a href="${pageContext.request.contextPath}/reports/inventory" class="flex items-center p-4 bg-purple-50 rounded-lg hover:bg-purple-100 transition-colors">
                    <div class="p-3 bg-syos-secondary rounded-lg">
                        <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
                        </svg>
                    </div>
                    <div class="ml-4">
                        <p class="font-medium text-gray-900">Stock Levels Report</p>
                        <p class="text-sm text-gray-500">Current inventory status</p>
                    </div>
                </a>

                <a href="${pageContext.request.contextPath}/reports/restock" class="flex items-center p-4 bg-yellow-50 rounded-lg hover:bg-yellow-100 transition-colors">
                    <div class="p-3 bg-syos-warning rounded-lg">
                        <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
                        </svg>
                    </div>
                    <div class="ml-4">
                        <p class="font-medium text-gray-900">Restock Recommendations</p>
                        <p class="text-sm text-gray-500">Items to replenish</p>
                    </div>
                </a>

                <a href="${pageContext.request.contextPath}/reports/reshelve" class="flex items-center p-4 bg-red-50 rounded-lg hover:bg-red-100 transition-colors">
                    <div class="p-3 bg-red-500 rounded-lg">
                        <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"/>
                        </svg>
                    </div>
                    <div class="ml-4">
                        <p class="font-medium text-gray-900">End of Day Reshelve Report</p>
                        <p class="text-sm text-gray-500">Items below minimum stock</p>
                    </div>
                </a>

                <a href="${pageContext.request.contextPath}/reports/reorder-level" class="flex items-center p-4 bg-orange-50 rounded-lg hover:bg-orange-100 transition-colors">
                    <div class="p-3 bg-orange-500 rounded-lg">
                        <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
                        </svg>
                    </div>
                    <div class="ml-4">
                        <p class="font-medium text-gray-900">Reorder Level Report</p>
                        <p class="text-sm text-gray-500">Main inventory below threshold</p>
                    </div>
                </a>

                <a href="${pageContext.request.contextPath}/reports/batch-stock" class="flex items-center p-4 bg-indigo-50 rounded-lg hover:bg-indigo-100 transition-colors">
                    <div class="p-3 bg-indigo-500 rounded-lg">
                        <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"/>
                        </svg>
                    </div>
                    <div class="ml-4">
                        <p class="font-medium text-gray-900">Batch-wise Stock Report</p>
                        <p class="text-sm text-gray-500">Stock details by batch</p>
                    </div>
                </a>
            </div>
        </div>
    </div>

    <!-- Top Products Quick View -->
    <div class="card mt-6">
        <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold text-gray-800">Top Selling Products This Month</h2>
            <a href="${pageContext.request.contextPath}/reports/top-products" class="text-sm text-syos-primary hover:underline">View all</a>
        </div>
        <c:choose>
            <c:when test="${not empty summary.topProducts()}">
                <div class="table-container">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th class="table-header">#</th>
                                <th class="table-header">Product</th>
                                <th class="table-header text-right">Qty Sold</th>
                                <th class="table-header text-right">Revenue</th>
                            </tr>
                        </thead>
                        <tbody class="bg-white divide-y divide-gray-200">
                            <c:forEach var="product" items="${summary.topProducts()}" varStatus="status">
                                <tr class="table-row">
                                    <td class="table-cell text-gray-500">${status.index + 1}</td>
                                    <td class="table-cell">
                                        <div class="font-medium">${product.productName()}</div>
                                        <div class="text-xs text-gray-500">${product.productCode()}</div>
                                    </td>
                                    <td class="table-cell text-right">${product.totalQuantitySold()}</td>
                                    <td class="table-cell text-right font-medium">
                                        Rs. <fmt:formatNumber value="${product.totalRevenue()}" pattern="#,##0.00"/>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <p class="text-center text-gray-500 py-8">No sales data available</p>
            </c:otherwise>
        </c:choose>
    </div>

</t:layout>
