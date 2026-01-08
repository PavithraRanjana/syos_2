<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout pageTitle="Store Stock" activeNav="store-stock">

    <!-- Page Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-6">
        <div>
            <h1 class="text-2xl font-bold text-gray-900">Store Stock Overview</h1>
            <p class="mt-1 text-sm text-gray-500">Manage physical and online store inventory</p>
        </div>
        <div class="mt-4 sm:mt-0">
            <a href="${pageContext.request.contextPath}/store-stock/restock" class="btn-primary">
                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
                </svg>
                Restock
            </a>
        </div>
    </div>

    <!-- Alert Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
        <div class="stat-card border-syos-warning">
            <div class="flex items-center justify-between">
                <div>
                    <p class="text-sm font-medium text-gray-500">Physical Store Low Stock</p>
                    <p class="text-3xl font-bold text-gray-900">${physicalLowStockCount}</p>
                </div>
                <a href="${pageContext.request.contextPath}/store-stock/low-stock" class="btn-warning btn-sm">View</a>
            </div>
        </div>
        <div class="stat-card border-syos-danger">
            <div class="flex items-center justify-between">
                <div>
                    <p class="text-sm font-medium text-gray-500">Online Store Low Stock</p>
                    <p class="text-3xl font-bold text-gray-900">${onlineLowStockCount}</p>
                </div>
                <a href="${pageContext.request.contextPath}/store-stock/low-stock" class="btn-danger btn-sm">View</a>
            </div>
        </div>
    </div>

    <!-- Store Tabs -->
    <div class="card">
        <div class="border-b border-gray-200 mb-4">
            <nav class="-mb-px flex space-x-8">
                <button onclick="showTab('physical')" id="physicalTab"
                        class="border-b-2 border-syos-primary py-2 px-1 text-sm font-medium text-syos-primary">
                    Physical Store
                </button>
                <button onclick="showTab('online')" id="onlineTab"
                        class="border-b-2 border-transparent py-2 px-1 text-sm font-medium text-gray-500 hover:text-gray-700">
                    Online Store
                </button>
            </nav>
        </div>

        <!-- Physical Store Stock -->
        <div id="physicalContent">
            <c:choose>
                <c:when test="${not empty physicalSummary}">
                    <div class="table-container">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th class="table-header">Product Code</th>
                                    <th class="table-header">Product Name</th>
                                    <th class="table-header text-right">Total Qty</th>
                                    <th class="table-header text-right">Batches</th>
                                    <th class="table-header text-right">Actions</th>
                                </tr>
                            </thead>
                            <tbody class="bg-white divide-y divide-gray-200">
                                <c:forEach var="item" items="${physicalSummary}">
                                    <tr class="table-row">
                                        <td class="table-cell font-mono">${item.productCode()}</td>
                                        <td class="table-cell">${item.productName()}</td>
                                        <td class="table-cell text-right">
                                            <span class="${item.totalQuantity() <= 10 ? 'text-red-600 font-bold' : ''}">${item.totalQuantity()}</span>
                                        </td>
                                        <td class="table-cell text-right">${item.batchCount()}</td>
                                        <td class="table-cell text-right">
                                            <a href="${pageContext.request.contextPath}/store-stock/physical?product=${item.productCode()}"
                                               class="text-syos-primary hover:underline">Details</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <p class="text-center text-gray-500 py-8">No stock in physical store</p>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- Online Store Stock -->
        <div id="onlineContent" class="hidden">
            <c:choose>
                <c:when test="${not empty onlineSummary}">
                    <div class="table-container">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th class="table-header">Product Code</th>
                                    <th class="table-header">Product Name</th>
                                    <th class="table-header text-right">Total Qty</th>
                                    <th class="table-header text-right">Batches</th>
                                    <th class="table-header text-right">Actions</th>
                                </tr>
                            </thead>
                            <tbody class="bg-white divide-y divide-gray-200">
                                <c:forEach var="item" items="${onlineSummary}">
                                    <tr class="table-row">
                                        <td class="table-cell font-mono">${item.productCode()}</td>
                                        <td class="table-cell">${item.productName()}</td>
                                        <td class="table-cell text-right">
                                            <span class="${item.totalQuantity() <= 10 ? 'text-red-600 font-bold' : ''}">${item.totalQuantity()}</span>
                                        </td>
                                        <td class="table-cell text-right">${item.batchCount()}</td>
                                        <td class="table-cell text-right">
                                            <a href="${pageContext.request.contextPath}/store-stock/online?product=${item.productCode()}"
                                               class="text-syos-primary hover:underline">Details</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <p class="text-center text-gray-500 py-8">No stock in online store</p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <script>
        function showTab(tab) {
            document.getElementById('physicalContent').classList.toggle('hidden', tab !== 'physical');
            document.getElementById('onlineContent').classList.toggle('hidden', tab !== 'online');
            document.getElementById('physicalTab').classList.toggle('border-syos-primary', tab === 'physical');
            document.getElementById('physicalTab').classList.toggle('text-syos-primary', tab === 'physical');
            document.getElementById('physicalTab').classList.toggle('border-transparent', tab !== 'physical');
            document.getElementById('physicalTab').classList.toggle('text-gray-500', tab !== 'physical');
            document.getElementById('onlineTab').classList.toggle('border-syos-primary', tab === 'online');
            document.getElementById('onlineTab').classList.toggle('text-syos-primary', tab === 'online');
            document.getElementById('onlineTab').classList.toggle('border-transparent', tab !== 'online');
            document.getElementById('onlineTab').classList.toggle('text-gray-500', tab !== 'online');
        }
    </script>

</t:layout>
