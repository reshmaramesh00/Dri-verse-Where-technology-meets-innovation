package Dri_verse.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class ForgotPasswordFrame extends JFrame {

    private JTextField emailField;
    private JTextField verificationCodeField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    public ForgotPasswordFrame() {

        setTitle("Dri-verse: Where Technology Meets Innovation");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);

        // ================= LEFT BLUE PANEL =================

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

        JPanel leftContent = new JPanel();

        leftContent.setLayout(
                new javax.swing.BoxLayout(
                        leftContent,
                        javax.swing.BoxLayout.Y_AXIS
                )
        );

        leftContent.setBackground(
                new Color(15, 75, 160)
        );

        titleLabel.setAlignmentX(
                java.awt.Component.CENTER_ALIGNMENT
        );

        subtitleLabel.setAlignmentX(
                java.awt.Component.CENTER_ALIGNMENT
        );

        taglineLabel.setAlignmentX(
                java.awt.Component.CENTER_ALIGNMENT
        );

        leftContent.add(
                javax.swing.Box.createVerticalStrut(70)
        );

        leftContent.add(titleLabel);

        leftContent.add(
                javax.swing.Box.createVerticalStrut(10)
        );

        leftContent.add(subtitleLabel);

        leftContent.add(
                javax.swing.Box.createVerticalStrut(50)
        );

        leftContent.add(
                javax.swing.Box.createVerticalGlue()
        );

        leftContent.add(taglineLabel);

        leftContent.add(
                javax.swing.Box.createVerticalStrut(25)
        );

        panel.add(
                leftContent,
                BorderLayout.CENTER
        );


        // ================= RIGHT PANEL =================

        JPanel rightPanel = new JPanel(
                new BorderLayout()
        );

        rightPanel.setBackground(Color.WHITE);

        // ================= FORM =================

        JPanel formPanel = new JPanel(
                new GridBagLayout()
        );

        formPanel.setBackground(Color.WHITE);

        formPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        50, 45, 50, 45
                )
        );

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(
                8, 8, 8, 8
        );

        // ================= TITLE =================

        JLabel forgotTitle = new JLabel(
                "Forgot Password",
                SwingConstants.CENTER
        );

        forgotTitle.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        30
                )
        );

        forgotTitle.setForeground(
                new Color(15, 75, 160)
        );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        formPanel.add(
                forgotTitle,
                gbc
        );


        // ================= EMAIL =================

        JLabel emailLabel = new JLabel("Email");

        emailLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        14
                )
        );

        emailField = new JTextField();

        emailField.setPreferredSize(
                new Dimension(300, 40)
        );

        emailField.setBorder(
                BorderFactory.createLineBorder(
                        new Color(200, 200, 200)
                )
        );

        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 1;

        formPanel.add(
                emailLabel,
                gbc
        );

        gbc.gridx = 1;

        formPanel.add(
                emailField,
                gbc
        );


        // ================= VERIFICATION CODE =================

        JLabel verificationLabel =
                new JLabel("Verification Code");

        verificationLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        14
                )
        );

        verificationCodeField =
                new JTextField();

        verificationCodeField.setPreferredSize(
                new Dimension(300, 40)
        );

        verificationCodeField.setBorder(
                BorderFactory.createLineBorder(
                        new Color(200, 200, 200)
                )
        );

        gbc.gridx = 0;
        gbc.gridy = 2;

        formPanel.add(
                verificationLabel,
                gbc
        );

        gbc.gridx = 1;

        formPanel.add(
                verificationCodeField,
                gbc
        );


        // ================= NEW PASSWORD =================

        JLabel newPasswordLabel =
                new JLabel("New Password");

        newPasswordLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        14
                )
        );

        newPasswordField =
                new JPasswordField();

        newPasswordField.setPreferredSize(
                new Dimension(300, 40)
        );

        newPasswordField.setBorder(
                BorderFactory.createLineBorder(
                        new Color(200, 200, 200)
                )
        );

        gbc.gridx = 0;
        gbc.gridy = 3;

        formPanel.add(
                newPasswordLabel,
                gbc
        );

        gbc.gridx = 1;

        formPanel.add(
                newPasswordField,
                gbc
        );


        // ================= CONFIRM PASSWORD =================

        JLabel confirmPasswordLabel =
                new JLabel("Confirm Password");

        confirmPasswordLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        14
                )
        );

        confirmPasswordField =
                new JPasswordField();

        confirmPasswordField.setPreferredSize(
                new Dimension(300, 40)
        );

        confirmPasswordField.setBorder(
                BorderFactory.createLineBorder(
                        new Color(200, 200, 200)
                )
        );

        gbc.gridx = 0;
        gbc.gridy = 4;

        formPanel.add(
                confirmPasswordLabel,
                gbc
        );

        gbc.gridx = 1;

        formPanel.add(
                confirmPasswordField,
                gbc
        );


        // ================= BUTTONS =================

        JButton sendCodeButton =
                new JButton("SEND VERIFICATION CODE");

        sendCodeButton.setPreferredSize(
                new Dimension(220, 40)
        );

        sendCodeButton.setBackground(
                new Color(15, 75, 160)
        );

        sendCodeButton.setForeground(
                Color.WHITE
        );

        sendCodeButton.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        11
                )
        );

        sendCodeButton.setOpaque(true);
        sendCodeButton.setBorderPainted(false);
        sendCodeButton.setFocusPainted(false);


        JButton resetButton =
                new JButton("RESET PASSWORD");

        resetButton.setPreferredSize(
                new Dimension(150, 40)
        );

        resetButton.setBackground(
                new Color(15, 75, 160)
        );

        resetButton.setForeground(
                Color.WHITE
        );

        resetButton.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        11
                )
        );

        resetButton.setOpaque(true);
        resetButton.setBorderPainted(false);
        resetButton.setFocusPainted(false);


        JButton backToLoginButton =
                new JButton("BACK TO LOGIN");

        backToLoginButton.setPreferredSize(
                new Dimension(150, 40)
        );

        backToLoginButton.setBackground(
                Color.WHITE
        );

        backToLoginButton.setForeground(
                new Color(15, 75, 160)
        );

        backToLoginButton.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        11
                )
        );

        backToLoginButton.setFocusPainted(false);


        // Put all 3 buttons in ONE row

        JPanel buttonPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER,
                                8,
                                5
                        )
                );

        buttonPanel.setBackground(Color.WHITE);

        buttonPanel.add(sendCodeButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(backToLoginButton);


        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;

        formPanel.add(
                buttonPanel,
                gbc
        );


        // ================= BACK TO LOGIN =================

        backToLoginButton.addActionListener(e -> {
            new LoginFrame();
            dispose();
        });


        // ================= ADD FORM =================

        rightPanel.add(
                formPanel,
                BorderLayout.CENTER
        );


        // ================= SPLIT SCREEN =================

        JPanel backgroundPanel =
                new JPanel(
                        new GridLayout(1, 2)
                );

        backgroundPanel.setBackground(Color.WHITE);

        backgroundPanel.add(panel);
        backgroundPanel.add(rightPanel);

        add(backgroundPanel);

        setVisible(true);
    }
}