package Dri_verse.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Dri-verse Admin Dashboard: manage instructors and learners. */
public class AdminDashboard extends JFrame {

    private static final Color OUTER_BG     = new Color(0x0A090F);
    private static final Color SHELL_BG     = new Color(0x120C1F);
    private static final Color PANEL        = new Color(0x1A1430);
    private static final Color PANEL_BORDER = new Color(0x2E2650);
    private static final Color CARD         = new Color(0x1F1938);
    private static final Color ACCENT       = new Color(0x8B5CF6);
    private static final Color TEXT_LIGHT   = new Color(0xF4F1FF);
    private static final Color TEXT_MUTED   = new Color(0x9C93B8);
    private static final Color DANGER       = new Color(0x7A2E3A);
    private static final Color GREY         = new Color(0x4A4266);

    /** Role text written to users.role for new instructors. Change if your login expects another spelling. */
    private static final String ROLE_INSTRUCTOR = "INSTRUCTOR";

    private CardLayout cardLayout;
    private JPanel cards;
    private JLabel pageTitle;
    private final List<NavRow> navRows = new ArrayList<>();
    private OverviewPanel overview;
    private InstructorsAdminPanel instructorsPanel;
    private LearnersAdminPanel learnersPanel;
private AdminPanels.PaymentsAdminPanel paymentsPanel;
private AdminPanels.FeedbackAdminPanel feedbackPanel;
private AdminPanels.TrackingAdminPanel trackingPanel;
private AdminPanels.AttendanceAdminPanel attendancePanel;
    private static class NavRow {
        final RoundedPanel row; final JLabel label; final String key;
        NavRow(RoundedPanel row, JLabel label, String key) { this.row = row; this.label = label; this.key = key; }
    }

    public AdminDashboard(int adminId) {
        setTitle("Dri-verse | Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1360, 860);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(OUTER_BG);
        setLayout(new BorderLayout());

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(OUTER_BG);
        outer.setBorder(new EmptyBorder(22, 22, 22, 22));

        RoundedPanel shell = new RoundedPanel(28, SHELL_BG, null);
        shell.setLayout(new BorderLayout());
        shell.add(buildSidebar(), BorderLayout.WEST);
        shell.add(buildMain(), BorderLayout.CENTER);

        outer.add(shell, BorderLayout.CENTER);
        add(outer, BorderLayout.CENTER);
    }

    // ---------------- sidebar ----------------
    private JComponent buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(232, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(22, 18, 22, 12));

        JLabel logo = new JLabel("Dri-verse Admin");
        logo.setFont(new Font("SansSerif", Font.BOLD, 18));
        logo.setForeground(TEXT_LIGHT);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(26));

        String[] items = {"Overview", "Instructors", "Learners", "Payments", "Feedback", "Vehicle Tracking", "Attendance"};
        for (int i = 0; i < items.length; i++) {
            sidebar.add(sidebarItem(items[i], i == 0));
            sidebar.add(Box.createVerticalStrut(4));
        }
        sidebar.add(Box.createVerticalGlue());

        JButton logout = pillButton("Logout", GREY);
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Log out of Dri-verse?", "Confirm Logout",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        });
        sidebar.add(logout);
        return sidebar;
    }

    private JComponent sidebarItem(String text, boolean active) {
        RoundedPanel row = new RoundedPanel(10, active ? ACCENT : PANEL, null);
        row.setLayout(new BorderLayout());
        row.setMaximumSize(new Dimension(500, 36));
        row.setPreferredSize(new Dimension(500, 36));
        row.setBorder(new EmptyBorder(0, 14, 0, 10));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 12));
        label.setForeground(active ? Color.WHITE : TEXT_MUTED);
        row.add(label, BorderLayout.WEST);

        navRows.add(new NavRow(row, label, text));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { navigateTo(text); }
        });
        return row;
    }

    private void navigateTo(String key) {
        for (NavRow nr : navRows) {
            boolean active = nr.key.equals(key);
            nr.row.setFillColor(active ? ACCENT : PANEL);
            nr.label.setForeground(active ? Color.WHITE : TEXT_MUTED);
            nr.label.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 12));
        }
        pageTitle.setText("Overview".equals(key) ? "Admin Overview" : "Manage " + key);
        switch (key) {
            case "Instructors": instructorsPanel.reload(); break;
            case "Learners":    learnersPanel.reload();    break;
            case "Payments":         paymentsPanel.reload();   break;
case "Feedback":         feedbackPanel.reload();   break;
case "Vehicle Tracking": trackingPanel.reload();   break;
case "Attendance":       attendancePanel.reload(); break;
            default:            overview.reload();
        }
        cardLayout.show(cards, key);
    }

    private JComponent buildMain() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(22, 20, 22, 22));

        pageTitle = new JLabel("Admin Overview");
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        pageTitle.setForeground(TEXT_LIGHT);
        pageTitle.setBorder(new EmptyBorder(0, 0, 18, 0));
        wrap.add(pageTitle, BorderLayout.NORTH);

        overview = new OverviewPanel();
        instructorsPanel = new InstructorsAdminPanel();
        learnersPanel = new LearnersAdminPanel();
