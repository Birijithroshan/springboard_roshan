// Test Manager JavaScript functionality
$(document).ready(function() {
    // Initialize form handlers
    initializeFormHandlers();

    // Check for ongoing executions on page load
    checkOngoingExecutions();

    // Initialize charts if any
    initializeCharts();

    // Load recent executions
    loadRecentExecutions();
});

function initializeFormHandlers() {
    // Run Tests Form Handler
    $('#runTestsForm').on('submit', function(e) {
        e.preventDefault();

        const formData = {
            suite: $('#testSuite').val(),
            browser: $('#browser').val(),
            environment: $('#environment').val(),
            threadCount: $('#threadCount').val(),
            headless: $('#headless').is(':checked'),
            captureScreenshots: $('#captureScreenshots').is(':checked'),
            generateReports: $('#generateReports').is(':checked'),
            testParameters: $('#testParameters').val()
        };

        if (!formData.suite) {
            showNotification('Please select a test suite', 'warning');
            return;
        }

        runTests(formData);
    });

    // Upload Test Form Handler
    $('#uploadTestForm').on('submit', function(e) {
        e.preventDefault();

        const formData = new FormData();
        const fileInput = $('#testFile')[0];
        const testType = $('#testType').val();
        const description = $('#testDescription').val();

        if (!fileInput.files[0]) {
            showNotification('Please select a test file', 'warning');
            return;
        }

        if (!testType) {
            showNotification('Please select test type', 'warning');
            return;
        }

        formData.append('file', fileInput.files[0]);
        formData.append('testType', testType);
        formData.append('description', description);

        uploadTest(formData);
    });

    // Schedule Test Form Handler
    $('#scheduleTestForm').on('submit', function(e) {
        e.preventDefault();

        const scheduleData = {
            suite: $('#scheduleSuite').val(),
            scheduleTime: $('#scheduleTime').val(),
            frequency: $('#scheduleFrequency').val()
        };

        scheduleTest(scheduleData);
    });

    // Add event handlers for data attribute buttons
    $(document).on('click', '.run-single-test-btn', function() {
        const testType = $(this).data('test-type');
        const testName = $(this).data('test');
        runSingleTest(testType, testName);
    });

    $(document).on('click', '.view-test-details-btn', function() {
        const testType = $(this).data('test-type');
        const testName = $(this).data('test');
        viewTestDetails(testType, testName);
    });
}

function quickRunTest(suite) {
    const formData = {
        suite: suite,
        browser: 'chrome',
        environment: 'test',
        threadCount: 3,
        headless: true,
        captureScreenshots: true,
        generateReports: true
    };

    showNotification(`Starting ${suite} tests...`, 'info');
    runTests(formData);
}

function runTests(formData) {
    // Show execution status
    $('#executionStatus').show();
    $('#runTestsForm button[type="submit"]').prop('disabled', true);

    $.ajax({
        url: '/dashboard/run-tests',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(formData),
        success: function(response) {
            if (response.status === 'success') {
                $('#executionId').text(response.executionId);
                showNotification('Test execution started successfully', 'success');

                // Start polling for status updates
                pollExecutionStatus(response.executionId);

                // Store execution ID in session storage
                sessionStorage.setItem('ongoingExecution', JSON.stringify({
                    executionId: response.executionId,
                    suite: formData.suite
                }));
            } else {
                showNotification('Failed to start test execution: ' + response.message, 'danger');
                hideExecutionStatus();
            }
        },
        error: function(xhr, status, error) {
            console.error('Error starting test execution:', error);
            showNotification('Error starting test execution', 'danger');
            hideExecutionStatus();
        }
    });
}

function runSingleTest(testType, testClass) {
    const formData = {
        suite: testType,
        testClass: testClass,
        browser: 'chrome',
        environment: 'test',
        headless: true,
        captureScreenshots: true,
        generateReports: true
    };

    showNotification(`Starting ${testClass} execution...`, 'info');
    runTests(formData);
}

function uploadTest(formData) {
    $('#uploadStatus').show().html(`
        <div class="alert alert-info">
            <div class="d-flex align-items-center">
                <div class="spinner-border spinner-border-sm text-primary me-2" role="status"></div>
                <div>Uploading test file...</div>
            </div>
        </div>
    `);

    $.ajax({
        url: '/dashboard/upload-test',
        method: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function(response) {
            if (response.status === 'success') {
                $('#uploadStatus').html(`
                    <div class="alert alert-success">
                        <i class="fas fa-check"></i> Test file uploaded successfully: ${response.fileName}
                    </div>
                `);

                // Reset form
                $('#uploadTestForm')[0].reset();

                // Refresh test lists
                setTimeout(() => {
                    refreshTestList('ui');
                    refreshTestList('api');
                }, 1000);

            } else {
                $('#uploadStatus').html(`
                    <div class="alert alert-danger">
                        <i class="fas fa-times"></i> Upload failed: ${response.message}
                    </div>
                `);
            }
        },
        error: function(xhr, status, error) {
            console.error('Error uploading test:', error);
            $('#uploadStatus').html(`
                <div class="alert alert-danger">
                    <i class="fas fa-times"></i> Upload failed: Network error
                </div>
            `);
        }
    });
}

function scheduleTest(scheduleData) {
    // Simulate scheduling (in real implementation, this would call backend)
    showNotification('Test scheduled successfully', 'success');

    // Add to scheduled tests list
    const scheduledList = $('#scheduledTestsList');
    const newSchedule = `
        <div class="list-group-item d-flex justify-content-between align-items-center">
            <div>
                <span class="fw-bold">${scheduleData.suite} Tests</span>
                <div class="small text-muted">${scheduleData.frequency} at ${new Date(scheduleData.scheduleTime).toLocaleString()}</div>
            </div>
            <button class="btn btn-sm btn-outline-danger" onclick="removeSchedule(this)">
                <i class="fas fa-trash"></i>
            </button>
        </div>
    `;
    scheduledList.append(newSchedule);

    // Reset form
    $('#scheduleTestForm')[0].reset();
}

