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
        String timestampForFile = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "artifacts/reports/TestReport_" + timestampForFile + ".html";

        int passCount = 0, failCount = 0, skipCount = 0;
        int total = 0;
        double successRate = 0;
        StringBuilder overallRows = new StringBuilder();

        StringBuilder weeksArrayJs = new StringBuilder();
        StringBuilder weeklySummaryJs = new StringBuilder();
        StringBuilder weeklyDetailsJs = new StringBuilder();
        StringBuilder overallTrendJs = new StringBuilder();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM execution_log ORDER BY execution_time DESC")) {
                boolean hasData = false;
                while (rs.next()) {
                    hasData = true;
                    String status = rs.getString("status");
                    if ("PASS".equalsIgnoreCase(status)) passCount++;
                    else if ("FAIL".equalsIgnoreCase(status)) failCount++;
                    else skipCount++;

                    String screenshotCell = "—";
                    String screenshotPath = rs.getString("screenshot_path");
                    if (screenshotPath != null && !screenshotPath.isEmpty()) {
                        File screenshotFile = new File(screenshotPath);
                        screenshotCell = screenshotFile.exists()
                                ? "<a class='screenshot-btn' href='../../" + escapeHtml(screenshotPath) + "' target='_blank'>📸</a>"
                                : "<span class='no-screenshot'>⚠️</span>";
                    }

                    String artifactCell = "—";
                    String artifactPath = rs.getString("artifact");
                    if (artifactPath != null && !artifactPath.isEmpty()) {
                        File artifactFile = new File(artifactPath);
                        artifactCell = artifactFile.exists()
                                ? "<a class='screenshot-btn' href='../../" + escapeHtml(artifactPath) + "' target='_blank'>📄</a>"
                                : "<span class='no-screenshot'>⚠️</span>";
                    }

                    String statusClass = statusBadgeClass(status);
                    overallRows.append("<tr>")
                            .append("<td>").append(rs.getInt("id")).append("</td>")
                            .append("<td>").append(escapeHtml(rs.getString("test_name"))).append("</td>")
                            .append("<td><span class='").append(statusClass).append("'>").append(escapeHtml(status)).append("</span></td>")
                            .append("<td>").append(escapeHtml(rs.getString("test_type"))).append("</td>")
                            .append("<td>").append(escapeHtml(rs.getString("us_id"))).append("</td>")
                            .append("<td>").append(escapeHtml(rs.getString("tc_id"))).append("</td>")
                            .append("<td>").append(escapeHtml(rs.getString("execution_time"))).append("</td>")
                            .append("<td>").append(screenshotCell).append("</td>")
                            .append("<td>").append(artifactCell).append("</td>")
                            .append("</tr>");
                }
                if (!hasData) overallRows.append("<tr><td colspan='9'>No records found</td></tr>");
            }

            total = passCount + failCount + skipCount;
            successRate = total == 0 ? 0 : ((double) passCount / total) * 100;

            String weekListQuery = "SELECT YEAR(execution_time) AS yr, WEEK(execution_time,1) AS wk, " +
                    "MIN(DATE(execution_time)) AS week_start, MAX(DATE(execution_time)) AS week_end " +
                    "FROM execution_log GROUP BY yr, wk ORDER BY week_start DESC";

            try (Statement weekStmt = conn.createStatement(); ResultSet weekListRs = weekStmt.executeQuery(weekListQuery)) {
                weeksArrayJs.append("[");
                weeklySummaryJs.append("{");
                weeklyDetailsJs.append("{");
                overallTrendJs.append("{");

                boolean firstWeek = true;
                while (weekListRs.next()) {
                    int yr = weekListRs.getInt("yr");
                    int wk = weekListRs.getInt("wk");
                    String weekId = yr + "_" + wk;
                    String weekStart = weekListRs.getString("week_start");
                    String weekEnd = weekListRs.getString("week_end");

                    if (!firstWeek) weeksArrayJs.append(",");
                    weeksArrayJs.append("{\"id\":\"").append(escapeJs(weekId)).append("\",\"label\":\"")
                            .append(escapeJs("Week " + wk + " (" + weekStart + " → " + weekEnd + ")")).append("\",\"start\":\"")
                            .append(escapeJs(weekStart)).append("\",\"end\":\"").append(escapeJs(weekEnd)).append("\"}");

                    // Weekly summary
                    String summaryQuery = "SELECT status, COUNT(*) AS cnt FROM execution_log " +
                            "WHERE YEAR(execution_time)=" + yr + " AND WEEK(execution_time,1)=" + wk + " GROUP BY status";
                    int wPass = 0, wFail = 0, wSkip = 0;
                    try (Statement sumStmt = conn.createStatement(); ResultSet sumRs = sumStmt.executeQuery(summaryQuery)) {
                        while (sumRs.next()) {
                            String s = sumRs.getString("status");
                            int c = sumRs.getInt("cnt");
                            if ("PASS".equalsIgnoreCase(s)) wPass = c;
                            else if ("FAIL".equalsIgnoreCase(s)) wFail = c;
                            else wSkip += c;
                        }
                    }
                    int wTotal = wPass + wFail + wSkip;

                    if (!firstWeek) weeklySummaryJs.append(",");
                    weeklySummaryJs.append("\"").append(escapeJs(weekId)).append("\":{\"pass\":").append(wPass)
                            .append(",\"fail\":").append(wFail).append(",\"skip\":").append(wSkip).append(",\"total\":").append(wTotal).append("}");

                    // Weekly details
                    String detailQuery = "SELECT id, test_name, status, test_type, us_id, tc_id, execution_time, screenshot_path, artifact " +
                            "FROM execution_log WHERE YEAR(execution_time)=" + yr + " AND WEEK(execution_time,1)=" + wk + " ORDER BY execution_time DESC";
                    try (Statement detStmt = conn.createStatement(); ResultSet detRs = detStmt.executeQuery(detailQuery)) {
                        StringBuilder detailArray = new StringBuilder();
                        detailArray.append("[");
                        boolean firstDet = true;
                        while (detRs.next()) {
                            if (!firstDet) detailArray.append(",");
                            detailArray.append("{")
                                    .append("\"id\":\"").append(detRs.getInt("id")).append("\",")
                                    .append("\"test_name\":\"").append(escapeJs(detRs.getString("test_name"))).append("\",")
                                    .append("\"status\":\"").append(escapeJs(detRs.getString("status"))).append("\",")
                                    .append("\"test_type\":\"").append(escapeJs(detRs.getString("test_type"))).append("\",")
                                    .append("\"us_id\":\"").append(escapeJs(detRs.getString("us_id"))).append("\",")
                                    .append("\"tc_id\":\"").append(escapeJs(detRs.getString("tc_id"))).append("\",")
                                    .append("\"execution_time\":\"").append(escapeJs(detRs.getString("execution_time"))).append("\",")
                                    .append("\"screenshot\":\"").append(escapeJs(detRs.getString("screenshot_path"))).append("\",")
                                    .append("\"artifact\":\"").append(escapeJs(detRs.getString("artifact"))).append("\"")
                                    .append("}");
                            firstDet = false;
                        }
                        detailArray.append("]");
                        if (!firstWeek) weeklyDetailsJs.append(",");
                        weeklyDetailsJs.append("\"").append(escapeJs(weekId)).append("\":").append(detailArray.toString());
                    }

                    // Overall trend
                    if (!firstWeek) overallTrendJs.append(",");
                    overallTrendJs.append("\"").append(escapeJs(weekId)).append("\":{\"pass\":").append(wPass)
                            .append(",\"fail\":").append(wFail).append(",\"skip\":").append(wSkip).append("}");

                    firstWeek = false;
                }
                weeksArrayJs.append("]");
                weeklySummaryJs.append("}");
                weeklyDetailsJs.append("}");
                overallTrendJs.append("}");
            }
        }

        // Write HTML
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("<!doctype html><html lang='en'><head><meta charset='utf-8'>");
            writer.write("<meta name='viewport' content='width=device-width, initial-scale=1'>");
            writer.write("<title>Automation Test Report</title>");
            writer.write("<script src='https://cdn.jsdelivr.net/npm/chart.js'></script>");
            // Professional, colorful CSS
            writer.write("<style>" +
                    "body{font-family:Arial,sans-serif;background:#f5f6fa;margin:0;padding:0;}" +
                    "h1,h2{color:#2f3640;}" +
                    ".container{padding:20px;max-width:1200px;margin:auto;}" +
                    ".card-grid{display:flex;flex-wrap:wrap;gap:15px;margin-bottom:20px;}" +
                    ".card{background:#ffffff;padding:15px;border-radius:10px;flex:1 1 150px;text-align:center;box-shadow:0 2px 10px rgba(0,0,0,0.1);}" +
                    ".charts{display:flex;justify-content:center;margin-bottom:20px;}" +
                    ".chart-card{background:#fff;padding:15px;border-radius:10px;box-shadow:0 2px 10px rgba(0,0,0,0.1);max-width:450px;width:100%;}" +
                    "table{width:100%;border-collapse:collapse;margin-top:20px;background:#fff;border-radius:10px;overflow:hidden;box-shadow:0 2px 10px rgba(0,0,0,0.1);}" +
                    "th,td{padding:10px;text-align:center;border-bottom:1px solid #ddd;}" +
                    "th{background:#4cd137;color:#fff;}" +
                    "tr:hover{background:#f1f2f6;}" +
                    ".status-pass{color:#00c853;font-weight:bold;}" +
                    ".status-fail{color:#d50000;font-weight:bold;}" +
                    ".status-skip{color:#ff9100;font-weight:bold;}" +
                    "select.select{padding:8px;margin-bottom:15px;border-radius:5px;border:1px solid #ccc;}" +
                    "footer{text-align:center;margin:20px 0;color:#636e72;}" +
                    "</style>");

            writer.write("</head><body><div class='container'>");

            // Header + Summary Cards
            writer.write("<h1>Automation Test Execution Report</h1>");
            writer.write("<div class='card-grid'>");
            writer.write("<div class='card'><h2>" + total + "</h2><p>Total Tests</p></div>");
            writer.write("<div class='card'><h2>" + passCount + "</h2><p>Passed</p></div>");
            writer.write("<div class='card'><h2>" + failCount + "</h2><p>Failed</p></div>");
            writer.write("<div class='card'><h2>" + skipCount + "</h2><p>Skipped</p></div>");
            writer.write("<div class='card'><h2>" + String.format("%.2f", successRate) + "%</h2><p>Success Rate</p></div>");
            writer.write("</div>");

            // Overall doughnut chart
            writer.write("<div class='charts'><div class='chart-card'><canvas id='overallDoughChart' width='350' height='350'></canvas></div></div>");

            // Weekly analysis selector + cards + chart
            writer.write("<section id='weekly'><div class='section-title'><h2>Weekly Analysis</h2><select class='select' id='weekSelector'></select></div>");
            writer.write("<div class='card-grid' id='weeklyCards'></div>");
            writer.write("<div class='charts'><div class='chart-card'><canvas id='weeklyDoughChart' width='350' height='350'></canvas></div></div>");

            // Results Table
            writer.write("<section id='results'><table id='resultsTable'><thead><tr>" +
                    "<th>ID</th><th>Test Name</th><th>Status</th><th>Type</th><th>US ID</th><th>TC ID</th><th>Execution Time</th><th>Screenshot</th><th>Artifact</th>" +
                    "</tr></thead><tbody>");
            writer.write(overallRows.toString());
            writer.write("</tbody></table></section>");

            // JS for charts, week selection, table update
            writer.write("<script>");
            writer.write("const weeks=" + weeksArrayJs.toString() + ";");
            writer.write("const weeklySummary=" + weeklySummaryJs.toString() + ";");
            writer.write("const weeklyDetails=" + weeklyDetailsJs.toString() + ";");

            writer.write(
                    "const weekSelect=document.getElementById('weekSelector');" +
                            "const weeklyCardsDiv=document.getElementById('weeklyCards');" +
                            "const resultsTable=document.getElementById('resultsTable').getElementsByTagName('tbody')[0];" +
                            "weeks.forEach(w=>{const opt=document.createElement('option');opt.value=w.id;opt.textContent=w.label;weekSelect.appendChild(opt);});" +
                            "const overallCtx=document.getElementById('overallDoughChart').getContext('2d');" +
                            "const overallDoughChart=new Chart(overallCtx,{type:'doughnut',data:{labels:['PASS','FAIL','SKIP'],datasets:[{data:[" + passCount + "," + failCount + "," + skipCount + "],backgroundColor:['#00c853','#d50000','#ff9100']}]},options:{responsive:true,plugins:{legend:{position:'bottom'}}}});" +
                            "const weeklyCtx=document.getElementById('weeklyDoughChart').getContext('2d');" +
                            "let weeklyDoughChart=new Chart(weeklyCtx,{type:'doughnut',data:{labels:['PASS','FAIL','SKIP'],datasets:[{data:[0,0,0],backgroundColor:['#00c853','#d50000','#ff9100']}]},options:{responsive:true,plugins:{legend:{position:'bottom'}}}});" +
                            "function updateWeeklyView(weekId){const data=weeklySummary[weekId];if(!data)return;" +
                            "weeklyDoughChart.data.datasets[0].data=[data.pass,data.fail,data.skip];" +
                            "weeklyDoughChart.update();" +
                            "weeklyCardsDiv.innerHTML='';['Total','Pass','Fail','Skip'].forEach(k=>{let val=k==='Total'?data.total:k==='Pass'?data.pass:k==='Fail'?data.fail:data.skip;" +
                            "const card=document.createElement('div');card.className='card';card.innerHTML='<h2>'+val+'</h2><p>'+k+'</p>';weeklyCardsDiv.appendChild(card);});" +
                            "const details=weeklyDetails[weekId]||[];resultsTable.innerHTML='';" +
                            "if(details.length===0){resultsTable.innerHTML='<tr><td colspan=\"9\">No records found</td></tr>';} else {details.forEach(r=>{const row=document.createElement('tr');" +
                            "row.innerHTML='<td>'+r.id+'</td><td>'+r.test_name+'</td><td><span class=\"'+(r.status==='PASS'?'status-pass':r.status==='FAIL'?'status-fail':'status-skip')+'\">'+r.status+'</span></td><td>'+r.test_type+'</td><td>'+r.us_id+'</td><td>'+r.tc_id+'</td><td>'+r.execution_time+'</td><td>'+(r.screenshot?'<a href=\"../../'+r.screenshot+'\" target=\"_blank\">📸</a>':'—')+'</td><td>'+(r.artifact?'<a href=\"../../'+r.artifact+'\" target=\"_blank\">📄</a>':'—')+'</td>';resultsTable.appendChild(row);});}}" +
                            "weekSelect.addEventListener('change',e=>updateWeeklyView(e.target.value));if(weeks.length>0)updateWeeklyView(weeks[0].id);"
            );

            writer.write("</script>");
            writer.write("<footer>Generated on " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</footer>");
            writer.write("</div></body></html>");
        }

        System.out.println("✅ HTML Dashboard generated: " + fileName);
    }

    private static String statusBadgeClass(String status) {
        switch (status.toUpperCase()) {
            case "PASS": return "status-pass";
            case "FAIL": return "status-fail";
            case "SKIP": return "status-skip";
            default: return "";
        }
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");
    }

    private static String escapeJs(String s) {
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("'","\\'").replace("\n","\\n").replace("\r","\\r");
    }
}
