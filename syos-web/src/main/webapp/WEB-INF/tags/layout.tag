<%@ tag description="Main Layout Template" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ attribute name="pageTitle" required="true" %>
<%@ attribute name="activeNav" required="false" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle} - SYOS</title>

    <!-- Tailwind CSS CDN -->
    <script src="https://cdn.tailwindcss.com"></script>

    <!-- Tailwind Configuration -->
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        'syos-primary': '#1e40af',
                        'syos-secondary': '#7c3aed',
                        'syos-success': '#10b981',
                        'syos-warning': '#f59e0b',
                        'syos-danger': '#ef4444',
                    },
                    fontFamily: {
                        sans: ['Inter', 'system-ui', 'sans-serif'],
                    }
                }
            }
        }
    </script>

    <!-- Custom Styles -->
    <style type="text/tailwindcss">
        @layer components {
            .btn-primary {
                @apply bg-syos-primary hover:bg-blue-800 text-white font-semibold py-2 px-4 rounded-lg transition duration-200 inline-flex items-center justify-center;
            }
            .btn-secondary {
                @apply bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 px-4 rounded-lg transition duration-200 inline-flex items-center justify-center;
            }
            .btn-success {
                @apply bg-syos-success hover:bg-green-600 text-white font-semibold py-2 px-4 rounded-lg transition duration-200 inline-flex items-center justify-center;
            }
            .btn-danger {
                @apply bg-syos-danger hover:bg-red-600 text-white font-semibold py-2 px-4 rounded-lg transition duration-200 inline-flex items-center justify-center;
            }
            .btn-warning {
                @apply bg-syos-warning hover:bg-amber-600 text-white font-semibold py-2 px-4 rounded-lg transition duration-200 inline-flex items-center justify-center;
            }
            .btn-sm {
                @apply py-1 px-3 text-sm;
            }
            .card {
                @apply bg-white rounded-lg shadow-md p-6;
            }
            .card-header {
                @apply text-lg font-semibold text-gray-800 mb-4 pb-2 border-b border-gray-200;
            }
            .input-field {
                @apply w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-syos-primary focus:border-transparent outline-none transition;
            }
            .input-label {
                @apply block text-sm font-medium text-gray-700 mb-1;
            }
            .table-container {
                @apply overflow-x-auto rounded-lg border border-gray-200;
            }
            .data-table {
                @apply min-w-full divide-y divide-gray-200;
            }
            .table-header {
                @apply px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider bg-gray-50;
            }
            .table-cell {
                @apply px-6 py-4 whitespace-nowrap text-sm text-gray-900;
            }
            .table-row {
                @apply hover:bg-gray-50 transition-colors;
            }
            .badge {
                @apply inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium;
            }
            .badge-success {
                @apply bg-green-100 text-green-800;
            }
            .badge-warning {
                @apply bg-yellow-100 text-yellow-800;
            }
            .badge-danger {
                @apply bg-red-100 text-red-800;
            }
            .badge-info {
                @apply bg-blue-100 text-blue-800;
            }
            .stat-card {
                @apply bg-white rounded-lg shadow p-6 border-l-4;
            }
            .nav-link {
                @apply text-gray-600 hover:text-syos-primary hover:bg-gray-100 px-3 py-2 rounded-md text-sm font-medium transition-colors;
            }
            .nav-link-active {
                @apply text-syos-primary bg-blue-50 px-3 py-2 rounded-md text-sm font-medium;
            }
        }
    </style>

    <!-- Inter Font -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
