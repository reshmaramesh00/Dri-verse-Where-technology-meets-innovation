package Dri_verse.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LoginFrame() {

        setTitle("Dri-verse: Where Technology Meets Innovation");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(15, 75, 160));
        panel.setPreferredSize(new Dimension(475, 650));

        JLabel titleLabel = new JLabel("DRI-VERSE", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 42));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Where Technology Meets Innovation", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("SansSerif", Font.ITALIC, 18));
        subtitleLabel.setForeground(Color.WHITE);

        JLabel taglineLabel = new JLabel("Drive today, lead tomorrow.", SwingConstants.CENTER);
        taglineLabel.setFont(new Font("SansSerif", Font.ITALIC, 18));
        taglineLabel.setForeground(Color.WHITE);

        ImageIcon carIcon = new ImageIcon(getClass().getResource("/images/car-logo.png"));

        JPanel leftContent = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                int panelWidth = getWidth();
                int panelHeight = getHeight();

                int imageWidth = carIcon.getIconWidth();
                int imageHeight = carIcon.getIconHeight();

                double scaleX = (double) panelWidth / imageWidth;
                double scaleY = (double) panelHeight / imageHeight;
                double scale = Math.max(scaleX, scaleY);

                int newWidth = (int) (imageWidth * scale);
                int newHeight = (int) (imageHeight * scale);

                int x = (panelWidth - newWidth) / 2;
                int y = (panelHeight - newHeight) / 2;

                g.drawImage(carIcon.getImage(), x, y, newWidth, newHeight, this);
            }
        };

        leftContent.setBackground(new Color(15, 75, 160));
        panel.add(leftContent, BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridLayout(10, 1, 10, 3));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(80, 60, 80, 60));

        JLabel welcomeLabel = new JLabel("Welcome Back!");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        welcomeLabel.setForeground(new Color(15, 75, 160));
        formPanel.add(welcomeLabel);

        // ROLE BUTTONS (Learner / Instructor / Admin)
        JRadioButton learnerButton = new JRadioButton("Learner");
        JRadioButton instructorButton = new JRadioButton("Instructor");
        JRadioButton adminButton = new JRadioButton("Admin");

        for (JRadioButton rb : new JRadioButton[]{learnerButton, instructorButton, adminButton}) {
            rb.setBackground(Color.WHITE);
            rb.setForeground(new Color(15, 75, 160));
        }

        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(learnerButton);
        roleGroup.add(instructorButton);
        roleGroup.add(adminButton);

        learnerButton.setSelected(true);

        // EMAIL
        JPanel emailPanel = new JPanel(new BorderLayout(10, 0));
        emailPanel.setBackground(Color.WHITE);

        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        emailLabel.setPreferredSize(new Dimension(80, 40));

        emailField = new JTextField();
        emailField.setPreferredSize(new Dimension(300, 40));
        emailField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        emailPanel.add(emailLabel, BorderLayout.WEST);
        emailPanel.add(emailField, BorderLayout.CENTER);

        formPanel.add(emailPanel);

        // PASSWORD
        JPanel passwordPanel = new JPanel(new BorderLayout(10, 0));
        passwordPanel.setBackground(Color.WHITE);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        passwordLabel.setPreferredSize(new Dimension(80, 40));

        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(300, 40));
        passwordField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        passwordPanel.add(passwordLabel, BorderLayout.WEST);
        passwordPanel.add(passwordField, BorderLayout.CENTER);

        // ENTER KEY NAVIGATION
        emailField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    passwordField.requestFocus();
                }
            }
        });

        passwordField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    loginButton.requestFocus();
                }
            }
        });

        formPanel.add(passwordPanel);

        JCheckBox rememberMe = new JCheckBox("Remember Me");
        JButton forgotPasswordButton = new JButton("Forgot Password?");
        forgotPasswordButton.addActionListener(e -> {
            new ForgotPasswordFrame();
            dispose();
        });

        rememberMe.setBackground(Color.WHITE);
        forgotPasswordButton.setBackground(Color.WHITE);
        forgotPasswordButton.setForeground(new Color(15, 75, 160));
        forgotPasswordButton.setBorderPainted(false);
        forgotPasswordButton.setFocusPainted(false);
        forgotPasswordButton.setContentAreaFilled(false);

        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        optionsPanel.setBackground(Color.WHITE);

        optionsPanel.add(rememberMe);
        optionsPanel.add(forgotPasswordButton);

        formPanel.add(optionsPanel);

        loginButton = new JButton("LOGIN");
        loginButton.setBackground(new Color(15, 75, 160));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.setFocusPainted(false);
        loginButton.setOpaque(true);
        loginButton.setBorderPainted(false);

        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rolePanel.setBackground(Color.WHITE);

        rolePanel.add(learnerButton);
        rolePanel.add(instructorButton);
        rolePanel.add(adminButton);

        formPanel.add(rolePanel);

        formPanel.add(loginButton);

        loginButton.addActionListener(e -> {

            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            String json = String.format(
                    "{\"email\":\"%s\",\"password\":\"%s\"}",
                    email, password
            );

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            try {
                HttpResponse<String> response =
                        client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {

                    String body = response.body();
                    int userId = extractUserId(body);

                    if (adminButton.isSelected()) {
                        // never trust the radio button: check the role stored in the database
                        if (!isAdmin(userId)) {
                            JOptionPane.showMessageDialog(this,
                                    "This account is not an administrator.");
                            return;
                        }
                        new AdminDashboard(userId).setVisible(true);
                    } else {
                        new LoadingFrame(userId, instructorButton.isSelected()).setVisible(true);
                    }
                    dispose();

                } else {
                    JOptionPane.showMessageDialog(this, response.body());
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not connect to server.");
            }
        });

        registerButton = new JButton("REGISTER");
        registerButton.setBackground(Color.WHITE);
        registerButton.setForeground(new Color(15, 75, 160));
        registerButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        registerButton.setFocusPainted(false);

        formPanel.add(registerButton);

        registerButton.addActionListener(e -> {
            new RegistrationFrame();
            dispose();
        });

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.add(formPanel, BorderLayout.CENTER);

        JPanel backgroundPanel = new JPanel(new GridLayout(1, 2));
        backgroundPanel.setBackground(Color.WHITE);

        backgroundPanel.add(panel);
        backgroundPanel.add(rightPanel);

        add(backgroundPanel);

        setVisible(true);
    }

    /** True only if users.role for this account is ADMIN (case-insensitive). */
    private boolean isAdmin(int userId) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT role FROM users WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && "ADMIN".equalsIgnoreCase(rs.getString(1));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Crude manual JSON extraction for the userId field.
     * Expects a response body like: {"userId":12,"message":"Login successful"}
     * Consider switching to a real JSON library (org.json, Gson, Jackson)
     * for anything more complex.
     */
    private int extractUserId(String json) {
        String key = "\"userId\":";
        int start = json.indexOf(key);
        if (start == -1) {
            throw new IllegalArgumentException("userId not found in response: " + json);
        }
        start += key.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        return Integer.parseInt(json.substring(start, end).trim());
    }

    public static void main(String[] args) {
        new LoginFrame();
    }
}