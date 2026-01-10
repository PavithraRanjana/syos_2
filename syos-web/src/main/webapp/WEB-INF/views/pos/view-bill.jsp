<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
            <%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

                <t:layout pageTitle="Bill Details" activeNav="pos">

                    <!-- Page Header -->
                    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-6">
                        <div>
                            <h1 class="text-2xl font-bold text-gray-900">Bill Details</h1>
                            <p class="mt-1 text-sm text-gray-500">Transaction #${bill.serialNumberString}</p>
                        </div>
                        <div class="mt-4 sm:mt-0 flex space-x-3">
                            <a href="${pageContext.request.contextPath}/pos/receipt/${bill.billId}" class="btn-success">
                                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M17 17h2a2 2 0 002-2v-4a2 2 0 00-2-2H5a2 2 0 00-2 2v4a2 2 0 002 2h2m2 4h6a2 2 0 002-2v-4a2 2 0 00-2-2H9a2 2 0 00-2 2v4a2 2 0 002 2zm8-12V5a2 2 0 00-2-2H9a2 2 0 00-2 2v4h10z" />
                                </svg>
                                Print Receipt
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

                    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
                        <!-- Bill Summary -->
                        <div class="card">
                            <h2 class="card-header">Bill Information</h2>
                            <div class="space-y-4">
                                <div class="flex justify-between">
                                    <span class="text-gray-500">Bill Number:</span>
                                    <span class="font-mono font-semibold">${bill.serialNumberString}</span>
                                </div>
                                <div class="flex justify-between">
                                    <span class="text-gray-500">Date:</span>
                                    <span>${bill.billDateFormatted}</span>
                                </div>
                                <div class="flex justify-between">
                                    <span class="text-gray-500">Time:</span>
                                    <span>${bill.billTime}</span>
                                </div>
                                <div class="flex justify-between">
                                    <span class="text-gray-500">Store Type:</span>
                                    <span
                                        class="badge ${bill.storeType.name() eq 'PHYSICAL' ? 'badge-info' : 'badge-success'}">
                                        <c:choose>
                                            <c:when test="${bill.storeType.name() eq 'PHYSICAL'}">Physical Store
                                            </c:when>
                                            <c:otherwise>Online Store</c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                                <div class="flex justify-between">
                                    <span class="text-gray-500">Transaction Type:</span>
                                    <span
                                        class="badge ${bill.transactionType.name() eq 'CASH' ? 'badge-success' : 'badge-info'}">
                                        ${bill.transactionType.name()}
                                    </span>
                                </div>
                                <c:if test="${not empty bill.cashierId}">
                                    <div class="flex justify-between">
                                        <span class="text-gray-500">Cashier:</span>
                                        <span>${bill.cashierId}</span>
                                    </div>
                                </c:if>
                            </div>
                        </div>

                        <!-- Payment Summary -->
                        <div class="card">
                            <h2 class="card-header">Payment Summary</h2>
                            <div class="space-y-4">
                                <div class="flex justify-between">
                                    <span class="text-gray-500">Subtotal:</span>
                                    <span>Rs.
                                        <fmt:formatNumber value="${bill.subtotal != null ? bill.subtotal.amount : 0}"
                                            pattern="#,##0.00" />
                                    </span>
                                </div>
                                <c:if test="${bill.discountAmount != null and bill.discountAmount.amount > 0}">
                                    <div class="flex justify-between text-green-600">
                                        <span>Discount:</span>
                                        <span>- Rs.
                                            <fmt:formatNumber value="${bill.discountAmount.amount}"
                                                pattern="#,##0.00" />
                                        </span>
                                    </div>
                                </c:if>
                                <c:if test="${bill.taxAmount != null and bill.taxAmount.amount > 0}">
                                    <div class="flex justify-between">
                                        <span class="text-gray-500">Tax:</span>
                                        <span>Rs.
                                            <fmt:formatNumber value="${bill.taxAmount.amount}" pattern="#,##0.00" />
                                        </span>
                                    </div>
                                </c:if>
                                <div class="border-t pt-4">
                                    <div class="flex justify-between text-xl font-bold">
                                        <span>Total:</span>
                                        <span class="text-syos-primary">Rs.
                                            <fmt:formatNumber
                                                value="${bill.totalAmount != null ? bill.totalAmount.amount : 0}"
                                                pattern="#,##0.00" />
                                        </span>
                                    </div>
                                </div>
                                <c:if test="${bill.transactionType.name() eq 'CASH'}">
                                    <div class="border-t pt-4 space-y-2">
                                        <div class="flex justify-between">
                                            <span class="text-gray-500">Cash Tendered:</span>
                                            <span>Rs.
                                                <fmt:formatNumber
                                                    value="${bill.tenderedAmount != null ? bill.tenderedAmount.amount : 0}"
                                                    pattern="#,##0.00" />
                                            </span>
                                        </div>
                                        <div class="flex justify-between font-semibold">
                                            <span class="text-gray-500">Change:</span>
                                            <span class="text-green-600">Rs.
                                                <fmt:formatNumber
                                                    value="${bill.changeAmount != null ? bill.changeAmount.amount : 0}"
                                                    pattern="#,##0.00" />
                                            </span>
                                        </div>
                                    </div>
                                </c:if>
                            </div>
                        </div>

                        <!-- Quick Stats -->
                        <div class="card">
                            <h2 class="card-header">Quick Stats</h2>
                            <div class="space-y-4">
                                <div class="stat-card border-syos-primary">
                                    <div class="text-center">
                                        <p class="text-sm text-gray-500">Total Items</p>
                                        <p class="text-3xl font-bold text-syos-primary">${items.size()}</p>
                                    </div>
                                </div>
                                <div class="stat-card border-syos-success">
                                    <div class="text-center">
                                        <p class="text-sm text-gray-500">Total Quantity</p>
                                        <p class="text-3xl font-bold text-syos-success">
                                            <c:set var="totalQty" value="0" />
                                            <c:forEach var="item" items="${items}">
                                                <c:set var="totalQty" value="${totalQty + item.quantity}" />
                                            </c:forEach>
                                            ${totalQty}
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Items Table -->
                    <div class="card mt-6">
                        <h2 class="card-header">Items Purchased</h2>
                        <c:choose>
                            <c:when test="${not empty items}">
                                <div class="table-container">
                                    <table class="data-table">
                                        <thead>
                                            <tr>
                                                <th class="table-header">#</th>
                                                <th class="table-header">Product Code</th>
                                                <th class="table-header">Product Name</th>
                                                <th class="table-header text-center">Quantity</th>
                                                <th class="table-header text-right">Unit Price</th>
                                                <th class="table-header text-right">Line Total</th>
                                            </tr>
                                        </thead>
                                        <tbody class="bg-white divide-y divide-gray-200">
                                            <c:forEach var="item" items="${items}" varStatus="status">
                                                <tr class="table-row">
                                                    <td class="table-cell text-gray-500">${status.index + 1}</td>
                                                    <td class="table-cell">
                                                        <span
                                                            class="font-mono text-sm bg-gray-100 px-2 py-1 rounded">${item.productCodeString}</span>
                                                    </td>
                                                    <td class="table-cell font-medium">${item.productName}</td>
                                                    <td class="table-cell text-center">${item.quantity}</td>
                                                    <td class="table-cell text-right">
                                                        Rs.
                                                        <fmt:formatNumber
                                                            value="${item.unitPrice != null ? item.unitPrice.amount : 0}"
                                                            pattern="#,##0.00" />
                                                    </td>
                                                    <td class="table-cell text-right font-semibold">
                                                        Rs.
                                                        <fmt:formatNumber
                                                            value="${item.lineTotal != null ? item.lineTotal.amount : 0}"
                                                            pattern="#,##0.00" />
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                        <tfoot class="bg-gray-50">
                                            <tr>
                                                <td colspan="5" class="table-cell text-right font-bold">Total:</td>
                                                <td class="table-cell text-right font-bold text-lg text-syos-primary">
                                                    Rs.
                                                    <fmt:formatNumber
                                                        value="${bill.totalAmount != null ? bill.totalAmount.amount : 0}"
                                                        pattern="#,##0.00" />
                                                </td>
                                            </tr>
                                        </tfoot>
                                    </table>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <p class="text-center text-gray-500 py-8">No items in this bill</p>
                            </c:otherwise>
                        </c:choose>
                    </div>

                </t:layout>