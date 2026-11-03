package Dri_verse.view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;


/**
 * Cyan wireframe "car diving into a track + drift" loading screen shown
 * between login success and the dashboard opening. Purely cosmetic - runs
 * a timed animation, loads the dashboard data on a background thread, then
 * swaps itself for the dashboard once both are done.
 */
public class LoadingFrame extends JFrame {

    private static final Color BG_TOP    = new Color(0x02030A);
    private static final Color BG_BOTTOM = new Color(0x030812);
    private static final Color CYAN      = new Color(0x22E5FF);
    private static final Color CYAN_DIM  = new Color(0x0FA3C7);
    private static final Color WHITE_HOT = new Color(0xE8FEFF);

    // Total animation timeline in ms
    private static final long DRIVE_IN_MS = 2000; // far -> close
    private static final long DRIFT_MS    = 1100; // sideways drift kick
    private static final long TOTAL_MS    = DRIVE_IN_MS + DRIFT_MS;

    private final long startTime = System.currentTimeMillis();
    private Timer animTimer;                 // no longer final (used inside its own lambda)
    private final CarDriftPanel panel;
    private final int currentUserId;

    private volatile boolean dataReady = false;
    private volatile boolean animDone = false;
    private boolean finished = false;        // guard: open the dashboard only once
    private final boolean instructor;

    public LoadingFrame(int userId) {
        this(userId, false);
    }

    public LoadingFrame(int userId, boolean instructor) {
        this.currentUserId = userId;
        this.instructor = instructor;

        setTitle("Dri-verse");
        setUndecorated(true);
        setSize(980, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        panel = new CarDriftPanel();
        panel.setLayout(new BorderLayout());

        JLabel title = new JLabel("DRI-VERSE", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(36, 0, 0, 0));

        JLabel status = new JLabel("Loading your dashboard...", SwingConstants.CENTER);
        status.setFont(new Font("SansSerif", Font.PLAIN, 13));
        status.setForeground(new Color(0x8FE9FF));
        status.setBorder(BorderFactory.createEmptyBorder(0, 0, 42, 0));

        panel.add(title, BorderLayout.NORTH);
        panel.add(status, BorderLayout.SOUTH);
        setContentPane(panel);

        animTimer = new Timer(16, e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= TOTAL_MS) {
                animTimer.stop();            // stop the 16 ms ticking
                panel.setElapsed(TOTAL_MS);
                panel.repaint();
                animDone = true;
                tryFinish();
            } else {
                panel.setElapsed(elapsed);
                panel.repaint();
            }
        });
        animTimer.start();

