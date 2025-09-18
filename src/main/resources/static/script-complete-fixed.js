// Enhanced Automation Testing Framework - Complete UI Integration
// Global Configuration
const API_BASE_URL = window.location.origin + '/automation-framework/api';
let currentSection = 'dashboard';
let refreshInterval = null;
let executionStatusPolling = new Map();
let parallelExecutionMonitor = null;

// Enhanced Error Handling and API Client
class ApiClient {
    static async makeRequest(url, options = {}) {
        try {
            console.log('Making API request to:', url);

            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 30000);

            const response = await fetch(url, {
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers
                },
                signal: controller.signal,
                ...options
            });

            clearTimeout(timeoutId);

            if (!response.ok) {
                const errorBody = await response.text();
                console.error('API Error:', response.status, response.statusText, errorBody);
                throw new Error(`HTTP ${response.status}: ${response.statusText} - ${errorBody}`);
            }

            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                const result = await response.json();
                console.log('API Response:', result);
                return result;
            } else if (contentType && contentType.includes('text/')) {
                const result = await response.text();
                console.log('API Text Response:', result);
                return result;
            }

            return await response.text();
        } catch (error) {
            console.error('API Request failed:', error);

            if (error.name === 'AbortError') {
                showNotification('Request timeout. Please try again.', 'error');
            } else {
                showNotification(`API Error: ${error.message}`, 'error');
            }
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

// Enhanced Notification System
function showNotification(message, type = 'info', duration = 5000) {
    console.log('Showing notification:', message, type);

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

    const container = getOrCreateNotificationContainer();
    container.appendChild(notification);

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
        container.style.cssText = 'position: fixed; top: 20px; right: 20px; z-index: 9999; display: flex; flex-direction: column; gap: 12px; max-width: 400px;';
        document.body.appendChild(container);
    }
    return container;
}

// Initialize Application
document.addEventListener('DOMContentLoaded', function () {
    console.log('DOM Content Loaded - Initializing application...');

    // Check for mobile devices
    const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
    const isSmallScreen = window.innerWidth < 1024;

    if (isMobile || isSmallScreen) {
        console.log('Mobile device detected, showing unsupported message');
        const appContainer = document.getElementById('app-container');
        const unsupportedMessage = document.getElementById('unsupported-device-message');

        if (appContainer) appContainer.style.display = 'none';
        if (unsupportedMessage) unsupportedMessage.style.display = 'flex';
        return;
    }

    initializeApp();
});

async function initializeApp() {
    try {
        console.log('Starting application initialization...');
        showLoading('Initializing application...');

        // Initialize core components
        await initializeNavigation();
        initializeMobileMenu();
        await checkApiStatus();
        initializeFormHandlers();

        // Load initial data
        await loadAllData();

        // Set default form values
        setDefaultFormValues();

        // Setup periodic refreshing
        setupAutoRefresh();

        showNotification('🚀 Application initialized successfully!', 'success');
        console.log('Application initialization completed successfully');
    } catch (error) {
        console.error('Failed to initialize application:', error);
        showNotification('❌ Failed to initialize application. Please refresh the page.', 'error');
    } finally {
        hideLoading();
    }
}

// Navigation Management
async function initializeNavigation() {
    const navLinks = document.querySelectorAll('.nav-link');

    navLinks.forEach(link => {
        link.addEventListener('click', async function(e) {
            e.preventDefault();
            const section = this.getAttribute('data-section');
            await switchSection(section);
        });
    });

    // Set initial active section
    await switchSection('dashboard');
}

