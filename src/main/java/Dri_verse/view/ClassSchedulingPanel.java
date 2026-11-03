package Dri_verse.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * "Class Scheduling" page for the learner dashboard.
 *
 * Works with this table in driverse_db:
 *   class_schedule(id, class_date, class_time, instructor, vehicle, class_type, status)
 * and reads the instructor list from driverse_db.instructors.
 *
 * Use it:  new ClassSchedulingPanel()
 * Refresh: call reload() whenever the page is opened.
 */
public class ClassSchedulingPanel extends JPanel {

    // ====== MySQL login (same as InstructorsPanel) ======
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/drivers_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "your_password_here";
    // ====================================================

    private static final String FONT = "Segoe UI";
    private static final Color ACCENT = new Color(0x3B63F5);
    private static final Color TEXT = Color.WHITE;
    private static final Color MUTED = new Color(0xC3CBF5);
    private static final Color TABLE_BG = new Color(25, 25, 60);
    private static final Color TABLE_BG_ALT = new Color(36, 36, 100);
    private static final Color FIELD_BG = new Color(45, 45, 120);

    private static final String NO_VEHICLE = "N/A (no vehicle)";
    private static final String[] CLASS_TYPES = {
            "Car Training", "Two-Wheeler Training", "Theory Class", "Mock Test", "Road Safety Training"
    };
    private static final String[] VEHICLES = {
            "Maruti Swift (Car)", "Hyundai i20 (Car)", "Honda Activa (Two-Wheeler)",
            "TVS Jupiter (Two-Wheeler)", NO_VEHICLE
    };
    private static final String[] TIME_SLOTS = {
            "07:00", "08:00", "09:00", "10:00", "11:00", "12:00",
            "14:00", "15:00", "16:00", "17:00", "18:00"
    };

    private final JComboBox<InstructorItem> instructorBox = new JComboBox<>();
    private final JComboBox<String> typeBox = new JComboBox<>(CLASS_TYPES);
    private final JComboBox<String> vehicleBox = new JComboBox<>(VEHICLES);
    private final JComboBox<String> timeBox = new JComboBox<>(TIME_SLOTS);
    private final JComboBox<String> locationBox = new JComboBox<>(DashboardDAO.LOCATIONS);
    private final JSpinner dateSpinner =
            new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
    private final JLabel status = new JLabel(" ");

