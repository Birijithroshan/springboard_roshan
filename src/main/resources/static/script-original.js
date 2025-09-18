// Global Configuration
const API_BASE_URL = window.location.origin + '/api';
let currentSection = 'dashboard';

// Initialize Dashboard
document.addEventListener('DOMContentLoaded', function () {
    // Check for mobile devices or desktop mode on mobile
    const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);

    // A simple heuristic to guess if it's a mobile device in desktop mode
    const isLikelyDesktopModeOnMobile = window.screen.width < 1024 && window.innerWidth > 900;

    if (isMobile || isLikelyDesktopModeOnMobile) {
        const appContainer = document.getElementById('app-container');
        const unsupportedMessage = document.getElementById('unsupported-device-message');

        if (appContainer) appContainer.style.display = 'none';
        if (unsupportedMessage) unsupportedMessage.style.display = 'flex';

    } else {
        // Initialize the app only for desktop users
        initializeNavigation();
        initializeMobileMenu();
        checkApiStatus();
        loadDashboardData();
        initializeFormHandlers();

        // Load performance data immediately
        loadPerformanceMetrics();
        loadRecentActivity();
        loadEnvironmentStatus();

        // Set default date values for analytics
        const now = new Date();
        const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);

        const trendFromDate = document.getElementById('trend-from-date');
        const trendToDate = document.getElementById('trend-to-date');

        if (trendFromDate) trendFromDate.value = formatDateForInput(weekAgo);
        if (trendToDate) trendToDate.value = formatDateForInput(now);

        // Auto-refresh performance data every 30 seconds
        setInterval(() => {
            loadPerformanceMetrics();
            loadRecentActivity();
            loadEnvironmentStatus();
        }, 30000);
    }
});

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

        // Close sidebar on window resize to desktop
        window.addEventListener('resize', function () {
            if (window.innerWidth > 1024) {
                sidebar.classList.remove('open');
            }
        });
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

function loadSectionData(section) {
    switch (section) {
        case 'dashboard':
            loadDashboardData();
            break;
        case 'testcases':
            loadTestCases();
            break;
        case 'execution':
            loadExecutions();
            break;
        case 'schedules':
            loadSchedules();
            break;
        case 'reports':
            loadReports();
            break;
        case 'analytics':
            // Analytics are generated on demand
            break;
    }
}

// API Status Check
async function checkApiStatus() {
    try {
        const response = await fetch(`${API_BASE_URL}/testcases`);
        const statusIndicator = document.getElementById('api-status');
        const statusText = document.getElementById('status-text');

        if (response.ok) {
            statusIndicator.className = 'status-indicator online';
            statusText.textContent = 'API Online';
        } else {
            throw new Error('API not responding');
        }
    } catch (error) {
        const statusIndicator = document.getElementById('api-status');
        const statusText = document.getElementById('status-text');
        statusIndicator.className = 'status-indicator offline';
        statusText.textContent = 'API Offline';
    }
}

// Enhanced Dashboard Data Loading
async function loadDashboardData() {
    try {
        showLoading();

        // Load all dashboard sections with error handling
        await Promise.allSettled([
            loadDashboardMetrics(),
            loadRecentBatches(),
            loadTestCases(),
            loadReports(),
            loadPerformanceMetrics(),
            loadRecentActivity(),
            loadEnvironmentStatus()
        ]);

    } catch (error) {
        console.error('Error loading dashboard data:', error);
        showNotification('Error loading dashboard data', 'error');
    } finally {
        hideLoading();
    }
}

// Load Performance Metrics with Mock Data Fallback
async function loadPerformanceMetrics() {
    try {
        const response = await fetch(`${API_BASE_URL}/dashboard/metrics`);
        if (!response.ok) throw new Error('API not available');

        const metrics = await response.json();
        displayPerformanceMetrics(metrics);

        // Load performance summary
        try {
            const perfResponse = await fetch(`${API_BASE_URL}/dashboard/performance-summary`);
            if (perfResponse.ok) {
                const perfData = await perfResponse.json();
                displayPerformanceSummary(perfData);
            }
        } catch (error) {
            console.warn('Performance summary not available, using mock data');
            displayMockPerformanceSummary();
        }

    } catch (error) {
        console.warn('Performance metrics API not available, using mock data');
        displayMockPerformanceMetrics();
    }
}

