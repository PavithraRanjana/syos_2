<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout pageTitle="Sales Report" activeNav="reports">

    <!-- Page Header -->
    <div class="mb-6">
        <div class="flex items-center justify-between">
            <div>
                <h1 class="text-2xl font-bold text-gray-900">Daily Sales Report</h1>
                <p class="mt-1 text-sm text-gray-500">Sales performance for selected date</p>
            </div>
            <a href="${pageContext.request.contextPath}/reports" class="btn btn-secondary">
                Back to Reports
            </a>
        </div>
    </div>

    <!-- Date Picker and Store Type Filter -->
    <div class="card mb-6">
        <form method="get" action="${pageContext.request.contextPath}/reports/sales" class="flex flex-wrap items-center gap-4">
            <div class="flex items-center gap-2">
                <label for="date" class="font-medium text-gray-700">Date:</label>
                <input type="date" id="date" name="date" value="${selectedDate}"
                       class="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-syos-primary focus:border-syos-primary">
            </div>
            <div class="flex items-center gap-2">
                <label for="storeType" class="font-medium text-gray-700">Store:</label>
                <select id="storeType" name="storeType"
                        class="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-syos-primary focus:border-syos-primary">
                    <option value="ALL" ${selectedStoreType == 'ALL' ? 'selected' : ''}>All Stores</option>
                    <option value="PHYSICAL" ${selectedStoreType == 'PHYSICAL' ? 'selected' : ''}>Physical Store</option>
                    <option value="ONLINE" ${selectedStoreType == 'ONLINE' ? 'selected' : ''}>Online Store</option>
                </select>
            </div>
            <button type="submit" class="btn btn-primary">View Report</button>
        </form>
    </div>

    <!-- Store Type Tabs -->
    <div class="mb-6">
        <div class="border-b border-gray-200">
            <nav class="-mb-px flex space-x-8">
                <a href="${pageContext.request.contextPath}/reports/sales?date=${selectedDate}&storeType=ALL"
                   class="py-3 px-1 border-b-2 font-medium text-sm ${selectedStoreType == 'ALL' ? 'border-syos-primary text-syos-primary' : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'}">
                    All Stores (Consolidated)
                </a>
                <a href="${pageContext.request.contextPath}/reports/sales?date=${selectedDate}&storeType=PHYSICAL"
                   class="py-3 px-1 border-b-2 font-medium text-sm ${selectedStoreType == 'PHYSICAL' ? 'border-green-600 text-green-600' : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'}">
                    Physical Store
                </a>
                <a href="${pageContext.request.contextPath}/reports/sales?date=${selectedDate}&storeType=ONLINE"
                   class="py-3 px-1 border-b-2 font-medium text-sm ${selectedStoreType == 'ONLINE' ? 'border-blue-600 text-blue-600' : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'}">
                    Online Store
                </a>
            </nav>
        </div>
    </div>

    <!-- Report Title Banner -->
    <div class="mb-6 p-4 rounded-lg ${selectedStoreType == 'PHYSICAL' ? 'bg-green-50 border border-green-200' : selectedStoreType == 'ONLINE' ? 'bg-blue-50 border border-blue-200' : 'bg-gray-50 border border-gray-200'}">
        <div class="flex items-center">
            <c:choose>
                <c:when test="${selectedStoreType == 'PHYSICAL'}">
                    <div class="p-2 bg-green-500 rounded-lg mr-3">
                        <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
                        </svg>
                    </div>
                    <div>
                        <h2 class="text-lg font-semibold text-green-800">Physical Store Sales Report</h2>
                        <p class="text-sm text-green-600">
                            <fmt:parseDate value="${selectedDate}" pattern="yyyy-MM-dd" var="parsedDate" type="date"/>
                            <fmt:formatDate value="${parsedDate}" pattern="EEEE, dd MMMM yyyy"/>
                        </p>
                    </div>
                </c:when>
                <c:when test="${selectedStoreType == 'ONLINE'}">
                    <div class="p-2 bg-blue-500 rounded-lg mr-3">
                        <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9"/>
                        </svg>
                    </div>
                    <div>
                        <h2 class="text-lg font-semibold text-blue-800">Online Store Sales Report</h2>
                        <p class="text-sm text-blue-600">
                            <fmt:parseDate value="${selectedDate}" pattern="yyyy-MM-dd" var="parsedDate" type="date"/>
                            <fmt:formatDate value="${parsedDate}" pattern="EEEE, dd MMMM yyyy"/>
                        </p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="p-2 bg-gray-500 rounded-lg mr-3">
                        <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
                        </svg>
                    </div>
                    <div>
                        <h2 class="text-lg font-semibold text-gray-800">Consolidated Sales Report (All Stores)</h2>
                        <p class="text-sm text-gray-600">
                            <fmt:parseDate value="${selectedDate}" pattern="yyyy-MM-dd" var="parsedDate" type="date"/>
                            <fmt:formatDate value="${parsedDate}" pattern="EEEE, dd MMMM yyyy"/>
                        </p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <!-- Summary Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div class="stat-card ${selectedStoreType == 'PHYSICAL' ? 'border-green-500' : selectedStoreType == 'ONLINE' ? 'border-blue-500' : 'border-syos-primary'}">
            <p class="text-sm font-medium text-gray-500">Total Revenue</p>
            <p class="text-2xl font-bold text-gray-900">
                Rs. <fmt:formatNumber value="${salesSummary.totalSales()}" pattern="#,##0.00"/>
            </p>
        </div>
        <div class="stat-card border-syos-success">
            <p class="text-sm font-medium text-gray-500">Transaction Count</p>
            <p class="text-2xl font-bold text-gray-900">${salesSummary.totalBills()}</p>
            <p class="text-xs text-gray-500 mt-1">individual purchases</p>
        </div>
        <div class="stat-card border-syos-secondary">
            <p class="text-sm font-medium text-gray-500">Items Sold</p>
            <p class="text-2xl font-bold text-gray-900">${totalQuantitySold}</p>
            <p class="text-xs text-gray-500 mt-1">total units</p>
        </div>
        <div class="stat-card border-syos-warning">
            <p class="text-sm font-medium text-gray-500">Avg. Bill Value</p>
            <p class="text-2xl font-bold text-gray-900">
                Rs. <fmt:formatNumber value="${salesSummary.averageBillValue()}" pattern="#,##0.00"/>
            </p>
            <p class="text-xs text-gray-500 mt-1">per transaction</p>
        </div>
    </div>

    <!-- Sales by Store Type (only show in consolidated view) -->
    <c:if test="${selectedStoreType == 'ALL'}">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
            <c:forEach var="storeReport" items="${storeTypeReport}">
                <div class="card">
                    <div class="flex items-center justify-between">
                        <div>
                            <h3 class="text-lg font-semibold text-gray-800">
                                <c:choose>
                                    <c:when test="${storeReport.storeType() == 'PHYSICAL'}">Physical Store</c:when>
                                    <c:otherwise>Online Store</c:otherwise>
                                </c:choose>
                            </h3>
                            <p class="text-sm text-gray-500">${storeReport.billCount()} transactions</p>
                        </div>
                        <div class="text-right">
                            <p class="text-2xl font-bold ${storeReport.storeType() == 'PHYSICAL' ? 'text-green-600' : 'text-blue-600'}">
                                Rs. <fmt:formatNumber value="${storeReport.totalSales()}" pattern="#,##0.00"/>
                            </p>
                            <a href="${pageContext.request.contextPath}/reports/sales?date=${selectedDate}&storeType=${storeReport.storeType()}"
                               class="text-sm ${storeReport.storeType() == 'PHYSICAL' ? 'text-green-600' : 'text-blue-600'} hover:underline">
                                View Details
                            </a>
                        </div>
                    </div>
                </div>
            </c:forEach>
            <c:if test="${empty storeTypeReport}">
                <div class="card col-span-2">
                    <p class="text-center text-gray-500 py-4">No sales recorded for this date</p>
                </div>
            </c:if>
        </div>
    </c:if>

    <!-- Top Selling Products -->
    <div class="card mb-6">
        <h2 class="card-header">Top Selling Products</h2>
        <c:choose>
            <c:when test="${not empty topProducts}">
                <div class="grid grid-cols-1 md:grid-cols-5 gap-4">
                    <c:forEach var="product" items="${topProducts}" varStatus="status">
                        <div class="p-4 bg-gradient-to-br ${status.index == 0 ? 'from-yellow-50 to-yellow-100 border-yellow-300' : status.index == 1 ? 'from-gray-50 to-gray-100 border-gray-300' : status.index == 2 ? 'from-orange-50 to-orange-100 border-orange-300' : 'from-blue-50 to-blue-100 border-blue-200'} border rounded-lg">
                            <div class="flex items-center justify-between mb-2">
                                <span class="text-2xl font-bold ${status.index == 0 ? 'text-yellow-600' : status.index == 1 ? 'text-gray-500' : status.index == 2 ? 'text-orange-600' : 'text-blue-500'}">#${status.index + 1}</span>
                                <span class="text-xs font-mono bg-white px-2 py-1 rounded">${product.productCode()}</span>
                            </div>
                            <p class="font-medium text-gray-900 text-sm truncate" title="${product.productName()}">${product.productName()}</p>
                            <p class="text-lg font-bold text-gray-800 mt-2">${product.totalQuantitySold()} sold</p>
                            <p class="text-sm text-gray-600">Rs. <fmt:formatNumber value="${product.totalRevenue()}" pattern="#,##0.00"/></p>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <p class="text-center text-gray-500 py-8">No products sold on this date</p>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- All Products Sold -->
    <div class="card">
        <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold text-gray-800">All Products Sold</h2>
            <span class="text-sm text-gray-500">${productSales.size()} products</span>
        </div>

        <c:choose>
            <c:when test="${not empty productSales}">
                <div class="table-container">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th class="table-header">#</th>
                                <th class="table-header">Product Code</th>
                                <th class="table-header">Product Name</th>
                                <th class="table-header text-right">Qty Sold</th>
                                <th class="table-header text-right">Total Revenue</th>
                            </tr>
                        </thead>
                        <tbody class="bg-white divide-y divide-gray-200">
                            <c:forEach var="product" items="${productSales}" varStatus="status">
                                <tr class="table-row">
                                    <td class="table-cell text-gray-500">${status.index + 1}</td>
                                    <td class="table-cell">
                                        <span class="font-mono text-sm bg-gray-100 px-2 py-1 rounded">${product.productCode()}</span>
                                    </td>
                                    <td class="table-cell font-medium">${product.productName()}</td>
                                    <td class="table-cell text-right">${product.totalQuantitySold()}</td>
                                    <td class="table-cell text-right font-medium">
                                        Rs. <fmt:formatNumber value="${product.totalRevenue()}" pattern="#,##0.00"/>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                        <tfoot class="bg-gray-50 font-bold">
                            <tr>
                                <td colspan="3" class="table-cell text-right">Totals:</td>
                                <td class="table-cell text-right">${totalQuantitySold}</td>
                                <td class="table-cell text-right">
                                    Rs. <fmt:formatNumber value="${salesSummary.totalSales()}" pattern="#,##0.00"/>
                                </td>
                            </tr>
                        </tfoot>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <div class="text-center py-12">
                    <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
                    </svg>
                    <h3 class="mt-2 text-lg font-medium text-gray-900">No Sales Data</h3>
                    <p class="mt-1 text-sm text-gray-500">No products were sold on this date
                        <c:if test="${selectedStoreType != 'ALL'}">
                            in the ${selectedStoreType == 'PHYSICAL' ? 'Physical' : 'Online'} Store
                        </c:if>.
                    </p>
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
