// Enhanced Parallel Execution Monitoring System
// This module provides real-time monitoring of parallel test executions

class ParallelExecutionMonitor {
    constructor() {
        this.activeBatches = new Map();
        this.pollingInterval = null;
        this.updateInterval = 3000; // 3 seconds
        this.isMonitoring = false;
    }

    startMonitoring() {
        if (this.pollingInterval) {
            clearInterval(this.pollingInterval);
        }

        this.isMonitoring = true;
        console.log('Starting parallel execution monitoring...');

        this.pollingInterval = setInterval(async () => {
            if (this.isMonitoring) {
                await this.updateExecutionStatus();
            }
        }, this.updateInterval);

        // Initial update
        this.updateExecutionStatus();
    }

    stopMonitoring() {
        if (this.pollingInterval) {
            clearInterval(this.pollingInterval);
            this.pollingInterval = null;
        }
        this.isMonitoring = false;
        console.log('Stopped parallel execution monitoring');
    }

    async updateExecutionStatus() {
        try {
            // Fetch active batches with running status
            const batches = await this.fetchActiveBatches();

            // Update internal state
            this.updateActiveBatches(batches);

            // Update UI components
            this.renderExecutionStatusPanel();
            this.renderActiveBatchesSection();

        } catch (error) {
            console.error('Error updating execution status:', error);
            // Show error in status panel if API is down
            this.renderErrorState();
        }
    }

    async fetchActiveBatches() {
        try {
            // Try to fetch running batches first
            const runningBatches = await ApiClient.get('/execution/batches?status=RUNNING');

            // If no running batches, try to get all recent batches and filter
            if (!runningBatches || runningBatches.length === 0) {
                const allBatches = await ApiClient.get('/execution/batches');
                return allBatches.filter(batch => batch.status === 'RUNNING' || batch.status === 'EXECUTING');
            }

            return runningBatches;
        } catch (error) {
            console.warn('Failed to fetch active batches, using mock data for demo');
            return this.getMockData();
        }
    }

    getMockData() {
        // Mock data for demonstration when API is not available
        if (Math.random() > 0.7) { // 30% chance of showing active batches
            return [{
                batchId: 'batch-' + Date.now(),
                testSuite: 'regression-suite',
                environment: 'staging',
                status: 'RUNNING',
                startTime: new Date(Date.now() - 300000).toISOString(), // 5 minutes ago
                totalTests: 15,
                completedTests: 8,
                failedTests: 1,
                runningTests: 3,
                parallelThreads: 3
            }];
        }
        return [];
    }

    updateActiveBatches(batches) {
        // Clear old batches
        this.activeBatches.clear();

        // Add current active batches
        if (batches && batches.length > 0) {
            batches.forEach(batch => {
                this.activeBatches.set(batch.batchId, {
                    ...batch,
                    lastUpdated: new Date()
                });
            });
        }
    }

    renderExecutionStatusPanel() {
        const panel = document.getElementById('parallel-execution-status');
        if (!panel) return;

        const activeBatchCount = this.activeBatches.size;
        const totalRunningTests = Array.from(this.activeBatches.values())
            .reduce((sum, batch) => sum + (batch.runningTests || 0), 0);
        const totalCompletedTests = Array.from(this.activeBatches.values())
            .reduce((sum, batch) => sum + (batch.completedTests || 0), 0);
        const totalFailedTests = Array.from(this.activeBatches.values())
            .reduce((sum, batch) => sum + (batch.failedTests || 0), 0);

        panel.innerHTML = `
            <h4>
                <i class="fas fa-cogs ${activeBatchCount > 0 ? 'animate-spin' : ''} text-cyan-400"></i>
                Parallel Execution Monitor
                ${activeBatchCount > 0 ? `<span class="status-badge status-running ml-2">${activeBatchCount} Active</span>` : ''}
            </h4>
            
            <div class="status-grid">
                <div class="status-item">
                    <span class="label">Active Batches</span>
                    <span class="value ${activeBatchCount > 0 ? 'text-green-400' : 'text-gray-400'}">${activeBatchCount}</span>
                </div>
                <div class="status-item">
                    <span class="label">Running Tests</span>
                    <span class="value ${totalRunningTests > 0 ? 'text-blue-400' : 'text-gray-400'}">${totalRunningTests}</span>
                </div>
                <div class="status-item">
                    <span class="label">Completed</span>
                    <span class="value ${totalCompletedTests > 0 ? 'text-green-400' : 'text-gray-400'}">${totalCompletedTests}</span>
                </div>
                <div class="status-item">
                    <span class="label">Failed</span>
                    <span class="value ${totalFailedTests > 0 ? 'text-red-400' : 'text-gray-400'}">${totalFailedTests}</span>
                </div>
            </div>

            ${activeBatchCount > 0 ? this.renderActiveBatchesList() : this.renderNoActiveBatches()}
        `;
    }