// Display Performance Metrics with Enhanced Error Handling
function displayPerformanceMetrics(metrics) {
    try {
        // Update test case metrics
        updateElement('total-test-cases', metrics.testCases?.total || 0);
        updateElement('active-test-cases', metrics.testCases?.active || 0);
        updateElement('inactive-test-cases', metrics.testCases?.inactive || 0);

        // Update batch metrics
        updateElement('total-batches', metrics.batches?.total || 0);
        updateElement('completed-batches', metrics.batches?.completed || 0);
        updateElement('pending-batches', metrics.batches?.pending || 0);

        // Update execution metrics
        updateElement('total-executions', metrics.executions?.total || 0);
        updateElement('success-rate', (metrics.executions?.successRate || 0) + '%');
        updateElement('passed-tests', metrics.executions?.passed || 0);
        updateElement('failed-tests', metrics.executions?.failed || 0);

        // Update schedule metrics
        updateElement('total-schedules', metrics.schedules?.total || 0);
        updateElement('active-schedules', metrics.schedules?.active || 0);
        updateElement('inactive-schedules', metrics.schedules?.inactive || 0);

        // Update progress bars
        updateProgressBar('success-rate-bar', metrics.executions?.successRate || 0);
        updateProgressBar('test-coverage-bar', 85); // Mock coverage
        updateProgressBar('automation-coverage-bar', 78); // Mock automation coverage

        // Update dashboard cards
        updateDashboardCard('test-cases-card', metrics.testCases?.total || 0, 'Total Test Cases');
        updateDashboardCard('batches-card', metrics.batches?.total || 0, 'Total Batches');
        updateDashboardCard('success-rate-card', (metrics.executions?.successRate || 0) + '%', 'Success Rate');
        updateDashboardCard('schedules-card', metrics.schedules?.active || 0, 'Active Schedules');

    } catch (error) {
        console.error('Error displaying performance metrics:', error);
        displayMockPerformanceMetrics();
    }
}

// Display Performance Summary
function displayPerformanceSummary(perfData) {
    try {
        updateElement('avg-execution-time', perfData.avgExecutionTime || 'N/A');
        updateElement('parallel-efficiency', perfData.parallelEfficiency || 'N/A');
        updateElement('resource-utilization', perfData.resourceUtilization || 'N/A');
        updateElement('system-health', perfData.systemHealth || 'Unknown');

        // Update trends
        if (perfData.trends) {
            updateElement('weekly-trend', perfData.trends.weeklyTrend || 'N/A');
            updateElement('monthly-improvement', perfData.trends.monthlyImprovement || 'N/A');
            updateElement('daily-success-rate', perfData.trends.dailySuccessRate || 'N/A');
        }

        // Update thresholds
        if (perfData.thresholds) {
            updateElement('response-time-threshold', perfData.thresholds.responseTime || 'N/A');
            updateElement('error-rate-threshold', perfData.thresholds.errorRate || 'N/A');
            updateElement('availability-threshold', perfData.thresholds.availability || 'N/A');
        }

    } catch (error) {
        console.error('Error displaying performance summary:', error);
        displayMockPerformanceSummary();
    }
}

// Load Recent Activity with Enhanced Display
async function loadRecentActivity() {
    try {
        const response = await fetch(`${API_BASE_URL}/dashboard/recent-activity`);
        if (!response.ok) throw new Error('API not available');

        const activity = await response.json();
        displayRecentActivity(activity);

    } catch (error) {
        console.warn('Recent activity API not available, using mock data');
        displayMockRecentActivity();
    }
}

