# 🧪 AutomationFramework

A robust and scalable Java-based automation testing framework designed for **parallel execution** of **UI** and **API** test suites.  
It integrates powerful tools like **Selenium WebDriver**, **REST Assured**, and **TestNG**, while enabling **traceability**, **artifact storage**, and **report generation** — all in one place.

---

## 🚀 Features

- ⚡ **Parallel Execution** – Run multiple UI & API tests simultaneously to reduce execution time.
- 🗄️ **Database Integration** – Store execution logs, test data, and results directly in **MySQL**.
- 📊 **Comprehensive Reporting** – Automatically generate **HTML**, **CSV**, **Excel**, and **JUnit** reports.
- 📁 **Artifact Storage** – Save screenshots, logs, and API request/response payloads for every test run.
- ⏱️ **Scheduling Support** – Schedule tests with the built-in `ParallelTestScheduler`.
- 🧩 **Traceability Support** – Track tests with **US ID** and **Test Case ID** mapping.
- 🧪 **End-to-End Testing** – Includes **10 UI tests** for [BlazeDemo](https://blazedemo.com/) and **10 API tests** using [ReqRes](https://reqres.in/) / [JSONPlaceholder](https://jsonplaceholder.typicode.com/).
- 🛠️ **Tech Stack** – Java, Selenium, REST Assured, TestNG, MySQL.

---

## 📁 Project Structure

AutomationFramework/
├── artifacts/ # Stores execution artifacts
│ ├── api/ # API request & response data
│ ├── reports/ # HTML, CSV, Excel, JUnit reports
│ └── screenshots/ # Captured screenshots
├── config/
│ └── db.properties # Database configuration
├── drivers/ # WebDriver executables (if needed)
├── src/
│ └── main/java/
│ └── org/automation/
│ ├── listeners/ # TestNG listeners for reporting & execution events
│ ├── reports/ # Report generators (HTML, CSV, Excel)
│ ├── scheduler/ # Parallel execution scheduler
│ ├── ui/ # UI test classes and utilities
│ └── utils/ # Utility classes (DB, Excel, Reports, Screenshots)
└── test/
└── java/org/automation/
├── api/ # API test classes and base classes
├── config/ # Config managers
└── drivers/ # WebDriver factory setup


---

## 🧪 Test Types

### 🌐 UI Tests
- Built with **Selenium WebDriver**
- Includes 10 comprehensive tests for the BlazeDemo flight booking site.
- Supports screenshot capture, artifact logging, and reporting.

### 🔗 API Tests
- Built with **REST Assured**
- Includes 10 API tests using **ReqRes** / **JSONPlaceholder**
- Request/Response data is stored as artifacts.

---

## ⚙️ Technologies Used

- **Language:** Java
- **Test Framework:** TestNG
- **UI Testing:** Selenium WebDriver
- **API Testing:** REST Assured
- **Database:** MySQL
- **Build Tool:** Maven
- **Reports:** HTML, CSV, Excel, JUnit

---

## 📦 Setup & Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/AutomationFramework.git
   cd AutomationFramework


2. Configure Database:

   * Update config/db.properties with your MySQL credentials.

   * Make sure the database automation_tests exists.

3. Build the project:

    mvn clean install


4. Run Tests:

  * All tests:

     mvn test


  * Only UI tests:

    mvn -DsuiteFile=testng-ui.xml test


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


🤝 Contributing

Contributions are welcome! Please fork this repository, create a feature branch, and submit a pull request.


📜 License

This project is licensed under the MIT License.
Feel free to use and modify it for your automation needs.


✨ Author

AutomationFramework – Designed for scalable, traceable, and fully automated testing pipelines.


License

MIT License © 2025 [Chandrakant Kumar]

Contact

For questions or support, contact: [chandrakant2522006@gmail.com]