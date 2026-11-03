package Dri_verse.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.time.LocalDate;

/**
 * Extra admin screens for Dri-verse: Payments, Feedback, Vehicle Tracking, Instructor Attendance.
 * Each panel has a reload() method that AdminDashboard calls when its tab is opened.
 *
 * Needs this table for attendance (run once in MySQL):
 *
 *   CREATE TABLE IF NOT EXISTS admin_instructor_attendance (
 *       id INT AUTO_INCREMENT PRIMARY KEY,
 *       instructor_id INT NOT NULL,
 *       att_date DATE NOT NULL,
 *       status VARCHAR(20) NOT NULL,
 *       check_in TIME NULL,
 *       UNIQUE KEY uq_instr_date (instructor_id, att_date),
 *       FOREIGN KEY (instructor_id) REFERENCES instructors(id) ON DELETE CASCADE
 *   );
 */
public final class AdminPanels {

    private AdminPanels() {}

    private static final Color PANEL        = new Color(0x1A1430);
    private static final Color PANEL_BORDER = new Color(0x2E2650);
    private static final Color CARD         = new Color(0x1F1938);
    private static final Color ACCENT       = new Color(0x8B5CF6);
    private static final Color TEXT_LIGHT   = new Color(0xF4F1FF);
    private static final Color TEXT_MUTED   = new Color(0x9C93B8);
    private static final Color DANGER       = new Color(0x7A2E3A);
    private static final Color GOOD         = new Color(0x1F6B4F);
    private static final Color GREY         = new Color(0x4A4266);

    // ==================================================================
    // Base class: a table + status label + reload()
    // ==================================================================
    abstract static class Section extends JPanel {
        protected final DefaultTableModel model;
        protected final JTable table;
        protected final JLabel status = new JLabel(" ");

        Section(String[] columns) {
            model = new DefaultTableModel(columns, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            table = new JTable(model);
            setOpaque(false);
            setLayout(new BorderLayout(0, 12));
            styleTable(table);
            add(scroll(table), BorderLayout.CENTER);
            status.setForeground(TEXT_MUTED);
        }

        abstract void reload();

        protected int selectedModelRow() {
            int r = table.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Select a row in the table first.");
                return -1;
            }
            return table.convertRowIndexToModel(r);
        }

        protected JPanel bottomBar(JButton... buttons) {
            JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            btns.setOpaque(false);
            for (JButton b : buttons) btns.add(b);
            JPanel bottom = new JPanel(new BorderLayout());
            bottom.setOpaque(false);
            bottom.add(btns, BorderLayout.WEST);
            bottom.add(status, BorderLayout.EAST);
            return bottom;
        }
    }

    // ==================================================================
    // PAYMENTS (all learners)
    // ==================================================================
    static class PaymentsAdminPanel extends Section {
        private final JLabel paid = big(), pending = big(), failed = big();
        private final JComboBox<String> filter =
                new JComboBox<>(new String[]{"All", "PAID", "PENDING", "FAILED"});

        PaymentsAdminPanel() {
            super(new String[]{"ID", "Learner", "Description", "Amount", "Status", "Method", "Reference", "Created"});

            JPanel strip = new JPanel(new GridLayout(1, 3, 14, 0));
            strip.setOpaque(false);
            strip.setPreferredSize(new Dimension(0, 92));
            strip.add(statCard("Total Paid", paid));
            strip.add(statCard("Pending Amount", pending));
            strip.add(statCard("Failed Amount", failed));

            JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            filterRow.setOpaque(false);
            JLabel l = new JLabel("Show:");
            l.setForeground(TEXT_MUTED);
            filterRow.add(l);
            filterRow.add(filter);

            JPanel top = new JPanel(new BorderLayout(0, 10));
            top.setOpaque(false);
            top.add(strip, BorderLayout.NORTH);
            top.add(filterRow, BorderLayout.SOUTH);
            add(top, BorderLayout.NORTH);

            JButton refresh = pillButton("Refresh", GREY);
            JButton markPaid = pillButton("Mark as Paid", GOOD);
            JButton markFailed = pillButton("Mark as Failed", DANGER);
            refresh.addActionListener(e -> reload());
            filter.addActionListener(e -> reload());
            markPaid.addActionListener(e -> mark("PAID"));
            markFailed.addActionListener(e -> mark("FAILED"));
            add(bottomBar(refresh, markPaid, markFailed), BorderLayout.SOUTH);

            reload();
        }

