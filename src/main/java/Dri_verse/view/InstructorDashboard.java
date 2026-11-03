package Dri_verse.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/** Dri-verse Instructor Dashboard - same dark-violet look as the learner dashboard. */
public class InstructorDashboard extends JFrame {

    private static final Color OUTER_BG     = new Color(0x0A090F);
    private static final Color SHELL_BG     = new Color(0x120C1F);
    private static final Color PANEL        = new Color(0x1A1430);
    private static final Color PANEL_BORDER = new Color(0x2E2650);
    private static final Color CARD         = new Color(0x1F1938);
    private static final Color ACCENT       = new Color(0x8B5CF6);
    private static final Color ACCENT_2     = new Color(0xC4B5FD);
    private static final Color TEXT_LIGHT   = new Color(0xF4F1FF);
    private static final Color TEXT_MUTED   = new Color(0x9C93B8);
    private static final Color PINK         = new Color(0xEC4899);

    private final int userId;
    private final InstructorDAO.InstructorData data;

    private CardLayout cardLayout;
    private JPanel cards;
    private JLabel pageTitle, placeholderIcon, placeholderTitle;
    private final java.util.List<NavRow> navRows = new java.util.ArrayList<>();
    /** Sections already created, so each one is built only once and then just reloaded. */
    private final java.util.Map<String, InstructorPanels.Section> sections = new java.util.HashMap<>();
    /** Learner requests screen (created the first time it is opened). */
    private InstructorRequestsPanel requestsPanel;

    private static class NavRow {
        final RoundedPanel row; final JLabel icon, label; final String key;
        NavRow(RoundedPanel row, JLabel icon, JLabel label, String key) {
            this.row = row; this.icon = icon; this.label = label; this.key = key;
        }
    }

    public InstructorDashboard(int userId) {
        this.userId = userId;
        this.data = new InstructorDAO().loadForUser(userId);

        setTitle("Dri-verse | Instructor Dashboard");
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

    // ---------------- SIDEBAR ----------------
    private JComponent buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(232, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(22, 18, 22, 12));

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        logoRow.setOpaque(false);
        logoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel logoText = new JLabel("Dri-verse");
        logoText.setFont(new Font("SansSerif", Font.BOLD, 18));
        logoText.setForeground(TEXT_LIGHT);
        logoRow.add(logoText);
        sidebar.add(logoRow);
        sidebar.add(Box.createVerticalStrut(20));

        sidebar.add(buildUserCard());
        sidebar.add(Box.createVerticalStrut(18));
        String[] icons = {"\u2302", "\uD83C\uDF93", "\uD83D\uDCE9", "\uD83D\uDCC5", "\uD83D\uDCDD", "\u2705", "\uD83D\uDCB3", "\uD83D\uDCAC"};
        String[] items = {"Dashboard", "My Students", "Learner Requests", "Class Schedule", "Test Evaluation", "Attendance", "Payments", "Feedback"};
        for (int i = 0; i < items.length; i++) {
            sidebar.add(sidebarItem(icons[i], items[i], i == 0));
            sidebar.add(Box.createVerticalStrut(4));
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(sidebarItem("\u2699", "Setting", false));
        return sidebar;
    }

    private JComponent buildUserCard() {
        RoundedPanel card = new RoundedPanel(14, PANEL, PANEL_BORDER);
        card.setLayout(new BorderLayout(8, 0));
        card.setMaximumSize(new Dimension(500, 54));
        card.setPreferredSize(new Dimension(500, 54));
        card.setBorder(new EmptyBorder(0, 12, 0, 10));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel avatar = new JLabel(initialOf(data.instructorName), SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, ACCENT, getWidth(), getHeight(), PINK));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(32, 32));
        avatar.setForeground(Color.WHITE);
        avatar.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(data.instructorName);
        name.setFont(new Font("SansSerif", Font.BOLD, 12));
        name.setForeground(TEXT_LIGHT);
        JLabel role = new JLabel("Instructor");
        role.setFont(new Font("SansSerif", Font.PLAIN, 10));
        role.setForeground(TEXT_MUTED);
        names.add(name);
        names.add(role);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(avatar);
        left.add(names);

