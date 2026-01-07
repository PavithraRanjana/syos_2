<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<header class="bg-white shadow-sm border-b border-gray-200">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
            <!-- Logo and Navigation -->
            <div class="flex items-center">
                <a href="${ctx}/" class="flex-shrink-0">
                    <span class="text-xl font-bold text-blue-700">SYOS</span>
                </a>
                <nav class="hidden md:ml-10 md:flex md:space-x-8">
                    <c:if test="${sessionScope.userRole == 'CASHIER' or sessionScope.userRole == 'MANAGER'}">
                        <a href="${ctx}/cashier" class="text-gray-600 hover:text-gray-900 px-3 py-2 text-sm font-medium">
                            Billing
                        </a>
                    </c:if>
                    <c:if test="${sessionScope.userRole == 'INVENTORY' or sessionScope.userRole == 'MANAGER'}">
                        <a href="${ctx}/inventory" class="text-gray-600 hover:text-gray-900 px-3 py-2 text-sm font-medium">
                            Inventory
                        </a>
                    </c:if>
                    <c:if test="${sessionScope.userRole == 'MANAGER'}">
                        <a href="${ctx}/manager" class="text-gray-600 hover:text-gray-900 px-3 py-2 text-sm font-medium">
                            Reports
                        </a>
                    </c:if>
                    <c:if test="${sessionScope.userRole == 'ONLINE_CUSTOMER'}">
                        <a href="${ctx}/shop" class="text-gray-600 hover:text-gray-900 px-3 py-2 text-sm font-medium">
                            Shop
                        </a>
                        <a href="${ctx}/cart" class="text-gray-600 hover:text-gray-900 px-3 py-2 text-sm font-medium">
                            Cart
                        </a>
                    </c:if>
                </nav>
            </div>

            <!-- User Menu -->
            <div class="flex items-center">
                <c:choose>
                    <c:when test="${not empty sessionScope.userId}">
                        <span class="text-sm text-gray-600 mr-4">
                            Welcome, ${sessionScope.userName}
                        </span>
                        <a href="${ctx}/logout"
                           class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-red-600 hover:bg-red-700">
                            Logout
                        </a>
                    </c:when>
                    <c:otherwise>
                        <a href="${ctx}/login"
                           class="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 mr-2">
                            Login
                        </a>
                        <a href="${ctx}/register"
                           class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700">
                            Register
                        </a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</header>