async function switchSection(sectionName) {
    if (currentSection === sectionName) return;

    try {
        showLoading(`Loading ${sectionName}...`);

        // Update navigation
        document.querySelectorAll('.nav-link').forEach(link => {
            link.classList.remove('active');
        });
        const targetLink = document.querySelector(`[data-section="${sectionName}"]`);
        if (targetLink) targetLink.classList.add('active');

        // Hide all content sections
        document.querySelectorAll('.content-section').forEach(section => {
            section.classList.remove('active');
        });

        // Show target section
        const targetSection = document.getElementById(sectionName);
        if (targetSection) targetSection.classList.add('active');

        // Update page title
        const titles = {
            'dashboard': 'Dashboard',
            'testcases': 'Test Cases',
            'execution': 'Test Execution',
            'schedules': 'Schedules',
            'reports': 'Reports',
            'analytics': 'Analytics'
        };
        const titleElement = document.getElementById('page-title');
        if (titleElement) titleElement.textContent = titles[sectionName];

        // Load section-specific data
        await loadSectionData(sectionName);

        currentSection = sectionName;
    } catch (error) {
        console.error(`Error switching to section ${sectionName}:`, error);
        showNotification(`Failed to load ${sectionName} section`, 'error');
    } finally {
        hideLoading();
    }
}

async function loadSectionData(sectionName) {
    switch (sectionName) {
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
            // Analytics loads on demand
            break;
    }
}

// Data Loading Functions
async function loadAllData() {
    try {
        await Promise.all([
            loadDashboardData(),
            loadTestCases(),
            loadExecutions(),
            loadSchedules(),
            loadReports()
        ]);
    } catch (error) {
        console.error('Error loading data:', error);
        showNotification('Some data failed to load', 'warning');
    }
}

async function loadDashboardData() {
    try {
        const [testCases, executions, schedules, batches] = await Promise.all([
            ApiClient.get('/testcases'),
            ApiClient.get('/execution/executions'),
            ApiClient.get('/schedules'),
            ApiClient.get('/execution/batches')
        ]);

        updateDashboardStats(testCases, executions, schedules);
        updateRecentBatches(batches);

    } catch (error) {
        console.error('Error loading dashboard data:', error);
        showNotification('Failed to load dashboard data', 'error');
    }
}

function updateDashboardStats(testCases, executions, schedules) {
    const totalTestCasesEl = document.getElementById('total-testcases');
    const totalExecutionsEl = document.getElementById('total-executions');
    const activeSchedulesEl = document.getElementById('active-schedules');
    const successRateEl = document.getElementById('success-rate');

    if (totalTestCasesEl) totalTestCasesEl.textContent = testCases.length || 0;
    if (totalExecutionsEl) totalExecutionsEl.textContent = executions.length || 0;
    if (activeSchedulesEl) activeSchedulesEl.textContent = schedules.filter(s => s.enabled).length || 0;

    // Calculate success rate
    const successfulExecutions = executions.filter(e => e.status === 'PASSED').length;
    const successRate = executions.length > 0 ? Math.round((successfulExecutions / executions.length) * 100) : 0;
    if (successRateEl) successRateEl.textContent = `${successRate}%`;
}

