# AutoTest Framework - Complete End-to-End Testing Platform

## 🚀 Overview

The AutoTest Framework is a comprehensive, enterprise-grade test automation platform that supports Web UI, API, AI, and Database testing with advanced features like parallel execution, scheduling, comprehensive reporting, and real-time analytics.

## 🌟 Key Features

### 🎯 Test Case Management
- **Multi-Type Support**: Web UI, API, AI, and Database test cases
- **Rich Test Data**: JSON-based configuration for complex test scenarios
- **Step-by-Step Execution**: Structured test steps with detailed logging
- **Priority Management**: High, Medium, Low priority classification
- **Environment Support**: Development, Staging, Production environments

### ⚡ Test Execution Engine
- **Parallel Execution**: Configure up to 10 parallel threads for faster test runs
- **Individual Test Runs**: Execute single tests for debugging and validation
- **Batch Processing**: Run entire test suites with consolidated reporting
- **Real-time Monitoring**: Live execution status tracking and notifications

### 📅 Advanced Scheduling
- **Cron-based Scheduling**: Flexible scheduling with cron expressions
- **Multi-environment Support**: Schedule tests across different environments
- **Automatic Execution**: Pipeline integration for CI/CD workflows
- **Schedule Management**: Enable/disable, execute immediately, or delete schedules

### 📊 Comprehensive Reporting
- **Multiple Formats**: HTML, CSV, XML, and Allure report generation
- **Beautiful HTML Reports**: Interactive reports with charts and detailed logs
- **Failure Analysis**: Screenshots for UI failures, request/response dumps for API failures
- **Batch Reports**: Consolidated reporting for test suites
- **Download & Preview**: Easy access to all generated reports

### 📈 Analytics & Insights
- **Trend Analysis**: Historical test execution trends with success rates
- **Regression Metrics**: Identify performance degradation and improvements
- **Environment Comparison**: Compare test results across environments
- **Daily Summaries**: Visual charts showing test execution patterns

### 🎨 Modern User Interface
- **Responsive Design**: Optimized for desktop and laptop usage
- **Real-time Updates**: Live dashboard with automatic data refresh
- **Intuitive Navigation**: Clean, modern interface with contextual actions
- **Glass Morphism Design**: Beautiful visual effects and animations
- **Dark Theme**: Professional dark theme optimized for long usage

## 🛠 Technical Stack

### Backend
- **Spring Boot 3.1.5** - Main application framework
- **H2 Database** - In-memory database with persistence
- **Quartz Scheduler** - Advanced scheduling capabilities
- **TestNG** - Test execution framework
- **Selenium WebDriver 4.15.0** - Web UI testing
- **REST-Assured 5.3.2** - API testing framework

### Frontend
- **HTML5 & CSS3** - Modern web standards
- **JavaScript ES6+** - Asynchronous operations and modern syntax
- **Font Awesome 6.5.0** - Rich iconography
- **Inter Font Family** - Professional typography
- **CSS Grid & Flexbox** - Advanced layout techniques

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Chrome/Firefox browser (for Web UI tests)

### Installation & Setup
1. Clone the repository
2. Run `mvn clean install` to build the project
3. Start the application: `mvn spring-boot:run`
4. Access the dashboard: `http://localhost:8080/automation-framework/`

## 📱 Usage Guide

### Creating Test Cases
1. Navigate to **Test Cases** section
2. Click **Add Test Case** button
3. Fill in the test case details:
   - **Name**: Descriptive test case name
   - **Type**: Select from Web UI, API, AI, or Database
   - **Priority**: High, Medium, or Low
   - **Environment**: Target environment
   - **Test Data**: JSON configuration for test parameters
   - **Test Steps**: Structured steps for execution
   - **Expected Result**: Define success criteria

### Test Execution Options

#### Single Test Execution
1. Go to **Test Execution** section
2. Use **Execute Single Test** form
3. Enter test case ID and select environment
4. Click **Execute Test**

#### Batch Execution
1. Use **Execute Batch** form
2. Specify test suite name
3. Select environment and parallel threads
4. Click **Execute Batch**

### Setting Up Schedules
1. Navigate to **Schedules** section
2. Click **Add Schedule**
3. Configure:
   - **Schedule Name**: Descriptive name
   - **Cron Expression**: Timing specification
   - **Test Suite**: Which tests to run
   - **Environment**: Target environment
   - **Parallel Threads**: Execution parallelism

### Generating Reports
1. Go to **Reports** section
2. Enter the batch ID
3. Choose report generation option:
   - **Generate All Reports**: Creates HTML, CSV, XML, and Allure reports
   - **HTML Report Only**: Quick HTML report generation
4. Download or preview reports from the reports list