// Enhanced Recent Activity Display
function displayRecentActivity(activity) {
    try {
        const activityContainer = document.getElementById('recent-activity');
        if (!activityContainer) return;

        let html = '<h3 class="section-title">Recent Activity</h3>';

        if (activity.recentBatches && activity.recentBatches.length > 0) {
            html += '<div class="activity-list">';
            activity.recentBatches.forEach((batch, index) => {
                const statusClass = getStatusClass(batch.status);
                const timeAgo = getTimeAgo(batch.startTime || new Date());

                html += `
                    <div class="activity-item ${statusClass}">
                        <div class="activity-icon">
                            <i class="fas fa-${getStatusIcon(batch.status)}"></i>
                        </div>
                        <div class="activity-content">
                            <div class="activity-title">${batch.batchName || batch.name || 'Unknown Batch'}</div>
                            <div class="activity-meta">
                                <span class="status-badge ${statusClass}">${batch.status}</span>
                                <span class="separator">•</span>
                                <span class="environment">${batch.environment}</span>
                                <span class="separator">•</span>
                                <span class="time">${timeAgo}</span>
                            </div>
                            ${batch.totalTests ? `
                                <div class="activity-stats">
                                    <span class="stat-item">
                                        <i class="fas fa-check-circle text-success"></i>
                                        ${batch.passedTests || 0} passed
                                    </span>
                                    <span class="stat-item">
                                        <i class="fas fa-times-circle text-error"></i>
                                        ${batch.failedTests || 0} failed
                                    </span>
                                </div>
                            ` : ''}
                        </div>
                    </div>
                `;
            });
            html += '</div>';

            // Add summary stats
            if (activity.last24Hours) {
                html += `
                    <div class="activity-summary">
                        <h4>Last 24 Hours</h4>
                        <div class="summary-stats">
                            <span class="stat">
                                <strong>${activity.last24Hours.totalExecutions || 0}</strong> executions
                            </span>
                        </div>
                    </div>
                `;
            }
        } else {
            html += '<div class="no-data"><i class="fas fa-info-circle"></i> No recent activity available</div>';
        }

        activityContainer.innerHTML = html;

    } catch (error) {
        console.error('Error displaying recent activity:', error);
        displayMockRecentActivity();
    }
}

// Load Environment Status with Enhanced Display
async function loadEnvironmentStatus() {
    try {
        const response = await fetch(`${API_BASE_URL}/dashboard/environment-status`);
        if (!response.ok) throw new Error('API not available');

        const envStatus = await response.json();
        displayEnvironmentStatus(envStatus);

    } catch (error) {
        console.warn('Environment status API not available, using mock data');
        displayMockEnvironmentStatus();
    }
}

// Enhanced Environment Status Display
function displayEnvironmentStatus(envStatus) {
    try {
        const envContainer = document.getElementById('environment-status');
        if (!envContainer) return;

        let html = '<h3 class="section-title">Environment Status</h3>';
        html += '<div class="env-grid">';

        Object.entries(envStatus).forEach(([env, status]) => {
            const statusClass = getEnvironmentStatusClass(status.status);
            const healthIcon = getHealthIcon(status.status);

            html += `
                <div class="env-card ${statusClass}">
                    <div class="env-header">
                        <h4 class="env-name">${env.toUpperCase()}</h4>
                        <div class="env-status-badge">
                            <i class="fas fa-${healthIcon}"></i>
                            <span>${status.status}</span>
                        </div>
                    </div>
                    <div class="env-details">
                        <div class="detail-item">
                            <span class="label">Last Run:</span>
                            <span class="value">${status.lastRun}</span>
                        </div>
                        <div class="detail-item">
                            <span class="label">Success Rate:</span>
                            <span class="value success-rate">${status.successRate}</span>
                        </div>
                        <div class="detail-item">
                            <span class="label">Issues:</span>
                            <span class="value ${status.issues > 0 ? 'error' : 'success'}">
                                ${status.issues} ${status.issues === 1 ? 'issue' : 'issues'}
                            </span>
                        </div>
                    </div>
                </div>
            `;
        });

        html += '</div>';
        envContainer.innerHTML = html;

    } catch (error) {
        console.error('Error displaying environment status:', error);
        displayMockEnvironmentStatus();
    }
}