        @Override void reload() {
            model.setRowCount(0);
            String f = (String) filter.getSelectedItem();
            boolean all = f == null || "All".equals(f);
            String sql = "SELECT p.payment_id, CONCAT(u.first_name, ' ', u.last_name), p.description, p.amount, "
                    + "p.payment_status, COALESCE(p.payment_method, ''), COALESCE(p.transaction_reference, ''), p.created_at "
                    + "FROM payments p JOIN users u ON u.user_id = p.learner_id "
                    + (all ? "" : "WHERE UPPER(p.payment_status) = ? ")
                    + "ORDER BY p.created_at DESC";
            String totals = "SELECT "
                    + "COALESCE(SUM(CASE WHEN UPPER(payment_status)='PAID' THEN amount ELSE 0 END),0), "
                    + "COALESCE(SUM(CASE WHEN UPPER(payment_status)='PENDING' THEN amount ELSE 0 END),0), "
                    + "COALESCE(SUM(CASE WHEN UPPER(payment_status)='FAILED' THEN amount ELSE 0 END),0) FROM payments";
            try (Connection c = DBConnection.getConnection()) {
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    if (!all) ps.setString(1, f);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            model.addRow(new Object[]{rs.getLong(1), rs.getString(2), rs.getString(3),
                                    "\u20B9 " + rs.getBigDecimal(4).toPlainString(), rs.getString(5),
                                    rs.getString(6), rs.getString(7), rs.getTimestamp(8)});
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(totals);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        paid.setText("\u20B9 " + rs.getBigDecimal(1).toPlainString());
                        pending.setText("\u20B9 " + rs.getBigDecimal(2).toPlainString());
                        failed.setText("\u20B9 " + rs.getBigDecimal(3).toPlainString());
                    }
                }
                status.setText(model.getRowCount() + " payment(s)");
            } catch (SQLException ex) {
                ex.printStackTrace();
                status.setText("Could not load: " + ex.getMessage());
            }
        }

        private void mark(String newStatus) {
            int r = selectedModelRow();
            if (r < 0) return;
            long id = ((Number) model.getValueAt(r, 0)).longValue();
            String current = String.valueOf(model.getValueAt(r, 4));
            if (!"PENDING".equalsIgnoreCase(current)) {
                JOptionPane.showMessageDialog(this, "Only PENDING payments can be changed.\nCurrent status: " + current);
                return;
            }
            if (JOptionPane.showConfirmDialog(this,
                    "Mark payment #" + id + " as " + newStatus + "?",
                    "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

            String sql = "UPDATE payments SET payment_status = ?, paid_at = ? "
                    + "WHERE payment_id = ? AND UPPER(payment_status) = 'PENDING'";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, newStatus);
                if ("PAID".equals(newStatus)) ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                else ps.setNull(2, Types.TIMESTAMP);
                ps.setLong(3, id);
                ps.executeUpdate();
                reload();
            } catch (SQLException ex) {
                error(this, ex);
            }
        }
    }

    // ==================================================================
    // FEEDBACK (from all learners)
    // ==================================================================
    static class FeedbackAdminPanel extends Section {
        private final JLabel total = big(), average = big();
        private final JComboBox<String> filter = new JComboBox<>(new String[]{
                "All", "Overall Experience", "Classes & Scheduling", "Instructors",
                "Mock Tests", "Payments", "Live Tracking", "Other"});

        FeedbackAdminPanel() {
            super(new String[]{"Learner", "Category", "Rating", "Comments"});

            JPanel strip = new JPanel(new GridLayout(1, 2, 14, 0));
            strip.setOpaque(false);
            strip.setPreferredSize(new Dimension(0, 92));
            strip.add(statCard("Feedback Received", total));
            strip.add(statCard("Average Rating (out of 5)", average));

            JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            filterRow.setOpaque(false);
            JLabel l = new JLabel("Category:");
            l.setForeground(TEXT_MUTED);
            filterRow.add(l);
            filterRow.add(filter);

            JPanel top = new JPanel(new BorderLayout(0, 10));
            top.setOpaque(false);
            top.add(strip, BorderLayout.NORTH);
            top.add(filterRow, BorderLayout.SOUTH);
            add(top, BorderLayout.NORTH);

            table.getColumnModel().getColumn(0).setPreferredWidth(140);
            table.getColumnModel().getColumn(1).setPreferredWidth(160);
            table.getColumnModel().getColumn(2).setPreferredWidth(60);
            table.getColumnModel().getColumn(3).setPreferredWidth(520);

            JButton refresh = pillButton("Refresh", GREY);
            JButton view = pillButton("View Full Comment", ACCENT);
            refresh.addActionListener(e -> reload());
            filter.addActionListener(e -> reload());
            view.addActionListener(e -> {
                int r = selectedModelRow();
                if (r < 0) return;
                JTextArea area = new JTextArea(String.valueOf(model.getValueAt(r, 3)), 8, 40);
                area.setLineWrap(true);
                area.setWrapStyleWord(true);
                area.setEditable(false);
                JOptionPane.showMessageDialog(this, new JScrollPane(area),
                        model.getValueAt(r, 0) + " - " + model.getValueAt(r, 1),
                        JOptionPane.PLAIN_MESSAGE);
            });
            add(bottomBar(refresh, view), BorderLayout.SOUTH);

            reload();
        }

        @Override void reload() {
            model.setRowCount(0);
            String f = (String) filter.getSelectedItem();
            boolean all = f == null || "All".equals(f);
            // If learner_feedback has a created_at column, change ORDER BY to: f.created_at DESC
            String sql = "SELECT CONCAT(u.first_name, ' ', u.last_name), f.feedback_type, f.rating, f.comments "
                    + "FROM learner_feedback f JOIN users u ON u.user_id = f.learner_id "
                    + (all ? "" : "WHERE f.feedback_type = ? ")
                    + "ORDER BY u.first_name";
            int count = 0;
            double sum = 0;
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                if (!all) ps.setString(1, f);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int rating = rs.getInt(3);
                        model.addRow(new Object[]{rs.getString(1), rs.getString(2), rating, rs.getString(4)});
                        count++;
                        sum += rating;
                    }
                }
                total.setText(String.valueOf(count));
                average.setText(count == 0 ? "-" : String.format("%.1f", sum / count));
                status.setText(count + " feedback entr" + (count == 1 ? "y" : "ies"));
            } catch (SQLException ex) {
                ex.printStackTrace();
                status.setText("Could not load: " + ex.getMessage());
            }
        }
    }

    // ==================================================================
    // VEHICLE TRACKING (latest position of every vehicle)
    // ==================================================================
    static class TrackingAdminPanel extends Section {

        TrackingAdminPanel() {
            super(new String[]{"Vehicle", "Learner", "Instructor(s)", "Latitude", "Longitude", "Last Updated"});

            JButton refresh = pillButton("Refresh", GREY);
            JButton open = pillButton("Open in Google Maps", ACCENT);
            refresh.addActionListener(e -> reload());
            open.addActionListener(e -> {
                int r = selectedModelRow();
                if (r < 0) return;
                String url = "https://www.google.com/maps/search/?api=1&query="
                        + model.getValueAt(r, 3) + "," + model.getValueAt(r, 4);
                try {
                    Desktop.getDesktop().browse(new java.net.URI(url));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Could not open Google Maps: " + ex.getMessage());
                }
            });
            add(bottomBar(refresh, open), BorderLayout.SOUTH);

            reload();
            // auto-refresh every 10 seconds, but only while this tab is visible
            new javax.swing.Timer(10000, e -> { if (isShowing()) reload(); }).start();
        }

        @Override void reload() {
            int keep = table.getSelectedRow();
            model.setRowCount(0);
            String sql = "SELECT t.vehicle_number, CONCAT(u.first_name, ' ', u.last_name), "
                    + "COALESCE((SELECT GROUP_CONCAT(i.name SEPARATOR ', ') FROM instructor_students s "
                    + "JOIN instructors i ON i.user_id = s.instructor_id WHERE s.student_id = u.user_id), '-'), "
                    + "t.latitude, t.longitude, t.updated_at "
                    + "FROM vehicle_tracking t JOIN users u ON u.user_id = t.learner_id "
                    + "WHERE t.updated_at = (SELECT MAX(t2.updated_at) FROM vehicle_tracking t2 WHERE t2.learner_id = t.learner_id) "
                    + "ORDER BY t.updated_at DESC";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getDouble(4), rs.getDouble(5), rs.getTimestamp(6)});
                }
                if (keep >= 0 && keep < table.getRowCount()) table.setRowSelectionInterval(keep, keep);
                status.setText(model.getRowCount() + " vehicle(s) \u2022 updated "
                        + new java.text.SimpleDateFormat("hh:mm:ss a").format(new java.util.Date()));
            } catch (SQLException ex) {
                ex.printStackTrace();
                status.setText("Could not load: " + ex.getMessage());
            }
        }
    }

    // ==================================================================
    // INSTRUCTOR ATTENDANCE (admin marks per day)
    // ==================================================================
    static class AttendanceAdminPanel extends Section {
        private final JTextField dateField = new JTextField(LocalDate.now().toString(), 10);

        AttendanceAdminPanel() {
            super(new String[]{"ID", "Instructor", "Status", "Check-in", "Present Days (Month)"});

            dateField.setBackground(new Color(0x2E2650));
            dateField.setForeground(TEXT_LIGHT);
            dateField.setCaretColor(TEXT_LIGHT);

            JButton load = pillButton("Load", ACCENT);
            JButton today = pillButton("Today", GREY);
            load.addActionListener(e -> reload());
            today.addActionListener(e -> { dateField.setText(LocalDate.now().toString()); reload(); });

            JPanel dateRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            dateRow.setOpaque(false);
            JLabel l = new JLabel("Date (yyyy-MM-dd):");
            l.setForeground(TEXT_MUTED);
            dateRow.add(l);
            dateRow.add(dateField);
            dateRow.add(load);
            dateRow.add(today);
            add(dateRow, BorderLayout.NORTH);

            JButton present = pillButton("Mark Present", GOOD);
            JButton absent = pillButton("Mark Absent", DANGER);
            JButton leave = pillButton("Mark On Leave", GREY);
            present.addActionListener(e -> mark("Present"));
            absent.addActionListener(e -> mark("Absent"));
            leave.addActionListener(e -> mark("On Leave"));
            add(bottomBar(present, absent, leave), BorderLayout.SOUTH);

            reload();
        }

        private LocalDate parseDate() {
            try {
                return LocalDate.parse(dateField.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Enter the date as yyyy-MM-dd, for example 2026-10-26.");
                return null;
            }
        }

        @Override void reload() {
            LocalDate d = parseDate();
            if (d == null) return;
            model.setRowCount(0);
            String sql = "SELECT i.id, i.name, COALESCE(a.status, 'Not marked'), a.check_in, "
                    + "(SELECT COUNT(*) FROM admin_instructor_attendance x WHERE x.instructor_id = i.id "
                    + " AND x.status = 'Present' AND YEAR(x.att_date) = YEAR(?) AND MONTH(x.att_date) = MONTH(?)) "
                    + "FROM instructors i LEFT JOIN admin_instructor_attendance a "
                    + "ON a.instructor_id = i.id AND a.att_date = ? ORDER BY i.name";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                java.sql.Date sqlDate = java.sql.Date.valueOf(d);
                ps.setDate(1, sqlDate);
                ps.setDate(2, sqlDate);
                ps.setDate(3, sqlDate);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Time t = rs.getTime(4);
                        model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3),
                                t == null ? "-" : t.toString(), rs.getInt(5)});
                    }
                }
                status.setText(model.getRowCount() + " instructor(s) on " + d);
            } catch (SQLException ex) {
                ex.printStackTrace();
                status.setText("Could not load: " + ex.getMessage());
            }
        }

        private void mark(String newStatus) {
            int r = selectedModelRow();
            if (r < 0) return;
            LocalDate d = parseDate();
            if (d == null) return;
            int instructorId = ((Number) model.getValueAt(r, 0)).intValue();
            String sql = "INSERT INTO admin_instructor_attendance (instructor_id, att_date, status, check_in) "
                    + "VALUES (?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE status = VALUES(status), check_in = VALUES(check_in)";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, instructorId);
                ps.setDate(2, java.sql.Date.valueOf(d));
                ps.setString(3, newStatus);
                if ("Present".equals(newStatus)) ps.setTime(4, new Time(System.currentTimeMillis()));
                else ps.setNull(4, Types.TIME);
                ps.executeUpdate();
                reload();
            } catch (SQLException ex) {
                error(this, ex);
            }
        }
    }

    // ==================================================================
    // Shared helpers
    // ==================================================================
    private static void error(Component parent, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(parent, "Database error:\n" + ex.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE);
    }

    private static JLabel big() {
        JLabel l = new JLabel("0");
        l.setFont(new Font("SansSerif", Font.BOLD, 24));
        l.setForeground(TEXT_LIGHT);
        return l;
    }

    private static JComponent statCard(String title, JLabel value) {
        JPanel p = new JPanel(new BorderLayout(0, 6)) {
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
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        t.setForeground(TEXT_MUTED);
        p.add(t, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private static void styleTable(JTable t) {
        t.setRowHeight(32);
        t.setBackground(CARD);
        t.setForeground(TEXT_LIGHT);
        t.setFont(new Font("SansSerif", Font.PLAIN, 13));
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(ACCENT);
        t.setSelectionForeground(Color.WHITE);
        t.setFillsViewportHeight(true);
        t.setAutoCreateRowSorter(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer header = new DefaultTableCellRenderer();
        header.setBackground(ACCENT);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setOpaque(true);
        t.getTableHeader().setDefaultRenderer(header);

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel,
                                                                     boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(tb, v, sel, foc, row, col);
                c.setBackground(sel ? ACCENT : (row % 2 == 0 ? CARD : new Color(0x241C3D)));
                c.setForeground(sel ? Color.WHITE : TEXT_LIGHT);
                return c;
            }
        });
    }

    private static JScrollPane scroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createLineBorder(PANEL_BORDER));
        sp.getViewport().setBackground(CARD);
        return sp;
    }

    private static JButton pillButton(String text, Color bg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}