<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout pageTitle="Customers" activeNav="customers">

    <!-- Page Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-6">
        <div>
            <h1 class="text-2xl font-bold text-gray-900">Customers</h1>
            <p class="mt-1 text-sm text-gray-500">${totalCustomers} registered customers</p>
        </div>
        <div class="mt-4 sm:mt-0">
            <a href="${pageContext.request.contextPath}/customers/add" class="btn-primary">
                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"/>
                </svg>
                Add Customer
            </a>
        </div>
    </div>

    <!-- Statistics Cards -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        <div class="stat-card border-syos-primary">
            <p class="text-sm font-medium text-gray-500">Total Customers</p>
            <p class="text-2xl font-bold text-gray-900">${stats.totalCustomers()}</p>
        </div>
        <div class="stat-card border-syos-success">
            <p class="text-sm font-medium text-gray-500">Active Customers</p>
            <p class="text-2xl font-bold text-gray-900">${stats.activeCustomers()}</p>
        </div>
        <div class="stat-card border-syos-secondary">
            <p class="text-sm font-medium text-gray-500">New This Month</p>
            <p class="text-2xl font-bold text-gray-900">${stats.newCustomersThisMonth()}</p>
        </div>
    </div>

    <!-- Search -->
    <div class="card mb-6">
        <form method="get" action="${pageContext.request.contextPath}/customers" class="flex flex-col sm:flex-row gap-4">
            <div class="flex-grow">
                <input type="text" name="search" value="${search}" placeholder="Search customers by name..."
                       class="input-field">
            </div>
            <button type="submit" class="btn-primary">Search</button>
            <c:if test="${not empty search}">
                <a href="${pageContext.request.contextPath}/customers" class="btn-secondary">Clear</a>
            </c:if>
        </form>
    </div>

    <!-- Customers Table -->
    <div class="card">
        <c:choose>
            <c:when test="${not empty customers}">
                <div class="table-container">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th class="table-header">ID</th>
                                <th class="table-header">Name</th>
                                <th class="table-header">Email</th>
                                <th class="table-header">Phone</th>
                                <th class="table-header text-center">Status</th>
                                <th class="table-header text-right">Actions</th>
                            </tr>
                        </thead>
                        <tbody class="bg-white divide-y divide-gray-200">
                            <c:forEach var="customer" items="${customers}">
                                <tr class="table-row">
                                    <td class="table-cell">#${customer.customerId}</td>
                                    <td class="table-cell font-medium">${customer.customerName}</td>
                                    <td class="table-cell">${customer.email}</td>
                                    <td class="table-cell">${customer.phone}</td>
                                    <td class="table-cell text-center">
                                        <span class="badge ${customer.active ? 'badge-success' : 'badge-danger'}">
                                            ${customer.active ? 'Active' : 'Inactive'}
                                        </span>
                                    </td>
                                    <td class="table-cell text-right">
                                        <div class="flex justify-end space-x-2">
                                            <a href="${pageContext.request.contextPath}/customers/view/${customer.customerId}"
                                               class="text-blue-600 hover:text-blue-800" title="View">
                                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                                                </svg>
                                            </a>
                                            <a href="${pageContext.request.contextPath}/customers/edit/${customer.customerId}"
                                               class="text-yellow-600 hover:text-yellow-800" title="Edit">
                                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                                                </svg>
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>

                <!-- Pagination -->
                <c:if test="${totalPages > 1}">
                    <div class="mt-4 flex items-center justify-between border-t border-gray-200 pt-4">
                        <div class="text-sm text-gray-500">Page ${currentPage + 1} of ${totalPages}</div>
                        <div class="flex space-x-2">
                            <c:if test="${currentPage > 0}">
                                <a href="${pageContext.request.contextPath}/customers?page=${currentPage - 1}&size=${pageSize}"
                                   class="btn-secondary btn-sm">Previous</a>
                            </c:if>
                            <c:if test="${currentPage < totalPages - 1}">
                                <a href="${pageContext.request.contextPath}/customers?page=${currentPage + 1}&size=${pageSize}"
                                   class="btn-secondary btn-sm">Next</a>
                            </c:if>
                        </div>
                    </div>
                </c:if>
            </c:when>
            <c:otherwise>
                <div class="text-center py-12">
                    <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/>
                    </svg>
                    <h3 class="mt-2 text-sm font-medium text-gray-900">No customers found</h3>
                    <div class="mt-6">
                        <a href="${pageContext.request.contextPath}/customers/add" class="btn-primary">Add Customer</a>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

</t:layout>
