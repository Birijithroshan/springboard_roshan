package org.automation.scheduler;

import org.automation.reports.CsvReportGenerator;
import org.automation.reports.ExcelReportGenerator;
import org.automation.reports.HtmlReportGenerator;
import org.testng.TestNG;
import org.testng.reporters.JUnitReportReporter;
import org.testng.reporters.XMLReporter;
import org.testng.reporters.EmailableReporter2;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ParallelTestScheduler {

    public static void main(String[] args) {

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter execution time (HH:mm:ss) → ");
            String inputTime = scanner.nextLine();

            LocalTime scheduledTime = LocalTime.parse(inputTime);
            LocalTime now = LocalTime.now();
            long delayInSeconds = Duration.between(now, scheduledTime).getSeconds();

            if (delayInSeconds > 0) {
                System.out.println("Scheduler will start in " + delayInSeconds + " seconds...");
                Thread.sleep(delayInSeconds * 1000);
            } else {
                System.out.println("Scheduled time is in the past. Running immediately...");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // -------------------- Create TestNG suite --------------------
        XmlSuite suite = new XmlSuite();
        suite.setName("AutomationFrameworkParallelSuite");
        suite.setParallel(XmlSuite.ParallelMode.TESTS);
        suite.setThreadCount(10);

        // -------------------- UI Tests --------------------
        XmlTest uiTest = new XmlTest(suite);
        uiTest.setName("BlazeDemo_UI_Tests");
        List<XmlClass> uiClasses = new ArrayList<>();
        uiClasses.add(new XmlClass("org.automation.ui.BlazeDemoTests"));
        uiTest.setXmlClasses(uiClasses);
        uiTest.setParallel(XmlSuite.ParallelMode.METHODS);
        uiTest.setThreadCount(10);

        // -------------------- API Tests --------------------
        XmlTest apiTest = new XmlTest(suite);
        apiTest.setName("JsonPlaceholder_API_Tests");
        List<XmlClass> apiClasses = new ArrayList<>();
        apiClasses.add(new XmlClass("org.automation.api.JsonPlaceholderTests"));
        apiTest.setXmlClasses(apiClasses);
        apiTest.setParallel(XmlSuite.ParallelMode.METHODS);
        apiTest.setThreadCount(10);

        // -------------------- Create TestNG instance --------------------
        TestNG testng = new TestNG();
        List<XmlSuite> suites = new ArrayList<>();
        suites.add(suite);
        testng.setXmlSuites(suites);

        // ✅ Set custom JUnit report output directory
        testng.setOutputDirectory("D:\\DevTools\\IdeaProject\\AutomationFramework\\artifacts\\j-unit");

        // ✅ Add reporters so that JUnit & TestNG XML reports are generated
        testng.addListener(new JUnitReportReporter());  // Generates JUnit-style XML
        testng.addListener(new XMLReporter());          // Generates TestNG default XML
        testng.addListener(new EmailableReporter2());   // Optional: HTML email report

        // -------------------- Run the suite --------------------
        System.out.println("🚀 Starting parallel test execution...");
        testng.run();

        // -------------------- Generate Custom Reports --------------------
        try {
            CsvReportGenerator.generateReport();
            ExcelReportGenerator.generateReport();
            HtmlReportGenerator.generateReport();
            System.out.println("✅ Reports generated successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error generating reports: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("✅ Suite Execution Finished: AutomationFrameworkSuite");
    }
}
