# Test Automation Framework

A comprehensive Spring Boot-based test automation framework with TestNG, Selenium WebDriver, REST Assured, and a web dashboard for test management and reporting.

## 🚀 Features

- **Parallel Test Execution**: UI and API tests run in parallel with configurable thread counts
- **GitHub Actions CI/CD**: Automated test execution on push and nightly schedule
- **Web Dashboard**: Real-time test management, execution, and reporting interface
- **Multi-Browser Support**: Chrome, Firefox, and Edge browsers with headless mode
- **Comprehensive Reporting**: HTML, XML, JSON reports with screenshots
- **Database Integration**: Test results tracking with H2 (dev) and MySQL (prod)
- **Analytics Service**: Test execution metrics and trend analysis
- **File Upload**: Upload new test cases through the web interface

## 🏗️ Project Structure

```
├── .github/workflows/
│   └── Auto_checkup.yml          # GitHub Actions workflow
├── src/
│   ├── main/resources/
│   │   ├── static/               # CSS, JS, and static assets
│   │   ├── templates/            # Thymeleaf HTML templates
│   │   └── application.properties
│   └── test/java/org/automation/
│       ├── analytics/            # Analytics service and models
│       ├── api/                  # API test classes
│       ├── config/               # Configuration management
│       ├── dashboard/            # Web dashboard controllers
│       ├── drivers/              # WebDriver management
│       ├── listeners/            # TestNG listeners
│       ├── reports/              # Report generation utilities
│       ├── scheduler/            # Test execution scheduler
│       ├── ui/                   # UI test classes
│       └── utils/                # Utility classes
├── artifacts/                    # Generated reports and screenshots
├── config/                       # Configuration files
├── testng.xml                    # Main TestNG configuration
├── testng-api.xml               # API tests configuration
├── testng-ui.xml                # UI tests configuration
└── pom.xml                      # Maven dependencies and build config
```

## 🛠️ Setup and Installation

### Prerequisites

- Java 21 or higher
- Maven 3.6+
- Chrome/Firefox browsers (for UI tests)
- Git

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Springboard
   ```

2. **Install dependencies**
   ```bash
   mvn clean install
   ```

3. **Configure database** (Optional - H2 is used by default)
   ```properties
   # For MySQL in production
   spring.datasource.url=jdbc:mysql://localhost:3306/testdb
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

4. **Start the dashboard**
   ```bash
   mvn spring-boot:run
   ```

5. **Access the web dashboard**
   - Open browser: http://localhost:8080/dashboard

## 🧪 Running Tests

### Command Line Execution

**Run All Tests:**
```bash
mvn clean test
```

**Run UI Tests Only:**
```bash
mvn clean test -Dsuite=ui -Dbrowser=chrome -Dheadless=false
```

**Run API Tests Only:**
```bash
mvn clean test -Dsuite=api
```

**Run with specific browser:**
```bash
mvn clean test -Dsuite=ui -Dbrowser=firefox -Dheadless=true
```

### Web Dashboard Execution

1. Navigate to http://localhost:8080/dashboard/test-manager
2. Select test suite (UI/API/All)
3. Configure browser settings (for UI tests)
4. Click "Run Tests"
5. Monitor execution progress in real-time

## 🤖 GitHub Actions CI/CD

The project includes a comprehensive GitHub Actions workflow (`Auto_checkup.yml`) that:

### Triggers
- **Push events**: On main/develop branches (excludes automation commits)
- **Scheduled runs**: Nightly at 2 AM UTC
- **Manual dispatch**: With configurable test suite and browser options

### Features
- ✅ Parallel execution of UI and API tests
- ✅ Multi-browser testing (Chrome, Firefox)
- ✅ Artifact uploads (reports, screenshots)
- ✅ Analytics endpoint integration
- ✅ Consolidated reporting
- ✅ Failure notifications

### Workflow Jobs
1. **Setup**: Environment preparation and commit validation
2. **UI Tests**: Selenium tests across multiple browsers
3. **API Tests**: REST API validation tests
4. **Analytics Update**: Refresh cached metrics
5. **Report Consolidation**: Merge all test artifacts
6. **Notifications**: Alert on failures

### Required Secrets (Optional)
```yaml
ANALYTICS_ENDPOINT_URL: Your analytics endpoint
ANALYTICS_TOKEN: Authentication token
```

## 📊 Web Dashboard

### Dashboard Overview (`/dashboard`)
- **Real-time Statistics**: Total, passed, failed tests and success rate
- **Interactive Charts**: Test execution trends and result distribution
- **Recent Executions**: Latest test runs with status tracking
- **Auto-refresh**: Updates every 30 seconds

### Test Manager (`/dashboard/test-manager`)
- **Test Execution**: Run individual or suite tests
- **Browser Selection**: Chrome, Firefox, Edge with headless option
- **Progress Monitoring**: Real-time execution status
- **File Upload**: Add new test cases via web interface

### Reports (`/dashboard/reports`)
- **Multiple Formats**: HTML, XML, JSON reports
- **Screenshot Gallery**: Failed test screenshots with thumbnails
- **Download/View**: Direct access to all generated artifacts
- **Search & Filter**: Find specific reports quickly

