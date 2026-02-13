// CivicPulse API Client

const API_BASE_URL = '/civicpulse/api';

// Helper function to get auth token
function getAuthToken() {
    return localStorage.getItem('civicpulse_token');
}

// Helper function to get user data
function getUserData() {
    const userData = localStorage.getItem('civicpulse_user');
    return userData ? JSON.parse(userData) : null;
}

// Helper function to save auth data
function saveAuthData(token, user) {
    localStorage.setItem('civicpulse_token', token);
    localStorage.setItem('civicpulse_user', JSON.stringify(user));
}

// Helper function to clear auth data
function clearAuthData() {
    localStorage.removeItem('civicpulse_token');
    localStorage.removeItem('civicpulse_user');
}

// Helper function to check if user is logged in
function isLoggedIn() {
    return !!getAuthToken();
}

// Helper function to make API requests
async function apiRequest(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    const token = getAuthToken();

    const headers = {
        ...options.headers
    };

    // Add auth token if available and not already set
    if (token && !headers['Authorization']) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    // Add Content-Type for JSON requests
    if (options.body && !(options.body instanceof FormData)) {
        headers['Content-Type'] = 'application/json';
    }

    try {
        const response = await fetch(url, {
            ...options,
            headers
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'Request failed');
        }

        return data;
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
}

// Auth API
const AuthAPI = {
    async register(name, email, phone, password) {
        return apiRequest('/auth/register', {
            method: 'POST',
            body: JSON.stringify({ name, email, phone, password })
        });
    },

    async login(identifier, password) {
        const response = await apiRequest('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ identifier, password })
        });

        if (response.success) {
            saveAuthData(response.token, response.user);
        }

        return response;
    },

    logout() {
        clearAuthData();
        window.location.href = 'index.html';
    }
};

// Issue API
const IssueAPI = {
    async reportIssue(formData) {
        return apiRequest('/issues/report', {
            method: 'POST',
            body: formData
        });
    },

    async trackIssue(issueId) {
        return apiRequest(`/issues/track/${issueId}`, {
            method: 'GET'
        });
    },

    async getMyIssues() {
        return apiRequest('/issues/my-issues', {
            method: 'GET'
        });
    }
};

// Admin API
const AdminAPI = {
    async getAllIssues() {
        return apiRequest('/admin/issues', {
            method: 'GET'
        });
    },

    async assignIssue(issueId, deptId) {
        return apiRequest(`/admin/issues/${issueId}/assign`, {
            method: 'PUT',
            body: JSON.stringify({ deptId })
        });
    },

    async getAnalytics() {
        return apiRequest('/admin/analytics', {
            method: 'GET'
        });
    }
};

// Department API
const DepartmentAPI = {
    async getDepartmentIssues() {
        return apiRequest('/department/issues', {
            method: 'GET'
        });
    },

    async updateIssue(issueId, status, remarks, proofImageUrl) {
        return apiRequest(`/department/issues/${issueId}/update`, {
            method: 'PUT',
            body: JSON.stringify({ status, remarks, proofImageUrl })
        });
    }
};

// UI Helper Functions
function showAlert(message, type = 'info') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.textContent = message;

    const container = document.querySelector('.container') || document.body;
    container.insertBefore(alertDiv, container.firstChild);

    setTimeout(() => {
        alertDiv.remove();
    }, 5000);
}

function showLoading(element) {
    element.innerHTML = '<div class="spinner"></div>';
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString('en-IN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function getStatusBadgeClass(status) {
    const statusMap = {
        'REPORTED': 'badge-reported',
        'ASSIGNED': 'badge-assigned',
        'IN_PROGRESS': 'badge-in-progress',
        'RESOLVED': 'badge-resolved'
    };
    return statusMap[status] || 'badge-reported';
}

function formatStatus(status) {
    return status.replace('_', ' ');
}

// Redirect if not authenticated
function requireAuth() {
    if (!isLoggedIn()) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

// Redirect if not admin
function requireAdmin() {
    if (!requireAuth()) return false;

    const user = getUserData();
    if (user.role !== 'ADMIN') {
        showAlert('Admin access required', 'error');
        window.location.href = 'index.html';
        return false;
    }
    return true;
}

// Redirect if not department
function requireDepartment() {
    if (!requireAuth()) return false;

    const user = getUserData();
    if (user.role !== 'DEPARTMENT') {
        showAlert('Department access required', 'error');
        window.location.href = 'index.html';
        return false;
    }
    return true;
}

// Update navbar based on auth status
function updateNavbar() {
    const user = getUserData();
    const navMenu = document.querySelector('.navbar-menu');

    if (!navMenu) return;

    if (user) {
        navMenu.innerHTML = `
            <li><a href="index.html">Home</a></li>
            ${user.role === 'CITIZEN' ? '<li><a href="citizen-dashboard.html">Dashboard</a></li>' : ''}
            ${user.role === 'CITIZEN' ? '<li><a href="report-issue.html">Report Issue</a></li>' : ''}
            ${user.role === 'ADMIN' ? '<li><a href="admin-dashboard.html">Admin Dashboard</a></li>' : ''}
            ${user.role === 'DEPARTMENT' ? '<li><a href="department-dashboard.html">Department Dashboard</a></li>' : ''}
            <li><a href="track-issue.html">Track Issue</a></li>
            <li><span style="color: var(--text-secondary);">Welcome, ${user.name}</span></li>
            <li><a href="#" onclick="AuthAPI.logout(); return false;" class="btn btn-sm btn-danger">Logout</a></li>
        `;
    } else {
        navMenu.innerHTML = `
            <li><a href="index.html">Home</a></li>
            <li><a href="track-issue.html">Track Issue</a></li>
            <li><a href="login.html" class="btn btn-sm btn-primary">Login</a></li>
            <li><a href="register.html" class="btn btn-sm btn-secondary">Register</a></li>
        `;
    }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    updateNavbar();
});
