# Automation Testing Framework - Testing Process Documentation

## Overview
This document explains the testing process and methodology for the Automation Testing Framework, including real-world testing scenarios, data management, and best practices.

## Table of Contents
1. [Framework Architecture](#framework-architecture)
2. [Testing Process](#testing-process)
3. [Real-World Test Scenarios](#real-world-test-scenarios)
4. [Test Data Management](#test-data-management)
5. [Parallel Execution](#parallel-execution)
6. [Reporting and Analytics](#reporting-and-analytics)
7. [Scheduling and Automation](#scheduling-and-automation)
8. [Best Practices](#best-practices)

## Framework Architecture

### Database Layer
- **MySQL Database** with Flyway migrations for version control
- **Entities**: TestBatch, TestCase, TestExecution, TestSchedule, TestReports
- **Repositories**: Spring Data JPA repositories for data access
- **Services**: Business logic layer for test execution, reporting, and scheduling

### Application Layers
```
┌─────────────────────────────────────────┐
│              Web Layer                   │
│  (Controllers, REST APIs, Dashboard)    │
├─────────────────────────────────────────┤
│             Service Layer                │
│   (Business Logic, Test Execution)      │
├─────────────────────────────────────────┤
│           Repository Layer               │
│      (Data Access, JPA Entities)        │
├─────────────────────────────────────────┤
│            Database Layer                │
│     (MySQL with Flyway Migration)       │
└─────────────────────────────────────────┘
```

## Testing Process

### 1. Test Case Creation
Tests are organized into two main categories:

#### UI Tests (BlazeDemo)
- **Homepage Validation**: Verify page load and essential elements
- **Flight Search**: Test search functionality with different routes
- **Booking Workflow**: Complete end-to-end flight booking process
- **Form Validation**: Test input validation and error handling
- **Destination Verification**: Validate all available destinations

#### API Tests (ReqRes.in)
- **CRUD Operations**: Create, Read, Update, Delete user operations
- **Authentication**: Login and registration scenarios
- **Error Handling**: Test invalid requests and error responses
- **Performance**: Delayed response testing
- **Data Validation**: JSON schema validation

### 2. Test Execution Flow

```
Start Test Batch
    ↓
Initialize WebDriver/API Client
    ↓
Execute Test Cases (Parallel/Sequential)
    ↓
Capture Results, Screenshots, Logs
    ↓
Store Results in Database
    ↓
Generate Reports (HTML, CSV, XML)
    ↓
Send Notifications (if configured)
    ↓
Cleanup Resources
```

### 3. Test Data Management

#### BlazeDemo Test Data
```json
{
  "validBooking": {
    "fromCity": "Boston",
    "toCity": "London",
    "passenger": {
      "name": "John Doe",
      "address": "123 Test Street",
      "city": "Test City",
      "state": "Test State",
      "zipCode": "12345"
    },
    "payment": {
      "cardType": "Visa",
      "cardNumber": "4111111111111111",
      "expiryMonth": "12",
      "expiryYear": "2025",
      "nameOnCard": "John Doe"
    }
  }
}
```

#### ReqRes API Test Data
```json
{
  "createUser": {
    "name": "John Doe",
    "job": "QA Engineer"
  },
  "loginUser": {
    "email": "eve.holt@reqres.in",
    "password": "cityslicka"
  },
  "registerUser": {
    "email": "eve.holt@reqres.in",
    "password": "pistol"
  }
}
```

## Real-World Test Scenarios

### Scenario 1: Flight Booking E2E Test
**Purpose**: Validate complete customer journey from search to booking confirmation

**Steps**:
1. Navigate to BlazeDemo homepage
2. Select departure city (Boston) and destination (London)
3. Search for available flights
4. Select a flight from results
5. Fill passenger information
6. Enter payment details
7. Complete booking
8. Verify confirmation page

**Expected Results**:
- All form fields accept valid data
- Booking confirmation displays correctly
- Transaction details are accurate

### Scenario 2: API User Management
**Purpose**: Test complete user lifecycle through API

**Steps**:
1. Create new user via POST /api/users
2. Retrieve user details via GET /api/users/{id}
3. Update user information via PUT /api/users/{id}
4. Partially update via PATCH /api/users/{id}
5. Delete user via DELETE /api/users/{id}

**Expected Results**:
- All CRUD operations return correct status codes
- Response data matches request data
- Error scenarios handled appropriately

## Parallel Execution

### Configuration
The framework supports parallel execution at multiple levels:

1. **Test Suite Level**: Different test suites run in parallel
2. **Test Class Level**: Test classes within a suite run in parallel
3. **Test Method Level**: Individual test methods run in parallel

### Thread Management
```xml
<suite name="AutomationFrameworkSuite" parallel="tests" thread-count="5">
    <test name="UITests" parallel="methods" thread-count="3">
        <!-- UI tests with 3 threads -->
    </test>
    <test name="APITests" parallel="methods" thread-count="2">
        <!-- API tests with 2 threads -->
    </test>
</suite>
```

### Resource Isolation
- Each thread maintains its own WebDriver instance
- Database transactions are isolated
- Test data is thread-safe
- Screenshots and logs are thread-specific

## Reporting and Analytics

### Report Types
1. **HTML Report**: Interactive ExtentReports with charts and detailed logs
2. **CSV Report**: Tabular data for analysis and integration
3. **XML Report**: JUnit-compatible format for CI/CD integration

### Analytics Features
- Test execution trends over time
- Success/failure rate analysis
- Performance metrics and duration analysis
- Environment-specific results comparison

### Report Access
- **Web Dashboard**: `http://localhost:8080/automation-framework`
- **API Endpoints**:
  - List reports: `GET /api/reports/list`
  - View report: `GET /api/reports/view/{filename}`
  - Download report: `GET /api/reports/download/{filename}`

## Scheduling and Automation

### Cron-based Scheduling
Tests can be scheduled using cron expressions:

```java
@Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
@Scheduled(cron = "0 0 8 * * MON") // Weekly on Monday at 8 AM
@Scheduled(fixedRate = 60000) // Every minute
```

### Schedule Management
- Create, update, and manage test schedules via REST API
- Enable/disable schedules dynamically
- Configure notifications for scheduled executions
- Set parallel thread counts and timeouts

## Best Practices

### Test Design
1. **Page Object Model**: Separate page logic from test logic
2. **Data-Driven Testing**: Use external data sources for test parameters
3. **Independent Tests**: Each test should be able to run independently
4. **Proper Assertions**: Use meaningful assertions with clear messages

### Error Handling
1. **Screenshot Capture**: Automatically capture screenshots on failures
2. **Detailed Logging**: Log all important steps and decisions
3. **Retry Mechanism**: Implement intelligent retry for flaky tests
4. **Graceful Cleanup**: Ensure resources are properly cleaned up

### Performance
1. **Parallel Execution**: Utilize parallel execution for faster feedback
2. **Resource Management**: Properly manage WebDriver instances
3. **Database Optimization**: Use efficient queries and indexes
4. **Report Generation**: Generate reports asynchronously

### Maintenance
1. **Regular Updates**: Keep dependencies and drivers updated
2. **Code Reviews**: Regular peer reviews for test code
3. **Refactoring**: Regular refactoring to maintain code quality
4. **Documentation**: Keep documentation updated with changes

## API Endpoints Reference

### Test Execution
- `POST /api/execution/run` - Execute tests manually
- `GET /api/execution/status/{batchId}` - Get execution status
- `GET /api/execution/results/{batchId}` - Get execution results

### Reports
- `GET /api/reports/list` - List all available reports
- `GET /api/reports/view/{filename}` - View report in browser
- `GET /api/reports/download/{filename}` - Download report file
- `DELETE /api/reports/delete/{filename}` - Delete report file

### Scheduling
- `POST /api/schedules` - Create new schedule
- `GET /api/schedules` - List all schedules
- `PUT /api/schedules/{id}` - Update schedule
- `DELETE /api/schedules/{id}` - Delete schedule

### Analytics
- `GET /api/analytics/trends` - Get execution trends
- `GET /api/analytics/summary` - Get summary statistics
- `GET /api/analytics/performance` - Get performance metrics

## Troubleshooting

### Common Issues
1. **WebDriver Issues**: Ensure ChromeDriver is compatible with Chrome version
2. **Database Connection**: Verify MySQL server is running and accessible
3. **Port Conflicts**: Check if port 8080 is available
4. **Permission Issues**: Ensure write permissions for report directory

### Logging
Check application logs at: `logs/automation-framework.log`

### Support
For issues and questions, check the application dashboard at:
`http://localhost:8080/automation-framework`