## 📈 Test Reporting

### Generated Reports
- **TestNG HTML Reports**: Detailed test execution summaries
- **Extent Reports**: Rich HTML reports with charts and screenshots
- **Allure Reports**: Interactive test reports with trend analysis
- **Custom CSV/Excel**: Structured data for analysis
- **JSON API Results**: Detailed API test responses

### Screenshots
- **Failure Capture**: Automatic screenshots on UI test failures
- **Organized Storage**: Timestamped files in `/artifacts/screenshots/`
- **Web Gallery**: Browse screenshots through dashboard interface

## 🔧 Configuration

### TestNG Configuration
- **Parallel Execution**: Methods, classes, or tests level
- **Thread Management**: Configurable thread counts per suite
- **Listeners**: Custom listeners for reporting and analytics
- **Parameters**: Browser, headless mode, environment settings

### Maven Configuration
- **Java 21**: Modern Java features and performance
- **Optimized Memory**: G1GC and heap size configuration
- **Dependency Management**: Latest versions with security updates
- **Plugin Integration**: Surefire, Spring Boot, Allure plugins

### Spring Boot Configuration
- **Database**: H2 (development) / MySQL (production)
- **Web Server**: Embedded Tomcat on port 8080
- **Static Resources**: CSS, JS, and asset management
- **File Upload**: 10MB limit for test file uploads

## 🧩 Key Components

### TestNG Listeners
- **TestListener**: Main execution tracking and database logging
- **ScreenshotListener**: UI test failure screenshot capture
- **ApiReportListener**: API test result processing
- **ReportListener**: Custom report generation

### Analytics Service
- **Execution Tracking**: Comprehensive test run statistics
- **Trend Analysis**: Historical data and success rate trends
- **Real-time Metrics**: Live dashboard data updates
- **Export Capabilities**: Data export for external analysis

### Utility Classes
- **ScreenshotUtils**: Screenshot capture and management
- **DatabaseUtils**: Database operations and connections
- **ReportUtils**: Custom report generation
- **ExcelUtils**: Excel file operations for data-driven tests

## 🌟 Advanced Features

### Parallel Test Execution
```xml
<suite name="Parallel Suite" parallel="methods" thread-count="5">
  <!-- Tests run in parallel across multiple threads -->
</suite>
```

### Dynamic Test Configuration
```java
@Parameters({"browser", "headless", "environment"})
public void setupTest(String browser, boolean headless, String env) {
    // Dynamic test configuration
}
```

### Custom Test Annotations
```java
@Test(groups = {"smoke", "api"}, priority = 1)
@Description("Validate user authentication API")
public void testUserAuth() {
    // Test implementation
}
```

## 🚨 Troubleshooting

### Common Issues

**Browser Driver Issues:**
```bash
# Update WebDriverManager cache
mvn clean compile -Dwdm.forceDownload=true
```

**Memory Issues:**
```bash
# Increase heap size
export MAVEN_OPTS="-Xmx4g -XX:+UseG1GC"
```

**Database Connection:**
```bash
# Check H2 console at http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:testdb
```

**Port Conflicts:**
```properties
# Change server port in application.properties
server.port=9090
```

## 📝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-feature`)
3. Commit changes (`git commit -am 'Add new feature'`)
4. Push to branch (`git push origin feature/new-feature`)
5. Create a Pull Request

### Code Standards
- Follow Java coding conventions
- Write comprehensive tests
- Update documentation
- Use meaningful commit messages

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Support

For questions, issues, or contributions:
- Create an issue in the repository
- Review existing documentation
- Check troubleshooting section

## 🎯 Roadmap

- [ ] Docker containerization
- [ ] Kubernetes deployment
- [ ] Advanced reporting with ML insights
- [ ] Cross-platform mobile testing
- [ ] API contract testing
- [ ] Performance testing integration
- [ ] Security testing automation

---

**Last Updated**: October 2, 2024  
**Version**: 1.0.0  
**Maintainer**: Automation Team
=======
* Only API tests:

   mvn -DsuiteFile=testng-api.xml test


📊 Reports & Artifacts

After execution, reports and artifacts are generated in the artifacts/ directory:

Artifact Type	Location
HTML Reports	artifacts/reports/html/
CSV Reports	artifacts/reports/csv/
Excel Reports	artifacts/reports/excel/
JUnit Reports	target/surefire-reports/
Screenshots	artifacts/screenshots/
API Logs	artifacts/api/


🧠 Advanced Features

   * Database Result Storage: All test results are inserted into MySQL for further analytics and dashboards.

   * Traceability: Each test case links to a US ID and TC ID for tracking user stories.

   * Parallel Execution: Configurable thread count in testng.xml for high-speed parallel testing.

   * Scheduler: Use ParallelTestScheduler to trigger scheduled or automated test runs.


🛣️ Roadmap / Future Enhancements

   * 📈 Integration with CI/CD (Jenkins, GitHub Actions)

   * 📬 Email notifications for test reports

   * 📊 Dashboard for result visualization

   * 🧪 Support for cross-browser testing


✨ Author

AutomationFramework – Designed for scalable, traceable, and fully automated testing pipelines.
