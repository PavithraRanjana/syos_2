<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
            <%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

                <t:layout pageTitle="Bill Report" activeNav="reports">
                    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">

                        <!-- Header & Filters -->
                        <div class="md:flex md:items-center md:justify-between mb-8">
                            <div class="flex-1 min-w-0">
                                <h2 class="text-2xl font-bold leading-7 text-gray-900 sm:text-3xl sm:truncate">
                                    Daily Bill Report
                                </h2>
                            </div>
                            <div class="mt-4 flex md:mt-0 md:ml-4">
                                <form action="${pageContext.request.contextPath}/reports/bills" method="get"
                                    class="flex items-center space-x-4">
                                    <!-- Store Type Tabs -->
                                    <div class="flex rounded-md shadow-sm">
                                        <input type="hidden" name="storeType" id="storeTypeInput"
                                            value="${selectedStoreType}">
                                        <button type="button" onclick="setStoreType('PHYSICAL')"
                                            class="relative inline-flex items-center px-4 py-2 rounded-l-md border ${selectedStoreType == 'PHYSICAL' ? 'bg-green-600 text-white border-green-600' : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'} text-sm font-medium focus:z-10 focus:outline-none focus:ring-1 focus:ring-green-500 focus:border-green-500">
                                            Physical Store
                                        </button>
                                        <button type="button" onclick="setStoreType('ONLINE')"
                                            class="relative -ml-px inline-flex items-center px-4 py-2 rounded-r-md border ${selectedStoreType == 'ONLINE' ? 'bg-blue-600 text-white border-blue-600' : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'} text-sm font-medium focus:z-10 focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500">
                                            Online Store
                                        </button>
                                    </div>

                                    <!-- Date Picker -->
                                    <div>
                                        <input type="date" name="date" value="${selectedDate}"
                                            onchange="this.form.submit()"
                                            class="shadow-sm focus:ring-syos-primary focus:border-syos-primary block w-full sm:text-sm border-gray-300 rounded-md">
                                    </div>
                                </form>
                            </div>
                        </div>

                        <!-- Summary Cards -->
                        <div class="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4 mb-8">
                            <div class="bg-white overflow-hidden shadow rounded-lg">
                                <div class="px-4 py-5 sm:p-6">
                                    <dt class="text-sm font-medium text-gray-500 truncate">Total Bills</dt>
                                    <dd class="mt-1 text-3xl font-semibold text-gray-900">${billReport.totalBills()}
                                    </dd>
                                </div>
                            </div>
                            <div class="bg-white overflow-hidden shadow rounded-lg">
                                <div class="px-4 py-5 sm:p-6">
                                    <dt class="text-sm font-medium text-gray-500 truncate">Total Revenue</dt>
                                    <dd class="mt-1 text-3xl font-semibold text-gray-900">
                                        <fmt:formatNumber value="${billReport.totalRevenue()}" type="currency"
                                            currencySymbol="$" />
                                    </dd>
                                </div>
                            </div>
                        </div>

                        <!-- Bill List -->
                        <div class="space-y-6">
                            <c:forEach var="bill" items="${billReport.bills()}">
                                <div
                                    class="bg-white shadow overflow-hidden sm:rounded-lg border-l-4 ${selectedStoreType == 'PHYSICAL' ? 'border-green-500' : 'border-blue-500'}">
                                    <div
                                        class="px-4 py-5 border-b border-gray-200 sm:px-6 bg-gray-50 flex justify-between items-center">
                                        <div>
                                            <h3 class="text-lg leading-6 font-medium text-gray-900">
                                                Bill #${bill.serialNumberString}
                                            </h3>
                                            <p class="mt-1 max-w-2xl text-sm text-gray-500">
                                                <span class="font-medium">Date:</span> ${bill.billDateFormatted} |
                                                <span class="font-medium">Time:</span> ${bill.billTime} |
                                                <c:if test="${not empty bill.customerName}">
                                                    <span class="font-medium">Customer:</span> ${bill.customerName} |
                                                </c:if>
                                                <span class="font-medium">Items:</span> ${bill.itemCount}
                                            </p>
                                        </div>
                                        <div class="text-right">
                                            <p class="text-xl font-bold text-gray-900">
                                                <fmt:formatNumber value="${bill.totalAmount.amount}" type="currency"
                                                    currencySymbol="$" />
                                            </p>
                                            <span
                                                class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${bill.transactionType == 'CASH' ? 'bg-green-100 text-green-800' : 'bg-blue-100 text-blue-800'}">
                                                ${bill.transactionType}
                                            </span>
                                        </div>
                                    </div>

                                    <!-- Line Items Table -->
                                    <div class="px-4 py-5 sm:p-6">
                                        <div class="flex flex-col">
                                            <div class="-my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
                                                <div class="py-2 align-middle inline-block min-w-full sm:px-6 lg:px-8">
                                                    <div class="overflow-hidden border-gray-200">
                                                        <table class="min-w-full divide-y divide-gray-200">
                                                            <thead class="bg-gray-50">
                                                                <tr>
                                                                    <th scope="col"
                                                                        class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                                        Product Code</th>
                                                                    <th scope="col"
                                                                        class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                                        Product Name</th>
                                                                    <th scope="col"
                                                                        class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                                        Quantity</th>
                                                                    <th scope="col"
                                                                        class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                                        Unit Price</th>
                                                                    <th scope="col"
                                                                        class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                                        Total</th>
                                                                </tr>
                                                            </thead>
                                                            <tbody class="bg-white divide-y divide-gray-200">
                                                                <c:forEach var="item" items="${bill.items}">
                                                                    <tr>
                                                                        <td
                                                                            class="px-6 py-2 whitespace-nowrap text-sm font-medium text-gray-900">
                                                                            ${item.productCode.code}
                                                                        </td>
                                                                        <td
                                                                            class="px-6 py-2 whitespace-nowrap text-sm text-gray-500">
                                                                            ${item.productName}
                                                                        </td>
                                                                        <td
                                                                            class="px-6 py-2 whitespace-nowrap text-sm text-gray-500 text-right">
                                                                            ${item.quantity}
                                                                        </td>
                                                                        <td
                                                                            class="px-6 py-2 whitespace-nowrap text-sm text-gray-500 text-right">
                                                                            <fmt:formatNumber
                                                                                value="${item.unitPrice.amount}"
                                                                                type="currency" currencySymbol="$" />
                                                                        </td>
                                                                        <td
                                                                            class="px-6 py-2 whitespace-nowrap text-sm text-gray-900 font-medium text-right">
                                                                            <fmt:formatNumber
                                                                                value="${item.totalPrice.amount}"
                                                                                type="currency" currencySymbol="$" />
                                                                        </td>
                                                                    </tr>
                                                                </c:forEach>
                                                            </tbody>
                                                        </table>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>

                            <c:if test="${empty billReport.bills()}">
                                <div class="text-center py-12 bg-white rounded-lg shadow">
                                    <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor"
                                        viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                                    </svg>
                                    <h3 class="mt-2 text-sm font-medium text-gray-900">No bills found</h3>
                                    <p class="mt-1 text-sm text-gray-500">No transactions recorded for this date and
                                        store type.</p>
                                </div>
                            </c:if>
                        </div>
                    </div>

                    <script>
                        function setStoreType(type) {
                            document.getElementById('storeTypeInput').value = type;
                            document.forms[0].submit();
                        }
                    </script>
                </t:layout>