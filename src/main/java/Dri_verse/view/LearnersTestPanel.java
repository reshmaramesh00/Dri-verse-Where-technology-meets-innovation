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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * "Learner's Test" page: book an RTO learner's licence test and view bookings.
 * Table: learners_test(id, test_date, test_time, test_center, application_no, status, result)
 */
public class LearnersTestPanel extends JPanel {

    private static final String DB_URL  = "jdbc:mysql://localhost:3306/drivers_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "your_password_here";

    private static final Color PANEL        = new Color(0x1A1430);
    private static final Color PANEL_BORDER = new Color(0x2E2650);
    private static final Color CARD         = new Color(0x1F1938);
    private static final Color ACCENT       = new Color(0x8B5CF6);
    private static final Color TEXT_LIGHT   = new Color(0xF4F1FF);
    private static final Color TEXT_MUTED   = new Color(0x9C93B8);

    private static final String[] CENTERS = {
            "RTO Ernakulam", "RTO Kochi", "RTO Thiruvananthapuram", "RTO Kozhikode", "RTO Thrissur"
    };
    private static final String[] SLOTS = {
            "09:00", "10:00", "11:00", "11:30", "12:00", "14:00", "15:00"
    };

    private final JComboBox<String> centerBox = new JComboBox<>(CENTERS);
    private final JComboBox<String> timeBox = new JComboBox<>(SLOTS);
    private final JTextField appNo = new JTextField(14);
    private final JSpinner dateSpinner =
            new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
    private final JLabel status = new JLabel(" ");

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Date", "Time", "Test Center", "Application No", "Status", "Result"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public LearnersTestPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));
        setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));

        JLabel heading = new JLabel("Learner's Licence Test");
        heading.setFont(new Font("SansSerif", Font.BOLD, 24));
        heading.setForeground(TEXT_LIGHT);
        JLabel sub = new JLabel("Book your RTO learner's test and track its status and result.");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(heading);
        top.add(Box.createVerticalStrut(6));
        top.add(sub);
        top.add(Box.createVerticalStrut(14));
        top.add(buildForm());
        add(top, BorderLayout.NORTH);

        add(buildTable(), BorderLayout.CENTER);

        status.setFont(new Font("SansSerif", Font.PLAIN, 13));
        status.setForeground(TEXT_MUTED);
        add(status, BorderLayout.SOUTH);

        reload();
    }

    // ---------- UI ----------
    private JComponent buildForm() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PANEL_BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        String[] labels = {"Test Center", "Date", "Time", "Application No (optional)"};
        JComponent[] inputs = {centerBox, dateSpinner, timeBox, appNo};
        for (int i = 0; i < inputs.length; i++) {
            JLabel l = new JLabel(labels[i]);
            l.setForeground(TEXT_MUTED);
            l.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.gridx = i; g.gridy = 0; g.weightx = 1;
            card.add(l, g);
            g.gridy = 1;
            card.add(inputs[i], g);
        }

        JButton book = pillButton("Book Test");
        book.addActionListener(e -> bookTest());
        JButton cancel = pillButton("Cancel Selected");
        cancel.addActionListener(e -> cancelSelected());
        JButton refresh = pillButton("Refresh");
        refresh.addActionListener(e -> reload());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setOpaque(false);
        btns.add(book);
        btns.add(cancel);
        btns.add(refresh);
        g.gridx = 0; g.gridy = 2; g.gridwidth = 4;
        g.insets = new Insets(10, 6, 0, 6);
        card.add(btns, g);
        return card;
    }

    private JComponent buildTable() {
        table.setRowHeight(32);
        table.setBackground(CARD);
        table.setForeground(TEXT_LIGHT);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.removeColumn(table.getColumnModel().getColumn(0));   // hide ID

        DefaultTableCellRenderer header = new DefaultTableCellRenderer();
        header.setBackground(ACCENT);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setOpaque(true);
        table.getTableHeader().setDefaultRenderer(header);

        DefaultTableCellRenderer cell = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel,
                                                                     boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tb, v, sel, focus, row, col);
                if (sel) {
                    c.setBackground(ACCENT);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? CARD : new Color(0x241C3D));
                    c.setForeground(TEXT_LIGHT);
                    String s = String.valueOf(v).toUpperCase();
                    int mc = tb.convertColumnIndexToModel(col);
                    if (mc == 5) {          // status
                        c.setForeground("CANCELLED".equals(s) ? new Color(0xFF8A8A)
                                : "COMPLETED".equals(s) ? new Color(0x6EE7A8) : new Color(0x8FA8FF));
                    } else if (mc == 6) {   // result
                        c.setForeground("PASSED".equals(s) ? new Color(0x6EE7A8)
                                : "FAILED".equals(s) ? new Color(0xFF8A8A) : new Color(0xFBBF24));
                    }
                }
                return c;
            }
        };
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(cell);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(PANEL_BORDER));
        sp.getViewport().setBackground(CARD);
        return sp;
    }

    private JButton pillButton(String text) {
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
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ---------- Database ----------
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public void reload() {
        model.setRowCount(0);
        String sql = "SELECT id, test_date, test_time, test_center, application_no, status, result "
                   + "FROM learners_test ORDER BY test_date, test_time";
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String time = String.valueOf(rs.getTime("test_time"));
                model.addRow(new Object[]{
                        rs.getInt("id"), rs.getDate("test_date"),
                        time.length() > 5 ? time.substring(0, 5) : time,
                        rs.getString("test_center"),
                        rs.getString("application_no") == null ? "-" : rs.getString("application_no"),
                        rs.getString("status"), rs.getString("result")});
            }
            status.setText(model.getRowCount() == 0
                    ? "No tests booked yet. Choose a center, date and time above."
                    : model.getRowCount() + " test(s)");
        } catch (SQLException ex) {
            ex.printStackTrace();
            status.setText("Could not load tests: " + ex.getMessage());
        }
    }

    private void bookTest() {
        LocalDate date = ((Date) dateSpinner.getValue()).toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalTime time = LocalTime.parse((String) timeBox.getSelectedItem());
        String center = (String) centerBox.getSelectedItem();
        String app = appNo.getText().trim();

        if (date.isBefore(LocalDate.now())
                || (date.equals(LocalDate.now()) && time.isBefore(LocalTime.now()))) {
            JOptionPane.showMessageDialog(this, "Please choose a date and time in the future.");
            return;
        }

        try (Connection c = connect()) {
            try (PreparedStatement chk = c.prepareStatement(
                    "SELECT COUNT(*) FROM learners_test WHERE test_center=? AND test_date=? "
                  + "AND test_time=? AND status<>'Cancelled'")) {
                chk.setString(1, center);
                chk.setDate(2, java.sql.Date.valueOf(date));
                chk.setTime(3, Time.valueOf(time));
                try (ResultSet rs = chk.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this,
                                "That slot is already booked at " + center + ". Please pick another.");
                        return;
                    }
                }
            }
            try (PreparedStatement ins = c.prepareStatement(
                    "INSERT INTO learners_test (test_date, test_time, test_center, application_no) "
                  + "VALUES (?, ?, ?, ?)")) {
                ins.setDate(1, java.sql.Date.valueOf(date));
                ins.setTime(2, Time.valueOf(time));
                ins.setString(3, center);
                ins.setString(4, app.isEmpty() ? null : app);
                ins.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Test booked successfully!");
            appNo.setText("");
            reload();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not book test:\n" + ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a test in the table first.");
            return;
        }
        int m = table.convertRowIndexToModel(row);
        if (!"Booked".equalsIgnoreCase(String.valueOf(model.getValueAt(m, 5)))) {
            JOptionPane.showMessageDialog(this, "Only booked tests can be cancelled.");
            return;
        }
        int id = ((Number) model.getValueAt(m, 0)).intValue();
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE learners_test SET status='Cancelled' WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            reload();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not cancel test:\n" + ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }
}