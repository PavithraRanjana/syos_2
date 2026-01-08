<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout pageTitle="Admin Dashboard" activeNav="admin">

    <!-- Page Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-6">
        <div>
            <h1 class="text-2xl font-bold text-gray-900">Admin Dashboard</h1>
            <p class="mt-1 text-sm text-gray-500">Manage users and assign roles</p>
        </div>
        <div class="mt-4 sm:mt-0">
            <button onclick="openCreateUserModal()" class="btn-primary">
                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"/>
                </svg>
                Create User
            </button>
        </div>
    </div>

    <!-- Statistics Cards -->
    <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-6">
        <div class="stat-card border-gray-400">
            <p class="text-xs font-medium text-gray-500 uppercase">Total Users</p>
            <p class="text-2xl font-bold text-gray-900">${totalUsers}</p>
        </div>
        <div class="stat-card border-blue-500">
            <p class="text-xs font-medium text-gray-500 uppercase">Customers</p>
            <p class="text-2xl font-bold text-blue-600">${customerCount}</p>
        </div>
        <div class="stat-card border-green-500">
            <p class="text-xs font-medium text-gray-500 uppercase">Cashiers</p>
            <p class="text-2xl font-bold text-green-600">${cashierCount}</p>
        </div>
        <div class="stat-card border-yellow-500">
            <p class="text-xs font-medium text-gray-500 uppercase">Inventory Mgrs</p>
            <p class="text-2xl font-bold text-yellow-600">${inventoryManagerCount}</p>
        </div>
        <div class="stat-card border-purple-500">
            <p class="text-xs font-medium text-gray-500 uppercase">Managers</p>
            <p class="text-2xl font-bold text-purple-600">${managerCount}</p>
        </div>
        <div class="stat-card border-red-500">
            <p class="text-xs font-medium text-gray-500 uppercase">Admins</p>
            <p class="text-2xl font-bold text-red-600">${adminCount}</p>
        </div>
    </div>

    <!-- Filter and Search -->
    <div class="card mb-6">
        <div class="flex flex-col sm:flex-row gap-4">
            <div class="flex-grow">
                <input type="text" id="searchInput" placeholder="Search users by name or email..."
                       class="input-field" onkeyup="filterTable()">
            </div>
            <div>
                <select id="roleFilter" class="input-field" onchange="filterByRole()">
                    <option value="">All Roles</option>
                    <c:forEach var="role" items="${roles}">
                        <option value="${role.name()}" ${selectedRole == role.name() ? 'selected' : ''}>
                            ${role.displayName}
                        </option>
                    </c:forEach>
                </select>
            </div>
        </div>
    </div>

    <!-- Users Table -->
    <div class="card">
        <div class="card-header flex justify-between items-center">
            <span>User Management</span>
            <span class="text-sm font-normal text-gray-500" id="userCount">${users.size()} users</span>
        </div>

        <c:choose>
            <c:when test="${not empty users}">
                <div class="table-container">
                    <table class="data-table" id="usersTable">
                        <thead>
                            <tr>
                                <th class="table-header">ID</th>
                                <th class="table-header">Name</th>
                                <th class="table-header">Email</th>
                                <th class="table-header">Role</th>
                                <th class="table-header text-center">Status</th>
                                <th class="table-header">Registered</th>
                                <th class="table-header text-right">Actions</th>
                            </tr>
                        </thead>
                        <tbody class="bg-white divide-y divide-gray-200">
                            <c:forEach var="user" items="${users}">
                                <tr class="table-row" data-name="${user.customerName}" data-email="${user.email}" data-role="${user.role.name()}">
                                    <td class="table-cell text-gray-500">#${user.customerId}</td>
                                    <td class="table-cell font-medium">${user.customerName}</td>
                                    <td class="table-cell">${user.email}</td>
                                    <td class="table-cell">
                                        <span class="badge
                                            <c:choose>
                                                <c:when test="${user.role.name() == 'ADMIN'}">bg-red-100 text-red-800</c:when>
                                                <c:when test="${user.role.name() == 'MANAGER'}">bg-purple-100 text-purple-800</c:when>
                                                <c:when test="${user.role.name() == 'INVENTORY_MANAGER'}">bg-yellow-100 text-yellow-800</c:when>
                                                <c:when test="${user.role.name() == 'CASHIER'}">bg-green-100 text-green-800</c:when>
                                                <c:otherwise>bg-blue-100 text-blue-800</c:otherwise>
                                            </c:choose>
                                        ">
                                            ${user.role.displayName}
                                        </span>
                                    </td>
                                    <td class="table-cell text-center">
                                        <span class="badge ${user.active ? 'badge-success' : 'badge-danger'}">
                                            ${user.active ? 'Active' : 'Inactive'}
                                        </span>
                                    </td>
                                    <td class="table-cell text-gray-500">
                                        ${user.registrationDateFormatted}
                                    </td>
                                    <td class="table-cell text-right">
                                        <div class="flex justify-end space-x-2">
                                            <button onclick="openRoleModal(${user.customerId}, '${user.customerName}', '${user.role.name()}')"
                                                    class="text-blue-600 hover:text-blue-800" title="Change Role">
                                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"/>
                                                </svg>
                                            </button>
                                            <button onclick="toggleStatus(${user.customerId}, ${user.active})"
                                                    class="${user.active ? 'text-yellow-600 hover:text-yellow-800' : 'text-green-600 hover:text-green-800'}"
                                                    title="${user.active ? 'Deactivate' : 'Activate'}">
                                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <c:choose>
                                                        <c:when test="${user.active}">
                                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"/>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </svg>
                                            </button>
                                            <button onclick="openResetPasswordModal(${user.customerId}, '${user.customerName}')"
                                                    class="text-gray-600 hover:text-gray-800" title="Reset Password">
                                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"/>
                                                </svg>
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <div class="text-center py-12">
                    <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/>
                    </svg>
                    <h3 class="mt-2 text-sm font-medium text-gray-900">No users found</h3>
                    <p class="mt-1 text-sm text-gray-500">Try adjusting your filters.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Role Legend -->
    <div class="card mt-6">
        <div class="card-header">Role Permissions</div>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <c:forEach var="role" items="${roles}">
                <div class="p-3 rounded-lg bg-gray-50 border border-gray-200">
                    <div class="font-medium text-gray-900">${role.displayName}</div>
                    <div class="text-sm text-gray-500">${role.description}</div>
                </div>
            </c:forEach>
        </div>
    </div>

    <!-- Change Role Modal -->
    <div id="roleModal" class="fixed inset-0 bg-black bg-opacity-50 hidden items-center justify-center z-50">
        <div class="bg-white rounded-lg shadow-xl max-w-md w-full mx-4">
            <div class="px-6 py-4 border-b border-gray-200">
                <h3 class="text-lg font-semibold text-gray-900">Change User Role</h3>
            </div>
            <div class="px-6 py-4">
                <p class="text-sm text-gray-600 mb-4">Changing role for: <span id="roleUserName" class="font-semibold"></span></p>
                <input type="hidden" id="roleUserId">
                <div class="space-y-2">
                    <label class="input-label">Select New Role</label>
                    <select id="newRole" class="input-field">
                        <c:forEach var="role" items="${roles}">
                            <option value="${role.name()}">${role.displayName} - ${role.description}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <div class="px-6 py-4 bg-gray-50 rounded-b-lg flex justify-end space-x-3">
                <button onclick="closeRoleModal()" class="btn-secondary">Cancel</button>
                <button onclick="updateRole()" class="btn-primary">Update Role</button>
            </div>
        </div>
    </div>

    <!-- Create User Modal -->
    <div id="createUserModal" class="fixed inset-0 bg-black bg-opacity-50 hidden items-center justify-center z-50">
        <div class="bg-white rounded-lg shadow-xl max-w-md w-full mx-4 max-h-[90vh] overflow-y-auto">
            <div class="px-6 py-4 border-b border-gray-200">
                <h3 class="text-lg font-semibold text-gray-900">Create New User</h3>
            </div>
            <div class="px-6 py-4 space-y-4">
                <div>
                    <label class="input-label">Full Name *</label>
                    <input type="text" id="newUserName" class="input-field" placeholder="Enter full name">
                </div>
                <div>
                    <label class="input-label">Email *</label>
                    <input type="email" id="newUserEmail" class="input-field" placeholder="Enter email address">
                </div>
                <div>
                    <label class="input-label">Phone</label>
                    <input type="tel" id="newUserPhone" class="input-field" placeholder="Enter phone number">
                </div>
                <div>
                    <label class="input-label">Address</label>
                    <input type="text" id="newUserAddress" class="input-field" placeholder="Enter address">
                </div>
                <div>
                    <label class="input-label">Password *</label>
                    <input type="password" id="newUserPassword" class="input-field" placeholder="Min 6 characters">
                </div>
                <div>
                    <label class="input-label">Role *</label>
                    <select id="newUserRole" class="input-field">
                        <c:forEach var="role" items="${roles}">
                            <option value="${role.name()}">${role.displayName}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <div class="px-6 py-4 bg-gray-50 rounded-b-lg flex justify-end space-x-3">
                <button onclick="closeCreateUserModal()" class="btn-secondary">Cancel</button>
                <button onclick="createUser()" class="btn-success">Create User</button>
            </div>
        </div>
    </div>

    <!-- Reset Password Modal -->
    <div id="resetPasswordModal" class="fixed inset-0 bg-black bg-opacity-50 hidden items-center justify-center z-50">
        <div class="bg-white rounded-lg shadow-xl max-w-md w-full mx-4">
            <div class="px-6 py-4 border-b border-gray-200">
                <h3 class="text-lg font-semibold text-gray-900">Reset Password</h3>
            </div>
            <div class="px-6 py-4">
                <p class="text-sm text-gray-600 mb-4">Reset password for: <span id="resetUserName" class="font-semibold"></span></p>
                <input type="hidden" id="resetUserId">
                <div>
                    <label class="input-label">New Password</label>
                    <input type="password" id="newPassword" class="input-field" placeholder="Min 6 characters">
                </div>
            </div>
            <div class="px-6 py-4 bg-gray-50 rounded-b-lg flex justify-end space-x-3">
                <button onclick="closeResetPasswordModal()" class="btn-secondary">Cancel</button>
                <button onclick="resetPassword()" class="btn-warning">Reset Password</button>
            </div>
        </div>
    </div>

    <script>
        // Filter table by search input
        function filterTable() {
            const searchTerm = document.getElementById('searchInput').value.toLowerCase();
            const rows = document.querySelectorAll('#usersTable tbody tr');
            let visibleCount = 0;

            rows.forEach(row => {
                const name = row.dataset.name.toLowerCase();
                const email = row.dataset.email.toLowerCase();
                const matches = name.includes(searchTerm) || email.includes(searchTerm);
                row.style.display = matches ? '' : 'none';
                if (matches) visibleCount++;
            });

            document.getElementById('userCount').textContent = visibleCount + ' users';
        }

        // Filter by role (redirect)
        function filterByRole() {
            const role = document.getElementById('roleFilter').value;
            if (role) {
                window.location.href = ctx + '/admin?role=' + role;
            } else {
                window.location.href = ctx + '/admin';
            }
        }

        // Role Modal
        function openRoleModal(userId, userName, currentRole) {
            document.getElementById('roleUserId').value = userId;
            document.getElementById('roleUserName').textContent = userName;
            document.getElementById('newRole').value = currentRole;
            document.getElementById('roleModal').classList.remove('hidden');
            document.getElementById('roleModal').classList.add('flex');
        }

        function closeRoleModal() {
            document.getElementById('roleModal').classList.add('hidden');
            document.getElementById('roleModal').classList.remove('flex');
        }

        async function updateRole() {
            const userId = document.getElementById('roleUserId').value;
            const newRole = document.getElementById('newRole').value;

            try {
                const response = await apiFetch('/admin/users/' + userId + '/role', {
                    method: 'PUT',
                    body: JSON.stringify({ role: newRole })
                });
                showNotification(response.message || 'Role updated successfully', 'success');
                closeRoleModal();
                setTimeout(() => location.reload(), 1000);
            } catch (error) {
                showNotification(error.message, 'error');
            }
        }

        // Create User Modal
        function openCreateUserModal() {
            document.getElementById('createUserModal').classList.remove('hidden');
            document.getElementById('createUserModal').classList.add('flex');
        }

        function closeCreateUserModal() {
            document.getElementById('createUserModal').classList.add('hidden');
            document.getElementById('createUserModal').classList.remove('flex');
            // Clear form
            document.getElementById('newUserName').value = '';
            document.getElementById('newUserEmail').value = '';
            document.getElementById('newUserPhone').value = '';
            document.getElementById('newUserAddress').value = '';
            document.getElementById('newUserPassword').value = '';
            document.getElementById('newUserRole').value = 'CUSTOMER';
        }

        async function createUser() {
            const userData = {
                name: document.getElementById('newUserName').value,
                email: document.getElementById('newUserEmail').value,
                phone: document.getElementById('newUserPhone').value,
                address: document.getElementById('newUserAddress').value,
                password: document.getElementById('newUserPassword').value,
                role: document.getElementById('newUserRole').value
            };

            if (!userData.name || !userData.email || !userData.password) {
                showNotification('Please fill in all required fields', 'error');
                return;
            }

            try {
                const response = await apiFetch('/admin/users', {
                    method: 'POST',
                    body: JSON.stringify(userData)
                });
                showNotification(response.message || 'User created successfully', 'success');
                closeCreateUserModal();
                setTimeout(() => location.reload(), 1000);
            } catch (error) {
                showNotification(error.message, 'error');
            }
        }

        // Toggle Status
        async function toggleStatus(userId, currentlyActive) {
            const action = currentlyActive ? 'deactivate' : 'activate';
            if (!confirm('Are you sure you want to ' + action + ' this user?')) return;

            try {
                const response = await apiFetch('/admin/users/' + userId + '/status', {
                    method: 'PUT',
                    body: JSON.stringify({ active: !currentlyActive })
                });
                showNotification(response.message || 'Status updated', 'success');
                setTimeout(() => location.reload(), 1000);
            } catch (error) {
                showNotification(error.message, 'error');
            }
        }

        // Reset Password Modal
        function openResetPasswordModal(userId, userName) {
            document.getElementById('resetUserId').value = userId;
            document.getElementById('resetUserName').textContent = userName;
            document.getElementById('newPassword').value = '';
            document.getElementById('resetPasswordModal').classList.remove('hidden');
            document.getElementById('resetPasswordModal').classList.add('flex');
        }

        function closeResetPasswordModal() {
            document.getElementById('resetPasswordModal').classList.add('hidden');
            document.getElementById('resetPasswordModal').classList.remove('flex');
        }

        async function resetPassword() {
            const userId = document.getElementById('resetUserId').value;
            const newPassword = document.getElementById('newPassword').value;

            if (!newPassword || newPassword.length < 6) {
                showNotification('Password must be at least 6 characters', 'error');
                return;
            }

            try {
                const response = await apiFetch('/admin/users/' + userId + '/password', {
                    method: 'PUT',
                    body: JSON.stringify({ newPassword: newPassword })
                });
                showNotification(response.message || 'Password reset successfully', 'success');
                closeResetPasswordModal();
            } catch (error) {
                showNotification(error.message, 'error');
            }
        }

        // Close modals on outside click
        document.querySelectorAll('[id$="Modal"]').forEach(modal => {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    modal.classList.add('hidden');
                    modal.classList.remove('flex');
                }
            });
        });
    </script>

</t:layout>