// Enhanced Mock Data Functions
function displayMockPerformanceMetrics() {
    const mockMetrics = {
        testCases: { total: 25, active: 20, inactive: 5 },
        batches: { total: 15, completed: 12, pending: 3 },
        executions: { total: 150, passed: 128, failed: 22, successRate: 85.3 },
        schedules: { total: 8, active: 6, inactive: 2 }
    };

    displayPerformanceMetrics(mockMetrics);
    displayMockPerformanceSummary();
}

function displayMockPerformanceSummary() {
    const mockPerf = {
        avgExecutionTime: "42.8 seconds",
        parallelEfficiency: "89%",
        resourceUtilization: "67%",
        systemHealth: "Excellent",
        trends: {
            weeklyTrend: "+3.2%",
            monthlyImprovement: "+7.1%",
            dailySuccessRate: "85.3%"
        },
        thresholds: {
            responseTime: "< 2000ms",
            errorRate: "< 2%",
            availability: "> 99.5%"
        }
    };

    displayPerformanceSummary(mockPerf);
}

function displayMockRecentActivity() {
    const mockActivity = {
        recentBatches: [
            {
                batchName: "Daily Smoke Tests",
                status: "COMPLETED",
                environment: "production",
                startTime: new Date(Date.now() - 2 * 60 * 60 * 1000), // 2 hours ago
                totalTests: 15,
                passedTests: 13,
                failedTests: 2
            },
            {
                batchName: "API Integration Tests",
                status: "RUNNING",
                environment: "staging",
                startTime: new Date(Date.now() - 30 * 60 * 1000), // 30 minutes ago
                totalTests: 20,
                passedTests: 15,
                failedTests: 0
            },
            {
                batchName: "Regression Suite",
                status: "COMPLETED",
                environment: "test",
                startTime: new Date(Date.now() - 6 * 60 * 60 * 1000), // 6 hours ago
                totalTests: 45,
                passedTests: 40,
                failedTests: 5
            },
            {
                batchName: "Performance Tests",
                status: "FAILED",
                environment: "performance",
                startTime: new Date(Date.now() - 12 * 60 * 60 * 1000), // 12 hours ago
                totalTests: 10,
                passedTests: 7,
                failedTests: 3
            },
            {
                batchName: "Security Scan",
                status: "SCHEDULED",
                environment: "production",
                startTime: new Date(Date.now() + 2 * 60 * 60 * 1000), // 2 hours from now
                totalTests: 8,
                passedTests: 0,
                failedTests: 0
            }
        ],
        last24Hours: {
            totalExecutions: 95,
            timestamp: new Date()
        }
    };

    displayRecentActivity(mockActivity);
}

function displayMockEnvironmentStatus() {
    const mockEnvStatus = {
        production: {
            status: "Healthy",
            lastRun: "2 hours ago",
            successRate: "94.2%",
            issues: 0
        },
        staging: {
            status: "Healthy",
            lastRun: "30 minutes ago",
            successRate: "88.9%",
            issues: 1
        },
        test: {
            status: "Warning",
            lastRun: "5 minutes ago",
            successRate: "82.1%",
            issues: 3
        },
        performance: {
            status: "Error",
            lastRun: "12 hours ago",
            successRate: "70.0%",
            issues: 5
        }
    };

    displayEnvironmentStatus(mockEnvStatus);
}

// Enhanced Utility Functions
function updateElement(id, value) {
    const element = document.getElementById(id);
    if (element) {
        element.textContent = value;
        element.classList.add('updated');
        setTimeout(() => element.classList.remove('updated'), 500);
    }
}

function updateProgressBar(id, percentage) {
    const progressBar = document.getElementById(id);
    if (progressBar) {
        progressBar.style.width = percentage + '%';
        progressBar.setAttribute('aria-valuenow', percentage);

        // Add color coding based on percentage
        progressBar.className = progressBar.className.replace(/progress-\w+/, '');
        if (percentage >= 90) {
            progressBar.classList.add('progress-excellent');
        } else if (percentage >= 75) {
            progressBar.classList.add('progress-good');
        } else if (percentage >= 50) {
            progressBar.classList.add('progress-warning');
        } else {
            progressBar.classList.add('progress-poor');
        }
    }
}

