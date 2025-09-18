// Global Configuration
const API_BASE_URL = window.location.origin + '/automation-framework/api';
let currentSection = 'dashboard';

// Enhanced Error Handling
class ApiClient {
    static async makeRequest(url, options = {}) {
        try {
            const response = await fetch(url, {
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers
                },
                ...options
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            // Check if response has content
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return await response.json();
            } else {
                return await response.text();
            }
        } catch (error) {
            console.error('API Request failed:', error);
            showNotification(`API Error: ${error.message}`, 'error');
            throw error;
        }
    }

    static async get(endpoint) {
        return this.makeRequest(`${API_BASE_URL}${endpoint}`, { method: 'GET' });
    }

    static async post(endpoint, data) {
        return this.makeRequest(`${API_BASE_URL}${endpoint}`, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }

    static async put(endpoint, data) {
        return this.makeRequest(`${API_BASE_URL}${endpoint}`, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    }

    static async delete(endpoint) {
        return this.makeRequest(`${API_BASE_URL}${endpoint}`, { method: 'DELETE' });
    }
}

// Notification System
function showNotification(message, type = 'info', duration = 5000) {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type} animate-slide-in`;
    notification.innerHTML = `
        <div class="notification-content">
            <i class="fas ${getNotificationIcon(type)}"></i>
            <span>${message}</span>
        </div>
        <button class="notification-close" onclick="this.parentElement.remove()">
            <i class="fas fa-times"></i>
        </button>
    `;

    // Add to DOM
    const container = getOrCreateNotificationContainer();
    container.appendChild(notification);

    // Auto-remove after duration
    setTimeout(() => {
        if (notification.parentElement) {
            notification.classList.add('animate-fade-out');
            setTimeout(() => notification.remove(), 300);
        }
    }, duration);
}

function getNotificationIcon(type) {
    const icons = {
        'success': 'fa-check-circle',
        'error': 'fa-exclamation-triangle',
        'warning': 'fa-exclamation-circle',
        'info': 'fa-info-circle'
    };
    return icons[type] || icons.info;
}

function getOrCreateNotificationContainer() {
    let container = document.getElementById('notification-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'notification-container';
        container.className = 'notification-container';
        document.body.appendChild(container);
    }
    return container;
}

// Initialize Dashboard
document.addEventListener('DOMContentLoaded', function () {
    // Check for mobile devices
    const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
    const isLikelyDesktopModeOnMobile = window.screen.width < 1024 && window.innerWidth > 900;

    if (isMobile || isLikelyDesktopModeOnMobile) {
        const appContainer = document.getElementById('app-container');
        const unsupportedMessage = document.getElementById('unsupported-device-message');

        if (appContainer) appContainer.style.display = 'none';
        if (unsupportedMessage) unsupportedMessage.style.display = 'flex';
    } else {
        initializeApp();
    }
});

async function initializeApp() {
    try {
        showLoading();
        
        // Initialize components
        initializeNavigation();
        initializeMobileMenu();
        await checkApiStatus();
        initializeFormHandlers();
        
        // Load initial data
        await loadDashboardData();
        
        // Set default date values
        setDefaultDateValues();
        
        // Setup auto-refresh
        setupAutoRefresh();
        
        showNotification('Application initialized successfully!', 'success');
    } catch (error) {
        console.error('Failed to initialize application:', error);
        showNotification('Failed to initialize application. Please refresh the page.', 'error');
    } finally {
        hideLoading();
    }
}

// Navigation Functions
function initializeNavigation() {
    const navLinks = document.querySelectorAll('.nav-link');
    navLinks.forEach(link => {
        link.addEventListener('click', function (e) {
            e.preventDefault();
            const section = this.getAttribute('data-section');
            switchSection(section);
        });
    });
}

function switchSection(section) {
    // Update navigation
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });
    document.querySelector(`[data-section="${section}"]`).classList.add('active');

    // Update content
    document.querySelectorAll('.content-section').forEach(sec => {
        sec.classList.remove('active');
    });
    document.getElementById(section).classList.add('active');

    // Update page title
    const titles = {
        'dashboard': 'Dashboard',
        'testcases': 'Test Cases',
        'execution': 'Test Execution',
        'schedules': 'Schedules',
        'reports': 'Reports',
        'analytics': 'Analytics'
    };
    document.getElementById('page-title').textContent = titles[section];

    currentSection = section;
    
    // Load section-specific data
    loadSectionData(section);
}

async function loadSectionData(section) {
    try {
        switch (section) {
            case 'dashboard':
                await loadDashboardData();
                break;
            case 'testcases':
                await loadTestCases();
                break;
            case 'execution':
                await loadExecutions();
                break;
            case 'schedules':
                await loadSchedules();
                break;
            case 'reports':
                await loadReports();
                break;
            case 'analytics':
                // Analytics are generated on demand
                break;
        }
    } catch (error) {
        console.error(`Error loading ${section} data:`, error);
        showNotification(`Failed to load ${section} data`, 'error');
    }
}

// API Status Check
async function checkApiStatus() {
    try {
        await ApiClient.get('/testcases');
        updateApiStatus(true, 'API Online');
        return true;
    } catch (error) {
        updateApiStatus(false, 'API Offline');
        return false;
    }
}

function updateApiStatus(online, message) {
    const statusIndicator = document.getElementById('api-status');
    const statusText = document.getElementById('status-text');
    
    if (statusIndicator && statusText) {
        statusIndicator.className = `status-indicator ${online ? 'online' : 'offline'}`;
        statusText.textContent = message;
    }
}

// Dashboard Data Loading
async function loadDashboardData() {
    try {
        showLoading();

        // Load all dashboard components
        await Promise.allSettled([
            loadDashboardMetrics(),
            loadRecentBatches(),
            loadPerformanceMetrics(),
            loadEnvironmentStatus()
        ]);

    } catch (error) {
        console.error('Error loading dashboard data:', error);
        showNotification('Error loading dashboard data', 'error');
    } finally {
        hideLoading();
    }
}

async function loadDashboardMetrics() {
    try {
        const metrics = await ApiClient.get('/dashboard/metrics');
        displayDashboardMetrics(metrics);
    } catch (error) {
        console.error('Error loading dashboard metrics:', error);
        displayDashboardMetrics(getMockMetrics());
    }
}

function displayDashboardMetrics(metrics) {
    // Update stat cards
    updateStatCard('total-testcases', metrics.testCases?.total || 0);
    updateStatCard('total-executions', metrics.executions?.total || 0);
    updateStatCard('active-schedules', metrics.schedules?.active || 0);
    updateStatCard('success-rate', `${metrics.executions?.successRate || 0}%`);
}

function updateStatCard(elementId, value) {
    const element = document.getElementById(elementId);
    if (element) {
        // Animate number change
        animateNumber(element, value);
    }
}

function animateNumber(element, targetValue) {
    const currentValue = parseInt(element.textContent) || 0;
    const isPercentage = typeof targetValue === 'string' && targetValue.includes('%');
    const target = isPercentage ? parseInt(targetValue) : targetValue;
    
    const duration = 1000;
    const steps = 60;
    const stepValue = (target - currentValue) / steps;
    const stepTime = duration / steps;
    
    let currentStep = 0;
    
    const timer = setInterval(() => {
        currentStep++;
        const newValue = Math.round(currentValue + (stepValue * currentStep));
        
        if (currentStep >= steps) {
            clearInterval(timer);
            element.textContent = isPercentage ? `${target}%` : target;
        } else {
            element.textContent = isPercentage ? `${newValue}%` : newValue;
        }
    }, stepTime);
}

// Recent Batches
async function loadRecentBatches() {
    try {
        const batches = await ApiClient.get('/execution/batches/recent?limit=10');
        displayRecentBatches(batches);
    } catch (error) {
        console.error('Error loading recent batches:', error);
        displayRecentBatches(getMockBatches());
    }
}

function displayRecentBatches(batches) {
    const tbody = document.getElementById('recent-batches-body');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (!batches || batches.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center text-gray-400">No recent test batches found</td>
            </tr>
        `;
        return;
    }