function pollExecutionStatus(executionId) {
    const pollInterval = setInterval(() => {
        $.ajax({
            url: `/dashboard/execution-status/${executionId}`,
            method: 'GET',
            success: function(response) {
                updateExecutionProgress(response);

                if (response.status === 'COMPLETED' || response.status === 'FAILED') {
                    clearInterval(pollInterval);
                    handleExecutionComplete(response);
                    sessionStorage.removeItem('ongoingExecution');
                }
            },
            error: function(xhr, status, error) {
                console.error('Error polling execution status:', error);
                clearInterval(pollInterval);
                hideExecutionStatus();
                sessionStorage.removeItem('ongoingExecution');
            }
        });
    }, 3000); // Poll every 3 seconds
}

function updateExecutionProgress(response) {
    const progress = response.progress || 0;
    const progressBar = $('#progressBar');

    progressBar.css('width', progress + '%');
    progressBar.attr('aria-valuenow', progress);

    if (response.currentTest) {
        $('#currentTestInfo').html(`
            <i class="fas fa-cog fa-spin text-primary"></i> 
            Currently running: <strong>${response.currentTest}</strong> (${progress}%)
        `);
    }
}

function handleExecutionComplete(response) {
    hideExecutionStatus();
    loadRecentExecutions(); // Refresh recent executions table

    if (response.status === 'COMPLETED') {
        showNotification('Test execution completed successfully!', 'success');

        if (response.reportUrl) {
            setTimeout(() => {
                window.open(response.reportUrl, '_blank');
            }, 1000);
        }
    } else {
        showNotification('Test execution failed. Check logs for details.', 'danger');
    }
}

function hideExecutionStatus() {
    $('#executionStatus').hide();
    $('#runTestsForm button[type="submit"]').prop('disabled', false);
    $('#progressBar').css('width', '0%');
    $('#currentTestInfo').html('');
}

function viewTestDetails(testType, testClass) {
    // Load test details into modal
    $('#testDetailsContent').html(`
        <div class="row">
            <div class="col-md-6">
                <h6>Test Information</h6>
                <table class="table table-sm">
                    <tr><td><strong>Class:</strong></td><td>${testClass}</td></tr>
                    <tr><td><strong>Type:</strong></td><td>${testType.toUpperCase()}</td></tr>
                    <tr><td><strong>Package:</strong></td><td>org.automation.${testType}</td></tr>
                    <tr><td><strong>Methods:</strong></td><td>5 test methods</td></tr>
                </table>
            </div>
            <div class="col-md-6">
                <h6>Recent Executions</h6>
                <ul class="list-group list-group-flush">
                    <li class="list-group-item d-flex justify-content-between">
                        <span>2024-10-02 14:30</span>
                        <span class="badge bg-success">Passed</span>
                    </li>
                    <li class="list-group-item d-flex justify-content-between">
                        <span>2024-10-01 10:15</span>
                        <span class="badge bg-danger">Failed</span>
                    </li>
                </ul>
            </div>
        </div>
    `);

    // Store current test info for modal actions
    $('#testDetailsModal').data('testType', testType);
    $('#testDetailsModal').data('testClass', testClass);

    $('#testDetailsModal').modal('show');
}

function runTestFromModal() {
    const modal = $('#testDetailsModal');
    const testType = modal.data('testType');
    const testClass = modal.data('testClass');

    modal.modal('hide');
    runSingleTest(testType, testClass);
}

function refreshTestList(testType) {
    // In real implementation, this would call backend to refresh test list
    showNotification(`Refreshing ${testType} test list...`, 'info');

    setTimeout(() => {
        showNotification(`${testType.toUpperCase()} test list refreshed`, 'success');
    }, 1000);
}

function stopAllTests() {
    // Simulate stopping all tests
    showNotification('Stopping all running tests...', 'warning');
    hideExecutionStatus();
    sessionStorage.removeItem('ongoingExecution');
}

function resetForm() {
    $('#runTestsForm')[0].reset();
    $('#threadCount').val('3');
    showNotification('Form reset', 'info');
}

function clearUploadForm() {
    $('#uploadTestForm')[0].reset();
    $('#uploadStatus').hide();
}

function removeSchedule(element) {
    $(element).closest('.list-group-item').remove();
    showNotification('Schedule removed', 'info');
}

function loadRecentExecutions() {
    // Load recent executions into table (simulate data)
    // In real implementation, this would call backend
}

function viewExecutionDetails(executionId) {
    showNotification(`Loading execution details for ${executionId}...`, 'info');
    // In real implementation, this would show detailed execution info
}

function rerunExecution(executionId) {
    showNotification(`Rerunning execution ${executionId}...`, 'info');
    // In real implementation, this would rerun the specific execution
}

function loadPage(pageNum) {
    showNotification(`Loading page ${pageNum}...`, 'info');
    // In real implementation, this would load paginated results
}

function checkOngoingExecutions() {
    // Check if there are any ongoing executions when page loads
    const ongoingExecution = sessionStorage.getItem('ongoingExecution');
    if (ongoingExecution) {
        const executionData = JSON.parse(ongoingExecution);
        $('#executionStatus').show();
        $('#executionId').text(executionData.executionId);
        pollExecutionStatus(executionData.executionId);
    }
}

function initializeCharts() {
    // Initialize any charts if needed
    // This can be extended based on requirements
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
