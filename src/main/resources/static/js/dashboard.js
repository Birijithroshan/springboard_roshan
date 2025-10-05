// Dashboard JavaScript functionality
$(document).ready(function() {
    // Initialize charts
    initializeCharts();

    // Auto-refresh dashboard data every 30 seconds
    setInterval(refreshDashboardData, 30000);

    // Initialize tooltips
    $('[data-bs-toggle="tooltip"]').tooltip();

    // Add event handler for view report buttons with data attributes
    $(document).on('click', '.view-report-btn', function() {
        const executionId = $(this).data('execution-id');
        viewReport(executionId);
    });
});

function initializeCharts() {
    // Test Trend Chart
    const trendCtx = document.getElementById('testTrendChart');
    if (trendCtx) {
        new Chart(trendCtx, {
            type: 'line',
            data: {
                labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
                datasets: [{
                    label: 'Passed Tests',
                    data: [65, 59, 80, 81, 56, 85],
                    borderColor: 'rgb(75, 192, 192)',
                    backgroundColor: 'rgba(75, 192, 192, 0.2)',
                    tension: 0.1
                }, {
                    label: 'Failed Tests',
                    data: [5, 12, 8, 4, 15, 6],
                    borderColor: 'rgb(255, 99, 132)',
                    backgroundColor: 'rgba(255, 99, 132, 0.2)',
                    tension: 0.1
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    title: {
                        display: true,
                        text: 'Test Results Over Time'
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }

    // Test Results Pie Chart
    const pieCtx = document.getElementById('testResultsPieChart');
    if (pieCtx) {
        new Chart(pieCtx, {
            type: 'doughnut',
            data: {
                labels: ['Passed', 'Failed', 'Skipped'],
                datasets: [{
                    data: [75, 15, 10],
                    backgroundColor: [
                        '#28a745',
                        '#dc3545',
                        '#ffc107'
                    ],
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
    }
}

function refreshDashboardData() {
    $.ajax({
        url: '/dashboard/api/stats',
        method: 'GET',
        success: function(data) {
            updateStatsCards(data);
            updateLastUpdated();
        },
        error: function(xhr, status, error) {
            console.error('Failed to refresh dashboard data:', error);
        }
    });
}

function updateStatsCards(data) {
    if (data.totalTests !== undefined) {
        $('.text-gray-800').eq(0).text(data.totalTests);
    }
    if (data.passedTests !== undefined) {
        $('.text-gray-800').eq(1).text(data.passedTests);
    }
    if (data.failedTests !== undefined) {
        $('.text-gray-800').eq(2).text(data.failedTests);
    }
    if (data.successRate !== undefined) {
        $('.text-gray-800').eq(3).text(data.successRate + '%');
    }
}

function updateLastUpdated() {
    const now = new Date();
    const timestamp = now.toLocaleString();
    $('.navbar-text span').text(timestamp);
}

function viewReport(executionId) {
    // Open report in new window
    window.open(`/dashboard/reports/execution/${executionId}`, '_blank');
}

// Utility function for showing notifications
function showNotification(message, type = 'info') {
    const alertClass = `alert-${type}`;
    const notification = $(`
        <div class="alert ${alertClass} alert-dismissible fade show position-fixed" 
             style="top: 20px; right: 20px; z-index: 9999;" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `);

    $('body').append(notification);

    // Auto-remove after 5 seconds
    setTimeout(() => {
        notification.alert('close');
    }, 5000);
}