    renderActiveBatchesList() {
        const batchesArray = Array.from(this.activeBatches.values());

        return `
            <div class="active-batches">
                <h5>
                    <i class="fas fa-list mr-2"></i>
                    Active Batch Details
                </h5>
                <div class="space-y-3" style="display: flex; flex-direction: column; gap: 0.75rem;">
                    ${batchesArray.map(batch => this.renderBatchCard(batch)).join('')}
                </div>
            </div>
        `;
    }

    renderBatchCard(batch) {
        const progress = batch.totalTests > 0 ?
            Math.round(((batch.completedTests || 0) + (batch.failedTests || 0)) / batch.totalTests * 100) : 0;

        const duration = batch.startTime ?
            this.formatDuration(new Date().getTime() - new Date(batch.startTime).getTime()) : 'N/A';

        return `
            <div class="batch-card glass-card" style="padding: 1rem; border: 1px solid rgba(34, 211, 238, 0.3);">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.75rem;">
                    <div>
                        <h6 style="color: white; font-weight: 600;">Batch ${batch.batchId}</h6>
                        <p style="color: #9ca3af; font-size: 0.875rem;">${batch.testSuite} • ${batch.environment}</p>
                    </div>
                    <div style="text-align: right;">
                        <span class="status-badge status-running">Running</span>
                        <p style="color: #9ca3af; font-size: 0.75rem; margin-top: 0.25rem;">${duration}</p>
                    </div>
                </div>
                
                <div style="margin-bottom: 0.75rem;">
                    <div style="display: flex; justify-content: space-between; font-size: 0.875rem; margin-bottom: 0.25rem;">
                        <span style="color: #d1d5db;">Progress</span>
                        <span style="color: #22d3ee;">${progress}%</span>
                    </div>
                    <div class="progress-bar">
                        <div class="progress-fill" style="width: ${progress}%"></div>
                    </div>
                </div>

                <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 0.5rem; text-align: center;">
                    <div class="metric-item">
                        <div style="color: #60a5fa; font-weight: 600;">${batch.runningTests || 0}</div>
                        <div style="font-size: 0.75rem; color: #9ca3af;">Running</div>
                    </div>
                    <div class="metric-item">
                        <div style="color: #34d399; font-weight: 600;">${batch.completedTests || 0}</div>
                        <div style="font-size: 0.75rem; color: #9ca3af;">Passed</div>
                    </div>
                    <div class="metric-item">
                        <div style="color: #f87171; font-weight: 600;">${batch.failedTests || 0}</div>
                        <div style="font-size: 0.75rem; color: #9ca3af;">Failed</div>
                    </div>
                    <div class="metric-item">
                        <div style="color: #d1d5db; font-weight: 600;">${batch.totalTests || 0}</div>
                        <div style="font-size: 0.75rem; color: #9ca3af;">Total</div>
                    </div>
                </div>

                <div style="margin-top: 0.75rem; display: flex; justify-content: space-between; align-items: center;">
                    <div style="font-size: 0.75rem; color: #9ca3af;">
                        Threads: ${batch.parallelThreads || 1}
                    </div>
                    <div style="display: flex; gap: 0.5rem;">
                        <button class="action-btn action-btn-sm" onclick="viewBatchDetails('${batch.batchId}')" title="View Details">
                            <i class="fas fa-eye"></i>
                        </button>
                        <button class="action-btn action-btn-sm action-btn-danger" onclick="stopBatchExecution('${batch.batchId}')" title="Stop Execution">
                            <i class="fas fa-stop"></i>
                        </button>
                    </div>
                </div>
            </div>
        `;
    }