function updateRecentBatches(batches) {
    const tbody = document.getElementById('recent-batches-body');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (!batches || batches.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-gray-400 py-8">No recent batches found</td></tr>';
        return;
    }

    // Show latest 10 batches
    batches.slice(0, 10).forEach(batch => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td class="font-mono text-sm">${batch.batchId}</td>
            <td><span class="status-badge status-${batch.status.toLowerCase()}">${batch.status}</span></td>
            <td><span class="env-badge env-${batch.environment}">${batch.environment}</span></td>
            <td>${formatDateTime(batch.startTime)}</td>
            <td>${formatDuration(batch.duration)}</td>
            <td class="action-buttons">
                <button class="action-btn" onclick="viewBatchDetails('${batch.batchId}')" title="View Details">
                    <i class="fas fa-eye"></i>
                </button>
                <button class="action-btn" onclick="generateBatchReport('${batch.batchId}')" title="Generate Report">
                    <i class="fas fa-file-alt"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Test Cases Management
async function loadTestCases() {
    try {
        const testCases = await ApiClient.get('/testcases');
        updateTestCasesTable(testCases);
    } catch (error) {
        console.error('Error loading test cases:', error);
        showNotification('Failed to load test cases', 'error');
    }
}

function updateTestCasesTable(testCases) {
    const tbody = document.getElementById('testcases-body');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (!testCases || testCases.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center text-gray-400 py-8">No test cases found. <a href="#" onclick="showCreateTestCaseModal()" class="text-cyan-400 hover:underline">Create your first test case</a></td></tr>';
        return;
    }

    testCases.forEach(testCase => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td class="font-mono">${testCase.id}</td>
            <td class="font-medium">${testCase.name}</td>
            <td><span class="type-badge type-${testCase.testType?.toLowerCase() || 'unknown'}">${testCase.testType || 'N/A'}</span></td>
            <td><span class="priority-badge priority-${testCase.priority?.toLowerCase() || 'medium'}">${testCase.priority || 'MEDIUM'}</span></td>
            <td><span class="env-badge env-${testCase.environment || 'dev'}">${testCase.environment || 'dev'}</span></td>
            <td>${testCase.testSuite || 'default'}</td>
            <td><span class="status-badge status-${testCase.isActive ? 'active' : 'inactive'}">${testCase.isActive ? 'Active' : 'Inactive'}</span></td>
            <td class="action-buttons">
                <button class="action-btn" onclick="executeTestCase(${testCase.id})" title="Execute Test">
                    <i class="fas fa-play"></i>
                </button>
                <button class="action-btn" onclick="editTestCase(${testCase.id})" title="Edit Test">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="action-btn action-btn-danger" onclick="deleteTestCase(${testCase.id})" title="Delete Test">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Test Execution Management
async function loadExecutions() {
    try {
        const executions = await ApiClient.get('/execution/executions');
        updateExecutionsTable(executions);
    } catch (error) {
        console.error('Error loading executions:', error);
        showNotification('Failed to load executions', 'error');
    }
}

function updateExecutionsTable(executions) {
    const tbody = document.getElementById('executions-body');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (!executions || executions.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-gray-400 py-8">No executions found. Execute a test to see results here.</td></tr>';
        return;
    }

    // Show latest 50 executions
    executions.slice(0, 50).forEach(execution => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td class="font-mono">${execution.id}</td>
            <td class="font-medium">${execution.testCase?.name || 'N/A'}</td>
            <td><span class="status-badge status-${execution.status?.toLowerCase() || 'unknown'}">${execution.status || 'UNKNOWN'}</span></td>
            <td><span class="env-badge env-${execution.environment || 'dev'}">${execution.environment || 'dev'}</span></td>
            <td>${formatDateTime(execution.startTime)}</td>
            <td>${formatDuration(execution.executionDuration)}</td>
            <td class="action-buttons">
                <button class="action-btn" onclick="viewExecutionDetails(${execution.id})" title="View Details">
                    <i class="fas fa-eye"></i>
                </button>
                ${execution.status === 'FAILED' ? `
                    <button class="action-btn" onclick="downloadExecutionLogs(${execution.id})" title="Download Logs">
                        <i class="fas fa-download"></i>
                    </button>
                ` : ''}
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Schedule Management
async function loadSchedules() {
    try {
        const schedules = await ApiClient.get('/schedules');
        updateSchedulesTable(schedules);
    } catch (error) {
        console.error('Error loading schedules:', error);
        showNotification('Failed to load schedules', 'error');
    }
}

function updateSchedulesTable(schedules) {
    const tbody = document.getElementById('schedules-body');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (!schedules || schedules.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center text-gray-400 py-8">No schedules found. <a href="#" onclick="showCreateScheduleModal()" class="text-cyan-400 hover:underline">Create your first schedule</a></td></tr>';
        return;
    }

    schedules.forEach(schedule => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td class="font-mono">${schedule.id}</td>
            <td class="font-medium">${schedule.scheduleName}</td>
            <td class="font-mono text-sm">${schedule.cronExpression}</td>
            <td>${schedule.testSuite}</td>
            <td><span class="env-badge env-${schedule.environment}">${schedule.environment}</span></td>
            <td><span class="status-badge status-${schedule.enabled ? 'active' : 'inactive'}">${schedule.enabled ? 'Active' : 'Inactive'}</span></td>
            <td>${formatDateTime(schedule.nextExecutionTime)}</td>
            <td class="action-buttons">
                <button class="action-btn" onclick="toggleSchedule(${schedule.id}, ${!schedule.enabled})" title="${schedule.enabled ? 'Disable' : 'Enable'} Schedule">
                    <i class="fas fa-${schedule.enabled ? 'pause' : 'play'}"></i>
                </button>
                <button class="action-btn" onclick="executeScheduleNow(${schedule.id})" title="Execute Now">
                    <i class="fas fa-bolt"></i>
                </button>
                <button class="action-btn action-btn-danger" onclick="deleteSchedule(${schedule.id})" title="Delete Schedule">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Reports Management
async function loadReports() {
    try {
        const reports = await ApiClient.get('/reports/list');
        updateReportsTable(reports);
    } catch (error) {
        console.error('Error loading reports:', error);
        updateReportsTable([]);
    }
}

function updateReportsTable(reports) {
    const tbody = document.getElementById('reports-body');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (!reports || reports.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-gray-400 py-8">No reports generated yet. Execute some tests and generate reports to see them here.</td></tr>';
        return;
    }

    reports.forEach(report => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td class="font-mono text-sm">${report.filename}</td>
            <td class="font-mono text-sm">${report.batchId}</td>
            <td><span class="type-badge type-${report.type?.toLowerCase() || 'html'}">${report.type || 'HTML'}</span></td>
            <td>${formatFileSize(report.size)}</td>
            <td>${formatDateTime(report.createdAt)}</td>
            <td class="action-buttons">
                <button class="action-btn" onclick="downloadReport('${report.filename}')" title="Download Report">
                    <i class="fas fa-download"></i>
                </button>
                <button class="action-btn" onclick="previewReport('${report.filename}')" title="Preview Report">
                    <i class="fas fa-eye"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Form Handlers
function initializeFormHandlers() {
    // Test Case Creation Form
    const createTestCaseForm = document.getElementById('create-testcase-form');
    if (createTestCaseForm) {
        createTestCaseForm.addEventListener('submit', handleCreateTestCase);
    }

    // Schedule Creation Form
    const createScheduleForm = document.getElementById('create-schedule-form');
    if (createScheduleForm) {
        createScheduleForm.addEventListener('submit', handleCreateSchedule);
    }

    // Batch Execution Form
    const batchExecutionForm = document.getElementById('batch-execution-form');
    if (batchExecutionForm) {
        batchExecutionForm.addEventListener('submit', handleBatchExecution);
    }

    // Single Test Execution Form
    const singleExecutionForm = document.getElementById('single-execution-form');
    if (singleExecutionForm) {
        singleExecutionForm.addEventListener('submit', handleSingleExecution);
    }

    // Search and Filter Handlers
    setupSearchAndFilters();
}

function setupSearchAndFilters() {
    const searchInput = document.getElementById('testcase-search');
    if (searchInput) {
        let searchTimeout;
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                filterTestCases();
            }, 300);
        });
    }

    const filterSelects = document.querySelectorAll('#testcase-filter-type, #testcase-filter-env');
    filterSelects.forEach(select => {
        select.addEventListener('change', filterTestCases);
    });
}

// Form Submit Handlers
async function handleCreateTestCase(event) {
    event.preventDefault();

    const formData = new FormData(event.target);
    const testCaseData = {};

    for (let [key, value] of formData.entries()) {
        if (key === 'testData') {
            // Validate JSON format but keep it as string for backend
            if (value && value.trim()) {
                try {
                    JSON.parse(value); // Validate JSON format
                    testCaseData[key] = value; // Send as string
                } catch (e) {
                    showNotification('Invalid JSON in test data field', 'error');
                    return;
                }
            } else {
                testCaseData[key] = '{}'; // Default empty JSON object as string
            }
        } else if (key === 'testSteps') {
            // Handle testSteps similarly - validate JSON but send as string
            if (value && value.trim()) {
                try {
                    JSON.parse(value); // Validate JSON format
                    testCaseData[key] = value; // Send as string
                } catch (e) {
                    showNotification('Invalid JSON in test steps field', 'error');
                    return;
                }
            } else {
                testCaseData[key] = '[]'; // Default empty array as string
            }
        } else if (key === 'isActive') {
            testCaseData[key] = value === 'true';
        } else {
            testCaseData[key] = value;
        }
    }

    try {
        showLoading('Creating test case...');
        await ApiClient.post('/testcases', testCaseData);

        showNotification('✅ Test case created successfully!', 'success');
        closeModal('testcase-modal');
        event.target.reset();

        await loadTestCases();
        await loadDashboardData();

    } catch (error) {
        console.error('Error creating test case:', error);
        showNotification('❌ Failed to create test case', 'error');
    } finally {
        hideLoading();
    }
}

async function handleCreateSchedule(event) {
    event.preventDefault();

    const formData = new FormData(event.target);
    const scheduleData = {};

    for (let [key, value] of formData.entries()) {
        if (key === 'parallelThreads') {
            scheduleData[key] = parseInt(value);
        } else if (key === 'enabled') {
            scheduleData[key] = value === 'true';
        } else {
            scheduleData[key] = value;
        }
    }

    scheduleData.enabled = true;

    try {
        showLoading('Creating schedule...');
        await ApiClient.post('/schedules', scheduleData);

        showNotification('✅ Schedule created successfully!', 'success');
        closeModal('schedule-modal');
        event.target.reset();

        await loadSchedules();
        await loadDashboardData();

    } catch (error) {
        console.error('Error creating schedule:', error);
        showNotification('❌ Failed to create schedule', 'error');
    } finally {
        hideLoading();
    }
}

async function handleBatchExecution(event) {
    event.preventDefault();

    const formData = new FormData(event.target);
    const executionData = {
        testSuite: formData.get('testSuite'),
        environment: formData.get('environment'),
        parallelThreads: parseInt(formData.get('parallelThreads'))
    };

    try {
        showLoading('Executing test batch...');
        const result = await ApiClient.post('/execution/batch', executionData);

        showNotification(`🚀 Batch execution started! Batch ID: ${result.batchId}`, 'success');

        // Start polling for execution status
        startExecutionStatusPolling(result.batchId);

        // Refresh executions
        setTimeout(() => {
            loadExecutions();
            loadDashboardData();
        }, 2000);

    } catch (error) {
        console.error('Error executing batch:', error);
        showNotification('❌ Failed to execute batch', 'error');
    } finally {
        hideLoading();
    }
}

async function handleSingleExecution(event) {
    event.preventDefault();

    const formData = new FormData(event.target);
    const testCaseId = formData.get('testCaseId');
    const environment = formData.get('environment');

    try {
        showLoading('Executing test case...');
        const result = await ApiClient.post(`/execution/single/${testCaseId}?environment=${environment}`, {});

        showNotification(`🚀 Test execution started! Test Case ID: ${testCaseId}`, 'success');

        setTimeout(() => {
            loadExecutions();
            loadDashboardData();
        }, 2000);

    } catch (error) {
        console.error('Error executing test case:', error);
        showNotification('❌ Failed to execute test case', 'error');
    } finally {
        hideLoading();
    }
}

// Action Functions
async function executeTestCase(testCaseId) {
    const environment = prompt('Enter environment (dev/staging/production):', 'dev');
    if (!environment) return;

    try {
        showLoading('Executing test case...');
        const result = await ApiClient.post(`/execution/single/${testCaseId}?environment=${environment}`, {});

        showNotification(`🚀 Test execution started! Test Case ID: ${testCaseId}`, 'success');

        setTimeout(() => {
            loadExecutions();
            loadDashboardData();
        }, 2000);

    } catch (error) {
        console.error('Error executing test case:', error);
        showNotification('❌ Failed to execute test case', 'error');
    } finally {
        hideLoading();
    }
}

async function deleteTestCase(testCaseId) {
    if (!confirm('Are you sure you want to delete this test case? This action cannot be undone.')) {
        return;
    }

    try {
        showLoading('Deleting test case...');
        await ApiClient.delete(`/testcases/${testCaseId}`);

        showNotification('✅ Test case deleted successfully!', 'success');

        await loadTestCases();
        await loadDashboardData();

    } catch (error) {
        console.error('Error deleting test case:', error);
        showNotification('❌ Failed to delete test case', 'error');
    } finally {
        hideLoading();
    }
}

async function toggleSchedule(scheduleId, enabled) {
    try {
        showLoading(`${enabled ? 'Enabling' : 'Disabling'} schedule...`);
        await ApiClient.put(`/schedules/${scheduleId}`, { enabled });

        showNotification(`✅ Schedule ${enabled ? 'enabled' : 'disabled'} successfully!`, 'success');

        await loadSchedules();
        await loadDashboardData();

    } catch (error) {
        console.error('Error toggling schedule:', error);
        showNotification('❌ Failed to update schedule', 'error');
    } finally {
        hideLoading();
    }
}

async function deleteSchedule(scheduleId) {
    if (!confirm('Are you sure you want to delete this schedule? This action cannot be undone.')) {
        return;
    }

    try {
        showLoading('Deleting schedule...');
        await ApiClient.delete(`/schedules/${scheduleId}`);

        showNotification('✅ Schedule deleted successfully!', 'success');

        await loadSchedules();
        await loadDashboardData();

    } catch (error) {
        console.error('Error deleting schedule:', error);
        showNotification('❌ Failed to delete schedule', 'error');
    } finally {
        hideLoading();
    }
}

async function executeScheduleNow(scheduleId) {
    try {
        showLoading('Executing schedule...');
        const result = await ApiClient.post(`/schedules/${scheduleId}/execute`);

        showNotification(`🚀 Schedule executed! Batch ID: ${result.batchId}`, 'success');

        setTimeout(() => {
            loadExecutions();
            loadDashboardData();
        }, 2000);

    } catch (error) {
        console.error('Error executing schedule:', error);
        showNotification('❌ Failed to execute schedule', 'error');
    } finally {
        hideLoading();
    }
}

async function generateBatchReport(batchId) {
    try {
        showLoading('Generating batch report...');
        await ApiClient.post(`/reports/html/${batchId}`);

        showNotification('✅ Batch report generated successfully!', 'success');

        await loadReports();

    } catch (error) {
        console.error('Error generating batch report:', error);
        showNotification('❌ Failed to generate batch report', 'error');
    } finally {
        hideLoading();
    }
}

function viewBatchDetails(batchId) {
    showNotification(`Viewing details for batch: ${batchId}`, 'info');
}

function viewExecutionDetails(executionId) {
    showNotification(`Viewing details for execution: ${executionId}`, 'info');
}

function downloadExecutionLogs(executionId) {
    showNotification(`Downloading logs for execution: ${executionId}`, 'info');
}

function editTestCase(testCaseId) {
    showNotification(`Edit functionality not implemented for test case: ${testCaseId}`, 'warning');
}

function downloadReport(filename) {
    showNotification(`Download functionality not implemented for: ${filename}`, 'warning');
}

function previewReport(filename) {
    showNotification(`Preview functionality not implemented for: ${filename}`, 'warning');
}

// Utility Functions
function filterTestCases() {
    const searchTerm = document.getElementById('testcase-search')?.value?.toLowerCase() || '';
    const typeFilter = document.getElementById('testcase-filter-type')?.value || '';
    const envFilter = document.getElementById('testcase-filter-env')?.value || '';

    const rows = document.querySelectorAll('#testcases-body tr');

    rows.forEach(row => {
        const cells = row.querySelectorAll('td');
        if (cells.length < 5) return;

        const name = cells[1]?.textContent?.toLowerCase() || '';
        const type = cells[2]?.textContent?.toLowerCase() || '';
        const env = cells[4]?.textContent?.toLowerCase() || '';

        const matchesSearch = !searchTerm || name.includes(searchTerm);
        const matchesType = !typeFilter || type.includes(typeFilter.toLowerCase());
        const matchesEnv = !envFilter || env.includes(envFilter.toLowerCase());

        row.style.display = matchesSearch && matchesType && matchesEnv ? '' : 'none';
    });
}

function formatDateTime(timestamp) {
    if (!timestamp) return 'N/A';
    return new Date(timestamp).toLocaleString();
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString();
}

function formatDuration(milliseconds) {
    if (!milliseconds || milliseconds === 0) return '0s';

    const seconds = Math.floor(milliseconds / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);

    if (hours > 0) {
        return `${hours}h ${minutes % 60}m ${seconds % 60}s`;
    } else if (minutes > 0) {
        return `${minutes}m ${seconds % 60}s`;
    } else {
        return `${seconds}s`;
    }
}

function formatFileSize(bytes) {
    if (!bytes) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function setDefaultFormValues() {
    const now = new Date();
    const oneWeekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);

    const fromDateInput = document.getElementById('trend-from-date');
    const toDateInput = document.getElementById('trend-to-date');

    if (fromDateInput) fromDateInput.value = oneWeekAgo.toISOString().slice(0, 16);
    if (toDateInput) toDateInput.value = now.toISOString().slice(0, 16);
}

// Modal Functions
function showCreateTestCaseModal() {
    const modal = document.getElementById('testcase-modal');
    if (modal) modal.style.display = 'flex';
}

function showCreateScheduleModal() {
    const modal = document.getElementById('schedule-modal');
    if (modal) modal.style.display = 'flex';
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.style.display = 'none';
}

// Mobile Menu
function initializeMobileMenu() {
    const mobileMenuToggle = document.getElementById('mobile-menu-toggle');
    const sidebar = document.querySelector('.sidebar');

    if (mobileMenuToggle && sidebar) {
        mobileMenuToggle.addEventListener('click', function() {
            sidebar.classList.toggle('mobile-open');
        });
    }

    document.addEventListener('click', function(event) {
        if (sidebar && !sidebar.contains(event.target) && mobileMenuToggle && !mobileMenuToggle.contains(event.target)) {
            sidebar.classList.remove('mobile-open');
        }
    });
}

// Loading States
function showLoading(message = 'Loading...') {
    const overlay = document.getElementById('loading-overlay');
    if (overlay) {
        const messageEl = overlay.querySelector('p');
        if (messageEl) messageEl.textContent = message;
        overlay.style.display = 'flex';
    }
}

function hideLoading() {
    const overlay = document.getElementById('loading-overlay');
    if (overlay) {
        overlay.style.display = 'none';
    }
}

// API Status Check
async function checkApiStatus() {
    const statusIndicator = document.getElementById('api-status');
    const statusText = document.getElementById('status-text');

    try {
        await ApiClient.get('/validation/health');
        if (statusIndicator) statusIndicator.className = 'status-indicator status-online';
        if (statusText) {
            statusText.textContent = 'API Online';
            statusText.className = 'text-sm font-medium text-green-400';
        }
    } catch (error) {
        if (statusIndicator) statusIndicator.className = 'status-indicator status-offline';
        if (statusText) {
            statusText.textContent = 'API Offline';
            statusText.className = 'text-sm font-medium text-red-400';
        }
    }
}

// Auto-refresh Setup
function setupAutoRefresh() {
    refreshInterval = setInterval(async () => {
        try {
            if (currentSection === 'dashboard') {
                await loadDashboardData();
            } else if (currentSection === 'execution') {
                await loadExecutions();
            }

            await checkApiStatus();
        } catch (error) {
            console.debug('Auto-refresh error:', error);
        }
    }, 30000);
}

// Execution Status Polling
function startExecutionStatusPolling(batchId) {
    if (executionStatusPolling.has(batchId)) return;

    const pollInterval = setInterval(async () => {
        try {
            const batchStatus = await ApiClient.get(`/execution/batch/${batchId}`);

            if (batchStatus.status === 'COMPLETED' || batchStatus.status === 'FAILED') {
                clearInterval(pollInterval);
                executionStatusPolling.delete(batchId);

                showNotification(
                    `Batch ${batchId} completed with status: ${batchStatus.status}`,
                    batchStatus.status === 'COMPLETED' ? 'success' : 'error'
                );

                setTimeout(() => {
                    loadExecutions();
                    loadDashboardData();
                }, 2000);
            }
        } catch (error) {
            console.error('Error polling execution status:', error);
            clearInterval(pollInterval);
            executionStatusPolling.delete(batchId);
        }
    }, 5000);

    executionStatusPolling.set(batchId, pollInterval);
}

// Cleanup on page unload
window.addEventListener('beforeunload', () => {
    if (refreshInterval) {
        clearInterval(refreshInterval);
    }

    executionStatusPolling.forEach((interval, batchId) => {
        clearInterval(interval);
    });
});

// Click outside modal to close
window.addEventListener('click', (event) => {
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    });
});
