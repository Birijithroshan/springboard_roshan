# Automation Testing Framework - Comprehensive Guide

## Overview
A comprehensive Spring Boot-based automation testing framework that supports UI testing (BlazeDemo), API testing (ReqRes), parallel execution, reporting, and scheduled test runs with real-time performance monitoring.

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Chrome Browser (for UI tests)

### Installation & Setup
1. Clone the repository
2. Configure database in `application.properties`
3. Run the application:
```bash
mvn spring-boot:run
```
4. Access dashboard: `http://localhost:8080/automation-framework`

## 📊 Mock Real-World Dataset

### Test Cases Dataset
The framework includes realistic test scenarios for real-world applications:

#### BlazeDemo Flight Booking Tests
```json
{
  "test_scenarios": [
    {
      "name": "BlazeDemo Homepage Load Test",
      "type": "UI",
      "priority": "HIGH",
      "category": "smoke",
      "test_data": {
        "url": "https://blazedemo.com",
        "expectedTitle": "BlazeDemo",
        "loadTime": "< 3 seconds",
        "elements": ["fromPort", "toPort", "findFlights"]
      },
      "expected_results": {
        "page_load": "success",
        "elements_visible": true,
        "performance": "< 3000ms"
      }
    },
    {
      "name": "Flight Search Functionality",
      "type": "UI",
      "priority": "HIGH",
      "category": "functional",
      "test_data": {
        "fromCity": "Boston",
        "toCity": "London",
        "expectedResults": ">=1 flight option"
      },
      "expected_results": {
        "search_results": "displayed",
        "flight_options": "> 0",
        "booking_buttons": "enabled"
      }
    },
    {
      "name": "Complete Flight Booking E2E",
      "type": "UI",
      "priority": "CRITICAL",
      "category": "e2e",
      "test_data": {
        "passenger": {
          "name": "John Doe",
          "address": "123 Test Street",
          "city": "Test City",
          "zipCode": "12345"
        },
        "payment": {
          "cardType": "Visa",
          "cardNumber": "4111111111111111",
          "expiryMonth": "12",
          "expiryYear": "2025"
        }
      },
      "expected_results": {
        "booking_confirmation": "Thank you for your purchase today!",
        "confirmation_details": "displayed",
        "booking_id": "generated"
      }
    }
  ]
}
```

#### ReqRes API Testing Dataset
```json
{
  "api_test_scenarios": [
    {
      "name": "Get Users List API",
      "type": "API",
      "priority": "HIGH",
      "category": "api",
      "test_data": {
        "endpoint": "/api/users",
        "method": "GET",
        "parameters": {"page": 2},
        "expectedCount": 6
      },
      "expected_results": {
        "status_code": 200,
        "response_time": "< 1000ms",
        "data_count": 6,
        "pagination": true
      }
    },
    {
      "name": "Create User API",
      "type": "API",
      "priority": "HIGH",
      "category": "api",
      "test_data": {
        "endpoint": "/api/users",
        "method": "POST",
        "payload": {
          "name": "John Doe",
          "job": "QA Engineer"
        }
      },
      "expected_results": {
        "status_code": 201,
        "response_contains": ["name", "job", "id", "createdAt"],
        "user_created": true
      }
    },
    {
      "name": "User Authentication API",
      "type": "API",
      "priority": "CRITICAL",
      "category": "auth",
      "test_data": {
        "login_endpoint": "/api/login",
        "register_endpoint": "/api/register",
        "credentials": {
          "email": "eve.holt@reqres.in",
          "password": "cityslicka"
        }
      },
      "expected_results": {
        "login_status": 200,
        "token_received": true,
        "register_status": 200
      }
    }
  ]
}
```