    // columns: ID (hidden), Date, Time, Instructor, Vehicle, Class Type, Status
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Date", "Time", "Instructor", "Vehicle", "Class Type", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    private static class InstructorItem {
        final int id; final String name, specialization;
        InstructorItem(int id, String name, String specialization) {
            this.id = id; this.name = name; this.specialization = specialization;
        }
        @Override public String toString() {
            return specialization == null || specialization.trim().isEmpty() ? name : name + "  -  " + specialization;
        }
    }

    public ClassSchedulingPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(28, 28, 20, 28));

        JLabel title = new JLabel("Class Scheduling");
        title.setFont(new Font(FONT, Font.BOLD, 28));
        title.setForeground(TEXT);
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.add(buildForm(), BorderLayout.NORTH);
        center.add(buildList(), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        status.setFont(new Font(FONT, Font.PLAIN, 14));
        status.setForeground(MUTED);
        add(status, BorderLayout.SOUTH);

        // theory-type classes need no vehicle
        typeBox.addActionListener(e -> {
            String t = (String) typeBox.getSelectedItem();
            if (isTheory(t)) vehicleBox.setSelectedItem(NO_VEHICLE);
            else if (NO_VEHICLE.equals(vehicleBox.getSelectedItem())) vehicleBox.setSelectedIndex(0);
        });

        reload();
    }

    private static boolean isTheory(String type) {
        return "Theory Class".equals(type) || "Mock Test".equals(type);
    }

    // ---------------- UI ----------------
    private JPanel buildForm() {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Shape s = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                g2.setColor(new Color(255, 255, 255, 22));
                g2.fill(s);
                g2.setColor(new Color(255, 255, 255, 45));
                g2.draw(s);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        for (JComponent c : new JComponent[]{instructorBox, typeBox, vehicleBox, timeBox, dateSpinner})
            c.setFont(new Font(FONT, Font.PLAIN, 13));

        styleCombo(instructorBox);
        styleCombo(typeBox);
        styleCombo(vehicleBox);
        styleCombo(timeBox);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;

        styleCombo(locationBox);
// ...
String[] labels = {"Instructor", "Class Type", "Vehicle", "Location", "Date", "Time"};
JComponent[] inputs = {instructorBox, typeBox, vehicleBox, locationBox, dateSpinner, timeBox};
double[] weights = {2.0, 1.4, 1.6, 1.4, 1.0, 0.8};
// ...
gc.gridx = 5; gc.gridy = 2; gc.weightx = 0.8;   // Book button

        for (int i = 0; i < inputs.length; i++) {
            JLabel l = new JLabel(labels[i]);
            l.setFont(new Font(FONT, Font.BOLD, 13));
            l.setForeground(TEXT);
            gc.gridx = i; gc.gridy = 0; gc.weightx = weights[i];
            card.add(l, gc);
            gc.gridy = 1;
            card.add(inputs[i], gc);
        }

        JButton book = button("Book Class");
        book.addActionListener(e -> bookClass());
        gc.gridx = 4; gc.gridy = 2; gc.weightx = 0.8;
        gc.insets = new Insets(10, 6, 0, 6);
        card.add(book, gc);
        return card;
    }

    private JPanel buildList() {
        JPanel list = new JPanel(new BorderLayout(0, 10));
        list.setOpaque(false);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel t = new JLabel("Booked Classes");
        t.setFont(new Font(FONT, Font.BOLD, 18));
        t.setForeground(TEXT);
        head.add(t, BorderLayout.WEST);

        JButton refresh = button("Refresh");
        refresh.addActionListener(e -> reload());
        JButton cancel = button("Cancel Selected Class");
        cancel.addActionListener(e -> cancelSelected());
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        btns.add(refresh);
        btns.add(cancel);
        head.add(btns, BorderLayout.EAST);
        list.add(head, BorderLayout.NORTH);

        table.setRowHeight(32);
        table.setBackground(TABLE_BG);
        table.setForeground(TEXT);
        table.setFont(new Font(FONT, Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.removeColumn(table.getColumnModel().getColumn(0));   // hide the ID column

        DefaultTableCellRenderer header = new DefaultTableCellRenderer();
        header.setBackground(ACCENT);
        header.setForeground(Color.WHITE);
        header.setFont(new Font(FONT, Font.BOLD, 13));
        header.setHorizontalAlignment(SwingConstants.LEFT);
        header.setOpaque(true);
        table.getTableHeader().setDefaultRenderer(header);

        // one renderer for every visible column: striped rows + coloured Status text
        DefaultTableCellRenderer cell = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel,
                                                                     boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tb, v, sel, focus, row, col);
                if (sel) {
                    c.setBackground(ACCENT);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? TABLE_BG : TABLE_BG_ALT);
                    c.setForeground(TEXT);
                    if (tb.convertColumnIndexToModel(col) == 6) {   // Status column
                        String s = String.valueOf(v).toUpperCase();
                        c.setForeground("COMPLETED".equals(s) ? new Color(0x6EE7A8)
                                : "CANCELLED".equals(s) ? new Color(0xFF8A8A) : new Color(0x8FA8FF));
                    }
                }
                return c;
            }
        };
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(cell);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 45)));
        sp.getViewport().setBackground(TABLE_BG);
        list.add(sp, BorderLayout.CENTER);
        return list;
    }

    private <T> void styleCombo(JComboBox<T> box) {
        box.setOpaque(true);
        box.setBackground(FIELD_BG);
        box.setForeground(TEXT);
        box.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean hasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
                setForeground(TEXT);
                setBackground(isSelected ? ACCENT : FIELD_BG);
                setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
                return this;
            }
        });
    }

    private JButton button(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? ACCENT.darker() : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFont(new Font(FONT, Font.BOLD, 14));
        b.setForeground(Color.WHITE);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ---------------- Database ----------------
    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    private void error(Throwable ex) {
        Throwable root = ex.getCause() != null ? ex.getCause() : ex;
        root.printStackTrace();
        status.setText("Database problem: " + root.getMessage());
        JOptionPane.showMessageDialog(this, "Database Error:\n" + root.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE);
    }

    /** Re-reads the instructors and the booked classes. */
    public void reload() {
        loadInstructors();
        loadClasses();
    }

    private void loadInstructors() {
        new SwingWorker<List<InstructorItem>, Void>() {
            @Override protected List<InstructorItem> doInBackground() throws Exception {
                List<InstructorItem> list = new ArrayList<>();
                try (Connection c = connect();
                     PreparedStatement ps = c.prepareStatement("SELECT id, name, specialization FROM instructors ORDER BY name");
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(new InstructorItem(rs.getInt(1), rs.getString(2), rs.getString(3)));
                }
                return list;
            }
            @Override protected void done() {
                try {
                    InstructorItem keep = (InstructorItem) instructorBox.getSelectedItem();
                    instructorBox.removeAllItems();
                    for (InstructorItem it : get()) {
                        instructorBox.addItem(it);
                        if (keep != null && keep.id == it.id) instructorBox.setSelectedItem(it);
                    }
                } catch (Exception ex) { error(ex); }
            }
        }.execute();
    }

    private void loadClasses() {
        new SwingWorker<List<Object[]>, Void>() {
            @Override protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> rows = new ArrayList<>();
                String sql = "SELECT id, class_date, class_time, instructor, vehicle, class_type, status "
                        + "FROM class_schedule ORDER BY class_date DESC, class_time DESC";
                try (Connection c = connect();
                     PreparedStatement ps = c.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String time = String.valueOf(rs.getTime(3));
                        rows.add(new Object[]{rs.getInt(1), rs.getDate(2),
                                time.length() > 5 ? time.substring(0, 5) : time,
                                rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7)});
                    }
                }
                return rows;
            }
            @Override protected void done() {
                try {
                    List<Object[]> rows = get();
                    model.setRowCount(0);
                    for (Object[] r : rows) model.addRow(r);
                    status.setText(rows.isEmpty()
                            ? "No classes booked yet. Choose an instructor, date and time above."
                            : rows.size() + " classes");
                } catch (Exception ex) { error(ex); }
            }
        }.execute();
    }

    private static int count(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    // ---------------- Book a class ----------------
    private void bookClass() {
        final InstructorItem inst = (InstructorItem) instructorBox.getSelectedItem();
        if (inst == null) {
            JOptionPane.showMessageDialog(this, "No instructor available. Add an instructor first.");
            return;
        }
        final String type = (String) typeBox.getSelectedItem();
        final String vehicle = (String) vehicleBox.getSelectedItem();
        final LocalDate date = ((Date) dateSpinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        final LocalTime time = LocalTime.parse((String) timeBox.getSelectedItem());

        if (!isTheory(type) && NO_VEHICLE.equals(vehicle)) {
            JOptionPane.showMessageDialog(this, "Please choose a vehicle for this class.");
            return;
        }
        if (date.isBefore(LocalDate.now()) || (date.equals(LocalDate.now()) && time.isBefore(LocalTime.now()))) {
            JOptionPane.showMessageDialog(this, "Please choose a date and time in the future.");
            return;
        }

        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                java.sql.Date d = java.sql.Date.valueOf(date);
                Time t = Time.valueOf(time);
                try (Connection c = connect()) {
                    if (count(c, "SELECT COUNT(*) FROM class_schedule WHERE instructor=? AND class_date=? "
                            + "AND class_time=? AND status<>'Cancelled'", inst.name, d, t) > 0)
                        return inst.name + " is already booked at that time. Please pick another time.";

                    if (!NO_VEHICLE.equals(vehicle) && count(c, "SELECT COUNT(*) FROM class_schedule WHERE vehicle=? "
                            + "AND class_date=? AND class_time=? AND status<>'Cancelled'", vehicle, d, t) > 0)
                        return vehicle + " is already booked at that time. Please choose another vehicle or time.";

                    try (PreparedStatement ins = c.prepareStatement(
                            "INSERT INTO class_schedule (class_date, class_time, instructor, vehicle, class_type, status) "
                                    + "VALUES (?, ?, ?, ?, ?, 'Scheduled')")) {
                        ins.setDate(1, d);
                        ins.setTime(2, t);
                        ins.setString(3, inst.name);
                        ins.setString(4, vehicle);
                        ins.setString(5, type);
                        ins.executeUpdate();
                    }
                }
                return null;
            }
            @Override protected void done() {
                try {
                    String problem = get();
                    if (problem != null) {
                        JOptionPane.showMessageDialog(ClassSchedulingPanel.this, problem);
                    } else {
                        JOptionPane.showMessageDialog(ClassSchedulingPanel.this, "Class booked successfully!");
                        loadClasses();
                    }
                } catch (Exception ex) { error(ex); }
            }
        }.execute();
    }

    // ---------------- Cancel a class ----------------
    private void cancelSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a class in the table first.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        final int classId = ((Number) model.getValueAt(modelRow, 0)).intValue();
        if (!"Scheduled".equalsIgnoreCase(String.valueOf(model.getValueAt(modelRow, 6)))) {
            JOptionPane.showMessageDialog(this, "Only scheduled classes can be cancelled.");
            return;
        }
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                try (Connection c = connect();
                     PreparedStatement ps = c.prepareStatement("UPDATE class_schedule SET status='Cancelled' WHERE id=?")) {
                    ps.setInt(1, classId);
                    ps.executeUpdate();
                }
                return null;
            }
            @Override protected void done() {
                try { get(); loadClasses(); } catch (Exception ex) { error(ex); }
            }
        }.execute();
    }

}