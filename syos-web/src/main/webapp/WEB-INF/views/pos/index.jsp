<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout pageTitle="Point of Sale" activeNav="pos">

    <!-- Page Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-6">
        <div>
            <h1 class="text-2xl font-bold text-gray-900">Point of Sale</h1>
            <p class="mt-1 text-sm text-gray-500">Create and manage sales transactions</p>
        </div>
        <div class="mt-4 sm:mt-0">
            <a href="${pageContext.request.contextPath}/pos/new" class="btn-primary">
                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"/>
                </svg>
                New Sale
            </a>
        </div>
    </div>

    <!-- Today's Summary -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
        <div class="stat-card border-syos-primary">
            <div class="flex items-center justify-between">
                <div>
                    <p class="text-sm font-medium text-gray-500">Today's Sales</p>
                    <p class="text-3xl font-bold text-gray-900">
                        Rs. <fmt:formatNumber value="${todaySales != null ? todaySales : 0}" pattern="#,##0.00"/>
                    </p>
                </div>
                <div class="p-3 bg-blue-100 rounded-full">
                    <svg class="w-8 h-8 text-syos-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                    </svg>
                </div>
            </div>
        </div>
        <div class="stat-card border-syos-success">
            <div class="flex items-center justify-between">
                <div>
                    <p class="text-sm font-medium text-gray-500">Transactions Today</p>
                    <p class="text-3xl font-bold text-gray-900">${todayBillCount != null ? todayBillCount : 0}</p>
                </div>
                <div class="p-3 bg-green-100 rounded-full">
                    <svg class="w-8 h-8 text-syos-success" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"/>
                    </svg>
                </div>
            </div>
        </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- Quick Actions -->
        <div class="card">
            <h2 class="card-header">Quick Actions</h2>
            <div class="space-y-3">
                <a href="${pageContext.request.contextPath}/pos/new?storeType=PHYSICAL"
                   class="flex items-center p-4 bg-blue-50 rounded-lg hover:bg-blue-100 transition-colors">
                    <div class="p-3 bg-syos-primary rounded-lg">
                        <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
                        </svg>
                    </div>
                    <div class="ml-4">
                        <p class="font-medium text-gray-900">Physical Store Sale</p>
                        <p class="text-sm text-gray-500">In-store cash register</p>
                    </div>
                </a>

                <a href="${pageContext.request.contextPath}/pos/new?storeType=ONLINE"
                   class="flex items-center p-4 bg-purple-50 rounded-lg hover:bg-purple-100 transition-colors">
                    <div class="p-3 bg-syos-secondary rounded-lg">
                        <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9"/>
                        </svg>
                    </div>
                    <div class="ml-4">
                        <p class="font-medium text-gray-900">Online Order</p>
                        <p class="text-sm text-gray-500">Process web order</p>
                    </div>
                </a>

                <a href="${pageContext.request.contextPath}/pos/history"
                   class="flex items-center p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors">
                    <div class="p-3 bg-gray-500 rounded-lg">
                        <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
                        </svg>
                    </div>
                    <div class="ml-4">
                        <p class="font-medium text-gray-900">Transaction History</p>
                        <p class="text-sm text-gray-500">View past transactions</p>
                    </div>
                </a>
            </div>
        </div>

        <!-- Recent Transactions -->
        <div class="card lg:col-span-2">
            <div class="flex items-center justify-between mb-4">
                <h2 class="text-lg font-semibold text-gray-800">Recent Transactions</h2>
                <a href="${pageContext.request.contextPath}/pos/history" class="text-sm text-syos-primary hover:underline">View all</a>
            </div>
            <c:choose>
                <c:when test="${not empty recentBills}">
                    <div class="table-container">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th class="table-header">Bill #</th>
                                    <th class="table-header">Type</th>
                                    <th class="table-header">Time</th>
                                    <th class="table-header text-right">Amount</th>
                                    <th class="table-header text-right">Action</th>
                                </tr>
                            </thead>
                            <tbody class="bg-white divide-y divide-gray-200">
                                <c:forEach var="bill" items="${recentBills}">
                                    <tr class="table-row">
                                        <td class="table-cell">
                                            <span class="font-mono text-sm">${bill.serialNumberString}</span>
                                        </td>
                                        <td class="table-cell">
                                            <span class="badge ${bill.storeType == 'PHYSICAL' ? 'badge-info' : 'badge-success'}">
                                                ${bill.storeType}
                                            </span>
                                        </td>
                                        <td class="table-cell text-gray-500">
                                            ${bill.billTime}
                                        </td>
                                        <td class="table-cell text-right font-medium">
                                            Rs. <fmt:formatNumber value="${bill.totalAmount.amount}" pattern="#,##0.00"/>
                                        </td>
                                        <td class="table-cell text-right">
                                            <a href="${pageContext.request.contextPath}/pos/bill/${bill.billId}"
                                               class="text-syos-primary hover:underline">View</a>
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
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
                        </svg>
                        <p class="mt-2">No transactions today</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

</t:layout>