paymentsPanel = new AdminPanels.PaymentsAdminPanel();
feedbackPanel = new AdminPanels.FeedbackAdminPanel();
trackingPanel = new AdminPanels.TrackingAdminPanel();
attendancePanel = new AdminPanels.AttendanceAdminPanel();
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.setOpaque(false);
        cards.add(overview, "Overview");
        cards.add(instructorsPanel, "Instructors");
        cards.add(learnersPanel, "Learners");
        cards.add(paymentsPanel, "Payments");
cards.add(feedbackPanel, "Feedback");
cards.add(trackingPanel, "Vehicle Tracking");
cards.add(attendancePanel, "Attendance");
        wrap.add(cards, BorderLayout.CENTER);
        return wrap;
    }

    // ==================================================================
    // OVERVIEW
    // ==================================================================
    private class OverviewPanel extends JPanel {
        private final JLabel learners = big(), instructors = big(), pending = big(), classes = big();

        OverviewPanel() {
            setOpaque(false);
            setLayout(new BorderLayout());
            JPanel strip = new JPanel(new GridLayout(1, 4, 16, 0));
            strip.setOpaque(false);
            strip.add(tile("Learners", learners, "Registered learners", new Color(0x1E1B2E), new Color(0x2A1F4A)));
            strip.add(tile("Instructors", instructors, "Instructor profiles", new Color(0x241C3D), new Color(0x3B2160)));
            strip.add(tile("Pending Requests", pending, "Learner requests waiting", new Color(0x2A1B3F), new Color(0x4A1F5E)));
            strip.add(tile("Scheduled Classes", classes, "Upcoming bookings", new Color(0x1B2440), new Color(0x24305E)));
            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            top.setPreferredSize(new Dimension(0, 130));
            top.add(strip, BorderLayout.CENTER);
            add(top, BorderLayout.NORTH);
            reload();
        }

        private JLabel big() {
            JLabel l = new JLabel("0");
            l.setFont(new Font("SansSerif", Font.BOLD, 28));
            l.setForeground(TEXT_LIGHT);
            return l;
        }

        private JComponent tile(String title, JLabel value, String sub, Color c1, Color c2) {
            JPanel p = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2));
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            p.setOpaque(false);
            p.setBorder(new EmptyBorder(14, 16, 14, 16));
            JLabel t = new JLabel(title);
            t.setFont(new Font("SansSerif", Font.PLAIN, 12));
            t.setForeground(TEXT_MUTED);
            JLabel s = new JLabel(sub);
            s.setFont(new Font("SansSerif", Font.PLAIN, 11));
            s.setForeground(TEXT_MUTED);
            p.add(t, BorderLayout.NORTH);
            p.add(value, BorderLayout.CENTER);
            p.add(s, BorderLayout.SOUTH);
            return p;
        }

        void reload() {
            learners.setText(String.valueOf(count("SELECT COUNT(*) FROM users WHERE UPPER(role) IN ('LEARNER','STUDENT')")));
            instructors.setText(String.valueOf(count("SELECT COUNT(*) FROM instructors")));
            pending.setText(String.valueOf(count("SELECT COUNT(*) FROM instructor_requests WHERE status = 'PENDING'")));
            classes.setText(String.valueOf(count(
                    "SELECT COUNT(*) FROM class_schedule WHERE status = 'Scheduled' AND class_date >= CURDATE()")));
        }
    }

    private int count(String sql) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException ex) {
            System.out.println("Admin count failed: " + ex.getMessage());
            return 0;
        }
    }

    // ==================================================================
    // INSTRUCTORS (add / edit / delete)
    // ==================================================================
    private class InstructorsAdminPanel extends JPanel {
        // 0 = instructor id, 1 = users.user_id (both hidden)
        private final DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "UserId", "Name", "Specialization", "Experience", "Phone", "Email", "License", "Login"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        private final JTable table = new JTable(model);
        private final JLabel status = new JLabel(" ");

        InstructorsAdminPanel() {
            setOpaque(false);
            setLayout(new BorderLayout(0, 12));
            styleTable(table, 2);
            JScrollPane sp = scroll(table);
            add(sp, BorderLayout.CENTER);

            JButton add = pillButton("+ Add Instructor", ACCENT);
            JButton edit = pillButton("Edit", ACCENT);
            JButton del = pillButton("Delete", DANGER);
            JButton refresh = pillButton("Refresh", GREY);
            add.addActionListener(e -> openForm(null));
            edit.addActionListener(e -> {
                int r = selected(table);
                if (r < 0) return;
                openForm(model.getValueAt(r, 0));
            });
            del.addActionListener(e -> deleteSelected());
            refresh.addActionListener(e -> reload());

            JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            btns.setOpaque(false);
            btns.add(add); btns.add(edit); btns.add(del); btns.add(refresh);
            status.setForeground(TEXT_MUTED);
            JPanel bottom = new JPanel(new BorderLayout());
            bottom.setOpaque(false);
            bottom.add(btns, BorderLayout.WEST);
            bottom.add(status, BorderLayout.EAST);
            add(bottom, BorderLayout.SOUTH);
            reload();
        }

        void reload() {
            model.setRowCount(0);
            String sql = "SELECT id, user_id, name, specialization, experience, phone, email, license_no FROM instructors ORDER BY name";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int uid = rs.getInt("user_id");
                    boolean hasLogin = !rs.wasNull();
                    model.addRow(new Object[]{rs.getInt("id"), hasLogin ? uid : null,
                            rs.getString("name"), nz(rs.getString("specialization")), nz(rs.getString("experience")),
                            nz(rs.getString("phone")), nz(rs.getString("email")), nz(rs.getString("license_no")),
                            hasLogin ? "Yes" : "No"});
                }
                status.setText(model.getRowCount() + " instructor(s)");
            } catch (SQLException ex) {
                ex.printStackTrace();
                status.setText("Could not load: " + ex.getMessage());
            }
        }

        private void openForm(Object instructorId) {
            boolean isNew = instructorId == null;
            JTextField name = field(), spec = field(), exp = field(), phone = field(), email = field(), lic = field();
            JPasswordField pass = new JPasswordField(20);
            stylePassword(pass);
            Integer userId = null;

            if (!isNew) {
                String sql = "SELECT name, specialization, experience, phone, email, license_no, user_id FROM instructors WHERE id = ?";
                try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setInt(1, ((Number) instructorId).intValue());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            name.setText(rs.getString(1)); spec.setText(nz2(rs.getString(2)));
                            exp.setText(nz2(rs.getString(3))); phone.setText(nz2(rs.getString(4)));
                            email.setText(nz2(rs.getString(5))); lic.setText(nz2(rs.getString(6)));
                            int u = rs.getInt(7);
                            userId = rs.wasNull() ? null : u;
                        }
                    }
                } catch (SQLException ex) {
                    error(ex);
                    return;
                }
            }
            final Integer existingUserId = userId;

            JDialog dlg = new JDialog(AdminDashboard.this, isNew ? "Add Instructor" : "Edit Instructor",
                    Dialog.ModalityType.APPLICATION_MODAL);
            JPanel body = new JPanel(new GridBagLayout());
            body.setBackground(PANEL);
            body.setBorder(new EmptyBorder(18, 20, 18, 20));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(5, 6, 5, 6);
            g.anchor = GridBagConstraints.WEST;

            String passLabel = isNew ? "Login password"
                    : (existingUserId == null ? "Create login password (optional)" : "New password (blank = keep)");
            String[] labels = {"Name", "Specialization", "Experience (e.g. 5 years)", "Phone (10 digits)", "Email", "License no.", passLabel};
            JComponent[] inputs = {name, spec, exp, phone, email, lic, pass};
            for (int i = 0; i < labels.length; i++) {
                JLabel l = new JLabel(labels[i]);
                l.setForeground(TEXT_MUTED);
                g.gridx = 0; g.gridy = i;
                body.add(l, g);
                g.gridx = 1;
                body.add(inputs[i], g);
            }

            JButton save = pillButton(isNew ? "Add" : "Save changes", ACCENT);
            JButton cancel = pillButton("Cancel", GREY);
            cancel.addActionListener(e -> dlg.dispose());
            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            btns.setOpaque(false);
            btns.add(cancel); btns.add(save);
            g.gridx = 0; g.gridy = labels.length; g.gridwidth = 2;
            g.anchor = GridBagConstraints.EAST;
            g.insets = new Insets(14, 6, 0, 6);
            body.add(btns, g);

            save.addActionListener(e -> {
                String n = name.getText().trim(), ph = phone.getText().trim(), em = email.getText().trim();
                String pw = new String(pass.getPassword());
                if (n.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Name is required."); return; }
                if (!ph.isEmpty() && !ph.matches("\\d{10}")) { JOptionPane.showMessageDialog(dlg, "Phone must be 10 digits."); return; }
                if (em.isEmpty() || !em.contains("@")) { JOptionPane.showMessageDialog(dlg, "Enter a valid email (needed for login)."); return; }
                if ((isNew || (existingUserId == null && !pw.isEmpty())) && pw.length() < 4) {
                    JOptionPane.showMessageDialog(dlg, "Password must be at least 4 characters."); return;
                }
                try {
                    saveInstructor(isNew ? null : ((Number) instructorId).intValue(), existingUserId,
                            n, spec.getText().trim(), exp.getText().trim(), ph, em, lic.getText().trim(), pw);
                    dlg.dispose();
                    reload();
                } catch (SQLException ex) {
                    error(ex);
                }
            });

            dlg.setContentPane(body);
            dlg.pack();
            dlg.setLocationRelativeTo(AdminDashboard.this);
            dlg.setVisible(true);
        }

        private void saveInstructor(Integer id, Integer userId, String name, String spec, String exp,
                                    String phone, String email, String lic, String password) throws SQLException {
            String[] parts = name.split("\\s+", 2);
            String first = parts[0], last = parts.length > 1 ? parts[1] : "";
            try (Connection c = DBConnection.getConnection()) {
                c.setAutoCommit(false);
                try {
                    Integer uid = userId;
                    if (uid == null && !password.isEmpty()) {
                        // create the login account
                        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?)")) {
                            ps.setString(1, email);
                            try (ResultSet rs = ps.executeQuery()) {
                                rs.next();
                                if (rs.getInt(1) > 0) throw new SQLException("A user with this email already exists.");
                            }
                        }
                        try (PreparedStatement ps = c.prepareStatement(
                                "INSERT INTO users (first_name, last_name, email, password, phone, role) VALUES (?,?,?,?,?,?)",
                                Statement.RETURN_GENERATED_KEYS)) {
                            ps.setString(1, first); ps.setString(2, last); ps.setString(3, email);
                            ps.setString(4, password); ps.setString(5, phone); ps.setString(6, ROLE_INSTRUCTOR);
                            ps.executeUpdate();
                            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) uid = k.getInt(1); }
                        }
                    } else if (uid != null) {
                        // keep the login account in step with the profile
                        String sql = password.isEmpty()
                                ? "UPDATE users SET first_name=?, last_name=?, email=?, phone=? WHERE user_id=?"
                                : "UPDATE users SET first_name=?, last_name=?, email=?, phone=?, password=? WHERE user_id=?";
                        try (PreparedStatement ps = c.prepareStatement(sql)) {
                            ps.setString(1, first); ps.setString(2, last); ps.setString(3, email); ps.setString(4, phone);
                            if (password.isEmpty()) ps.setInt(5, uid);
                            else { ps.setString(5, password); ps.setInt(6, uid); }
                            ps.executeUpdate();
                        }
                    }

                    if (id == null) {
                        try (PreparedStatement ps = c.prepareStatement(
                                "INSERT INTO instructors (name, specialization, experience, phone, email, license_no, user_id) VALUES (?,?,?,?,?,?,?)")) {
                            fill(ps, name, spec, exp, phone, email, lic, uid);
                            ps.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement ps = c.prepareStatement(
                                "UPDATE instructors SET name=?, specialization=?, experience=?, phone=?, email=?, license_no=?, user_id=? WHERE id=?")) {
                            fill(ps, name, spec, exp, phone, email, lic, uid);
                            ps.setInt(8, id);
                            ps.executeUpdate();
                        }
                    }
                    c.commit();
                } catch (SQLException ex) {
                    c.rollback();
                    throw ex;
                }
            }
        }

        private void fill(PreparedStatement ps, String name, String spec, String exp, String phone,
                          String email, String lic, Integer uid) throws SQLException {
            ps.setString(1, name); ps.setString(2, spec); ps.setString(3, exp);
            ps.setString(4, phone); ps.setString(5, email); ps.setString(6, lic);
            if (uid == null) ps.setNull(7, Types.INTEGER); else ps.setInt(7, uid);
        }

        private void deleteSelected() {
            int r = selected(table);
            if (r < 0) return;
            String name = String.valueOf(model.getValueAt(r, 2));
            if (JOptionPane.showConfirmDialog(this,
                    "Delete instructor " + name + "?\nTheir login account, student links and requests are removed too.",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;

            int id = ((Number) model.getValueAt(r, 0)).intValue();
            Object u = model.getValueAt(r, 1);
            try (Connection c = DBConnection.getConnection()) {
                c.setAutoCommit(false);
                try {
                    if (u != null) {
                        int uid = ((Number) u).intValue();
                        exec(c, "DELETE FROM instructor_students WHERE instructor_id = ?", uid);
                        exec(c, "DELETE FROM instructor_requests WHERE instructor_user_id = ?", uid);
                    }
                    exec(c, "DELETE FROM instructors WHERE id = ?", id);
                    if (u != null) exec(c, "DELETE FROM users WHERE user_id = ?", ((Number) u).intValue());
                    c.commit();
                } catch (SQLException ex) {
                    c.rollback();
                    throw ex;
                }
                reload();
            } catch (SQLException ex) {
                error(ex);
            }
        }
    }

    // ==================================================================
    // LEARNERS (view / assign instructor / delete)
    // ==================================================================
    private class LearnersAdminPanel extends JPanel {
        private final DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Name", "Email", "Phone", "Instructor(s)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        private final JTable table = new JTable(model);
        private final JLabel status = new JLabel(" ");

        LearnersAdminPanel() {
            setOpaque(false);
            setLayout(new BorderLayout(0, 12));
            styleTable(table, 1);
            add(scroll(table), BorderLayout.CENTER);

            JButton assign = pillButton("Assign Instructor", ACCENT);
            JButton del = pillButton("Delete Learner", DANGER);
            JButton refresh = pillButton("Refresh", GREY);
            assign.addActionListener(e -> assignInstructor());
            del.addActionListener(e -> deleteSelected());
            refresh.addActionListener(e -> reload());

            JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            btns.setOpaque(false);
            btns.add(assign); btns.add(del); btns.add(refresh);
            status.setForeground(TEXT_MUTED);
            JPanel bottom = new JPanel(new BorderLayout());
            bottom.setOpaque(false);
            bottom.add(btns, BorderLayout.WEST);
            bottom.add(status, BorderLayout.EAST);
            add(bottom, BorderLayout.SOUTH);
            reload();
        }

        void reload() {
            model.setRowCount(0);
            String sql = "SELECT u.user_id, CONCAT(u.first_name, ' ', u.last_name), u.email, u.phone, "
                       + "COALESCE((SELECT GROUP_CONCAT(i.name SEPARATOR ', ') FROM instructor_students s "
                       + "JOIN instructors i ON i.user_id = s.instructor_id WHERE s.student_id = u.user_id), '-') "
                       + "FROM users u WHERE UPPER(u.role) IN ('LEARNER','STUDENT') ORDER BY u.first_name, u.last_name";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getInt(1), rs.getString(2), nz(rs.getString(3)),
                            nz(rs.getString(4)), rs.getString(5)});
                }
                status.setText(model.getRowCount() + " learner(s)");
            } catch (SQLException ex) {
                ex.printStackTrace();
                status.setText("Could not load: " + ex.getMessage());
            }
        }

        private void assignInstructor() {
            int r = selected(table);
            if (r < 0) return;
            int learnerId = ((Number) model.getValueAt(r, 0)).intValue();

            List<int[]> ids = new ArrayList<>();
            DefaultComboBoxModel<String> names = new DefaultComboBoxModel<>();
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "SELECT user_id, name FROM instructors WHERE user_id IS NOT NULL ORDER BY name");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) { ids.add(new int[]{rs.getInt(1)}); names.addElement(rs.getString(2)); }
            } catch (SQLException ex) { error(ex); return; }

            if (ids.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No instructors with a login account yet.");
                return;
            }
            JComboBox<String> box = new JComboBox<>(names);
            if (JOptionPane.showConfirmDialog(this, box, "Assign instructor to " + model.getValueAt(r, 1),
                    JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

            int instructorUserId = ids.get(box.getSelectedIndex())[0];
            try (Connection c = DBConnection.getConnection()) {
                boolean exists;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT COUNT(*) FROM instructor_students WHERE instructor_id = ? AND student_id = ?")) {
                    ps.setInt(1, instructorUserId); ps.setInt(2, learnerId);
                    try (ResultSet rs = ps.executeQuery()) { rs.next(); exists = rs.getInt(1) > 0; }
                }
                if (exists) {
                    JOptionPane.showMessageDialog(this, "This learner is already assigned to that instructor.");
                    return;
                }
                exec(c, "INSERT INTO instructor_students (instructor_id, student_id) VALUES (?, ?)", instructorUserId, learnerId);
                reload();
            } catch (SQLException ex) { error(ex); }
        }

        private void deleteSelected() {
            int r = selected(table);
            if (r < 0) return;
            if (JOptionPane.showConfirmDialog(this,
                    "Delete learner " + model.getValueAt(r, 1) + "?\nTheir account, links and requests are removed.",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;

            int id = ((Number) model.getValueAt(r, 0)).intValue();
            try (Connection c = DBConnection.getConnection()) {
                c.setAutoCommit(false);
                try {
                    exec(c, "DELETE FROM instructor_students WHERE student_id = ?", id);
                    exec(c, "DELETE FROM instructor_requests WHERE learner_id = ?", id);
                    exec(c, "DELETE FROM users WHERE user_id = ?", id);
                    c.commit();
                } catch (SQLException ex) {
                    c.rollback();
                    throw ex;
                }
                reload();
            } catch (SQLException ex) { error(ex); }
        }
    }

    // ==================================================================
    // helpers
    // ==================================================================
    private static void exec(Connection c, String sql, int... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setInt(i + 1, params[i]);
            ps.executeUpdate();
        }
    }

    private int selected(JTable table) {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Select a row in the table first.");
            return -1;
        }
        return table.convertRowIndexToModel(r);
    }

    private void error(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
    }

    private static String nz(String s) { return s == null || s.isEmpty() ? "-" : s; }
    private static String nz2(String s) { return s == null ? "" : s; }

    /** Hides the first {@code hiddenCols} model columns and applies the dark theme. */
    private void styleTable(JTable t, int hiddenCols) {
        t.setRowHeight(32);
        t.setBackground(CARD);
        t.setForeground(TEXT_LIGHT);
        t.setFont(new Font("SansSerif", Font.PLAIN, 13));
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(ACCENT);
        t.setSelectionForeground(Color.WHITE);
        t.setFillsViewportHeight(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        for (int i = 0; i < hiddenCols; i++) t.removeColumn(t.getColumnModel().getColumn(0));

        DefaultTableCellRenderer header = new DefaultTableCellRenderer();
        header.setBackground(ACCENT);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setOpaque(true);
        t.getTableHeader().setDefaultRenderer(header);

        DefaultTableCellRenderer cell = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel,
                                                                     boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(tb, v, sel, foc, row, col);
                c.setBackground(sel ? ACCENT : (row % 2 == 0 ? CARD : new Color(0x241C3D)));
                c.setForeground(sel ? Color.WHITE : TEXT_LIGHT);
                return c;
            }
        };
        for (int i = 0; i < t.getColumnModel().getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setCellRenderer(cell);
    }

    private JScrollPane scroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createLineBorder(PANEL_BORDER));
        sp.getViewport().setBackground(CARD);
        return sp;
    }

    private JTextField field() {
        JTextField f = new JTextField(20);
        f.setBackground(new Color(0x2E2650));
        f.setForeground(TEXT_LIGHT);
        f.setCaretColor(TEXT_LIGHT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT.darker()), BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        return f;
    }

    private void stylePassword(JPasswordField f) {
        f.setBackground(new Color(0x2E2650));
        f.setForeground(TEXT_LIGHT);
        f.setCaretColor(TEXT_LIGHT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT.darker()), BorderFactory.createEmptyBorder(4, 6, 4, 6)));
    }

    private JButton pillButton(String text, Color bg) {
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

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private Color fill;
        private final Color border;
        RoundedPanel(int radius, Color fill, Color border) {
            this.radius = radius; this.fill = fill; this.border = border;
            setOpaque(false);
        }
        void setFillColor(Color c) { this.fill = c; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
            if (border != null) {
                g2.setColor(border);
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminDashboard(1).setVisible(true));
    }
}