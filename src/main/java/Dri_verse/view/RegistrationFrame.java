package Dri_verse.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;


public class RegistrationFrame extends JFrame {

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField phoneField;
    private JButton registerButton;

    public RegistrationFrame() {

        setTitle("Dri-verse: Where Technology Meets Innovation");
setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        JPanel panel = new JPanel(new BorderLayout());
panel.setBackground(new Color(15, 75, 160));
panel.setPreferredSize(new Dimension(475, 650));

        JLabel titleLabel = new JLabel(
        "DRI-VERSE",
        SwingConstants.CENTER
);
titleLabel.setFont(
        new Font("SansSerif", Font.BOLD, 42)
);
titleLabel.setForeground(Color.WHITE);

JLabel subtitleLabel = new JLabel(
        "Where Technology Meets Innovation",
        SwingConstants.CENTER
);
subtitleLabel.setFont(
        new Font("SansSerif", Font.ITALIC, 18)
);
subtitleLabel.setForeground(Color.WHITE);

JLabel taglineLabel = new JLabel(
        "Drive today, lead tomorrow.",
        SwingConstants.CENTER
);
taglineLabel.setFont(
        new Font("SansSerif", Font.ITALIC, 18)
);
taglineLabel.setForeground(Color.WHITE);

ImageIcon carIcon = new ImageIcon(
        getClass().getResource("/images/car-logo.png")
);

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

        g.drawImage(
                carIcon.getImage(),
                x,
                y,
                newWidth,
                newHeight,
                this
        );
    }
};

leftContent.setBackground(new Color(15, 75, 160));

panel.add(leftContent, BorderLayout.CENTER);
JPanel formPanel = new JPanel(new GridBagLayout());
formPanel.setBackground(Color.WHITE);
formPanel.setBorder(
        BorderFactory.createEmptyBorder(45, 60, 45, 60)
);

GridBagConstraints gbc = new GridBagConstraints();
gbc.insets = new java.awt.Insets(8, 5, 8, 5);
gbc.fill = GridBagConstraints.HORIZONTAL;

JLabel headingLabel = new JLabel("Create Account");
headingLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
headingLabel.setForeground(new Color(15, 75, 160));

gbc.gridx = 0;
gbc.gridy = 0;
gbc.gridwidth = 2;
gbc.anchor = GridBagConstraints.CENTER;
formPanel.add(headingLabel, gbc);

gbc.gridwidth = 1;
gbc.anchor = GridBagConstraints.WEST;

// FIRST NAME
JLabel firstNameLabel = new JLabel("First Name *");
firstNameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

firstNameField = new JTextField();
firstNameField.setPreferredSize(new Dimension(250, 40));
firstNameField.setBorder(
        BorderFactory.createLineBorder(new Color(200, 200, 200))
);

gbc.gridx = 0;
gbc.gridy = 1;
formPanel.add(firstNameLabel, gbc);

gbc.gridx = 1;
formPanel.add(firstNameField, gbc);


// LAST NAME
JLabel lastNameLabel = new JLabel("Last Name *");
lastNameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

lastNameField = new JTextField();
lastNameField.setPreferredSize(new Dimension(250, 40));
lastNameField.setBorder(
        BorderFactory.createLineBorder(new Color(200, 200, 200))
);

gbc.gridx = 0;
gbc.gridy = 2;
formPanel.add(lastNameLabel, gbc);

gbc.gridx = 1;
formPanel.add(lastNameField, gbc);

// EMAIL
JLabel emailLabel = new JLabel("Email *");
emailLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

emailField = new JTextField();
emailField.setPreferredSize(new Dimension(250, 40));
emailField.setBorder(
        BorderFactory.createLineBorder(new Color(200, 200, 200))
);

gbc.gridx = 0;
gbc.gridy = 3;
formPanel.add(emailLabel, gbc);

gbc.gridx = 1;
formPanel.add(emailField, gbc);

// PASSWORD
JLabel passwordLabel = new JLabel("Password *");
passwordLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

passwordField = new JPasswordField();
passwordField.setPreferredSize(new Dimension(210, 40));
passwordField.setBorder(
        BorderFactory.createLineBorder(new Color(200, 200, 200))
);

