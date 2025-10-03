package org.automation.reports;

import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HtmlReportGenerator {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/automation_tests";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Ck@709136";

    public static void generateReport() throws Exception {
        // ✅ Unique timestamp for file name
        String timestampForFile = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "artifacts/reports/TestReport_" + timestampForFile + ".html";

        int passCount = 0, failCount = 0, skipCount = 0;
        StringBuilder tableRows = new StringBuilder();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM execution_log ORDER BY execution_time ASC")) {

            while (rs.next()) {
                String status = rs.getString("status");
                String statusClass = "status-skip";
                if ("PASS".equalsIgnoreCase(status)) statusClass = "status-pass";
                else if ("FAIL".equalsIgnoreCase(status)) statusClass = "status-fail";

                if ("PASS".equalsIgnoreCase(status)) passCount++;
                else if ("FAIL".equalsIgnoreCase(status)) failCount++;
                else skipCount++;

                // UI Screenshot
                String screenshotCell = "—";
                String screenshotPath = rs.getString("screenshot_path");
                if (screenshotPath != null && !screenshotPath.isEmpty()) {
                    File screenshotFile = new File(screenshotPath);
                    screenshotCell = screenshotFile.exists()
                            ? "<a class='screenshot-btn' href='../../" + screenshotPath + "' target='_blank'>📸 View Screenshot</a>"
                            : "<span class='no-screenshot'>⚠️ Not Found</span>";
                }

                // API Artifact
                String artifactCell = "—";
                String artifactPath = rs.getString("artifact");
                if (artifactPath != null && !artifactPath.isEmpty()) {
                    File artifactFile = new File(artifactPath);
                    artifactCell = artifactFile.exists()
                            ? "<a class='screenshot-btn' href='../../" + artifactPath + "' target='_blank'>📄 View API Artifact</a>"
                            : "<span class='no-screenshot'>⚠️ Not Found</span>";
                }

                tableRows.append("<tr>")
                        .append("<td>").append(rs.getInt("id")).append("</td>")
                        .append("<td>").append(rs.getString("test_name")).append("</td>")
                        .append("<td><span class='").append(statusClass).append("'>").append(status).append("</span></td>")
                        .append("<td>").append(rs.getString("test_type")).append("</td>")
                        .append("<td>").append(rs.getString("us_id")).append("</td>")
                        .append("<td>").append(rs.getString("tc_id")).append("</td>")
                        .append("<td>").append(rs.getString("execution_time")).append("</td>")
                        .append("<td>").append(screenshotCell).append("</td>")
                        .append("<td>").append(artifactCell).append("</td>")
                        .append("</tr>");
            }
        }

        int total = passCount + failCount + skipCount;
        double successRate = total == 0 ? 0 : ((double) passCount / total) * 100;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>");
            writer.write("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            writer.write("<title>Automation Test Report</title>");
            writer.write("<script src='https://cdn.jsdelivr.net/npm/chart.js'></script>");
            writer.write("<style>");
            writer.write("body { font-family: 'Segoe UI', sans-serif; margin: 0; background: linear-gradient(135deg, #141E30, #243B55); color: var(--text); transition: 0.4s; }");
            writer.write(":root { --text: #f5f7fa; --card-bg: rgba(255,255,255,0.08); --nav-bg: rgba(255,255,255,0.08); --border: rgba(255,255,255,0.2); }");
            writer.write("body.light { background: linear-gradient(135deg, #e0f7fa, #80deea); --text: #2c3e50; --card-bg: rgba(255,255,255,0.9); --nav-bg: #ffffffcc; --border: #dcdcdc; }");
            writer.write(".navbar { backdrop-filter: blur(10px); background: var(--nav-bg); border-bottom: 1px solid var(--border); padding: 15px 40px; display: flex; justify-content: space-between; align-items: center; color: var(--text); position: sticky; top: 0; z-index: 1000; }");
            writer.write(".navbar a { color: var(--text); margin: 0 15px; text-decoration: none; font-weight: 500; transition: 0.3s; }");
            writer.write(".navbar a:hover { color: #00e5ff; }");
            writer.write(".theme-toggle { cursor: pointer; padding: 8px 15px; border: 1px solid var(--border); border-radius: 20px; background: transparent; transition: 0.3s; }");
            writer.write(".theme-toggle:hover { background: #00e5ff33; }");
            writer.write(".container { max-width: 1400px; margin: auto; padding: 40px 30px; }");
            writer.write("h1 { text-align: center; font-size: 3rem; margin-bottom: 40px; background: linear-gradient(90deg,#00e5ff,#00ff99); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }");
            writer.write(".summary { display: flex; justify-content: space-around; flex-wrap: wrap; margin-bottom: 40px; }");
            writer.write(".card { background: var(--card-bg); border: 1px solid var(--border); border-radius: 20px; box-shadow: 0 8px 24px rgba(0,0,0,0.2); padding: 25px; margin: 10px; flex: 1 1 220px; text-align: center; transition: 0.4s; }");
            writer.write(".card:hover { transform: translateY(-8px) scale(1.03); box-shadow: 0 12px 28px rgba(0,255,255,0.4); }");
            writer.write(".card h2 { margin: 0; font-size: 2.8rem; color: #00e5ff; }");
            writer.write(".card p { margin-top: 8px; font-weight: 500; color: #ddd; }");
            writer.write("canvas { margin: 50px auto; display: block; max-width: 500px; }");
            writer.write(".search-bar { margin: 20px 0; text-align: right; }");
            writer.write(".search-bar input { padding: 10px; width: 300px; border: 1px solid var(--border); border-radius: 8px; background: var(--card-bg); color: var(--text); }");
            writer.write("table { width: 100%; border-collapse: collapse; margin-top: 20px; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.3); }");
            writer.write("th, td { padding: 14px 16px; text-align: center; }");
            writer.write("th { background: linear-gradient(90deg, #00e5ff, #00ff99); color: #fff; text-transform: uppercase; letter-spacing: 1px; }");
            writer.write("tr:nth-child(even) { background: rgba(255,255,255,0.05); }");
            writer.write("tr:hover { background: rgba(0,229,255,0.1); }");
            writer.write(".status-pass { background: #00c853; color: white; padding: 6px 14px; border-radius: 30px; font-weight: bold; }");
            writer.write(".status-fail { background: #d50000; color: white; padding: 6px 14px; border-radius: 30px; font-weight: bold; }");
            writer.write(".status-skip { background: #ff9100; color: white; padding: 6px 14px; border-radius: 30px; font-weight: bold; }");
            writer.write(".screenshot-btn { background: #00e5ff; padding: 6px 12px; color: #000; border-radius: 6px; text-decoration: none; font-weight: bold; display: inline-block; transition: 0.3s; }");
            writer.write(".screenshot-btn:hover { background: #00bcd4; color: #fff; transform: scale(1.05); }");
            writer.write(".no-screenshot { color: #ccc; font-style: italic; }");
            writer.write("footer { text-align: center; padding: 30px; margin-top: 60px; font-size: 0.9rem; color: #aaa; border-top: 1px solid var(--border); }");
            writer.write("</style></head><body>");

            // Navbar
            writer.write("<div class='navbar'><div><strong>🚀 Test Dashboard</strong></div>");
            writer.write("<div><a href='#summary'>Summary</a><a href='#charts'>Charts</a><a href='#results'>Results</a></div>");
            writer.write("<div class='theme-toggle' onclick='toggleTheme()'>🌓 Theme</div></div>");

            // Container
            writer.write("<div class='container'>");
            writer.write("<h1>Automation Test Execution Report</h1>");

            // Summary cards
            writer.write("<section id='summary'><div class='summary'>");
            writer.write("<div class='card'><h2>" + total + "</h2><p>Total Tests</p></div>");
            writer.write("<div class='card'><h2>" + passCount + "</h2><p>Passed</p></div>");
            writer.write("<div class='card'><h2>" + failCount + "</h2><p>Failed</p></div>");
            writer.write("<div class='card'><h2>" + skipCount + "</h2><p>Skipped</p></div>");
            writer.write("<div class='card'><h2>" + String.format("%.2f", successRate) + "%</h2><p>Success Rate</p></div>");
            writer.write("</div></section>");

            // Chart
            writer.write("<section id='charts'><canvas id='statusChart'></canvas></section>");
            writer.write("<script>");
            writer.write("new Chart(document.getElementById('statusChart').getContext('2d'), {");
            writer.write("type: 'doughnut', data: { labels: ['PASS','FAIL','SKIP'], datasets: [{");
            writer.write("data: [" + passCount + "," + failCount + "," + skipCount + "], backgroundColor: ['#00c853','#d50000','#ff9100'] }] },");
            writer.write("options: { responsive: true, plugins: { legend: { position: 'bottom' } }, animation: { animateScale: true } } });");
            writer.write("</script>");

            // Results table
            writer.write("<section id='results'>");
            writer.write("<div class='search-bar'><input type='text' id='searchInput' placeholder='🔍 Search by Test Name...' onkeyup='searchTable()'></div>");
            writer.write("<table id='resultTable'>");
            writer.write("<tr><th>ID</th><th>Test Name</th><th>Status</th><th>Type</th><th>US_ID</th><th>TC_ID</th><th>Execution Time</th><th>Screenshot</th><th>API Artifact</th></tr>");
            writer.write(tableRows.toString());
            writer.write("</table></section>");

            writer.write("<footer>📅 Generated on " + timestamp + " | ⚙️ Automation Framework © 2025</footer>");

            // Scripts
            writer.write("<script>");
            writer.write("function searchTable(){ var input=document.getElementById('searchInput').value.toLowerCase(); var rows=document.querySelectorAll('#resultTable tr'); for(var i=1;i<rows.length;i++){ var cells=rows[i].getElementsByTagName('td'); var match=false; for(var j=0;j<cells.length;j++){ if(cells[j].innerText.toLowerCase().includes(input)){ match=true; break; } } rows[i].style.display = match ? '' : 'none'; } }");
            writer.write("function toggleTheme(){ document.body.classList.toggle('light'); }");
            writer.write("</script>");

            writer.write("</div></body></html>");
        }

        System.out.println("✅ 📊 HTML Dashboard generated: " + fileName);
    }

    public static void generateHtmlReport() throws Exception {
        generateReport();
    }
}