function updateDashboardCard(cardId, value, label) {
    const card = document.getElementById(cardId);
    if (card) {
        const valueElement = card.querySelector('.card-value') || card.querySelector('.metric-value') || card.querySelector('h3');
        const labelElement = card.querySelector('.card-label') || card.querySelector('.metric-label') || card.querySelector('p');

        if (valueElement) {
            valueElement.textContent = value;
            valueElement.classList.add('updated');
            setTimeout(() => valueElement.classList.remove('updated'), 500);
        }
        if (labelElement) labelElement.textContent = label;
    }
}

function getStatusClass(status) {
    switch (status?.toUpperCase()) {
        case 'COMPLETED': return 'status-success';
        case 'RUNNING': return 'status-running';
        case 'FAILED': return 'status-error';
        case 'SCHEDULED': return 'status-scheduled';
        case 'PENDING': return 'status-pending';
        default: return 'status-unknown';
    }
}

function getEnvironmentStatusClass(status) {
    switch (status?.toLowerCase()) {
        case 'healthy': return 'env-healthy';
        case 'warning': return 'env-warning';
        case 'error': return 'env-error';
        default: return 'env-unknown';
    }
}

function getStatusIcon(status) {
    switch (status?.toUpperCase()) {
        case 'COMPLETED': return 'check-circle';
        case 'RUNNING': return 'play-circle';
        case 'FAILED': return 'times-circle';
        case 'SCHEDULED': return 'clock';
        case 'PENDING': return 'hourglass-half';
        default: return 'question-circle';
    }
}

function getHealthIcon(status) {
    switch (status?.toLowerCase()) {
        case 'healthy': return 'check-circle';
        case 'warning': return 'exclamation-triangle';
        case 'error': return 'times-circle';
        default: return 'question-circle';
    }
}

function getTimeAgo(date) {
    const now = new Date();
    const diffInMs = now - new Date(date);
    const diffInMinutes = Math.floor(diffInMs / (1000 * 60));
    const diffInHours = Math.floor(diffInMs / (1000 * 60 * 60));
    const diffInDays = Math.floor(diffInMs / (1000 * 60 * 60 * 24));

    if (diffInMinutes < 1) return 'Just now';
    if (diffInMinutes < 60) return `${diffInMinutes} minute${diffInMinutes > 1 ? 's' : ''} ago`;
    if (diffInHours < 24) return `${diffInHours} hour${diffInHours > 1 ? 's' : ''} ago`;
    return `${diffInDays} day${diffInDays > 1 ? 's' : ''} ago`;
}

// Enhanced Dashboard Metrics Loading
async function loadDashboardMetrics() {
    try {
        const response = await fetch(`${API_BASE_URL}/dashboard/metrics`);
        if (response.ok) {
            const metrics = await response.json();
            displayDashboardOverview(metrics);
        } else {
            throw new Error('API not available');
        }
    } catch (error) {
        console.warn('Using mock dashboard data:', error.message);
        displayMockDashboardOverview();
    }
}

function displayDashboardOverview(metrics) {
    // Update dashboard cards with animation
    updateDashboardCard('test-cases-card', metrics.testCases?.total || 0, 'Total Test Cases');
    updateDashboardCard('batches-card', metrics.batches?.total || 0, 'Total Batches');
    updateDashboardCard('success-rate-card', (metrics.executions?.successRate || 0) + '%', 'Success Rate');
    updateDashboardCard('schedules-card', metrics.schedules?.active || 0, 'Active Schedules');
}

function displayMockDashboardOverview() {
    updateDashboardCard('test-cases-card', 25, 'Total Test Cases');
    updateDashboardCard('batches-card', 15, 'Total Batches');
    updateDashboardCard('success-rate-card', '85.3%', 'Success Rate');
    updateDashboardCard('schedules-card', 6, 'Active Schedules');
}
