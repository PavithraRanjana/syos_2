<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout pageTitle="Dashboard" activeNav="dashboard">

    <!-- Page Header -->
    <div class="mb-8">
        <h1 class="text-2xl font-bold text-gray-900">Dashboard</h1>
        <p class="mt-1 text-sm text-gray-500">Overview of your store operations</p>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <!-- Today's Sales -->
        <div class="stat-card border-syos-primary">
            <div class="flex items-center justify-between">
                <div>
                    <p class="text-sm font-medium text-gray-500">Today's Sales</p>
                    <p class="text-2xl font-bold text-gray-900">
                        Rs. <fmt:formatNumber value="${todaySales != null ? todaySales : 0}" pattern="#,##0.00"/>
                    </p>
                </div>
                <div class="p-3 bg-blue-100 rounded-full">
                    <svg class="w-6 h-6 text-syos-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                    </svg>
                </div>
            </div>
            <p class="mt-2 text-sm text-gray-500">
                <span class="font-medium text-gray-700">${todayBillCount != null ? todayBillCount : 0}</span> transactions
            </p>
        </div>

        <!-- This Week's Sales -->
        <div class="stat-card border-syos-success">
            <div class="flex items-center justify-between">
                <div>
                    <p class="text-sm font-medium text-gray-500">This Week</p>
                    <p class="text-2xl font-bold text-gray-900">
                        Rs. <fmt:formatNumber value="${weekSales != null ? weekSales : 0}" pattern="#,##0.00"/>
                    </p>
                </div>
                <div class="p-3 bg-green-100 rounded-full">
                    <svg class="w-6 h-6 text-syos-success" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"/>
                    </svg>
                </div>
            </div>
            <p class="mt-2 text-sm text-gray-500">7-day total</p>
        </div>

        <!-- Low Stock Alert -->
        <div class="stat-card border-syos-warning">
            <div class="flex items-center justify-between">
                <div>
                    <p class="text-sm font-medium text-gray-500">Low Stock Items</p>
                    <p class="text-2xl font-bold text-gray-900">${lowStockCount != null ? lowStockCount : 0}</p>
                </div>
                <div class="p-3 bg-yellow-100 rounded-full">
                    <svg class="w-6 h-6 text-syos-warning" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
                    </svg>
                </div>
            </div>
            <a href="${pageContext.request.contextPath}/store-stock?filter=low" class="mt-2 text-sm text-syos-warning hover:underline inline-flex items-center">
                View items
                <svg class="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
                </svg>
            </a>
        </div>

        <!-- Expiring Soon -->
        <div class="stat-card border-syos-danger">
            <div class="flex items-center justify-between">
                <div>
                    <p class="text-sm font-medium text-gray-500">Expiring Soon</p>
                    <p class="text-2xl font-bold text-gray-900">${expiringCount != null ? expiringCount : 0}</p>
                </div>
                <div class="p-3 bg-red-100 rounded-full">
                    <svg class="w-6 h-6 text-syos-danger" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
                    </svg>
                </div>
            </div>
            <a href="${pageContext.request.contextPath}/inventory?filter=expiring" class="mt-2 text-sm text-syos-danger hover:underline inline-flex items-center">
                View batches
                <svg class="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
                </svg>
            </a>
        </div>
    </div>

    <!-- Quick Actions & Top Products -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- Quick Actions -->
        <div class="card">
            <h2 class="card-header">Quick Actions</h2>
            <div class="space-y-3">
                <a href="${pageContext.request.contextPath}/pos" class="flex items-center p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors">
                    <div class="p-2 bg-syos-primary rounded-lg">
                        <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 7h6m0 10v-3m-3 3h.01M9 17h.01M9 14h.01M12 14h.01M15 11h.01M12 11h.01M9 11h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z"/>
                        </svg>
                    </div>
                    <div class="ml-3">
                        <p class="text-sm font-medium text-gray-900">New Sale</p>
                        <p class="text-xs text-gray-500">Start a new POS transaction</p>
                    </div>
                </a>

                <a href="${pageContext.request.contextPath}/inventory/add" class="flex items-center p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors">
                    <div class="p-2 bg-syos-success rounded-lg">
                        <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
                        </svg>
                    </div>
                    <div class="ml-3">
                        <p class="text-sm font-medium text-gray-900">Add Inventory</p>
                        <p class="text-xs text-gray-500">Receive new stock batch</p>
                    </div>
                </a>

                <a href="${pageContext.request.contextPath}/store-stock/restock" class="flex items-center p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors">
                    <div class="p-2 bg-syos-secondary rounded-lg">
                        <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
                        </svg>
                    </div>
                    <div class="ml-3">
                        <p class="text-sm font-medium text-gray-900">Restock Shelves</p>
                        <p class="text-xs text-gray-500">Move stock to store</p>
                    </div>
                </a>

                <a href="${pageContext.request.contextPath}/reports" class="flex items-center p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors">
                    <div class="p-2 bg-syos-warning rounded-lg">
                        <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
                        </svg>
                    </div>
                    <div class="ml-3">
                        <p class="text-sm font-medium text-gray-900">View Reports</p>
                        <p class="text-xs text-gray-500">Sales & inventory analytics</p>
                    </div>
                </a>
            </div>
        </div>

        <!-- Top Selling Products -->
        <div class="card lg:col-span-2">
            <h2 class="card-header">Top Selling Products (This Month)</h2>
            <c:choose>
                <c:when test="${not empty topProducts}">
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
                                <c:forEach var="product" items="${topProducts}" varStatus="status">
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
                    <div class="text-center py-8 text-gray-500">
                        <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
                        </svg>
                        <p class="mt-2">No sales data available yet</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <!-- Monthly Sales Overview -->
    <div class="mt-6 card">
        <h2 class="card-header">This Month's Performance</h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div class="text-center p-4 bg-gray-50 rounded-lg">
                <p class="text-sm text-gray-500">Total Sales</p>
                <p class="text-3xl font-bold text-gray-900">
                    Rs. <fmt:formatNumber value="${monthSales != null ? monthSales : 0}" pattern="#,##0.00"/>
                </p>
            </div>
            <div class="text-center p-4 bg-gray-50 rounded-lg">
                <p class="text-sm text-gray-500">Today vs Average</p>
                <c:set var="avgDaily" value="${monthSales != null && monthSales > 0 ? monthSales / 30 : 0}"/>
                <c:set var="todayVsAvg" value="${avgDaily > 0 ? ((todaySales - avgDaily) / avgDaily * 100) : 0}"/>
                <p class="text-3xl font-bold ${todayVsAvg >= 0 ? 'text-green-600' : 'text-red-600'}">
                    <c:if test="${todayVsAvg >= 0}">+</c:if><fmt:formatNumber value="${todayVsAvg}" pattern="#,##0.0"/>%
                </p>
            </div>
            <div class="text-center p-4 bg-gray-50 rounded-lg">
                <p class="text-sm text-gray-500">Daily Average</p>
                <p class="text-3xl font-bold text-gray-900">
                    Rs. <fmt:formatNumber value="${avgDaily != null ? avgDaily : 0}" pattern="#,##0.00"/>
                </p>
            </div>
        </div>
    </div>

</t:layout>
