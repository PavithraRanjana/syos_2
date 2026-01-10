<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
            <%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

                <t:layout pageTitle="Transaction History" activeNav="pos">

                    <!-- Page Header -->
                    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-6">
                        <div>
                            <h1 class="text-2xl font-bold text-gray-900">Transaction History</h1>
                            <p class="mt-1 text-sm text-gray-500">View all sales transactions</p>
                        </div>
                        <div class="mt-4 sm:mt-0 flex space-x-3">
                            <a href="${pageContext.request.contextPath}/pos/new" class="btn-primary">
                                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                                </svg>
                                New Sale
                            </a>
                            <a href="${pageContext.request.contextPath}/pos" class="btn-secondary">
                                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                                </svg>
                                Back to POS
                            </a>
                        </div>
                    </div>

                    <!-- Date Filter -->
                    <div class="card mb-6">
                        <form method="get" action="${pageContext.request.contextPath}/pos/history"
                            class="flex flex-wrap items-end gap-4">
                            <div>
                                <label class="input-label">Filter by Date</label>
                                <input type="date" name="date" value="${selectedDate}" class="input-field"
                                    onchange="this.form.submit()">
                            </div>
                            <div class="flex space-x-2">
                                <button type="submit" class="btn-primary">
                                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                                    </svg>
                                    Filter
                                </button>
                                <a href="${pageContext.request.contextPath}/pos/history" class="btn-secondary">
                                    Clear
                                </a>
                            </div>
                        </form>
                    </div>

                    <!-- Summary Stats -->
                    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
                        <div class="stat-card border-syos-primary">
                            <div class="flex items-center justify-between">
                                <div>
                                    <p class="text-sm font-medium text-gray-500">Total Transactions</p>
                                    <p class="text-3xl font-bold text-gray-900">${bills.size()}</p>
                                </div>
                                <div class="p-3 bg-blue-100 rounded-full">
                                    <svg class="w-8 h-8 text-syos-primary" fill="none" stroke="currentColor"
                                        viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
                                    </svg>
                                </div>
                            </div>
                        </div>
                        <div class="stat-card border-syos-success">
                            <div class="flex items-center justify-between">
                                <div>
                                    <p class="text-sm font-medium text-gray-500">Total Sales</p>
                                    <p class="text-3xl font-bold text-gray-900">
                                        <c:set var="totalSales" value="0" />
                                        <c:forEach var="bill" items="${bills}">
                                            <c:set var="totalSales"
                                                value="${totalSales + (bill.totalAmount != null ? bill.totalAmount.amount : 0)}" />
                                        </c:forEach>
                                        Rs.
                                        <fmt:formatNumber value="${totalSales}" pattern="#,##0.00" />
                                    </p>
                                </div>
                                <div class="p-3 bg-green-100 rounded-full">
                                    <svg class="w-8 h-8 text-syos-success" fill="none" stroke="currentColor"
                                        viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                    </svg>
                                </div>
                            </div>
                        </div>
                        <div class="stat-card border-syos-warning">
                            <div class="flex items-center justify-between">
                                <div>
                                    <p class="text-sm font-medium text-gray-500">Viewing Date</p>
                                    <p class="text-2xl font-bold text-gray-900">${selectedDate}</p>
                                </div>
                                <div class="p-3 bg-yellow-100 rounded-full">
                                    <svg class="w-8 h-8 text-syos-warning" fill="none" stroke="currentColor"
                                        viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                    </svg>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Transactions Table -->
                    <div class="card">
                        <h2 class="card-header">All Transactions</h2>
                        <c:choose>
                            <c:when test="${not empty bills}">
                                <div class="table-container">
                                    <table class="data-table">
                                        <thead>
                                            <tr>
                                                <th class="table-header">#</th>
                                                <th class="table-header">Bill Number</th>
                                                <th class="table-header">Store Type</th>
                                                <th class="table-header">Payment</th>
                                                <th class="table-header">Time</th>
                                                <th class="table-header text-right">Amount</th>
                                                <th class="table-header text-right">Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody class="bg-white divide-y divide-gray-200">
                                            <c:forEach var="bill" items="${bills}" varStatus="status">
                                                <tr class="table-row">
                                                    <td class="table-cell text-gray-500">${status.index + 1}</td>
                                                    <td class="table-cell">
                                                        <span
                                                            class="font-mono text-sm bg-gray-100 px-2 py-1 rounded">${bill.serialNumberString}</span>
                                                    </td>
                                                    <td class="table-cell">
                                                        <span
                                                            class="badge ${bill.storeType.name() eq 'PHYSICAL' ? 'badge-info' : 'badge-success'}">
                                                            <c:choose>
                                                                <c:when test="${bill.storeType.name() eq 'PHYSICAL'}">
                                                                    Physical</c:when>
                                                                <c:otherwise>Online</c:otherwise>
                                                            </c:choose>
                                                        </span>
                                                    </td>
                                                    <td class="table-cell">
                                                        <span
                                                            class="badge ${bill.transactionType.name() eq 'CASH' ? 'badge-success' : 'badge-warning'}">
                                                            ${bill.transactionType.name()}
                                                        </span>
                                                    </td>
                                                    <td class="table-cell text-gray-500">${bill.billTime}</td>
                                                    <td class="table-cell text-right font-semibold">
                                                        Rs.
                                                        <fmt:formatNumber
                                                            value="${bill.totalAmount != null ? bill.totalAmount.amount : 0}"
                                                            pattern="#,##0.00" />
                                                    </td>
                                                    <td class="table-cell text-right space-x-2">
                                                        <a href="${pageContext.request.contextPath}/pos/bill/${bill.billId}"
                                                            class="text-syos-primary hover:underline">View</a>
                                                        <a href="${pageContext.request.contextPath}/pos/receipt/${bill.billId}"
                                                            class="text-green-600 hover:underline">Receipt</a>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                        <tfoot class="bg-gray-50">
                                            <tr>
                                                <td colspan="5" class="table-cell text-right font-bold">Total:</td>
                                                <td class="table-cell text-right font-bold text-lg text-syos-primary">
                                                    Rs.
                                                    <fmt:formatNumber value="${totalSales}" pattern="#,##0.00" />
                                                </td>
                                                <td></td>
                                            </tr>
                                        </tfoot>
                                    </table>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="text-center py-12 text-gray-500">
                                    <svg class="mx-auto h-16 w-16 text-gray-400 mb-4" fill="none" stroke="currentColor"
                                        viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                                    </svg>
                                    <p class="text-lg font-medium">No transactions found</p>
                                    <p class="text-sm mt-1">No bills were recorded on ${selectedDate}</p>
                                    <a href="${pageContext.request.contextPath}/pos/new"
                                        class="btn-primary mt-4 inline-flex">
                                        Create New Sale
                                    </a>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                </t:layout>