### Performance Test Dataset
```json
{
  "performance_scenarios": [
    {
      "name": "Load Test - 100 Users",
      "type": "INTEGRATION",
      "priority": "MEDIUM",
      "category": "performance",
      "test_data": {
        "concurrent_users": 100,
        "duration": "5 minutes",
        "ramp_up": "30 seconds",
        "target_endpoints": [
          "https://blazedemo.com",
          "https://reqres.in/api/users"
        ]
      },
      "performance_thresholds": {
        "avg_response_time": "< 2000ms",
        "95th_percentile": "< 5000ms",
        "error_rate": "< 1%",
        "throughput": "> 10 TPS"
      }
    },
    {
      "name": "Stress Test - API Endpoints",
      "type": "INTEGRATION",
      "priority": "LOW",
      "category": "performance",
      "test_data": {
        "requests_per_second": 50,
        "duration": "10 minutes",
        "endpoints": ["/api/users", "/api/login", "/api/register"]
      },
      "performance_thresholds": {
        "max_response_time": "< 10000ms",
        "error_rate": "< 5%",
        "cpu_usage": "< 80%",
        "memory_usage": "< 2GB"
      }
    }
  ]
}
```

### Test Execution Results Dataset
```json
{
  "execution_batches": [
    {
      "batch_id": "SMOKE_2025090421",
      "batch_name": "Daily Smoke Tests",
      "status": "COMPLETED",
      "environment": "production",
      "execution_summary": {
        "total_tests": 15,
        "passed": 13,
        "failed": 2,
        "skipped": 0,
        "success_rate": "86.7%",
        "duration": "00:45:32"
      },
      "performance_metrics": {
        "avg_execution_time": "45.2 seconds",
        "parallel_threads": 3,
        "peak_memory": "512MB",
        "cpu_utilization": "45%"
      }
    },
    {
      "batch_id": "REGRESSION_2025090320",
      "batch_name": "Weekly Regression Suite",
      "status": "COMPLETED", 
      "environment": "staging",
      "execution_summary": {
        "total_tests": 45,
        "passed": 40,
        "failed": 4,
        "skipped": 1,
        "success_rate": "88.9%",
        "duration": "03:15:48"
      },
      "performance_metrics": {
        "avg_execution_time": "4.3 minutes",
        "parallel_threads": 5,
        "peak_memory": "1.2GB",
        "cpu_utilization": "65%"
      }
    }
  ]
}
```

### Scheduled Test Configuration Dataset
```json
{
  "test_schedules": [
    {
      "name": "Daily Smoke Tests",
      "cron_expression": "0 8 * * *",
      "description": "Run smoke tests every day at 8 AM",
      "test_suite": "smoke",
      "environment": "production",
      "parallel_threads": 3,
      "notification_enabled": true,
      "notification_emails": [
        "qa-team@company.com",
        "devops@company.com"
      ],
      "retry_configuration": {
        "enabled": true,
        "max_attempts": 2,
        "delay_between_retries": "5 minutes"
      }
    },
    {
      "name": "Weekly Regression",
      "cron_expression": "0 2 * * 0",
      "description": "Full regression suite every Sunday at 2 AM",
      "test_suite": "regression",
      "environment": "staging",
      "parallel_threads": 5,
      "notification_enabled": true,
      "timeout_minutes": 240
    },
    {
      "name": "API Health Check",
      "cron_expression": "0 */4 * * *",
      "description": "API health monitoring every 4 hours",
      "test_suite": "api",
      "environment": "production",
      "parallel_threads": 2,
      "notification_enabled": true
    }
  ]
}
```

## 🎯 Real-World Test Scenarios

### E-Commerce Testing (BlazeDemo)
The framework includes comprehensive e-commerce testing scenarios:

1. **User Journey Testing**
   - Homepage loading and navigation
   - Product search and filtering
   - Shopping cart functionality
   - Checkout process validation
   - Payment gateway integration

2. **Cross-Browser Testing**
   - Chrome, Firefox, Edge compatibility
   - Responsive design validation
   - Mobile viewport testing

3. **Performance Testing**
   - Page load time monitoring
   - Database query optimization
   - API response time tracking

### API Testing (ReqRes)
Comprehensive API testing coverage:

1. **CRUD Operations**
   - Create, Read, Update, Delete users
   - Data validation and error handling
   - Authentication and authorization

2. **Integration Testing**
   - Third-party API integration
   - Database connectivity
   - Service-to-service communication

3. **Security Testing**
   - Input validation
   - SQL injection prevention
   - Authentication bypass testing

## 📈 Performance Metrics & KPIs

### Dashboard Metrics
The framework tracks and displays:

