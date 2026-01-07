<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
                @apply bg-syos-primary hover:bg-blue-800 text-white font-semibold py-2 px-4 rounded transition duration-200;
            }
            .btn-secondary {
                @apply bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 px-4 rounded transition duration-200;
            }
            .btn-success {
                @apply bg-syos-success hover:bg-green-600 text-white font-semibold py-2 px-4 rounded transition duration-200;
            }
            .btn-danger {
                @apply bg-syos-danger hover:bg-red-600 text-white font-semibold py-2 px-4 rounded transition duration-200;
            }
            .card {
                @apply bg-white rounded-lg shadow-md p-6;
            }
            .input-field {
                @apply w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-syos-primary focus:border-transparent;
            }
            .table-header {
                @apply px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider;
            }
            .table-cell {
                @apply px-6 py-4 whitespace-nowrap text-sm text-gray-900;
            }
        }
    </style>

    <!-- Additional page-specific styles -->
    <c:if test="${not empty pageStyles}">
        ${pageStyles}
    </c:if>
</head>
<body class="bg-gray-50 min-h-screen flex flex-col">
    <!-- Header -->
    <jsp:include page="header.jsp" />

    <!-- Main Content -->
    <main class="flex-grow">
        <c:if test="${not empty successMessage}">
            <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-4">
                <div class="bg-green-50 border-l-4 border-green-400 p-4">
                    <div class="flex">
                        <div class="flex-shrink-0">
                            <svg class="h-5 w-5 text-green-400" viewBox="0 0 20 20" fill="currentColor">
                                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                            </svg>
                        </div>
                        <div class="ml-3">
                            <p class="text-sm text-green-700">${successMessage}</p>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>

        <c:if test="${not empty errorMessage}">
            <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-4">
                <div class="bg-red-50 border-l-4 border-red-400 p-4">
                    <div class="flex">
                        <div class="flex-shrink-0">
                            <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
                            </svg>
                        </div>
                        <div class="ml-3">
                            <p class="text-sm text-red-700">${errorMessage}</p>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>

        <!-- Page content injected here -->
        <jsp:doBody />
    </main>

    <!-- Footer -->
    <jsp:include page="footer.jsp" />

    <!-- Common Scripts -->
    <script>
        // CSRF token handling
        const csrfToken = document.querySelector('meta[name="csrf-token"]')?.content;

        // Base API URL
        const API_BASE = '${ctx}/api';

        // Common fetch wrapper
        async function apiFetch(url, options = {}) {
            const defaultOptions = {
                headers: {
                    'Content-Type': 'application/json',
                }
            };

            if (csrfToken) {
                defaultOptions.headers['X-CSRF-Token'] = csrfToken;
            }

            const response = await fetch(API_BASE + url, { ...defaultOptions, ...options });
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || 'An error occurred');
            }

            return data;
        }

        // Toast notification helper
        function showToast(message, type = 'info') {
            // Simple alert for now, can be replaced with a proper toast library
            alert(message);
        }

        // Format currency
        function formatCurrency(amount) {
            return 'Rs. ' + parseFloat(amount).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ",");
        }
    </script>

    <!-- Additional page-specific scripts -->
    <c:if test="${not empty pageScripts}">
        ${pageScripts}
    </c:if>
</body>
</html>