JButton showPasswordButton = new JButton("Show");
showPasswordButton.setPreferredSize(new Dimension(60, 40));
showPasswordButton.setFocusPainted(false);

JPanel passwordPanel = new JPanel(new BorderLayout());
passwordPanel.setBackground(Color.WHITE);
passwordPanel.add(passwordField, BorderLayout.CENTER);
passwordPanel.add(showPasswordButton, BorderLayout.EAST);

showPasswordButton.addActionListener(e -> {
    if (passwordField.getEchoChar() == (char) 0) {
        passwordField.setEchoChar('•');
        showPasswordButton.setText("Show");
    } else {
        passwordField.setEchoChar((char) 0);
        showPasswordButton.setText("Hide");
    }
});

gbc.gridx = 0;
gbc.gridy = 4;
formPanel.add(passwordLabel, gbc);

gbc.gridx = 1;
formPanel.add(passwordPanel, gbc);
// CONFIRM PASSWORD
JLabel confirmPasswordLabel = new JLabel("Confirm Password *");
confirmPasswordLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

confirmPasswordField = new JPasswordField();
confirmPasswordField.setPreferredSize(new Dimension(210, 40));
confirmPasswordField.setBorder(
        BorderFactory.createLineBorder(new Color(200, 200, 200))
);

JButton showConfirmPasswordButton = new JButton("Show");
showConfirmPasswordButton.setPreferredSize(new Dimension(60, 40));
showConfirmPasswordButton.setFocusPainted(false);

JPanel confirmPasswordPanel = new JPanel(new BorderLayout());
confirmPasswordPanel.setBackground(Color.WHITE);
confirmPasswordPanel.add(confirmPasswordField, BorderLayout.CENTER);
confirmPasswordPanel.add(showConfirmPasswordButton, BorderLayout.EAST);

showConfirmPasswordButton.addActionListener(e -> {
    if (confirmPasswordField.getEchoChar() == (char) 0) {
        confirmPasswordField.setEchoChar('•');
        showConfirmPasswordButton.setText("Show");
    } else {
        confirmPasswordField.setEchoChar((char) 0);
        showConfirmPasswordButton.setText("Hide");
    }
});

gbc.gridx = 0;
gbc.gridy = 5;
formPanel.add(confirmPasswordLabel, gbc);

gbc.gridx = 1;
formPanel.add(confirmPasswordPanel, gbc);


// PHONE
JLabel phoneLabel = new JLabel("Phone *");
phoneLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

phoneField = new JTextField();
phoneField.setPreferredSize(new Dimension(250, 40));
phoneField.setBorder(
        BorderFactory.createLineBorder(new Color(200, 200, 200))
);

gbc.gridx = 0;
gbc.gridy = 6;
formPanel.add(phoneLabel, gbc);

gbc.gridx = 1;
formPanel.add(phoneField, gbc);

// ENTER KEY NAVIGATION

firstNameField.addKeyListener(new java.awt.event.KeyAdapter() {
    @Override
    public void keyPressed(java.awt.event.KeyEvent e) {
        if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            lastNameField.requestFocus();
        }
    }
});

lastNameField.addKeyListener(new java.awt.event.KeyAdapter() {
    @Override
    public void keyPressed(java.awt.event.KeyEvent e) {
        if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            emailField.requestFocus();
        }
    }
});

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
            confirmPasswordField.requestFocus();
        }
    }
});

confirmPasswordField.addKeyListener(new java.awt.event.KeyAdapter() {
    @Override
    public void keyPressed(java.awt.event.KeyEvent e) {
        if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            phoneField.requestFocus();
        }
    }
});

phoneField.addKeyListener(new java.awt.event.KeyAdapter() {
    @Override
    public void keyPressed(java.awt.event.KeyEvent e) {
        if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            registerButton.requestFocus();
        }
    }
});

       
// ROLE
JLabel roleLabel = new JLabel("Role *");
roleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

JPanel rolePanel = new JPanel(
        new java.awt.FlowLayout(
                java.awt.FlowLayout.LEFT, 5, 0
        )
);
rolePanel.setBackground(Color.WHITE);

JRadioButton learnerButton = new JRadioButton("Learner");
JRadioButton instructorButton = new JRadioButton("Instructor");

learnerButton.setBackground(Color.WHITE);
instructorButton.setBackground(Color.WHITE);

