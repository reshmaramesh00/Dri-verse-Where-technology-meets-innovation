package Dri_verse.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * All the "inside" screens of the Instructor Dashboard:
 * My Students, Class Schedule, Test Evaluation, Attendance, Feedback.
 * Same dark-violet look as InstructorDashboard. Run schema_instructor.sql once first.
 */
public final class InstructorPanels {
    private InstructorPanels() {}

    // ---------- Palette (same as the dashboards) ----------
    static final Color PANEL        = new Color(0x1A1430);
    static final Color PANEL_BORDER = new Color(0x2E2650);
    static final Color CARD         = new Color(0x1F1938);
    static final Color ACCENT       = new Color(0x8B5CF6);
    static final Color ACCENT_2     = new Color(0xC4B5FD);
    static final Color TEXT_LIGHT   = new Color(0xF4F1FF);
    static final Color TEXT_MUTED   = new Color(0x9C93B8);

    // =====================================================================
    // DATA ACCESS
    // =====================================================================
    static class Student {
        final int id; final String name; final int progress;
        Student(int id, String name, int progress) { this.id = id; this.name = name; this.progress = progress; }
        @Override public String toString() { return name; }
    }

    static class ClassRow {
        final int id; final String title, date, time, location; final int students;
        ClassRow(int id, String title, String date, String time, String location, int students) {
            this.id = id; this.title = title; this.date = date; this.time = time;
            this.location = location; this.students = students;
        }
        @Override public String toString() { return date + "  \u2022  " + title; }
    }

