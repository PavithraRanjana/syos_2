<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SYOS - Retail Management System</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        'syos-primary': '#1e40af',
                        'syos-secondary': '#7c3aed',
                    }
                }
            }
        }
    </script>
</head>
<body class="bg-gradient-to-br from-blue-50 to-indigo-100 min-h-screen">
    <!-- Navigation -->
    <nav class="bg-white shadow-sm">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex justify-between h-16">
                <div class="flex items-center">
                    <span class="text-2xl font-bold text-syos-primary">SYOS</span>
                    <span class="ml-2 text-gray-500">Retail Management</span>
                </div>
                <div class="flex items-center space-x-4">
                    <a href="${ctx}/login" class="text-gray-600 hover:text-gray-900 px-3 py-2 text-sm font-medium">
                        Login
                    </a>
                    <a href="${ctx}/register"
                       class="bg-syos-primary text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-blue-700">
                        Register
                    </a>
                </div>
            </div>
        </div>
    </nav>

    <!-- Hero Section -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
        <div class="text-center">
            <h1 class="text-4xl md:text-6xl font-bold text-gray-900 mb-6">
                Synergize Your
                <span class="text-syos-primary">Operations</span>
                Store
            </h1>
            <p class="text-xl text-gray-600 max-w-2xl mx-auto mb-10">
                Complete retail management solution with inventory tracking, billing,
                and comprehensive reporting for your grocery store.
            </p>
            <div class="flex flex-col sm:flex-row gap-4 justify-center">
                <a href="${ctx}/shop"
                   class="inline-flex items-center justify-center px-8 py-3 border border-transparent text-base font-medium rounded-md text-white bg-syos-primary hover:bg-blue-700">
                    Start Shopping
                </a>
                <a href="${ctx}/login"
                   class="inline-flex items-center justify-center px-8 py-3 border border-gray-300 text-base font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50">
                    Staff Login
                </a>
            </div>
        </div>
    </div>

    <!-- Features Section -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div class="text-center mb-12">
            <h2 class="text-3xl font-bold text-gray-900">Key Features</h2>
            <p class="text-gray-600 mt-2">Everything you need to manage your retail operations</p>
        </div>

        <div class="grid md:grid-cols-3 gap-8">
            <!-- Feature 1 -->
            <div class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition">
                <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mb-4">
                    <svg class="w-6 h-6 text-syos-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                    </svg>
                </div>
                <h3 class="text-lg font-semibold text-gray-900 mb-2">Inventory Management</h3>
                <p class="text-gray-600">
                    Track stock across main warehouse, physical store, and online channels
                    with FIFO batch management and expiry tracking.
                </p>
            </div>

            <!-- Feature 2 -->
            <div class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition">
                <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center mb-4">
                    <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z" />
                    </svg>
                </div>
                <h3 class="text-lg font-semibold text-gray-900 mb-2">Point of Sale</h3>
                <p class="text-gray-600">
                    Fast and efficient billing system for cashiers with product search,
                    cart management, and instant receipt generation.
                </p>
            </div>

            <!-- Feature 3 -->
            <div class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition">
                <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center mb-4">
                    <svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                    </svg>
                </div>
                <h3 class="text-lg font-semibold text-gray-900 mb-2">Reports & Analytics</h3>
                <p class="text-gray-600">
                    Comprehensive reports for sales, inventory status, expiring items,
                    and reorder alerts to help make informed decisions.
                </p>
            </div>
        </div>
    </div>

    <!-- Quick Access Section -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div class="bg-white rounded-xl shadow-lg p-8">
            <h2 class="text-2xl font-bold text-gray-900 mb-6 text-center">Quick Access</h2>
            <div class="grid sm:grid-cols-2 lg:grid-cols-4 gap-4">
                <a href="${ctx}/cashier"
                   class="flex items-center p-4 bg-blue-50 rounded-lg hover:bg-blue-100 transition">
                    <div class="flex-shrink-0 w-10 h-10 bg-blue-500 rounded-lg flex items-center justify-center">
                        <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                  d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2" />
                        </svg>
                    </div>
                    <div class="ml-4">
                        <p class="text-sm font-medium text-gray-900">Cashier Terminal</p>
                        <p class="text-xs text-gray-500">Process sales</p>
                    </div>
                </a>

                <a href="${ctx}/inventory"
                   class="flex items-center p-4 bg-green-50 rounded-lg hover:bg-green-100 transition">
                    <div class="flex-shrink-0 w-10 h-10 bg-green-500 rounded-lg flex items-center justify-center">
                        <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                  d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                        </svg>
                    </div>
                    <div class="ml-4">
                        <p class="text-sm font-medium text-gray-900">Inventory</p>
                        <p class="text-xs text-gray-500">Manage stock</p>
                    </div>
                </a>

                <a href="${ctx}/manager"
                   class="flex items-center p-4 bg-purple-50 rounded-lg hover:bg-purple-100 transition">
                    <div class="flex-shrink-0 w-10 h-10 bg-purple-500 rounded-lg flex items-center justify-center">
                        <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                  d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2z" />
                        </svg>
                    </div>
                    <div class="ml-4">
                        <p class="text-sm font-medium text-gray-900">Reports</p>
                        <p class="text-xs text-gray-500">View analytics</p>
                    </div>
                </a>

                <a href="${ctx}/shop"
                   class="flex items-center p-4 bg-orange-50 rounded-lg hover:bg-orange-100 transition">
                    <div class="flex-shrink-0 w-10 h-10 bg-orange-500 rounded-lg flex items-center justify-center">
                        <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                  d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17" />
                        </svg>
                    </div>
                    <div class="ml-4">
                        <p class="text-sm font-medium text-gray-900">Online Shop</p>
                        <p class="text-xs text-gray-500">Browse products</p>
                    </div>
                </a>
            </div>
        </div>
    </div>

    <!-- Footer -->
    <footer class="bg-white border-t">
        <div class="max-w-7xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
            <div class="flex flex-col md:flex-row justify-between items-center">
                <div class="text-gray-600">
                    <span class="font-bold text-syos-primary">SYOS</span>
                    - Synergize Your Operations Store v2.0.0
                </div>
                <div class="text-gray-400 text-sm mt-2 md:mt-0">
                    Built with Java 25, Tomcat 11, MySQL 9.4, Tailwind CSS
                </div>
            </div>
        </div>
    </footer>
</body>
</html>