        JLabel chevron = new JLabel("\u25BE");
        chevron.setForeground(TEXT_MUTED);

        card.add(left, BorderLayout.WEST);
        card.add(chevron, BorderLayout.EAST);

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JPopupMenu menu = new JPopupMenu();
                JMenuItem logout = new JMenuItem("Logout");
                logout.addActionListener(ev -> confirmLogout());
                menu.add(logout);
                menu.show(card, 0, card.getHeight());
            }
        });
        return card;
    }

    private String initialOf(String n) {
        return (n == null || n.isEmpty()) ? "?" : n.substring(0, 1).toUpperCase();
    }

    private void confirmLogout() {
        int c = JOptionPane.showConfirmDialog(this, "Log out of Dri-verse?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    private JComponent sidebarItem(String icon, String text, boolean active) {
        RoundedPanel row = new RoundedPanel(10, active ? ACCENT : PANEL, null);
        row.setLayout(new BorderLayout(10, 0));
        row.setMaximumSize(new Dimension(500, 36));
        row.setPreferredSize(new Dimension(500, 36));
        row.setBorder(new EmptyBorder(0, 12, 0, 10));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        iconLbl.setForeground(active ? Color.WHITE : ACCENT_2);

        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 12));
        label.setForeground(active ? Color.WHITE : TEXT_MUTED);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(iconLbl);
        left.add(label);
        row.add(left, BorderLayout.WEST);

        navRows.add(new NavRow(row, iconLbl, label, text));

        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { navigateTo(text, icon); }
        });
        return row;
    }

    /** Builds the screen for a sidebar item. Returns null for items with no screen yet (e.g. Setting). */
    private InstructorPanels.Section createSection(String key) {
        switch (key) {
            case "My Students":     return new InstructorPanels.StudentsPanel(userId);
            case "Class Schedule":  return new InstructorPanels.SchedulePanel(userId);
            case "Test Evaluation": return new InstructorPanels.EvaluationPanel(userId);
            case "Attendance":      return new InstructorPanels.AttendancePanel(userId);
            case "Feedback":        return new InstructorPanels.FeedbackPanel(userId);
            case "Payments":        return new InstructorPanels.PaymentsPanel(userId);
            default:                return null;
        }
    }

    private void navigateTo(String key, String icon) {
        for (NavRow nr : navRows) {
            boolean active = nr.key.equals(key);
            nr.row.setFillColor(active ? ACCENT : PANEL);
            nr.icon.setForeground(active ? Color.WHITE : ACCENT_2);
            nr.label.setForeground(active ? Color.WHITE : TEXT_MUTED);
            nr.label.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 12));
        }
        pageTitle.setText("Dashboard".equals(key) ? "Instructor Overview" : key);

        if ("Dashboard".equals(key)) {
            cardLayout.show(cards, "dashboard");
            return;
        }

        // Learner requests: created once, reloaded every time it is reopened
        if ("Learner Requests".equals(key)) {
            if (requestsPanel == null) {
                requestsPanel = new InstructorRequestsPanel(userId);
                cards.add(requestsPanel, key);
            } else {
                requestsPanel.reload();
            }
            cardLayout.show(cards, key);
            cards.revalidate();
            cards.repaint();
            return;
        }

        InstructorPanels.Section s = sections.get(key);
        if (s == null) {
            s = createSection(key);
            if (s != null) {
                sections.put(key, s);
                cards.add(s, key);
            }
        } else {
            s.reload();   // refresh data each time the tab is reopened
        }

        if (s != null) {
            cardLayout.show(cards, key);
            cards.revalidate();
            cards.repaint();
        } else {
            placeholderIcon.setText(icon);
            placeholderTitle.setText(key);
            cardLayout.show(cards, "placeholder");
        }
    }

    // ---------------- MAIN ----------------
    private JComponent buildMain() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(22, 20, 22, 22));

        pageTitle = new JLabel("Instructor Overview");
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        pageTitle.setForeground(TEXT_LIGHT);
        pageTitle.setBorder(new EmptyBorder(0, 0, 18, 0));
        wrap.add(pageTitle, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.setOpaque(false);
        cards.add(buildDashboardCard(), "dashboard");
        cards.add(buildPlaceholderCard(), "placeholder");
        wrap.add(cards, BorderLayout.CENTER);
        return wrap;
    }

    private JScrollPane buildDashboardCard() {
        JPanel main = new JPanel();
        main.setOpaque(false);
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.add(buildKpiRow());
        main.add(Box.createVerticalStrut(18));
        main.add(buildListsRow());

        JScrollPane scroll = new JScrollPane(main);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JComponent buildPlaceholderCard() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        placeholderIcon = new JLabel("\uD83C\uDF93", SwingConstants.CENTER);
        placeholderIcon.setFont(new Font("SansSerif", Font.PLAIN, 52));
        placeholderIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        placeholderTitle = new JLabel("Coming Soon", SwingConstants.CENTER);
        placeholderTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        placeholderTitle.setForeground(TEXT_LIGHT);
        placeholderTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        placeholderTitle.setBorder(new EmptyBorder(14, 0, 6, 0));

        JLabel sub = new JLabel("This section is ready to be wired up to real data.", SwingConstants.CENTER);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton back = pillButton("\u2190  Back to Dashboard");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> navigateTo("Dashboard", "\u2302"));

        col.add(placeholderIcon);
        col.add(placeholderTitle);
        col.add(sub);
        col.add(Box.createVerticalStrut(18));
        col.add(back);
        wrap.add(col);
        return wrap;
    }

    // ---------------- KPI ROW ----------------
    private JComponent buildKpiRow() {
        JPanel strip = new JPanel(new GridLayout(1, 4, 16, 0));
        strip.setOpaque(false);
        strip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));
        strip.setAlignmentX(Component.LEFT_ALIGNMENT);

        strip.add(kpiCard("\uD83C\uDF93", "Students", String.valueOf(data.totalStudents),
                "Assigned to you", new Color(0x1E1B2E), new Color(0x2A1F4A)));
        strip.add(kpiCard("\uD83D\uDCC5", "Today", String.valueOf(data.classesToday),
                "Classes scheduled today", new Color(0x241C3D), new Color(0x3B2160)));
        strip.add(kpiCard("\u23F3", "Upcoming", String.valueOf(data.upcomingClasses),
                "Classes from today onward", new Color(0x2A1B3F), new Color(0x4A1F5E)));
        strip.add(kpiCard("\uD83D\uDCC8", "Avg Progress", data.avgProgress + "%",
                "Across your students", new Color(0x1B2440), new Color(0x24305E)));
        return strip;
    }

    private JComponent kpiCard(String icon, String label, String value, String sub, Color c1, Color c2) {
        GradientPanel card = new GradientPanel(16, c1, c2);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JLabel labelLbl = new JLabel("  " + label);
        labelLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        labelLbl.setForeground(TEXT_MUTED);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        top.add(iconLbl);
        top.add(labelLbl);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("SansSerif", Font.BOLD, 26));
        valueLbl.setForeground(TEXT_LIGHT);
        valueLbl.setBorder(new EmptyBorder(10, 0, 2, 0));

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subLbl.setForeground(TEXT_MUTED);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(valueLbl);
        bottom.add(subLbl);

        card.add(top, BorderLayout.NORTH);
        card.add(bottom, BorderLayout.CENTER);
        return card;
    }

    // ---------------- LISTS ROW ----------------
    private JComponent buildListsRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(buildClassesPanel());
        row.add(buildStudentsPanel());
        return row;
    }

    private RoundedPanel panelWithHeading(String title) {
        RoundedPanel card = new RoundedPanel(16, PANEL, PANEL_BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("SansSerif", Font.BOLD, 14));
        heading.setForeground(TEXT_LIGHT);
        heading.setBorder(new EmptyBorder(0, 0, 12, 0));
        card.add(heading, BorderLayout.NORTH);
        return card;
    }

    private JLabel emptyLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(TEXT_MUTED);
        return l;
    }

    private JComponent buildClassesPanel() {
        RoundedPanel card = panelWithHeading("Upcoming Classes");
        if (data.classes.isEmpty()) {
            card.add(emptyLabel("No upcoming classes."), BorderLayout.CENTER);
            return card;
        }
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        for (int i = 0; i < data.classes.size(); i++) {
            InstructorDAO.ClassItem c = data.classes.get(i);
            list.add(classRow(i + 1, c));
            if (i < data.classes.size() - 1) list.add(Box.createVerticalStrut(10));
        }
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JComponent classRow(int number, InstructorDAO.ClassItem c) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(600, 48));

        JLabel num = new JLabel(String.valueOf(number), SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        num.setPreferredSize(new Dimension(28, 28));
        num.setForeground(ACCENT_2);
        num.setFont(new Font("SansSerif", Font.BOLD, 12));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(c.title);
        t.setFont(new Font("SansSerif", Font.BOLD, 12));
        t.setForeground(TEXT_LIGHT);
        JLabel s = new JLabel(c.when + "  \u2022  " + c.location);
        s.setFont(new Font("SansSerif", Font.PLAIN, 11));
        s.setForeground(TEXT_MUTED);
        text.add(t);
        text.add(s);

        JLabel count = new JLabel(c.students + (c.students == 1 ? " student" : " students"));
        count.setFont(new Font("SansSerif", Font.PLAIN, 11));
        count.setForeground(ACCENT_2);

        row.add(num, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        row.add(count, BorderLayout.EAST);
        return row;
    }

    private JComponent buildStudentsPanel() {
        RoundedPanel card = panelWithHeading("My Students");
        if (data.students.isEmpty()) {
            card.add(emptyLabel("No students assigned yet."), BorderLayout.CENTER);
            return card;
        }
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        for (InstructorDAO.StudentItem s : data.students) {
            list.add(studentRow(s));
            list.add(Box.createVerticalStrut(8));
        }
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JComponent studentRow(InstructorDAO.StudentItem s) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(600, 30));

        JLabel name = new JLabel(s.name);
        name.setFont(new Font("SansSerif", Font.BOLD, 12));
        name.setForeground(TEXT_LIGHT);
        name.setPreferredSize(new Dimension(140, 20));

        JLabel pct = new JLabel(s.progress + "%", SwingConstants.RIGHT);
        pct.setFont(new Font("SansSerif", Font.BOLD, 11));
        pct.setForeground(ACCENT_2);
        pct.setPreferredSize(new Dimension(40, 20));

        row.add(name, BorderLayout.WEST);
        row.add(new ProgressBar(s.progress), BorderLayout.CENTER);
        row.add(pct, BorderLayout.EAST);
        return row;
    }

    // ---------------- PAINTED COMPONENTS ----------------
    private static class ProgressBar extends JPanel {
        private final int pct;
        ProgressBar(int pct) {
            this.pct = Math.max(0, Math.min(100, pct));
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int barH = 8, y = (getHeight() - barH) / 2;
            g2.setColor(CARD);
            g2.fill(new RoundRectangle2D.Double(0, y, getWidth(), barH, barH, barH));
            g2.setColor(ACCENT);
            g2.fill(new RoundRectangle2D.Double(0, y, getWidth() * pct / 100.0, barH, barH, barH));
            g2.dispose();
        }
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

    private static class GradientPanel extends JPanel {
        private final int radius;
        private final Color c1, c2;
        GradientPanel(int radius, Color c1, Color c2) {
            this.radius = radius; this.c1 = c1; this.c2 = c2;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2));
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private JButton pillButton(String text) {
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InstructorDashboard(1).setVisible(true));
    }
}