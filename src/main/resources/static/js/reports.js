// Reports page JavaScript functionality
$(document).ready(function() {
    // Initialize report functionality
    initializeReports();

    // Initialize charts
    initializeReportCharts();

    // Load screenshots on demand
    $('#loadScreenshots').on('click', loadScreenshots);

    // Initialize filter functionality
    initializeFilters();

    // Load initial statistics
    loadReportStatistics();
});

function initializeReports() {
    // Add click handlers for report links
    $('a[href*="/artifacts/"]').on('click', function(e) {
        const href = $(this).attr('href');
        if (href.endsWith('.json')) {
            e.preventDefault();
            viewJsonReport(href);
        }
    });

    // Initialize bulk operations
    initializeBulkOperations();

    // Auto-refresh reports every 5 minutes
    setInterval(refreshReports, 300000);

    // Add event handlers for data attribute buttons
    $(document).on('click', '.share-report-btn', function() {
        const reportName = $(this).data('report');
        shareReport(reportName);
    });

    $(document).on('click', '.delete-report-btn', function() {
        const reportName = $(this).data('report');
        deleteReport(reportName);
    });

    $(document).on('click', '.view-xml-report-btn', function() {
        const reportName = $(this).data('report');
        viewXmlReport(reportName);
    });

    $(document).on('click', '.convert-to-html-btn', function() {
        const reportName = $(this).data('report');
        convertToHtml(reportName);
    });

    $(document).on('click', '.view-json-report-btn', function() {
        const reportName = $(this).data('report');
        viewJsonReport(`/artifacts/api/${reportName}`);
    });

    $(document).on('click', '.format-json-btn', function() {
        const reportName = $(this).data('report');
        formatJson(reportName);
    });
}