    batches.forEach(batch => {
        const row = createBatchRow(batch);
        tbody.appendChild(row);
    });
}

function createBatchRow(batch) {
    const row = document.createElement('tr');
    row.innerHTML = `
        <td>
            <div class="flex items-center">
                <i class="fas fa-layer-group mr-2 text-cyan-400"></i>
                <span class="font-mono text-sm">${batch.batchId || 'N/A'}</span>
            </div>
        </td>
        <td>
            <span class="status-badge status-${(batch.status || 'pending').toLowerCase()}">
                ${batch.status || 'Pending'}
            </span>
        </td>
        <td>${batch.environment || 'dev'}</td>
        <td>${formatDateTime(batch.createdAt)}</td>
        <td>${calculateDuration(batch.createdAt, batch.completedAt)}</td>
        <td>
            <div class="flex space-x-2">
                <button class="btn-icon btn-primary" onclick="viewBatchDetails('${batch.batchId}')" title="View Details">
                    <i class="fas fa-eye"></i>
                </button>
                <button class="btn-icon btn-secondary" onclick="generateBatchReport('${batch.batchId}')" title="Generate Report">
                    <i class="fas fa-file-alt"></i>
                </button>
            </div>
        </td>
    `;
    return row;
}

// Test Cases Management
async function loadTestCases() {
    try {
        showLoading();
        const testCases = await ApiClient.get('/testcases');
        displayTestCases(testCases);
    } catch (error) {
        console.error('Error loading test cases:', error);
        displayTestCases(getMockTestCases());
        showNotification('Using sample test cases data', 'warning');
    } finally {
        hideLoading();
    }
}