```json
{
  "performance_kpis": {
    "test_execution": {
      "success_rate": "85-95%",
      "avg_execution_time": "< 60 seconds",
      "parallel_efficiency": "> 80%",
      "resource_utilization": "< 70%"
    },
    "system_performance": {
      "response_time": "< 2 seconds",
      "throughput": "> 100 requests/minute",
      "error_rate": "< 2%",
      "availability": "> 99.5%"
    },
    "quality_metrics": {
      "code_coverage": "> 80%",
      "defect_detection_rate": "> 90%",
      "false_positive_rate": "< 5%",
      "test_maintenance_ratio": "< 20%"
    }
  }
}
```

### Trending Analysis
Historical performance tracking:

- **Daily Trends**: Success rate, execution time, resource usage
- **Weekly Patterns**: Test stability, failure analysis, performance degradation
- **Monthly Reports**: Quality metrics, ROI analysis, improvement recommendations

## 🔧 Configuration Examples

### Environment-Specific Configurations

#### Production Environment
```properties
# Production Configuration
automation.framework.webDriver.headless=true
automation.framework.execution.maxParallelThreads=3
automation.framework.execution.defaultTimeout=120
automation.framework.reporting.captureScreenshots=false
automation.framework.api.connectionTimeout=15000
```

#### Staging Environment
```properties
# Staging Configuration
automation.framework.webDriver.headless=false
automation.framework.execution.maxParallelThreads=5
automation.framework.execution.defaultTimeout=300
automation.framework.reporting.captureScreenshots=true
automation.framework.api.connectionTimeout=30000
```

#### Development Environment
```properties
# Development Configuration
automation.framework.webDriver.headless=false
automation.framework.execution.maxParallelThreads=2
automation.framework.execution.enableRetry=true
automation.framework.execution.maxRetryAttempts=3
automation.framework.reporting.generateHtml=true
```

## 📊 Sample Reports

### HTML Report Structure
```html
<!DOCTYPE html>
<html>
<head>
    <title>Test Execution Report - SMOKE_2025090421</title>
</head>
<body>
    <div class="summary">
        <h1>Execution Summary</h1>
        <table>
            <tr><td>Total Tests:</td><td>15</td></tr>
            <tr><td>Passed:</td><td>13</td></tr>
            <tr><td>Failed:</td><td>2</td></tr>
            <tr><td>Success Rate:</td><td>86.7%</td></tr>
        </table>
    </div>
    <div class="charts">
        <!-- Interactive charts showing pass/fail trends -->
    </div>
    <div class="details">
        <!-- Detailed test case results with screenshots -->
    </div>
</body>
</html>
```

### CSV Report Format
```csv
Test Case,Status,Duration,Error Message,Screenshot,Environment
BlazeDemo Homepage Load,PASSED,2.3s,,screenshot_001.png,production
Flight Search Test,PASSED,4.1s,,screenshot_002.png,production
Booking Form Validation,FAILED,1.8s,Element not found,screenshot_003.png,production
```

## 🚀 API Endpoints

### Core Endpoints
- `GET /api/reports/list` - List all test reports
- `GET /api/reports/view/{filename}` - View specific report
- `POST /api/execution/run` - Trigger test execution
- `GET /api/analytics/trends` - Get performance trends
- `GET /api/validation/framework` - Framework health check

### Test Management
- `GET /api/testcases` - List all test cases
- `POST /api/testcases` - Create new test case
- `GET /api/schedules` - List scheduled tests
- `POST /api/schedules` - Create test schedule

## 🔍 Troubleshooting

### Common Issues
1. **Database Connection**: Verify MySQL is running and credentials are correct
2. **WebDriver Issues**: Ensure Chrome is installed and ChromeDriver is compatible
3. **Port Conflicts**: Check if port 8080 is available
4. **Memory Issues**: Increase JVM heap size for large test suites

### Performance Optimization
- Use parallel execution for faster test runs
- Implement data-driven testing for efficiency
- Configure appropriate timeouts for different environments
- Monitor resource usage and optimize accordingly

## 📞 Support

For technical support and feature requests:
- Documentation: `/automation-framework/`
- Health Check: `/automation-framework/api/validation/health`
- Logs: `logs/automation-framework.log`

---

**Framework Version**: 2.0  
**Last Updated**: September 4, 2025  
**Compatibility**: Java 17+, Spring Boot 3.1.5, TestNG 7.8.0