function initializeReportCharts() {
    // Report Generation Trends Chart
    const trendsCtx = document.getElementById('reportTrendsChart');
    if (trendsCtx) {
        new Chart(trendsCtx, {
            type: 'line',
            data: {
                labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
                datasets: [{
                    label: 'HTML Reports',
                    data: [12, 15, 8, 20, 18, 5, 3],
                    borderColor: 'rgb(75, 192, 192)',
                    backgroundColor: 'rgba(75, 192, 192, 0.2)',
                    tension: 0.1
                }, {
                    label: 'XML Reports',
                    data: [8, 10, 6, 15, 12, 3, 2],
                    borderColor: 'rgb(255, 206, 86)',
                    backgroundColor: 'rgba(255, 206, 86, 0.2)',
                    tension: 0.1
                }, {
                    label: 'JSON Reports',
                    data: [5, 8, 4, 12, 10, 2, 1],
                    borderColor: 'rgb(153, 102, 255)',
                    backgroundColor: 'rgba(153, 102, 255, 0.2)',
                    tension: 0.1
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    title: {
                        display: true,
                        text: 'Report Generation Trends (Last 7 Days)'
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

    // Report Types Distribution Chart
    const typesCtx = document.getElementById('reportTypesChart');
    if (typesCtx) {
        new Chart(typesCtx, {
            type: 'doughnut',
            data: {
                labels: ['HTML Reports', 'XML Reports', 'JSON Reports', 'PDF Reports', 'Excel Reports'],
                datasets: [{
                    data: [45, 25, 15, 10, 5],
                    backgroundColor: [
                        '#FF6384',
                        '#36A2EB',
                        '#FFCE56',
                        '#4BC0C0',
                        '#9966FF'
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

function initializeFilters() {
    // Set default date range (last 7 days)
    const today = new Date();
    const lastWeek = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);

    $('#dateToFilter').val(today.toISOString().split('T')[0]);
    $('#dateFromFilter').val(lastWeek.toISOString().split('T')[0]);
}

function filterReports() {
    const searchTerm = $('#reportSearch').val().toLowerCase();
    const typeFilter = $('#reportTypeFilter').val();
    const statusFilter = $('#statusFilter').val();
    const suiteFilter = $('#suiteFilter').val();
    const dateFrom = $('#dateFromFilter').val();
    const dateTo = $('#dateToFilter').val();

    // Filter each report section
    filterReportSection('#htmlReportsSection', searchTerm, typeFilter, statusFilter, suiteFilter, dateFrom, dateTo);
    filterReportSection('#xmlReportsSection', searchTerm, typeFilter, statusFilter, suiteFilter, dateFrom, dateTo);
    filterReportSection('#jsonReportsSection', searchTerm, typeFilter, statusFilter, suiteFilter, dateFrom, dateTo);

    updateFilteredCount();
}

function filterReportSection(sectionId, searchTerm, typeFilter, statusFilter, suiteFilter, dateFrom, dateTo) {
    const section = $(sectionId);
    const rows = section.find('tbody tr');

    rows.each(function() {
        const row = $(this);
        const reportName = row.find('td:first').text().toLowerCase();
        const reportType = sectionId.includes('html') ? 'html' :
                          sectionId.includes('xml') ? 'xml' : 'json';

        let shouldShow = true;

        // Apply search filter
        if (searchTerm && !reportName.includes(searchTerm)) {
            shouldShow = false;
        }

        // Apply type filter
        if (typeFilter && typeFilter !== reportType) {
            shouldShow = false;
        }

        // Apply other filters (status, suite, date range would be implemented based on actual data)

        row.toggle(shouldShow);
    });
}

function clearFilters() {
    $('#reportSearch').val('');
    $('#reportTypeFilter').val('');
    $('#statusFilter').val('');
    $('#suiteFilter').val('');
    $('#dateFromFilter').val('');
    $('#dateToFilter').val('');

    filterReports();
    showNotification('Filters cleared', 'info');
}

function viewJsonReport(url) {
    showNotification('Loading JSON report...', 'info');

    $.ajax({
        url: url,
        method: 'GET',
        dataType: 'json',
        success: function(data) {
            showJsonModal(data, url);
        },
        error: function(xhr, status, error) {
            console.error('Error loading JSON report:', error);

            // Try to load as text if JSON parsing fails
            $.get(url, function(textData) {
                showJsonModal(textData, url, false);
            }).fail(function() {
                showNotification('Failed to load JSON report', 'danger');
            });
        }
    });
}

function showJsonModal(data, title, isJson = true) {
    const fileName = title.split('/').pop();
    let content;

    if (isJson) {
        content = `<pre class="bg-light p-3 rounded" style="max-height: 400px; overflow-y: auto;"><code>${JSON.stringify(data, null, 2)}</code></pre>`;
    } else {
        content = `<pre class="bg-light p-3 rounded" style="max-height: 400px; overflow-y: auto;"><code>${data}</code></pre>`;
    }

    $('#reportModalTitle').text(`JSON Report: ${fileName}`);
    $('#reportModalContent').html(content);
    $('#reportDownloadBtn').attr('href', title);

    $('#reportViewModal').modal('show');
}

function viewXmlReport(reportName) {
    const url = `/artifacts/reports/${reportName}`;
    showNotification('Loading XML report...', 'info');

    $.get(url, function(data) {
        const fileName = reportName;
        const content = `<pre class="bg-light p-3 rounded" style="max-height: 400px; overflow-y: auto;"><code>${$('<div>').text(data).html()}</code></pre>`;

        $('#reportModalTitle').text(`XML Report: ${fileName}`);
        $('#reportModalContent').html(content);
        $('#reportDownloadBtn').attr('href', url);

        $('#reportViewModal').modal('show');
    }).fail(function() {
        showNotification('Failed to load XML report', 'danger');
    });
}

function loadScreenshots() {
    $('#loadScreenshots').prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Loading...');

    // Simulate loading screenshots (in real implementation, this would call backend)
    setTimeout(() => {
        displayScreenshots([
            {
                filename: 'test_login_failed_20241002_143022.png',
                testName: 'Test Login Failed',
                timestamp: '2024-10-02 14:30:22',
                status: 'FAILED'
            },
            {
                filename: 'test_checkout_success_20241002_142015.png',
                testName: 'Test Checkout Success',
                timestamp: '2024-10-02 14:20:15',
                status: 'PASSED'
            },
            {
                filename: 'test_navigation_failed_20241002_141508.png',
                testName: 'Test Navigation Failed',
                timestamp: '2024-10-02 14:15:08',
                status: 'FAILED'
            }
        ]);
    }, 2000);
}

function displayScreenshots(screenshots) {
    const gallery = $('#screenshotGallery');
    gallery.empty();

    if (screenshots.length === 0) {
        gallery.html('<div class="col-12"><p class="text-muted text-center">No screenshots available</p></div>');
        return;
    }

    screenshots.forEach(screenshot => {
        const screenshotHtml = `
            <div class="col-md-4 col-lg-3 mb-3">
                <div class="card">
                    <img src="/artifacts/screenshots/${screenshot.filename}" 
                         class="card-img-top screenshot-thumbnail" 
                         alt="${screenshot.testName}"
                         style="height: 200px; object-fit: cover; cursor: pointer;"
                         onclick="viewScreenshot('${screenshot.filename}', '${screenshot.testName}')">
                    <div class="card-body p-2">
                        <h6 class="card-title mb-1" style="font-size: 0.9rem;">${screenshot.testName}</h6>
                        <small class="text-muted">${screenshot.timestamp}</small>
                        <div class="mt-2">
                            <span class="badge ${screenshot.status === 'FAILED' ? 'bg-danger' : 'bg-success'}">
                                ${screenshot.status}
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        `;
        gallery.append(screenshotHtml);
    });

    $('#loadScreenshots').prop('disabled', false).html('<i class="fas fa-images"></i> Load Screenshots');
    showNotification(`Loaded ${screenshots.length} screenshots`, 'success');
}

function viewScreenshot(filename, testName) {
    $('#screenshotModalTitle').text(`Screenshot: ${testName}`);
    $('#screenshotModalContent').html(`
        <img src="/artifacts/screenshots/${filename}" 
             class="img-fluid" 
             alt="${testName}"
             style="max-height: 80vh; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);">
    `);
    $('#screenshotDownloadBtn').attr('href', `/artifacts/screenshots/${filename}`).attr('download', filename);

    $('#screenshotModal').modal('show');
}

function clearScreenshots() {
    $('#screenshotGallery').empty();
    showNotification('Screenshot gallery cleared', 'info');
}

function refreshReports() {
    showNotification('Refreshing reports...', 'info');

    // Simulate refresh (in real implementation, this would reload data from backend)
    setTimeout(() => {
        loadReportStatistics();
        showNotification('Reports refreshed', 'success');
    }, 2000);
}

function generateNewReport() {
    showNotification('Generating new report...', 'info');

    // Simulate report generation
    setTimeout(() => {
        showNotification('New report generated successfully', 'success');
        refreshReports();
    }, 3000);
}

function shareReport(reportName) {
    const shareUrl = `${window.location.origin}/artifacts/reports/${reportName}`;

    if (navigator.share) {
        navigator.share({
            title: `Test Report: ${reportName}`,
            url: shareUrl
        });
    } else {
        // Fallback: copy to clipboard
        navigator.clipboard.writeText(shareUrl).then(() => {
            showNotification('Report URL copied to clipboard', 'success');
        }).catch(() => {
            showNotification('Failed to copy URL', 'danger');
        });
    }
}

function deleteReport(reportName) {
    if (confirm(`Are you sure you want to delete the report "${reportName}"?`)) {
        showNotification(`Deleting report: ${reportName}...`, 'warning');

        // Simulate deletion (in real implementation, this would call backend)
        setTimeout(() => {
            showNotification('Report deleted successfully', 'success');
            refreshReports();
        }, 1500);
    }
}

function convertToHtml(reportName) {
    showNotification(`Converting ${reportName} to HTML format...`, 'info');

    // Simulate conversion
    setTimeout(() => {
        showNotification('Report converted to HTML successfully', 'success');
        refreshReports();
    }, 2500);
}

function formatJson(reportName) {
    showNotification(`Formatting JSON report: ${reportName}...`, 'info');
    viewJsonReport(`/artifacts/api/${reportName}`);
}

// Bulk Operations
function initializeBulkOperations() {
    // Add checkboxes to each report row
    $('table tbody tr').each(function() {
        const checkbox = '<td><input type="checkbox" class="report-checkbox" /></td>';
        $(this).prepend(checkbox);
    });

    // Add header checkbox
    $('table thead tr').each(function() {
        $(this).prepend('<th><input type="checkbox" id="selectAllReports" onchange="toggleSelectAll()" /></th>');
    });

    // Update selected count when checkboxes change
    $(document).on('change', '.report-checkbox', updateSelectedCount);
}

function toggleSelectAll() {
    const selectAll = $('#selectAllReports').is(':checked');
    $('.report-checkbox').prop('checked', selectAll);
    updateSelectedCount();
}

function updateSelectedCount() {
    const selectedCount = $('.report-checkbox:checked').length;
    $('#selectedCount').text(selectedCount);
}

function updateFilteredCount() {
    const visibleRows = $('table tbody tr:visible').length;
    showNotification(`Showing ${visibleRows} reports`, 'info');
}

function bulkDownload() {
    const selectedReports = getSelectedReports();
    if (selectedReports.length === 0) {
        showNotification('Please select reports to download', 'warning');
        return;
    }

    showNotification(`Preparing ${selectedReports.length} reports for download...`, 'info');

    // Simulate bulk download
    setTimeout(() => {
        showNotification('Bulk download completed', 'success');
    }, 3000);
}

function bulkArchive() {
    const selectedReports = getSelectedReports();
    if (selectedReports.length === 0) {
        showNotification('Please select reports to archive', 'warning');
        return;
    }

    if (confirm(`Archive ${selectedReports.length} selected reports?`)) {
        showNotification(`Archiving ${selectedReports.length} reports...`, 'info');

        setTimeout(() => {
            showNotification('Reports archived successfully', 'success');
            refreshReports();
        }, 2500);
    }
}

function bulkDelete() {
    const selectedReports = getSelectedReports();
    if (selectedReports.length === 0) {
        showNotification('Please select reports to delete', 'warning');
        return;
    }

    if (confirm(`Delete ${selectedReports.length} selected reports? This action cannot be undone.`)) {
        showNotification(`Deleting ${selectedReports.length} reports...`, 'warning');

        setTimeout(() => {
            showNotification('Reports deleted successfully', 'success');
            refreshReports();
        }, 2500);
    }
}

function bulkEmail() {
    const selectedReports = getSelectedReports();
    if (selectedReports.length === 0) {
        showNotification('Please select reports to email', 'warning');
        return;
    }

    const emailAddress = prompt('Enter email address:');
    if (emailAddress) {
        showNotification(`Sending ${selectedReports.length} reports to ${emailAddress}...`, 'info');

        setTimeout(() => {
            showNotification('Reports sent successfully', 'success');
        }, 3000);
    }
}

function bulkExport() {
    const selectedReports = getSelectedReports();
    if (selectedReports.length === 0) {
        showNotification('Please select reports to export', 'warning');
        return;
    }

    showNotification(`Exporting ${selectedReports.length} reports to CSV...`, 'info');

    setTimeout(() => {
        // Create and download CSV file
        const csvContent = generateReportsCsv(selectedReports);
        downloadCsv(csvContent, 'reports_export.csv');
        showNotification('CSV export completed', 'success');
    }, 2000);
}

function exportFilteredReports() {
    const visibleRows = $('table tbody tr:visible');
    showNotification(`Exporting ${visibleRows.length} filtered reports...`, 'info');

    setTimeout(() => {
        showNotification('Filtered reports exported successfully', 'success');
    }, 2000);
}

function getSelectedReports() {
    const selected = [];
    $('.report-checkbox:checked').each(function() {
        const row = $(this).closest('tr');
        const reportName = row.find('td').eq(1).text().trim();
        selected.push(reportName);
    });
    return selected;
}

function generateReportsCsv(reports) {
    const headers = ['Report Name', 'Type', 'Status', 'Generated Date', 'Size'];
    const rows = reports.map(report => [report, 'HTML', 'Complete', new Date().toISOString(), '2.3MB']);

    return [headers, ...rows].map(row => row.join(',')).join('\n');
}

function downloadCsv(content, filename) {
    const blob = new Blob([content], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    window.URL.revokeObjectURL(url);
}

function loadReportStatistics() {
    // Update report statistics (simulate data loading)
    $('#totalReportsCount').text('24');
    $('#recentReportsCount').text('8');
    $('#storageUsed').text('156 MB');
    $('#avgGenTime').text('2.5s');
}

// Utility function for notifications
function showNotification(message, type = 'info') {
    const alertClass = `alert-${type}`;
    const iconClass = type === 'success' ? 'fa-check' :
                     type === 'danger' ? 'fa-times' :
                     type === 'warning' ? 'fa-exclamation-triangle' : 'fa-info';

    const notification = $(`
        <div class="alert ${alertClass} alert-dismissible fade show position-fixed" 
             style="top: 20px; right: 20px; z-index: 9999; min-width: 300px;" role="alert">
            <i class="fas ${iconClass} me-2"></i>${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `);

    $('body').append(notification);

    setTimeout(() => {
        notification.alert('close');
    }, 5000);
}
