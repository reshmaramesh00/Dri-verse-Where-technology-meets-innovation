package Dri_verse.view;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DashboardDAO {

    /** Training locations shown in the heatmap and the booking form. */
    public static final String[] LOCATIONS = {
        "MG Road Ground", "City Test Track", "Highway Rte 9",
        "Indoor Theory Hall", "Night Practice Zone", "Parking Yard B", "Mock Test Center"
    };

    public static class ModuleItem {
        public int moduleNumber;
        public String moduleTitle;
        public String note;
        public ModuleItem(int moduleNumber, String moduleTitle, String note) {
            this.moduleNumber = moduleNumber;
            this.moduleTitle = moduleTitle;
            this.note = note;
        }
    }

    public static class TestResultItem {
        public String studentName;
        public String testName;
        public double score;
        public int attempts;
        public TestResultItem(String studentName, String testName, double score, int attempts) {
            this.studentName = studentName;
            this.testName = testName;
            this.score = score;
            this.attempts = attempts;
        }
    }

    public static class ClassItem {
        public String date, time, instructor, vehicle, type, location;
    }

    public static class DashboardData {
        public int classesEnrolled = 0;
        public int upcomingClassesCount = 0;
        public int testsCompleted = 0;
        public double paymentsTotal = 0;
        public String nextClassTitle = "No upcoming class";
        public String nextClassTime = "";
        public String nextClassLocation = "";
        public String nextClassInstructor = "";
        public int theoryPct = 0;
        public int practicalPct = 0;
        public int mockTestPct = 0;
        public int assignmentPct = 0;
        public String learnerName = "Learner";

        public List<ModuleItem> modules = new ArrayList<>();
        public List<TestResultItem> testResults = new ArrayList<>();
        public List<ClassItem> upcomingClasses = new ArrayList<>();
        public int[][] locationLoad = new int[LOCATIONS.length][9];   // [location][week]
    }

    public DashboardData loadForUser(int userId) {
        DashboardData data = new DashboardData();
        try (Connection conn = DBConnection.getConnection()) {

            // ---- Name ----
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT CONCAT(first_name, ' ', last_name) AS full_name " +
                    "FROM users WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        data.learnerName = rs.getString("full_name");
                    }
                }
            } catch (Exception e) {
                log("name", e);
            }

            // ---- Counts ----
            data.classesEnrolled = scalar(conn,
                    "SELECT COUNT(*) FROM class_enrollments WHERE user_id = ?", userId);

            // Upcoming count now comes from class_schedule (the "? > 0" just keeps scalar() working)
            data.upcomingClassesCount = scalar(conn,
                    "SELECT COUNT(*) FROM class_schedule WHERE status = 'Scheduled' " +
                    "AND class_date >= CURDATE() AND ? > 0", userId);

            data.testsCompleted = scalar(conn,
                    "SELECT COUNT(*) FROM mock_test_results WHERE learner_id = ?", userId);

            // ---- Payments (paid only) ----
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COALESCE(SUM(amount),0) AS total FROM payments " +
                    "WHERE learner_id = ? AND payment_status = 'PAID'")) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) data.paymentsTotal = rs.getDouble("total");
            } catch (Exception e) { log("payments", e); }

            // ---- Next class ----
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT title, class_time, location, instructor FROM upcoming_classes " +
                    "WHERE user_id = ? AND class_date >= CURDATE() ORDER BY class_date ASC LIMIT 1")) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    data.nextClassTitle = rs.getString("title");
                    data.nextClassTime = rs.getString("class_time");
                    data.nextClassLocation = rs.getString("location");
                    data.nextClassInstructor = rs.getString("instructor");
                }
            } catch (Exception e) { log("next class", e); }

            // ---- Progress ----
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT theory_pct, practical_pct, mock_test_pct, assignment_pct " +
                    "FROM progress WHERE user_id = ?")) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    data.theoryPct = rs.getInt("theory_pct");
                    data.practicalPct = rs.getInt("practical_pct");
                    data.mockTestPct = rs.getInt("mock_test_pct");
                    data.assignmentPct = rs.getInt("assignment_pct");
                }
            } catch (Exception e) { log("progress", e); }

            // ---- Upcoming modules ----
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT module_number, module_title, note FROM learner_modules " +
                    "WHERE user_id = ? AND status = 'PENDING' ORDER BY module_number ASC LIMIT 4")) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    data.modules.add(new ModuleItem(
                            rs.getInt("module_number"),
                            rs.getString("module_title"),
                            rs.getString("note")));
                }
            } catch (Exception e) { log("modules", e); }

            // ---- Recent test results ----
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT category AS test_name, " +
                    "score_percentage AS score, " +
                    "completed_at " +
                    "FROM mock_test_results " +
                    "WHERE learner_id = ? " +
                    "ORDER BY completed_at DESC LIMIT 5")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        data.testResults.add(new TestResultItem(
                                data.learnerName,
                                rs.getString("test_name"),
                                rs.getDouble("score"),
                                1));
                    }
                }
            } catch (Exception e) {
                log("test results", e);
            }

            // ---- Upcoming classes (from class_schedule) ----
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT class_date, class_time, instructor, vehicle, class_type, " +
                    "COALESCE(location,'Not set') AS location FROM class_schedule " +
                    "WHERE status = 'Scheduled' AND (class_date > CURDATE() " +
                    "OR (class_date = CURDATE() AND class_time >= CURTIME())) " +
                    "ORDER BY class_date, class_time LIMIT 5");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ClassItem c = new ClassItem();
                    c.date = rs.getDate("class_date").toLocalDate()
                            .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"));
                    String t = String.valueOf(rs.getTime("class_time"));
                    c.time = t.length() > 5 ? t.substring(0, 5) : t;
                    c.instructor = rs.getString("instructor");
                    c.vehicle = rs.getString("vehicle");
                    c.type = rs.getString("class_type");
                    c.location = rs.getString("location");
                    data.upcomingClasses.add(c);
                }
            } catch (Exception e) { log("upcoming classes", e); }

            // ---- Training load: classes per location per week (4 weeks back, 4 ahead) ----
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT location, FLOOR(DATEDIFF(class_date, CURDATE() - INTERVAL 28 DAY) / 7) AS wk, " +
                    "COUNT(*) AS n FROM class_schedule " +
                    "WHERE status <> 'Cancelled' AND location IS NOT NULL " +
                    "AND class_date >= CURDATE() - INTERVAL 28 DAY " +
                    "AND class_date < CURDATE() + INTERVAL 35 DAY " +
                    "GROUP BY location, wk");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String loc = rs.getString("location");
                    int wk = rs.getInt("wk");
                    for (int i = 0; i < LOCATIONS.length; i++) {
                        if (LOCATIONS[i].equals(loc) && wk >= 0 && wk < 9)
                            data.locationLoad[i][wk] = rs.getInt("n");
                    }
                }
            } catch (Exception e) { log("training load", e); }

        } catch (Exception e) {
            System.out.println("DashboardDAO: could not connect (" + e.getMessage() + ")");
        }

        return data;
    }

    private int scalar(Connection conn, String sql, int id) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { log(sql, e); }
        return 0;
    }

    private void log(String what, Exception e) {
        System.out.println("DashboardDAO [" + what + "]: " + e.getMessage());
    }
}