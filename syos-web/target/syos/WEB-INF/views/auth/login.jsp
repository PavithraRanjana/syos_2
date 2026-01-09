<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - SYOS</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        'syos-primary': '#2563eb',
                        'syos-secondary': '#7c3aed',
                        'syos-success': '#059669',
                        'syos-warning': '#d97706',
                        'syos-danger': '#dc2626',
                    }
                }
            }
        }
    </script>
</head>
<body class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
        <!-- Logo/Brand -->
        <div class="text-center">
            <div class="mx-auto h-16 w-16 bg-syos-primary rounded-xl flex items-center justify-center">
                <svg class="h-10 w-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"/>
                </svg>
            </div>
            <h2 class="mt-6 text-3xl font-extrabold text-gray-900">SYOS Retail</h2>
            <p class="mt-2 text-sm text-gray-600">Sign in to your account</p>
        </div>

        <!-- Login Form Card -->
        <div class="bg-white rounded-2xl shadow-xl p-8">
            <!-- Error Message -->
            <div id="errorMessage" class="hidden mb-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
                <p id="errorText"></p>
            </div>

            <!-- Success Message -->
            <c:if test="${not empty successMessage}">
                <div class="mb-4 bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg">
                    <p>${successMessage}</p>
                </div>
            </c:if>

            <form id="loginForm" class="space-y-6">
                <div>
                    <label for="email" class="block text-sm font-medium text-gray-700">Email address</label>
                    <div class="mt-1 relative">
                        <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                            <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207"/>
                            </svg>
                        </div>
                        <input type="email" id="email" name="email" required
                               class="appearance-none block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-syos-primary focus:border-syos-primary"
                               placeholder="you@example.com">
                    </div>
                </div>

                <div>
                    <label for="password" class="block text-sm font-medium text-gray-700">Password</label>
                    <div class="mt-1 relative">
                        <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                            <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
                            </svg>
                        </div>
                        <input type="password" id="password" name="password" required minlength="6"
                               class="appearance-none block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-syos-primary focus:border-syos-primary"
                               placeholder="Enter your password">
                    </div>
                </div>

                <div class="flex items-center justify-between">
                    <div class="flex items-center">
                        <input type="checkbox" id="remember" name="remember"
                               class="h-4 w-4 text-syos-primary focus:ring-syos-primary border-gray-300 rounded">
                        <label for="remember" class="ml-2 block text-sm text-gray-700">Remember me</label>
                    </div>
                </div>

                <div>
                    <button type="submit" id="submitBtn"
                            class="w-full flex justify-center py-3 px-4 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-syos-primary hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-syos-primary transition-colors">
                        <span id="btnText">Sign in</span>
                        <svg id="btnSpinner" class="hidden animate-spin ml-2 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                    </button>
                </div>
            </form>

            <div class="mt-6">
                <div class="relative">
                    <div class="absolute inset-0 flex items-center">
                        <div class="w-full border-t border-gray-300"></div>
                    </div>
                    <div class="relative flex justify-center text-sm">
                        <span class="px-2 bg-white text-gray-500">New to SYOS?</span>
                    </div>
                </div>

                <div class="mt-6">
                    <a href="${pageContext.request.contextPath}/register"
                       class="w-full flex justify-center py-3 px-4 border border-gray-300 rounded-lg shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-syos-primary transition-colors">
                        Create an account
                    </a>
                </div>
            </div>
        </div>

        <!-- Back to Home -->
        <div class="text-center">
            <a href="${pageContext.request.contextPath}/" class="text-sm text-syos-primary hover:text-blue-700">
                &larr; Back to home
            </a>
        </div>
    </div>

    <script>
        const ctx = '${pageContext.request.contextPath}';

        document.getElementById('loginForm').addEventListener('submit', async function(e) {
            e.preventDefault();

            const submitBtn = document.getElementById('submitBtn');
            const btnText = document.getElementById('btnText');
            const btnSpinner = document.getElementById('btnSpinner');
            const errorDiv = document.getElementById('errorMessage');
            const errorText = document.getElementById('errorText');

            // Show loading state
            submitBtn.disabled = true;
            btnText.textContent = 'Signing in...';
            btnSpinner.classList.remove('hidden');
            errorDiv.classList.add('hidden');

            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;

            try {
                const response = await fetch(ctx + '/api/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ email, password })
                });

                const data = await response.json();

                if (data.success) {
                    // Redirect based on role or to intended page
                    const redirect = new URLSearchParams(window.location.search).get('redirect');
                    if (redirect) {
                        window.location.href = redirect;
                    } else {
                        // Role-based redirect
                        const role = data.data.role;
                        const roleRedirects = {
                            'CUSTOMER': '/shop',
                            'CASHIER': '/pos',
                            'INVENTORY_MANAGER': '/inventory/reports',
                            'MANAGER': '/reports',
                            'ADMIN': '/admin'
                        };
                        window.location.href = ctx + (roleRedirects[role] || '/shop');
                    }
                } else {
                    // Show error
                    errorText.textContent = data.message || 'Login failed. Please try again.';
                    errorDiv.classList.remove('hidden');
                }
            } catch (error) {
                errorText.textContent = 'An error occurred. Please try again.';
                errorDiv.classList.remove('hidden');
            } finally {
                // Reset button state
                submitBtn.disabled = false;
                btnText.textContent = 'Sign in';
                btnSpinner.classList.add('hidden');
            }
        });
    </script>
</body>
</html>
