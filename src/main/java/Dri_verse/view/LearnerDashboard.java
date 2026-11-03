package Dri_verse.view;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

/**
 * Dri-verse Learner Dashboard - Java Swing + JDBC
 *
 * Dark-purple shell card with sidebar, KPI tiles, training-load heatmap
 * (real data from class_schedule), tracking map, upcoming classes,
 * module list and results table.
 */
public class LearnerDashboard extends JFrame {

    // ---------- Palette ----------
    private static final Color OUTER_BG    = new Color(0x0A090F);
    private static final Color SHELL_BG    = new Color(0x120C1F);
    private static final Color PANEL       = new Color(0x1A1430);
    private static final Color PANEL_BORDER= new Color(0x2E2650);
    private static final Color CARD        = new Color(0x1F1938);
    private static final Color CARD_HOVER  = new Color(0x281F45);
    private static final Color ACCENT      = new Color(0x8B5CF6);
    private static final Color ACCENT_2    = new Color(0xC4B5FD);
    private static final Color TEXT_LIGHT  = new Color(0xF4F1FF);
    private static final Color TEXT_MUTED  = new Color(0x9C93B8);
    private static final Color GOOD        = new Color(0x34D399);
    private static final Color GOOD_BG     = new Color(0x143024);
    private static final Color PINK        = new Color(0xEC4899);

    private final DashboardDAO.DashboardData data;
    private final int userId;

    private CardLayout contentCardLayout;
    private JPanel contentCards;
    private JLabel pageTitle;
    private JLabel placeholderIcon;
    private JLabel placeholderTitle;
    private final java.util.List<NavRow> navRows = new java.util.ArrayList<>();

    /** Bundles a sidebar row with the labels inside it, so we can re-style it on click. */
    private static class NavRow {
        final RoundedPanel row;
        final JLabel icon;
        final JLabel label;
        final String key;
        NavRow(RoundedPanel row, JLabel icon, JLabel label, String key) {
            this.row = row; this.icon = icon; this.label = label; this.key = key;
        }
    }

    public LearnerDashboard(int userId) {
        this.userId = userId;
        this.data = new DashboardDAO().loadForUser(userId);

        setTitle("Dri-verse | Learner Dashboard");
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
        shell.setLayout(new BorderLayout(0, 0));
        shell.setBorder(new EmptyBorder(0, 0, 0, 0));

        shell.add(buildSidebar(), BorderLayout.WEST);
        shell.add(buildMain(), BorderLayout.CENTER);

        outer.add(shell, BorderLayout.CENTER);
        add(outer, BorderLayout.CENTER);
    }

    // ==================================================================
    // SIDEBAR
    // ==================================================================
    private JComponent buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(232, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(22, 18, 22, 12));

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        logoRow.setOpaque(false);
        logoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel logoIcon = new JLabel("\uD83D\uDE97");
        logoIcon.setFont(new Font("SansSerif", Font.PLAIN, 20));
        JLabel logoText = new JLabel("Dri-verse");
        logoText.setFont(new Font("SansSerif", Font.BOLD, 18));
        logoText.setForeground(TEXT_LIGHT);
        logoRow.add(logoIcon);
        logoRow.add(logoText);
        sidebar.add(logoRow);
        sidebar.add(Box.createVerticalStrut(20));

        sidebar.add(buildUserCard());
        sidebar.add(Box.createVerticalStrut(18));

        String[] icons = {"\u2302", "\uD83D\uDCC5", "\uD83C\uDFEB", "\uD83D\uDCDD", "\uD83E\uDDE9",
                "\uD83D\uDCB3", "\uD83D\uDCCD", "\uD83D\uDCAC"};
        String[] items = {"Dashboard", "Class Scheduling", "Instructors", "Learner's Test",
                "Mock Tests", "Payments", "Live Tracking", "Feedback"};
        for (int i = 0; i < items.length; i++) {
            sidebar.add(sidebarItem(icons[i], items[i], i == 0));
            sidebar.add(Box.createVerticalStrut(4));
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(sidebarItem("\u2699", "Setting", false));
        sidebar.add(Box.createVerticalStrut(14));
        sidebar.add(buildTrialCard());
        return sidebar;
    }