        SwingWorker<DashboardDAO.DashboardData, Void> worker = new SwingWorker<>() {
            @Override protected DashboardDAO.DashboardData doInBackground() {
                if (instructor) {
                    new InstructorDAO().loadForUser(userId); // warm up the DB connection
                    return null;
                }
                return new DashboardDAO().loadForUser(userId);
            }

            @Override protected void done() {
                try {
                    get();
                } catch (Exception ignored) {
                }
                dataReady = true;
                tryFinish();
            }
        };
        worker.execute();
    }

    /** Only transition once BOTH the animation has played out and data has loaded. */
    private void tryFinish() {
        if (finished) return;                  // never run twice
        if (!(animDone && dataReady)) return;  // wait for BOTH
        finished = true;

        if (instructor) {
            new InstructorDashboard(currentUserId).setVisible(true);
        } else {
            new LearnerDashboard(currentUserId).setVisible(true);
        }
        dispose();                             // close the loading screen
    }

    // ==================================================================
    // Painted panel: perspective grid + wireframe car driving in + drift
    // ==================================================================
    private static class CarDriftPanel extends JPanel {
        private long elapsed = 0;

        CarDriftPanel() { setOpaque(true); }

        void setElapsed(long e) { this.elapsed = e; }

        private float easeOutCubic(float t) {
            float f = t - 1f;
            return f * f * f + 1f;
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            int w = getWidth(), h = getHeight();

            // ---- Background gradient ----
            g2.setPaint(new GradientPaint(0, 0, BG_TOP, 0, h, BG_BOTTOM));
            g2.fillRect(0, 0, w, h);

            int horizonY = (int) (h * 0.42);
            int vpX = w / 2;

            // ---- Perspective grid floor ----
            drawGrid(g2, w, h, vpX, horizonY);

            // ---- Timeline progress ----
            float driveT = Math.min(1f, elapsed / (float) DRIVE_IN_MS);
            float driftT = elapsed <= DRIVE_IN_MS ? 0f
                    : Math.min(1f, (elapsed - DRIVE_IN_MS) / (float) DRIFT_MS);

            float eDrive = easeOutCubic(driveT);

            // Car travels from near the vanishing point (small) down toward
            // the lower-center of the panel (large), i.e. "driving toward camera".
            float carY = horizonY + eDrive * (h * 0.86f - horizonY);
            float scale = 0.08f + eDrive * 0.92f;

            // Drift phase: rotate + slide sideways
            float driftAngle = (float) Math.toRadians(driftT * -26f);
            float driftSlideX = driftT * (w * 0.16f);
            float carX = vpX + driftSlideX;

            drawReflection(g2, carX, carY, scale, driftAngle, w, h);
            drawCar(g2, carX, carY, scale, driftAngle);

            if (driftT > 0.02f) {
                drawDriftSmoke(g2, carX, carY, scale, driftT);
            }

            g2.dispose();
        }

        private void drawGrid(Graphics2D g2, int w, int h, int vpX, int horizonY) {
            g2.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 70));
            g2.setStroke(new BasicStroke(1.2f));

            // Horizon glow line
            g2.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 140));
            g2.drawLine(0, horizonY, w, horizonY);

            // Vertical converging lines
            g2.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 55));
            int spanBottom = (int) (w * 1.4);
            int lines = 14;
            for (int i = -lines; i <= lines; i++) {
                int bottomX = vpX + i * (spanBottom / (2 * lines));
                g2.drawLine(vpX, horizonY, bottomX, h);
            }

            // Horizontal floor lines with perspective spacing (denser near horizon)
            int rows = 10;
            for (int r = 1; r <= rows; r++) {
                float t = r / (float) rows;
                float easedT = t * t; // bunch lines near horizon
                int y = (int) (horizonY + easedT * (h - horizonY));
                int alpha = (int) (110 * (1f - t) + 20);
                g2.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), Math.max(10, alpha)));
                g2.drawLine(0, y, w, y);
            }
        }

        /** Builds the car silhouette as a GeneralPath in local units (~240 x 80, centered). */
        private GeneralPath buildCarPath() {
            GeneralPath p = new GeneralPath();
            // Roofline + hood + trunk profile (low sports coupe)
            p.moveTo(-118, 6);
            p.curveTo(-118, -6, -100, -10, -86, -10);
            p.curveTo(-70, -34, -40, -46, -6, -46);
            p.curveTo(24, -46, 48, -34, 62, -12);
            p.curveTo(86, -10, 108, -4, 118, 6);
            p.curveTo(118, 16, 110, 22, 96, 22);
            p.curveTo(70, 30, 30, 33, -20, 33);
            p.curveTo(-60, 33, -96, 28, -112, 18);
            p.curveTo(-118, 15, -118, 10, -118, 6);
            p.closePath();

            // Windshield/window line
            p.moveTo(-70, -10);
            p.curveTo(-56, -30, -34, -40, -6, -40);
            p.curveTo(16, -40, 34, -32, 48, -14);

            // Belt line accent
            p.moveTo(-110, 8);
            p.lineTo(108, 8);

            return p;
        }

        private void drawCar(Graphics2D g2, float cx, float cy, float scale, float angle) {
            GeneralPath carPath = buildCarPath();

            AffineTransform tx = new AffineTransform();
            tx.translate(cx, cy);
            tx.rotate(angle);
            tx.scale(scale, scale);

            Shape transformed = tx.createTransformedShape(carPath);

            // Glow passes (wide, low alpha -> narrow, bright)
            int[] widths = {10, 6, 3};
            int[] alphas = {40, 90, 210};
            for (int i = 0; i < widths.length; i++) {
                g2.setStroke(new BasicStroke(widths[i] * Math.max(scale, 0.15f),
                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), alphas[i]));
                g2.draw(transformed);
            }
            g2.setStroke(new BasicStroke(1.2f * Math.max(scale, 0.2f)));
            g2.setColor(WHITE_HOT);
            g2.draw(transformed);

            // Wheels
            drawWheel(g2, tx, -78, 28, scale);
            drawWheel(g2, tx, 66, 28, scale);
        }

        private void drawWheel(Graphics2D g2, AffineTransform carTx, double localX, double localY, float scale) {
            double r = 20;
            Ellipse2D wheel = new Ellipse2D.Double(localX - r, localY - r, r * 2, r * 2);
            Shape transformed = carTx.createTransformedShape(wheel);

            g2.setStroke(new BasicStroke(5f * Math.max(scale, 0.15f)));
            g2.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 60));
            g2.draw(transformed);

            g2.setStroke(new BasicStroke(2f * Math.max(scale, 0.2f)));
            g2.setColor(CYAN_DIM);
            g2.draw(transformed);
        }

        private void drawReflection(Graphics2D g2, float cx, float cy, float scale, float angle, int w, int h) {
            AffineTransform tx = new AffineTransform();
            tx.translate(cx, cy + 10 * scale);
            tx.scale(1, -0.35);
            tx.rotate(angle);
            tx.scale(scale, scale);

            GeneralPath carPath = buildCarPath();
            Shape reflected = tx.createTransformedShape(carPath);

            g2.setStroke(new BasicStroke(4f * Math.max(scale, 0.15f)));
            g2.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 45));
            g2.draw(reflected);
        }

        private void drawDriftSmoke(Graphics2D g2, float cx, float cy, float scale, float driftT) {
            // A couple of curved smoke/skid arcs trailing behind the rear wheel
            float alpha = Math.min(1f, driftT) * 0.65f;
            g2.setStroke(new BasicStroke(3.5f * scale, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            for (int i = 0; i < 3; i++) {
                float spread = i * 14f;
                Arc2D arc = new Arc2D.Float(
                        cx - 150 * scale - spread, cy - 10,
                        90 * scale, 46 * scale,
                        200, 90 + driftT * 40, Arc2D.OPEN);
                int a = (int) (alpha * (255 - i * 60));
                g2.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), Math.max(0, a)));
                g2.draw(arc);
            }
        }
    }
}