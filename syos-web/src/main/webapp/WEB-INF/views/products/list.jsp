<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout pageTitle="Products" activeNav="products">

    <!-- Page Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-6">
        <div>
            <h1 class="text-2xl font-bold text-gray-900">Products</h1>
            <p class="mt-1 text-sm text-gray-500">${totalProducts} total products</p>
        </div>
        <div class="mt-4 sm:mt-0">
            <a href="${pageContext.request.contextPath}/products/add" class="btn-primary">
                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"/>
                </svg>
                Add Product
            </a>
        </div>
    </div>

    <!-- Search and Filters -->
    <div class="card mb-6">
        <form method="get" action="${pageContext.request.contextPath}/products" class="flex flex-col sm:flex-row gap-4">
            <div class="flex-grow">
                <input type="text" name="search" value="${search}" placeholder="Search products by name..."
                       class="input-field">
            </div>
            <button type="submit" class="btn-primary">
                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
                </svg>
                Search
            </button>
            <c:if test="${not empty search}">
                <a href="${pageContext.request.contextPath}/products" class="btn-secondary">Clear</a>
            </c:if>
        </form>
    </div>

    <!-- Products Table -->
    <div class="card">
        <c:choose>
            <c:when test="${not empty products}">
                <div class="table-container">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th class="table-header">Code</th>
                                <th class="table-header">Name</th>
                                <th class="table-header text-right">Unit Price</th>
                                <th class="table-header text-center">Status</th>
                                <th class="table-header text-right">Actions</th>
                            </tr>
                        </thead>
                        <tbody class="bg-white divide-y divide-gray-200">
                            <c:forEach var="product" items="${products}">
                                <tr class="table-row">
                                    <td class="table-cell">
                                        <span class="font-mono text-sm bg-gray-100 px-2 py-1 rounded">${product.productCodeString}</span>
                                    </td>
                                    <td class="table-cell">
                                        <div class="font-medium text-gray-900">${product.productName}</div>
                                        <c:if test="${not empty product.description}">
                                            <div class="text-xs text-gray-500 truncate max-w-xs">${product.description}</div>
                                        </c:if>
                                    </td>
                                    <td class="table-cell text-right font-medium">
                                        Rs. <fmt:formatNumber value="${product.unitPrice.amount}" pattern="#,##0.00"/>
                                    </td>
                                    <td class="table-cell text-center">
                                        <c:choose>
                                            <c:when test="${product.active}">
                                                <span class="badge badge-success">Active</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-danger">Inactive</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="table-cell text-right">
                                        <div class="flex justify-end space-x-2">
                                            <a href="${pageContext.request.contextPath}/products/view/${product.productCodeString}"
                                               class="text-blue-600 hover:text-blue-800" title="View">
                                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                                                </svg>
                                            </a>
                                            <a href="${pageContext.request.contextPath}/products/edit/${product.productCodeString}"
                                               class="text-yellow-600 hover:text-yellow-800" title="Edit">
                                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                                                </svg>
                                            </a>
                                            <button onclick="toggleProductStatus('${product.productCodeString}', ${product.active})"
                                                    class="${product.active ? 'text-red-600 hover:text-red-800' : 'text-green-600 hover:text-green-800'}"
                                                    title="${product.active ? 'Deactivate' : 'Activate'}">
                                                <c:choose>
                                                    <c:when test="${product.active}">
                                                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"/>
                                                        </svg>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                                                        </svg>
                                                    </c:otherwise>
                                                </c:choose>
                                            </button>
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
                        <div class="text-sm text-gray-500">
                            Page ${currentPage + 1} of ${totalPages}
                        </div>
                        <div class="flex space-x-2">
                            <c:if test="${currentPage > 0}">
                                <a href="${pageContext.request.contextPath}/products?page=${currentPage - 1}&size=${pageSize}${not empty search ? '&search='.concat(search) : ''}"
                                   class="btn-secondary btn-sm">Previous</a>
                            </c:if>
                            <c:if test="${currentPage < totalPages - 1}">
                                <a href="${pageContext.request.contextPath}/products?page=${currentPage + 1}&size=${pageSize}${not empty search ? '&search='.concat(search) : ''}"
                                   class="btn-secondary btn-sm">Next</a>
                            </c:if>
                        </div>
                    </div>
                </c:if>
            </c:when>
            <c:otherwise>
                <div class="text-center py-12">
                    <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
                    </svg>
                    <h3 class="mt-2 text-sm font-medium text-gray-900">No products found</h3>
                    <p class="mt-1 text-sm text-gray-500">
                        <c:choose>
                            <c:when test="${not empty search}">
                                No products match your search criteria.
                            </c:when>
                            <c:otherwise>
                                Get started by adding your first product.
                            </c:otherwise>
                        </c:choose>
                    </p>
                    <div class="mt-6">
                        <a href="${pageContext.request.contextPath}/products/add" class="btn-primary">
                            <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"/>
                            </svg>
                            Add Product
                        </a>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <script>
        async function toggleProductStatus(productCode, isActive) {
            const action = isActive ? 'deactivate' : 'activate';
            if (!confirm(`Are you sure you want to ${action} this product?`)) {
                return;
            }

            try {
                await apiFetch(`/products/${productCode}/${action}`, { method: 'PUT' });
                showNotification(`Product ${action}d successfully`, 'success');
                setTimeout(() => location.reload(), 1000);
            } catch (error) {
                showNotification(error.message, 'error');
            }
        }
    </script>

</t:layout>
