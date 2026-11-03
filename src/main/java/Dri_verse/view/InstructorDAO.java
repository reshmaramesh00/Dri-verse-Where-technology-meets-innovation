package Dri_verse.view;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/** Loads an instructor's dashboard data. Everything defaults to 0 / empty. */
public class InstructorDAO {

    public static class ClassItem {
        public String title, when, location;
        public int students;
    }

    public static class StudentItem {
        public String name;
        public int progress;
    }

    public static class InstructorData {
        public String instructorName = "Instructor";
        public int totalStudents = 0;
        public int classesToday = 0;
        public int upcomingClasses = 0;
        public int avgProgress = 0;
        public int pendingReviews = 0;
        public List<ClassItem> classes = new ArrayList<>();
        public List<StudentItem> students = new ArrayList<>();
    }

    /** Used by the learner dashboard's Instructors tab. */
    public static class InstructorInfo {
        public int id;
        public String name;
        public int totalStudents;
        public int upcomingClasses;
    }

    public InstructorData loadForUser(int userId) {
        InstructorData d = new InstructorData();
        try (Connection conn = DBConnection.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT CONCAT(first_name, ' ', last_name) AS full_name FROM users WHERE user_id = ?")) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) d.instructorName = rs.getString("full_name");
            } catch (Exception e) { log("name", e); }

            d.totalStudents = scalar(conn,
                    "SELECT COUNT(*) FROM instructor_students WHERE instructor_id = ?", userId);

            d.classesToday = scalar(conn,
                    "SELECT COUNT(*) FROM instructor_classes WHERE instructor_id = ? AND class_date = CURDATE()", userId);

            d.upcomingClasses = scalar(conn,
                    "SELECT COUNT(*) FROM instructor_classes WHERE instructor_id = ? AND class_date >= CURDATE()", userId);

            d.avgProgress = scalar(conn,
                    "SELECT COALESCE(AVG((p.theory_pct + p.practical_pct + p.mock_test_pct + p.assignment_pct) / 4), 0) " +
                    "FROM instructor_students s JOIN progress p ON p.user_id = s.student_id " +
                    "WHERE s.instructor_id = ?", userId);

            d.pendingReviews = scalar(conn,
                    "SELECT COUNT(*) FROM mock_tests m JOIN instructor_students s ON s.student_id = m.user_id " +
                    "WHERE s.instructor_id = ? AND m.completed = 0", userId);

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT title, DATE_FORMAT(class_date, '%d %b') AS d, class_time, location, student_count " +
                    "FROM instructor_classes WHERE instructor_id = ? AND class_date >= CURDATE() " +
                    "ORDER BY class_date ASC LIMIT 4")) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    ClassItem c = new ClassItem();
                    c.title = rs.getString("title");
                    c.when = rs.getString("d") + "  \u2022  " + rs.getString("class_time");
                    c.location = rs.getString("location");
                    c.students = rs.getInt("student_count");
                    d.classes.add(c);
                }
            } catch (Exception e) { log("classes", e); }

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT CONCAT(u.first_name, ' ', u.last_name) AS full_name, " +
                    "COALESCE((p.theory_pct + p.practical_pct + p.mock_test_pct + p.assignment_pct) / 4, 0) AS pct " +
                    "FROM instructor_students s JOIN users u ON u.user_id = s.student_id " +
                    "LEFT JOIN progress p ON p.user_id = u.user_id " +
                    "WHERE s.instructor_id = ? ORDER BY u.first_name, u.last_name LIMIT 6")) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    StudentItem s = new StudentItem();
                    s.name = rs.getString("full_name");
                    s.progress = rs.getInt("pct");
                    d.students.add(s);
                }
            } catch (Exception e) { log("students", e); }

        } catch (Exception e) {
            System.out.println("InstructorDAO: could not connect (" + e.getMessage() + ")");
        }
        return d;
    }

    /** Instructors assigned to a given learner. */
    public List<InstructorInfo> loadForLearner(int learnerId) {
        List<InstructorInfo> out = new ArrayList<>();
        String sql =
            "SELECT u.user_id AS id, CONCAT(u.first_name, ' ', u.last_name) AS full_name, " +
            "  (SELECT COUNT(*) FROM instructor_students s2 WHERE s2.instructor_id = u.user_id) AS total_students, " +
            "  (SELECT COUNT(*) FROM instructor_classes c WHERE c.instructor_id = u.user_id AND c.class_date >= CURDATE()) AS upcoming " +
            "FROM instructor_students s JOIN users u ON u.user_id = s.instructor_id " +
            "WHERE s.student_id = ? ORDER BY u.first_name, u.last_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, learnerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                InstructorInfo i = new InstructorInfo();
                i.id = rs.getInt("id");
                i.name = rs.getString("full_name");
                i.totalStudents = rs.getInt("total_students");
                i.upcomingClasses = rs.getInt("upcoming");
                out.add(i);
            }
        } catch (Exception e) { log("loadForLearner", e); }
        return out;
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
        System.out.println("InstructorDAO [" + what + "]: " + e.getMessage());
    }
}