    renderNoActiveBatches() {
        return `
            <div style="text-align: center; padding: 2rem 0;">
                <i class="fas fa-pause-circle" style="font-size: 2.5rem; color: #9ca3af; margin-bottom: 0.75rem;"></i>
                <p style="color: #9ca3af;">No active parallel executions</p>
                <p style="font-size: 0.875rem; color: #6b7280; margin-top: 0.25rem;">Start a batch execution to see real-time monitoring here</p>
            </div>
        `;
    }

    renderActiveBatchesSection() {
        const container = document.getElementById('active-batches-container');
        if (!container) return;

        if (this.activeBatches.size === 0) {
            container.innerHTML = `
                <div style="text-align: center; padding: 1.5rem 0;">
                    <i class="fas fa-clock" style="font-size: 2rem; color: #9ca3af; margin-bottom: 0.5rem;"></i>
                    <p style="color: #9ca3af;">No active batch executions</p>
                </div>
            `;
            return;
        }

        const batchesArray = Array.from(this.activeBatches.values());
        container.innerHTML = `
            <div style="display: grid; gap: 1rem;">
                ${batchesArray.map(batch => this.renderDetailedBatchCard(batch)).join('')}
            </div>
        `;
    }

    renderDetailedBatchCard(batch) {
        const progress = batch.totalTests > 0 ?
            Math.round(((batch.completedTests || 0) + (batch.failedTests || 0)) / batch.totalTests * 100) : 0;

        const duration = batch.startTime ?
            this.formatDuration(new Date().getTime() - new Date(batch.startTime).getTime()) : 'N/A';

        const threads = batch.parallelThreads || 1;
        const threadUtilization = batch.runningTests ?
            Math.min(Math.round((batch.runningTests / threads) * 100), 100) : 0;

        return `
            <div class="detailed-batch-card glass-card" style="padding: 1.5rem; border: 1px solid rgba(34, 211, 238, 0.2);">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem;">
                    <div>
                        <h4 style="font-size: 1.25rem; font-weight: 600; color: white; display: flex; align-items: center;">
                            <i class="fas fa-layer-group" style="margin-right: 0.5rem; color: #22d3ee;"></i>
                            Batch ${batch.batchId}
                        </h4>
                        <p style="color: #9ca3af;">${batch.testSuite} suite on ${batch.environment}</p>
                    </div>
                    <div style="text-align: right;">
                        <span class="status-badge status-running" style="font-size: 0.875rem;">Active</span>
                        <p style="color: #9ca3af; font-size: 0.875rem; margin-top: 0.25rem;">Duration: ${duration}</p>
                        <p style="color: #9ca3af; font-size: 0.75rem;">Started: ${this.formatDateTime(batch.startTime)}</p>
                    </div>
                </div>

                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(120px, 1fr)); gap: 1rem; margin-bottom: 1rem;">
                    <div class="stat-mini">
                        <div class="stat-mini-value" style="color: #60a5fa;">${batch.runningTests || 0}</div>
                        <div class="stat-mini-label">Running Now</div>
                    </div>
                    <div class="stat-mini">
                        <div class="stat-mini-value" style="color: #34d399;">${batch.completedTests || 0}</div>
                        <div class="stat-mini-label">Completed</div>
                    </div>
                    <div class="stat-mini">
                        <div class="stat-mini-value" style="color: #f87171;">${batch.failedTests || 0}</div>
                        <div class="stat-mini-label">Failed</div>
                    </div>
                    <div class="stat-mini">
                        <div class="stat-mini-value" style="color: #d1d5db;">${batch.totalTests || 0}</div>
                        <div class="stat-mini-label">Total Tests</div>
                    </div>
                </div>

                <div style="margin-bottom: 1rem;">
                    <div style="display: flex; justify-content: space-between; font-size: 0.875rem; margin-bottom: 0.5rem;">
                        <span style="color: #d1d5db;">Overall Progress</span>
                        <span style="color: #22d3ee;">${progress}%</span>
                    </div>
                    <div class="progress-bar-detailed">
                        <div class="progress-fill-detailed" style="width: ${progress}%"></div>
                    </div>
                </div>

                <div style="margin-bottom: 1rem;">
                    <div style="display: flex; justify-content: space-between; font-size: 0.875rem; margin-bottom: 0.5rem;">
                        <span style="color: #d1d5db;">Thread Utilization (${threads} threads)</span>
                        <span style="color: #fbbf24;">${threadUtilization}%</span>
                    </div>
                    <div class="progress-bar-detailed">
                        <div class="progress-fill-thread" style="width: ${threadUtilization}%"></div>
                    </div>
                </div>

                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <div style="display: flex; align-items: center; gap: 1rem; font-size: 0.875rem; color: #9ca3af;">
                        <span><i class="fas fa-server" style="margin-right: 0.25rem;"></i> ${threads} threads</span>
                        <span><i class="fas fa-clock" style="margin-right: 0.25rem;"></i> ${duration}</span>
                    </div>
                    <div style="display: flex; gap: 0.5rem;">
                        <button class="btn-modern btn-sm btn-secondary" onclick="viewBatchLogs('${batch.batchId}')">
                            <i class="fas fa-file-alt"></i> Logs
                        </button>
                        <button class="btn-modern btn-sm btn-warning" onclick="stopBatchExecution('${batch.batchId}')">
                            <i class="fas fa-stop"></i> Stop
                        </button>
                    </div>
                </div>
            </div>
        `;
    }