ButtonGroup roleGroup = new ButtonGroup();
roleGroup.add(learnerButton);
roleGroup.add(instructorButton);

learnerButton.setSelected(true);

rolePanel.add(learnerButton);
rolePanel.add(instructorButton);

gbc.gridx = 0;
gbc.gridy = 7;
formPanel.add(roleLabel, gbc);

gbc.gridx = 1;
formPanel.add(rolePanel, gbc);

 // REGISTER BUTTON
registerButton = new JButton("REGISTER");
registerButton.setPreferredSize(new Dimension(150, 40));
registerButton.setBackground(new Color(15, 75, 160));
registerButton.setForeground(Color.WHITE);
registerButton.setFont(new Font("SansSerif", Font.BOLD, 14));
registerButton.setFocusPainted(false);
registerButton.setOpaque(true);
registerButton.setBorderPainted(false);

gbc.gridx = 0;
gbc.gridy = 8;
gbc.gridwidth = 2;
gbc.anchor = GridBagConstraints.CENTER;
gbc.fill = GridBagConstraints.NONE;

formPanel.add(registerButton, gbc);

        registerButton.addActionListener(e -> {

String firstName = firstNameField.getText().trim();
String lastName = lastNameField.getText().trim();
String email = emailField.getText().trim();
String password = new String(passwordField.getPassword());
String confirmPassword = new String(confirmPasswordField.getPassword());
String phone = phoneField.getText().trim();
String role = learnerButton.isSelected() ? "STUDENT" : "INSTRUCTOR";

// Check mandatory fields
if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()
        || password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty()) {

    JOptionPane.showMessageDialog(
            this,
            "Please fill in all mandatory fields."
    );
    return;
}


// Check first name
if (!firstName.matches("^[A-Z][a-z]*$")) {

    JOptionPane.showMessageDialog(
            this,
            "First name must begin with a capital letter and contain only letters."
    );
    return;
}


// Check last name
if (!lastName.matches("^[A-Z][a-z]*$")) {

    JOptionPane.showMessageDialog(
            this,
            "Last name must begin with a capital letter and contain only letters."
    );
    return;
}

// Check email format
if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

    JOptionPane.showMessageDialog(
            this,
            "Incorrect email format.\nPlease use: username@domain.extension"
    );
    return;
}

// Check phone number
if (!phone.matches("\\d{10}")) {

    JOptionPane.showMessageDialog(
            this,
            "Phone number must contain exactly 10 digits."
    );
    return;
}

// Check password strength
if (!password.matches(".*[A-Z].*")
        || !password.matches(".*[a-z].*")
        || !password.matches(".*\\d.*")
        || !password.matches(".*[^A-Za-z0-9].*")
        || password.length() < 8) {

    JOptionPane.showMessageDialog(
            this,
            "Password must be at least 8 characters and contain:\n"
            + "• One uppercase letter\n"
            + "• One lowercase letter\n"
            + "• One number\n"
            + "• One special character"
    );
    return;
}

// Check confirm password
if (!password.equals(confirmPassword)) {

    JOptionPane.showMessageDialog(
            this,
            "Passwords do not match."
    );
    return;
}

    String json = String.format(
        "{\"firstName\":\"%s\",\"lastName\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"phone\":\"%s\",\"role\":\"%s\"}",
        firstName, lastName, email, password, phone, role
);

    HttpClient client = HttpClient.newHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/register"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

    try {
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        JOptionPane.showMessageDialog(
                this,
                response.body()
        );

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
                this,
                "Could not connect to server."
        );
    }
});

// LOGIN BUTTON
JButton loginButton = new JButton("LOGIN");
loginButton.setPreferredSize(new Dimension(150, 40));
loginButton.setBackground(Color.WHITE);
loginButton.setForeground(new Color(15, 75, 160));
loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
loginButton.setFocusPainted(false);

gbc.gridx = 0;
gbc.gridy = 9;
gbc.gridwidth = 2;
gbc.anchor = GridBagConstraints.CENTER;
gbc.fill = GridBagConstraints.NONE;

formPanel.add(loginButton, gbc);

loginButton.addActionListener(e -> {
    new LoginFrame();
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

    public static void main(String[] args) {
        new RegistrationFrame();
    }
}
