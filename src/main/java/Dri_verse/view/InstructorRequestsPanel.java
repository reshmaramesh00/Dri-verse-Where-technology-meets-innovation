package Dri_verse.view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/** Instructor page: see learner requests and accept or reject them. */
public class InstructorRequestsPanel extends JPanel {

    private static final Color PANEL_BORDER = new Color(0x2E2650);
    private static final Color CARD         = new Color(0x1F1938);
    private static final Color ACCENT       = new Color(0x8B5CF6);
    private static final Color TEXT_LIGHT   = new Color(0xF4F1FF);
    private static final Color TEXT_MUTED   = new Color(0x9C93B8);

    private final int instructorUserId;
    private final JLabel status = new JLabel(" ");

    // model columns: 0 = request id, 1 = learner id (both hidden), 2.. visible
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "LearnerId", "Learner", "Email", "Requested", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public InstructorRequestsPanel(int instructorUserId) {
        this.instructorUserId = instructorUserId;
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));
        setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));

        JLabel title = new JLabel("Learner Requests");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(TEXT_LIGHT);
        JLabel sub = new JLabel("Learners who asked to add you as their instructor.");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(title);
        top.add(Box.createVerticalStrut(6));
        top.add(sub);
        add(top, BorderLayout.NORTH);

        table.setRowHeight(32);
        table.setBackground(CARD);
        table.setForeground(TEXT_LIGHT);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.removeColumn(table.getColumnModel().getColumn(0));
        table.removeColumn(table.getColumnModel().getColumn(0));

        DefaultTableCellRenderer header = new DefaultTableCellRenderer();
        header.setBackground(ACCENT);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setOpaque(true);
        table.getTableHeader().setDefaultRenderer(header);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(PANEL_BORDER));
        sp.getViewport().setBackground(CARD);
        add(sp, BorderLayout.CENTER);

        JButton accept = button("Accept", ACCENT);
        JButton reject = button("Reject", new Color(0x7A2E3A));
        JButton refresh = button("Refresh", new Color(0x4A4266));
        accept.addActionListener(e -> respond("ACCEPTED"));
        reject.addActionListener(e -> respond("REJECTED"));
        refresh.addActionListener(e -> reload());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.setOpaque(false);
        btns.add(accept);
        btns.add(reject);
        btns.add(refresh);

        status.setForeground(TEXT_MUTED);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(btns, BorderLayout.WEST);
        bottom.add(status, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        reload();
    }

    public void reload() {
        model.setRowCount(0);
        String sql = "SELECT r.id, r.learner_id, CONCAT(u.first_name, ' ', u.last_name) AS name, "
                   + "u.email, r.created_at, r.status "
                   + "FROM instructor_requests r JOIN users u ON u.user_id = r.learner_id "
                   + "WHERE r.instructor_user_id = ? ORDER BY r.status = 'PENDING' DESC, r.created_at DESC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, instructorUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getInt(1), rs.getInt(2), rs.getString(3),
                            rs.getString(4), rs.getTimestamp(5), rs.getString(6)});
                }
            }
            long pending = 0;
            for (int i = 0; i < model.getRowCount(); i++)
                if ("PENDING".equals(model.getValueAt(i, 5))) pending++;
            status.setText(pending + " pending request(s)");
        } catch (SQLException ex) {
            ex.printStackTrace();
            status.setText("Could not load requests: " + ex.getMessage());
        }
    }

    private void respond(String newStatus) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a request first.");
            return;
        }
        int m = table.convertRowIndexToModel(row);
        if (!"PENDING".equals(model.getValueAt(m, 5))) {
            JOptionPane.showMessageDialog(this, "This request has already been answered.");
            return;
        }
        int requestId = ((Number) model.getValueAt(m, 0)).intValue();
        int learnerId = ((Number) model.getValueAt(m, 1)).intValue();

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int updated;
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE instructor_requests SET status = ?, responded_at = CURRENT_TIMESTAMP "
                      + "WHERE id = ? AND instructor_user_id = ? AND status = 'PENDING'")) {
                    ps.setString(1, newStatus);
                    ps.setInt(2, requestId);
                    ps.setInt(3, instructorUserId);
                    updated = ps.executeUpdate();
                }
                if (updated > 0 && "ACCEPTED".equals(newStatus)) {
                    boolean exists;
                    try (PreparedStatement ps = c.prepareStatement(
                            "SELECT COUNT(*) FROM instructor_students WHERE instructor_id = ? AND student_id = ?")) {
                        ps.setInt(1, instructorUserId);
                        ps.setInt(2, learnerId);
                        try (ResultSet rs = ps.executeQuery()) { rs.next(); exists = rs.getInt(1) > 0; }
                    }
                    if (!exists) {
                        try (PreparedStatement ps = c.prepareStatement(
                                "INSERT INTO instructor_students (instructor_id, student_id) VALUES (?, ?)")) {
                            ps.setInt(1, instructorUserId);
                            ps.setInt(2, learnerId);
                            ps.executeUpdate();
                        }
                    }
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
            reload();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not update request:\n" + ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton button(String text, Color bg) {
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