function displayTestCases(testCases) {
    const tbody = document.getElementById('testcases-body');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (!testCases || testCases.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" class="text-center text-gray-400">No test cases found. Click "Add Test Case" to create one.</td>
            </tr>
        `;
        return;
    }

    testCases.forEach(testCase => {
        const row = createTestCaseRow(testCase);
        tbody.appendChild(row);
    });
}

function createTestCaseRow(testCase) {
    const row = document.createElement('tr');
    row.innerHTML = `
        <td>${testCase.id}</td>
        <td>
            <div class="test-case-name">
                <span class="font-medium">${testCase.name}</span>
                <div class="text-xs text-gray-400 mt-1">${testCase.description || ''}</div>
            </div>
        </td>
        <td>
            <span class="type-badge type-${(testCase.testType || '').toLowerCase().replace('_', '-')}">
                ${testCase.testType || 'N/A'}
            </span>
        </td>
        <td>
            <span class="priority-badge priority-${(testCase.priority || 'medium').toLowerCase()}">
                ${testCase.priority || 'Medium'}
            </span>
        </td>
        <td>${testCase.environment || 'dev'}</td>
        <td>${testCase.testSuite || 'N/A'}</td>
        <td>
            <span class="status-badge ${testCase.isActive ? 'status-active' : 'status-inactive'}">
                ${testCase.isActive ? 'Active' : 'Inactive'}
            </span>
        </td>
        <td>
            <div class="flex space-x-2">
                <button class="btn-icon btn-primary" onclick="editTestCase(${testCase.id})" title="Edit">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn-icon btn-success" onclick="executeTestCase(${testCase.id})" title="Execute">
                    <i class="fas fa-play"></i>
                </button>
                <button class="btn-icon btn-danger" onclick="deleteTestCase(${testCase.id})" title="Delete">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        </td>
    `;
    return row;
}

// Form Handlers
function initializeFormHandlers() {
    // Test Case Creation Form
    const testCaseForm = document.getElementById('create-testcase-form');
    if (testCaseForm) {
        testCaseForm.addEventListener('submit', handleTestCaseSubmit);
    }

    // Single Test Execution Form
    const singleExecutionForm = document.getElementById('single-execution-form');
    if (singleExecutionForm) {
        singleExecutionForm.addEventListener('submit', handleSingleExecution);
    }

    // Batch Execution Form
    const batchExecutionForm = document.getElementById('batch-execution-form');
    if (batchExecutionForm) {
        batchExecutionForm.addEventListener('submit', handleBatchExecution);
    }

    // Schedule Creation Form
    const scheduleForm = document.getElementById('create-schedule-form');
    if (scheduleForm) {
        scheduleForm.addEventListener('submit', handleScheduleSubmit);
    }

    // Search and Filter Handlers
    const searchInput = document.getElementById('testcase-search');
    if (searchInput) {
        searchInput.addEventListener('input', handleTestCaseSearch);
    }
}

async function handleTestCaseSubmit(e) {
    e.preventDefault();
    
    try {
        showLoading();
        
        const formData = new FormData(e.target);
        const testCaseData = {
            name: formData.get('name'),
            description: formData.get('description'),
            testType: formData.get('testType'),
            priority: formData.get('priority'),
            testSuite: formData.get('testSuite'),
            environment: formData.get('environment'),
            testData: formData.get('testData') || '{}', // Keep as string
            expectedResult: formData.get('expectedResult')
        };

        const result = await ApiClient.post('/testcases', testCaseData);
        
        showNotification('Test case created successfully!', 'success');
        closeModal('testcase-modal');
        e.target.reset();
        
        // Reload test cases if we're on that section
        if (currentSection === 'testcases') {
            await loadTestCases();
        }
        
    } catch (error) {
        console.error('Error creating test case:', error);
        showNotification('Failed to create test case', 'error');
    } finally {
        hideLoading();
    }
}

async function handleSingleExecution(e) {
    e.preventDefault();
    
    try {
        showLoading();
        
        const formData = new FormData(e.target);
        const testCaseId = formData.get('testCaseId');
        const environment = formData.get('environment');

        const result = await ApiClient.post(`/execution/single/${testCaseId}?environment=${environment}`);
        
        showNotification(`Test execution started! Execution ID: ${result.id}`, 'success');
        
        // Reload executions
        await loadExecutions();
        
    } catch (error) {
        console.error('Error executing test:', error);
        showNotification('Failed to execute test', 'error');
    } finally {
        hideLoading();
    }
}

async function handleBatchExecution(e) {
    e.preventDefault();
    
    try {
        showLoading();
        
        const formData = new FormData(e.target);
        const batchData = {
            testSuite: formData.get('testSuite'),
            environment: formData.get('environment'),
            parallelThreads: parseInt(formData.get('parallelThreads')) || 1
        };

        const result = await ApiClient.post('/execution/batch', batchData);
        
        showNotification(`Batch execution started! Batch ID: ${result.batchId}`, 'success');
        
        // Reload dashboard and executions
        await loadRecentBatches();
        
    } catch (error) {
        console.error('Error executing batch:', error);
        showNotification('Failed to execute batch', 'error');
    } finally {
        hideLoading();
    }
}

// Mock Data for Fallback
function getMockMetrics() {
    return {
        testCases: { total: 25, active: 23 },
        executions: { total: 142, passed: 128, successRate: 90.1 },
        schedules: { total: 5, active: 4 },
        batches: { total: 18, completed: 16 }
    };
}

function getMockTestCases() {
    return [
        {
            id: 1,
            name: "Login Functionality Test",
            description: "Test user login with valid credentials",
            testType: "WEB_UI",
            priority: "HIGH",
            environment: "dev",
            testSuite: "Authentication",
            isActive: true
        },
        {
            id: 2,
            name: "User Registration API",
            description: "Test user registration endpoint",
            testType: "API",
            priority: "MEDIUM",
            environment: "staging",
            testSuite: "UserAPI",
            isActive: true
        },
        {
            id: 3,
            name: "Product Search Feature",
            description: "Test product search functionality",
            testType: "WEB_UI",
            priority: "MEDIUM",
            environment: "dev",
            testSuite: "E-Commerce",
            isActive: true
        }
    ];
}

function getMockBatches() {
    return [
        {
            batchId: "batch-001",
            status: "COMPLETED",
            environment: "dev",
            createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
            completedAt: new Date(Date.now() - 1.5 * 60 * 60 * 1000).toISOString()
        },
        {
            batchId: "batch-002",
            status: "RUNNING",
            environment: "staging",
            createdAt: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
            completedAt: null
        }
    ];
}

// Utility Functions
function formatDateTime(dateString) {
    if (!dateString) return 'N/A';
    
    try {
        const date = new Date(dateString);
        return date.toLocaleString();
    } catch (error) {
        return 'Invalid Date';
    }
}

function calculateDuration(startTime, endTime) {
    if (!startTime) return 'N/A';
    
    const start = new Date(startTime);
    const end = endTime ? new Date(endTime) : new Date();
    const diffMs = end - start;
    
    const minutes = Math.floor(diffMs / 60000);
    const seconds = Math.floor((diffMs % 60000) / 1000);
    
    if (minutes > 0) {
        return `${minutes}m ${seconds}s`;
    } else {
        return `${seconds}s`;
    }
}

function setDefaultDateValues() {
    const now = new Date();
    const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);

    const trendFromDate = document.getElementById('trend-from-date');
    const trendToDate = document.getElementById('trend-to-date');

    if (trendFromDate) trendFromDate.value = formatDateForInput(weekAgo);
    if (trendToDate) trendToDate.value = formatDateForInput(now);
}

function formatDateForInput(date) {
    return date.toISOString().slice(0, 16);
}

function setupAutoRefresh() {
    // Auto-refresh every 30 seconds
    setInterval(async () => {
        if (currentSection === 'dashboard') {
            try {
                await loadDashboardMetrics();
                await loadRecentBatches();
            } catch (error) {
                console.error('Auto-refresh failed:', error);
            }
        }
    }, 30000);
}

// Loading Functions
function showLoading() {
    const overlay = document.getElementById('loading-overlay');
    if (overlay) {
        overlay.style.display = 'flex';
    }
}

function hideLoading() {
    const overlay = document.getElementById('loading-overlay');
    if (overlay) {
        overlay.style.display = 'none';
    }
}

// Modal Functions
function showCreateTestCaseModal() {
    const modal = document.getElementById('testcase-modal');
    if (modal) {
        modal.style.display = 'flex';
    }
}

function showCreateScheduleModal() {
    const modal = document.getElementById('schedule-modal');
    if (modal) {
        modal.style.display = 'flex';
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
    }
}

// Mobile Menu Functionality
function initializeMobileMenu() {
    const mobileToggle = document.getElementById('mobile-menu-toggle');
    const sidebar = document.querySelector('.sidebar');

    if (mobileToggle && sidebar) {
        mobileToggle.addEventListener('click', function () {
            sidebar.classList.toggle('open');
        });

        // Close sidebar when clicking outside on mobile
        document.addEventListener('click', function (e) {
            if (window.innerWidth <= 1024) {
                if (!sidebar.contains(e.target) && !mobileToggle.contains(e.target)) {
                    sidebar.classList.remove('open');
                }
            }
        });
    }
}

// Test Case Actions
async function executeTestCase(testCaseId) {
    try {
        const environment = prompt('Enter environment (dev/staging/production):', 'dev');
        if (!environment) return;

        showLoading();
        const result = await ApiClient.post(`/execution/single/${testCaseId}?environment=${environment}`);
        showNotification(`Test case executed! Execution ID: ${result.id}`, 'success');
    } catch (error) {
        console.error('Error executing test case:', error);
        showNotification('Failed to execute test case', 'error');
    } finally {
        hideLoading();
    }
}

async function deleteTestCase(testCaseId) {
    if (!confirm('Are you sure you want to delete this test case?')) {
        return;
    }

    try {
        showLoading();
        await ApiClient.delete(`/testcases/${testCaseId}`);
        showNotification('Test case deleted successfully', 'success');
        
        if (currentSection === 'testcases') {
            await loadTestCases();
        }
    } catch (error) {
        console.error('Error deleting test case:', error);
        showNotification('Failed to delete test case', 'error');
    } finally {
        hideLoading();
    }
}

// Additional stub functions for other features
async function loadExecutions() {
    // Implementation for loading test executions
    console.log('Loading executions...');
}

async function loadSchedules() {
    // Implementation for loading test schedules
    console.log('Loading schedules...');
}

async function loadReports() {
    // Implementation for loading reports
    console.log('Loading reports...');
}

async function loadPerformanceMetrics() {
    try {
        const performance = await ApiClient.get('/dashboard/performance-summary');
        console.log('Performance metrics loaded:', performance);
    } catch (error) {
        console.error('Error loading performance metrics:', error);
    }
}

async function loadEnvironmentStatus() {
    try {
        const status = await ApiClient.get('/dashboard/environment-status');
        console.log('Environment status loaded:', status);
    } catch (error) {
        console.error('Error loading environment status:', error);
    }
}

function viewBatchDetails(batchId) {
    console.log(`View batch details for: ${batchId}`);
    showNotification(`Viewing details for batch: ${batchId}`, 'info');
}

function generateBatchReport(batchId) {
    console.log(`Generate report for batch: ${batchId}`);
    showNotification(`Generating report for batch: ${batchId}`, 'info');
}

function editTestCase(testCaseId) {
    console.log(`Edit test case: ${testCaseId}`);
    showNotification(`Editing test case: ${testCaseId}`, 'info');
}

function handleTestCaseSearch(e) {
    const searchTerm = e.target.value.toLowerCase();
    console.log(`Searching test cases for: ${searchTerm}`);
    // Implementation for filtering test cases
}