</head>
<body class="bg-gray-100 min-h-screen flex flex-col font-sans">
    <!-- Header -->
    <header class="bg-white shadow-sm sticky top-0 z-50">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex justify-between items-center h-16">
                <!-- Logo and Navigation -->
                <div class="flex items-center">
                    <a href="${ctx}/" class="flex-shrink-0 flex items-center">
                        <svg class="h-8 w-8 text-syos-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
                        </svg>
                        <span class="ml-2 text-xl font-bold text-syos-primary">SYOS</span>
                    </a>
                    <nav class="hidden md:ml-10 md:flex md:space-x-2">
                        <c:if test="${sessionScope.userRole == 'ADMIN'}">
                            <a href="${ctx}/dashboard" class="${activeNav == 'dashboard' ? 'nav-link-active' : 'nav-link'}">
                                Dashboard
                            </a>
                        </c:if>
                        <c:if test="${sessionScope.userRole == 'CASHIER' or sessionScope.userRole == 'ADMIN'}">
                            <a href="${ctx}/pos" class="${activeNav == 'pos' ? 'nav-link-active' : 'nav-link'}">
                                POS / Billing
                            </a>
                        </c:if>
                        <c:if test="${sessionScope.userRole == 'ADMIN'}">
                            <a href="${ctx}/products" class="${activeNav == 'products' ? 'nav-link-active' : 'nav-link'}">
                                Products
                            </a>
                        </c:if>
                        <c:if test="${sessionScope.userRole == 'INVENTORY_MANAGER' or sessionScope.userRole == 'ADMIN'}">
                            <a href="${ctx}/inventory" class="${activeNav == 'inventory' ? 'nav-link-active' : 'nav-link'}">
                                Inventory
                            </a>
                            <a href="${ctx}/store-stock" class="${activeNav == 'store-stock' ? 'nav-link-active' : 'nav-link'}">
                                Store Stock
                            </a>
                        </c:if>
                        <c:if test="${sessionScope.userRole == 'ADMIN'}">
                            <a href="${ctx}/customers" class="${activeNav == 'customers' ? 'nav-link-active' : 'nav-link'}">
                                Customers
                            </a>
                        </c:if>
                        <c:if test="${sessionScope.userRole == 'MANAGER' or sessionScope.userRole == 'ADMIN'}">
                            <a href="${ctx}/reports" class="${activeNav == 'reports' ? 'nav-link-active' : 'nav-link'}">
                                Reports
                            </a>
                        </c:if>
                        <c:if test="${sessionScope.userRole == 'ADMIN'}">
                            <a href="${ctx}/admin" class="${activeNav == 'admin' ? 'nav-link-active' : 'nav-link'}">
                                <span class="flex items-center">
                                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/>
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                                    </svg>
                                    Admin
                                </span>
                            </a>
                        </c:if>
                        <c:if test="${sessionScope.userRole == 'CUSTOMER'}">
                            <a href="${ctx}/shop" class="${activeNav == 'shop' ? 'nav-link-active' : 'nav-link'}">
                                Shop
                            </a>
                            <a href="${ctx}/cart" class="${activeNav == 'cart' ? 'nav-link-active' : 'nav-link'}">
                                Cart
                            </a>
                            <a href="${ctx}/orders" class="${activeNav == 'orders' ? 'nav-link-active' : 'nav-link'}">
                                My Orders
                            </a>
                        </c:if>
                    </nav>
                </div>

                <!-- Right side -->
                <div class="flex items-center space-x-4">
                    <c:choose>
                        <c:when test="${not empty sessionScope.userId}">
                            <div class="hidden sm:flex items-center space-x-3">
                                <span class="text-sm text-gray-600">
                                    ${sessionScope.userName}
                                    <span class="ml-1 px-2 py-0.5 text-xs rounded-full
                                        <c:choose>
                                            <c:when test="${sessionScope.userRole == 'ADMIN'}">bg-red-100 text-red-700</c:when>
                                            <c:when test="${sessionScope.userRole == 'MANAGER'}">bg-purple-100 text-purple-700</c:when>
                                            <c:when test="${sessionScope.userRole == 'INVENTORY_MANAGER'}">bg-yellow-100 text-yellow-700</c:when>
                                            <c:when test="${sessionScope.userRole == 'CASHIER'}">bg-green-100 text-green-700</c:when>
                                            <c:otherwise>bg-blue-100 text-blue-700</c:otherwise>
                                        </c:choose>
                                    ">${sessionScope.userRole}</span>
                                </span>
                                <button onclick="logout()" class="btn-danger btn-sm">Logout</button>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="hidden sm:flex items-center space-x-2">
                                <a href="${ctx}/login" class="btn-secondary btn-sm">Login</a>
                                <a href="${ctx}/register" class="btn-primary btn-sm">Register</a>
                            </div>
                        </c:otherwise>
                    </c:choose>
                    <!-- Mobile menu button -->
                    <button type="button" class="md:hidden inline-flex items-center justify-center p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100" onclick="toggleMobileMenu()">
                        <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
                        </svg>
                    </button>
                </div>
            </div>
        </div>

        <!-- Mobile menu -->
        <div id="mobileMenu" class="hidden md:hidden bg-white border-t">
            <div class="px-2 pt-2 pb-3 space-y-1">
                <c:if test="${not empty sessionScope.userId}">
                    <div class="px-3 py-2 text-sm text-gray-500 border-b mb-2">
                        Logged in as: ${sessionScope.userName} (${sessionScope.userRole})
                    </div>
                </c:if>
                <c:if test="${sessionScope.userRole == 'ADMIN'}">
                    <a href="${ctx}/dashboard" class="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:bg-gray-100">Dashboard</a>
                    <a href="${ctx}/products" class="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:bg-gray-100">Products</a>
                    <a href="${ctx}/customers" class="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:bg-gray-100">Customers</a>
                    <a href="${ctx}/admin" class="block px-3 py-2 rounded-md text-base font-medium text-red-700 hover:bg-red-50">Admin</a>
                </c:if>
                <c:if test="${sessionScope.userRole == 'CASHIER' or sessionScope.userRole == 'ADMIN'}">
                    <a href="${ctx}/pos" class="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:bg-gray-100">POS / Billing</a>
                </c:if>
                <c:if test="${sessionScope.userRole == 'INVENTORY_MANAGER' or sessionScope.userRole == 'ADMIN'}">
                    <a href="${ctx}/inventory" class="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:bg-gray-100">Inventory</a>
                    <a href="${ctx}/store-stock" class="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:bg-gray-100">Store Stock</a>
                </c:if>
                <c:if test="${sessionScope.userRole == 'MANAGER' or sessionScope.userRole == 'ADMIN'}">
                    <a href="${ctx}/reports" class="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:bg-gray-100">Reports</a>
                </c:if>
                <c:if test="${sessionScope.userRole == 'CUSTOMER'}">
                    <a href="${ctx}/shop" class="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:bg-gray-100">Shop</a>
                    <a href="${ctx}/cart" class="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:bg-gray-100">Cart</a>
                    <a href="${ctx}/orders" class="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:bg-gray-100">My Orders</a>
                </c:if>
                <c:choose>
                    <c:when test="${not empty sessionScope.userId}">
                        <div class="border-t mt-2 pt-2">
                            <button onclick="logout()" class="block w-full text-left px-3 py-2 rounded-md text-base font-medium text-red-600 hover:bg-red-50">Logout</button>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="border-t mt-2 pt-2">
                            <a href="${ctx}/login" class="block px-3 py-2 rounded-md text-base font-medium text-blue-600 hover:bg-blue-50">Login</a>
                            <a href="${ctx}/register" class="block px-3 py-2 rounded-md text-base font-medium text-blue-600 hover:bg-blue-50">Register</a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </header>

    <!-- Flash Messages -->
    <c:if test="${not empty successMessage}">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-4">
            <div class="bg-green-50 border border-green-200 rounded-lg p-4 flex items-center" role="alert">
                <svg class="h-5 w-5 text-green-500 mr-3" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                </svg>
                <span class="text-green-800">${successMessage}</span>
            </div>
        </div>
    </c:if>

    <c:if test="${not empty errorMessage}">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-4">
            <div class="bg-red-50 border border-red-200 rounded-lg p-4 flex items-center" role="alert">
                <svg class="h-5 w-5 text-red-500 mr-3" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
                </svg>
                <span class="text-red-800">${errorMessage}</span>
            </div>
        </div>
    </c:if>

    <!-- Main Content -->
    <main class="flex-grow py-6">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <jsp:doBody />
        </div>
    </main>

    <!-- Footer -->
    <footer class="bg-white border-t border-gray-200 mt-auto">
        <div class="max-w-7xl mx-auto px-4 py-4 sm:px-6 lg:px-8">
            <div class="flex flex-col md:flex-row justify-between items-center text-sm text-gray-500">
                <div>SYOS Retail Management System v2.0.0</div>
                <div class="mt-2 md:mt-0">Java 21 | Tomcat 11 | MySQL 9.4</div>
            </div>
        </div>
    </footer>

    <!-- Common Scripts -->
    <script>
        const ctx = '${ctx}';
        const API_BASE = ctx + '/api';

        function toggleMobileMenu() {
            const menu = document.getElementById('mobileMenu');
            menu.classList.toggle('hidden');
        }

        async function apiFetch(url, options = {}) {
            const defaultOptions = {
                headers: {
                    'Content-Type': 'application/json',
                }
            };
            const response = await fetch(API_BASE + url, { ...defaultOptions, ...options });
            const data = await response.json();
            if (!response.ok) {
                throw new Error(data.error || 'An error occurred');
            }
            return data;
        }

        function formatCurrency(amount) {
            return 'Rs. ' + parseFloat(amount || 0).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2});
        }

        function formatDate(dateStr) {
            if (!dateStr) return '-';
            return new Date(dateStr).toLocaleDateString('en-US', {year: 'numeric', month: 'short', day: 'numeric'});
        }

        function showNotification(message, type = 'info') {
            const colors = {
                success: 'bg-green-500',
                error: 'bg-red-500',
                warning: 'bg-yellow-500',
                info: 'bg-blue-500'
            };
            const notification = document.createElement('div');
            notification.className = `fixed top-4 right-4 \${colors[type]} text-white px-6 py-3 rounded-lg shadow-lg z-50 transition-opacity duration-300`;
            notification.textContent = message;
            document.body.appendChild(notification);
            setTimeout(() => {
                notification.style.opacity = '0';
                setTimeout(() => notification.remove(), 300);
            }, 3000);
        }

        async function logout() {
            try {
                await apiFetch('/auth/logout', { method: 'POST' });
                window.location.href = ctx + '/login';
            } catch (error) {
                // Even if API fails, redirect to login
                window.location.href = ctx + '/login';
            }
        }
    </script>
</body>
</html>
