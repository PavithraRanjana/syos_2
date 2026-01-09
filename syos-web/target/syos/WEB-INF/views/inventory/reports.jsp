<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout pageTitle="Inventory Reports" activeNav="inventory">

    <!-- Page Header -->
    <div class="mb-6">
        <div class="flex items-center justify-between">
            <div>
                <h1 class="text-2xl font-bold text-gray-900">Inventory Reports</h1>
                <p class="mt-1 text-sm text-gray-500">Stock management and reorder reports</p>
            </div>
            <a href="${pageContext.request.contextPath}/inventory" class="btn btn-secondary">
                Back to Inventory
            </a>
        </div>
    </div>

    <!-- Summary Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div class="stat-card border-red-500">
            <p class="text-sm font-medium text-gray-500">Items to Reshelve</p>
            <p class="text-2xl font-bold text-gray-900">${totalReshelveCount}</p>
            <p class="text-xs text-gray-500 mt-1">
                Physical: ${physicalReshelveCount} | Online: ${onlineReshelveCount}
            </p>
        </div>
        <div class="stat-card border-orange-500">
            <p class="text-sm font-medium text-gray-500">Items to Reorder</p>
            <p class="text-2xl font-bold text-gray-900">${reorderCount}</p>
            <p class="text-xs text-gray-500 mt-1">${totalReorderQty} units needed</p>
        </div>
        <div class="stat-card border-green-500">
            <p class="text-sm font-medium text-gray-500">Physical Reshelve Qty</p>
            <p class="text-2xl font-bold text-gray-900">${totalPhysicalReshelveQty}</p>
            <p class="text-xs text-gray-500 mt-1">units below minimum</p>
        </div>
        <div class="stat-card border-blue-500">
            <p class="text-sm font-medium text-gray-500">Online Reshelve Qty</p>
            <p class="text-2xl font-bold text-gray-900">${totalOnlineReshelveQty}</p>
            <p class="text-xs text-gray-500 mt-1">units below minimum</p>
        </div>
    </div>

    <!-- Report Links -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <!-- Reshelve Report -->
        <a href="${pageContext.request.contextPath}/reports/reshelve" class="card hover:shadow-lg transition-shadow">
            <div class="flex items-center">
                <div class="p-3 bg-red-500 rounded-lg">
                    <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"/>
                    </svg>
                </div>
                <div class="ml-4 flex-1">
                    <h3 class="text-lg font-semibold text-gray-900">End of Day Reshelve Report</h3>
                    <p class="text-sm text-gray-500">Items below minimum stock in stores</p>
                    <div class="mt-2 flex items-center text-red-600 font-medium">
                        <span class="text-2xl">${totalReshelveCount}</span>
                        <span class="ml-2 text-sm">items need attention</span>
                    </div>
                </div>
                <svg class="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
                </svg>
            </div>
        </a>

        <!-- Reorder Level Report -->
        <a href="${pageContext.request.contextPath}/reports/reorder-level" class="card hover:shadow-lg transition-shadow">
            <div class="flex items-center">
                <div class="p-3 bg-orange-500 rounded-lg">
                    <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
                    </svg>
                </div>
                <div class="ml-4 flex-1">
                    <h3 class="text-lg font-semibold text-gray-900">Reorder Level Report</h3>
                    <p class="text-sm text-gray-500">Main inventory below threshold (70 units)</p>
                    <div class="mt-2 flex items-center text-orange-600 font-medium">
                        <span class="text-2xl">${reorderCount}</span>
                        <span class="ml-2 text-sm">products need reorder</span>
                    </div>
                </div>
                <svg class="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
                </svg>
            </div>
        </a>

        <!-- Batch Stock Report -->
        <a href="${pageContext.request.contextPath}/reports/batch-stock" class="card hover:shadow-lg transition-shadow">
            <div class="flex items-center">
                <div class="p-3 bg-indigo-500 rounded-lg">
                    <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"/>
                    </svg>
                </div>
                <div class="ml-4 flex-1">
                    <h3 class="text-lg font-semibold text-gray-900">Batch-wise Stock Report</h3>
                    <p class="text-sm text-gray-500">Complete batch details from main inventory</p>
                    <div class="mt-2 flex items-center text-indigo-600 font-medium">
                        <span class="text-2xl">${batchCount}</span>
                        <span class="ml-2 text-sm">active batches</span>
                    </div>
                </div>
                <svg class="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
                </svg>
            </div>
        </a>
    </div>

    <!-- Quick Info -->
    <div class="card mt-6">
        <h2 class="card-header">Quick Reference</h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
            <div class="p-4 bg-red-50 rounded-lg">
                <h4 class="font-medium text-red-800">Reshelve Report</h4>
                <p class="text-red-600 mt-1">Products where store stock is below the configured minimum. Use this at end of day to know what needs restocking.</p>
            </div>
            <div class="p-4 bg-orange-50 rounded-lg">
                <h4 class="font-medium text-orange-800">Reorder Level Report</h4>
                <p class="text-orange-600 mt-1">Products where main inventory total is below 70 units. These need to be purchased from suppliers.</p>
            </div>
            <div class="p-4 bg-indigo-50 rounded-lg">
                <h4 class="font-medium text-indigo-800">Batch Stock Report</h4>
                <p class="text-indigo-600 mt-1">Complete view of all batches with quantities in main inventory, physical store, and online store.</p>
            </div>
        </div>
    </div>

</t:layout>
