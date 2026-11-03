package Dri_verse.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/** Learner page: browse registered instructors and send them a request. */
public class InstructorsPanel extends JPanel {

    private static final Color PANEL        = new Color(0x1A1430);
    private static final Color PANEL_BORDER = new Color(0x2E2650);
    private static final Color ACCENT       = new Color(0x8B5CF6);
    private static final Color PINK         = new Color(0xEC4899);
    private static final Color TEXT_LIGHT   = new Color(0xF4F1FF);
    private static final Color TEXT_MUTED   = new Color(0x9C93B8);
    private static final Color GREY         = new Color(0x4A4266);

    private static class Instructor {
        int userId;
        String name, specialization, experience, phone, email, license;
    }

    private final int learnerId;
    private final JPanel grid = new JPanel(new GridLayout(0, 2, 16, 16));

    public InstructorsPanel() { this(0); }

    public InstructorsPanel(int learnerId) {
        this.learnerId = learnerId;
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Our Instructors");
        title.setForeground(TEXT_LIGHT);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));

        JButton refresh = pillButton("Refresh", ACCENT);
        refresh.addActionListener(e -> reload());

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.WEST);
        top.add(refresh, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        grid.setOpaque(false);
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(grid, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(holder);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        reload();
    }

    /** Re-reads instructors and this learner's request statuses. */
    public void reload() {
        grid.removeAll();

        Map<Integer, String> status = new HashMap<>();
        String sql = "SELECT id, user_id, name, email, phone, license_no, experience, specialization "
                   + "FROM instructors WHERE user_id IS NOT NULL ORDER BY name";
        try (Connection c = DBConnection.getConnection()) {

            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT instructor_user_id, status FROM instructor_requests WHERE learner_id = ?")) {
                ps.setInt(1, learnerId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) status.put(rs.getInt(1), rs.getString(2));
                }
            }

            int count = 0;
            try (PreparedStatement ps = c.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Instructor i = new Instructor();
                    i.userId = rs.getInt("user_id");
                    i.name = rs.getString("name");
                    i.email = rs.getString("email");
                    i.phone = rs.getString("phone");
                    i.license = rs.getString("license_no");
                    i.experience = rs.getString("experience");
                    i.specialization = rs.getString("specialization");
                    grid.add(card(i, status.get(i.userId)));
                    count++;
                }
            }
            if (count == 0) grid.add(infoLabel("No registered instructors yet."));

        } catch (SQLException ex) {
            ex.printStackTrace();
            grid.add(infoLabel("Could not load instructors: " + ex.getMessage()));
        }
        grid.revalidate();
        grid.repaint();
    }

    // ---------- card ----------
    private JComponent card(Instructor ins, String reqStatus) {
        JPanel p = new JPanel() {
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
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(14, 18, 14, 18));

        String initial = (ins.name == null || ins.name.isEmpty()) ? "?" : ins.name.substring(0, 1).toUpperCase();
        JLabel avatar = new JLabel(initial, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, ACCENT, getWidth(), getHeight(), PINK));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(46, 46));
        avatar.setForeground(Color.WHITE);
        avatar.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel n = new JLabel(nz(ins.name));
        n.setForeground(TEXT_LIGHT);
        n.setFont(new Font("SansSerif", Font.BOLD, 16));
        JLabel sp = new JLabel(nz(ins.specialization));
        sp.setForeground(new Color(0xB79CFF));
        sp.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        names.add(n);
        names.add(Box.createVerticalStrut(2));
        names.add(sp);

        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setOpaque(false);
        header.add(avatar, BorderLayout.WEST);
        header.add(names, BorderLayout.CENTER);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        p.add(header);

        p.add(Box.createVerticalStrut(10));
        p.add(line("Experience", ins.experience));
        p.add(line("Phone", ins.phone));
        p.add(line("Email", ins.email));
        p.add(line("License", ins.license));
        p.add(Box.createVerticalStrut(10));

        JButton btn;
        if ("PENDING".equals(reqStatus)) {
            btn = pillButton("Request Sent", GREY);
            btn.setEnabled(false);
        } else if ("ACCEPTED".equals(reqStatus)) {
            btn = pillButton("\u2713 Added", new Color(0x2E7D32));
            btn.setEnabled(false);
        } else {
            btn = pillButton("REJECTED".equals(reqStatus) ? "Send Again" : "Send Request", ACCENT);
            btn.addActionListener(e -> sendRequest(ins));
        }
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(btn);
        return p;
    }

    private JLabel line(String label, String value) {
        JLabel l = new JLabel(label + ": " + nz(value));
        l.setForeground(TEXT_MUTED);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel infoLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_LIGHT);
        return l;
    }

    private String nz(String s) { return s == null || s.isEmpty() ? "-" : s; }

    // ---------- send request ----------
    private void sendRequest(Instructor ins) {
        if (learnerId <= 0) {
            JOptionPane.showMessageDialog(this, "Please log in as a learner first.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Send a request to " + ins.name + "?", "Add Instructor", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        String sql = "INSERT INTO instructor_requests (learner_id, instructor_user_id, status) "
                   + "VALUES (?, ?, 'PENDING') "
                   + "ON DUPLICATE KEY UPDATE status = 'PENDING', created_at = CURRENT_TIMESTAMP, responded_at = NULL";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, learnerId);
            ps.setInt(2, ins.userId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Request sent to " + ins.name + ".");
            reload();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not send request:\n" + ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
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
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}