    private JComponent buildUserCard() {
        RoundedPanel card = new RoundedPanel(14, PANEL, PANEL_BORDER);
        card.setLayout(new BorderLayout(8, 0));
        card.setMaximumSize(new Dimension(500, 54));
        card.setPreferredSize(new Dimension(500, 54));
        card.setBorder(new EmptyBorder(0, 12, 0, 10));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel avatar = new JLabel(initialOf(data.learnerName), SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT, getWidth(), getHeight(), PINK);
                g2.setPaint(gp);
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
        JLabel name = new JLabel(data.learnerName);
        name.setFont(new Font("SansSerif", Font.BOLD, 12));
        name.setForeground(TEXT_LIGHT);
        JLabel role = new JLabel("Learner");
        role.setFont(new Font("SansSerif", Font.PLAIN, 10));
        role.setForeground(TEXT_MUTED);
        names.add(name);
        names.add(role);

        JLabel chevron = new JLabel("\u25BE");
        chevron.setForeground(TEXT_MUTED);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(avatar);
        left.add(names);

        card.add(left, BorderLayout.WEST);
        card.add(chevron, BorderLayout.EAST);

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JPopupMenu menu = new JPopupMenu();
                JMenuItem viewProfile = new JMenuItem("View Profile");
                viewProfile.addActionListener(ev -> openProfileFrame());
                JMenuItem logout = new JMenuItem("Logout");
                logout.addActionListener(ev -> confirmLogout());
                menu.add(viewProfile);
                menu.add(logout);
                menu.show(card, 0, card.getHeight());
            }
        });
        return card;
    }

    private String initialOf(String name) {
        return (name == null || name.isEmpty()) ? "?" : name.substring(0, 1).toUpperCase();
    }

    private void confirmLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Log out of Dri-verse?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    private JComponent sidebarItem(String icon, String text, boolean active) {
        RoundedPanel row = new RoundedPanel(10, active ? ACCENT : PANEL, null);
        row.setOpaque(false);
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

        if (!"Logout".equals(text)) {
            navRows.add(new NavRow(row, iconLbl, label, text));
        }

        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if ("Logout".equals(text)) {
                    confirmLogout();
                } else {
                    navigateTo(text, icon);
                }
            }
        });
        return row;
    }

    /** Highlights the clicked sidebar row and swaps the visible content card. */
    private void navigateTo(String key, String icon) {
        for (NavRow nr : navRows) {
            boolean active = nr.key.equals(key);
            nr.row.setFillColor(active ? ACCENT : PANEL);
            nr.icon.setForeground(active ? Color.WHITE : ACCENT_2);
            nr.label.setForeground(active ? Color.WHITE : TEXT_MUTED);
            nr.label.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 12));
        }
        pageTitle.setText("Dashboard".equals(key) ? "Learner Overview" : key);

        if ("Dashboard".equals(key)) {
            contentCardLayout.show(contentCards, "dashboard");
        } else if ("Class Scheduling".equals(key)) {
            contentCardLayout.show(contentCards, "classScheduling");
        } else if ("Instructors".equals(key)) {
            contentCardLayout.show(contentCards, "instructors");
        } else if ("Learner's Test".equals(key)) {
            contentCardLayout.show(contentCards, "learnersTest");
        } else if ("Mock Tests".equals(key)) {
            contentCardLayout.show(contentCards, "mockTests");
        } else if ("Payments".equals(key)) {
            contentCardLayout.show(contentCards, "payments");
        } else if ("Live Tracking".equals(key)) {
            contentCardLayout.show(contentCards, "liveTracking");
        } else if ("Feedback".equals(key)) {
            contentCardLayout.show(contentCards, "feedback");
        } else if ("Setting".equals(key)) {
            contentCardLayout.show(contentCards, "settings");
        } else if ("Upgrade Plan".equals(key)) {
            contentCardLayout.show(contentCards, "upgradePlan");
        } else {
            placeholderIcon.setText(icon);
            placeholderTitle.setText(key);
            contentCardLayout.show(contentCards, "placeholder");
        }
    }

    private JComponent buildTrialCard() {
        GradientRoundedPanel card = new GradientRoundedPanel(14, new Color(0x1D1730), new Color(0x0F0B1C));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));
        card.setMaximumSize(new Dimension(500, 74));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel line1 = new JLabel("Trial period in 28 days");
        line1.setFont(new Font("SansSerif", Font.PLAIN, 11));
        line1.setForeground(TEXT_MUTED);

        JLabel line2 = new JLabel("Upgrade Plan  \u2192");
        line2.setFont(new Font("SansSerif", Font.BOLD, 13));
        line2.setForeground(TEXT_LIGHT);
        line2.setBorder(new EmptyBorder(4, 0, 0, 0));

        card.add(line1);
        card.add(line2);

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                navigateTo("Upgrade Plan", "\u2B50");
            }
        });
        return card;
    }

    // ==================================================================
    // MAIN CONTENT
    // ==================================================================
    private JComponent buildMain() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(22, 20, 22, 22));

        pageTitle = new JLabel("Learner Overview");
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        pageTitle.setForeground(TEXT_LIGHT);
        pageTitle.setBorder(new EmptyBorder(0, 0, 18, 0));
        wrap.add(pageTitle, BorderLayout.NORTH);

        contentCardLayout = new CardLayout();
        contentCards = new JPanel(contentCardLayout);
        contentCards.setOpaque(false);
        contentCards.add(buildDashboardCard(), "dashboard");
        contentCards.add(buildPlaceholderCard(), "placeholder");
        contentCards.add(buildMockTestsCard(), "mockTests");
        contentCards.add(buildPaymentsCard(), "payments");
        contentCards.add(buildLiveTrackingCard(), "liveTracking");
        contentCards.add(buildFeedbackCard(), "feedback");
        contentCards.add(buildSettingsCard(), "settings");
        contentCards.add(buildUpgradePlanCard(), "upgradePlan");
        contentCards.add(new InstructorsPanel(userId), "instructors");
        contentCards.add(new ClassSchedulingPanel(), "classScheduling");
        contentCards.add(new LearnersTestPanel(), "learnersTest");

        wrap.add(contentCards, BorderLayout.CENTER);
        return wrap;
    }

    /** The real dashboard: KPI tiles, heatmap, tracking map, upcoming classes, modules, results. */
    private JScrollPane buildDashboardCard() {
        JPanel main = new JPanel();
        main.setOpaque(false);
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

        main.add(buildKpiRow());
        main.add(Box.createVerticalStrut(18));
        main.add(buildMidRow());
        main.add(Box.createVerticalStrut(18));
        main.add(buildUpcomingClassesPanel());
        main.add(Box.createVerticalStrut(18));
        main.add(buildBottomRow());
        main.add(Box.createVerticalStrut(10));

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

        placeholderIcon = new JLabel("\uD83D\uDE97", SwingConstants.CENTER);
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

        JButton back = pillButton("\u2190  Back to Dashboard", ACCENT, Color.WHITE, true);
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

    // ---------- KPI row ----------
    private JComponent buildKpiRow() {
        JPanel strip = new JPanel(new GridLayout(1, 4, 16, 0));
        strip.setOpaque(false);
        strip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));
        strip.setAlignmentX(Component.LEFT_ALIGNMENT);

        int overall = (data.theoryPct + data.practicalPct + data.mockTestPct + data.assignmentPct) / 4;

        strip.add(kpiCard("\uD83D\uDCD8", "Classes", String.valueOf(data.classesEnrolled),
                "Total enrolled modules", "+2 new", new Color(0x1E1B2E), new Color(0x2A1F4A)));
        strip.add(kpiCard("\u23F3", "Upcoming", String.valueOf(data.upcomingClassesCount),
                "Scheduled classes", "On track", new Color(0x241C3D), new Color(0x3B2160)));
        strip.add(kpiCard("\u2705", "Tests", String.valueOf(data.testsCompleted),
                "Completed so far", "+1 this wk", new Color(0x2A1B3F), new Color(0x4A1F5E)));
        strip.add(kpiCard("\uD83D\uDCC8", "Progress", overall + "%",
                "Theory + practical avg", "+5%", new Color(0x1B2440), new Color(0x24305E)));
        return strip;
    }

    private JComponent kpiCard(String icon, String label, String value, String sub,
                                String badge, Color c1, Color c2) {
        GradientRoundedPanel card = new GradientRoundedPanel(16, c1, c2);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JLabel labelLbl = new JLabel("  " + label);
        labelLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        labelLbl.setForeground(TEXT_MUTED);
        JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topLeft.setOpaque(false);
        topLeft.add(iconLbl);
        topLeft.add(labelLbl);

        JLabel badgeLbl = new JLabel(badge, SwingConstants.CENTER);
        badgeLbl.setOpaque(true);
        badgeLbl.setBackground(GOOD_BG);
        badgeLbl.setForeground(GOOD);
        badgeLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        badgeLbl.setBorder(new EmptyBorder(3, 8, 3, 8));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(topLeft, BorderLayout.WEST);
        top.add(badgeLbl, BorderLayout.EAST);

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

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                openCarFrame(label, icon);
            }
        });
        return card;
    }

    // ---------- Middle row: heatmap + tracking map ----------
    private JComponent buildMidRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(buildHeatmapPanel());
        row.add(buildTrackingPanel());
        return row;
    }

    private JComponent buildHeatmapPanel() {
        RoundedPanel card = new RoundedPanel(16, PANEL, PANEL_BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 14, 18));

        JLabel heading = new JLabel("Training Load by Location");
        heading.setFont(new Font("SansSerif", Font.BOLD, 14));
        heading.setForeground(TEXT_LIGHT);

        JLabel filter = new JLabel("Classes per week");
        filter.setFont(new Font("SansSerif", Font.PLAIN, 11));
        filter.setForeground(TEXT_MUTED);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(heading, BorderLayout.WEST);
        head.add(filter, BorderLayout.EAST);

        card.add(head, BorderLayout.NORTH);
        card.add(new HeatmapGrid(), BorderLayout.CENTER);
        return card;
    }

    /** Heatmap: rows = locations, columns = weeks (4 back .. 4 ahead), cell = number of classes. */
    private class HeatmapGrid extends JPanel {
        private final String[] rows = DashboardDAO.LOCATIONS;
        private final int[][] values = data.locationLoad;

        HeatmapGrid() { setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int max = 1;
            for (int[] r : values) for (int v : r) max = Math.max(max, v);

            int labelW = 130, bottomH = 18;
            int gridW = getWidth() - labelW - 4;
            int gridH = getHeight() - bottomH - 4;
            int cols = values[0].length;
            int rowH = gridH / rows.length;
            int colW = gridW / cols;

            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            for (int r = 0; r < rows.length; r++) {
                g2.setColor(TEXT_MUTED);
                g2.drawString(rows[r], 0, r * rowH + rowH / 2 + 4);
                for (int c = 0; c < cols; c++) {
                    int v = values[r][c];
                    float alpha = v == 0 ? 0.08f : 0.25f + (v / (float) max) * 0.7f;
                    g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(),
                            Math.min(255, (int) (alpha * 255))));
                    int x = labelW + c * colW, y = r * rowH;
                    g2.fill(new RoundRectangle2D.Double(x, y, colW - 4, rowH - 4, 6, 6));
                    if (v > 0) {
                        g2.setColor(Color.WHITE);
                        g2.drawString(String.valueOf(v), x + colW / 2 - 6, y + rowH / 2 + 3);
                    }
                }
            }
            g2.setColor(TEXT_MUTED);
            String[] wkLabels = {"-4w", "-3w", "-2w", "-1w", "Now", "+1w", "+2w", "+3w", "+4w"};
            for (int c = 0; c < cols; c++)
                g2.drawString(wkLabels[c], labelW + c * colW, getHeight() - 4);
            g2.dispose();
        }
    }

    private JComponent buildTrackingPanel() {
        RoundedPanel card = new RoundedPanel(16, PANEL, PANEL_BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 14, 18));

        JLabel heading = new JLabel("Live Vehicle Tracking");
        heading.setFont(new Font("SansSerif", Font.BOLD, 14));
        heading.setForeground(TEXT_LIGHT);
        card.add(heading, BorderLayout.NORTH);

        TrackingMap map = new TrackingMap();
        map.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        map.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { navigateTo("Live Tracking", "\uD83D\uDCCD"); }
        });
        card.add(map, BorderLayout.CENTER);
        return card;
    }

    /** Sample "map" of scattered vehicle glyphs on a stylized dark canvas. */
    private static class TrackingMap extends JPanel {
        private java.util.List<int[]> spots;

        TrackingMap() { setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(0x140F26));
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));

            g2.setColor(new Color(0x1E1740));
            int w = Math.max(getWidth(), 1), h = Math.max(getHeight(), 1);
            g2.fill(new Ellipse2D.Double(w * 0.05, h * 0.15, w * 0.35, h * 0.5));
            g2.fill(new Ellipse2D.Double(w * 0.45, h * 0.35, w * 0.30, h * 0.4));
            g2.fill(new Ellipse2D.Double(w * 0.70, h * 0.10, w * 0.28, h * 0.55));

            if (spots == null) {
                spots = new java.util.ArrayList<>();
                java.util.Random rnd = new java.util.Random(7);
                for (int i = 0; i < 7; i++) {
                    spots.add(new int[]{rnd.nextInt(w), rnd.nextInt(h)});
                }
            }
            g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
            for (int[] s : spots) {
                g2.setColor(new Color(0x8B5CF6));
                g2.fill(new Ellipse2D.Double(s[0] - 3, s[1] - 3, 20, 20));
                g2.setColor(Color.WHITE);
                g2.drawString("\uD83D\uDE97", s[0] - 8, s[1] + 8);
            }
            g2.dispose();
        }
    }

    // ---------- Upcoming classes panel ----------
    private JComponent buildUpcomingClassesPanel() {
        RoundedPanel card = new RoundedPanel(16, PANEL, PANEL_BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 330));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel heading = new JLabel("Upcoming Classes");
        heading.setFont(new Font("SansSerif", Font.BOLD, 14));
        heading.setForeground(TEXT_LIGHT);

        JButton all = pillButton("View all", PANEL, TEXT_LIGHT, false);
        all.addActionListener(e -> navigateTo("Class Scheduling", "\uD83D\uDCC5"));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setBorder(new EmptyBorder(0, 0, 12, 0));
        head.add(heading, BorderLayout.WEST);
        head.add(all, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        if (data.upcomingClasses.isEmpty()) {
            JLabel empty = new JLabel("No upcoming classes. Book one from Class Scheduling.");
            empty.setFont(new Font("SansSerif", Font.PLAIN, 12));
            empty.setForeground(TEXT_MUTED);
            card.add(empty, BorderLayout.CENTER);
            return card;
        }

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        for (DashboardDAO.ClassItem c : data.upcomingClasses) {
            JPanel row = new JPanel(new BorderLayout(14, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

            JLabel when = new JLabel("<html><b>" + c.date + "</b><br>" + c.time + "</html>");
            when.setFont(new Font("SansSerif", Font.PLAIN, 11));
            when.setForeground(ACCENT_2);
            when.setPreferredSize(new Dimension(100, 40));

            JLabel title = new JLabel(c.type + " \u2014 " + c.instructor);
            title.setFont(new Font("SansSerif", Font.BOLD, 12));
            title.setForeground(TEXT_LIGHT);
            JLabel sub = new JLabel("\uD83D\uDCCD " + c.location + "  \u2022  " + c.vehicle);
            sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
            sub.setForeground(TEXT_MUTED);

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(title);
            text.add(sub);

            row.add(when, BorderLayout.WEST);
            row.add(text, BorderLayout.CENTER);
            list.add(row);
            list.add(Box.createVerticalStrut(8));
        }
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    // ---------- Bottom row: module list + results table ----------
    private JComponent buildBottomRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(buildModuleListPanel());
        row.add(buildResultsTablePanel());
        return row;
    }

    private JComponent buildModuleListPanel() {
        RoundedPanel card = new RoundedPanel(16, PANEL, PANEL_BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel heading = new JLabel("Upcoming Modules");
        heading.setFont(new Font("SansSerif", Font.BOLD, 14));
        heading.setForeground(TEXT_LIGHT);
        heading.setBorder(new EmptyBorder(0, 0, 12, 0));
        card.add(heading, BorderLayout.NORTH);

        java.util.List<DashboardDAO.ModuleItem> modules = data.modules;

        if (modules.isEmpty()) {
            JLabel empty = new JLabel("No modules assigned yet.");
            empty.setFont(new Font("SansSerif", Font.PLAIN, 12));
            empty.setForeground(TEXT_MUTED);
            card.add(empty, BorderLayout.CENTER);
            return card;
        }

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        for (int i = 0; i < modules.size(); i++) {
            DashboardDAO.ModuleItem m = modules.get(i);
            String title = "Module " + String.format("%02d", m.moduleNumber) + " \u2014 " + m.moduleTitle;
            list.add(moduleRow(i + 1, title, m.note));
            if (i < modules.size() - 1) list.add(Box.createVerticalStrut(10));
        }
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JComponent moduleRow(int number, String title, String sub) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(600, 44));

        JLabel numLbl = new JLabel(String.valueOf(number), SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        numLbl.setPreferredSize(new Dimension(28, 28));
        numLbl.setForeground(ACCENT_2);
        numLbl.setFont(new Font("SansSerif", Font.BOLD, 12));

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        titleLbl.setForeground(TEXT_LIGHT);
        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subLbl.setForeground(TEXT_MUTED);
        textCol.add(titleLbl);
        textCol.add(subLbl);

        JButton plus = circleButton("+", ACCENT);
        plus.setPreferredSize(new Dimension(28, 28));
        plus.addActionListener(e -> openCarFrame(title, "\uD83D\uDCC5"));

        JPanel left = new JPanel(new BorderLayout(10, 0));
        left.setOpaque(false);
        left.add(numLbl, BorderLayout.WEST);
        left.add(textCol, BorderLayout.CENTER);

        row.add(left, BorderLayout.CENTER);
        row.add(plus, BorderLayout.EAST);
        return row;
    }

    private JComponent buildResultsTablePanel() {
        RoundedPanel card = new RoundedPanel(16, PANEL, PANEL_BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel heading = new JLabel("Recent Test Results");
        heading.setFont(new Font("SansSerif", Font.BOLD, 14));
        heading.setForeground(TEXT_LIGHT);

        JButton menu = circleButton("\u22EE", PANEL);
        menu.setPreferredSize(new Dimension(26, 26));
        menu.addActionListener(e -> openCarFrame("Table Options", "\u2699"));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(heading, BorderLayout.WEST);
        head.add(menu, BorderLayout.EAST);
        head.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(head, BorderLayout.NORTH);

        if (data.testResults.isEmpty()) {
            JLabel empty = new JLabel("No test results yet.");
            empty.setFont(new Font("SansSerif", Font.PLAIN, 12));
            empty.setForeground(TEXT_MUTED);
            card.add(empty, BorderLayout.CENTER);
            return card;
        }

        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));

        rows.add(resultHeaderRow());
        rows.add(Box.createVerticalStrut(6));

        for (DashboardDAO.TestResultItem r : data.testResults) {
            String attemptsLabel = r.attempts + (r.attempts == 1 ? " attempt" : " attempts");
            rows.add(resultRow(r.studentName, r.testName, String.valueOf(r.score), attemptsLabel));
            rows.add(Box.createVerticalStrut(6));
        }
        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    private JComponent resultHeaderRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(600, 18));
        String[] heads = {"Student / Test", "Score", "Attempts", ""};
        for (String h : heads) {
            JLabel l = new JLabel(h);
            l.setFont(new Font("SansSerif", Font.BOLD, 10));
            l.setForeground(TEXT_MUTED);
            row.add(l);
        }
        return row;
    }

    private JComponent resultRow(String student, String test, String score, String attempts) {
        JPanel row = new JPanel(new GridLayout(1, 4, 8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(600, 34));

        JPanel nameCol = new JPanel();
        nameCol.setOpaque(false);
        nameCol.setLayout(new BoxLayout(nameCol, BoxLayout.Y_AXIS));
        JLabel nameLbl = new JLabel(student);
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        nameLbl.setForeground(TEXT_LIGHT);
        JLabel testLbl = new JLabel(test);
        testLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
        testLbl.setForeground(TEXT_MUTED);
        nameCol.add(nameLbl);
        nameCol.add(testLbl);

        double s = Double.parseDouble(score);
        Color badgeFg = s >= 8 ? GOOD : (s >= 6 ? new Color(0xFBBF24) : new Color(0xF87171));
        Color badgeBg = s >= 8 ? GOOD_BG : (s >= 6 ? new Color(0x332813) : new Color(0x341417));
        JLabel scoreLbl = new JLabel(score, SwingConstants.CENTER);
        scoreLbl.setOpaque(true);
        scoreLbl.setBackground(badgeBg);
        scoreLbl.setForeground(badgeFg);
        scoreLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        scoreLbl.setBorder(new EmptyBorder(3, 8, 3, 8));

        JLabel attemptsLbl = new JLabel(attempts);
        attemptsLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        attemptsLbl.setForeground(TEXT_MUTED);

        JButton action = circleButton("\u22EF", PANEL);
        action.setPreferredSize(new Dimension(24, 24));
        action.addActionListener(e -> openCarFrame(student + " - " + test, "\uD83D\uDCDD"));
        JPanel actionWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionWrap.setOpaque(false);
        actionWrap.add(action);

        row.add(nameCol);
        row.add(scoreLbl);
        row.add(attemptsLbl);
        row.add(actionWrap);
        return row;
    }

    // ==================================================================
    // POP-UP FEATURE PAGE
    // ==================================================================
    private void openCarFrame(String title, String icon) {
        JFrame frame = new JFrame(title + " | Dri-verse");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(560, 420);
        frame.setLocationRelativeTo(this);

        CarBackdrop backdrop = new CarBackdrop(SHELL_BG, OUTER_BG);
        backdrop.setLayout(new GridBagLayout());
        frame.setContentPane(backdrop);

        RoundedPanel card = new RoundedPanel(20, PANEL, PANEL_BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(34, 40, 34, 40));
        card.setPreferredSize(new Dimension(420, 250));
        card.setMaximumSize(new Dimension(420, 250));

        JLabel bigIcon = new JLabel(icon, SwingConstants.CENTER);
        bigIcon.setFont(new Font("SansSerif", Font.PLAIN, 48));
        bigIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 21));
        titleLbl.setForeground(TEXT_LIGHT);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLbl.setBorder(new EmptyBorder(14, 0, 6, 0));

        JLabel sub = new JLabel("<html><div style='text-align:center;width:280px;'>"
                + "This screen is ready to be connected to real data. Wire it up to "
                + "your controller / DAO whenever you're ready.</div></html>");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setHorizontalAlignment(SwingConstants.CENTER);

        JButton close = pillButton("\u2190  Back to Dashboard", ACCENT, Color.WHITE, true);
        close.setAlignmentX(Component.CENTER_ALIGNMENT);
        close.addActionListener(e -> frame.dispose());

        card.add(bigIcon);
        card.add(titleLbl);
        card.add(sub);
        card.add(Box.createVerticalStrut(22));
        card.add(close);

        backdrop.add(card);
        frame.setVisible(true);
    }

    private void openProfileFrame() {
        JFrame frame = new JFrame("Profile | Dri-verse");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(420, 520);
        frame.setLocationRelativeTo(this);

        CarBackdrop backdrop = new CarBackdrop(SHELL_BG, OUTER_BG);
        backdrop.setLayout(new GridBagLayout());
        frame.setContentPane(backdrop);

        RoundedPanel card = new RoundedPanel(20, PANEL, PANEL_BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 30, 26, 30));
        card.setPreferredSize(new Dimension(340, 400));
        card.setMaximumSize(new Dimension(340, 400));

        JLabel big = new JLabel(initialOf(data.learnerName), SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT, getWidth(), getHeight(), PINK);
                g2.setPaint(gp);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        big.setPreferredSize(new Dimension(84, 84));
        big.setMaximumSize(new Dimension(84, 84));
        big.setForeground(Color.WHITE);
        big.setFont(new Font("SansSerif", Font.BOLD, 32));
        big.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel(data.learnerName, SwingConstants.CENTER);
        name.setFont(new Font("SansSerif", Font.BOLD, 20));
        name.setForeground(TEXT_LIGHT);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        name.setBorder(new EmptyBorder(14, 0, 2, 0));

        JLabel role = new JLabel("Learner \u2022 Dri-verse", SwingConstants.CENTER);
        role.setFont(new Font("SansSerif", Font.PLAIN, 12));
        role.setForeground(TEXT_MUTED);
        role.setAlignmentX(Component.CENTER_ALIGNMENT);
        role.setBorder(new EmptyBorder(0, 0, 20, 0));

        RoundedPanel statsBox = new RoundedPanel(14, CARD, PANEL_BORDER);
        statsBox.setLayout(new BoxLayout(statsBox, BoxLayout.Y_AXIS));
        statsBox.setBorder(new EmptyBorder(14, 16, 14, 16));
        statsBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsBox.setMaximumSize(new Dimension(280, 130));
        statsBox.add(statRow("Classes enrolled", String.valueOf(data.classesEnrolled)));
        statsBox.add(statRow("Upcoming classes", String.valueOf(data.upcomingClassesCount)));
        statsBox.add(statRow("Tests completed", String.valueOf(data.testsCompleted)));
        statsBox.add(statRow("Total paid", "\u20B9 " + String.format("%,.0f", data.paymentsTotal)));

        JButton edit = pillButton("Edit Profile", ACCENT, Color.WHITE, true);
        edit.setAlignmentX(Component.CENTER_ALIGNMENT);
        edit.addActionListener(e -> openCarFrame("Edit Profile", "\uD83D\uDC64"));

        JButton close = pillButton("Close", PANEL, TEXT_LIGHT, false);
        close.setAlignmentX(Component.CENTER_ALIGNMENT);
        close.addActionListener(e -> frame.dispose());

        card.add(big);
        card.add(name);
        card.add(role);
        card.add(statsBox);
        card.add(Box.createVerticalStrut(20));
        card.add(edit);
        card.add(Box.createVerticalStrut(8));
        card.add(close);

        backdrop.add(card);
        frame.setVisible(true);
    }

    private JComponent statRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(280, 22));
        row.setBorder(new EmptyBorder(3, 0, 3, 0));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.BOLD, 12));
        val.setForeground(TEXT_LIGHT);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    /** Gradient backdrop scattered with faint car glyphs, used by pop-up windows. */
    private static class CarBackdrop extends JPanel {
        private final Color top, bottom;
        private java.util.List<int[]> spots;

        CarBackdrop(Color top, Color bottom) {
            this.top = top;
            this.bottom = bottom;
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(0, 0, top, 0, getHeight(), bottom);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (spots == null) {
                spots = new java.util.ArrayList<>();
                java.util.Random rnd = new java.util.Random(42);
                int w = Math.max(getWidth(), 1);
                int h = Math.max(getHeight(), 1);
                for (int i = 0; i < 18; i++) {
                    spots.add(new int[]{rnd.nextInt(w), rnd.nextInt(h), 26 + rnd.nextInt(30)});
                }
            }

            String car = "\uD83D\uDE97";
            Composite original = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.14f));
            for (int[] s : spots) {
                g2.setFont(new Font("SansSerif", Font.PLAIN, s[2]));
                g2.drawString(car, s[0], s[1]);
            }
            g2.setComposite(original);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ==================================================================
    // Reusable painted components
    // ==================================================================
    private static class RoundedPanel extends JPanel {
        private final int radius;
        private Color fill;
        private final Color border;
        RoundedPanel(int radius, Color fill, Color border) {
            this.radius = radius;
            this.fill = fill;
            this.border = border;
            setOpaque(false);
        }
        void setFillColor(Color c) {
            this.fill = c;
            repaint();
        }
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

    private static class GradientRoundedPanel extends JPanel {
        private final int radius;
        private final Color c1, c2;
        GradientRoundedPanel(int radius, Color c1, Color c2) {
            this.radius = radius;
            this.c1 = c1;
            this.c2 = c2;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private JButton pillButton(String text, Color bg, Color fg, boolean filled) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isRollover() ? bg.darker() : bg;
                if (!filled) c = getModel().isRollover() ? CARD_HOVER : PANEL;
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight()));
                if (!filled) {
                    g2.setColor(PANEL_BORDER);
                    g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight()));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(fg);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton circleButton(String icon, Color bg) {
        JButton btn = new JButton(icon) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.darker() : bg);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setPreferredSize(new Dimension(38, 38));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new LearnerDashboard(2).setVisible(true);   // change to the learner id you want to test
        });
    }

    // ==================================================================
    // MOCK TESTS
    // ==================================================================
    private JComponent buildMockTestsCard() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("Indian Learner's Licence Mock Tests");
        heading.setFont(new Font("SansSerif", Font.BOLD, 24));
        heading.setForeground(TEXT_LIGHT);

        JLabel subtitle = new JLabel(
            "<html>Practise Indian traffic rules, road signs and road safety "
            + "with learner's licence sample questions.</html>"
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);

        panel.add(heading);
        panel.add(Box.createVerticalStrut(10));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(28));

        String[][] tests = {
            {"Road Signs", "Identify traffic signs and their meanings."},
            {"Traffic Rules", "Practise road rules and right-of-way questions."},
            {"Road Safety", "Test your knowledge of safe driving."},
            {"Full Practice Test", "Try a mixed learner's licence quiz."}
        };

        for (String[] test : tests) {
            JPanel card = new JPanel(new BorderLayout(12, 8));
            card.setBackground(PANEL);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PANEL_BORDER),
                new EmptyBorder(18, 18, 18, 18)
            ));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            card.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel details = new JPanel();
            details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
            details.setOpaque(false);

            JLabel name = new JLabel(test[0]);
            name.setFont(new Font("SansSerif", Font.BOLD, 16));
            name.setForeground(TEXT_LIGHT);

            JLabel description = new JLabel(test[1]);
            description.setFont(new Font("SansSerif", Font.PLAIN, 12));
            description.setForeground(TEXT_MUTED);

            details.add(name);
            details.add(Box.createVerticalStrut(7));
            details.add(description);

            JButton start = new JButton("Start Test");
            start.setFocusPainted(false);
            start.setOpaque(true);
            start.setContentAreaFilled(true);
            start.setBorderPainted(false);
            start.setBackground(ACCENT);
            start.setForeground(Color.WHITE);
            start.setFont(new Font("SansSerif", Font.BOLD, 12));
            start.setCursor(new Cursor(Cursor.HAND_CURSOR));

            start.addActionListener(e -> startMockTest(test[0]));

            card.add(details, BorderLayout.CENTER);
            card.add(start, BorderLayout.EAST);

            panel.add(card);
            panel.add(Box.createVerticalStrut(14));
        }

        panel.add(Box.createVerticalGlue());

        return new JScrollPane(panel);
    }

    // ==================================================================
    // PAYMENTS
    // ==================================================================
    private JComponent buildPaymentsCard() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(8, 4, 8, 4));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Payments & Fees");
        heading.setFont(new Font("SansSerif", Font.BOLD, 24));
        heading.setForeground(TEXT_LIGHT);

        JLabel subtitle = new JLabel(
                "Track your fees, payment status and transaction history."
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_MUTED);

        JLabel note = new JLabel(
                "Adding a record creates a PENDING entry. It does not charge money."
        );
        note.setFont(new Font("SansSerif", Font.PLAIN, 11));
        note.setForeground(ACCENT_2);

        top.add(heading);
        top.add(Box.createVerticalStrut(8));
        top.add(subtitle);
        top.add(Box.createVerticalStrut(5));
        top.add(note);
        top.add(Box.createVerticalStrut(18));

        final JLabel paidTotal = new JLabel("\u20B9 0.00");
        final JLabel pendingTotal = new JLabel("\u20B9 0.00");

        JPanel summary = new JPanel(new GridLayout(1, 2, 14, 0));
        summary.setOpaque(false);
        summary.add(paymentSummaryCard("Total Paid", paidTotal));
        summary.add(paymentSummaryCard("Pending Amount", pendingTotal));
        top.add(summary);

        panel.add(top, BorderLayout.NORTH);

        final javax.swing.table.DefaultTableModel model =
                new javax.swing.table.DefaultTableModel(
                        new Object[]{
                            "ID", "Description", "Amount", "Status",
                            "Method", "Reference", "Created At"
                        }, 0
                ) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

        final javax.swing.JTable table = new javax.swing.JTable(model);
        table.setRowHeight(30);
        table.setBackground(CARD);
        table.setForeground(TEXT_LIGHT);
        table.setGridColor(PANEL_BORDER);
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));

        table.getTableHeader().setBackground(PANEL);
        table.getTableHeader().setForeground(TEXT_LIGHT);
        table.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(
                BorderFactory.createLineBorder(PANEL_BORDER)
        );
        tableScroll.getViewport().setBackground(CARD);
        tableScroll.setBackground(CARD);

        panel.add(tableScroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );
        actions.setOpaque(false);

        JButton refreshButton = pillButton("Refresh", PANEL, TEXT_LIGHT, false);
        JButton viewButton = pillButton("View Details", PANEL, TEXT_LIGHT, false);
        JButton paidButton = pillButton("Mark as Paid", ACCENT, Color.WHITE, true);
        JButton failedButton = pillButton("Mark as Failed", PANEL, TEXT_LIGHT, false);
        JButton addButton = pillButton("+ Add Payment Record", ACCENT, Color.WHITE, true);

        refreshButton.addActionListener(e ->
                loadPayments(model, paidTotal, pendingTotal)
        );

        addButton.addActionListener(e -> {
            String description = JOptionPane.showInputDialog(
                    this,
                    "Enter the fee or payment description:",
                    "Add Payment Record",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (description == null) {
                return;
            }

            description = description.trim();

            if (description.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this, "Description cannot be empty."
                );
                return;
            }

            String amountText = JOptionPane.showInputDialog(
                    this,
                    "Enter the amount in rupees:",
                    "0.00"
            );

            if (amountText == null) {
                return;
            }

            final java.math.BigDecimal amount;

            try {
                amount = new java.math.BigDecimal(amountText.trim());

                if (amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    JOptionPane.showMessageDialog(
                            this, "Amount must be greater than zero."
                    );
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        this, "Enter a valid amount, such as 1500.00."
                );
                return;
            }

            String sql =
                    "INSERT INTO payments "
                    + "(learner_id, description, amount, payment_status) "
                    + "VALUES (?, ?, ?, 'PENDING')";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, userId);
                ps.setString(2, description);
                ps.setBigDecimal(3, amount);

                ps.executeUpdate();

                JOptionPane.showMessageDialog(
                        this,
                        "Payment record added successfully.\n"
                        + "Status: PENDING",
                        "Payment Record Created",
                        JOptionPane.INFORMATION_MESSAGE
                );

                loadPayments(model, paidTotal, pendingTotal);

            } catch (SQLException ex) {
                ex.printStackTrace();

                JOptionPane.showMessageDialog(
                        this,
                        "Could not save the payment record:\n"
                        + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        viewButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();

            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please select a payment record first.",
                        "No Selection",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            int row = table.convertRowIndexToModel(selectedRow);

            String details =
                    "Payment ID: " + model.getValueAt(row, 0) + "\n"
                    + "Description: " + model.getValueAt(row, 1) + "\n"
                    + "Amount: " + model.getValueAt(row, 2) + "\n"
                    + "Status: " + model.getValueAt(row, 3) + "\n"
                    + "Method: " + model.getValueAt(row, 4) + "\n"
                    + "Reference: " + model.getValueAt(row, 5) + "\n"
                    + "Created: " + model.getValueAt(row, 6);

            JOptionPane.showMessageDialog(
                    this,
                    details,
                    "Payment Details",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        paidButton.addActionListener(e ->
                updateSelectedPaymentStatus(
                        table, model, paidTotal, pendingTotal, "PAID"
                )
        );

        failedButton.addActionListener(e ->
                updateSelectedPaymentStatus(
                        table, model, paidTotal, pendingTotal, "FAILED"
                )
        );

        actions.add(refreshButton);
        actions.add(viewButton);
        actions.add(paidButton);
        actions.add(failedButton);
        actions.add(addButton);

        panel.add(actions, BorderLayout.SOUTH);

        loadPayments(model, paidTotal, pendingTotal);

        return panel;
    }

    private JComponent paymentSummaryCard(String title, JLabel value) {
        RoundedPanel card = new RoundedPanel(14, CARD, PANEL_BORDER);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_MUTED);

        value.setFont(new Font("SansSerif", Font.BOLD, 24));
        value.setForeground(TEXT_LIGHT);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);

        return card;
    }

    private void updateSelectedPaymentStatus(
            javax.swing.JTable table,
            javax.swing.table.DefaultTableModel model,
            JLabel paidTotal,
            JLabel pendingTotal,
            String newStatus
    ) {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a payment record first.",
                    "No Selection",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        int row = table.convertRowIndexToModel(selectedRow);

        long paymentId =
                ((Number) model.getValueAt(row, 0)).longValue();

        String description = String.valueOf(model.getValueAt(row, 1));
        String currentStatus = String.valueOf(model.getValueAt(row, 3));

        if (!"PENDING".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Only PENDING payments can be updated.\n"
                    + "Current status: " + currentStatus,
                    "Status Cannot Be Changed",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Change the status of \"" + description
                        + "\" to " + newStatus + "?\n\n"
                        + "This updates the demo record only. "
                        + "It does not process or verify a payment.",
                "Confirm Status Change",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql =
                "UPDATE payments "
                + "SET payment_status = ?, paid_at = ? "
                + "WHERE payment_id = ? "
                + "AND learner_id = ? "
                + "AND UPPER(payment_status) = 'PENDING'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);

            if ("PAID".equals(newStatus)) {
                ps.setTimestamp(
                        2,
                        new java.sql.Timestamp(System.currentTimeMillis())
                );
            } else {
                ps.setNull(2, java.sql.Types.TIMESTAMP);
            }

            ps.setLong(3, paymentId);
            ps.setInt(4, userId);

            int updated = ps.executeUpdate();

            if (updated > 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Demo payment status updated to "
                                + newStatus + ".",
                        "Status Updated",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "The record was not updated. Refresh the page "
                                + "and check its current status.",
                        "Update Unsuccessful",
                        JOptionPane.WARNING_MESSAGE
                );
            }

            loadPayments(model, paidTotal, pendingTotal);

        } catch (SQLException ex) {
            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Could not update the payment status:\n"
                            + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void loadPayments(
            javax.swing.table.DefaultTableModel model,
            JLabel paidTotal,
            JLabel pendingTotal
    ) {
        model.setRowCount(0);

        String recordsSql =
                "SELECT payment_id, description, amount, payment_status, "
                + "COALESCE(payment_method, '') AS payment_method, "
                + "COALESCE(transaction_reference, '') AS reference, "
                + "created_at "
                + "FROM payments "
                + "WHERE learner_id = ? "
                + "ORDER BY created_at DESC";

        String totalsSql =
                "SELECT "
                + "COALESCE(SUM(CASE WHEN UPPER(payment_status) = 'PAID' "
                + "THEN amount ELSE 0 END), 0) AS paid_total, "
                + "COALESCE(SUM(CASE WHEN UPPER(payment_status) = 'PENDING' "
                + "THEN amount ELSE 0 END), 0) AS pending_total "
                + "FROM payments WHERE learner_id = ?";

        try (Connection conn = DBConnection.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(recordsSql)) {
                ps.setInt(1, userId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        java.math.BigDecimal amount =
                                rs.getBigDecimal("amount");

                        model.addRow(new Object[]{
                            rs.getLong("payment_id"),
                            rs.getString("description"),
                            "\u20B9 " + amount.toPlainString(),
                            rs.getString("payment_status"),
                            rs.getString("payment_method"),
                            rs.getString("reference"),
                            rs.getTimestamp("created_at")
                        });
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(totalsSql)) {
                ps.setInt(1, userId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        paidTotal.setText(
                                "\u20B9 "
                                + rs.getBigDecimal("paid_total").toPlainString()
                        );

                        pendingTotal.setText(
                                "\u20B9 "
                                + rs.getBigDecimal("pending_total").toPlainString()
                        );
                    }
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Could not load payment history:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ==================================================================
    // MOCK TEST WINDOW
    // ==================================================================
    private void startMockTest(String category) {
        java.util.List<String[]> questions = new java.util.ArrayList<>();

        String sql;

        if ("Full Practice Test".equals(category)) {
            sql = "SELECT question_text, option_a, option_b, option_c, option_d, "
                    + "correct_option, COALESCE(explanation, '') "
                    + "FROM mock_test_questions "
                    + "WHERE active = TRUE "
                    + "ORDER BY RAND() LIMIT 30";
        } else {
            sql = "SELECT question_text, option_a, option_b, option_c, option_d, "
                    + "correct_option, COALESCE(explanation, '') "
                    + "FROM mock_test_questions "
                    + "WHERE category = ? AND active = TRUE "
                    + "ORDER BY RAND() LIMIT 20";
        }

        try (java.sql.Connection conn = DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            if (!"Full Practice Test".equals(category)) {
                ps.setString(1, category);
            }

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    questions.add(new String[]{
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getString(6),
                        rs.getString(7)
                    });
                }
            }

        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Could not load mock test questions.\n" + ex.getMessage(),
                    "Database Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
            return;
        }

        if (questions.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "No questions are available for " + category
                            + ". Please add active questions to the database.",
                    "No Questions",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        final String[][] q = questions.toArray(new String[0][]);
        final int total = q.length;
        final int[] answers = new int[total];

        java.util.Arrays.fill(answers, -1);

        final int[] current = {0};
        final int testDurationSeconds =
                "Full Practice Test".equals(category)
                        ? 30 * 60
                        : 20 * 60;

        final int[] seconds = {testDurationSeconds};
        final boolean[] submitted = {false};

        final javax.swing.JDialog dialog =
                new javax.swing.JDialog(this, category, true);

        dialog.setSize(760, 570);
        dialog.setMinimumSize(new java.awt.Dimension(650, 480));
        dialog.setLocationRelativeTo(this);
        dialog.setDefaultCloseOperation(
                javax.swing.WindowConstants.DISPOSE_ON_CLOSE
        );

        javax.swing.JPanel root = new javax.swing.JPanel(
                new java.awt.BorderLayout(15, 15)
        );
        root.setBorder(
                javax.swing.BorderFactory.createEmptyBorder(20, 24, 20, 24)
        );
        root.setBackground(SHELL_BG);

        javax.swing.JPanel header = new javax.swing.JPanel(new java.awt.BorderLayout());
        header.setOpaque(false);

        javax.swing.JLabel title = new javax.swing.JLabel(category);
        title.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 22));
        title.setForeground(TEXT_LIGHT);

        final javax.swing.JLabel timerLabel = new javax.swing.JLabel(
                String.format("%02d:%02d",
                        testDurationSeconds / 60,
                        testDurationSeconds % 60)
        );
        timerLabel.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 18));
        timerLabel.setForeground(GOOD);

        header.add(title, java.awt.BorderLayout.WEST);
        header.add(timerLabel, java.awt.BorderLayout.EAST);

        javax.swing.JPanel center = new javax.swing.JPanel();
        center.setOpaque(false);
        center.setLayout(new javax.swing.BoxLayout(
                center, javax.swing.BoxLayout.Y_AXIS
        ));

        final javax.swing.JLabel progressLabel =
                new javax.swing.JLabel();
        progressLabel.setFont(
                new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 13)
        );
        progressLabel.setForeground(TEXT_MUTED);
        progressLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        final javax.swing.JTextArea questionText =
                new javax.swing.JTextArea();
        questionText.setFont(
                new java.awt.Font("SansSerif", java.awt.Font.BOLD, 17)
        );
        questionText.setForeground(TEXT_LIGHT);
        questionText.setLineWrap(true);
        questionText.setWrapStyleWord(true);
        questionText.setEditable(false);
        questionText.setFocusable(false);
        questionText.setOpaque(false);
        questionText.setBorder(
                javax.swing.BorderFactory.createEmptyBorder(8, 0, 15, 0)
        );
        questionText.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        questionText.setPreferredSize(new java.awt.Dimension(600, 70));
        questionText.setMaximumSize(
                new java.awt.Dimension(Integer.MAX_VALUE, 90)
        );

        final javax.swing.JButton[] optionButtons =
                new javax.swing.JButton[4];

        javax.swing.ButtonGroup optionGroup =
                new javax.swing.ButtonGroup();

        for (int i = 0; i < 4; i++) {
            final int optionIndex = i;

            optionButtons[i] = new javax.swing.JButton();
            optionButtons[i].setHorizontalAlignment(
                    javax.swing.SwingConstants.LEFT
            );
            optionButtons[i].setFont(
                    new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14)
            );
            optionButtons[i].setFocusPainted(false);
            optionButtons[i].setCursor(
                    new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)
            );

            optionButtons[i].setOpaque(true);
            optionButtons[i].setContentAreaFilled(true);
            optionButtons[i].setBackground(CARD);
            optionButtons[i].setForeground(TEXT_LIGHT);

            optionButtons[i].setAlignmentX(
                    java.awt.Component.LEFT_ALIGNMENT
            );

            optionButtons[i].setMaximumSize(
                    new java.awt.Dimension(Integer.MAX_VALUE, 52)
            );
            optionButtons[i].setBorder(
                    javax.swing.BorderFactory.createCompoundBorder(
                            javax.swing.BorderFactory.createLineBorder(
                                    new java.awt.Color(203, 213, 225)
                            ),
                            javax.swing.BorderFactory.createEmptyBorder(
                                    12, 14, 12, 14
                            )
                    )
            );

            optionGroup.add(optionButtons[i]);

            optionButtons[i].addActionListener(e -> {
                if (submitted[0]) {
                    return;
                }

                answers[current[0]] = optionIndex;

                for (int j = 0; j < 4; j++) {
                    boolean selected = answers[current[0]] == j;

                    optionButtons[j].setBackground(selected ? ACCENT : CARD);
                    optionButtons[j].setForeground(TEXT_LIGHT);
                    optionButtons[j].setOpaque(true);
                    optionButtons[j].setContentAreaFilled(true);

                    optionButtons[j].setBorder(
                            javax.swing.BorderFactory.createCompoundBorder(
                                    javax.swing.BorderFactory.createLineBorder(
                                            selected
                                                    ? new java.awt.Color(37, 99, 235)
                                                    : new java.awt.Color(203, 213, 225),
                                            selected ? 2 : 1
                                    ),
                                    javax.swing.BorderFactory.createEmptyBorder(
                                            12, 14, 12, 14
                                    )
                            )
                    );
                }
            });
        }

        center.add(progressLabel);
        center.add(javax.swing.Box.createVerticalStrut(18));
        center.add(questionText);
        center.add(javax.swing.Box.createVerticalStrut(15));

        for (int i = 0; i < 4; i++) {
            center.add(optionButtons[i]);
            center.add(javax.swing.Box.createVerticalStrut(10));
        }

        javax.swing.JPanel footer = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 12, 5)
        );
        footer.setOpaque(false);

        final javax.swing.JButton previousButton =
                new javax.swing.JButton("Previous");
        final javax.swing.JButton nextButton =
                new javax.swing.JButton("Next");
        final javax.swing.JButton submitButton =
                new javax.swing.JButton("Submit Test");

        submitButton.setPreferredSize(new java.awt.Dimension(125, 38));
        submitButton.setBackground(new java.awt.Color(37, 99, 235));
        submitButton.setForeground(java.awt.Color.WHITE);
        submitButton.setOpaque(true);
        submitButton.setContentAreaFilled(true);
        submitButton.setBorderPainted(false);

        previousButton.setFocusPainted(false);
        nextButton.setFocusPainted(false);
        submitButton.setFocusPainted(false);

        footer.add(previousButton);
        footer.add(nextButton);
        footer.add(submitButton);

        root.add(header, java.awt.BorderLayout.NORTH);
        root.add(center, java.awt.BorderLayout.CENTER);
        root.add(footer, java.awt.BorderLayout.SOUTH);

        dialog.setContentPane(root);

        final Runnable[] showQuestion = new Runnable[1];

        showQuestion[0] = () -> {
            int index = current[0];

            progressLabel.setText(
                    "Question " + (index + 1) + " of " + total
            );

            questionText.setText(q[index][0]);
            questionText.setCaretPosition(0);

            for (int j = 0; j < 4; j++) {
                optionButtons[j].setText(
                        (char) ('A' + j) + ". " + q[index][j + 1]
                );

                boolean selected = answers[index] == j;

                optionButtons[j].setBackground(selected ? ACCENT : CARD);
                optionButtons[j].setForeground(TEXT_LIGHT);
                optionButtons[j].setOpaque(true);
                optionButtons[j].setContentAreaFilled(true);

                optionButtons[j].setBorder(
                        javax.swing.BorderFactory.createCompoundBorder(
                                javax.swing.BorderFactory.createLineBorder(
                                        selected
                                                ? new java.awt.Color(37, 99, 235)
                                                : new java.awt.Color(203, 213, 225),
                                        selected ? 2 : 1
                                ),
                                javax.swing.BorderFactory.createEmptyBorder(
                                        12, 14, 12, 14
                                )
                        )
                );
            }

            previousButton.setEnabled(index > 0);
            nextButton.setEnabled(index < total - 1);

            nextButton.setText(
                    index == total - 1 ? "Last Question" : "Next"
            );
        };

        previousButton.addActionListener(e -> {
            if (!submitted[0] && current[0] > 0) {
                current[0]--;
                showQuestion[0].run();
            }
        });

        nextButton.addActionListener(e -> {
            if (!submitted[0] && current[0] < total - 1) {
                current[0]++;
                showQuestion[0].run();
            }
        });

        final javax.swing.Timer[] countdown = new javax.swing.Timer[1];

        final Runnable finishTest = () -> {
            if (submitted[0]) {
                return;
            }

            submitted[0] = true;

            if (countdown[0] != null) {
                countdown[0].stop();
            }

            int attempted = 0;
            int correct = 0;

            for (int i = 0; i < total; i++) {
                if (answers[i] != -1) {
                    attempted++;

                    try {
                        int correctOption = Integer.parseInt(q[i][5]) - 1;

                        if (answers[i] == correctOption) {
                            correct++;
                        }
                    } catch (NumberFormatException ex) {
                        System.err.println(
                                "Invalid correct_option for question " + (i + 1)
                        );
                    }
                }
            }

            int unanswered = total - attempted;
            int percentage = (int) Math.round(correct * 100.0 / total);

            String insertSql =
                    "INSERT INTO mock_test_results "
                    + "(learner_id, category, total_questions, attempted, "
                    + "unanswered, correct_answers, score_percentage) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

            boolean saved = false;

            try (java.sql.Connection conn = DBConnection.getConnection();
                 java.sql.PreparedStatement ps =
                         conn.prepareStatement(insertSql)) {

                ps.setInt(1, userId);
                ps.setString(2, category);
                ps.setInt(3, total);
                ps.setInt(4, attempted);
                ps.setInt(5, unanswered);
                ps.setInt(6, correct);
                ps.setInt(7, percentage);

                saved = ps.executeUpdate() > 0;

            } catch (java.sql.SQLException ex) {
                ex.printStackTrace();
            }

            String resultMessage =
                    "Test completed!\n\n"
                    + "Category: " + category + "\n"
                    + "Total questions: " + total + "\n"
                    + "Attempted: " + attempted + "\n"
                    + "Unanswered: " + unanswered + "\n"
                    + "Correct answers: " + correct + "\n"
                    + "Incorrect answers: " + (attempted - correct) + "\n"
                    + "Score: " + percentage + "%\n\n"
                    + (saved
                        ? "Your result has been saved."
                        : "Warning: Your result could not be saved to the database.");

            Object[] choices = {"View Answers", "Close"};
            int choice = javax.swing.JOptionPane.showOptionDialog(
                    dialog,
                    resultMessage,
                    "Test Results",
                    javax.swing.JOptionPane.DEFAULT_OPTION,
                    javax.swing.JOptionPane.INFORMATION_MESSAGE,
                    null,
                    choices,
                    choices[0]
            );

            if (choice == 0) {
                StringBuilder review = new StringBuilder();

                for (int i = 0; i < total; i++) {
                    int correctOption;

                    try {
                        correctOption = Integer.parseInt(q[i][5]) - 1;
                    } catch (NumberFormatException ex) {
                        correctOption = -1;
                    }

                    review.append("Question ")
                            .append(i + 1)
                            .append(": ")
                            .append(q[i][0])
                            .append("\n");

                    review.append("Your answer: ");

                    if (answers[i] == -1) {
                        review.append("Not answered\n");
                    } else {
                        review.append((char) ('A' + answers[i]))
                                .append(". ")
                                .append(q[i][answers[i] + 1])
                                .append("\n");
                    }

                    review.append("Correct answer: ");

                    if (correctOption >= 0 && correctOption < 4) {
                        review.append((char) ('A' + correctOption))
                                .append(". ")
                                .append(q[i][correctOption + 1])
                                .append("\n");
                    } else {
                        review.append("Unavailable\n");
                    }

                    if (q[i][6] != null && !q[i][6].trim().isEmpty()) {
                        review.append("Explanation: ")
                                .append(q[i][6])
                                .append("\n");
                    }

                    review.append("\n------------------------------\n\n");
                }

                javax.swing.JTextArea reviewArea =
                        new javax.swing.JTextArea(review.toString(), 22, 55);

                reviewArea.setEditable(false);
                reviewArea.setLineWrap(true);
                reviewArea.setWrapStyleWord(true);
                reviewArea.setCaretPosition(0);
                reviewArea.setFont(
                        new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 13)
                );

                javax.swing.JScrollPane scrollPane =
                        new javax.swing.JScrollPane(reviewArea);

                scrollPane.setPreferredSize(new java.awt.Dimension(650, 430));

                javax.swing.JOptionPane.showMessageDialog(
                        dialog,
                        scrollPane,
                        "Answer Review",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE
                );
            }

            dialog.dispose();
        };

        submitButton.addActionListener(e -> {
            if (submitted[0]) {
                return;
            }

            int choice = javax.swing.JOptionPane.showConfirmDialog(
                    dialog,
                    "Are you sure you want to submit this test?",
                    "Confirm Submission",
                    javax.swing.JOptionPane.YES_NO_OPTION
            );

            if (choice == javax.swing.JOptionPane.YES_OPTION) {
                finishTest.run();
            }
        });

        countdown[0] = new javax.swing.Timer(1000, e -> {
            if (submitted[0]) {
                countdown[0].stop();
                return;
            }

            seconds[0]--;

            int minutes = seconds[0] / 60;
            int remainingSeconds = seconds[0] % 60;

            timerLabel.setText(
                    String.format("%02d:%02d", minutes, remainingSeconds)
            );

            if (seconds[0] <= 60) {
                timerLabel.setForeground(new java.awt.Color(185, 28, 28));
            }

            if (seconds[0] <= 0) {
                countdown[0].stop();

                javax.swing.JOptionPane.showMessageDialog(
                        dialog,
                        "Time is up! Your test will be submitted automatically.",
                        "Time Expired",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE
                );

                finishTest.run();
            }
        });

        showQuestion[0].run();

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (countdown[0] != null) {
                    countdown[0].stop();
                }
            }
        });

        countdown[0].start();
        dialog.setVisible(true);
    }

    // ==================================================================
    // LIVE TRACKING
    // ==================================================================
    private JComponent buildLiveTrackingCard() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 8, 12, 8));

        JLabel heading = new JLabel("Live Vehicle Tracking");
        heading.setFont(new Font("SansSerif", Font.BOLD, 24));
        heading.setForeground(TEXT_LIGHT);

        JLabel subtitle = new JLabel(
            "View the latest vehicle location saved for your account."
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_MUTED);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(heading);
        top.add(Box.createVerticalStrut(8));
        top.add(subtitle);

        panel.add(top, BorderLayout.NORTH);

        RoundedPanel details = new RoundedPanel(16, PANEL, PANEL_BORDER);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel vehicle = new JLabel("Vehicle: Loading...");
        JLabel latitude = new JLabel("Latitude: \u2014");
        JLabel longitude = new JLabel("Longitude: \u2014");
        JLabel distance = new JLabel("Distance Travelled: 0.00 km");
        final double[] previousLat = {Double.NaN};
        final double[] previousLon = {Double.NaN};
        final double[] totalDistanceKm = {0.0};
        JLabel updated = new JLabel("Last Updated: \u2014");
        JLabel status = new JLabel("Status: Checking...");

        JLabel[] labels = {vehicle, latitude, longitude, distance, updated, status};

        for (JLabel label : labels) {
            label.setFont(new Font("SansSerif", Font.PLAIN, 15));
            label.setForeground(TEXT_LIGHT);
            label.setBorder(new EmptyBorder(8, 0, 8, 0));
            details.add(label);
        }

        JButton refresh = pillButton(
            "Refresh Location", ACCENT, Color.WHITE, true
        );

        JButton openMap = pillButton(
            "Open in Google Maps", ACCENT, Color.WHITE, true
        );
        JButton simulate = pillButton(
            "Simulate Movement", ACCENT, Color.WHITE, true
        );

        simulate.addActionListener(e -> {
            String sql =
                "UPDATE vehicle_tracking "
                + "SET latitude = latitude + 0.0010, "
                + "longitude = longitude + 0.0010 "
                + "WHERE learner_id = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, userId);

                int rows = ps.executeUpdate();

                if (rows > 0) {
                    refresh.doClick();
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "No vehicle record found for this learner."
                    );
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Could not simulate movement: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });

        openMap.addActionListener(e -> {
            try {
                String lat = latitude.getText().replace("Latitude: ", "").trim();
                String lon = longitude.getText().replace("Longitude: ", "").trim();

                if (lat.equals("\u2014") || lon.equals("\u2014")
                        || lat.isEmpty() || lon.isEmpty()) {
                    JOptionPane.showMessageDialog(
                        this,
                        "No vehicle coordinates available yet."
                    );
                    return;
                }

                String url = "https://www.google.com/maps/search/?api=1&query="
                        + lat + "," + lon;

                java.awt.Desktop.getDesktop().browse(
                    new java.net.URI(url)
                );

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Could not open Google Maps: " + ex.getMessage()
                );
            }
        });

        refresh.addActionListener(e -> {
            String sql =
                "SELECT vehicle_number, latitude, longitude, updated_at "
                + "FROM vehicle_tracking "
                + "WHERE learner_id = ? "
                + "ORDER BY updated_at DESC LIMIT 1";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, userId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        vehicle.setText(
                            "Vehicle: " + rs.getString("vehicle_number")
                        );

                        double currentLat = rs.getDouble("latitude");
                        double currentLon = rs.getDouble("longitude");

                        latitude.setText("Latitude: " + currentLat);
                        longitude.setText("Longitude: " + currentLon);

                        if (!Double.isNaN(previousLat[0])
                                && !Double.isNaN(previousLon[0])) {

                            totalDistanceKm[0] += calculateDistanceKm(
                                previousLat[0], previousLon[0],
                                currentLat, currentLon
                            );

                            distance.setText(String.format(
                                "Distance Travelled: %.2f km",
                                totalDistanceKm[0]
                            ));
                        }

                        previousLat[0] = currentLat;
                        previousLon[0] = currentLon;

                        updated.setText(
                            "Last Updated: " +
                            new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm:ss a")
                                .format(rs.getTimestamp("updated_at"))
                        );

                        status.setText("Status: Tracking Data Available");
                        status.setForeground(new Color(46, 204, 113));

                    } else {
                        vehicle.setText("No vehicle assigned yet.");
                        latitude.setText("Latitude: \u2014");
                        longitude.setText("Longitude: \u2014");
                        updated.setText("Last Updated: \u2014");

                        status.setText("Status: Tracking Data Unavailable");
                        status.setForeground(Color.RED);
                    }
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Could not load vehicle location:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });

        refresh.setAlignmentX(Component.LEFT_ALIGNMENT);
        details.add(Box.createVerticalStrut(16));
        details.add(refresh);
        details.add(Box.createVerticalStrut(10));
        details.add(openMap);
        details.add(Box.createVerticalStrut(10));
        details.add(simulate);

        panel.add(details, BorderLayout.CENTER);

        refresh.doClick();
        javax.swing.Timer trackingTimer = new javax.swing.Timer(
            10000,
            e -> refresh.doClick()
        );

        trackingTimer.start();

        return panel;
    }

    // ==================================================================
    // FEEDBACK
    // ==================================================================
    private JComponent buildFeedbackCard() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 8, 12, 8));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Share Your Feedback");
        heading.setFont(new Font("SansSerif", Font.BOLD, 24));
        heading.setForeground(TEXT_LIGHT);

        JLabel subtitle = new JLabel(
            "Help us improve your Dri-verse learning experience."
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_MUTED);

        top.add(heading);
        top.add(Box.createVerticalStrut(8));
        top.add(subtitle);

        panel.add(top, BorderLayout.NORTH);

        RoundedPanel form = new RoundedPanel(16, PANEL, PANEL_BORDER);
        form.setLayout(new GridBagLayout());
        form.setBorder(new EmptyBorder(24, 22, 24, 22));

        java.awt.GridBagConstraints gbc =
            new java.awt.GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;

        JLabel categoryLabel = new JLabel("Feedback Category");
        categoryLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        categoryLabel.setForeground(TEXT_LIGHT);

        String[] categories = {
            "Overall Experience",
            "Classes & Scheduling",
            "Instructors",
            "Mock Tests",
            "Payments",
            "Live Tracking",
            "Other"
        };

        FeedbackDropdown categoryBox =
            new FeedbackDropdown(categories);

        JLabel ratingLabel = new JLabel("Rate Your Experience");
        ratingLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        ratingLabel.setForeground(TEXT_LIGHT);

        String[] ratings = {
            "5 - Excellent",
            "4 - Very Good",
            "3 - Good",
            "2 - Fair",
            "1 - Poor"
        };

        FeedbackDropdown ratingBox =
            new FeedbackDropdown(ratings);

        JLabel commentsLabel = new JLabel("Comments");
        commentsLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        commentsLabel.setForeground(TEXT_LIGHT);

        javax.swing.JTextArea commentsArea =
            new javax.swing.JTextArea(5, 35);

        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);
        commentsArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        commentsArea.setOpaque(true);
        commentsArea.setBackground(CARD);
        commentsArea.setForeground(TEXT_LIGHT);
        commentsArea.setCaretColor(TEXT_LIGHT);
        commentsArea.setSelectionColor(ACCENT);
        commentsArea.setSelectedTextColor(Color.WHITE);
        commentsArea.setBorder(
            new EmptyBorder(10, 12, 10, 12)
        );

        JScrollPane commentsScroll =
            new JScrollPane(commentsArea);

        commentsScroll.setPreferredSize(
            new Dimension(500, 135)
        );
        commentsScroll.setMinimumSize(
            new Dimension(200, 135)
        );
        commentsScroll.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 135)
        );
        commentsScroll.setOpaque(true);
        commentsScroll.setBackground(CARD);
        commentsScroll.getViewport().setOpaque(true);
        commentsScroll.getViewport().setBackground(CARD);
        commentsScroll.setBorder(
            BorderFactory.createLineBorder(PANEL_BORDER)
        );

        JButton submitButton = pillButton(
            "Submit Feedback", ACCENT, Color.WHITE, true
        );

        gbc.gridy = 0;
        gbc.weighty = 0;
        gbc.insets = new java.awt.Insets(0, 0, 8, 0);
        form.add(categoryLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new java.awt.Insets(0, 0, 22, 0);
        form.add(categoryBox, gbc);

        gbc.gridy = 2;
        gbc.insets = new java.awt.Insets(0, 0, 8, 0);
        form.add(ratingLabel, gbc);

        gbc.gridy = 3;
        gbc.insets = new java.awt.Insets(0, 0, 22, 0);
        form.add(ratingBox, gbc);

        gbc.gridy = 4;
        gbc.insets = new java.awt.Insets(0, 0, 8, 0);
        form.add(commentsLabel, gbc);

        gbc.gridy = 5;
        gbc.insets = new java.awt.Insets(0, 0, 20, 0);
        form.add(commentsScroll, gbc);

        gbc.gridy = 6;
        gbc.fill = java.awt.GridBagConstraints.NONE;
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.weightx = 0;
        gbc.insets = new java.awt.Insets(0, 0, 0, 0);
        form.add(submitButton, gbc);

        JPanel spacer = new JPanel();
        spacer.setOpaque(false);

        gbc.gridy = 7;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        form.add(spacer, gbc);

        submitButton.addActionListener(e -> {
            String feedbackType = categoryBox.getSelectedValue();

            int rating = Integer.parseInt(
                ratingBox.getSelectedValue().substring(0, 1)
            );

            String comments = commentsArea.getText().trim();

            if (comments.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Please enter your feedback comments."
                );
                return;
            }

            String sql =
                "INSERT INTO learner_feedback "
                + "(learner_id, rating, feedback_type, comments) "
                + "VALUES (?, ?, ?, ?)";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, userId);
                ps.setInt(2, rating);
                ps.setString(3, feedbackType);
                ps.setString(4, comments);

                ps.executeUpdate();

                JOptionPane.showMessageDialog(
                    this,
                    "Thank you! Your feedback was submitted.",
                    "Feedback Submitted",
                    JOptionPane.INFORMATION_MESSAGE
                );

                commentsArea.setText("");

            } catch (SQLException ex) {
                ex.printStackTrace();

                JOptionPane.showMessageDialog(
                    this,
                    "Could not save your feedback:\n"
                        + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });

        panel.add(form, BorderLayout.CENTER);

        return panel;
    }

    /** Custom dark-themed dropdown used by the Feedback page. */
    private class FeedbackDropdown extends JPanel {

        private String selectedValue;
        private final JButton valueButton;

        FeedbackDropdown(String[] options) {
            super(new BorderLayout());

            if (options == null || options.length == 0) {
                throw new IllegalArgumentException(
                    "Dropdown options cannot be empty."
                );
            }

            selectedValue = options[0];

            setOpaque(true);
            setBackground(CARD);
            setBorder(
                BorderFactory.createLineBorder(PANEL_BORDER)
            );

            setPreferredSize(new Dimension(500, 38));
            setMinimumSize(new Dimension(180, 38));
            setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 38)
            );

            valueButton = createDropdownButton(selectedValue);
            valueButton.setHorizontalAlignment(SwingConstants.LEFT);
            valueButton.setBorder(
                new EmptyBorder(0, 12, 0, 8)
            );

            JButton arrowButton =
                createDropdownButton("\u25BE");

            arrowButton.setPreferredSize(
                new Dimension(38, 36)
            );

            valueButton.addActionListener(
                e -> showOptions(options)
            );

            arrowButton.addActionListener(
                e -> showOptions(options)
            );

            add(valueButton, BorderLayout.CENTER);
            add(arrowButton, BorderLayout.EAST);
        }

        private JButton createDropdownButton(String text) {
            JButton button = new JButton(text);

            button.setUI(
                new javax.swing.plaf.basic.BasicButtonUI()
            );

            button.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
            );
            button.setBackground(CARD);
            button.setForeground(TEXT_LIGHT);
            button.setOpaque(true);
            button.setContentAreaFilled(true);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setFocusable(false);
            button.setMargin(
                new java.awt.Insets(0, 0, 0, 0)
            );

            return button;
        }

        private void showOptions(String[] options) {
            JPopupMenu popup = new JPopupMenu();

            popup.setUI(
                new javax.swing.plaf.basic.BasicPopupMenuUI()
            );
            popup.setOpaque(true);
            popup.setBackground(PANEL);
            popup.setBorder(
                BorderFactory.createLineBorder(PANEL_BORDER)
            );

            int menuWidth = Math.max(
                260, Math.min(getWidth(), 460)
            );

            JPanel optionList = new JPanel();
            optionList.setOpaque(true);
            optionList.setBackground(PANEL);
            optionList.setLayout(
                new BoxLayout(optionList, BoxLayout.Y_AXIS)
            );

            for (String option : options) {
                JButton item = new JButton(option);

                item.setUI(
                    new javax.swing.plaf.basic.BasicButtonUI()
                );
                item.setFont(
                    new Font("SansSerif", Font.PLAIN, 13)
                );
                item.setHorizontalAlignment(
                    SwingConstants.LEFT
                );

                item.setForeground(TEXT_LIGHT);
                item.setBackground(
                    option.equals(selectedValue) ? ACCENT : CARD
                );

                item.setOpaque(true);
                item.setContentAreaFilled(true);
                item.setBorderPainted(false);
                item.setFocusPainted(false);
                item.setFocusable(false);
                item.setAlignmentX(Component.LEFT_ALIGNMENT);

                item.setBorder(
                    new EmptyBorder(9, 12, 9, 12)
                );

                item.setPreferredSize(
                    new Dimension(menuWidth, 36)
                );
                item.setMaximumSize(
                    new Dimension(menuWidth, 36)
                );

                item.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        item.setBackground(ACCENT);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        item.setBackground(
                            option.equals(selectedValue) ? ACCENT : CARD
                        );
                    }
                });

                item.addActionListener(e -> {
                    selectedValue = option;
                    valueButton.setText(selectedValue);
                    popup.setVisible(false);
                });

                optionList.add(item);
            }

            optionList.setPreferredSize(
                new Dimension(menuWidth, options.length * 36)
            );

            popup.add(optionList);
            popup.show(this, 0, getHeight());
        }

        String getSelectedValue() {
            return selectedValue;
        }
    }

    /** Haversine distance between two GPS points, in kilometres. */
    private double calculateDistanceKm(
            double lat1, double lon1,
            double lat2, double lon2) {

        double earthRadiusKm = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return earthRadiusKm * 2
                * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // ==================================================================
    // SETTINGS
    // ==================================================================
    private JComponent buildSettingsCard() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 8, 12, 8));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Account Settings");
        heading.setFont(new Font("SansSerif", Font.BOLD, 24));
        heading.setForeground(TEXT_LIGHT);

        JLabel subtitle = new JLabel(
            "Manage your personal information and account details."
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_MUTED);

        top.add(heading);
        top.add(Box.createVerticalStrut(8));
        top.add(subtitle);

        RoundedPanel form = new RoundedPanel(16, PANEL, PANEL_BORDER);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel nameLabel = new JLabel("First Name");
        JLabel emailLabel = new JLabel("Email Address");
        JLabel phoneLabel = new JLabel("Phone Number");

        JLabel[] labels = {nameLabel, emailLabel, phoneLabel};

        for (JLabel label : labels) {
            label.setFont(new Font("SansSerif", Font.BOLD, 13));
            label.setForeground(TEXT_LIGHT);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();

        JTextField[] fields = {
            nameField, emailField, phoneField
        };

        Dimension fieldSize = new Dimension(420, 38);

        for (JTextField field : fields) {
            field.setFont(new Font("SansSerif", Font.PLAIN, 14));
            field.setPreferredSize(fieldSize);
            field.setMaximumSize(fieldSize);
            field.setAlignmentX(Component.LEFT_ALIGNMENT);

            field.setOpaque(true);
            field.setBackground(CARD);
            field.setForeground(TEXT_LIGHT);
            field.setCaretColor(TEXT_LIGHT);
            field.setSelectionColor(ACCENT);
            field.setSelectedTextColor(Color.WHITE);

            field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PANEL_BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
        }

        JButton saveButton = pillButton(
            "Save Settings", ACCENT, Color.WHITE, true
        );
        saveButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        saveButton.addActionListener(e ->
            JOptionPane.showMessageDialog(
                this,
                "The Settings UI is ready. Saving changes to MySQL "
                    + "has not been connected yet.",
                "Account Settings",
                JOptionPane.INFORMATION_MESSAGE
            )
        );

        form.add(nameLabel);
        form.add(Box.createVerticalStrut(8));
        form.add(nameField);
        form.add(Box.createVerticalStrut(20));

        form.add(emailLabel);
        form.add(Box.createVerticalStrut(8));
        form.add(emailField);
        form.add(Box.createVerticalStrut(20));

        form.add(phoneLabel);
        form.add(Box.createVerticalStrut(8));
        form.add(phoneField);
        form.add(Box.createVerticalStrut(25));

        form.add(saveButton);

        panel.add(top, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);

        return panel;
    }

    // ==================================================================
    // UPGRADE PLAN
    // ==================================================================
    private JComponent buildUpgradePlanCard() {
        JPanel panel = new JPanel(new BorderLayout(0, 22));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 8, 12, 8));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Upgrade Your Learning Plan");
        heading.setFont(new Font("SansSerif", Font.BOLD, 24));
        heading.setForeground(TEXT_LIGHT);

        JLabel subtitle = new JLabel(
            "Explore additional features for your driving journey."
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_MUTED);

        top.add(heading);
        top.add(Box.createVerticalStrut(8));
        top.add(subtitle);

        RoundedPanel planCard = new RoundedPanel(
            18, PANEL, PANEL_BORDER
        );
        planCard.setLayout(new BoxLayout(planCard, BoxLayout.Y_AXIS));
        planCard.setBorder(new EmptyBorder(28, 30, 28, 30));
        planCard.setPreferredSize(new Dimension(500, 340));
        planCard.setMaximumSize(new Dimension(500, 340));
        planCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel planName = new JLabel("DRI-VERSE PREMIUM");
        planName.setFont(new Font("SansSerif", Font.BOLD, 21));
        planName.setForeground(ACCENT_2);
        planName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel price = new JLabel("Contact us for pricing");
        price.setFont(new Font("SansSerif", Font.BOLD, 17));
        price.setForeground(TEXT_LIGHT);
        price.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel feature1 = new JLabel("\u2713  Unlimited mock tests");
        JLabel feature2 = new JLabel("\u2713  Advanced learning progress");
        JLabel feature3 = new JLabel("\u2713  Driving practice resources");
        JLabel feature4 = new JLabel("\u2713  Priority support");

        JLabel[] features = {
            feature1, feature2, feature3, feature4
        };

        for (JLabel feature : features) {
            feature.setFont(new Font("SansSerif", Font.PLAIN, 13));
            feature.setForeground(TEXT_LIGHT);
            feature.setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        JButton upgradeButton = pillButton(
            "Upgrade Plan", ACCENT, Color.WHITE, true
        );
        upgradeButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        upgradeButton.addActionListener(e ->
            JOptionPane.showMessageDialog(
                this,
                "Premium upgrade is coming soon. "
                    + "Online payment is not available yet.",
                "Upgrade Plan",
                JOptionPane.INFORMATION_MESSAGE
            )
        );

        planCard.add(planName);
        planCard.add(Box.createVerticalStrut(14));
        planCard.add(price);
        planCard.add(Box.createVerticalStrut(24));
        planCard.add(feature1);
        planCard.add(Box.createVerticalStrut(14));
        planCard.add(feature2);
        planCard.add(Box.createVerticalStrut(14));
        planCard.add(feature3);
        planCard.add(Box.createVerticalStrut(14));
        planCard.add(feature4);
        planCard.add(Box.createVerticalStrut(25));
        planCard.add(upgradeButton);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(planCard);

        panel.add(top, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);

        return panel;
    }
}