    static final class Repo {
        static List<Student> students(int instr) throws Exception {
            List<Student> out = new ArrayList<>();
            String sql = "SELECT u.user_id AS id, CONCAT(u.first_name, ' ', u.last_name) AS full_name, " +
                    "COALESCE((p.theory_pct + p.practical_pct + p.mock_test_pct + p.assignment_pct) / 4, 0) AS pct " +
                    "FROM instructor_students s JOIN users u ON u.user_id = s.student_id " +
                    "LEFT JOIN progress p ON p.user_id = u.user_id " +
                    "WHERE s.instructor_id = ? ORDER BY u.first_name, u.last_name";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, instr);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        out.add(new Student(rs.getInt("id"), rs.getString("full_name"), rs.getInt("pct")));
                }
            }
            return out;
        }

        static List<ClassRow> classes(int instr) throws Exception {
            List<ClassRow> out = new ArrayList<>();
            String sql = "SELECT id, title, DATE_FORMAT(class_date, '%d %b %Y') AS d, class_time, location, student_count " +
                    "FROM instructor_classes WHERE instructor_id = ? ORDER BY class_date DESC";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, instr);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        out.add(new ClassRow(rs.getInt("id"), rs.getString("title"), rs.getString("d"),
                                rs.getString("class_time"), rs.getString("location"), rs.getInt("student_count")));
                }
            }
            return out;
        }

        static void addClass(int instr, String title, String date, String time, String loc, int count) throws Exception {
            String sql = "INSERT INTO instructor_classes (instructor_id, title, class_date, class_time, location, student_count) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, instr);
                ps.setString(2, title);
                ps.setString(3, date);
                ps.setString(4, time);
                ps.setString(5, loc);
                ps.setInt(6, count);
                ps.executeUpdate();
            }
        }

        static void deleteClass(int instr, int classId) throws Exception {
            try (Connection c = DBConnection.getConnection()) {
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM instructor_attendance WHERE class_id = ?")) {
                    ps.setInt(1, classId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "DELETE FROM instructor_classes WHERE id = ? AND instructor_id = ?")) {
                    ps.setInt(1, classId);
                    ps.setInt(2, instr);
                    ps.executeUpdate();
                }
            }
        }

        static Map<Integer, String> attendance(int classId) throws Exception {
            Map<Integer, String> out = new HashMap<>();
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "SELECT student_id, status FROM instructor_attendance WHERE class_id = ?")) {
                ps.setInt(1, classId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) out.put(rs.getInt("student_id"), rs.getString("status"));
                }
            }
            return out;
        }

        static void saveAttendance(int classId, Map<Integer, String> marks) throws Exception {
            String sql = "INSERT INTO instructor_attendance (class_id, student_id, status) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE status = VALUES(status)";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                for (Map.Entry<Integer, String> e : marks.entrySet()) {
                    ps.setInt(1, classId);
                    ps.setInt(2, e.getKey());
                    ps.setString(3, e.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }

        static void addEvaluation(int instr, int studentId, String test, double score, String remarks) throws Exception {
            String sql = "INSERT INTO instructor_evaluations (instructor_id, student_id, test_name, score, remarks) " +
                    "VALUES (?, ?, ?, ?, ?)";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, instr);
                ps.setInt(2, studentId);
                ps.setString(3, test);
                ps.setDouble(4, score);
                ps.setString(5, remarks);
                ps.executeUpdate();
            }
        }

        static List<Object[]> evaluations(int instr) throws Exception {
            List<Object[]> out = new ArrayList<>();
            String sql = "SELECT CONCAT(u.first_name, ' ', u.last_name), e.test_name, e.score, e.remarks, " +
                    "DATE_FORMAT(e.evaluated_on, '%d %b %Y') AS d " +
                    "FROM instructor_evaluations e JOIN users u ON u.user_id = e.student_id " +
                    "WHERE e.instructor_id = ? ORDER BY e.id DESC LIMIT 100";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, instr);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        out.add(new Object[]{rs.getString(1), rs.getString(2), rs.getDouble(3), rs.getString(4), rs.getString(5)});
                }
            }
            return out;
        }

        static void addFeedback(int from, int to, int rating, String message) throws Exception {
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "INSERT INTO instructor_feedback (from_user_id, to_user_id, rating, message) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, from);
                ps.setInt(2, to);
                ps.setInt(3, rating);
                ps.setString(4, message);
                ps.executeUpdate();
            }
        }

        // ---------- Payments ----------
        static List<Student> instructorsOf(int learnerId) throws Exception {
            List<Student> out = new ArrayList<>();
            String sql = "SELECT u.user_id, CONCAT(u.first_name, ' ', u.last_name) AS nm " +
                    "FROM instructor_students s JOIN users u ON u.user_id = s.instructor_id " +
                    "WHERE s.student_id = ? ORDER BY u.first_name, u.last_name";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, learnerId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) out.add(new Student(rs.getInt(1), rs.getString(2), 0));
                }
            }
            return out;
        }

        static void submitPayment(int learner, int instructor, double amount, String method, String ref) throws Exception {
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "INSERT INTO learner_payments (learner_id, instructor_id, amount, method, reference) VALUES (?, ?, ?, ?, ?)")) {
                ps.setInt(1, learner);
                ps.setInt(2, instructor);
                ps.setDouble(3, amount);
                ps.setString(4, method);
                ps.setString(5, ref);
                ps.executeUpdate();
            }
        }

        /** Learner's own payment history. */
        static List<Object[]> myPayments(int learner) throws Exception {
            List<Object[]> out = new ArrayList<>();
            String sql = "SELECT CONCAT(u.first_name, ' ', u.last_name), p.amount, p.method, p.reference, " +
                    "DATE_FORMAT(p.paid_on, '%d %b %Y'), p.status " +
                    "FROM learner_payments p JOIN users u ON u.user_id = p.instructor_id " +
                    "WHERE p.learner_id = ? ORDER BY p.id DESC LIMIT 100";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, learner);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        out.add(new Object[]{rs.getString(1), rs.getDouble(2), rs.getString(3),
                                rs.getString(4), rs.getString(5), rs.getString(6)});
                }
            }
            return out;
        }

        /** Payments sent to this instructor. status == null means all. Column 0 is the payment id. */
        static List<Object[]> payments(int instr, String status) throws Exception {
            List<Object[]> out = new ArrayList<>();
            String sql = "SELECT p.id, CONCAT(u.first_name, ' ', u.last_name), p.amount, p.method, p.reference, " +
                    "DATE_FORMAT(p.paid_on, '%d %b %Y'), p.status " +
                    "FROM learner_payments p JOIN users u ON u.user_id = p.learner_id " +
                    "WHERE p.instructor_id = ? " + (status == null ? "" : "AND p.status = ? ") +
                    "ORDER BY p.id DESC LIMIT 200";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, instr);
                if (status != null) ps.setString(2, status);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        out.add(new Object[]{rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getString(4),
                                rs.getString(5), rs.getString(6), rs.getString(7)});
                }
            }
            return out;
        }

        /** Accept or reject a pending payment. Returns false if it was no longer pending. */
        static boolean reviewPayment(int instr, int paymentId, String status) throws Exception {
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "UPDATE learner_payments SET status = ?, reviewed_on = NOW() " +
                         "WHERE id = ? AND instructor_id = ? AND status = 'Pending'")) {
                ps.setString(1, status);
                ps.setInt(2, paymentId);
                ps.setInt(3, instr);
                return ps.executeUpdate() > 0;
            }
        }

        /** {total accepted amount, number of pending payments} */
        static double[] paymentTotals(int instr) throws Exception {
            String sql = "SELECT COALESCE(SUM(CASE WHEN status = 'Accepted' THEN amount END), 0), " +
                    "COUNT(CASE WHEN status = 'Pending' THEN 1 END) FROM learner_payments WHERE instructor_id = ?";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, instr);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return new double[]{rs.getDouble(1), rs.getDouble(2)};
                }
            }
            return new double[]{0, 0};
        }

        /** received = feedback learners gave this instructor; otherwise feedback this instructor sent. */
        static List<Object[]> feedback(int instr, boolean received) throws Exception {
            List<Object[]> out = new ArrayList<>();
            String sql = "SELECT CONCAT(u.first_name, ' ', u.last_name), f.rating, f.message, DATE_FORMAT(f.created_at, '%d %b %Y') AS d " +
                    "FROM instructor_feedback f JOIN users u ON u.user_id = " +
                    (received ? "f.from_user_id WHERE f.to_user_id = ? " : "f.to_user_id WHERE f.from_user_id = ? ") +
                    "ORDER BY f.id DESC LIMIT 100";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, instr);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int r = Math.max(0, Math.min(5, rs.getInt(2)));
                        String stars = "\u2605".repeat(r) + "\u2606".repeat(5 - r);
                        out.add(new Object[]{rs.getString(1), stars, rs.getString(3), rs.getString(4)});
                    }
                }
            }
            return out;
        }
    }

    // =====================================================================
    // SHARED UI HELPERS
    // =====================================================================
    /** Base class: every screen has reload() so the dashboard can refresh it. */
    public abstract static class Section extends JPanel {
        protected final int userId;
        Section(int userId) {
            this.userId = userId;
            setOpaque(false);
            setLayout(new BorderLayout(0, 16));
        }
        public abstract void reload();
    }

    static class Card extends JPanel {
        Card() {
            setOpaque(false);
            setBorder(new EmptyBorder(16, 18, 16, 18));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(PANEL);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
            g2.setColor(PANEL_BORDER);
            g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 14));
        l.setForeground(TEXT_LIGHT);
        return l;
    }

    static JButton pill(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT.darker() : ACCENT);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    static <T extends JComponent> T styleField(T c) {
        c.setBackground(CARD);
        c.setForeground(TEXT_LIGHT);
        c.setFont(new Font("SansSerif", Font.PLAIN, 12));
        if (c instanceof JTextComponent) {
            ((JTextComponent) c).setCaretColor(TEXT_LIGHT);
            c.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PANEL_BORDER), new EmptyBorder(6, 8, 6, 8)));
        } else if (c instanceof JSpinner) {
            JTextField tf = ((JSpinner.DefaultEditor) ((JSpinner) c).getEditor()).getTextField();
            tf.setBackground(CARD);
            tf.setForeground(TEXT_LIGHT);
            tf.setCaretColor(TEXT_LIGHT);
            c.setBorder(BorderFactory.createLineBorder(PANEL_BORDER));
        }
        return c;
    }

    /** Small caption above a field. */
    static JPanel labeled(String caption, JComponent field) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(caption);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(TEXT_MUTED);
        l.setBorder(new EmptyBorder(0, 0, 4, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(field);
        return p;
    }

    static Card formCard(String title, JComponent... parts) {
        Card card = new Card();
        card.setLayout(new BorderLayout(0, 12));
        card.add(heading(title), BorderLayout.NORTH);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row.setOpaque(false);
        for (JComponent p : parts) row.add(p);
        card.add(row, BorderLayout.CENTER);
        return card;
    }

    static Card tableCard(String title, JComponent topRight, JTable table, JComponent bottom) {
        Card card = new Card();
        card.setLayout(new BorderLayout(0, 12));
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(heading(title), BorderLayout.WEST);
        if (topRight != null) head.add(topRight, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.getViewport().setBackground(PANEL);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        card.add(sp, BorderLayout.CENTER);
        if (bottom != null) card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    static JPanel rightAligned(JComponent c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        p.setOpaque(false);
        p.add(c);
        return p;
    }

    static DefaultTableModel readOnly(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    static JTable styledTable(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setBackground(PANEL);
        t.setForeground(TEXT_LIGHT);
        t.setGridColor(PANEL_BORDER);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        t.setRowHeight(34);
        t.setShowVerticalLines(false);
        t.setSelectionBackground(ACCENT);
        t.setSelectionForeground(Color.WHITE);
        t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(tb, v, s, f, r, c);
                setBackground(CARD);
                setForeground(TEXT_MUTED);
                setFont(new Font("SansSerif", Font.BOLD, 11));
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
        return t;
    }

    static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Dri-verse", JOptionPane.INFORMATION_MESSAGE);
    }

    static void error(Component parent, Exception e) {
        JOptionPane.showMessageDialog(parent, "Database problem:\n" + e.getMessage(),
                "Dri-verse", JOptionPane.ERROR_MESSAGE);
    }

    /** Draws a violet progress bar inside a table cell. */
    static class BarRenderer extends JPanel implements TableCellRenderer {
        private int pct;
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            pct = (v instanceof Number) ? Math.max(0, Math.min(100, ((Number) v).intValue())) : 0;
            setBackground(sel ? t.getSelectionBackground() : t.getBackground());
            return this;
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int barH = 8, y = (getHeight() - barH) / 2, w = Math.max(10, getWidth() - 64);
            g2.setColor(CARD);
            g2.fill(new RoundRectangle2D.Double(8, y, w, barH, barH, barH));
            g2.setColor(ACCENT);
            g2.fill(new RoundRectangle2D.Double(8, y, w * pct / 100.0, barH, barH, barH));
            g2.setColor(ACCENT_2);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString(pct + "%", w + 16, getHeight() / 2 + 4);
            g2.dispose();
        }
    }

    // =====================================================================
    // 1. MY STUDENTS
    // =====================================================================
    public static class StudentsPanel extends Section {
        private final DefaultTableModel model = readOnly("#", "Student", "Overall Progress");

        public StudentsPanel(int userId) {
            super(userId);
            JTable t = styledTable(model);
            t.getColumnModel().getColumn(0).setMaxWidth(50);
            t.getColumnModel().getColumn(2).setCellRenderer(new BarRenderer());
            add(tableCard("All Assigned Students", null, t, null), BorderLayout.CENTER);
            reload();
        }

        @Override public void reload() {
            model.setRowCount(0);
            try {
                int i = 1;
                for (Student s : Repo.students(userId))
                    model.addRow(new Object[]{i++, s.name, s.progress});
            } catch (Exception e) { error(this, e); }
        }
    }

    // =====================================================================
    // 2. CLASS SCHEDULE
    // =====================================================================
    public static class SchedulePanel extends Section {
        private final JTextField title    = styleField(new JTextField(14));
        private final JTextField date     = styleField(new JTextField(java.time.LocalDate.now().toString(), 9));
        private final JTextField time     = styleField(new JTextField("10:00", 5));
        private final JTextField location = styleField(new JTextField(12));
        private final JSpinner count      = styleField(new JSpinner(new SpinnerNumberModel(1, 0, 100, 1)));
        private final DefaultTableModel model = readOnly("Date", "Time", "Class", "Location", "Students");
        private final JTable table = styledTable(model);
        private List<ClassRow> rows = new ArrayList<>();

        public SchedulePanel(int userId) {
            super(userId);
            JButton add = pill("+  Add Class");
            add.addActionListener(e -> addClass());
            add(formCard("Schedule a New Class",
                    labeled("Class title", title),
                    labeled("Date (yyyy-mm-dd)", date),
                    labeled("Time (HH:mm)", time),
                    labeled("Location", location),
                    labeled("Students", count),
                    labeled(" ", add)), BorderLayout.NORTH);

            JButton del = pill("Delete Selected");
            del.addActionListener(e -> deleteSelected());
            add(tableCard("My Classes", null, table, rightAligned(del)), BorderLayout.CENTER);
            reload();
        }

        private void addClass() {
            String t = title.getText().trim();
            if (t.isEmpty()) { info(this, "Please enter a class title."); return; }
            java.time.LocalDate d;
            String tm;
            try { d = java.time.LocalDate.parse(date.getText().trim()); }
            catch (Exception ex) { info(this, "Date must look like 2026-10-25."); return; }
            try { tm = java.time.LocalTime.parse(time.getText().trim()).toString(); }
            catch (Exception ex) { info(this, "Time must look like 14:30."); return; }
            try {
                Repo.addClass(userId, t, d.toString(), tm, location.getText().trim(), (Integer) count.getValue());
                title.setText("");
                reload();
            } catch (Exception ex) { error(this, ex); }
        }

        private void deleteSelected() {
            int r = table.getSelectedRow();
            if (r < 0) { info(this, "Select a class in the table first."); return; }
            int ok = JOptionPane.showConfirmDialog(this, "Delete this class and its attendance records?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
            try {
                Repo.deleteClass(userId, rows.get(r).id);
                reload();
            } catch (Exception ex) { error(this, ex); }
        }

        @Override public void reload() {
            model.setRowCount(0);
            try {
                rows = Repo.classes(userId);
                for (ClassRow c : rows)
                    model.addRow(new Object[]{c.date, c.time, c.title, c.location, c.students});
            } catch (Exception e) { error(this, e); }
        }
    }

    // =====================================================================
    // 3. TEST EVALUATION
    // =====================================================================
    public static class EvaluationPanel extends Section {
        private final JComboBox<Student> studentBox = styleField(new JComboBox<>());
        private final JTextField test    = styleField(new JTextField("Mock Test 1", 14));
        private final JSpinner score     = styleField(new JSpinner(new SpinnerNumberModel(5.0, 0.0, 10.0, 0.5)));
        private final JTextField remarks = styleField(new JTextField(22));
        private final DefaultTableModel model = readOnly("Student", "Test", "Score (/10)", "Remarks", "Date");

        public EvaluationPanel(int userId) {
            super(userId);
            JButton save = pill("Save Evaluation");
            save.addActionListener(e -> save());
            add(formCard("Evaluate a Test",
                    labeled("Student", studentBox),
                    labeled("Test name", test),
                    labeled("Score (0-10)", score),
                    labeled("Remarks", remarks),
                    labeled(" ", save)), BorderLayout.NORTH);
            add(tableCard("Recent Evaluations", null, styledTable(model), null), BorderLayout.CENTER);
            reload();
        }

        private void save() {
            Student s = (Student) studentBox.getSelectedItem();
            if (s == null) { info(this, "No students assigned to you yet."); return; }
            String t = test.getText().trim();
            if (t.isEmpty()) { info(this, "Please enter the test name."); return; }
            try {
                Repo.addEvaluation(userId, s.id, t, ((Number) score.getValue()).doubleValue(), remarks.getText().trim());
                remarks.setText("");
                reload();
            } catch (Exception ex) { error(this, ex); }
        }

        @Override public void reload() {
            try {
                Student keep = (Student) studentBox.getSelectedItem();
                studentBox.removeAllItems();
                for (Student s : Repo.students(userId)) {
                    studentBox.addItem(s);
                    if (keep != null && keep.id == s.id) studentBox.setSelectedItem(s);
                }
                model.setRowCount(0);
                for (Object[] row : Repo.evaluations(userId)) model.addRow(row);
            } catch (Exception e) { error(this, e); }
        }
    }

    // =====================================================================
    // 4. ATTENDANCE
    // =====================================================================
    public static class AttendancePanel extends Section {
        private static final String[] STATUSES = {"Present", "Absent", "Late"};
        private final JComboBox<ClassRow> classBox = styleField(new JComboBox<>());
        private final DefaultTableModel model = new DefaultTableModel(new String[]{"Student", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 1; }
        };
        private final JTable table = styledTable(model);
        private List<Student> students = new ArrayList<>();
        private boolean loading = false;

        public AttendancePanel(int userId) {
            super(userId);
            JComboBox<String> editor = styleField(new JComboBox<>(STATUSES));
            table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(editor));
            classBox.addActionListener(e -> { if (!loading) loadMarks(); });

            JButton save = pill("Save Attendance");
            save.addActionListener(e -> save());
            add(formCard("Pick a Class", labeled("Class", classBox)), BorderLayout.NORTH);
            add(tableCard("Mark Attendance (click a Status cell to change it)", null, table, rightAligned(save)),
                    BorderLayout.CENTER);
            reload();
        }

        @Override public void reload() {
            try {
                loading = true;
                ClassRow keep = (ClassRow) classBox.getSelectedItem();
                classBox.removeAllItems();
                for (ClassRow c : Repo.classes(userId)) {
                    classBox.addItem(c);
                    if (keep != null && keep.id == c.id) classBox.setSelectedItem(c);
                }
                students = Repo.students(userId);
            } catch (Exception e) {
                error(this, e);
            } finally {
                loading = false;
            }
            loadMarks();
        }

        private void loadMarks() {
            model.setRowCount(0);
            ClassRow c = (ClassRow) classBox.getSelectedItem();
            if (c == null) return;
            try {
                Map<Integer, String> saved = Repo.attendance(c.id);
                for (Student s : students)
                    model.addRow(new Object[]{s.name, saved.getOrDefault(s.id, "Present")});
            } catch (Exception e) { error(this, e); }
        }

        private void save() {
            ClassRow c = (ClassRow) classBox.getSelectedItem();
            if (c == null) { info(this, "Schedule a class first (Class Schedule tab)."); return; }
            if (table.isEditing()) table.getCellEditor().stopCellEditing();
            Map<Integer, String> marks = new HashMap<>();
            for (int i = 0; i < students.size() && i < model.getRowCount(); i++)
                marks.put(students.get(i).id, String.valueOf(model.getValueAt(i, 1)));
            try {
                Repo.saveAttendance(c.id, marks);
                info(this, "Attendance saved for " + c.title + ".");
            } catch (Exception e) { error(this, e); }
        }
    }

    // =====================================================================
    // 5. FEEDBACK
    // =====================================================================
    public static class FeedbackPanel extends Section {
        private final JComboBox<Student> studentBox = styleField(new JComboBox<>());
        private final JSpinner rating  = styleField(new JSpinner(new SpinnerNumberModel(5, 1, 5, 1)));
        private final JTextField message = styleField(new JTextField(30));
        private final JComboBox<String> viewBox =
                styleField(new JComboBox<>(new String[]{"Received from learners", "Sent to students"}));
        private final DefaultTableModel model = readOnly("Person", "Rating", "Message", "Date");

        public FeedbackPanel(int userId) {
            super(userId);
            JButton send = pill("Send Feedback");
            send.addActionListener(e -> send());
            add(formCard("Give Feedback to a Student",
                    labeled("Student", studentBox),
                    labeled("Rating (1-5)", rating),
                    labeled("Message", message),
                    labeled(" ", send)), BorderLayout.NORTH);

            JTable t = styledTable(model);
            t.getColumnModel().getColumn(1).setMaxWidth(110);
            viewBox.addActionListener(e -> loadTable());
            add(tableCard("Feedback", viewBox, t, null), BorderLayout.CENTER);
            reload();
        }

        private void send() {
            Student s = (Student) studentBox.getSelectedItem();
            if (s == null) { info(this, "No students assigned to you yet."); return; }
            String m = message.getText().trim();
            if (m.isEmpty()) { info(this, "Please write a message."); return; }
            try {
                Repo.addFeedback(userId, s.id, (Integer) rating.getValue(), m);
                message.setText("");
                viewBox.setSelectedIndex(1);
                loadTable();
            } catch (Exception ex) { error(this, ex); }
        }

        private void loadTable() {
            model.setRowCount(0);
            try {
                for (Object[] row : Repo.feedback(userId, viewBox.getSelectedIndex() == 0)) model.addRow(row);
            } catch (Exception e) { error(this, e); }
        }

        @Override public void reload() {
            try {
                Student keep = (Student) studentBox.getSelectedItem();
                studentBox.removeAllItems();
                for (Student s : Repo.students(userId)) {
                    studentBox.addItem(s);
                    if (keep != null && keep.id == s.id) studentBox.setSelectedItem(s);
                }
            } catch (Exception e) { error(this, e); }
            loadTable();
        }
    }

    // =====================================================================
    // 6. PAYMENTS (instructor accepts / rejects what learners paid)
    // =====================================================================
    public static class PaymentsPanel extends Section {
        private final JLabel summary = new JLabel(" ");
        private final JComboBox<String> filter =
                styleField(new JComboBox<>(new String[]{"Pending", "Accepted", "Rejected", "All"}));
        private final DefaultTableModel model =
                readOnly("Learner", "Amount (\u20B9)", "Method", "Reference", "Date", "Status");
        private final JTable table = styledTable(model);
        private final List<Integer> ids = new ArrayList<>();

        public PaymentsPanel(int userId) {
            super(userId);
            summary.setFont(new Font("SansSerif", Font.BOLD, 14));
            summary.setForeground(TEXT_LIGHT);
            Card top = new Card();
            top.setLayout(new BorderLayout());
            top.add(summary, BorderLayout.WEST);
            add(top, BorderLayout.NORTH);

            JButton accept = pill("\u2713  Accept Selected");
            JButton reject = pill("Reject Selected");
            accept.addActionListener(e -> review("Accepted"));
            reject.addActionListener(e -> review("Rejected"));
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            buttons.setOpaque(false);
            buttons.add(reject);
            buttons.add(accept);

            filter.addActionListener(e -> loadTable());
            add(tableCard("Learner Payments", filter, table, buttons), BorderLayout.CENTER);
            reload();
        }

        private void review(String newStatus) {
            int r = table.getSelectedRow();
            if (r < 0) { info(this, "Select a payment in the table first."); return; }
            if (!"Pending".equals(String.valueOf(model.getValueAt(r, 5)))) {
                info(this, "Only pending payments can be accepted or rejected.");
                return;
            }
            int ok = JOptionPane.showConfirmDialog(this,
                    newStatus.replace("ed", "") + " the payment of \u20B9 " + model.getValueAt(r, 1)
                            + " from " + model.getValueAt(r, 0) + "?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
            try {
                if (!Repo.reviewPayment(userId, ids.get(r), newStatus))
                    info(this, "That payment was already reviewed.");
                reload();
            } catch (Exception e) { error(this, e); }
        }

        private void loadTable() {
            model.setRowCount(0);
            ids.clear();
            String f = (String) filter.getSelectedItem();
            try {
                for (Object[] row : Repo.payments(userId, "All".equals(f) ? null : f)) {
                    ids.add((Integer) row[0]);
                    model.addRow(java.util.Arrays.copyOfRange(row, 1, row.length));
                }
            } catch (Exception e) { error(this, e); }
        }

        @Override public void reload() {
            try {
                double[] t = Repo.paymentTotals(userId);
                summary.setText("Collected: \u20B9 " + String.format("%,.2f", t[0])
                        + "     \u2022     Pending approval: " + (int) t[1]);
            } catch (Exception e) { error(this, e); }
            loadTable();
        }
    }
}