### Analytics & Insights
1. Navigate to **Analytics** section
2. **Trend Analysis**:
   - Select date range
   - Generate trends to see execution patterns
3. **Regression Metrics**:
   - Choose environment and time period
   - Analyze performance degradation

## 🔧 API Endpoints

### Test Cases
- `GET /api/testcases` - List all test cases
- `POST /api/testcases` - Create new test case
- `PUT /api/testcases/{id}` - Update test case
- `DELETE /api/testcases/{id}` - Delete test case

### Test Execution
- `POST /api/execution/testcase/{id}` - Execute single test
- `POST /api/execution/batch` - Execute test batch

### Schedules
- `GET /api/schedules` - List all schedules
- `POST /api/schedules` - Create new schedule
- `PUT /api/schedules/{id}/toggle` - Enable/disable schedule
- `POST /api/schedules/{id}/execute` - Execute schedule immediately

### Reports
- `POST /api/reports/generate-html/{batchId}` - Generate HTML report
- `POST /api/reports/generate-csv/{batchId}` - Generate CSV report
- `POST /api/reports/generate-xml/{batchId}` - Generate XML report
- `POST /api/reports/generate-allure/{batchId}` - Generate Allure report
- `GET /api/reports/download/{filename}` - Download report

### Analytics
- `GET /api/analytics/trends` - Get trend analysis
- `GET /api/analytics/regression-metrics` - Get regression metrics

## 🎯 Test Data Examples

### Web UI Test Data
```json
{
  "url": "https://example.com/login",
  "username": "testuser@example.com",
  "password": "password123",
  "expectedTitle": "Dashboard",
  "timeout": 10000
}
```

### API Test Data
```json
{
  "baseUrl": "https://api.example.com",
  "endpoint": "/users",
  "method": "POST",
  "headers": {
    "Content-Type": "application/json"
  },
  "body": {
    "name": "Test User",
    "email": "test@example.com"
  },
  "expectedStatus": 201
}
```

### AI Test Data
```json
{
  "modelEndpoint": "https://api.openai.com/v1/chat/completions",
  "prompt": "Explain quantum computing in simple terms",
  "expectedKeywords": ["quantum", "computing", "bits"],
  "maxTokens": 150,
  "temperature": 0.7
}
```

### Database Test Data
```json
{
  "connectionString": "jdbc:h2:mem:testdb",
  "queries": [
    "SELECT COUNT(*) FROM users WHERE active = true",
    "INSERT INTO test_data (name, value) VALUES ('test', 'value')"
  ],
  "expectedResults": [5, 1]
}
```

## 🎨 UI Features

### Dashboard
- **Real-time Statistics**: Live test case counts, execution statistics
- **Success Rate Tracking**: Visual success rate indicators
- **Recent Activity**: Latest test batch executions
- **API Status**: Real-time API connectivity status

### Advanced Filtering
- **Search Functionality**: Real-time search across test cases
- **Type Filtering**: Filter by test type (Web UI, API, AI, Database)
- **Environment Filtering**: Filter by environment (dev, staging, production)
- **Status Filtering**: Filter by active/inactive status

### Interactive Tables
- **Sortable Columns**: Click headers to sort data
- **Action Buttons**: Quick access to execute, edit, delete
- **Status Badges**: Visual status indicators
- **Responsive Design**: Adapts to different screen sizes

## 🔒 Security Features

- **CORS Configuration**: Proper cross-origin resource sharing
- **Input Validation**: Server-side validation for all inputs
- **Error Handling**: Comprehensive error handling and logging
- **Audit Logging**: Complete audit trail for all operations

## 🚀 Performance Features

- **Connection Pooling**: Optimized database connections
- **Async Processing**: Non-blocking test execution
- **Caching**: Intelligent caching for better performance
- **Auto-refresh**: Configurable auto-refresh intervals

## 🐛 Debugging Features

- **Detailed Logging**: Comprehensive logging at all levels
- **Error Screenshots**: Automatic screenshot capture on UI failures
- **Request/Response Dumps**: Complete API request/response logging
- **Step-by-step Execution**: Granular execution tracking

## 📝 Best Practices

### Test Case Organization
- Use descriptive names for test cases
- Group related tests into suites
- Maintain consistent naming conventions
- Document expected outcomes clearly

### Environment Management
- Use appropriate environments for different test phases
- Separate test data for each environment
- Configure environment-specific settings

### Scheduling Guidelines
- Use meaningful schedule names
- Set appropriate parallel thread counts
- Monitor scheduled executions regularly
- Use cron expressions that align with business needs

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Check the documentation
- Review the API endpoints
- Examine the test data examples

---

**AutoTest Framework v2.0** - Complete End-to-End Testing Solution
