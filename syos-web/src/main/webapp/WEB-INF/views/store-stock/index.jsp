<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
            <%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

                <t:layout pageTitle="Store Stock" activeNav="store-stock">

                    <!-- Page Header -->
                    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-6">
                        <div>
                            <h1 class="text-2xl font-bold text-gray-900">Store Stock</h1>
                            <p class="mt-1 text-sm text-gray-500">View and manage stock for physical and online stores
                            </p>
                        </div>
                        <div class="mt-4 sm:mt-0 flex gap-2">
                            <a href="${pageContext.request.contextPath}/store-stock/restock" class="btn-secondary">
                                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                                </svg>
                                Restock
                            </a>
                        </div>
                    </div>

                    <!-- Summary Cards -->
                    <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
                        <div class="stat-card border-blue-500">
                            <div class="flex items-center justify-between">
                                <div>
                                    <p class="text-sm font-medium text-gray-500">Physical Store Products</p>
                                    <p class="text-2xl font-bold text-gray-900">${physicalSummary.size()}</p>
                                </div>
                                <div class="p-3 bg-blue-100 rounded-full">
                                    <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor"
                                        viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                                    </svg>
                                </div>
                            </div>
                        </div>
                        <div class="stat-card border-yellow-500">
                            <div class="flex items-center justify-between">
                                <div>
                                    <p class="text-sm font-medium text-gray-500">Physical Low Stock</p>
                                    <p class="text-2xl font-bold text-yellow-600">${physicalLowStockCount}</p>
                                </div>
                                <div class="p-3 bg-yellow-100 rounded-full">
                                    <svg class="w-6 h-6 text-yellow-600" fill="none" stroke="currentColor"
                                        viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                                    </svg>
                                </div>
                            </div>
                        </div>
                        <div class="stat-card border-green-500">
                            <div class="flex items-center justify-between">
                                <div>
                                    <p class="text-sm font-medium text-gray-500">Online Store Products</p>
                                    <p class="text-2xl font-bold text-gray-900">${onlineSummary.size()}</p>
                                </div>
                                <div class="p-3 bg-green-100 rounded-full">
                                    <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor"
                                        viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9" />
                                    </svg>
                                </div>
                            </div>
                        </div>
                        <div class="stat-card border-orange-500">
                            <div class="flex items-center justify-between">
                                <div>
                                    <p class="text-sm font-medium text-gray-500">Online Low Stock</p>
                                    <p class="text-2xl font-bold text-orange-600">${onlineLowStockCount}</p>
                                </div>
                                <div class="p-3 bg-orange-100 rounded-full">
                                    <svg class="w-6 h-6 text-orange-600" fill="none" stroke="currentColor"
                                        viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                                    </svg>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Tabs -->
                    <div class="card">
                        <div class="border-b border-gray-200">
                            <nav class="-mb-px flex" aria-label="Tabs">
                                <button type="button" onclick="showTab('physical')" id="tab-physical"
                                    class="tab-button active w-1/2 py-4 px-1 text-center border-b-2 font-medium text-sm border-syos-primary text-syos-primary">
                                    <svg class="w-5 h-5 mx-auto mb-1" fill="none" stroke="currentColor"
                                        viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                                    </svg>
                                    Physical Store Stock
                                </button>
                                <button type="button" onclick="showTab('online')" id="tab-online"
                                    class="tab-button w-1/2 py-4 px-1 text-center border-b-2 font-medium text-sm border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300">
                                    <svg class="w-5 h-5 mx-auto mb-1" fill="none" stroke="currentColor"
                                        viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9" />
                                    </svg>
                                    Online Store Stock
                                </button>
                            </nav>
                        </div>

                        <!-- Physical Store Tab Content -->
                        <div id="panel-physical" class="tab-panel">
                            <c:choose>
                                <c:when test="${not empty physicalSummary}">
                                    <div class="table-container">
                                        <table class="data-table">
                                            <thead>
                                                <tr>
                                                    <th class="table-header">Product Code</th>
                                                    <th class="table-header">Product Name</th>
                                                    <th class="table-header text-right">Batches</th>
                                                    <th class="table-header text-right">Quantity</th>
                                                    <th class="table-header text-right">Status</th>
                                                </tr>
                                            </thead>
                                            <tbody class="bg-white divide-y divide-gray-200">
                                                <c:forEach var="item" items="${physicalSummary}">
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
                                        <svg class="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none"
                                            stroke="currentColor" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                                d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                                        </svg>
                                        <p>No stock available in physical store</p>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <!-- Online Store Tab Content -->
                        <div id="panel-online" class="tab-panel hidden">
                            <c:choose>
                                <c:when test="${not empty onlineSummary}">
                                    <div class="table-container">
                                        <table class="data-table">
                                            <thead>
                                                <tr>
                                                    <th class="table-header">Product Code</th>
                                                    <th class="table-header">Product Name</th>
                                                    <th class="table-header text-right">Batches</th>
                                                    <th class="table-header text-right">Quantity</th>
                                                    <th class="table-header text-right">Status</th>
                                                </tr>
                                            </thead>
                                            <tbody class="bg-white divide-y divide-gray-200">
                                                <c:forEach var="item" items="${onlineSummary}">
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
                                        <svg class="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none"
                                            stroke="currentColor" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                                d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9" />
                                        </svg>
                                        <p>No stock available in online store</p>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <script>
                        function showTab(tabName) {
                            // Hide all panels
                            document.getElementById('panel-physical').classList.add('hidden');
                            document.getElementById('panel-online').classList.add('hidden');

                            // Deactivate all tabs
                            document.getElementById('tab-physical').classList.remove('border-syos-primary', 'text-syos-primary');
                            document.getElementById('tab-physical').classList.add('border-transparent', 'text-gray-500');
                            document.getElementById('tab-online').classList.remove('border-syos-primary', 'text-syos-primary');
                            document.getElementById('tab-online').classList.add('border-transparent', 'text-gray-500');

                            // Show selected panel and activate tab
                            document.getElementById('panel-' + tabName).classList.remove('hidden');
                            document.getElementById('tab-' + tabName).classList.remove('border-transparent', 'text-gray-500');
                            document.getElementById('tab-' + tabName).classList.add('border-syos-primary', 'text-syos-primary');
                        }
                    </script>

                </t:layout>