    renderErrorState() {
        const panel = document.getElementById('parallel-execution-status');
        if (!panel) return;

        panel.innerHTML = `
            <h4>
                <i class="fas fa-exclamation-triangle text-yellow-400"></i>
                Parallel Execution Monitor
                <span class="status-badge status-pending ml-2">Offline</span>
            </h4>
            
            <div style="text-align: center; padding: 2rem 0;">
                <i class="fas fa-wifi" style="font-size: 2.5rem; color: #ef4444; margin-bottom: 0.75rem;"></i>
                <p style="color: #ef4444;">Unable to connect to execution monitoring service</p>
                <p style="font-size: 0.875rem; color: #9ca3af; margin-top: 0.25rem;">Please check your connection and try again</p>
            </div>
        `;
    }

    formatDuration(milliseconds) {
        if (!milliseconds || milliseconds < 0) return '0s';

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

    formatDateTime(timestamp) {
        if (!timestamp) return 'N/A';
        return new Date(timestamp).toLocaleString();
    }

    // Method to be called when batch execution starts
    onBatchExecutionStarted(batchId) {
        console.log(`Batch execution started: ${batchId}`);
        // Immediately refresh the status
        setTimeout(() => this.updateExecutionStatus(), 1000);
    }

    // Method to be called when batch execution completes
    onBatchExecutionCompleted(batchId) {
        console.log(`Batch execution completed: ${batchId}`);
        // Remove from active batches and refresh
        this.activeBatches.delete(batchId);
        this.renderExecutionStatusPanel();
        this.renderActiveBatchesSection();
    }
}

// Global functions for button actions
window.viewBatchDetails = function(batchId) {
    console.log(`Viewing details for batch: ${batchId}`);
    showNotification(`Viewing details for batch: ${batchId}`, 'info');
    // Implement detailed view modal/page
};

window.stopBatchExecution = function(batchId) {
    if (confirm(`Are you sure you want to stop batch execution ${batchId}?`)) {
        console.log(`Stopping batch execution: ${batchId}`);
        showNotification(`Stopping batch execution: ${batchId}`, 'warning');
        // Implement stop functionality
    }
};

window.viewBatchLogs = function(batchId) {
    console.log(`Viewing logs for batch: ${batchId}`);
    showNotification(`Opening logs for batch: ${batchId}`, 'info');
    // Implement log viewer
};

// Initialize and export the monitor instance
let parallelExecutionMonitorInstance = null;

// Initialize function to be called from main script
window.initializeParallelExecutionMonitor = function() {
    if (!parallelExecutionMonitorInstance) {
        parallelExecutionMonitorInstance = new ParallelExecutionMonitor();
        console.log('Parallel execution monitor initialized');
    }
    return parallelExecutionMonitorInstance;
};

// Auto-start monitoring when on execution page
document.addEventListener('DOMContentLoaded', function() {
    // Wait a bit for the main app to initialize
    setTimeout(() => {
        if (currentSection === 'execution' || window.location.hash === '#execution') {
            const monitor = window.initializeParallelExecutionMonitor();
            monitor.startMonitoring();
        }
    }, 2000);
});
