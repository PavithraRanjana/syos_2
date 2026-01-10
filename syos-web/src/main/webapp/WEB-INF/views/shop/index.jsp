<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Shop - SYOS</title>
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

        <body class="bg-gray-50 min-h-screen">
            <!-- Navigation -->
            <nav class="bg-white shadow-sm sticky top-0 z-50">
                <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div class="flex justify-between h-16">
                        <div class="flex items-center">
                            <a href="${pageContext.request.contextPath}/" class="flex items-center">
                                <div class="h-10 w-10 bg-syos-primary rounded-lg flex items-center justify-center">
                                    <svg class="h-6 w-6 text-white" fill="none" stroke="currentColor"
                                        viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                                    </svg>
                                </div>
                                <span class="ml-2 text-xl font-bold text-gray-900">SYOS Shop</span>
                            </a>
                            <div id="headerReportsBtn" class="ml-6 hidden hover:scale-105 transition-transform"></div>
                        </div>

                        <div class="flex items-center space-x-4">
                            <!-- Search -->
                            <div class="hidden md:block">
                                <div class="relative">
                                    <input type="text" id="searchInput" placeholder="Search products..."
                                        class="w-64 pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-syos-primary focus:border-syos-primary">
                                    <div class="absolute inset-y-0 left-0 pl-3 flex items-center">
                                        <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor"
                                            viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                                        </svg>
                                    </div>
                                </div>
                            </div>

                            <!-- Cart -->
                            <a href="${pageContext.request.contextPath}/cart"
                                class="relative p-2 text-gray-600 hover:text-syos-primary">
                                <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                                </svg>
                                <span id="cartBadge"
                                    class="hidden absolute -top-1 -right-1 h-5 w-5 bg-syos-danger text-white text-xs rounded-full flex items-center justify-center">0</span>
                            </a>

                            <!-- User Menu -->
                            <div id="userMenu" class="flex items-center space-x-2">
                                <a href="${pageContext.request.contextPath}/login"
                                    class="text-gray-600 hover:text-syos-primary">Login</a>
                            </div>
                        </div>
                    </div>
                </div>
            </nav>

            <!-- Main Content -->
            <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <!-- Page Header -->
                <div class="mb-8">
                    <h1 class="text-3xl font-bold text-gray-900">Shop Our Products</h1>
                    <p class="mt-2 text-gray-600">Browse our selection of quality grocery items</p>
                </div>

                <!-- Filters -->
                <div class="flex flex-col md:flex-row gap-4 mb-8">
                    <select id="categoryFilter"
                        class="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-syos-primary focus:border-syos-primary">
                        <option value="">All Categories</option>
                    </select>
                    <select id="sortFilter"
                        class="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-syos-primary focus:border-syos-primary">
                        <option value="name">Sort by Name</option>
                        <option value="price-low">Price: Low to High</option>
                        <option value="price-high">Price: High to Low</option>
                    </select>
                </div>

                <!-- Products Grid -->
                <div id="productsGrid" class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
                    <!-- Products will be loaded here -->
                </div>

                <!-- Loading State -->
                <div id="loadingState" class="flex justify-center items-center py-12">
                    <svg class="animate-spin h-8 w-8 text-syos-primary" fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4">
                        </circle>
                        <path class="opacity-75" fill="currentColor"
                            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                        </path>
                    </svg>
                    <span class="ml-2 text-gray-600">Loading products...</span>
                </div>

                <!-- Empty State -->
                <div id="emptyState" class="hidden text-center py-12">
                    <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                            d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
                    </svg>
                    <h3 class="mt-2 text-lg font-medium text-gray-900">No products found</h3>
                    <p class="mt-1 text-gray-500">Try adjusting your search or filter criteria.</p>
                </div>
            </main>

            <!-- Add to Cart Toast -->
            <div id="toast"
                class="hidden fixed bottom-4 right-4 bg-syos-success text-white px-6 py-3 rounded-lg shadow-lg flex items-center space-x-2">
                <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
                <span id="toastMessage">Added to cart!</span>
            </div>

            <script>
                const ctx = '${pageContext.request.contextPath}';
                let products = [];
                let categories = [];
                let isAuthenticated = false;
                let userRole = null;
                let userName = null;

                // Check authentication status
                async function checkAuth() {
                    try {
                        const response = await fetch(ctx + '/api/auth/status', {
                            credentials: 'same-origin'
                        });
                        const data = await response.json();
                        isAuthenticated = data.data?.authenticated || false;
                        userRole = data.data?.role || null;
                        userName = data.data?.name || null;
                        updateUserMenu();
                        updateCartBadge();
                    } catch (error) {
                        console.error('Auth check failed:', error);
                    }
                }

                function updateUserMenu() {
                    const userMenu = document.getElementById('userMenu');
                    if (isAuthenticated) {
                        let menuHtml = '<span class="text-sm text-gray-600">' + (userName || 'User') + '</span>';

                        // Show My Orders link for customers
                        if (userRole === 'CUSTOMER') {
                            menuHtml += '<a href="' + ctx + '/orders" class="text-gray-600 hover:text-syos-primary">My Orders</a>';
                        }

                        // Show POS link for cashiers
                        if (userRole === 'CASHIER') {
                            menuHtml += '<a href="' + ctx + '/pos" class="px-3 py-1 bg-green-500 text-white text-sm rounded hover:bg-green-600">Go to POS</a>';
                        }

                        if (userRole === 'MANAGER') {
                            // Update Reports button in header
                            const headerReportsBtn = document.getElementById('headerReportsBtn');
                            if (headerReportsBtn) {
                                headerReportsBtn.innerHTML = '<a href="' + ctx + '/reports" class="bg-green-600 text-white hover:bg-green-700 px-3 py-2 rounded-md text-sm font-medium transition-colors">Reports</a>';
                                headerReportsBtn.classList.remove('hidden');
                            }
                        }

                        // Logout button for all users
                        menuHtml += '<button onclick="logout()" class="px-3 py-1 bg-red-500 text-white text-sm rounded hover:bg-red-600">Logout</button>';

                        userMenu.innerHTML = menuHtml;
                    }
                }

                function logout() {
                    // Use form submission for reliable logout
                    const form = document.createElement('form');
                    form.method = 'POST';
                    form.action = ctx + '/logout';
                    document.body.appendChild(form);
                    form.submit();
                }

                async function updateCartBadge() {
                    if (!isAuthenticated) return;
                    try {
                        const response = await fetch(ctx + '/api/cart/count', {
                            credentials: 'same-origin'
                        });
                        const data = await response.json();
                        const badge = document.getElementById('cartBadge');
                        const count = data.data?.count || 0;
                        if (count > 0) {
                            badge.textContent = count;
                            badge.classList.remove('hidden');
                        } else {
                            badge.classList.add('hidden');
                        }
                    } catch (error) {
                        console.error('Cart count failed:', error);
                    }
                }

                // Load categories
                async function loadCategories() {
                    try {
                        const response = await fetch(ctx + '/api/categories');
                        const data = await response.json();
                        categories = data.data?.categories || [];
                        populateCategoryFilter();
                    } catch (error) {
                        console.error('Failed to load categories:', error);
                    }
                }

                function populateCategoryFilter() {
                    const select = document.getElementById('categoryFilter');
                    categories.forEach(cat => {
                        const option = document.createElement('option');
                        option.value = cat.categoryId;
                        option.textContent = cat.categoryName;
                        select.appendChild(option);
                    });
                }

                // Load products
                async function loadProducts(categoryId = '', search = '') {
                    document.getElementById('loadingState').classList.remove('hidden');
                    document.getElementById('emptyState').classList.add('hidden');
                    document.getElementById('productsGrid').innerHTML = '';

                    try {
                        let url = ctx + '/api/products?active=true';
                        if (categoryId) url += '&categoryId=' + categoryId;
                        if (search) url = ctx + '/api/products/search?q=' + encodeURIComponent(search);

                        const response = await fetch(url);
                        const data = await response.json();
                        products = data.data?.products || [];

                        document.getElementById('loadingState').classList.add('hidden');

                        if (products.length === 0) {
                            document.getElementById('emptyState').classList.remove('hidden');
                        } else {
                            renderProducts();
                        }
                    } catch (error) {
                        console.error('Failed to load products:', error);
                        document.getElementById('loadingState').classList.add('hidden');
                        document.getElementById('emptyState').classList.remove('hidden');
                    }
                }

                function renderProducts() {
                    const grid = document.getElementById('productsGrid');
                    const sortBy = document.getElementById('sortFilter').value;

                    let sortedProducts = [...products];
                    if (sortBy === 'price-low') {
                        sortedProducts.sort((a, b) => parseFloat(a.unitPrice) - parseFloat(b.unitPrice));
                    } else if (sortBy === 'price-high') {
                        sortedProducts.sort((a, b) => parseFloat(b.unitPrice) - parseFloat(a.unitPrice));
                    } else {
                        sortedProducts.sort((a, b) => a.productName.localeCompare(b.productName));
                    }

                    grid.innerHTML = sortedProducts.map(product => `
                <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden hover:shadow-md transition-shadow">
                    <div class="h-48 bg-gradient-to-br from-gray-100 to-gray-200 flex items-center justify-center">
                        <svg class="h-20 w-20 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
                        </svg>
                    </div>
                    <div class="p-4">
                        <p class="text-xs text-gray-500 mb-1">\${product.productCode}</p>
                        <h3 class="font-semibold text-gray-900 mb-2 line-clamp-2">\${product.productName}</h3>
                        <div class="flex items-center justify-between mb-3">
                            <span class="text-lg font-bold text-syos-primary">\${product.unitPrice}</span>
                            <span class="text-xs text-gray-500">\${product.unitOfMeasure || 'PCS'}</span>
                        </div>
                        <button onclick="addToCart('\${product.productCode}')"
                                class="w-full bg-syos-primary text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition-colors flex items-center justify-center space-x-2">
                            <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"/>
                            </svg>
                            <span>Add to Cart</span>
                        </button>
                    </div>
                </div>
            `).join('');
                }

                async function addToCart(productCode) {
                    if (!isAuthenticated) {
                        window.location.href = ctx + '/login?redirect=' + encodeURIComponent(window.location.pathname);
                        return;
                    }

                    try {
                        const response = await fetch(ctx + '/api/cart/items', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            credentials: 'same-origin',
                            body: JSON.stringify({ productCode, quantity: 1 })
                        });

                        const data = await response.json();
                        if (data.success) {
                            showToast('Added to cart!');
                            updateCartBadge();
                        } else {
                            showToast(data.error || 'Failed to add to cart', true);
                        }
                    } catch (error) {
                        showToast('Error adding to cart', true);
                    }
                }

                function showToast(message, isError = false) {
                    const toast = document.getElementById('toast');
                    const toastMessage = document.getElementById('toastMessage');
                    toastMessage.textContent = message;
                    toast.className = `fixed bottom-4 right-4 \${isError ? 'bg-syos-danger' : 'bg-syos-success'} text-white px-6 py-3 rounded-lg shadow-lg flex items-center space-x-2`;
                    setTimeout(() => toast.classList.add('hidden'), 3000);
                }

                // Event listeners
                document.getElementById('categoryFilter').addEventListener('change', (e) => {
                    loadProducts(e.target.value);
                });

                document.getElementById('sortFilter').addEventListener('change', () => {
                    renderProducts();
                });

                let searchTimeout;
                document.getElementById('searchInput').addEventListener('input', (e) => {
                    clearTimeout(searchTimeout);
                    searchTimeout = setTimeout(() => {
                        loadProducts('', e.target.value);
                    }, 300);
                });

                // Initialize
                checkAuth();
                loadCategories();
                loadProducts();
            </script>
        </body>

        </html>